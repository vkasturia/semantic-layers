package com.vaibhav.ranking_cat_pagerank;

/**
 * @author Vaibhav Kasturia <kasturia at l3s.de>
 * <p>
 * The main class: Reads inputs from file, passes input to different files for ranking and generates the NDCG
 */

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.vaibhav.ranking_cat_pagerank.pagerank.GraphCreator;
import com.vaibhav.ranking_cat_pagerank.ranking_ndcg.NDCG_Calc;
import com.vaibhav.ranking_cat_pagerank.relatedness.RelatednessScore;
import com.vaibhav.ranking_cat_pagerank.relativeness.RelativenessScore;
import com.vaibhav.ranking_cat_pagerank.timeliness.ImportanceScoreP;
import com.vaibhav.ranking_cat_pagerank.timeliness.TimelinessScore;

public class ranking_algo_main {

    public static void main(String[] args) throws FileNotFoundException, IOException {

        //File in which the query parameters are stored 
        String file = "./query_param";

        int counter = 22;

        //Read file
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"))) {
            String str;
            while ((str = in.readLine()) != null) {
                //System.out.println(str);
                if (str.startsWith("#")) {
                    counter += 1;
                    continue;
                }

                String[] data = str.split("\t");

                String Category = data[0].trim();

                if (data.length != 4) {
                    System.out.println("*** Malformed line! Continuing to next line...");
                }

                //Period of Interest P(Q)
                String fromDate = data[1].trim();
                String toDate = data[2].trim();
                String timePeriod = data[3].trim();

                //Getting the Entities of Interest from the Category
                Cat_to_entities categoryToEntities = new Cat_to_entities();
                String[] entityArray = categoryToEntities.entitiesreturn(Category, fromDate, toDate);

                //Get Importance Score for each Time Period P
                ImportanceScoreP impscore = new ImportanceScoreP();
                Map<String, Double> impscore_map = impscore.imp_score_return(entityArray, fromDate, toDate, timePeriod);

                //Timeliness Score for each document d
                TimelinessScore timescore = new TimelinessScore();
                Map<String, Double> article_timelinessScore_map = timescore.timeliness_score(entityArray, fromDate, toDate, impscore_map, timePeriod, counter);

                //Relativeness Score for each document d
                RelativenessScore relscore = new RelativenessScore();
                Map<String, Double> article_relativenessScore_map = relscore.relativeness_score(entityArray, fromDate, toDate, counter);


                //Combination of timeliness and relativeness score for each document d
                ScoreCombos combined = new ScoreCombos();
                Map<String, Double> article_time_relativeness_map = combined.scoreCombination(article_timelinessScore_map, article_relativenessScore_map, counter);

                //Map of all query documents and their extracted entities 
                Test_Docs_and_Entities docs_entities = new Test_Docs_and_Entities();
                Object[] returned_values = docs_entities.docs_entities_return(entityArray, fromDate, toDate);
                Map<String, Map<String, Double>> docs_entities_entitycount_map = (Map<String, Map<String, Double>>) returned_values[0];
                Map<String, Map<String, Double>> entity_docs_entitycount_map = (Map<String, Map<String, Double>>) returned_values[1];

                //Extracting the docs and entities out of the docs_entities_entitycount_map
                List<String> entityList;
                Map<String, List<String>> docs_entities_map = new LinkedHashMap();

                for (Map.Entry<String, Map<String, Double>> entry : docs_entities_entitycount_map.entrySet()) {
                    entityList = new LinkedList();
                    for (Map.Entry<String, Double> entry2 : entry.getValue().entrySet()) {
                        entityList.add(entry2.getKey());
                        docs_entities_map.put(entry.getKey(), entityList);
                    }
                }

                for (Map.Entry<String, List<String>> entry : docs_entities_map.entrySet()) {
                    System.out.println(entry.getKey() + " : " + entry.getValue());
                }

                //Map of all entities and their relatedness scores 
                RelatednessScore relatedScore = new RelatednessScore();
                Object[] values = relatedScore.relatedness_score(entityArray, fromDate, toDate, timePeriod, counter);
                Map<String, Double> relEntities_relatednessScore_Map = (Map<String, Double>) values[1];
                Map<String, List<String>> articleUrl_relEntities_Map = (Map<String, List<String>>) values[2];

                for (Map.Entry<String, Double> entry : relEntities_relatednessScore_Map.entrySet()) {
                    System.out.println(entry.getKey() + " : " + entry.getValue());
                }

                //Create the transition Graph to run the algorithm
                GraphCreator graphCreator = new GraphCreator();
                double decayFactor = 0.8;
                graphCreator.createGraph(docs_entities_entitycount_map, entity_docs_entitycount_map, article_time_relativeness_map, relEntities_relatednessScore_Map, docs_entities_map, entityArray.length, entityArray, counter, decayFactor);

                //Generate the NDCG 
                NDCG_Calc ndcg_calc = new NDCG_Calc();
                ndcg_calc.ndcg_table_creator(counter);

                counter += 1;
            }
        }
    }
}    