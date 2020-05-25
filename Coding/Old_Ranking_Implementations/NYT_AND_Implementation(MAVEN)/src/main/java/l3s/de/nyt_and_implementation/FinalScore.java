/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package l3s.de.nyt_and_implementation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FinalScore{
    public LinkedHashMap<String, Double> finalscore(double beta, Map<String, String> DocEFPScoreMap, Map<String, String> DEDocScoreMap){
        Map<String, Double> FinalScoreMap = new LinkedHashMap<String, Double>();
        for(Map.Entry<String, String> entry: DocEFPScoreMap.entrySet()){
           
           String docURI = entry.getKey();
           
           String DocEFPScore = entry.getValue();
           DocEFPScore = DocEFPScore.replaceAll("\n", "").replaceAll(" ", "");
           
           String DEDocScore = DEDocScoreMap.get(docURI);
           DEDocScore = DEDocScore.replaceAll("\n", "").replaceAll(" ", "");
           
           double docEFPScore = Double.parseDouble(DocEFPScore);
           double DEdocScore = Double.parseDouble(DEDocScore);
           
           double finalscore = beta * docEFPScore + (1 - beta) * DEdocScore;
           
           FinalScoreMap.put(docURI, finalscore);
        }
        LinkedHashMap<String, Double> rankSortedArticlesMap = sortHashMapByValues(FinalScoreMap);
        return rankSortedArticlesMap;
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
