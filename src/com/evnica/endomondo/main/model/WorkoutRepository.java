package com.evnica.endomondo.main.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Class: WorkoutRepository
 * Version: 0.1
 * Created on 21.02.2017 with the help of IntelliJ IDEA (thanks!)
 * Author: Evnica
 * Description:
 */
public class WorkoutRepository
{
    private static final String TABLE_NAME = "workout";
    private static final String SCHEMA_NAME = "spatial";
    private static final String INSERT_STATEMENT = "INSERT INTO " + SCHEMA_NAME + "." + TABLE_NAME +
            "(id, sport, user_id, start_dt) VALUES (?, ?, ?, ?)";

    private static Connection connection;

    public static void setConnection( Connection connection )
    {
        WorkoutRepository.connection = connection;
    }

    public int insert(Workout workout) throws SQLException
    {
        PreparedStatement statement = connection.prepareStatement( INSERT_STATEMENT );

        statement.setInt( 1, workout.getId() );
        statement.setInt( 2, workout.getSport() );
        statement.setInt( 3, workout.getUserId() );
        statement.setTimestamp( 4, new Timestamp( workout.getLocalStartTime().getMillis() ) );

        int rowsAffected = statement.executeUpdate();
        statement.clearParameters();
        statement.close();

        return rowsAffected;

    }
}
