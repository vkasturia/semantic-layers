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