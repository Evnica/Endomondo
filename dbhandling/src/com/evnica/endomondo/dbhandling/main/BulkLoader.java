package com.evnica.endomondo.dbhandling.main;

import com.evnica.endomondo.main.connect.DbConnector;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Project: Endomondo
 * Class: BulkLoader
 * Version: 0.1
 * Created on 4/4/2017 with the help of IntelliJ IDEA (thanks!)
 * Author: DS
 * Description:
 */
class BulkLoader
{

    private static Connection connection;
    private final static Logger LOGGER =
            org.apache.logging.log4j.LogManager.getLogger(BulkLoader.class.getName());
    private static final String COUNT_ROWS_INTERIM_STM = "SELECT COUNT(*) FROM interim.%s;";
    private static final String COUNT_WRKT_INTERIM_STM = "SELECT COUNT(DISTINCT wrkt_id) FROM interim.%s;";
    private static final String COPY_INTO_INTERIM_STM = "COPY interim.%s FROM '%s';";
    private static final String COPY_POINTS_TO_PRODUCTION =
            "INSERT INTO production.%s (id, wrkt_id, distance, duration, dt, geom, distance_offset, duration_offset)\n" +
            "  SELECT pi.id, pi.wrkt_id, pi.distance, pi.duration, pi.dt, ST_GeomFromText('POINT(' || pi.lon || ' ' || pi.lat || ')', 4326), pi.distance_offset, pi.duration_offset\n" +
            "  FROM interim.%s pi;";
    private static final String COPY_ATHLETE = "COPY production.%s FROM '%s' WITH NULL 'NULL'";
    private static final String COPY_WRKT_TO_PRODUCTION = "INSERT INTO production.%s\n SELECT * FROM interim.%s;";
    private static final String DELETE = "DELETE FROM interim.%s;";
    private static Map<String, Integer[]> addToDbByRegion = new LinkedHashMap<>();
    private static int dubiousInDir = 0, fullInDir = 0;

    static void loadInDb(String dirWithInterimFiles)
    {
        connection = DbConnector.getConnection();
        try
        {
            DbConnector.setAutoCommit(false);
            WorkoutConverter.initializeLogMap(addToDbByRegion);
            File[] interimFiles = new File(dirWithInterimFiles).listFiles();
            if (interimFiles != null)
            {
                for (File file: interimFiles)
                {
                    processFile(file);
                }
            }
            connection.commit();
            String dirToLog;
            try {
                dirToLog = dirWithInterimFiles.split("Copy")[1].replace("/", "");
            } catch (Exception e) {
                dirToLog = dirWithInterimFiles;
            }
            WorkoutConverter.writeStatistics(dirToLog, String.format(Converter.OUTPUT_DIR, "stat_db"),
                   Converter.STAT_FILE_CONVERSION, -1, -1, -1, dubiousInDir, fullInDir,
                   -1, addToDbByRegion);

        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    private static boolean processFile(File file)
    {
        Statement statement = null;
        boolean success = true;
        try
        {
            statement = connection.createStatement();
            String interimTable = file.getName().replace(".txt", "");
            String path = file.getAbsolutePath();
            //---------------------- copy into an interim table from file -------------------
            String query = String.format(COPY_INTO_INTERIM_STM, interimTable, path);
            statement.executeUpdate(query);
            //-------------------------------------------------------------------------------
            String region = null;
            if (interimTable.contains("work"))
            {
                //-------------------------- count inserted rows in interim workout table----------------------
                query = String.format(COUNT_ROWS_INTERIM_STM, interimTable);
                ResultSet resultSet = statement.executeQuery(query);
                resultSet.next();
                int rows = resultSet.getInt("count");
                resultSet.close();
                //---------------------------------------------------------------------------------------------
                if (interimTable.contains("dbs"))
                {
                    dubiousInDir += rows;
                }
                else
                {
                    fullInDir += rows;
                }
            }
            else // get region to log points added
            {
                region = interimTable.split("_")[2];
            }

            if (region != null) // log added points
            {
                //--------------------------------count points added to a region----------------------------------
                String selectQuery = String.format(COUNT_ROWS_INTERIM_STM, interimTable);
                ResultSet resultSet = statement.executeQuery(selectQuery);
                resultSet.next();
                int pointCount = resultSet.getInt("count");
                //---------------------------------count workouts added to a region-------------------------------
                selectQuery = String.format(COUNT_WRKT_INTERIM_STM, interimTable);
                resultSet = statement.executeQuery(selectQuery);
                resultSet.next();
                int wrktCount = resultSet.getInt("count");
                addToDbByRegion.put(region, new Integer[]{wrktCount, pointCount}); // log
                resultSet.close();
            }

            String productionTargetTable = interimTable.replace("intr_", ""); // get production table name
            if (productionTargetTable.contains("point"))
            {
                // can't copy as is, geometry has to be created from lat and lon
                query = String.format(COPY_POINTS_TO_PRODUCTION, productionTargetTable, interimTable);
            }
            else
            { // copy as is; theoretically can copy directly from file, but then can't rollback all if something failed with points...
                query = String.format(COPY_WRKT_TO_PRODUCTION, productionTargetTable, interimTable);
            }
            // copy and count copied
            int countAffected = statement.executeUpdate(query);

            System.out.println(countAffected + " entries moved to " + productionTargetTable);

            query = String.format(DELETE, interimTable);
            // delete and count deleted
            countAffected = statement.executeUpdate(query);

            System.out.println(countAffected + " entries deleted from " + interimTable);

        }
        catch (Exception e)
        {
            if (connection != null)
            {
                try
                {
                    System.err.print("Transaction is being rolled back");
                    connection.rollback();
                }
                catch (SQLException ex)
                {
                    System.err.print(ex.getMessage());
                    LOGGER.error("----------- ROLLED BACK TRANSACTION FOR " + file.getAbsolutePath(), ex);
                    StringBuilder sb = new StringBuilder();
                    sb.append(file.getAbsolutePath());
                    sb.append("\n");
                    try
                    {
                        Converter.write(sb, String.format(Converter.OUTPUT_DIR, "stat_db"), "copyFailed.txt" );
                    }
                    catch (Exception e1)
                    {
                        LOGGER.error("File" + file.getAbsolutePath() + " not saved to copyFailed.txt", e1);
                    }
                }
            }
            success = false;
            System.out.println("Did not copy data for file " + file.getAbsolutePath() + " into DB");
            e.printStackTrace();
        }
        finally
        {
            if(statement != null)
            {
                try
                {
                    statement.close();
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }

        }

        return success;
    }

    static void loadAthletes(String outDir)
    {
        Statement statement;
        try
        {
            AthleteConverter.getAthleteConnection().setAutoCommit(false);
            statement = AthleteConverter.getAthleteConnection().createStatement();
            String query = String.format(COPY_ATHLETE, "athlete", (outDir + AthleteConverter.OUTPUT_NAME_ATHLETE));
            statement.execute(query);
            query = String.format(COPY_ATHLETE, "summary", (outDir + AthleteConverter.OUTPUT_NAME_SUMMARY));
            statement.execute(query);
            AthleteConverter.getAthleteConnection().commit();
            statement.close();
        }
        catch (Exception e)
        {
            try
            {
                AthleteConverter.getAthleteConnection().rollback();
            } catch (SQLException e1)
            {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }

}
