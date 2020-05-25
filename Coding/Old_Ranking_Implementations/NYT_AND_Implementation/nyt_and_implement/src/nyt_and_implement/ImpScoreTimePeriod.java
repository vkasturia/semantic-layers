/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nyt_and_implement;

import java.util.LinkedHashMap;
import java.util.Map;

public class ImpScoreTimePeriod {
    public Map<String, String> returnTimePeriodScore (Map<String, String> docsByYearMap, int totaldocs){
        Map<String, String> TimePeriodScoreMap = new LinkedHashMap<String, String>();
        for(Map.Entry<String, String> entry: docsByYearMap.entrySet()){
            String year = entry.getKey();
            String numDocs = entry.getValue();
            numDocs = numDocs.replace("\n", "").replaceAll(" ", "");
            double number_of_dox = Double.parseDouble(numDocs);
            int number_of_docs = Integer.parseInt(numDocs);
            double timePeriodScore = number_of_dox/totaldocs;
            String periodScore = Double.toString(timePeriodScore);
            TimePeriodScoreMap.put(year, periodScore);
        }
        return TimePeriodScoreMap;
    }    
}
