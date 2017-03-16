package com.evnica.endomondo.main.model;

import org.joda.time.DateTime;

import java.sql.*;
/*
import java.io.*;
import java.text.SimpleDateFormat;
*/
import java.util.ArrayList;
import java.util.List;

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
/*    private static final String SELECT_ALL_STATEMENT = "SELECT * FROM " + SCHEMA_NAME + "." + TABLE_NAME +
                                                       " ORDER BY user_id";*/
    private static final String SELECT_BY_ID_STATEMENT = "SELECT * FROM " + SCHEMA_NAME + "." + TABLE_NAME +
                                " WHERE user_id = ?";
    private final static String SELECT_USERS = "SELECT DISTINCT user_id from " +
            SCHEMA_NAME + "." + TABLE_NAME + " ORDER BY user_id";

    private static Connection connection;

    public static void setConnection( Connection connection )
    {
        WorkoutRepository.connection = connection;
    }

    public static List<Workout> selectByUserId(int id) throws SQLException
    {
        List<Workout> workouts = new ArrayList<>(  );

        PreparedStatement statement = connection.prepareStatement( SELECT_BY_ID_STATEMENT );
        statement.setInt( 1, id );
        ResultSet resultSet = statement.executeQuery();

        while ( resultSet.next() )
        {
            workouts.add( new Workout( resultSet.getInt( "id" ), resultSet.getInt( "sport" ),
                                        new DateTime( resultSet.getTimestamp( "start_dt" ).getTime() ),
                                        resultSet.getInt( "user_id" )));
        }
        statement.clearParameters();
        statement.close();

        return workouts;
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

    /*public static void toCsv(String filename)
    {
        String file = filename + "-all.txt";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try
        {
            BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(file));
            PrintWriter writer = new PrintWriter(new OutputStreamWriter
                    (stream, "UTF-8"));
            Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            try
            {
                statement.setFetchSize( 1 );

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
                    writer.close();
                    stream.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }*/

    // load all distinct users from db
    public static List<Integer> getUserIds() throws SQLException
    {
        ResultSet resultSet;
        List<Integer> ids = new ArrayList<>(  );
        PreparedStatement statement = connection.prepareStatement( SELECT_USERS );
        resultSet = statement.executeQuery();
        while ( resultSet.next() )
        {
            ids.add( resultSet.getInt( 1 ) );
        }
        statement.close();
        resultSet.close();
        return ids;
    }
}
