package com.evnica.endomondo.test;

import com.evnica.endomondo.main.connect.DbConnector;
import com.evnica.endomondo.main.connect.UrlConnector;
import com.evnica.endomondo.main.model.User;
import com.evnica.endomondo.main.model.UserRepository;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Class: UserTableFilling
 * Version: 0.1
 * Created on 21.02.2017 with the help of IntelliJ IDEA (thanks!)
 * Author: Evnica
 * Description:
 */
public class UserTableFilling
{
    public static void main( String[] args ) throws Exception
    {
        //3130468 filled in as test
        // first iteration - 1 to 707
        // second from 26400086 to 26400750 excl.
        int start = 26412323;
        ArrayList<Integer> rejectedIds = new ArrayList<>( );
        int end = start + 25;
        String urlContent;
        User user = null;
        System.out.println("Start: " + new DateTime( ));
        DbConnector.connectToDb();
        for (int j = 0; j < 200; j ++)
        {
            System.out.println("Start: " + start + ", end: " + end);
            for (int i = start; i < end; i++)
            {
                try
                {
                    UrlConnector.setUrlUser( i );
                    try{
                        urlContent = UrlConnector.getUrlContent();
                        user = UrlConnector.parseUser( urlContent );
                    }
                    catch ( IOException e )
                    {
                        System.out.println(i + ": " + e);
                        if (e.getMessage().contains( "429" ))
                        {
                            System.out.println("Rejected due to multiple requests on ID " + i );
                            rejectedIds.add( i );
                            Thread.sleep( 20000 );
                            user = null;
                        }
                        else
                        {
                        user = null;
                        }
                    }

                    if (user != null)
                    {
                        UserRepository userRepository = new UserRepository( DbConnector.getConnection() );
                        userRepository.insert( user );
                    }
                }
                catch ( Exception e )
                {
                    System.out.println("DB Except " + i + ": " + e);
                }
                System.out.println("Processed " + i);
            }
            Thread.sleep( 15000 );
            start = end;
            end = start + 25;
        }
        System.out.println("Processing rejected:");
        for (int id: rejectedIds)
        {
            try
            {
                UrlConnector.setUrlUser( id );
                try{
                    urlContent = UrlConnector.getUrlContent();
                    user = UrlConnector.parseUser( urlContent );
                }
                catch ( IOException e )
                {
                    System.out.println(id + ": " + e);
                    if (e.getMessage().contains( "429" ))
                    {
                        System.out.println(id + " rejected" );
                        Thread.sleep( 15000 );
                    }
                    else
                    {
                        user = null;
                    }
                }

                if (user != null)
                {
                    UserRepository userRepository = new UserRepository( DbConnector.getConnection() );
                    userRepository.insert( user );
                }
            }
            catch ( Exception e )
            {
                System.out.println(id + ": " + e);
            }
        }

        System.out.println("End: " + new DateTime( ));
        DbConnector.closeConnection();

    }
}
