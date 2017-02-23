package com.evnica.endomondo.main.model;

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

    public void setLaps( List<Lap> laps )
    {
        this.laps = laps;
    }

    public List<Point> getPoints()
    {
        return points;
    }

    public void setPoints( List<Point> points )
    {
        this.points = points;
    }
}
