package com.evnica.endomondo.main.model;

import org.joda.time.DateTimeZone;
import org.postgis.PGgeometry;

import java.sql.*;
import java.util.TimeZone;

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

    public static int insertPoint(Point point, int workoutId) throws SQLException
    {
        int rowsAffected = 0;

        try {
            PreparedStatement statement = connection.prepareStatement(insertStatement);
            statement.setInt( 1, point.getOrder() );
            statement.setInt( 2, workoutId );
            try {
                statement.setDouble( 3, point.getDistanceFromPrevious() );
            } catch (SQLException e) {
                statement.setNull(3, Types.DOUBLE);
            }
            try {
                statement.setInt( 4, point.getDurationFromPrevious() );
            } catch (SQLException e) {
                statement.setNull(4, Types.INTEGER);
            }
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
                geom.getGeometry().setSrid( 4326 );
                statement.setObject( 6, geom);
            } catch (Exception e) {
                statement.setObject(6, null);
            }
            try {
                statement.setDouble( 7, point.getDistanceFromOffset() );
            } catch (SQLException e) {
                statement.setNull(7, Types.DOUBLE);
            }
            try {
                statement.setInt( 8, point.getDurationFromOffset() );
            } catch (SQLException e) {
                statement.setNull(8, Types.INTEGER);
            }

            rowsAffected = statement.executeUpdate();
            statement.clearParameters();
            statement.close();
        }
        catch (SQLException e)
        {
            System.out.println("Point insertion error: " + e);
        }
        return rowsAffected;
    }

}
