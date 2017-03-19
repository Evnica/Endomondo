package com.evnica.endomondo.main.model;

import java.sql.*;

import org.joda.time.DateTimeZone;
import org.postgis.PGgeometry;

import java.util.List;
import java.util.TimeZone;

/**
 * Class: LapRepository
 * Version: 0.1
 * Created on 26.02.2017 with the help of IntelliJ IDEA (thanks!)
 * Author: Evnica
 * Description:
 */
public class LapRepository
{
    private static final String TABLE_NAME = "lap";
    private static final String SCHEMA_NAME = "spatial";
    private static final String INSERT_STATEMENT = "INSERT INTO " + SCHEMA_NAME + "." + TABLE_NAME +
       "(id, workout_id, b_lat, b_lon, e_lat, e_lon, with_geom, geom, timestamp, duration) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static Connection connection;

    public static void setConnection( Connection connection )
    {
        LapRepository.connection = connection;
    }

    public static int insert(WorkoutJSON workoutJSON, TargetGeometry targetGeometry) throws SQLException
    {
        int rowsAffected = 0;
        if (targetGeometry.equals( TargetGeometry.LAPS ))
        {
            if (workoutJSON.getLaps().size() > 0)
            {
                for (Lap lap: workoutJSON.getLaps())
                {
                    rowsAffected += insert( lap );
                }
            }
        }
        else // can't input both as it will duplicate tracks; in case BOTH selected, input points only
        {
            List<Lap> laps = workoutJSON.pointsToLaps();
            if (laps.size() > 0)
            {
                for (Lap lap: laps)
                {
                    rowsAffected += insert( lap );
                }
            }
        }
        return rowsAffected;
    }

    public static int insert(Lap lap) throws SQLException
    {
        PreparedStatement statement = connection.prepareStatement( INSERT_STATEMENT );
        statement.setInt( 1, lap.getId() );
        statement.setInt( 2, lap.getWorkoutId() );
        try {
            statement.setDouble( 3, lap.getBeginLat() );
        } catch (SQLException e) {
            statement.setNull( 3, Types.DOUBLE);
        }
        try {
            statement.setDouble( 4, lap.getBeginLon() );
        } catch (SQLException e) {
            statement.setNull( 4, Types.DOUBLE);
        }
        try {
            statement.setDouble( 5, lap.getEndLat() );
        } catch (SQLException e) {
            statement.setNull( 5, Types.DOUBLE);
        }
        try {
            statement.setDouble( 6, lap.getEndLon() );
        } catch (SQLException e) {
            statement.setNull( 6, Types.DOUBLE);
        }
        statement.setBoolean( 7, lap.containsPolyline() );
        try {
            PGgeometry lineString = new PGgeometry(lap.getSmallPolyline().toLineString());
            lineString.getGeometry().setSrid( 4326 );
            statement.setObject( 8, lineString);
        } catch (Exception e) {
            statement.setObject(8, null);
        }
        try {
            long millis = lap.getOffset().getZone()
                    .getMillisKeepLocal(DateTimeZone.forTimeZone(TimeZone.getDefault()),
                            lap.getOffset().getMillis());
            statement.setTimestamp( 9, new Timestamp( millis ));
        } catch (Exception e) {
            statement.setNull( 9, Types.TIMESTAMP);
        }
        try {
            statement.setLong(10, lap.getDuration());
        } catch (SQLException e) {
            statement.setNull( 3, Types.BIGINT);
        }

        int rowsAffected = statement.executeUpdate();
        statement.clearParameters();
        statement.close();

        return rowsAffected;
    }
}
