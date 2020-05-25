/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package l3s.de.nyt_entities;

/**
 *
 * @author Vaibhav Kasturia <kasturia at l3s.de>
 * 
 * The main class: Reads inputs from file and passes input to query builder 
 * 
 */

import l3s.de.entity_to_types.Entity_to_Types;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class NYT_Entities_Main {

    public static void main(String[] args) throws FileNotFoundException, IOException {
        
        //File in which the query parameters are stored 
        String file = "./query_param";
 
        int counter = 1;
        
        //Read file
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"))) {
            String str;
            while ((str = in.readLine()) != null) {
                //System.out.println(str);
                if (str.startsWith("#")) {
                    continue;
                }

                int count = 1998 + counter;

                String[] data = str.split("\t");
                
                //Period of Interest
                String fromDate = data[0].trim();
                String toDate = data[1].trim();
                
                if (data.length != 2) {
                    System.out.println("*** Malformed line! Continuing to next line...");
                }
                
                
                //Get popular entities for each period of interest
                Query_Entities query_entities = new Query_Entities();
                List<String> entityURIList = query_entities.entity_finder(fromDate, toDate);
                
                System.out.println("Finished first part of "+count);
                
                //Get type for each Entity 
                Entity_to_Types entity2types = new Entity_to_Types();
                entity2types.entity_to_type(entityURIList, count);
                
                System.out.println("Finished second part of "+count);
                counter += 1;
            }
        }
    }
}        

