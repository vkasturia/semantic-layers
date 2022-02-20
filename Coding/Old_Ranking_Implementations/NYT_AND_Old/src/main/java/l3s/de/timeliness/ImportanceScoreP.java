/*
Copyright 2017-2022 Vaibhav Kasturia <vbh18kas@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package l3s.de.timeliness;

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

/**
 *
 * @author Class to calculate Timeliness Score
 * 
 * Class to calculate Importance Scores for each Time Period p
 */

public class ImportanceScoreP {

    public Map<String, Double> imp_score_return(String[] entityArray, String fromDate, String toDate, String timePeriod) {
        
        //Setting granularity as specified in timePeriod to take as granularity
        ParameterizedSparqlString totalDocsByP = new ParameterizedSparqlString("PREFIX dc: <http://purl.org/dc/terms/>\n" +
                                                                               "PREFIX oae: <http://www.ics.forth.gr/isl/oae/core#>\n" +
                                                                               "PREFIX dbc: <http://dbpedia.org/resource/Category:>\n" +
                                                                               "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                                                                               "PREFIX dbo: <http://dbpedia.org/ontology/>\n" +
                                                                               "PREFIX dbr: <http://dbpedia.org/resource/>\n" +
                                                                               "PREFIX schema:  <http://schema.org/>\n" +
                                                                               "SELECT ?"+timePeriod+" (count(distinct ?article) as ?numOfArticles) WHERE {\n" +
                                                                               "?article oae:mentions ?entity .\n" +
                                                                               "?entity oae:hasMatchedURI  <" + entityArray[0] + "> .\n");
        
        for(int i = 1; i< entityArray.length; i++){
            ParameterizedSparqlString totalDocsByP_toAppend = new ParameterizedSparqlString("?article oae:mentions ?entity"+i+" .\n" +
                                                                                            "?entity"+i+" oae:hasMatchedURI  <" + entityArray[i] + "> .\n"); 
            totalDocsByP.append(totalDocsByP_toAppend);
        }
        
        ParameterizedSparqlString totalDocsByP_endAppend = new ParameterizedSparqlString("?article dc:date ?date FILTER(?date >= xsd:date('" + fromDate + "') &&\n" +
                                                                                         "?date <= xsd:date('" + toDate + "')).\n" +
                                                                                         "} GROUP BY ("+timePeriod+"(?date) AS ?"+timePeriod+") order by ?"+timePeriod+" ");
        
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
                String timeperiod = eElement.getElementsByTagName("binding").item(0).getTextContent();
                String numDocs = eElement.getElementsByTagName("binding").item(1).getTextContent();
                Period_Docs_Map.put(timeperiod, numDocs);
            }
        }
        
        int totaldocs = 0;
        
        //Printing each time period p and the number of documents in each time period
        for(Map.Entry<String, String> entry: Period_Docs_Map.entrySet()){
            String granularity = entry.getKey();
            System.out.println(timePeriod + " : " + granularity);
            String numDocs = entry.getValue();
            System.out.println("Number of Documents"+ numDocs);
            numDocs = numDocs.replaceAll("\n", "").replaceAll(" ", "");
            totaldocs += Integer.parseInt(numDocs);
        }         
        
        System.out.println("Total Docs "+ totaldocs);
        
        //Calculate the timeliness score 
        Map<String, Double> TimePeriodP_Score_Map = new LinkedHashMap<>();
        
        for (Map.Entry<String, String> entry : Period_Docs_Map.entrySet()) {
            String timePeriodp = entry.getKey();
            String numDocs = entry.getValue();
            numDocs = numDocs.replaceAll("\n", "").replaceAll(" ", "");
            double number_of_docs = Double.parseDouble(numDocs);
            double timePeriodScore = number_of_docs / totaldocs;
            TimePeriodP_Score_Map.put(timePeriodp, timePeriodScore);
        }

        //Printing each time period p and the importance score 
        TimePeriodP_Score_Map.entrySet().stream().map((entry) -> {
            String granularity = entry.getKey();
            System.out.println(timePeriod + " : " + granularity);
            Double numDocs = entry.getValue();
            return numDocs;
        }).forEach((numDocs) -> {
            System.out.println("Score " + numDocs);
        });
        
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


