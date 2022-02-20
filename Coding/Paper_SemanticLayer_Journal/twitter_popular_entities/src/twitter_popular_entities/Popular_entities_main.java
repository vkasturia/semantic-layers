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

package twitter_popular_entities;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Vaibhav Kasturia <kasturia at l3s.de>
 * 
 * Program to find the most popular entities for the entire Twitter Set
 */
public class Popular_entities_main {

    public static void main(String[] args) throws IOException {
        Popular_entities_main popular_entities = new Popular_entities_main();

        String csvFile = "./tweets_us_nps_2016/tweets_us_nps_2016_bfyannotated.csv";

        HashMap<String, Integer> entity_entityCount_map = new LinkedHashMap();
        
        BufferedReader br = null;

        String line = "";
        String csvSplitBy = ";";
        String annotationsSplitBy = "\t";

        List<List<String>> annotationsLists = new ArrayList<>();

        br = new BufferedReader(new FileReader(csvFile));
        while ((line = br.readLine()) != null) {
            //use semicolon as separator
            String[] tweet = line.split(csvSplitBy, -1);

            //Store the annotations in a list
            List<String> bfyAnnotations = new ArrayList();
            String annotationLine = tweet[8];
            String date = tweet[0];

            String[] annotationsinLine = annotationLine.split(annotationsSplitBy);
            for (String annotations : annotationsinLine) {
                bfyAnnotations.add(annotations);
            }
            annotationsLists.add(bfyAnnotations);
        }

        for (int i = 0; i < annotationsLists.size(); i++) {
            List<String> babelfyList = annotationsLists.get(i);
            for(int j = 0; j < babelfyList.size(); j++ ){
                //Store the Entities and their Count in the entity_entityCount_map 
                    if (entity_entityCount_map.containsKey(babelfyList.get(j))) {
                        int entity_count = entity_entityCount_map.get(babelfyList.get(j));
                        entity_count += 1;
                        entity_entityCount_map.put(babelfyList.get(j), entity_count);
                    } else {
                        entity_entityCount_map.put(babelfyList.get(j), 1);
                    }
            }    
        }
        
        Map<String, Integer> Ranked_entity_entityCount_map = popular_entities.sortHashMapByValues(entity_entityCount_map);
        
        //Store the entities and their count in a file
        PrintWriter writer = new PrintWriter("./results/twitter_popular_entities.csv");
	writer.println("Entity; Entity_Count");
        
        for(Map.Entry<String, Integer> entry: Ranked_entity_entityCount_map.entrySet()){
            String entity = entry.getKey();
            //System.out.println("Entity "+ entity);
            int entityCount = entry.getValue();
            //System.out.println("entityCount"+ entityCount);
            writer.println(entity + "; "+ entityCount);
        }
        
        writer.close();
    }
    public static LinkedHashMap<String, Integer> sortHashMapByValues(Map<String, Integer> passedMap) {
        List<String> mapKeys = new ArrayList<>(passedMap.keySet());
        List<Integer> mapValues = new ArrayList<>(passedMap.values());
        Collections.sort(mapValues, Collections.reverseOrder());
        Collections.sort(mapKeys, Collections.reverseOrder());

        LinkedHashMap<String, Integer> sortedMap
                = new LinkedHashMap<>();

        Iterator<Integer> valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            Integer val = valueIt.next();
            Iterator<String> keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                String key = keyIt.next();
                Integer comp1 = passedMap.get(key);
                Integer comp2 = val;

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
     
