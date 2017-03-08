package com.evnica.endomondo.main.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Project: Endomondo
 * Class: ${CLASS_NAME}
 * Version: 0.1
 * Created on 3/7/2017 with the help of IntelliJ IDEA (thanks!)
 * Author: DS
 * Description:
 */
public class SummaryRepository
{
    private static final String TABLE_NAME = "summary";
    private static final String SCHEMA_NAME = "spatial";
    private static final String INSERT_STATEMENT = "INSERT INTO " + SCHEMA_NAME + "." + TABLE_NAME +
            "(user_id, sport, count, duration, distance) VALUES (?, ?, ?, ?, ?)";

    private static Connection connection;

    public static void setConnection( Connection connection )
    {
        SummaryRepository.connection = connection;
    }

    public static int insert(SummaryBySport summary, int userId) throws SQLException
    {
        PreparedStatement statement = connection.prepareStatement( INSERT_STATEMENT );

        statement.setInt( 1, userId );
        statement.setInt( 2, summary.sport );
        statement.setInt( 3, summary.count );
        statement.setDouble(4, summary.totalDuration);
        statement.setDouble(5, summary.totalDistance);

        int rowsAffected = statement.executeUpdate();
        statement.clearParameters();
        statement.close();

        return rowsAffected;
    }
}
