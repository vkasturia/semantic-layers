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
 * Program to get No. of Entities for a Type
 */

public class Type_to_NumEntities {

    public static void main(String[] args) throws IOException {
        for (int u = 1; u < 15; u++) {
            Type_to_NumEntities type_to_num_entities = new Type_to_NumEntities();

            String csvFile = "./entity2types_2016-" + u;

            BufferedReader br = null;

            String line = "";
            String fileSplitBy = "\t";
            String typeSplitBy = "; ";

            HashMap<String, Integer> Type_NumEntities_Map = new LinkedHashMap<>();
            HashMap<String, Integer> Type_NumEntities_SortedMap;
            int numEntities;

            int y = 1;

            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {
                if (line.isEmpty() || line.trim().equals("") || line.trim().equals("\n")) {
                    System.out.println("Skipped a blank line");
                } else {
                    //Using Tab as separator
                    String[] Entity_Types_line = line.split(fileSplitBy, -1);

                    if (Entity_Types_line[1].isEmpty()) {
                        Type_NumEntities_Map.put("No Type", y);
                        y += 1;
                    } else {
                        //Using semi-colon as separator
                        String[] typesLine = Entity_Types_line[1].split(typeSplitBy, -1);
                        for (String type : typesLine) {
                            //Store the Types and Number of Entities in the Type_NumEntities_Map 
                            if (Type_NumEntities_Map.containsKey(type)) {
                                numEntities = Type_NumEntities_Map.get(type);
                                numEntities += 1;
                                Type_NumEntities_Map.put(type, numEntities);
                            } else {
                                Type_NumEntities_Map.put(type, 1);
                            }
                        }
                    }
                }
            }

            Type_NumEntities_SortedMap = type_to_num_entities.sortHashMapByValues(Type_NumEntities_Map);

            PrintWriter writer = new PrintWriter("./types/TypeFile" + u + ".txt");
            for (Map.Entry<String, Integer> entry : Type_NumEntities_SortedMap.entrySet()) {
                writer.println(entry.getKey() + "; " + entry.getValue());
            }
            writer.close();
        }
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
