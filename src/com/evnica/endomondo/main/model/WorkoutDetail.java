package com.evnica.endomondo.main.model;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
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
    private int id;
    private double distance = 0,
                   duration = 0;
    private DateTime startAt;
    private int weather,
                userId = -1,
                showMap = -1, sport = 0;
    private WorkoutGeometryType workoutGeometryType;
    private List<Lap> laps = new ArrayList<>();
    private List<Point> points = new ArrayList<>();
    private DateTimeZone timeZone = DateTimeZone.UTC;
    private int pointCount = 0, lapCount = 0;
    private double speed = 0;

    public WorkoutDetail(int id) {
        this.id = id;
    }

    public WorkoutDetail() {}

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public int getPointCount() {
        return pointCount;
    }

    public void setPointCount(int pointCount) {
        this.pointCount = pointCount;
    }

    public int getLapCount() {
        return lapCount;
    }

    public void setLapCount(int lapCount) {
        this.lapCount = lapCount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public DateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(DateTime startAt) {
        this.startAt = startAt;
    }

    public int getWeather() {
        return weather;
    }

    public void setWeather(int weather) {
        this.weather = weather;
    }

    public int getSport() {
        return sport;
    }

    public void setSport(int sport) {
        this.sport = sport;
    }

    public int getShowMap() {
        return showMap;
    }

    public void setShowMap(int showMap) {
        this.showMap = showMap;
    }

    public WorkoutGeometryType getWorkoutGeometryType() {
        return workoutGeometryType;
    }

    public void setWorkoutGeometryType(WorkoutGeometryType workoutGeometryType) {
        this.workoutGeometryType = workoutGeometryType;
    }

    public DateTimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(DateTimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
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

/*    public Point getPoint(int i)
    {
        return points.get(i);
    }*/

    public void addPoint(Point point)
    {
        points.add(point);
    }

    public void addLap(Lap lap)
    {
        laps.add(lap);
    }

/*    public String toJSONString()
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
    }*/


}
