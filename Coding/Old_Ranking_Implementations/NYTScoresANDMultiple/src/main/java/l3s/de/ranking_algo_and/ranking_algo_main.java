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

package l3s.de.ranking_algo_and;

/**
 *
 * @author Vaibhav Kasturia (kasturia at l3s.de)
 */
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import relatedness.RelatednessScore;
import relativeness.RelativenessScore;
import timeliness.ImportanceScoreP;
import timeliness.TimelinessScore;

public class ranking_algo_main {

    public static void main(String[] args) throws FileNotFoundException, IOException {
        
        //File in which the query parameters are stored 
        String file = "./query_param";
 
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
                
                String entity = "http://dbpedia.org/resource/Nelson_Mandela";

                //Get Importance Score for each Time Period P
                //For test case granularity set to Month
                ImportanceScoreP impscore = new ImportanceScoreP();
                Map<String, Double> impscore_map = impscore.imp_score_return(entityArray, fromDate, toDate, timePeriod);

                //Timeliness Score for each document d
                TimelinessScore timescore = new TimelinessScore();
                timescore.timeliness_score(entityArray, fromDate, toDate, impscore_map, timePeriod);

                //Relativeness Score for each document d
                RelativenessScore relscore = new RelativenessScore();
                relscore.relativeness_score(entityArray, fromDate, toDate);

                //Relatedness Score for each document d
                RelatednessScore relatedscore = new RelatednessScore();
                relatedscore.relatedness_score(entityArray, fromDate, toDate, timePeriod);
            }
        }
    }
}     