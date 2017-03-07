package com.evnica.endomondo.main.model;

import org.joda.time.DateTime;

/**
 * Class: Athlete
 * Version: 0.1
 * Created on 06.03.2017 with the help of IntelliJ IDEA (thanks!)
 * Author: Evnica
 * Description:
 */
public class Athlete
{
    private int id,
                gender,
                workoutCount,
                favouriteSport;
    private DateTime dateOfBirth, createdDate;
    private String country;
    private SummaryBySport[] summaryBySport = new SummaryBySport[3];

    public Athlete( int id )
    {
        this.id = id;
    }

    public int getGender()
    {
        return gender;
    }

    public void setGender( int gender )
    {
        this.gender = gender;
    }

    public int getWorkoutCount()
    {
        return workoutCount;
    }

    public void setWorkoutCount( int workoutCount )
    {
        this.workoutCount = workoutCount;
    }

    public int getFavouriteSport()
    {
        return favouriteSport;
    }

    public void setFavouriteSport( int favouriteSport )
    {
        this.favouriteSport = favouriteSport;
    }

    public DateTime getDateOfBirth()
    {
        return dateOfBirth;
    }

    public void setDateOfBirth( DateTime dateOfBirth )
    {
        this.dateOfBirth = dateOfBirth;
    }

    public DateTime getCreatedDate()
    {
        return createdDate;
    }

    public void setCreatedDate( DateTime createdDate )
    {
        this.createdDate = createdDate;
    }

    public String getCountry()
    {
        return country;
    }

    public void setCountry( String country )
    {
        this.country = country;
    }

    public SummaryBySport[] getSummaryBySport()
    {
        return summaryBySport;
    }

    public void setSummaryBySport( SummaryBySport[] summaryBySport )
    {
        this.summaryBySport = summaryBySport;
    }

    public int getId()
    {
        return id;
    }
}
