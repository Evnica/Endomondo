package com.evnica.endomondo.matching;

import org.joda.time.DateTime;

/**
 * Project: Endomondo
 * Class: TractWorkoutAthleteDetail
 * Version: 0.1
 * Created on 5/22/2017 with the help of IntelliJ IDEA (thanks!)
 * Author: DS
 * Description:
 */
class TractWorkoutAthleteDetail
{
     String tractId;

     int    wrktId,
            athleteId,
            gender,
            sport;
     Integer age = null;
     double distance,
            duration,
            speed;
     DateTime startAt;

     String getTractId() {
          return tractId;
     }
}
