package com.evnica.endomondo.main.model;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Class: WorkoutJSON
 * Version: 0.1
 * Created on 23.02.2017 with the help of IntelliJ IDEA (thanks!)
 * Author: Evnica
 * Description:
 */
public class WorkoutJSON extends Workout
{
    private int userGender;
    private List<Lap> laps;
    private List<Point> points;

    public WorkoutJSON( List<Lap> laps )
    {
        this.laps = laps;
    }

    public int getUserGender()
    {
        return userGender;
    }

    public void setUserGender( int userGender )
    {
        this.userGender = userGender;
    }

    public List<Lap> getLaps()
    {
        return laps;
    }

    public List<Point> getPoints()
    {
        return points;
    }

    public void setPoints( List<Point> points )
    {
        this.points = points;
    }

    public List<Lap> pointsToLaps()
    {
        List<Lap> laps = null;
        // less than 2 points is not a line/polyline
        if (points != null && points.size() > 1)
        {
            laps = new ArrayList<>();
            int order = 0; // for lap id
            Polyline polyline = new Polyline();
            Point previous = points.get( 0 );
            polyline.addPoint( previous );
            Lap lap = new Lap(previous.getLat(), previous.getLon());
            double tolerance = 1E-6;
            for (int i = 1; i < points.size(); i++)
            {
                Point current = points.get( i );
                if (Math.abs( current.getDistance() - previous.getDistance() ) >= tolerance)
                {
                    lap.setEndLat( current.getLat() );
                    lap.setEndLon( current.getLon() );
                    polyline.addPoint( current );
                    lap.setWorkoutId( getId() );
                    lap.setId( order );
                    lap.setSmallPolyline( polyline );
                    lap.setOffset( previous.getTimeCaptured());
                    lap.setDuration( current.getTimeCaptured().getMillis() - previous.getTimeCaptured().getMillis() );
                    laps.add( lap );
                    order++;

                    previous = current;
                    lap = new Lap(previous.getLat(), previous.getLon());
                    polyline = new Polyline();
                    polyline.addPoint( previous );
                }
            }
        }
        return laps;
    }

    @Override
    public String toString()
    {
        String gender;
        switch (userGender)
        {
            case 0:
                gender = "male";
                break;
            case 1:
                gender = "female";
                break;
            default:
                gender = "unknown";
        }
        StringBuilder workoutString = new StringBuilder(  );
        workoutString.append( this.getId() );
        workoutString.append( " [" );
        workoutString.append( gender );
        workoutString.append( "], " );

        if (laps != null && laps.size() > 0)
        {

            workoutString.append( laps.size() );
            workoutString.append( " laps:\n" );
            for ( Lap lap: laps )
            {
                workoutString.append( lap );
                workoutString.append( "\n" );
            }
        }

        if (points != null && points.size() > 0)
        {
            workoutString.append( points.size() );
            workoutString.append( " points:\n" );
            if (points.get( 0 ).getTimeCaptured().getDayOfMonth() ==
                points.get( points.size() - 1 ).getTimeCaptured().getDayOfMonth())
            {
                workoutString.append( points.get( 0 ).getTimeCaptured().toString( "yyyy-MM-dd" ));
                workoutString.append( '\n' );
                for (int i = 0; i < points.size(); i++)
                {
                    workoutString.append( points.get( i ).toStringTimeOnly() );
                    workoutString.append( " " );
                    if (i % 5 == 0) workoutString.append( "\n" );
                }
            }
            else
            {
                for (int i = 0; i < points.size(); i++)
                {
                    workoutString.append( points.get( i ).toString() );
                    workoutString.append( " " );
                    if (i % 3 == 0) workoutString.append( "\n" );
                }
            }
        }

        return workoutString.toString();
    }
}
