package com.evnica.endomondo;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Project: Endomondo
 * Class: com.evnica.endomondo.MessCleaner
 * Version: 0.1
 * Created on 4/18/2017 with the help of IntelliJ IDEA (thanks!)
 * Author: DS
 * Description:
 */
public class MessCleanerTest {
/*
    @Test
    public void getDuplicateIds() throws Exception
    {
        ArrayList<Integer> ids = MessCleaner.getDuplicateIds("T:\\geomatics\\Dariia\\complex retrieval\\logs\\3800000-4000000\\dbs_wrkt.tsv");
        System.out.println(ids.size());
        int cnt = MessCleaner.moveDuplicates
                ("T:\\geomatics\\Dariia\\complex retrieval\\in_db\\workout\\INTERIM\\15600000-15900000-part2/",
                "T:\\geomatics\\Dariia\\complex retrieval\\in_db\\workout\\INTERIM\\dupl/", ids);
        System.out.println(cnt);
    }*/

    @Test
    public void dbCheck() throws Exception
    {
        ArrayList<Integer> idsInDb = new MessCleaner().getIdsPresentInDb
                ("T:\\geomatics\\Dariia\\complex retrieval\\in_db\\workout\\INTERIM\\15600000-15900000-part2");
        System.out.println("IN DB:");
        idsInDb.forEach(System.out::println);
    }

}