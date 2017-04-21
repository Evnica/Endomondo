package com.evnica.endomondo.main.connect;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Class: DbConnector
 * Version: 0.1
 * Created on 21.02.2017 with the help of IntelliJ IDEA (thanks!)
 * Author: Evnica
 * Description:
 */
public class DbConnector
{
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/Endomondo";
    private static final String USER = "postgres";
    private static String pwd = "Hi, EndoProductioN!";
    private static Connection connection = null;

    public static Connection getConnection() {
        return connection;
    }

    public static void connectToDb() throws ClassNotFoundException, SQLException {
        if (pwd == null) {
            readPassword();
        }
        connection = DriverManager.getConnection(DB_URL, USER, pwd);
    }

    public static Connection getNewConnection()
    {
        Connection connection = null;
        if (pwd == null)
        {
            readPassword();
        }
        try
        {
            connection = DriverManager.getConnection(DB_URL, USER, pwd);
        }
        catch (Exception e)
        {
            System.out.println("Can't get  a new connection: " + e);
        }
        return connection;
    }

    public static void closeConnection() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    public static void setAutoCommit(boolean autoCommit) throws SQLException {
        connection.setAutoCommit(autoCommit);
    }

    public static void rollback() throws SQLException
    {
        connection.rollback();
    }

    private static void readPassword ()
    {
        if (pwd == null)
        {
            JPasswordField passwordField = new JPasswordField();
            passwordField.addAncestorListener (new AncestorListener() // focus in the text area
            {
                @Override
                public void ancestorAdded(AncestorEvent event) { passwordField.requestFocusInWindow(); }
                @Override
                public void ancestorRemoved(AncestorEvent event) {}
                @Override
                public void ancestorMoved(AncestorEvent event) {}     });

            JPanel messagePanel = new JPanel(new GridLayout(0,1));
            messagePanel.add(new JLabel("<html><p style='width: 220px;'>Please enter the password for "
                    + USER + "<br>(DB: " + DB_URL + ")</p></html>"));
            messagePanel.add(passwordField);
            passwordField.requestFocus();
            String title = "Password Input";
            JOptionPane.showMessageDialog(null, messagePanel, title, JOptionPane.PLAIN_MESSAGE);

            pwd = new String(passwordField.getPassword());
        }
    }

    public static void setPwd(String pwd)
    {
        DbConnector.pwd = pwd;
    }

}
