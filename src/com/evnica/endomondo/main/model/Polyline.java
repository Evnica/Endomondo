package com.evnica.endomondo.main.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Class: Polyline
 * Version: 0.1
 * Created on 21.02.2017 with the help of IntelliJ IDEA (thanks!)
 * Author: Evnica
 * Description:
 */
public class Polyline
{
    private List<Point> polyline = new ArrayList<>();

    Polyline(){}

    public Polyline( List<Point> polyline )
    {
        this.polyline = polyline;
    }

    public List<Point> getPolyline()
    {
        return polyline;
    }

    public void addPoint(Point p)
    {
        polyline.add( p );
    }

    public Point getPoint(int i)
    {
        return polyline.get( i );
    }

    public int size() { return polyline.size(); }

    public boolean isEmpty()
    {
        return polyline.size() == 0;
    }

    @Override
    public String toString()
    {
        // In spatial databases spatial coordinates are in x = longitude, and y = latitude.
        StringBuilder result = new StringBuilder();
        result.append( "LINESTRING(" );
        result.append( polyline.get(0).getLon() );
        result.append( " " );
        result.append( polyline.get(0).getLat());

        for (int i = 1; i < polyline.size(); i++)
        {
            result.append( "," );
            result.append( polyline.get(i).getLon() );
            result.append( " " );
            result.append( polyline.get(i).getLat());
        }
        result.append( ")" );

        return result.toString();
    }
}
