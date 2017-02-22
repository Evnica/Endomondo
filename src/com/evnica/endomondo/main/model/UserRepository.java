package com.evnica.endomondo.main.model;

import java.sql.*;

/**
 * Class: UserRepository
 * Version: 0.1
 * Created on 21.02.2017 with the help of IntelliJ IDEA (thanks!)
 * Author: Evnica
 * Description:
 */
public class UserRepository
{
    private static final String TABLE_NAME = "user";
    private static final String SCHEMA_NAME = "spatial";
    private static final String INSERT_STATEMENT = "INSERT INTO " + SCHEMA_NAME + "." + TABLE_NAME +
            "(id, gender, ccl_sp, ccl_tr, mnt_bk, dt_crtd) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SELECT_BY_ID_STATEMENT = "SELECT * FROM " + SCHEMA_NAME + "." + TABLE_NAME +
            " WHERE id = ?";

    private Connection connection;
    protected ResultSet resultSet;

    public UserRepository(Connection connection)
    {
        this.connection = connection;
    }

    public int insert(User user) throws SQLException
    {
        PreparedStatement statement = connection.prepareStatement( INSERT_STATEMENT );
        statement.setInt( 1, user.getId() );
        statement.setInt( 2, user.getGender() );
        statement.setInt( 3, user.getCyclingSportCount() );
        statement.setInt( 4, user.getCyclingTransportCount() );
        statement.setInt( 5, user.getMountainBikingCount() );
        // convert joda datetime in millis and create sql timestamp
        statement.setTimestamp( 6, new Timestamp( user.getDateCreated().getMillis() ) );

        int rowsAffected = statement.executeUpdate();
        statement.clearParameters();
        statement.close();

        return rowsAffected;

    }
}
