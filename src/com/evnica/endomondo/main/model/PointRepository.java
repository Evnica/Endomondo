package com.evnica.endomondo.main.model;

import org.postgis.PGgeometry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

/**
 * Class: PointRepository
 * Version: 0.1
 * Created on 02.03.2017 with the help of IntelliJ IDEA (thanks!)
 * Author: Evnica
 * Description:
 */
public class PointRepository
{
    private static String table = "point";
    private static final String SCHEMA_NAME = "spatial";
    private static String insertStatement;

    private static Connection connection;

    public static void setConnection( Connection connection )
    {
        PointRepository.connection = connection;
    }

    public static int insertPoint(String country, Point point, int workoutId) throws SQLException
    {
        if (country != null)
        {
            table = table + "_" + country.toLowerCase();
        }

        insertStatement = "INSERT INTO " + SCHEMA_NAME + "." + table +
                "(id, wrkt_id, distance, duration, dt, geom ) VALUES (?, ?, ?, ?, ?, ?)";;

        PreparedStatement statement = connection.prepareStatement( insertStatement );
        statement.setInt( 1, point.getOrder() );
        statement.setInt( 2, workoutId );
        statement.setDouble( 3, point.getDistance() );
        statement.setDouble( 4, point.getDuration() );
        statement.setTimestamp( 5, new Timestamp( point.getTimeCaptured().getMillis() ) );
        PGgeometry geom = new PGgeometry(point.getPoint());
        geom.getGeometry().setSrid( 4326 );
        statement.setObject( 6, geom);
        int rowsAffected = statement.executeUpdate();
        statement.clearParameters();
        statement.close();

        return rowsAffected;
    }

    public static int insertPoints(String country, List<Point> points, int workoutId) throws SQLException
    {
        int rowsAffected = 0;
        for (Point p: points)
        {
            rowsAffected += insertPoint(country, p, workoutId  );
        }
        return rowsAffected;
    }

}
