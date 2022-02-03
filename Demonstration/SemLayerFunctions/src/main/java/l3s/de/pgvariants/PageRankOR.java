package l3s.de.pgvariants;

import java.io.FileNotFoundException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import l3s.de.converter.JSONConverter;
import l3s.de.ranking_or_pagerank.ScoreCombos;
import l3s.de.ranking_or_pagerank.Test_Docs_and_Entities;
import l3s.de.ranking_or_pagerank.pagerank.GraphCreator;
import l3s.de.ranking_or_pagerank.relatedness.RelatednessScore;
import l3s.de.ranking_or_pagerank.relativeness.RelativenessScore;
import l3s.de.ranking_or_pagerank.timeliness.ImportanceScoreP;
import l3s.de.ranking_or_pagerank.timeliness.TimelinessScore;


public class PageRankOR {
	public Map<String, Double> getArticleURLWithScore(String[] entityArray, String fromDate, String toDate, String timePeriod, double decayFactor) throws FileNotFoundException {
		int counter = 1;
		int num_of_entities = entityArray.length;
		
        //Get Importance Score for each Time Period P
        ImportanceScoreP impscore = new ImportanceScoreP();
        Map<String, Double> impscore_map = impscore.imp_score_return(entityArray, fromDate, toDate, timePeriod);

        //Timeliness Score for each document d
        TimelinessScore timescore = new TimelinessScore();
        Map<String, Double> article_timelinessScore_map = timescore.timeliness_score(entityArray, fromDate, toDate, impscore_map, timePeriod, counter);

        //Relativeness Score for each document d
        RelativenessScore relscore = new RelativenessScore();
        Map<String, Double> article_relativenessScore_map = relscore.relativeness_score(entityArray, fromDate, toDate, counter);

        //Combination of timeliness and relativeness score for each document d
        ScoreCombos combined = new ScoreCombos();
        Map<String, Double> article_time_relativeness_map = combined.scoreCombination(article_timelinessScore_map, article_relativenessScore_map, counter);

        //Map of all query documents and their extracted entities 
        Test_Docs_and_Entities docs_entities = new Test_Docs_and_Entities();
        Object[] returned_values = docs_entities.docs_entities_return(entityArray, fromDate, toDate);
        Map<String, Map<String, Double>> docs_entities_entitycount_map = (Map<String, Map<String, Double>>) returned_values[0];
        Map<String, Map<String, Double>> entity_docs_entitycount_map = (Map<String, Map<String, Double>>) returned_values[1];

        //Extracting the docs and entities out of the docs_entities_entitycount_map
        List<String> entityList;
        Map<String, List<String>> docs_entities_map = new LinkedHashMap();

        for (Map.Entry<String, Map<String, Double>> entry : docs_entities_entitycount_map.entrySet()) {
            entityList = new LinkedList();
            for (Map.Entry<String, Double> entry2 : entry.getValue().entrySet()) {
                entityList.add(entry2.getKey());
                docs_entities_map.put(entry.getKey(), entityList);
            }
        }

        //Map of all entities and their relatedness scores 
        RelatednessScore relatedScore = new RelatednessScore();
        Object[] values = relatedScore.relatedness_score(entityArray, fromDate, toDate, timePeriod, counter);
        Map<String, Double> relEntities_relatednessScore_Map = (Map<String, Double>) values[1];
        Map<String, List<String>> articleUrl_relEntities_Map = (Map<String, List<String>>) values[2];

        //Create the transition Graph to run the algorithm
        GraphCreator graphCreator = new GraphCreator();
        Map<String, Double> articleUrl_score_map = graphCreator.createGraph(docs_entities_entitycount_map, entity_docs_entitycount_map, article_time_relativeness_map, relEntities_relatednessScore_Map, docs_entities_map, num_of_entities, entityArray, counter, decayFactor);
        return articleUrl_score_map;
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		String[] entityArray = new String[2];
		entityArray[0] = "http://dbpedia.org/resource/Fahd_of_Saudi_Arabia";		
		entityArray[1] = "http://dbpedia.org/resource/Zayed_bin_Sultan_Al_Nahyan";	
		
		String fromDate = "1989-04-01";
		String toDate = "1989-12-31";
		String timePeriod = "day";
		double decayFactor = 0.8;
		
		PageRankOR tester = new PageRankOR();
		Map<String, Double> articleURL_score_map = tester.getArticleURLWithScore(entityArray, fromDate, toDate, timePeriod, decayFactor);
		String jsonResponse = JSONConverter.convertArticleUrlAndScoreToJSON(articleURL_score_map);
		System.out.println(jsonResponse);
		
	}

}
