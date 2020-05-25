/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ranking_randomized_ndcg;

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
 * Program to Calculate NDCG for by performing random ranking and store output in a CSV File
 */

public class Ranking_randomized_ndcg {
   public static void main(String[] args) throws FileNotFoundException, IOException{
       
       Ranking_randomized_ndcg ndcg_calculator = new Ranking_randomized_ndcg();
       
       //Ground Truth file to calculate IDCG
       String groundTruth_csvFile = "./20_SPARQL.csv";
       
       String outputLine = "";
       
       BufferedReader br = null;
       String line = "";
       
       Map<String, Double> groundTruth_Map = new HashMap<>();
       Map<String, Double> random_Map = new LinkedHashMap<>();
       
       List<String> RandomizedList =new ArrayList<>();
       List<Double> DCG_List = new LinkedList<>();
       
       br = new BufferedReader(new FileReader(groundTruth_csvFile));
       while ((line = br.readLine()) != null) {
           String[] article_info = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
           groundTruth_Map.put(article_info[0], Double.parseDouble(article_info[3]));
           RandomizedList.add(article_info[0]);
       }
       
       Map<String, Double> groundTruth_sortedMap = ndcg_calculator.sortHashMapByValues(groundTruth_Map);
       
       //Calculate IDCG using sorted ground truth map
       List<Double> IDCG_List = new LinkedList<>();
       IDCG_List = ndcg_calculator.calculateDCG(groundTruth_sortedMap);
       
       Collections.shuffle(RandomizedList);
       
       RandomizedList.stream().forEach((element) -> {
           random_Map.put(element, groundTruth_Map.get(element));
       });
       
       groundTruth_csvFile = groundTruth_csvFile.replaceAll("./", "");
           
       //Create a CSV File for writing NDCG Outputs
       PrintWriter writer = new PrintWriter("./results/Output_"+groundTruth_csvFile);
       outputLine += "NDCG";
       
       String b = ", "+groundTruth_csvFile;
       outputLine += b;
       
       outputLine += "\n";
       
       DCG_List = ndcg_calculator.calculateDCG(random_Map);
    
           String[] NDCG_Position = new String[]{"@5", "@10", "@20", "end"};

           for (int y = 0; y < 4; y++) {
               outputLine += NDCG_Position[y];
               String c = ", " + (DCG_List.get(y) / IDCG_List.get(y));                   
               outputLine += c;
               outputLine += "\n";
           }
           
           writer.println(outputLine);
           
           writer.close();
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
   
    public List<Double> calculateDCG(Map<String, Double> article_ranking_map) {

        List<Double> DCG = new LinkedList<>();

        double[] idcg_values = new double[]{0, 0, 0, 0};
        double idcg_endposition = 0;
        int[] position = new int[]{5, 10, 20, 10000};
       
        int z = 1;
        for (int j = 0; j < position.length; j++) {
            for (Map.Entry<String, Double> entry : article_ranking_map.entrySet()) {

                idcg_values[j] += (Math.pow(2, entry.getValue()) - 1.0) * (Math.log(2) / Math.log(z + 1));
                z += 1;
                if (position[j] != 10000) {
                    if (z == position[j] + 1) {
                        DCG.add(idcg_values[j]);
                        z = 1;
                        break;
                    }
                } else {
                    idcg_endposition += (Math.pow(2, entry.getValue()) - 1.0) * (Math.log(2) / Math.log(z + 1));
                }
            }
        }
        DCG.add(idcg_endposition);

        return DCG;
    }
}
