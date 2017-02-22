package com.evnica.endomondo.main.model;

import org.joda.time.DateTime;

/**
 * Class: Workout
 * Version: 0.1
 * Created on 21.02.2017 with the help of IntelliJ IDEA (thanks!)
 * Author: Evnica
 * Description:
 */
public class Workout
{
    private int id;
    // 1 - transportation cycling, 2 - sport cycling, 3 mountain biking
    private int sport;
    private DateTime localStartTime;
    private int userId;

    public Workout( int id, int sport, DateTime localStartTime, int userId )
    {
        this.id = id;
        this.sport = sport;
        this.localStartTime = localStartTime;
        this.userId = userId;
    }

    public int getId()
    {
        return id;
    }

    public int getSport()
    {
        return sport;
    }

    public DateTime getLocalStartTime()
    {
        return localStartTime;
    }

    public int getUserId()
    {
        return userId;
    }
}
