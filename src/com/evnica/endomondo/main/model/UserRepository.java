package com.evnica.endomondo.main.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
    private static final String SELECT_USER_IDS = "SELECT (id) FROM " + SCHEMA_NAME + "." + TABLE_NAME;

    private Connection connection;
    private ResultSet resultSet;

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
        if ( user.getDateCreated() != null )
        {   // convert joda datetime in millis and create sql timestamp
            statement.setTimestamp( 6, new Timestamp( user.getDateCreated().getMillis() ) );
        }
        else
        {
            statement.setNull( 6, Types.TIMESTAMP );
        }

        int rowsAffected = statement.executeUpdate();
        statement.clearParameters();
        statement.close();

        return rowsAffected;

    }

    public List<Integer> getUserIdsFromDB() throws SQLException
    {
        PreparedStatement statement = connection.prepareStatement( SELECT_USER_IDS );
        resultSet = statement.executeQuery();
        List<Integer> userIds = new ArrayList<>();

        while ( resultSet.next() )
        {
            userIds.add( resultSet.getInt( "id" ) );
        }

        statement.clearParameters();
        statement.close();
        return userIds;
    }

    public boolean userInDb(int id) throws SQLException
    {
        PreparedStatement statement = connection.prepareStatement( SELECT_BY_ID_STATEMENT );
        statement.setInt( 1, id );
        resultSet = statement.executeQuery();
        boolean inDb = resultSet.next();
        statement.clearParameters();
        statement.close();
        return inDb;
    }

}
