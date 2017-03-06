package com.evnica.endomondo.main.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

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

    private static Connection connection;

    public static void setConnection( Connection connection )
    {
        AthleteRepository.connection = connection;
    }

    public static int insertInvalidity( int id, boolean invalid) throws SQLException
    {
        PreparedStatement statement = connection.prepareStatement( INSERT_VALIDITY_STATEMENT );

        statement.setInt( 1, id );
        statement.setBoolean( 2, invalid );
        int rowsAffected = statement.executeUpdate();
        statement.clearParameters();
        statement.close();

        return rowsAffected;
    }


}
