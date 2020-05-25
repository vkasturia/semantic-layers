/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package l3s.de.timelinessscoreand;

/**
 *
 * @author vaibhav
 */

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
import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

/**
 *
 * @author vaibhav
 */
public class ImportanceScoreP {

    public Map<String, Double> imp_score_return(String entity, String fromDate, String toDate) {
        //Setting granularity for the test case to month
        ParameterizedSparqlString totalDocsByP = new ParameterizedSparqlString("PREFIX dc: <http://purl.org/dc/terms/>\n" +
                                                                               "PREFIX oae: <http://www.ics.forth.gr/isl/oae/core#>\n" +
                                                                               "PREFIX dbc: <http://dbpedia.org/resource/Category:>\n" +
                                                                               "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                                                                               "PREFIX dbo: <http://dbpedia.org/ontology/>\n" +
                                                                               "PREFIX dbr: <http://dbpedia.org/resource/>\n" +
                                                                               "SELECT ?month (count(distinct ?article) as ?numOfArticles) WHERE {\n" +
                                                                               "?article oae:mentions ?entity .\n" +
                                                                               "?entity oae:hasMatchedURI  <" + entity + "> .\n" +
                                                                               "?article dc:date ?date FILTER(?date >= xsd:date('" + fromDate + "') &&\n" +
                                                                               "?date <= xsd:date('" + toDate + "')).\n" +
                                                                               "} GROUP BY (month(?date) AS ?month) order by ?month");
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
        Map<String, String> Period_Docs_Map = new LinkedHashMap<String, String>();
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
        
        //Printing each time period p and the number of documents in each time period
        for(Map.Entry<String, String> entry: Period_Docs_Map.entrySet()){
            String month = entry.getKey();
            System.out.println("Month "+ month);
            String numDocs = entry.getValue();
            System.out.println("Number of Documents"+ numDocs);
            numDocs = numDocs.replaceAll("\n", "").replaceAll(" ", "");
            totaldocs += Integer.parseInt(numDocs);
        }         
        
        System.out.println("Total Docs "+ totaldocs);
        
        //Calculate the timeliness score 
        Map<String, Double> TimePeriodP_Score_Map = new LinkedHashMap<String, Double>();
        
        for (Map.Entry<String, String> entry : Period_Docs_Map.entrySet()) {
            String year = entry.getKey();
            String numDocs = entry.getValue();
            numDocs = numDocs.replaceAll("\n", "").replaceAll(" ", "");
            double number_of_docs = Double.parseDouble(numDocs);
            double timePeriodScore = number_of_docs / totaldocs;
            TimePeriodP_Score_Map.put(year, timePeriodScore);
        }

        //Printing each time period p and the importance score 
        for(Map.Entry<String, Double> entry: TimePeriodP_Score_Map.entrySet()){
            String month = entry.getKey();
            System.out.println("Year "+ month);
            Double score = entry.getValue();
            System.out.println("Score"+ score);
        }
        
        return TimePeriodP_Score_Map;
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
