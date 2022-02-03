package l3s.de.converter;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import l3s.de.SemLayerFunctions.ArticleDetails;



public class JSONConverter {
	
	public static String convertArticleUrlAndScoreToJSON (Map<String, Double> articleURL_score_map){
		JSONObject result = new JSONObject();
	    JSONArray articles = new JSONArray();
	    result.put("articles", articles);

	    articleURL_score_map.entrySet().stream()       //iterate the map
	        .map(e -> {                 //build an object
	            JSONObject article = new JSONObject();
	            article.put("articleUrl", e.getKey());
	            article.put("score", e.getValue());
	            return article;
	        })
	        .forEach(articles::put);   //insert into the array

	    return result.toString();
    }
	
	public static String convertArticleUrlAndDetailsToJSON (Map<String, ArticleDetails> articleURL_articleDetails_map){
		JSONObject result = new JSONObject();
	    JSONArray articles = new JSONArray();
	    result.put("articles", articles);

	    articleURL_articleDetails_map.entrySet().stream()       //iterate the map
	        .map(e -> {                 //build an object
	            JSONObject article = new JSONObject();
	            article.put("articleUrl", e.getKey());
	            ArticleDetails articleDetails = e.getValue();
	            article.put("score", articleDetails.getScore());
	            article.put("date", articleDetails.getDate());
	            article.put("title", articleDetails.getTitle());
	            return article;
	        })
	        .forEach(articles::put);   

	    return result.toString();
    }
}