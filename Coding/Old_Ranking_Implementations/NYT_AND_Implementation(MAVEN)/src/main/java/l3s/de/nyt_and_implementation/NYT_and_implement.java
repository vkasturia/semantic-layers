package l3s.de.nyt_and_implementation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class NYT_and_implement {

    public static void main(String[] args) {
        
        //Entity of Interest E(subscript(Q))
        String entity = "http://dbpedia.org/resource/Nelson_Mandela";
        
        //Period of Interest P(subscript(Q))
        String fromDate = "1989-01-01";
        String toDate = "1990-01-01";
        
        //Decay Factor Beta
        double beta = 0.5;
        
        //Num of Documents mentioning Entity of Interest D(Q)
        TotalDocumentsFinder docFinder = new TotalDocumentsFinder();
        int totaldocs = docFinder.totaldocsreturn(entity, fromDate, toDate);
        System.out.println(totaldocs);   
        
        //Documents in each Time Period p 
        DocumentsFinderByYear yeardocFinder = new DocumentsFinderByYear();
        Map<String, String> docsByYearMap = yeardocFinder.totaldocsreturn(entity, fromDate, toDate);
        
        //Calculating importance score of each Time Period p
        ImpScoreTimePeriod istperiod = new ImpScoreTimePeriod();
        Map<String, String> TimePeriodScoreMap = istperiod.returnTimePeriodScore(docsByYearMap, totaldocs);
        
        //Related Entities and their Occurrences
        RelEntities_and_OccurenceFinder relEntitiesFinder = new RelEntities_and_OccurenceFinder();
        Map<String, String> relEntitiesMap = relEntitiesFinder.relentitiesreturn(entity, fromDate, toDate);
        
        //Documents mentioning Entity of Interest in Period of Interest P(Q)
        EntityDocuments relevantDocuments = new EntityDocuments();
        List<String> docsList = relevantDocuments.returndocuments(entity, fromDate, toDate);
        
        //Finding the maximum entity frequency out of all documents in D(Q)
        MaxEntityFreq max_freq = new MaxEntityFreq();
        double highest_freq = max_freq.return_maxfreq(entity, docsList);
        System.out.println("higher:"+highest_freq);
        
        //Documents score based on Entity Frequency and Time Period Importance ScoreDefP
        DocEFPScore defScore = new DocEFPScore();
        Map<String, String> DocEFPScoreMap = defScore.return_docefpscore(entity, docsList, TimePeriodScoreMap, highest_freq);
        
        //Finding Sum of Score of Related Entities : Summation(ScoreE(e'))
        SumScoreRelEntities ScoreRel_Entities = new SumScoreRelEntities();
        double rel_entity_score_sum = ScoreRel_Entities.return_rel_entity_score_sum(totaldocs, entity, fromDate, toDate, relEntitiesMap, docsByYearMap, TimePeriodScoreMap);
        
        //Calculating Score DE(d)
        DEdocScore DE_DocScore = new DEdocScore();
        Map<String, String> DEDocScoreMap = DE_DocScore.return_dedoc_score(totaldocs, entity, fromDate, toDate, docsList, docsByYearMap, TimePeriodScoreMap, rel_entity_score_sum);
        
        //Calculating Final Score and returning the ranked list of documents
        FinalScore final_score = new FinalScore();
        LinkedHashMap<String, Double> rankSortedArticlesMap = final_score.finalscore(beta, DocEFPScoreMap, DEDocScoreMap);
        
        //Printing the Final List of Rank Sorted Articles
        for(Map.Entry<String, Double> entry: rankSortedArticlesMap.entrySet()){
            String year = entry.getKey().replaceAll("\n", "").replaceAll(" ", "");
            System.out.println("DocURI: "+ year);
            double numDocs = entry.getValue();
            System.out.println("Score: "+ numDocs);
        } 
    }
    
}
