package l3s.de.statsvariants;

import java.io.FileNotFoundException;
import java.util.Locale;
import java.util.Map;

import l3s.de.converter.JSONConverter;
import l3s.de.ranking_algo_and.ScoreCombos;
import l3s.de.ranking_algo_and.relatedness.RelatednessScore;
import l3s.de.ranking_algo_and.relativeness.RelativenessScore;
import l3s.de.ranking_algo_and.timeliness.ImportanceScoreP;
import l3s.de.ranking_algo_and.timeliness.TimelinessScore;

public class StatisticalAND {

	public Map<String, Double> getArticleURLWithScore(String[] entityArray, String fromDate, String toDate, String timePeriod) throws FileNotFoundException {
		int counter = 1;
		// Get Importance Score for each Time Period P
		// For test case granularity set to Month
		ImportanceScoreP impscore = new ImportanceScoreP();
		Map<String, Double> impscore_map = impscore.imp_score_return(entityArray, fromDate, toDate, timePeriod);

		// Timeliness Score for each document d
		TimelinessScore timescore = new TimelinessScore();
		Map<String, Double> article_timelinessScore_map = timescore.timeliness_score(entityArray, fromDate, toDate, impscore_map, timePeriod, counter);

		// Relativeness Score for each document d
		RelativenessScore relscore = new RelativenessScore();
		Map<String, Double> article_relativenessScore_map = relscore.relativeness_score(entityArray, fromDate, toDate, counter);

		// Relatedness Score for each document d
		RelatednessScore relatedscore = new RelatednessScore();
		Map<String, Double> article_relatednessScore_map = relatedscore.relatedness_score(entityArray, fromDate, toDate, timePeriod, counter);

		// Combination of scores for each document d
		ScoreCombos combined = new ScoreCombos();
		Map<String, Double> articleURL_score_map = combined.getTimeRelativeRelatedMap(article_timelinessScore_map, article_relativenessScore_map, article_relatednessScore_map);
		return articleURL_score_map;
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		String[] entityArray = new String[2];
		entityArray[0] = "http://dbpedia.org/resource/Nelson_Mandela";
		entityArray[1] = "http://dbpedia.org/resource/South_Africa";
		String fromDate = "1990-01-01";
		String toDate = "1990-01-30";
		String timePeriod = "day";
		
		StatisticalAND tester = new StatisticalAND();
		Map<String, Double> articleURL_score_map = tester.getArticleURLWithScore(entityArray, fromDate, toDate, timePeriod);
		String jsonResponse = JSONConverter.convertArticleUrlAndScoreToJSON(articleURL_score_map);
		System.out.println(jsonResponse);
		
	}
}