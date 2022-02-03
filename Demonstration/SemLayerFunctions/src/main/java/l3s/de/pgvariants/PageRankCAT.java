package l3s.de.pgvariants;

import java.io.FileNotFoundException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import l3s.de.ranking_algo_category.Cat_to_entities;
import com.vaibhav.ranking_cat_pagerank.ScoreCombos;
import com.vaibhav.ranking_cat_pagerank.Test_Docs_and_Entities;
import com.vaibhav.ranking_cat_pagerank.pagerank.GraphCreator;
import com.vaibhav.ranking_cat_pagerank.relatedness.RelatednessScore;
import com.vaibhav.ranking_cat_pagerank.relativeness.RelativenessScore;
import com.vaibhav.ranking_cat_pagerank.timeliness.ImportanceScoreP;
import com.vaibhav.ranking_cat_pagerank.timeliness.TimelinessScore;

import l3s.de.converter.JSONConverter;

public class PageRankCAT {
	public Map<String, Double> getArticleURLWithScore(String category, String fromDate, String toDate, String timePeriod, double decayFactor) throws FileNotFoundException {
		int counter = 1;
		//Getting the Entities of Interest from the Category
        Cat_to_entities categoryToEntities = new Cat_to_entities();
        String[] entityArray = categoryToEntities.entitiesreturn(category, fromDate, toDate);

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

        for (Map.Entry<String, List<String>> entry : docs_entities_map.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }

        //Map of all entities and their relatedness scores 
        RelatednessScore relatedScore = new RelatednessScore();
        Object[] values = relatedScore.relatedness_score(entityArray, fromDate, toDate, timePeriod, counter);
        Map<String, Double> relEntities_relatednessScore_Map = (Map<String, Double>) values[1];
        Map<String, List<String>> articleUrl_relEntities_Map = (Map<String, List<String>>) values[2];

        for (Map.Entry<String, Double> entry : relEntities_relatednessScore_Map.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }

        //Create the transition Graph to run the algorithm
        GraphCreator graphCreator = new GraphCreator();

		Map<String, Double> articleURL_score_map =  graphCreator.createGraph(docs_entities_entitycount_map, entity_docs_entitycount_map, article_time_relativeness_map, relEntities_relatednessScore_Map, docs_entities_map, entityArray.length, entityArray, counter, decayFactor);

		return articleURL_score_map;
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		String category  = "NASA_civilian_astronauts";	
		String fromDate = "1990-01-01";
		String toDate = "1991-04-10";
		String timePeriod = "day";
		double decayFactor = 0.8;
		
		PageRankCAT tester = new PageRankCAT();
		Map<String, Double> articleURL_score_map = tester.getArticleURLWithScore(category, fromDate, toDate, timePeriod, decayFactor);
		String jsonResponse = JSONConverter.convertArticleUrlAndScoreToJSON(articleURL_score_map);
		System.out.println(jsonResponse);
	}
}
