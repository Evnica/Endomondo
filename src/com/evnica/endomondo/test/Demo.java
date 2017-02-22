package com.evnica.endomondo.test;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class: Demo
 * Version: 0.1
 * Created on 21.02.2017 with the help of IntelliJ IDEA (thanks!)
 * Author: Evnica
 * Description:
 */
public class Demo
{
    public static void main( String[] args ) throws Exception
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat( "dd.MM.yyyy HH:mm" );
        Date workoutDate = dateFormat.parse( "24.10.2015 10:01" );
        System.out.println(workoutDate.getTime()/1000);
    }
}
