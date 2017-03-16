package com.evnica.endomondo.main.model;

import java.sql.*;

/**
 * Class: AthleteRepository
 * Version: 0.1
 * Created on 05.03.2017 with the help of IntelliJ IDEA (thanks!)
 * Author: Evnica
 * Description:
 */
public class AthleteRepository
{
    private static final String TABLE_NAME = "athlete";
    private static final String SCHEMA_NAME = "spatial";
    private static final String INSERT_VALIDITY_STATEMENT = "INSERT INTO " + SCHEMA_NAME + "." + TABLE_NAME +
            "(id, invalid) VALUES (?, ?)";
    private static final String INSERT_STATEMENT = "INSERT INTO " + SCHEMA_NAME + "." + TABLE_NAME +
            "(id, invalid, gender, born, workout_cnt, country, created) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String GET_COUNTRY = "SELECT country FROM " + SCHEMA_NAME + "." + TABLE_NAME + " WHERE id = ?";

    private static Connection connection;

    public static void setConnection( Connection connection )
    {
        AthleteRepository.connection = connection;
    }

    public static int insertInvalidity( int id) throws SQLException
    {
        PreparedStatement statement = connection.prepareStatement( INSERT_VALIDITY_STATEMENT );

        statement.setInt( 1, id );
        statement.setBoolean( 2, true );
        int rowsAffected = statement.executeUpdate();
        statement.clearParameters();
        statement.close();

        return rowsAffected;
    }

    public static int insert(Athlete athlete) throws SQLException
    {
        PreparedStatement statement = connection.prepareStatement( INSERT_STATEMENT );

        statement.setInt( 1, athlete.getId() );
        statement.setBoolean( 2, false );
        statement.setInt(3, athlete.getGender());
        try {
            statement.setDate(4, new Date(athlete.getDateOfBirth().getMillis()));
        } catch (Exception e) {
            statement.setNull(4, Types.DATE);
        }
        statement.setInt(5, athlete.getWorkoutCount());
        try {
            statement.setString(6, athlete.getCountry());
        } catch (Exception e) {
            statement.setNull(6, Types.VARCHAR);
        }
        try {
            statement.setTimestamp(7, new Timestamp(athlete.getCreatedDate().getMillis()));
        } catch (Exception e) {
            statement.setNull(4, Types.TIMESTAMP);
        }


        int rowsAffected = statement.executeUpdate();
        statement.clearParameters();
        statement.close();

        return rowsAffected;
    }

    public static String getCountry(int id) throws SQLException
    {
        PreparedStatement statement = connection.prepareStatement(GET_COUNTRY);
        statement.setInt(1, id);
        ResultSet resultSet = statement.executeQuery();
        resultSet.next();
        return resultSet.getString(1);
    }


}
