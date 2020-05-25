

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