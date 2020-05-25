/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ranking_fscore;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Vaibhav Kasturia <kasturia at l3s.de>
 * 
 * Program to Calculate Precision, Recall and F-Score for all ranking files and store output in a CSV File
 */

public class ranking_fscore {
   public static void main(String[] args) throws FileNotFoundException, IOException{
       
       ranking_fscore parameter_calc = new ranking_fscore();
       
       //Ground Truth file for getting the relevance score of the documents 
       String groundTruth_csvFile = "./5_SPARQL.csv";
       
       //Data structure to store Precision for all ranking files
       List<List<Double>> Precision_ListofLists = new LinkedList<>();
       
       //Data structure to store Recall for all ranking files
       List<List<Double>> Recall_ListofLists = new LinkedList<>();
       
       //Data structure to store FScore for all ranking files
       List<List<Double>> FScore_ListofLists = new LinkedList<>();
       
       
       String outputLine = "";
       String outputLine2 = "";
       String outputLine3 = "";
       
       BufferedReader br = null;
       String line = "";
       
       //Specify the separator used to split the rest of the csv files
       String restFilesSplitBy = "; ";
       
       Map<String, Double> groundTruth_Map = new HashMap<String, Double>();
       
       br = new BufferedReader(new FileReader(groundTruth_csvFile));
       while ((line = br.readLine()) != null) {
           String[] article_info = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
           groundTruth_Map.put(article_info[0], Double.parseDouble(article_info[3]));
       }
       
       //Files to calculate Precision, Recall and F-Scores 
       File dir = new File("./AND_files/");
        
       File[] directoryListing = dir.listFiles(new FilenameFilter() {
           @Override
           public boolean accept(File dir, String name) {
               return !name.equals(".DS_Store");
           }
       });
       
       if (directoryListing != null) {
           //Create a CSV File for writing NDCG Outputs
           PrintWriter writer = new PrintWriter("./Precision.csv");
           outputLine += "Precision";
           
           PrintWriter writer2 = new PrintWriter("./Recall.csv");
           outputLine2 += "Recall";
           
           PrintWriter writer3 = new PrintWriter("./FScore.csv");
           outputLine3 += "F-Score";
           
           int u = 0;
           
           for (File child : directoryListing) {
               String b = ", "+child.getName();
               outputLine += b;
               outputLine2 += b;
               outputLine3 += b;
           }
           outputLine += "\n";
           outputLine2 += "\n";
           outputLine3 += "\n";
           
           for (File child : directoryListing) {
               // Store file input in Article_Ranking_map
               Map<String, Double> article_ranking_map = new LinkedHashMap<>();

               br = new BufferedReader(new FileReader(child));
               while ((line = br.readLine()) != null) {
                   String[] article_info = line.split(restFilesSplitBy);
                   if(groundTruth_Map.get(article_info[0]) != null)
                     article_ranking_map.put(article_info[0], groundTruth_Map.get(article_info[0]));
               }
               
               List<Double> Precision_List = new LinkedList<>();
               Precision_List = parameter_calc.calculatePrecision(article_ranking_map);
               Precision_ListofLists.add(Precision_List);
               
               List<Double> Recall_List = new LinkedList<>();
               Recall_List = parameter_calc.calculateRecall(article_ranking_map);
               Recall_ListofLists.add(Recall_List);

          }
           
           String[] Ranking_Position = new String[]{"@5", "@10", "@20", "end"};

           for (int y = 0; y < 4; y++) {
               outputLine += Ranking_Position[y];
               
               for (List<Double> CurrentList : Precision_ListofLists) {
                   String c = ", " + (CurrentList.get(y));                   
                   outputLine += c;
               }
               outputLine += "\n";
               
               outputLine2 += Ranking_Position[y];
               
               for (List<Double> CurrentList : Recall_ListofLists) {
                   String d = ", " + (CurrentList.get(y));                   
                   outputLine2 += d;
               }
               outputLine2 += "\n";
               
               outputLine3 += Ranking_Position[y];
               
               for(int t = 0; t < Precision_ListofLists.size(); t++){
                   String e =", " + (2 * Precision_ListofLists.get(t).get(y) * Recall_ListofLists.get(t).get(y) / (Precision_ListofLists.get(t).get(y) + Recall_ListofLists.get(t).get(y)));
                   outputLine3 += e;
               }
               
               outputLine3 += "\n";
               
           }
           
           writer.println(outputLine);
           writer2.println(outputLine2);
           writer3.println(outputLine3);
           
           writer.close();
           writer2.close();
           writer3.close();
           
       } 
       else {
            // In case where directory is not correctly input
            // Print warning and terminate the program
            if(!dir.isDirectory()){
               System.out.println("Error in directory path input");
               System.exit(1);
            }
       } 
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

   public List<Double> calculatePrecision(Map<String, Double> article_ranking_map) {
        
        List<Double> Precision = new LinkedList<>();
        
        double[] prec_values = new double[]{0 , 0, 0, 0};
        double prec_endposition = 0;
        int[] position = new int[]{5, 10, 20, 10000};
        
        int z = 1;
        for (int j = 0; j < position.length; j++) {
            for (Map.Entry<String, Double> entry : article_ranking_map.entrySet()) {

                if(entry.getValue() == 2 || entry.getValue() == 3)
                    prec_values[j] += 1;
                z += 1;
                if (position[j] != 10000) {
                    if (z == position[j] + 1) {
                        Precision.add(prec_values[j]/position[j]);
                        z = 1;
                        break;
                    }
                } else {
                    if(entry.getValue() == 2 || entry.getValue() == 3)
                        prec_endposition += 1;
                }
            }
        }
        Precision.add(prec_endposition / article_ranking_map.size());

        return Precision;
   }
   
    public List<Double> calculateRecall(Map<String, Double> article_ranking_map) {

        List<Double> Recall = new LinkedList<>();

        double[] recall_values = new double[]{0, 0, 0, 0};
        double recall_endposition = 0;
        double total_relevant_results = 0; 
        
        int[] position = new int[]{5, 10, 20, 10000};

        int z = 1;
        
        for (Map.Entry<String, Double> entry : article_ranking_map.entrySet()) {
            if (entry.getValue() == 2 || entry.getValue() == 3)
                total_relevant_results += 1;
        }
        
        for (int j = 0; j < position.length; j++) {
            for (Map.Entry<String, Double> entry : article_ranking_map.entrySet()) {

                if (entry.getValue() == 2 || entry.getValue() == 3) {
                    recall_values[j] += 1;
                }
                z += 1;
                if (position[j] != 10000) {
                    if (z == position[j] + 1) {
                        if(total_relevant_results > 0)
                            Recall.add(recall_values[j] / total_relevant_results);
                        else 
                            Recall.add(0.0);
                        z = 1;
                        break;
                    }
                } else {
                    if (entry.getValue() == 2 || entry.getValue() == 3) {
                        recall_endposition += 1;
                    }
                }
            }
        }
        
        if(total_relevant_results > 0)
            Recall.add(recall_endposition / total_relevant_results);
        else 
            Recall.add(0.0);
        
        return Recall;
    }

}
