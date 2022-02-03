/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package l3s.de.ranking_algo_or.timeliness;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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

import l3s.de.ranking_algo_or.VirtuosoConnector;
import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

/**
 * @author Vaibhav Kasturia <kasturia at l3s.de>
 *         <p>
 *         Class to calculate Importance Scores for each Time Period p In
 *         addition, class also calculates the average percentage of EoI
 *         discussed in articles of p
 */

public class ImportanceScoreP {

	public Map<String, Double> imp_score_return(String[] entityArray, String fromDate, String toDate,
			String timePeriod) {
		ImportanceScoreP impscore = new ImportanceScoreP();

		// Setting granularity as specified in timePeriod to take as granularity
		ParameterizedSparqlString totalDocsByP = new ParameterizedSparqlString(
				"PREFIX dc: <http://purl.org/dc/terms/>\n" + "PREFIX oae: <http://www.ics.forth.gr/isl/oae/core#>\n"
						+ "PREFIX dbc: <http://dbpedia.org/resource/Category:>\n"
						+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
						+ "PREFIX dbo: <http://dbpedia.org/ontology/>\n"
						+ "PREFIX dbr: <http://dbpedia.org/resource/>\n" + "PREFIX schema:  <http://schema.org/>\n"
						+ "SELECT ?" + timePeriod + " (count(distinct ?article) as ?numOfArticles) WHERE {\n" + "{\n"
						+ "?article schema:mentions ?entity .\n" + "?entity oae:hasMatchedURI  <" + entityArray[0]
						+ "> .\n" + "?article dc:date ?date FILTER(?date >= xsd:date('" + fromDate + "') &&\n"
						+ "?date <= xsd:date('" + toDate + "')).\n"
						+ "BIND (CONCAT(month(?date), \"-\", year(?date)) AS ?month). \n"
						+ "BIND (CONCAT(day(?date), \"-\",month(?date), \"-\", year(?date)) AS ?day). \n"
						+ "BIND (CONCAT(year(?date)) AS ?year). \n}");

		for (int i = 1; i < entityArray.length; i++) {
			ParameterizedSparqlString totalDocsByP_toAppend = new ParameterizedSparqlString("UNION{\n"
					+ "?article dc:date ?date FILTER(?date >= xsd:date('" + fromDate + "') &&\n" + "?date <= xsd:date('"
					+ toDate + "')).\n" + "BIND (CONCAT(month(?date), \"-\", year(?date)) AS ?month). \n"
					+ "BIND (CONCAT(day(?date), \"-\",month(?date), \"-\", year(?date)) AS ?day). \n"
					+ "BIND (CONCAT(year(?date)) AS ?year). \n" + "?article schema:mentions ?entity" + i + " .\n"
					+ "?entity" + i + " oae:hasMatchedURI  <" + entityArray[i] + "> .}\n");
			totalDocsByP.append(totalDocsByP_toAppend);
		}

		ParameterizedSparqlString totalDocsByP_endAppend = new ParameterizedSparqlString(
				"} GROUP BY (?" + timePeriod + ") order by ?" + timePeriod + "");

		totalDocsByP.append(totalDocsByP_endAppend);

		VirtuosoConnector connec = new VirtuosoConnector();
		VirtGraph graph = new VirtGraph(connec.getGraph(), connec.getHost(), connec.getUsername(), connec.getPwd());
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(totalDocsByP.toString(), graph);
		ResultSet results = vqe.execSelect();
		String totaldocsxml = ResultSetFormatter.asXMLString(results);
		Document doc = null;
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			doc = loadXMLFromString(totaldocsxml);
		} catch (Exception e) {
			System.out.println("Error in parsing DocsByPeriod XML");
		}
		Map<String, String> Period_Docs_Map = new LinkedHashMap<>();
		NodeList nList = doc.getElementsByTagName("result");
		for (int i = 0; i < nList.getLength(); i++) {
			Node nNode = nList.item(i);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				String month = eElement.getElementsByTagName("binding").item(0).getTextContent();
				String numDocs = eElement.getElementsByTagName("binding").item(1).getTextContent();
				Period_Docs_Map.put(month, numDocs);
			}
		}

		int totaldocs = 0;

		// Printing each time period p and the number of documents in each time period
		for (Map.Entry<String, String> entry : Period_Docs_Map.entrySet()) {
			String granularity = entry.getKey();
			// System.out.println(timePeriod + " : " + granularity);
			String numDocs = entry.getValue();
			// System.out.println("Number of Documents"+ numDocs);
			numDocs = numDocs.replaceAll("\n", "").replaceAll(" ", "");
			totaldocs += Integer.parseInt(numDocs);
		}

		// System.out.println("Total Docs "+ totaldocs);

		// Calculate the timeliness score
		// First, we get the eoi percentage score for each timeperiod p
		Map<String, Double> timePeriod_eoiPercentage_Map = impscore.eoi_percentage_return(entityArray, fromDate, toDate,
				timePeriod);
		Map<String, Double> TimePeriodP_Score_Map = new LinkedHashMap<>();

		for (Map.Entry<String, String> entry : Period_Docs_Map.entrySet()) {
			String timegranularity = entry.getKey();
			String numDocs = entry.getValue();
			timegranularity = timegranularity.replaceAll("\n", "").replaceAll(" ", "");
			numDocs = numDocs.replaceAll("\n", "").replaceAll(" ", "");
			double number_of_docs = Double.parseDouble(numDocs);
			double timePeriodScore = (number_of_docs / totaldocs) * timePeriod_eoiPercentage_Map.get(timegranularity);
			TimePeriodP_Score_Map.put(timegranularity, timePeriodScore);
		}

		// Printing each time period p and the importance score
		TimePeriodP_Score_Map.entrySet().stream().map((entry) -> {
			String granularity = entry.getKey();
			// System.out.println(timePeriod + " : " + granularity);
			Double numDocs = entry.getValue();
			return numDocs;
		}).forEach((numDocs) -> {
			// System.out.println("Score " + numDocs);
		});

		return TimePeriodP_Score_Map;
	}

	public Map<String, Double> eoi_percentage_return(String[] entityArray, String fromDate, String toDate,
			String timePeriod) {

		// Declare map to store timePeriod p and the corresponding eoi percentage
		Map<String, Double> timePeriod_eoiPercentage_Map = new LinkedHashMap<>();

		// Declare data structures to store the query outputs
		// We then create a Map<timePeriod, Map<articleurl, List<entity>>> Structure
		List<String> entitiesList = new ArrayList<>();
		Map<String, List<String>> articleurl_entitiesList_Map = new LinkedHashMap<>();
		Map<String, Map<String, List<String>>> timePeriod_articleurl_Map = new LinkedHashMap<>();

		// SPARQL Query to get the Entities of Interest and Time Period p of all
		// Articles of Interest
		ParameterizedSparqlString EOI_TimePeriod_Query = new ParameterizedSparqlString(
				"PREFIX dc: <http://purl.org/dc/terms/>\n" + "PREFIX oae: <http://www.ics.forth.gr/isl/oae/core#>\n"
						+ "PREFIX dbc: <http://dbpedia.org/resource/Category:>\n"
						+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
						+ "PREFIX dbo: <http://dbpedia.org/ontology/>\n"
						+ "PREFIX dbr: <http://dbpedia.org/resource/>\n" + "PREFIX schema:  <http://schema.org/>\n"
						+ "SELECT ?article ?entityURI ?" + timePeriod + "\n" + "WHERE{ \n"
						+ "?article dc:date ?date FILTER(?date >= xsd:date('" + fromDate + "') && \n"
						+ "?date <= xsd:date('" + toDate + "')). \n"
						+ "BIND (CONCAT(month(?date), \"-\", year(?date)) AS ?month). \n"
						+ "BIND (CONCAT(day(?date), \"-\",month(?date), \"-\", year(?date)) AS ?day). \n"
						+ "BIND (CONCAT(year(?date)) AS ?year). \n" + "?article schema:mentions ?entity . \n"
						+ "?entity oae:hasMatchedURI ?entityURI \n" + "FILTER(?entityURI = <" + entityArray[0] + ">");

		for (int i = 1; i < entityArray.length; i++) {
			ParameterizedSparqlString EOI_TimePeriod_Query_toAppend = new ParameterizedSparqlString(
					"|| ?entityURI = <" + entityArray[i] + "> \n");
			EOI_TimePeriod_Query.append(EOI_TimePeriod_Query_toAppend);
		}

		ParameterizedSparqlString EOI_TimePeriod_Query_endAppend = new ParameterizedSparqlString(").\n}");

		EOI_TimePeriod_Query.append(EOI_TimePeriod_Query_endAppend);

		VirtuosoConnector connec = new VirtuosoConnector();
		VirtGraph graph = new VirtGraph(connec.getGraph(), connec.getHost(), connec.getUsername(), connec.getPwd());
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(EOI_TimePeriod_Query.toString(), graph);
		ResultSet results = vqe.execSelect();
		String totaldocsxml = ResultSetFormatter.asXMLString(results);
		Document doc = null;
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			doc = loadXMLFromString(totaldocsxml);
		} catch (Exception e) {
			System.out.println("Error in parsing DocsByPeriod XML");
		}

		NodeList nList = doc.getElementsByTagName("result");
		for (int i = 0; i < nList.getLength(); i++) {
			Node nNode = nList.item(i);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;

				String Articleurl = eElement.getElementsByTagName("binding").item(0).getTextContent();
				String Entity = eElement.getElementsByTagName("binding").item(1).getTextContent();
				String TimePeriod = eElement.getElementsByTagName("binding").item(2).getTextContent();

				Articleurl = Articleurl.replaceAll("\n", "").replaceAll(" ", "");
				Entity = Entity.replaceAll("\n", "").replaceAll(" ", "");
				TimePeriod = TimePeriod.replaceAll("\n", "").replaceAll(" ", "");

				// Store the results in the Map<timePeriod, Map<articleurl, List<entity>>>
				// Structure
				if (timePeriod_articleurl_Map.containsKey(TimePeriod)) {
					// If the key (timePeriod) is already present in the timePeriod_articleurl_Map,
					// we proceed to check the articleurl_entitiesList_Map inside the
					// timePeriod_articleurl_Map
					articleurl_entitiesList_Map = timePeriod_articleurl_Map.get(TimePeriod);
					if (articleurl_entitiesList_Map.containsKey(Articleurl)) {
						// If the key (Articleurl) is already present in the
						// articleurl_entitiesList_Map,
						// we get the entitiesList and add the entity to it and
						// then store the new entitiesList for the article inside
						// relEntities_articleUrlList_Map
						entitiesList = articleurl_entitiesList_Map.get(Articleurl);
						if (!entitiesList.contains(Entity))
							entitiesList.add(Entity);
						articleurl_entitiesList_Map.put(Articleurl, entitiesList);
					} else {
						// If the key (Articleurl) hasn't been used yet,
						// then we add ArticleUrl and Entity to articleurl_entitiesList_Map
						entitiesList = new ArrayList<>();
						entitiesList.add(Entity);
						articleurl_entitiesList_Map.put(Articleurl, entitiesList);
					}
				} else {
					// If the key (timePeriod) hasn't been used yet,
					// then we add all three: timePeriod, articleurl, entity
					// first we add articleurl and entity to articleurl_entitiesList_Map
					// then we add timePeriod and articleurl_entitiesList_Map to
					// timePeriod_articleurl_Map
					entitiesList = new ArrayList<>();
					entitiesList.add(Entity);
					articleurl_entitiesList_Map = new LinkedHashMap<>();
					articleurl_entitiesList_Map.put(Articleurl, entitiesList);
					timePeriod_articleurl_Map.put(TimePeriod, articleurl_entitiesList_Map);
				}
			}
		}
		for (Map.Entry<String, Map<String, List<String>>> timeperiod : timePeriod_articleurl_Map.entrySet()) {
			double eoi_percentage_score = 0;
			for (Map.Entry<String, List<String>> article : timeperiod.getValue().entrySet()) {
				eoi_percentage_score += ((double) article.getValue().size() / (double) entityArray.length);
			}
			eoi_percentage_score /= timeperiod.getValue().size();
			timePeriod_eoiPercentage_Map.put(timeperiod.getKey(), eoi_percentage_score);
		}
		return timePeriod_eoiPercentage_Map;
	}

	protected static Document loadXMLFromString(String xml) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource is = new InputSource(new StringReader(xml));
		return builder.parse(is);
	}

	protected String getString(String tagName, Element element) {
		NodeList list = element.getElementsByTagName(tagName);
		if (list != null && list.getLength() > 0) {
			NodeList subList = list.item(0).getChildNodes();

			if (subList != null && subList.getLength() > 0) {
				return subList.item(0).getNodeValue();
			}
		}
		return null;
	}
}
