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

package l3s.de.pgvariants;

import java.io.FileNotFoundException;
import java.util.Map;

import l3s.de.SemLayerFunctions.ArticleDetails;
import l3s.de.SemLayerFunctions.ArticleTitleDateFetcher;
import l3s.de.converter.JSONConverter;
import l3s.de.ranking_and_pagerank.Docs_and_Entities;
import l3s.de.ranking_and_pagerank.ScoreCombos;
import l3s.de.ranking_and_pagerank.pagerank.GraphCreator;
import l3s.de.ranking_and_pagerank.relatedness.RelatednessScore;
import l3s.de.ranking_and_pagerank.relativeness.RelativenessScore;
import l3s.de.ranking_and_pagerank.timeliness.ImportanceScoreP;
import l3s.de.ranking_and_pagerank.timeliness.TimelinessScore;


public class PageRankAND {
	@SuppressWarnings("unchecked")
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
        Docs_and_Entities docs_entities = new Docs_and_Entities();
        Object[] returned_values = docs_entities.docs_entities_return(entityArray, fromDate, toDate);
        Map<String, Map<String, Double>> docs_entities_map = (Map<String, Map<String, Double>>) returned_values[0];
        Map<String, Map<String, Double>> entity_docs_map = (Map<String, Map<String, Double>>) returned_values[1];

        //Map of all entities and their relatedness scores 
        RelatednessScore relatedScore = new RelatednessScore();
        Object[] values = relatedScore.relatedness_score(entityArray, fromDate, toDate, timePeriod, counter);
        Map<String, Double> relEntities_relatednessScore_Map = (Map<String, Double>) values[1];

        //Create the transition Graph to run the algorithm
        GraphCreator graphCreator = new GraphCreator();
        Map<String, Double> articleUrl_score_map = graphCreator.createGraph(docs_entities_map, entity_docs_map, article_time_relativeness_map, relEntities_relatednessScore_Map, num_of_entities, entityArray, counter, decayFactor);
        return articleUrl_score_map;
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		String[] entityArray = new String[1];
		entityArray[0] = "http://dbpedia.org/resource/Saint_Petersburg";
		
		String fromDate = "1989-02-06";
		String toDate = "1989-02-15";
		String timePeriod = "day";
		
		PageRankAND tester = new PageRankAND();
		double decayFactor = 0.8;
		Map<String, Double> articleURL_score_map = tester.getArticleURLWithScore(entityArray, fromDate, toDate, timePeriod, decayFactor);
		//String jsonResponse = JSONConverter.convertArticleUrlAndScoreToJSON(articleURL_score_map);
		
		ArticleTitleDateFetcher fetcher = new ArticleTitleDateFetcher();
		Map<String, ArticleDetails> articleUrl_articleDetails_map = fetcher.getArticleTitleAndDate(articleURL_score_map);
		String jsonResponse = JSONConverter.convertArticleUrlAndDetailsToJSON(articleUrl_articleDetails_map);
		System.out.println(jsonResponse);
	}
}
