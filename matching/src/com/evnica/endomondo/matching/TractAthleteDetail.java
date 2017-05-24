package com.evnica.endomondo.matching;

import org.joda.time.DateTime;

/**
 * Project: Endomondo
 * Class: TractAthleteDetail
 * Version: 0.1
 * Created on 5/23/2017 with the help of IntelliJ IDEA (thanks!)
 * Author: DS
 * Description:
 */
public class TractAthleteDetail
{
    String tractId;
    int    id,
           gender;
    Integer age = null;
    String country;

    String getTractId() {
        return tractId;
    }
}
