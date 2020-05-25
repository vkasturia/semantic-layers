/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ranking_algo_and;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Vaibhav Kasturia <kasturia at l3s.de>
 * 
 * Class to get combination of Timeliness, Relativeness and Relatedness Scores  
 */

public class ScoreCombos {
   public void scoreCombination(Map<String, Double> article_timelinessScore_map, Map<String, Double> article_relativenessScore_map, Map<String, Double> article_relatednessScore_map, int counter) throws FileNotFoundException
   {
        ScoreCombos score_combos = new ScoreCombos();
       
        //Decay Factor Beta
//        double[] BetaArray = new double[]{0.2, 0.4, 0.6, 0.8, 1.0};
        
        //Data Structures to store combination of scores 
        Map<String, Double> article_time_relativeness_map = new LinkedHashMap<>();
        Map<String, Double> article_relative_relatedness_map = new LinkedHashMap<>();
        Map<String, Double> article_time_relatedness_map = new LinkedHashMap<>();
        Map<String, Double> article_time_relative_relatedness_map = new LinkedHashMap<>();
        
//        Map<String, Double> article_allScoresCombined_map1 = new LinkedHashMap<>();
//        Map<String, Double> article_allScoresCombined_map2 = new LinkedHashMap<>();
//        Map<String, Double> article_allScoresCombined_map3 = new LinkedHashMap<>();
//        Map<String, Double> article_allScoresCombined_map4 = new LinkedHashMap<>();
//        Map<String, Double> article_allScoresCombined_map5 = new LinkedHashMap<>();
//        List<Map<String, Double>> article_allScoresCombined_maplist = new ArrayList();
           
        article_relativenessScore_map.entrySet().stream().forEach((article) -> {
           double time_relativenessScore = 0;
           double relative_relatednessScore = 0;
           double time_relative_relatednessScore = 0;
           
           try {
            
               time_relativenessScore = article.getValue() * article_timelinessScore_map.get(article.getKey());
               relative_relatednessScore = article.getValue() * article_relatednessScore_map.get(article.getKey());
               time_relative_relatednessScore = 10000 * article.getValue() * article_timelinessScore_map.get(article.getKey()) * article_relatednessScore_map.get(article.getKey());
               
               article_time_relativeness_map.put(article.getKey(), time_relativenessScore);
               article_relative_relatedness_map.put(article.getKey(), relative_relatednessScore);
               article_time_relative_relatedness_map.put(article.getKey(), time_relative_relatednessScore);

//               double combinedScore = 0;
//
//               combinedScore = time_relativenessScore + (BetaArray[0] * article_relatednessScore_map.get(article.getKey()));
//               article_allScoresCombined_map1.put(article.getKey(), combinedScore);
//
//               combinedScore = time_relativenessScore + (BetaArray[1] * article_relatednessScore_map.get(article.getKey()));
//               article_allScoresCombined_map2.put(article.getKey(), combinedScore);
//
//               combinedScore = time_relativenessScore + (BetaArray[2] * article_relatednessScore_map.get(article.getKey()));
//               article_allScoresCombined_map3.put(article.getKey(), combinedScore);
//
//               combinedScore = time_relativenessScore + (BetaArray[3] * article_relatednessScore_map.get(article.getKey()));
//               article_allScoresCombined_map4.put(article.getKey(), combinedScore);
//
//               combinedScore = time_relativenessScore + (BetaArray[4] * article_relatednessScore_map.get(article.getKey()));
//               article_allScoresCombined_map5.put(article.getKey(), combinedScore);
            
           } catch (NullPointerException e) {
               e.printStackTrace();
           }
           
       });
        
       article_timelinessScore_map.entrySet().stream().forEach((article) -> {
          double time_relatednessScore = 0;
          try{
             time_relatednessScore =  article.getValue() * article_relatednessScore_map.get(article.getKey());
             article_time_relatedness_map.put(article.getKey(), time_relatednessScore);
          } catch (NullPointerException e) {
              e.printStackTrace();
          }
       });
       
       Map<String, Double> article_time_relativeness_map_sorted = new LinkedHashMap<>(); 
       article_time_relativeness_map_sorted = score_combos.sortHashMapByValues(article_time_relativeness_map);
       
       Map<String, Double> article_relative_relatedness_map_sorted = new LinkedHashMap<>(); 
       article_relative_relatedness_map_sorted = score_combos.sortHashMapByValues(article_relative_relatedness_map);
       
       Map<String, Double> article_time_relatedness_map_sorted = new LinkedHashMap<>(); 
       article_time_relatedness_map_sorted = score_combos.sortHashMapByValues(article_time_relatedness_map);
       
       Map<String, Double> article_time_relative_relatedness_map_sorted = new LinkedHashMap<>(); 
       article_time_relative_relatedness_map_sorted = score_combos.sortHashMapByValues(article_time_relative_relatedness_map);
       
       
//       Map<String, Double> article_allScoresCombined_map1_sorted = new LinkedHashMap<>();
//       article_allScoresCombined_map1_sorted = score_combos.sortHashMapByValues(article_allScoresCombined_map1);
//       article_allScoresCombined_maplist.add(article_allScoresCombined_map1_sorted);
//
//       Map<String, Double> article_allScoresCombined_map2_sorted = new LinkedHashMap<>();
//       article_allScoresCombined_map2_sorted = score_combos.sortHashMapByValues(article_allScoresCombined_map2);
//       article_allScoresCombined_maplist.add(article_allScoresCombined_map2_sorted);
//
//       Map<String, Double> article_allScoresCombined_map3_sorted = new LinkedHashMap<>();
//       article_allScoresCombined_map3_sorted = score_combos.sortHashMapByValues(article_allScoresCombined_map3);
//       article_allScoresCombined_maplist.add(article_allScoresCombined_map3_sorted);
//
//       Map<String, Double> article_allScoresCombined_map4_sorted = new LinkedHashMap<>();
//       article_allScoresCombined_map4_sorted = score_combos.sortHashMapByValues(article_allScoresCombined_map4);
//       article_allScoresCombined_maplist.add(article_allScoresCombined_map4_sorted);
//
//       Map<String, Double> article_allScoresCombined_map5_sorted = new LinkedHashMap<>();
//       article_allScoresCombined_map5_sorted = score_combos.sortHashMapByValues(article_allScoresCombined_map5);
//       article_allScoresCombined_maplist.add(article_allScoresCombined_map5_sorted);
        
        //Printing each article and the different score combinations in a csv file
        PrintWriter writer = new PrintWriter("./results/"+ counter +"/ranking_time_relativeness"+counter+".csv");
	writer.println("Article; Time_Relativeness_Score");
        article_time_relativeness_map_sorted.entrySet().stream().forEach((entry) -> {
           writer.println(entry.getKey() + "; "+ entry.getValue());
        });
        writer.close();
    
        PrintWriter writer2 = new PrintWriter("./results/"+ counter +"/ranking_relative_relatedness"+counter+".csv");
        writer2.println("Article; Relative_Relatedness_Score");
        article_relative_relatedness_map_sorted.entrySet().stream().forEach((entry) -> {
           writer2.println(entry.getKey() + "; "+ entry.getValue());
        });
        writer2.close();    
        
        PrintWriter writer3 = new PrintWriter("./results/"+ counter +"/ranking_time_relatedness"+counter+".csv");
        writer3.println("Article; Time_Relatedness_Score");
        article_time_relatedness_map_sorted.entrySet().stream().forEach((entry) -> {
           writer3.println(entry.getKey() + "; "+ entry.getValue());
        });
        writer3.close();    
        
        PrintWriter writer4 = new PrintWriter("./results/"+ counter +"/ranking_time_relative_relatedness"+counter+".csv");
        writer4.println("Article; Time_Relative_Relatedness_Score");
        article_time_relative_relatedness_map_sorted.entrySet().stream().forEach((entry) -> {
           writer4.println(entry.getKey() + "; "+ entry.getValue());
        });
        writer4.close();
        
        
//        for(int z = 0; z < BetaArray.length; z++){
//            PrintWriter writer3 = new PrintWriter("./results/ranking"+counter+"combination_beta="+BetaArray[z]+".csv");
//            //writer3.println("Article; Combined_Score_Beta="+BetaArray[z]);
//            article_allScoresCombined_maplist.get(z).entrySet().stream().forEach((entry) -> {
//            writer3.println(entry.getKey() + "; "+ entry.getValue());
//            });
//            writer3.close();
//        }
   }
   
   public static LinkedHashMap<String, Double> sortHashMapByValues(Map<String, Double> passedMap) {
        List<String> mapKeys = new ArrayList<>(passedMap.keySet());
        List<Double> mapValues = new ArrayList<>(passedMap.values());
        Collections.sort(mapValues, Collections.reverseOrder());
        Collections.sort(mapKeys, Collections.reverseOrder());

        LinkedHashMap<String, Double> sortedMap
                = new LinkedHashMap<>();

        Iterator<Double> valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            Double val = valueIt.next();
            Iterator<String> keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                String key = keyIt.next();
                Double comp1 = passedMap.get(key);
                Double comp2 = val;

                if (comp1.equals(comp2)) {
                    keyIt.remove();
                    sortedMap.put(key, val);
                    break;
                }
            }
        }
        return sortedMap;
    }
}
