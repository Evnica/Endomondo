package com.evnica.endomondo.main.model;

import com.evnica.endomondo.main.connect.DbConnector;
import org.joda.time.DateTimeZone;
import org.postgis.PGgeometry;

import java.sql.*;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.IntStream;

/**
 * Class: PointRepository
 * Version: 0.1
 * Created on 02.03.2017 with the help of IntelliJ IDEA (thanks!)
 * Author: Evnica
 * Description:
 */
public class PointRepository
{
    private static final String TABLE = "point";
    private static final String SCHEMA_NAME = "spatial";
    private static String insertStatement;
    private static Connection connection;


    public static void setConnection( Connection connection )
    {
        PointRepository.connection = connection;
    }

    public static void setInsertStatement(String country)
    {
        if (country != null)
        {
            try
            {
                PreparedStatement s = connection.prepareStatement("SELECT * FROM " + SCHEMA_NAME + "." + TABLE + "_" + country.toLowerCase());
                s.execute();
                insertStatement = "INSERT INTO " + SCHEMA_NAME + "." + TABLE + "_" + country.toLowerCase() +
                        " (id, wrkt_id, distance, duration, dt, geom, distance_offset, duration_offset ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            } catch (SQLException e) {
                insertStatement = "INSERT INTO " + SCHEMA_NAME + "." + TABLE +
                        " (id, wrkt_id, distance, duration, dt, geom, distance_offset, duration_offset ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            }
        }
        else
        {
            insertStatement = "INSERT INTO " + SCHEMA_NAME + "." + TABLE +
                    " (id, wrkt_id, distance, duration, dt, geom, distance_offset, duration_offset ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        }
    }

    public static int insertPoints(List<Point> points, int workoutId) throws SQLException
    {
        int[] rowsAffected = {0};
        try
        {
            PreparedStatement statement = connection.prepareStatement(insertStatement);
            for (Point point: points)
            {
                try {
                    statement.setInt( 1, point.getOrder() );
                    statement.setInt( 2, workoutId );
                    statement.setDouble( 3, point.getDistanceFromPrevious() );
                    statement.setInt( 4, point.getDurationFromPrevious() );
                    long millis = point.getTimeCaptured().getZone()
                                .getMillisKeepLocal(DateTimeZone.forTimeZone(TimeZone.getDefault()),
                                        point.getTimeCaptured().getMillis());
                    statement.setTimestamp( 5, new Timestamp( millis ));
                    PGgeometry geom = new PGgeometry(point.getPoint());
                    statement.setObject( 6, geom);
                    statement.setDouble( 7, point.getDistanceFromOffset() );
                    statement.setInt( 8, point.getDurationFromOffset() );

                    statement.addBatch();
                }
                catch (SQLException e)
                {
                    System.out.println("Point insertion error: " + e);
                    e.printStackTrace();
                   // DbConnector.commit();
                   // statement.clearParameters();
                }
                catch (Exception e)
                {
                    System.out.println("Invalid point " + point.getOrder() );
                }
            }
            rowsAffected = statement.executeBatch();
            statement.close();
        }
        catch (Exception e)
        {
            System.out.println("Insertion error for the list of points: " + e);
            e.printStackTrace();
        }

        return IntStream.of(rowsAffected).sum();
    }

    public static String determineRegion(Point point)
    {
        String region;
        Statement statement = null;
        try
        {
            statement = connection.createStatement();

            final String query = String.format(
                    "SELECT spatial.world.iso FROM spatial.world WHERE  ST_Intersects(ST_SetSRID(ST_MakePoint(%f, %f),4326), spatial.world.geom);",
                    point.getLon(), point.getLat());
            ResultSet resultSet = statement.executeQuery(query);
            resultSet.next();
            region = resultSet.getString("iso");
        }
        catch (SQLException e)
        {
            region = "all";
            System.out.println("Unable to determine region for point " + point.toString());
        }
        finally
        {
            if (statement != null)
            {
                try
                {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return region;

    }


/*    private static int insertPoint(Point point, int workoutId, PreparedStatement statement) throws SQLException
    {
        int rowsAffected = 0;
        try {
            statement.setInt( 1, point.getOrder() );
            statement.setInt( 2, workoutId );
            statement.setDouble( 3, point.getDistanceFromPrevious() );
            statement.setInt( 4, point.getDurationFromPrevious() );
            try {
                long millis = point.getTimeCaptured().getZone()
                        .getMillisKeepLocal(DateTimeZone.forTimeZone(TimeZone.getDefault()),
                                point.getTimeCaptured().getMillis());
                statement.setTimestamp( 5, new Timestamp( millis ));
            } catch (Exception e) {
                statement.setNull(5, Types.TIMESTAMP);
            }
            try {
                PGgeometry geom = new PGgeometry(point.getPoint());
                statement.setObject( 6, geom);
            } catch (Exception e) {
                statement.setObject(6, null);
            }
            statement.setDouble( 7, point.getDistanceFromOffset() );
            statement.setInt( 8, point.getDurationFromOffset() );

            rowsAffected = statement.executeUpdate();
            statement.clearParameters();

        }
        catch (SQLException e)
        {
            System.out.println("Point insertion error: " + e);
            e.printStackTrace();
            DbConnector.commit();
            statement.clearParameters();
        }
        return rowsAffected;
    }*/

/*    public static int insertPointsOneByOne(List<Point> points, int workoutId) throws SQLException
    {
        int rowsAffected = 0;
        PreparedStatement statement = connection.prepareStatement(insertStatement);
        for (Point p: points)
        {
            try {
                rowsAffected += insertPoint(p, workoutId, statement);
            } catch (SQLException e) {
                System.out.println("Exc on point " + p.getOrder());
                e.printStackTrace();
            }
        }
        statement.close();
        return rowsAffected;
    }*/

}
