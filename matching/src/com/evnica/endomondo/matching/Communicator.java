package com.evnica.endomondo.matching;

import com.evnica.endomondo.main.connect.DbConnector;

import java.sql.*;

/**
 * Project: Endomondo
 * Class: ${CLASS_NAME}
 * Version: 0.1
 * Created on 4/18/2017 with the help of IntelliJ IDEA (thanks!)
 * Author: DS
 * Description:
 */
class Communicator
{
    private static Connection connection = null;
    private static final String CREATE_POINT_VIEW =
            "CREATE OR REPLACE VIEW interim.points AS SELECT * FROM production.point_fl WHERE wrkt_id = %d";
    private static final String CREATE_NETWORK_VIEW = "CREATE OR REPLACE VIEW interim.network AS " +
            "SELECT mi.id, mi.geom, mi.buffer, mi.source, mi.target FROM network.miami_dade mi\n" +
            "WHERE st_intersects(mi.buffer, st_geomfromtext('%s', 4326));";
    private static final String CALC_EXTENT =
            "SELECT st_astext(st_extent(geom)) FROM interim.points";

    static boolean initialize()
    {
        boolean success;
        try
        {
            DbConnector.connectToDb();
            connection = DbConnector.getConnection();
            success = true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            success = false;
        }
        return success;
    }

    static boolean createPointViewForWorkout(int wrktId)
    {
        boolean created;
        Statement statement = null;
        String query = String.format(CREATE_POINT_VIEW, wrktId);
        try
        {
            statement = connection.createStatement();
            statement.execute(query);
            created = true;
        } catch (SQLException e) {
            e.printStackTrace();
            created = false;
        }
        finally
        {
            if (statement != null)
            {
                try
                {
                    statement.close();
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
        }

        return created;
    }

    static String calculateExtent()
    {
        Statement statement = null;
        String extent = null;
        try
        {
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(CALC_EXTENT);
            resultSet.next();
            extent = resultSet.getString(1);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                assert statement != null;
                statement.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return extent;
    }

    static boolean createNetworkViewForExtent(String extent)
    {
        boolean created;
        Statement statement = null;
        String query = String.format(CREATE_NETWORK_VIEW, extent);
        try
        {
            statement = connection.createStatement();
            statement.execute(query);
            created = true;
        } catch (SQLException e) {
            e.printStackTrace();
            created = false;
        }
        finally
        {
            try
            {
                assert statement != null;
                statement.close();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }

        return created;
    }

    static boolean terminate()
    {
        boolean success;

        try
        {
            DbConnector.closeConnection();
            success = true;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            success = false;
        }
        return success;
    }

}
