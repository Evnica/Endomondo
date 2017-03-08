package com.evnica.endomondo.main.model;

import java.sql.*;

/**
 * Project: Endomondo
 * Class: ${CLASS_NAME}
 * Version: 0.1
 * Created on 3/8/2017 with the help of IntelliJ IDEA (thanks!)
 * Author: DS
 * Description:
 */
public class WorkoutDetailRepository
{
    private static Connection connection;
    private static final String TABLE_NAME = "workout_detail";
    private static final String SCHEMA_NAME = "spatial";
    private static final String INSERT_STATEMENT = "INSERT INTO " + SCHEMA_NAME + "." + TABLE_NAME +
            "(id, distance, duration, start_at, weather, user_id, show_map, geomtype) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    public static int insert(WorkoutDetail workout) throws SQLException
    {
        PreparedStatement statement = connection.prepareStatement( INSERT_STATEMENT );

        statement.setInt( 1, workout.getId() );
        try {
            statement.setDouble( 2, workout.getDistance() );
        } catch (SQLException e) {
            statement.setNull(2, Types.DOUBLE);
        }
        try {
            statement.setDouble( 3, workout.getDuration() );
        } catch (SQLException e) {
            statement.setNull(3, Types.DOUBLE);
        }
        try {
            statement.setTimestamp( 4, new Timestamp( workout.getStartAt().getMillis() ) );
        } catch (SQLException e) {
            statement.setNull(4, Types.TIMESTAMP);
        }
        try {
            statement.setInt(5, workout.getWeather());
        } catch (SQLException e) {
            statement.setNull(5, Types.INTEGER);
        }
        try {
            statement.setInt(6, workout.getUserId());
        } catch (SQLException e) {
            statement.setNull(6, Types.INTEGER);
        }
        try {
            statement.setInt(7, workout.getWorkoutGeometryType().getCode());
        } catch (SQLException e) {
            statement.setNull(7, Types.INTEGER);
        }

        int rowsAffected = statement.executeUpdate();
        statement.clearParameters();
        statement.close();

        return rowsAffected;
    }
}
