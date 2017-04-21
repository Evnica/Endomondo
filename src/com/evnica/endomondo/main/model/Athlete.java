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
    private String country = "all";
    private List<SummaryBySport> summaryBySport = new ArrayList<>();
    public DateTime retrieved = new DateTime();


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

    @Override
    public String toString()
    {
        String dob, created, retrieved;
        //25424028	0	1984-06-16	194	TH	2015-10-27 04:30:41	2017-02-15
        if (createdDate == null) created = "NULL"; //"1800-01-01 00:00:00";
            else created = createdDate.toString("yyyy-MM-dd HH:mm:ss");
        if (dateOfBirth == null) dob = "NULL"; //"1800-01-01";
            else dob = dateOfBirth.toString("yyyy-MM-dd");
        if (this.retrieved == null) retrieved = "NULL"; //"1800-01-01";
        else retrieved  = this.retrieved.toString("yyyy-MM-dd");

        return id + "\t" +  gender + "\t" + dob + "\t" + workoutCount
                + "\t" + country.toLowerCase() + "\t" + created + "\t"
                + retrieved;
    }
}
