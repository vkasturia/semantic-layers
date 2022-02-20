/*
Copyright 2017-2022 Vaibhav Kasturia <vbh18kas@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package l3s.de.spark;

import static spark.Spark.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

import l3s.de.SemLayerFunctions.ArticleDetails;
import l3s.de.SemLayerFunctions.ArticleTitleDateFetcher;
import l3s.de.converter.JSONConverter;
import l3s.de.pgvariants.PageRankAND;
import l3s.de.pgvariants.PageRankCAT;
import l3s.de.pgvariants.PageRankOR;
import l3s.de.statsvariants.StatisticalAND;
import l3s.de.statsvariants.StatisticalCAT;
import l3s.de.statsvariants.StatisticalOR;

public class SemanticLayerSparkApplication {
    public static void main(String[] args) {
        get("/query", (request, response) ->{
            String jsonResponse = "";
            try {
                String queryType = request.queryParams("queryType");
                String fromDate = request.queryParams("fromDate");
                String toDate = request.queryParams("toDate");
                String timePeriod = request.queryParams("timePeriod");
                String algorithm = request.queryParams("algorithm");
                String decayFactorString = request.queryParams("decayFactor");
                Double decayFactor = Double.valueOf(decayFactorString);
            	
                String text = request.queryParams("text");
                String decodedText = null;
                try {
                    decodedText = URLDecoder.decode(text, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                String[] splitString = decodedText.split(",");
                List<String> entities = new ArrayList<>();
                
                for (String entity : splitString) {
                    System.out.println(entity);
                    entity = entity.trim().replaceAll(" ", "_");
                    if (!entity.isEmpty()) {
						entities.add(entity);
					}
                }
                                

            	if (Algorithm.valueOf(algorithm) == Algorithm.PAGERANK){
        			if (decayFactor > 1.0 || decayFactor < 0.0){
        				String errorMessage = "{\"error\": \"Decay Factor must lie between 0 and 1\"}";
        				return errorMessage;
        			}
        		}

        		try {
        			if (TimePeriod.valueOf(timePeriod) != TimePeriod.DAY && TimePeriod.valueOf(timePeriod) != TimePeriod.MONTH && TimePeriod.valueOf(timePeriod) != TimePeriod.YEAR){
        				String errorMessage = "{\"error\": \"Importance Time Period can be either Day, Month or Year\"}";
        				return errorMessage;
        			}
					timePeriod = timePeriod.toLowerCase(Locale.ROOT);
					Map<String, Double> articleURL_score_map = new LinkedHashMap<>();
        			if (QueryType.valueOf(queryType) == QueryType.SINGLE) {
        				if (entities.size() != 1) {
        					String errorMessage = "{\"error\": \"Only One Entity expected for Single Entity Query Type\"}";
        					return errorMessage;
        				}
        				entities.replaceAll(s -> "http://dbpedia.org/resource/" + s);
        				String[] entityArray = new String[1];
        				entities.toArray(entityArray);

        				if (Algorithm.valueOf(algorithm) == Algorithm.STATISTICAL) {
        					StatisticalAND tester = new StatisticalAND();
        					articleURL_score_map = tester.getArticleURLWithScore(entityArray, fromDate, toDate, timePeriod);
        				} else if (Algorithm.valueOf(algorithm) == Algorithm.PAGERANK) {
        					PageRankAND tester = new PageRankAND();
        					articleURL_score_map = tester.getArticleURLWithScore(entityArray, fromDate, toDate, timePeriod, decayFactor);
        				} else {
        					String errorMessage = "{\"error\": \"Algorithm can be either Statistical or PageRank\"}";
        					return errorMessage;
        				}
        			} else if (QueryType.valueOf(queryType) == QueryType.AND) {
        				entities.replaceAll(s -> "http://dbpedia.org/resource/" + s);
        				String[] entityArray = new String[entities.size()];
        				entities.toArray(entityArray);

        				if (Algorithm.valueOf(algorithm) == Algorithm.STATISTICAL) {
        					StatisticalAND tester = new StatisticalAND();
        					articleURL_score_map = tester.getArticleURLWithScore(entityArray, fromDate, toDate, timePeriod);
        				} else if (Algorithm.valueOf(algorithm) == Algorithm.PAGERANK) {
        					PageRankAND tester = new PageRankAND();
        					articleURL_score_map = tester.getArticleURLWithScore(entityArray, fromDate, toDate, timePeriod, decayFactor);
        				} else {
        					String errorMessage = "{\"error\": \"Algorithm can be either Statistical or PageRank\"}";
        					return errorMessage;
        				}
        			} else if (QueryType.valueOf(queryType) == QueryType.OR) {
        				entities.replaceAll(s -> "http://dbpedia.org/resource/" + s);
        				String[] entityArray = new String[entities.size()];
        				entities.toArray(entityArray);

        				if (Algorithm.valueOf(algorithm) == Algorithm.STATISTICAL) {
        					StatisticalOR tester = new StatisticalOR();
        					articleURL_score_map = tester.getArticleURLWithScore(entityArray, fromDate, toDate, timePeriod);
        				} else if (Algorithm.valueOf(algorithm) == Algorithm.PAGERANK) {
        					PageRankOR tester = new PageRankOR();
        					articleURL_score_map = tester.getArticleURLWithScore(entityArray, fromDate, toDate, timePeriod, decayFactor);
        				} else {
        					String errorMessage = "{\"error\": \"Algorithm can be either Statistical or PageRank\"}";
        					return errorMessage;
        				}
        			} else if (QueryType.valueOf(queryType) == QueryType.CATEGORY) {
        				if (entities.size() != 1) {
        					String errorMessage = "{\"error\": \"Single Category expected for Category Query Type\"}";
        					return errorMessage;
        				}
        				String category = entities.get(0);

        				if (Algorithm.valueOf(algorithm) == Algorithm.STATISTICAL) {
        					StatisticalCAT tester = new StatisticalCAT();
        					articleURL_score_map = tester.getArticleURLWithScore(category, fromDate, toDate, timePeriod);
        				} else if (Algorithm.valueOf(algorithm) == Algorithm.PAGERANK) {
        					PageRankCAT tester = new PageRankCAT();
        					articleURL_score_map = tester.getArticleURLWithScore(category, fromDate, toDate, timePeriod, decayFactor);
        				} else {
        					String errorMessage = "{\"error\": \"Algorithm can be either Statistical or PageRank\"}";
        					return errorMessage;
        				}
        			} else {
        				String errorMessage = "{\"error\": \"Invalid Query Type\"}";
        				return errorMessage;
        			}
        			ArticleTitleDateFetcher fetcher = new ArticleTitleDateFetcher();
        			Map<String, ArticleDetails> articleUrl_articleDetails_map = fetcher.getArticleTitleAndDate(articleURL_score_map);
        			jsonResponse = JSONConverter.convertArticleUrlAndDetailsToJSON(articleUrl_articleDetails_map);
        		} catch (Exception e) {
        			String errorMessage = "{\"error\": \"No Results found\"}";
        			return errorMessage;
        		}
            } catch(Exception e){
                return "{\"error\":\"Malformed Query or Database not working\"}";
            }
            return jsonResponse;
        });
    }
}

enum Algorithm {
	STATISTICAL, 
	PAGERANK
}

enum QueryType {
	SINGLE,
	AND,
	OR,
	CATEGORY
}

enum TimePeriod {
	DAY,
	MONTH,
	YEAR
}
