/*
Copyright 2017-2022 Vaibhav Kasturia <vbh18kas@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package nyt_and_implement;

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
