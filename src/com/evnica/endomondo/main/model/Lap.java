package com.evnica.endomondo.main.model;

import org.joda.time.DateTime;

/**
 * Class: Lap
 * Version: 0.1
 * Created on 21.02.2017 with the help of IntelliJ IDEA (thanks!)
 * Author: Evnica
 * Description:
 */
public class Lap
{
    private long id;
    private double beginLat, beginLon, endLat, endLon;
    private boolean containsPolyline; // in JSON "show_map" = 1 means false,
                                      // 0 means true, e.g. track is displayed on the map
    private int workoutId;
    private Polyline smallPolyline;
    private DateTime offset;
    private long duration; // in millis

    public Lap( double beginLat, double beginLon, double endLat, double endLon )
    {
        this.beginLat = beginLat;
        this.beginLon = beginLon;
        this.endLat = endLat;
        this.endLon = endLon;
        containsPolyline = false;
    }

    public Lap(double beginLat, double beginLon)
    {
        this.beginLat = beginLat;
        this.beginLon = beginLon;
        containsPolyline = false;
    }

    void setEndLat(double endLat)
    {
        this.endLat = endLat;
    }

    void setEndLon(double endLon)
    {
        this.endLon = endLon;
    }

    public void setSmallPolyline( Polyline smallPolyline )
    {
        this.smallPolyline = smallPolyline;
        containsPolyline = (smallPolyline != null);
    }

    public void setWorkoutId( int workoutId )
    {
        this.workoutId = workoutId;
    }

    public void setId (int order)
    {
        this.id =  workoutId * 1000L + order ;
    }

    double getBeginLat()
    {
        return beginLat;
    }

    double getBeginLon()
    {
        return beginLon;
    }

    double getEndLat()
    {
        return endLat;
    }

    double getEndLon()
    {
        return endLon;
    }

    public long getId()
    {
        return id;
    }

    DateTime getOffset()
    {
        return offset;
    }

    public void setOffset( DateTime offset )
    {
        this.offset = offset;
    }

    Polyline getSmallPolyline()
    {
        return smallPolyline;
    }

    boolean containsPolyline()
    {
        return containsPolyline;
    }

    int getWorkoutId()
    {
        return workoutId;
    }

    public long getDuration()
    {
        return duration;
    }

    public void setDuration( long duration )
    {
        this.duration = duration;
    }

    @Override
    public String toString()
    {
        String lapString = id + ": ";
        if (offset != null)
            lapString +=  "[" + offset.toString("yyyy-MM-dd'T'HH:mm:ss") + ", " + duration + " ms], ";
        lapString += "start (" + beginLat + ", " + beginLon + "), end (" + endLat + ", " + endLon + ")";

        if (containsPolyline)
        {
            lapString += ": " + smallPolyline.size() + " points\n"  + smallPolyline;
        }
        return lapString;
    }
}
