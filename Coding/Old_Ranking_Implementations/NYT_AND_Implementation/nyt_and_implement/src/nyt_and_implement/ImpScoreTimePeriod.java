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
