/*
Copyright 2017-2022 Vaibhav Kasturia <vbh18kas@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ranking_randomized_ndcg;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
 * Program to Calculate NDCG and Precision for Random Ranking Lists
 * 
 */
public class Ranking_randomized_ndcg {

    public static void main(String[] args) throws FileNotFoundException, IOException {
        for (int l = 1; l < 25; l++) {
            Ranking_randomized_ndcg ndcg_calculator = new Ranking_randomized_ndcg();

            //Ground Truth file to calculate IDCG
            String groundTruth_csvFile = "./ground_truth/" + l + "_NEW_SPARQL.csv";

            String outputLine = "";
            
            String outputLine2 = "";

            BufferedReader br = null;
            String line = "";

            Map<String, Double> groundTruth_Map = new HashMap<>();

            Map<String, Double> random_Map1 = new LinkedHashMap<>();
            Map<String, Double> random_Map2 = new LinkedHashMap<>();
            Map<String, Double> random_Map3 = new LinkedHashMap<>();
            Map<String, Double> random_Map4 = new LinkedHashMap<>();
            Map<String, Double> random_Map5 = new LinkedHashMap<>();
            Map<String, Double> random_Map6 = new LinkedHashMap<>();
            Map<String, Double> random_Map7 = new LinkedHashMap<>();
            Map<String, Double> random_Map8 = new LinkedHashMap<>();
            Map<String, Double> random_Map9 = new LinkedHashMap<>();
            Map<String, Double> random_Map10 = new LinkedHashMap<>();

            List<String> RandomizedList = new ArrayList<>();

            List<Double> DCG_List1 = new LinkedList<>();
            List<Double> DCG_List2 = new LinkedList<>();
            List<Double> DCG_List3 = new LinkedList<>();
            List<Double> DCG_List4 = new LinkedList<>();
            List<Double> DCG_List5 = new LinkedList<>();
            List<Double> DCG_List6 = new LinkedList<>();
            List<Double> DCG_List7 = new LinkedList<>();
            List<Double> DCG_List8 = new LinkedList<>();
            List<Double> DCG_List9 = new LinkedList<>();
            List<Double> DCG_List10 = new LinkedList<>();

            List<Double> DCG_List = new LinkedList<>();
            
            List<Double> Precision_List1 = new LinkedList<>();
            List<Double> Precision_List2 = new LinkedList<>();
            List<Double> Precision_List3 = new LinkedList<>();
            List<Double> Precision_List4 = new LinkedList<>();
            List<Double> Precision_List5 = new LinkedList<>();
            List<Double> Precision_List6 = new LinkedList<>();
            List<Double> Precision_List7 = new LinkedList<>();
            List<Double> Precision_List8 = new LinkedList<>();
            List<Double> Precision_List9 = new LinkedList<>();
            List<Double> Precision_List10 = new LinkedList<>();

            List<Double> Precision_List = new LinkedList<>();


            br = new BufferedReader(new FileReader(groundTruth_csvFile));
            br.readLine();
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
                random_Map1.put(element, groundTruth_Map.get(element));
            });

            Collections.shuffle(RandomizedList);

            RandomizedList.stream().forEach((element) -> {
                random_Map2.put(element, groundTruth_Map.get(element));
            });

            Collections.shuffle(RandomizedList);

            RandomizedList.stream().forEach((element) -> {
                random_Map3.put(element, groundTruth_Map.get(element));
            });

            Collections.shuffle(RandomizedList);

            RandomizedList.stream().forEach((element) -> {
                random_Map4.put(element, groundTruth_Map.get(element));
            });

            Collections.shuffle(RandomizedList);

            RandomizedList.stream().forEach((element) -> {
                random_Map5.put(element, groundTruth_Map.get(element));
            });

            Collections.shuffle(RandomizedList);

            RandomizedList.stream().forEach((element) -> {
                random_Map6.put(element, groundTruth_Map.get(element));
            });

            Collections.shuffle(RandomizedList);

            RandomizedList.stream().forEach((element) -> {
                random_Map7.put(element, groundTruth_Map.get(element));
            });

            Collections.shuffle(RandomizedList);

            RandomizedList.stream().forEach((element) -> {
                random_Map8.put(element, groundTruth_Map.get(element));
            });

            Collections.shuffle(RandomizedList);

            RandomizedList.stream().forEach((element) -> {
                random_Map9.put(element, groundTruth_Map.get(element));
            });

            Collections.shuffle(RandomizedList);

            RandomizedList.stream().forEach((element) -> {
                random_Map10.put(element, groundTruth_Map.get(element));
            });

            groundTruth_csvFile = groundTruth_csvFile.replaceAll("./", "");

            //Create a CSV File for writing NDCG Outputs
            PrintWriter writer = new PrintWriter("./results/NDCG_" + l + ".csv");
            outputLine += "NDCG";

            String b = ", " + groundTruth_csvFile;
            outputLine += b;

            outputLine += "\n";

            //Create a CSV File for writing NDCG Outputs
            PrintWriter writer2 = new PrintWriter("./results/Precision_" + l + ".csv");
            outputLine2 += "Precision";

            b = ", " + groundTruth_csvFile;
            outputLine2 += b;

