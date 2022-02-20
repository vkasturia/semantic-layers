/*
* Copyright 2017-2022 Vaibhav Kasturia <vbh18kas@gmail.com>
*
* Permission is hereby granted, free of charge, to any person obtaining a copy of this software 
* and associated documentation files (the "Software"), to deal in the Software without restriction, 
* including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
* and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
* subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in all copies or substantial 
* portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT 
* LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
* IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
* WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE 
* OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/


package ranking_algo_and;

/**
 *
 *  @author Vaibhav Kasturia <kasturia at l3s.de>
 * 
 * The main class: Reads inputs from file and passes input to different files for ranking 
 */

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import position.ProximityScore;
import ranking_ndcg.NDCG_Calc;
import relatedness.RelatednessScore;
import relativeness.RelativenessScore;
import timeliness.ImportanceScoreP;
import timeliness.TimelinessScore;

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
                
                //Get Proximity Score
                ProximityScore proxscore = new ProximityScore();
                Object[] values = proxscore.proximity_score_calc(entityArray, fromDate, toDate, timePeriod);
                Map<String, Map<String, List<Integer>>> document_queryEntity_positionList_map = (Map<String, Map<String, List<Integer>>>) values[0];
                Map<String, Map<String, List<Integer>>> document_relatedEntity_positionList_map = (Map<String, Map<String, List<Integer>>>) values[1];
                Map<String, Map<String, Double>> relEntity_timePeriod_relEntityProximityScoreInPeriod_map = (Map<String, Map<String, Double>>) values[2];

                //Get Importance Score for each Time Period P
                //For test case granularity set to Month
                ImportanceScoreP impscore = new ImportanceScoreP();
                Map<String, Double> impscore_map = impscore.imp_score_return(entityArray, fromDate, toDate, timePeriod);

                //Timeliness Score for each document d
                TimelinessScore timescore = new TimelinessScore();
                Map<String, Double> article_timelinessScore_map = timescore.timeliness_score(entityArray, fromDate, toDate, impscore_map, timePeriod, counter);

                //Relativeness Score for each document d
                RelativenessScore relscore = new RelativenessScore();
                Map<String, Double> article_relativenessScore_map = relscore.relativeness_score(entityArray, fromDate, toDate, counter, document_queryEntity_positionList_map, document_relatedEntity_positionList_map);

                //Relatedness Score for each document d
                RelatednessScore relatedscore = new RelatednessScore();
                Map<String, Double> article_relatednessScore_map = relatedscore.relatedness_score(entityArray, fromDate, toDate, timePeriod, counter, relEntity_timePeriod_relEntityProximityScoreInPeriod_map);
                
                //Combination of scores for each document d
                ScoreCombos combined = new ScoreCombos();
                combined.scoreCombination(article_timelinessScore_map, article_relativenessScore_map, article_relatednessScore_map, counter); 
        
                //Generate NDCG values for query results
                NDCG_Calc ndcg_calc = new NDCG_Calc();
                ndcg_calc.ndcg_table_creator(counter);
              
                counter+=1;
            }
        }
    }
}     