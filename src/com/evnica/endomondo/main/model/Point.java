package com.evnica.endomondo.main.model;

import org.joda.time.DateTime;

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

    public Point( double lat, double lon )
    {
        this.lat = lat;
        this.lon = lon;
    }

    public double getLat()
    {
        return lat;
    }

    public double getLon()
    {
        return lon;
    }

    @Override
    public String toString()
    {
        return "(" + lat + ", " + lon + "), " + timeCaptured.toString( "yyyy-MM-dd'T'HH:mm:ss" );
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
}
