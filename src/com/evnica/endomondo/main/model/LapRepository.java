package com.evnica.endomondo.main.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.postgis.PGgeometry;

import java.sql.Timestamp;
import java.util.List;

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
       "(id, workout_id, b_lat, b_lon, e_lat, e_lon, with_geom, geom, timestamp) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
        statement.setLong( 1, lap.getId() );
        statement.setInt( 2, lap.getWorkoutId() );
        statement.setDouble( 3, lap.getBeginLat() );
        statement.setDouble( 4, lap.getBeginLon() );
        statement.setDouble( 5, lap.getEndLat() );
        statement.setDouble( 6, lap.getEndLon() );
        statement.setBoolean( 7, lap.containsPolyline() );
        PGgeometry lineString = new PGgeometry(lap.getSmallPolyline().toLineString());
        lineString.getGeometry().setSrid( 4326 );
        statement.setObject( 8, lineString);
        statement.setTimestamp( 9, new Timestamp( lap.getOffset().getMillis() ));

        int rowsAffected = statement.executeUpdate();
        statement.clearParameters();
        statement.close();

        return rowsAffected;
    }
}
