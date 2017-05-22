package com.evnica.endomondo.test;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Set;
import java.util.TimeZone;

/**
 * Project: Endomondo
 * Class: ${CLASS_NAME}
 * Version: 0.1
 * Created on 3/17/2017 with the help of IntelliJ IDEA (thanks!)
 * Author: DS
 * Description:
 */
public class DateConverter
{
    private static String startTime = "2016-03-12T22:48:38.000Z";
    private static DateTimeFormatter df = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public static void main(String[] args) {

        String test = "2016-03-13T00:53:58.000Z";
        String localStartTime = "2016-03-13T05:48:38.000+07:00";
        DateTime temp = df.withOffsetParsed().parseDateTime(localStartTime);
        DateTimeZone theZone = temp.getZone();
        DateTime t = df.withOffsetParsed().parseDateTime(startTime).withZone(theZone);
        long currentMillis = t.getMillis();
        DateTimeZone localZone = DateTimeZone.forTimeZone(TimeZone.getDefault());
        long nexMillis = t.getZone().getMillisKeepLocal(localZone, currentMillis);
        DateTime d = new DateTime(nexMillis);


        System.out.println(temp);
        System.out.println(t);
        System.out.println(d);


       Set<String> zones =  DateTimeZone.getAvailableIDs();
       zones.forEach(System.out::println);

    }

}
