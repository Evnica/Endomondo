package com.evnica.endomondo.main.model;

import org.joda.time.DateTime;

import java.util.List;

/**
 * Class: WorkoutDetail
 * Version: 0.1
 * Created on 03.03.2017 with the help of IntelliJ IDEA (thanks!)
 * Author: Evnica
 * Description:
 */
public class WorkoutDetail
{
    int id;
    double distance,
           duration;
    DateTime time;
    int weather,
        athlete_id,
        showMap;
    WorkoutGeometryType workoutGeometryType;
    List<Point> points;

public String toJSONString()
{
    StringBuilder jsonBuilder = new StringBuilder();
    if ( points != null && points.size() > 1 )
    {
        jsonBuilder.append( "{\"" );
        jsonBuilder.append( id );
        jsonBuilder.append( "\":[" );
        jsonBuilder.append( points.get( 0 ).toJSONString() );
        for ( int i = 1; i < points.size(); i++ )
        {
            jsonBuilder.append( "," );
            jsonBuilder.append( points.get( i ).toJSONString() );
        }
        jsonBuilder.append( "]}" );
    }
    else
    {
        jsonBuilder.append( "{\"" );
        jsonBuilder.append( id );
        jsonBuilder.append( "\":[]" );
    }

    return jsonBuilder.toString();
}


}
