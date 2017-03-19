package com.evnica.endomondo.main.model;

import org.joda.time.DateTimeZone;

import java.sql.*;
import java.util.TimeZone;

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
            "(id, distance, duration, start_at, weather, user_id, show_map, geomtype, sport, time_zone) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_LAP_COUNT = "UPDATE " + SCHEMA_NAME + "." + TABLE_NAME +
                                               " SET lap_count = ? WHERE id = ?";
    private static final String UPDATE_POINT_COUNT = "UPDATE " + SCHEMA_NAME + "." + TABLE_NAME +
            " SET point_count = ? WHERE id = ?";

    public static void setConnection(Connection connection) {
        WorkoutDetailRepository.connection = connection;
    }

    public static int update(boolean lap, int id, int count) throws SQLException
    {
        PreparedStatement statement;
        if (lap) statement = connection.prepareStatement(UPDATE_LAP_COUNT);
        else statement = connection.prepareStatement(UPDATE_POINT_COUNT);

        statement.setInt(1, count);
        statement.setInt(2, id);

        int rowsAffected = statement.executeUpdate();
        statement.clearParameters();
        statement.close();

        return rowsAffected;

    }

    public static int insert(WorkoutDetail workout) throws SQLException
    {
        PreparedStatement statement = connection.prepareStatement( INSERT_STATEMENT );

        statement.setInt( 1, workout.getId() );
        try {
            statement.setDouble( 2, workout.getDistance() );
        } catch (Exception e) {
            statement.setNull(2, Types.DOUBLE);
        }
        try {
            statement.setDouble( 3, workout.getDuration() );
        } catch (Exception e) {
            statement.setNull(3, Types.DOUBLE);
        }
        try {
            long millis = workout.getStartAt().getZone()
                    .getMillisKeepLocal(DateTimeZone.forTimeZone(TimeZone.getDefault()),
                            workout.getStartAt().getMillis());
            statement.setTimestamp( 4, new Timestamp( millis ) );
        } catch (Exception e) {
            statement.setNull(4, Types.TIMESTAMP);
        }
        try {
            statement.setInt(5, workout.getWeather());
        } catch (Exception e) {
            statement.setNull(5, Types.INTEGER);
        }
        try {
            statement.setInt(6, workout.getUserId());
        } catch (SQLException e) {
            statement.setNull(6, Types.INTEGER);
        }
        try {
            statement.setInt(7, workout.getShowMap());
        } catch (Exception e) {
            statement.setNull(7, Types.INTEGER);
        }
        try {
            statement.setInt(8, workout.getWorkoutGeometryType().getCode());
        } catch (Exception e) {
            statement.setNull(8, Types.INTEGER);
        }

        try {
            statement.setInt(9, workout.getSport());
        } catch (Exception e) {
            statement.setNull(9, Types.INTEGER);
        }

        try {
            statement.setString(10, workout.getStartAt().getZone().getID());
        } catch (Exception e) {
            statement.setNull(10, Types.VARCHAR);
        }

        int rowsAffected = statement.executeUpdate();
        statement.clearParameters();
        statement.close();

        return rowsAffected;
    }
}
