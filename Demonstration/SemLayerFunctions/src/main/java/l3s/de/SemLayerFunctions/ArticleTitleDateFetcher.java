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

package l3s.de.SemLayerFunctions;

import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import l3s.de.ranking_algo_and.VirtuosoConnector;
import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

public class ArticleTitleDateFetcher {
	public Map<String, ArticleDetails> getArticleTitleAndDate (Map<String, Double> articleUrl_score_map){
		Map<String, ArticleDetails> articleUrl_articleDetails_map = new LinkedHashMap<>();
		for (Map.Entry<String, Double> entry : articleUrl_score_map.entrySet()) {
			ArticleDetails articleDetails = new ArticleDetails();
			articleDetails.setScore(entry.getValue());
		
			String url = entry.getKey();
			ParameterizedSparqlString articleTitleDateQuery = new ParameterizedSparqlString(
					"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" + 
			        "PREFIX dc: <http://purl.org/dc/terms/> \n" + 
					"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n" + 
			        "PREFIX nyt: <http://query.nytimes.com/gst/fullpage.html?res=> \n" + 
					"SELECT ?title ?date \n" + 
			        "WHERE { \n" + 
					"<" + url + "> dc:date ?date;\n" + 
			        "dc:title  ?title" + "} \n");
			
			VirtuosoConnector connec = new VirtuosoConnector();
			VirtGraph graph = new VirtGraph(connec.getGraph(), connec.getHost(), connec.getUsername(), connec.getPwd());

			VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(articleTitleDateQuery.toString(), graph);
			
			ResultSet results = vqe.execSelect();
			String articleTitleDateQueryXml = ResultSetFormatter.asXMLString(results);
			System.out.println(articleTitleDateQueryXml);
			Document doc = null;
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			try {
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				doc = loadXMLFromString(articleTitleDateQueryXml);
			} catch (Exception e) {
				System.out.println("Error in parsing article Title Date Query XML");
			}

			NodeList nList = doc.getElementsByTagName("result");
			for (int i = 0; i < nList.getLength(); i++) {
				Node nNode = nList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					String articleTitle = eElement.getElementsByTagName("binding").item(0).getTextContent();
					articleTitle = articleTitle.replaceAll("\n", "").trim();
					String articleDate = eElement.getElementsByTagName("binding").item(1).getTextContent();
					articleDate = articleDate.replaceAll("\n", "").trim();
					
					//System.out.println("Title: " + articleTitle);
					//System.out.println("Date: " + articleDate);
					articleDetails.setTitle(articleTitle);
					articleDetails.setDate(articleDate);
				}
			}
			articleUrl_articleDetails_map.put(url, articleDetails); 
		}
		return articleUrl_articleDetails_map;
	}
	
	protected static Document loadXMLFromString(String xml) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource is = new InputSource(new StringReader(xml));
		return builder.parse(is);
	}
	
	public static void main(String[] args) {
		ArticleTitleDateFetcher tester = new ArticleTitleDateFetcher();
		Map<String, Double> articleUrl_score_map = new LinkedHashMap<>();
		String url = "http://query.nytimes.com/gst/fullpage.html?res=950DE5DC1331F936A3575AC0A96F948260";
		Double score = 1.0;
		articleUrl_score_map.put(url, score);
		tester.getArticleTitleAndDate(articleUrl_score_map);
	}

}
