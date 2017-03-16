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
    private double distanceFromPrevious, distanceFromOffset;
    private int durationFromPrevious, durationFromOffset;
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

    public int getOrder()
    {
        return order;
    }

    public void setOrder( int order )
    {
        this.order = order;
    }

    public int getDurationFromPrevious()
    {
        return durationFromPrevious;
    }

    public void setDurationFromPrevious(int durationFromPrevious)
    {
        this.durationFromPrevious = durationFromPrevious;
    }

    public double getLat()
    {
        return lat;
    }

    double getLon()
    {
        return lon;
    }

    public double getDistanceFromPrevious()
    {
        return distanceFromPrevious;
    }

    public void setDistanceFromPrevious(double distanceFromPrevious)
    {
        this.distanceFromPrevious = distanceFromPrevious;
    }

    String toJSONString()
    {
        return "{\"lat\":" + lat + "," +
                "\"lon\":" + lon + "," +
                "\"dist\":"+ distanceFromPrevious + "," +
                "\"dur\":" + durationFromPrevious + "," +
                "\"time\":"+ timeCaptured + "}";
    }

    public double getDistanceFromOffset() {
        return distanceFromOffset;
    }

    public void setDistanceFromOffset(double distanceFromOffset) {
        this.distanceFromOffset = distanceFromOffset;
    }

    public int getDurationFromOffset() {
        return durationFromOffset;
    }

    public void setDurationFromOffset(int durationFromOffset) {
        this.durationFromOffset = durationFromOffset;
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

    String toStringTimeOnly()
    {
        return timeCaptured.toString( "HH:mm:ss" ) + " (" + lat + ", " + lon + ")";
    }

    private String toWKTString()
    {
        return "POINT(" + lon + " " + lat + ")";
    }

    DateTime getTimeCaptured()
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
