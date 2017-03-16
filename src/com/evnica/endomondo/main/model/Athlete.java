package com.evnica.endomondo.main.model;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

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
                gender = -1,
                workoutCount = 0;
    private DateTime dateOfBirth, createdDate;
    private String country;
    private List<SummaryBySport> summaryBySport = new ArrayList<>();

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

    int getWorkoutCount()
    {
        return workoutCount;
    }

    public void setWorkoutCount( int workoutCount )
    {
        this.workoutCount = workoutCount;
    }

    DateTime getDateOfBirth()
    {
        return dateOfBirth;
    }

    public void setDateOfBirth( DateTime dateOfBirth )
    {
        this.dateOfBirth = dateOfBirth;
    }

    DateTime getCreatedDate()
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

    public void addSummaryBySport(SummaryBySport summary)
    {
        summaryBySport.add(summary);
    }

    public List<SummaryBySport> getSummaryBySport()
    {
        return summaryBySport;
    }

    public int getId()
    {
        return id;
    }
}
