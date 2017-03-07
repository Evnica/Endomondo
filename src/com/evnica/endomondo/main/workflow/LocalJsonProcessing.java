package com.evnica.endomondo.main.workflow;

import com.evnica.endomondo.main.model.Athlete;
import com.evnica.endomondo.main.model.SummaryBySport;
import com.evnica.endomondo.main.model.User;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class: LocalJsonProcessing
 * Version: 0.1
 * Created on 06.03.2017 with the help of IntelliJ IDEA (thanks!)
 * Author: Evnica
 * Description:
 */
public class LocalJsonProcessing
{
/*    public static Athlete parseAthlete( String jsonContent)
    {
        Athlete athlete = null;
        JSONObject userObject = new JSONObject( jsonContent );
        try
        {
            int id = userObject.getInt( "id" );
            int gender = userObject.getInt( "gender" );
            if (userObject.getInt( "workout_count" ) > 0)
            {
                athlete = new Athlete(id);
                JSONArray summaryBySport = userObject.getJSONArray( "summary_by_sport" );
                JSONObject individualSummary;
                int sportId;
                for (int i = 0; i < summaryBySport.length(); i++)
                {
                    individualSummary = summaryBySport.getJSONObject( i );
                    sportId = individualSummary.getInt( "sport" );
                    if (sportId == 1)
                    {
                        SummaryBySport summary1 = new SummaryBySport();
                        summary1.sport = 1;
                        summary1.count = individualSummary.getInt( "count" );
                        summary1.totalDistance = individualSummary.getDouble( "total_distance" );
                        summary1.totalDuration = individualSummary.getDouble( "total_duration" );

                    }
                    else if (sportId == 2)
                        athlete.setCyclingSportCount(  );
                    else if (sportId == 3)
                        athlete.setMountainBikingCount( individualSummary.getInt( "count" ) );
                }
                if (athlete.getCyclingSportCount() > 0 || athlete.getMountainBikingCount() > 0 ||
                        athlete.getCyclingTransportCount() > 0)
                {
                    athlete.setId( id );
                    athlete.setGender( gender );
                    String date = userObject.getString( "created_date" );
                    date = date.substring( 0, 19 );
                    athlete.setDateCreated( formatter.parseDateTime( date ) );
                }
                else
                {
                    athlete = null;
                }
            }

        }
        catch ( JSONException e )
        {
            System.out.println("Except: " + e);
        }
        return athlete;
    }*/

}
