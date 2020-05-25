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

public class RelEntities_and_OccurenceFinder{
    public Map<String, String> relentitiesreturn(String entity, String fromDate, String toDate){
         ParameterizedSparqlString relatedEntities = new ParameterizedSparqlString("PREFIX oa: <http://www.w3.org/ns/oa#>\n" +
                                                                                   "PREFIX oae: <http://www.ics.forth.gr/isl/oae/core#>\n" +
                                                                                   "PREFIX dc: <http://purl.org/dc/terms/>\n" +
                                                                                   "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                                                                                   "SELECT DISTINCT  ?entityURI2 (count(distinct ?article) as ?numOfArticles) WHERE {\n" +
                                                                                   "?article dc:date ?date FILTER(?date >= xsd:date('"+fromDate+"') &&\n" + 
                                                                                   "?date <= xsd:date('"+toDate+"')).\n" +                                                                         
                                                                                   "?article oae:mentions ?entity .\n" +
                                                                                   "?entity oae:hasMatchedURI  <"+entity+"> .\n" +
                                                                                   "?article oae:mentions ?entity2 ." +
                                                                                   "?entity2 oae:hasMatchedURI  ?entityURI2\n" +
                                                                                   "FILTER(<"+entity+"> != ?entityURI2)\n" +
                                                                                   "} GROUP BY ?entityURI2 ORDER BY DESC(?numOfArticles)");
    
         VirtuosoConnector connec = new VirtuosoConnector();
         VirtGraph graph = new VirtGraph(connec.getGraph(),connec.getHost(),connec.getUsername(),connec.getPwd()); 
         VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(relatedEntities.toString(), graph); 
         ResultSet results = vqe.execSelect();
         String relEntitiesxml = ResultSetFormatter.asXMLString(results);
         //System.out.println(relEntitiesxml);
         Document doc = null;
         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
         try{
               DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	       doc = loadXMLFromString(relEntitiesxml);
         }catch(Exception e){
             System.out.println("Error in parsing Related Entities XML");
         }
         Map<String, String> relEntitiesMap = new LinkedHashMap<String, String>();
         NodeList nList = doc.getElementsByTagName("result");
             for (int i = 0; i < nList.getLength(); i++){
                 Node nNode = nList.item(i);
                 if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                      Element eElement = (Element) nNode;
                      String relEntity = eElement.getElementsByTagName("binding").item(0).getTextContent();
                      String numOccurences = eElement.getElementsByTagName("binding").item(1).getTextContent();
                      relEntitiesMap.put(relEntity, numOccurences);
                }
             }
        return relEntitiesMap;     
//        for(Map.Entry<String, String> entry: relEntitiesMap.entrySet()){
//            String relEntity = entry.getKey();
//            System.out.println("Related Entity "+ relEntity);
//            String numOccurences = entry.getValue();
//            System.out.println("Number of Documents"+ numOccurences);
//        }     
    }     
    protected static Document loadXMLFromString(String xml) throws Exception{
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
    }
}