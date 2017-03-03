package com.evnica.endomondo.main.model;

import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;

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
    private static final String SELECT_ALL_STATEMENT = "SELECT * FROM " + SCHEMA_NAME + "." + TABLE_NAME;

    private static Connection connection;

    public static void setConnection( Connection connection )
    {
        WorkoutRepository.connection = connection;
    }

    public static int insert(Workout workout) throws SQLException
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

    public static void toCsv(String filename)
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try (
             PrintWriter writer = new PrintWriter(new OutputStreamWriter
                        (new BufferedOutputStream(new FileOutputStream(filename)), "UTF-8"));
             Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
        )
        {
            statement.setFetchSize(1);

            try (ResultSet resultSet = statement.executeQuery(SELECT_ALL_STATEMENT))
            {
                while (resultSet.next())
                {
                    String time = format.format(resultSet.getTimestamp("start_dt"));
                    writer.append(Integer.toString(resultSet.getInt("id"))).append(",")
                          .append((Integer.toString(resultSet.getInt("sport")))).append(",")
                          .append((Integer.toString(resultSet.getInt("user_id")))).append(",")
                          .append(time).append("\n");
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
}
