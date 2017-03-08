package com.evnica.endomondo.main.model;

import org.joda.time.DateTime;

import java.sql.SQLException;

/**
 * Class: Point
 * Version: 0.1
 * Created on 21.02.2017 with the help of IntelliJ IDEA (thanks!)
 * Author: Evnica
 * Description:
 */

public class Point
{
    private double lat, lon;
    private DateTime timeCaptured;
    private double distance, duration;
    private int order;
    private org.postgis.Point point;

    public Point( double lat, double lon )
    {
        this.lat = lat;
        this.lon = lon;
        this.point = new org.postgis.Point(lon, lat);
    }

    public org.postgis.Point getPoint()
    {
        return point;
    }

    public Point( double lat, double lon, DateTime timeCaptured, double distance, double duration )
    {
        this.lat = lat;
        this.lon = lon;
        this.timeCaptured = timeCaptured;
        this.distance = distance;
        this.duration = duration;
    }

    public int getOrder()
    {
        return order;
    }

    public void setOrder( int order )
    {
        this.order = order;
    }

    public double getDuration()
    {
        return duration;
    }

    public void setDuration( double duration )
    {
        this.duration = duration;
    }

    public double getLat()
    {
        return lat;
    }

    public double getLon()
    {
        return lon;
    }

    public double getDistance()
    {
        return distance;
    }

    public void setDistance( double distance )
    {
        this.distance = distance;
    }

    public String toJSONString()
    {
        return "{\"lat\":" + lat + "," +
                "\"lon\":" + lon + "," +
                "\"dist\":"+ distance + "," +
                "\"dur\":" + duration + "," +
                "\"time\":"+ timeCaptured + "}";
    }


    @Override
    public String toString()
    {
        String result;
        if (timeCaptured != null)
            result = "(" + lat + ", " + lon + "), " + timeCaptured.toString( "yyyy-MM-dd'T'HH:mm:ss" );
        else
            result = "(" + lat + ", " + lon + ")";
        return result;
    }

    public String toStringTimeOnly()
    {
        return timeCaptured.toString( "HH:mm:ss" ) + " (" + lat + ", " + lon + ")";
    }

    public String toWKTString()
    {
        return "POINT(" + lon + " " + lat + ")";
    }

    public DateTime getTimeCaptured()
    {
        return timeCaptured;
    }

    public void setTimeCaptured( DateTime timeCaptured )
    {
        this.timeCaptured = timeCaptured;
    }

    @Override
    public boolean equals( Object obj )
    {
        double tolerance = 1E-6;
        boolean equal = false;
        if (obj instanceof Point)
        {
            if (Math.abs( ((Point) obj).lat - this.lat ) < tolerance &&
                Math.abs( ((Point) obj).lon - this.lon ) < tolerance)
            {
                equal = true;
            }
        }
        return equal;
    }

    public org.postgis.Point toGeom() throws SQLException
    {
        return new org.postgis.Point( toWKTString() );
    }
}
