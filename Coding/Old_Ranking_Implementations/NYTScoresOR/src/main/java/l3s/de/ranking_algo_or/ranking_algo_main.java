/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package l3s.de.ranking_algo_or;

/**
 *
 * @author vaibhav
 */
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import l3s.de.relatedness.RelatednessScore;
import l3s.de.timeliness.ImportanceScoreP;
import l3s.de.timeliness.TimelinessScore;
import l3s.de.relativeness.RelativenessScore;

public class ranking_algo_main {

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

                String[] data = str.split("\t");
                
                String numOfEntities = data[0].trim();
                int num_of_entities = Integer.parseInt(numOfEntities);
                
                if (data.length != num_of_entities + 4) {
                    System.out.println("*** Malformed line! Continuing to next line...");
                }
                
                //Maintaining an array for the number of entities to be AND together
                String[] entityArray = new String[num_of_entities]; 
                
                //Entities of Interest E(Q)
                for(int i = 0; i < num_of_entities; i++){
                    entityArray[i] = data[i+1].trim();
                }
                
                //Period of Interest P(Q)
                String fromDate = data[num_of_entities+1].trim();
                String toDate = data[num_of_entities+2].trim();
                String timePeriod = data[num_of_entities+3].trim();
                
                //Get Importance Score for each Time Period P
                ImportanceScoreP impscore = new ImportanceScoreP();
                Map<String, Double> impscore_map = impscore.imp_score_return(entityArray, fromDate, toDate, timePeriod);

                //Timeliness Score for each document d
                TimelinessScore timescore = new TimelinessScore();
                timescore.timeliness_score(entityArray, fromDate, toDate, impscore_map, timePeriod, counter);

                //Relativeness Score for each document d
                RelativenessScore relscore = new RelativenessScore();
                relscore.relativeness_score(entityArray, fromDate, toDate, counter);

                //Relatedness Score for each document d
                RelatednessScore relatedscore = new RelatednessScore();
                relatedscore.relatedness_score(entityArray, fromDate, toDate, timePeriod, counter);
                
                counter += 1;
            }
        }
    }
}        

