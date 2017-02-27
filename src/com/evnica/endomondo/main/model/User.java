package com.evnica.endomondo.main.model;

import org.joda.time.DateTime;

/**
 * Class: User
 * Version: 0.1
 * Created on 21.02.2017 with the help of IntelliJ IDEA (thanks!)
 * Author: Evnica
 * Description:
 */
public class User
{
    private int id;
    private int gender; // in JSON 1 - female
    private int cyclingTransportCount = -1;
    private int cyclingSportCount = -1;
    private int mountainBikingCount = -1;
    private DateTime dateCreated;

    public void setId( int id )
    {
        this.id = id;
    }

    public void setGender( int gender )
    {
        this.gender = gender;
    }

    public int getCyclingTransportCount()
    {
        return cyclingTransportCount;
    }

    public void setCyclingTransportCount( int cyclingTransportCount )
    {
        this.cyclingTransportCount = cyclingTransportCount;
    }

    public int getId()
    {
        return id;
    }

    public int getGender()
    {
        return gender;
    }

    public int getCyclingSportCount()
    {
        return cyclingSportCount;
    }

    public void setCyclingSportCount( int cyclingSportCount )
    {
        this.cyclingSportCount = cyclingSportCount;
    }

    public int getMountainBikingCount()
    {
        return mountainBikingCount;
    }

    public void setMountainBikingCount( int mountainBikingCount )
    {
        this.mountainBikingCount = mountainBikingCount;
    }

    public DateTime getDateCreated()
    {
        return dateCreated;
    }

    public void setDateCreated( DateTime dateCreated )
    {
        this.dateCreated = dateCreated;
    }

    @Override
    public String toString()
    {
        return "id: " + id + ";\ngender " + gender + ";\ncycling transport: " + cyclingTransportCount +
                "\ncycling sport: " + cyclingSportCount + "\nmountain biking: " + mountainBikingCount +
                "\ndate created: " + dateCreated.toString( "yyyy-MM-dd'T'HH:mm:ss" );
    }
}