            outputLine2 += "\n";

            
            DCG_List1 = ndcg_calculator.calculateDCG(random_Map1);
            DCG_List2 = ndcg_calculator.calculateDCG(random_Map2);
            DCG_List3 = ndcg_calculator.calculateDCG(random_Map3);
            DCG_List4 = ndcg_calculator.calculateDCG(random_Map4);
            DCG_List5 = ndcg_calculator.calculateDCG(random_Map5);
            DCG_List6 = ndcg_calculator.calculateDCG(random_Map6);
            DCG_List7 = ndcg_calculator.calculateDCG(random_Map7);
            DCG_List8 = ndcg_calculator.calculateDCG(random_Map8);
            DCG_List9 = ndcg_calculator.calculateDCG(random_Map9);
            DCG_List10 = ndcg_calculator.calculateDCG(random_Map10);

            List<List<Double>> listofDCG = new ArrayList<>();

            listofDCG.add(DCG_List1);
            listofDCG.add(DCG_List2);
            listofDCG.add(DCG_List3);
            listofDCG.add(DCG_List4);
            listofDCG.add(DCG_List5);
            listofDCG.add(DCG_List6);
            listofDCG.add(DCG_List7);
            listofDCG.add(DCG_List8);
            listofDCG.add(DCG_List9);
            listofDCG.add(DCG_List10);
            
            Precision_List1 = ndcg_calculator.calculatePrecision(random_Map1);
            Precision_List2 = ndcg_calculator.calculatePrecision(random_Map2);
            Precision_List3 = ndcg_calculator.calculatePrecision(random_Map3);
            Precision_List4 = ndcg_calculator.calculatePrecision(random_Map4);
            Precision_List5 = ndcg_calculator.calculatePrecision(random_Map5);
            Precision_List6 = ndcg_calculator.calculatePrecision(random_Map6);
            Precision_List7 = ndcg_calculator.calculatePrecision(random_Map7);
            Precision_List8 = ndcg_calculator.calculatePrecision(random_Map8);
            Precision_List9 = ndcg_calculator.calculatePrecision(random_Map9);
            Precision_List10 = ndcg_calculator.calculatePrecision(random_Map10);

            List<List<Double>> listofPrecision = new ArrayList<>();

            listofPrecision.add(Precision_List1);
            listofPrecision.add(Precision_List2);
            listofPrecision.add(Precision_List3);
            listofPrecision.add(Precision_List4);
            listofPrecision.add(Precision_List5);
            listofPrecision.add(Precision_List6);
            listofPrecision.add(Precision_List7);
            listofPrecision.add(Precision_List8);
            listofPrecision.add(Precision_List9);
            listofPrecision.add(Precision_List10);


            for (int y = 0; y < 4; y++) {
                double sum = 0;
                for (int i = 0; i < 10; i++) {
                    sum += listofDCG.get(i).get(y);
                }
                sum = sum / 10;
                DCG_List.add(y, sum);
            }

            String[] NDCG_Position = new String[]{"@5", "@10", "@20", "end"};

            for (int y = 0; y < 4; y++) {
                outputLine += NDCG_Position[y];
                String c = ", " + (DCG_List.get(y) / IDCG_List.get(y));
                outputLine += c;
                outputLine += "\n";
            }

            writer.println(outputLine);

            writer.close();
            
            for (int y = 0; y < 4; y++) {
                double sum = 0;
                for (int i = 0; i < 10; i++) {
                    sum += listofPrecision.get(i).get(y);
                }
                sum = sum / 10;
                Precision_List.add(y, sum);
            }

            for (int y = 0; y < 4; y++) {
                outputLine2 += NDCG_Position[y];
                String c = ", " + Precision_List.get(y);
                outputLine2 += c;
                outputLine2 += "\n";
            }

            writer2.println(outputLine2);

            writer2.close();
        
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

    public List<Double> calculatePrecision(Map<String, Double> article_ranking_map) {

        List<Double> Precision = new LinkedList<>();

        double[] prec_values = new double[]{0, 0, 0, 0};
        double prec_endposition = 0;
        int[] position = new int[]{5, 10, 20, 10000};

        int z = 1;
        for (int j = 0; j < position.length; j++) {
            for (Map.Entry<String, Double> entry : article_ranking_map.entrySet()) {

                if (entry.getValue() == 2 || entry.getValue() == 3) {
                    prec_values[j] += 1;
                }
                z += 1;
                if (position[j] != 10000) {
                    if (z == position[j] + 1) {
                        Precision.add(prec_values[j] / position[j]);
                        z = 1;
                        break;
                    }
                } else {
                    if (entry.getValue() == 2 || entry.getValue() == 3) {
                        prec_endposition += 1;
                    }
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
            if (entry.getValue() == 2 || entry.getValue() == 3) {
                total_relevant_results += 1;
            }
        }

        for (int j = 0; j < position.length; j++) {
            for (Map.Entry<String, Double> entry : article_ranking_map.entrySet()) {

                if (entry.getValue() == 2 || entry.getValue() == 3) {
                    recall_values[j] += 1;
                }
                z += 1;
                if (position[j] != 10000) {
                    if (z == position[j] + 1) {
                        if (total_relevant_results > 0) {
                            Recall.add(recall_values[j] / total_relevant_results);
                        } else {
                            Recall.add(0.0);
                        }
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

        if (total_relevant_results > 0) {
            Recall.add(recall_endposition / total_relevant_results);
        } else {
            Recall.add(0.0);
        }

        return Recall;
    }

}
