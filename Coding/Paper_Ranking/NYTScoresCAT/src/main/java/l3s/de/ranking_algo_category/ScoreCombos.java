/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package l3s.de.ranking_algo_category;

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
       
        
        //Data Structures to store combination of scores 
        Map<String, Double> article_time_relativeness_map = new LinkedHashMap<>();
        Map<String, Double> article_relative_relatedness_map = new LinkedHashMap<>();
        Map<String, Double> article_time_relatedness_map = new LinkedHashMap<>();
        Map<String, Double> article_time_relative_relatedness_map = new LinkedHashMap<>();
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
