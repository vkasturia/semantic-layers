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

package l3s.de.ranking_algo_category;

/**
 * @author Vaibhav Kasturia <kasturia at l3s.de>
 * 
 * The main class: Reads inputs from file and passes input to different files for ranking
 */

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import l3s.de.ranking_algo_category.ranking_ndcg.NDCG_Calc;
import l3s.de.ranking_algo_category.relatedness.RelatednessScore;
import l3s.de.ranking_algo_category.relativeness.RelativenessScore;
import l3s.de.ranking_algo_category.timeliness.ImportanceScoreP;
import l3s.de.ranking_algo_category.timeliness.TimelinessScore;

public class ranking_algo_main {

	public static void main(String[] args) throws FileNotFoundException, IOException {

		// File in which the query parameters are stored
		String file = "./query_param";

		int counter = 22;

		// Read file
		try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"))) {
			String str;
			while ((str = in.readLine()) != null) {
				// System.out.println(str);
				if (str.startsWith("#")) {
					continue;
				}

				String[] data = str.split("\t");

				String Category = data[0].trim();

				if (data.length != 4) {
					System.out.println("*** Malformed line! Continuing to next line...");
				}

				// Period of Interest P(Q)
				String fromDate = data[1].trim();
				String toDate = data[2].trim();
				String timePeriod = data[3].trim();

				// Getting the Entities of Interest from the Category
				Cat_to_entities categoryToEntities = new Cat_to_entities();
				String[] entityArray = categoryToEntities.entitiesreturn(Category, fromDate, toDate);

				// Get Importance Score for each Time Period P
				ImportanceScoreP impscore = new ImportanceScoreP();
				Map<String, Double> impscore_map = impscore.imp_score_return(entityArray, fromDate, toDate, timePeriod);

				// Timeliness Score for each document d
				TimelinessScore timescore = new TimelinessScore();
				Map<String, Double> article_timelinessScore_map = timescore.timeliness_score(entityArray, fromDate,
						toDate, impscore_map, timePeriod, counter);

				// Relativeness Score for each document d
				RelativenessScore relscore = new RelativenessScore();
				Map<String, Double> article_relativenessScore_map = relscore.relativeness_score(entityArray, fromDate,
						toDate, counter);

				// Relatedness Score for each document d
				RelatednessScore relatedscore = new RelatednessScore();
				Map<String, Double> article_relatednessScore_map = relatedscore.relatedness_score(entityArray, fromDate,
						toDate, timePeriod, counter);

				// Combination of scores for each document d
				ScoreCombos combined = new ScoreCombos();
				combined.scoreCombination(article_timelinessScore_map, article_relativenessScore_map,
						article_relatednessScore_map, counter);

				// Generate NDCG values for query results
				NDCG_Calc ndcg_calc = new NDCG_Calc();
				ndcg_calc.ndcg_table_creator(counter);

				counter += 1;
			}
		}
	}
}
