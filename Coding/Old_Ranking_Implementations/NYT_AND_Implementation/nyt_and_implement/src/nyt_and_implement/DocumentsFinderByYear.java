/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nyt_and_implement;

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

public class DocumentsFinderByYear{
    public Map<String, String> totaldocsreturn(String entity, String fromDate, String toDate){
         ParameterizedSparqlString totalDocsByYear = new ParameterizedSparqlString("PREFIX dc: <http://purl.org/dc/terms/>\n" +
                                                                                   "PREFIX oae: <http://www.ics.forth.gr/isl/oae/core#>\n" +
                                                                                   "PREFIX dbc: <http://dbpedia.org/resource/Category:>\n" +
                                                                                   "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" + 
                                                                                   "PREFIX dbo: <http://dbpedia.org/ontology/>\n" +
                                                                                   "PREFIX dbr: <http://dbpedia.org/resource/>\n" +
                                                                                   "SELECT ?year (count(distinct ?article) as ?numOfArticles) WHERE {\n" +
                                                                                   "?article oae:mentions ?entity .\n" +
                                                                                   "?entity oae:hasMatchedURI  <"+entity+"> .\n" + 
                                                                                   "?article dc:date ?date FILTER(?date >= xsd:date('"+fromDate+"') &&\n" + 
                                                                                   "?date <= xsd:date('"+toDate+"')).\n" +
                                                                                   "} GROUP BY (year(?date) AS ?year) order by ?year");
    
         VirtuosoConnector connec = new VirtuosoConnector();
         VirtGraph graph = new VirtGraph(connec.getGraph(),connec.getHost(),connec.getUsername(),connec.getPwd()); 
         VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(totalDocsByYear.toString(), graph); 
         ResultSet results = vqe.execSelect();
         String totaldocsxml = ResultSetFormatter.asXMLString(results);
         Document doc = null;
         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
         try{
               DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	       doc = loadXMLFromString(totaldocsxml);
         }catch(Exception e){
             System.out.println("Error in parsing DocsByYear XML");
         }
         Map<String, String> docsByYearMap = new LinkedHashMap<String, String>();
         NodeList nList = doc.getElementsByTagName("result");
             for (int i = 0; i < nList.getLength(); i++){
                 Node nNode = nList.item(i);
                 if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                      Element eElement = (Element) nNode;
                      String year = eElement.getElementsByTagName("binding").item(0).getTextContent();
                      String numDocs = eElement.getElementsByTagName("binding").item(1).getTextContent();
                      docsByYearMap.put(year, numDocs);
                 }
             }
        return docsByYearMap;     
//        for(Map.Entry<String, String> entry: docsByYearMap.entrySet()){
//            String year = entry.getKey();
//            System.out.println("Year "+ year);
//            String numDocs = entry.getValue();
//            System.out.println("Number of Documents"+ numDocs);
//        }     
    }     
    protected static Document loadXMLFromString(String xml) throws Exception{
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
    }
}
