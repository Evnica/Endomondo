package com.evnica.endomondo.main.model;

/**
 * Class: Lap
 * Version: 0.1
 * Created on 21.02.2017 with the help of IntelliJ IDEA (thanks!)
 * Author: Evnica
 * Description:
 */
public class Lap
{
    private int id;
    private double beginLat, beginLon, endLat, endLon;
    private boolean containsPolyline; // in JSON "show_map" = 1 means false,
                                      // 0 means true, e.g. track is displayed on the map
    private int workoutId;
    private Polyline smallPolyline;

    public Lap( double beginLat, double beginLon, double endLat, double endLon )
    {
        this.beginLat = beginLat;
        this.beginLon = beginLon;
        this.endLat = endLat;
        this.endLon = endLon;
        containsPolyline = false;
    }

    public void setSmallPolyline( Polyline smallPolyline )
    {
        this.smallPolyline = smallPolyline;
        containsPolyline = true;
    }

    public void setWorkoutId( int workoutId )
    {
        this.workoutId = workoutId;
    }

    public void setId (int order)
    {
        this.id = workoutId * 1000 + order;
    }

    public double getBeginLat()
    {
        return beginLat;
    }

    public double getBeginLon()
    {
        return beginLon;
    }

    public double getEndLat()
    {
        return endLat;
    }

    public double getEndLon()
    {
        return endLon;
    }

    public int getId()
    {
        return id;
    }

    public Polyline getSmallPolyline()
    {
        return smallPolyline;
    }

    public boolean isContainsPolyline()
    {
        return containsPolyline;
    }

    public int getWorkoutId()
    {
        return workoutId;
    }
}
