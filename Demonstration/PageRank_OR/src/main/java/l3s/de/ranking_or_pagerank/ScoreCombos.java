/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package l3s.de.ranking_or_pagerank;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Vaibhav Kasturia <kasturia at l3s.de>
 *         <p>
 *         Class to get combination of Timeliness and Relativeness Score
 */

public class ScoreCombos {
	public Map<String, Double> scoreCombination(Map<String, Double> article_timelinessScore_map,
			Map<String, Double> article_relativenessScore_map, int counter) throws FileNotFoundException {
		ScoreCombos score_combos = new ScoreCombos();

		// Data Structures to store combination of scores
		Map<String, Double> article_time_relativeness_map = new LinkedHashMap<>();

		article_relativenessScore_map.entrySet().stream().forEach((article) -> {
			double time_relativenessScore = 0;

			try {

				time_relativenessScore = article.getValue() * article_timelinessScore_map.get(article.getKey());

				article_time_relativeness_map.put(article.getKey(), time_relativenessScore);

			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		});

		Map<String, Double> article_time_relativeness_map_sorted = new LinkedHashMap<>();
		article_time_relativeness_map_sorted = score_combos.sortHashMapByValues(article_time_relativeness_map);

		// Printing each article and the different score combinations in a csv file
//        PrintWriter writer = new PrintWriter("./results2/" + counter + "/ranking_time_relativeness" + counter + ".csv");
//        writer.println("Article; Time_Relativeness_Score");
//        article_time_relativeness_map_sorted.entrySet().stream().forEach((entry) -> {
//            writer.println(entry.getKey() + "; " + entry.getValue());
//        });
//        writer.close();

		return article_time_relativeness_map;
	}

	public static LinkedHashMap<String, Double> sortHashMapByValues(Map<String, Double> passedMap) {
		List<String> mapKeys = new ArrayList<>(passedMap.keySet());
		List<Double> mapValues = new ArrayList<>(passedMap.values());
		Collections.sort(mapValues, Collections.reverseOrder());
		Collections.sort(mapKeys, Collections.reverseOrder());

		LinkedHashMap<String, Double> sortedMap = new LinkedHashMap<>();

		Iterator<Double> valueIt = mapValues.iterator();
		while (valueIt.hasNext()) {
			Double val = valueIt.next();
			Iterator<String> keyIt = mapKeys.iterator();

			while (keyIt.hasNext()) {
				String key = keyIt.next();
				Double comp1 = passedMap.get(key);
				Double comp2 = val;

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
