package com.evnica.endomondo.test;

import com.evnica.endomondo.main.connect.UrlConnector;
import com.evnica.endomondo.main.decode.JSONContentParser;
import com.evnica.endomondo.main.model.User;
import org.junit.Before;
import org.junit.Test;

/**
 * Class: UrlConnectorTest
 * Version: 0.1
 * Created on 21.02.2017 with the help of IntelliJ IDEA (thanks!)
 * Author: Evnica
 * Description:
 */
public class UrlConnectorTest
{
    private String content;
    @Before
    public void setUp() throws Exception
    {
        UrlConnector.setUrl( "https://www.endomondo.com/rest/v1/users/3130468/" );
    }

    @Test
    public void getUrlContent() throws Exception
    {
        content = UrlConnector.getUrlContent();
        System.out.println(UrlConnector.getUrlContent());
    }

    @Test
    public void parseUser() throws Exception
    {
        content = UrlConnector.getUrlContent();
        User user = JSONContentParser.parseUser( content );
        System.out.println(user);

    }

    @Test
    public void invalidUrlParse() throws Exception
    {
        try
        {
            UrlConnector.setUrl( "https://www.endomondo.com/rest/v1/users/13829480932483130468/" );
            content = UrlConnector.getUrlContent();
            User user = JSONContentParser.parseUser( content );
            System.out.println(user);
        }
        catch ( Exception e )
        {
            System.out.println(""+ e);
        }


    }



}