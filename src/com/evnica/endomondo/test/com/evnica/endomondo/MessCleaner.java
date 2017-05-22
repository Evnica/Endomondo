package com.evnica.endomondo;

import com.evnica.endomondo.main.connect.DbConnector;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Project: Endomondo
 * Class: ${CLASS_NAME}
 * Version: 0.1
 * Created on 4/18/2017 with the help of IntelliJ IDEA (thanks!)
 * Author: DS
 * Description:
 */
public class MessCleaner
{


    static  ArrayList<Integer> getDuplicateIds(String log) throws Exception
    {
        InputStream inputStream = new FileInputStream(new File(log));
        String content = new Scanner(inputStream, "UTF-8").useDelimiter("\\A").next();

        ArrayList<Integer> ids = new ArrayList<>();
        String[] lines = content.split("\n");
        for (String line: lines)
        {
            int id = Integer.parseInt(line.split("\t")[0]);
            ids.add(id);
        }
        return ids;
    }

    static int moveDuplicates(String pathFrom, String pathTo, ArrayList<Integer> ids)
    {
        int count = 0;
        for (int id: ids)
        {
            File file = new File(pathFrom + id + ".json");
            if (file.exists())
            {
                if (file.renameTo(new File(pathTo + id + ".json")))
                {
                    count++;
                    System.out.println(id + " moved");
                }
                else
                {
                    System.out.println(id + " failed to move");
                }
            }
        }
        return count;
    }

    public ArrayList<Integer> getIdsPresentInDb(String folderWithFiles) throws Exception
    {
        File[] files = new File(folderWithFiles).listFiles();
        ArrayList<Integer> idsInDb = new ArrayList<>();
        ArrayList<Integer> jsonIds = new ArrayList<>();
        if (files != null)
        {
            for (File f: files)
            {
                int id = Integer.parseInt(f.getName().split(".j")[0]);
                jsonIds.add(id);
            }
        }

        DbConnector.connectToDb();
        String queryTemplate = "SELECT id, athlete_id, region FROM production.workout WHERE id = %d";

        for (int id: jsonIds)
        {
            String query = String.format(queryTemplate, id);
            Statement statement = DbConnector.getConnection().createStatement();
            ResultSet rs = statement.executeQuery(query);
            while (rs.next())
            {
                int idInDb = rs.getInt("id");
                idsInDb.add(idInDb);
                System.out.println(id + "\t" +rs.getInt("athlete_id") + "\t" + rs.getString("region"));
            }
            statement.close();
            rs.close();
        }
        DbConnector.closeConnection();

        return idsInDb;
    }
}
