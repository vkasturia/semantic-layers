/*
Copyright 2017-2022 Vaibhav Kasturia <vbh18kas@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/



package l3s.de.timelinessscoreand;

/**
 *
 * @author vaibhav
 */

import java.io.FileNotFoundException;
import java.util.Map;

public class ranking_algo_main {   
    public static void main(String[] args) throws FileNotFoundException {
        
        //Entity of Interest E(Q)
        String entity = "http://dbpedia.org/resource/Nelson_Mandela";
        
        //Period of Interest P(Q)
        String fromDate = "1990-01-01";
        String toDate = "1990-03-31";
        
        //Get Importance Score for each Time Period P
        //For test case granularity set to Month
        ImportanceScoreP impscore = new ImportanceScoreP();
        Map<String, Double> impscore_map = impscore.imp_score_return(entity, fromDate, toDate);
        
        //Timeliness Score for each document d
        TimelinessScore timescore = new TimelinessScore();
        timescore.timeliness_score(entity, fromDate, toDate, impscore_map);
        
    }
}