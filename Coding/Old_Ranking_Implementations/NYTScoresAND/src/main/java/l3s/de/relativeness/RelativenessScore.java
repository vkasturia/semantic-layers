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

package l3s.de.relativeness;


import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
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
import l3s.de.ranking_algo_and.VirtuosoConnector;
import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

/**
 *
 * @author vaibhav
 * 
 * Class to calculate the Relativeness Score
 * The Score calculation queries still need to be verified
 */
public class RelativenessScore {
    public void relativeness_score(String entity, String fromDate, String toDate) throws FileNotFoundException{
        RelativenessScore relScore = new RelativenessScore();
        Map<String, Double> document_relativenessScore_map = relScore.relativenessScoreMapReturn(entity, fromDate, toDate);
            
        Map<String, Double> Ranked_Article_RelativenessScore_Map = relScore.sortHashMapByValues(document_relativenessScore_map);
        
        //Printing each article and the timelinessScore in a csv file
        PrintWriter writer = new PrintWriter("./ranking_relativeness.csv");
	writer.println("Article; RelativenessScore");
        
        for(Map.Entry<String, Double> entry: Ranked_Article_RelativenessScore_Map.entrySet()){
            String article = entry.getKey();
            System.out.println("Article "+ article);
            Double relativenessScore = entry.getValue();
            System.out.println("RelativenessScore "+ relativenessScore);
            writer.println(article + "; "+ relativenessScore);
        }
        
        writer.close();    
    } 
    public Map<String, Double> relativenessScoreMapReturn(String entity, String fromDate, String toDate){
         int counter = Integer.MAX_VALUE;
         int h = 0;
         
         Map<String, Double> document_relativenessScore_map = new LinkedHashMap<>();
         
         while(counter > 10000){
         ParameterizedSparqlString resultsCounter = new ParameterizedSparqlString("PREFIX oa: <http://www.w3.org/ns/oa#>\n" +                  
                                                                                          "PREFIX oae: <http://www.ics.forth.gr/isl/oae/core#>\n" +
                                                                                          "PREFIX dc: <http://purl.org/dc/terms/>\n" +
                                                                                          "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                                                                                          "PREFIX schema:  <http://schema.org/>\n" +
                                                                                          "SELECT COUNT DISTINCT ?article (xsd:double(?entitycount)/(xsd:double(?relentitycount)/xsd:double(?entitycount)) as ?relativenessscore)\n" +
                                                                                          "WHERE{\n" + 
                                                                                          "SELECT DISTINCT ?article  (count(?entityURI2) as ?relentitycount) (count(distinct ?entity) as ?entitycount)\n" + 
                                                                                          "WHERE {\n" +
                                                                                          "?article dc:date ?date FILTER(?date >= xsd:date('"+fromDate+"') &&\n" +
                                                                                          "?date <= xsd:date('"+toDate+"')).\n" + 
                                                                                          "?article schema:mentions ?entity .\n" +
                                                                                          "?entity oae:hasMatchedURI <"+entity+">." +   
                                                                                          "?article schema:mentions ?entity2 ." +
                                                                                          "?entity2 oae:hasMatchedURI  ?entityURI2" +
                                                                                          "}\n"+
                                                                                          "}LIMIT 10000 OFFSET " + h
                                                                                          );
         
         VirtuosoConnector connec = new VirtuosoConnector();
         VirtGraph graph = new VirtGraph(connec.getGraph(),connec.getHost(),connec.getUsername(),connec.getPwd()); 
         
         VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(resultsCounter.toString(), graph); 
         ResultSet results = vqe.execSelect();
         String resultsCounterXml = ResultSetFormatter.asXMLString(results);
         //System.out.println(resultsCounterXml);
         Document doc = null;
         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
         try{
               DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	       doc = loadXMLFromString(resultsCounterXml);
         }catch(Exception e){
             System.out.println("Error in parsing Results Counter XML");
         }
         
         Element rootElement = doc.getDocumentElement();
         String resultscount = getString("literal",rootElement);
         resultscount = resultscount.replaceAll("\n", "").replaceAll(" ", "");
         
         counter = Integer.parseInt(resultscount);
         
         ParameterizedSparqlString relativenessScoreQuery = new ParameterizedSparqlString("PREFIX oa: <http://www.w3.org/ns/oa#>\n" +                  
                                                                                          "PREFIX oae: <http://www.ics.forth.gr/isl/oae/core#>\n" +
                                                                                          "PREFIX dc: <http://purl.org/dc/terms/>\n" +
                                                                                          "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                                                                                          "PREFIX schema:  <http://schema.org/>\n" +
                                                                                          "SELECT DISTINCT ?article (xsd:double(?entitycount)/(xsd:double(?relentitycount)/xsd:double(?entitycount)) as ?relativenessscore)\n" +
                                                                                          "WHERE{\n" + 
                                                                                          "SELECT DISTINCT ?article  (count(?entityURI2) as ?relentitycount) (count(distinct ?entity) as ?entitycount)\n" + 
                                                                                          "WHERE {\n" +
                                                                                          "?article dc:date ?date FILTER(?date >= xsd:date('"+fromDate+"') &&\n" +
                                                                                          "?date <= xsd:date('"+toDate+"')).\n" + 
                                                                                          "?article schema:mentions ?entity .\n" +
                                                                                          "?entity oae:hasMatchedURI <"+entity+">." +   
                                                                                          "?article schema:mentions ?entity2 ." +
                                                                                          "?entity2 oae:hasMatchedURI  ?entityURI2" +
                                                                                          "}\n"+
                                                                                          "}LIMIT 10000 OFFSET " + h
                                                                                          );
         
         
         
         vqe = VirtuosoQueryExecutionFactory.create(relativenessScoreQuery.toString(), graph); 
         results = vqe.execSelect();
         String relativenessScoreQueryXml = ResultSetFormatter.asXMLString(results);
         //System.out.println(relativenessScoreQueryXml);
         doc = null;
         dbFactory = DocumentBuilderFactory.newInstance();
         try{
               DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	       doc = loadXMLFromString(relativenessScoreQueryXml);
         }catch(Exception e){
             System.out.println("Error in parsing Relativeness Score Query XML");
         }
         
         
         NodeList nList = doc.getElementsByTagName("result");
             for (int i = 0; i < nList.getLength(); i++){
                 Node nNode = nList.item(i);
                 if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                      Element eElement = (Element) nNode;
                      
                      String articleurl = eElement.getElementsByTagName("binding").item(0).getTextContent();
                      String relativenessScore = eElement.getElementsByTagName("binding").item(1).getTextContent();
                      
                      articleurl = articleurl.replaceAll("\n", "").replaceAll(" ", "");
                      relativenessScore = relativenessScore.replaceAll("\n", "").replaceAll(" ", "");
                      
                      Double relativeScore = Double.parseDouble(relativenessScore);
                      document_relativenessScore_map.put(articleurl, relativeScore);
                }
             }
             
          h += 10000;
       }
       return document_relativenessScore_map;  
    }
    protected static Document loadXMLFromString(String xml) throws Exception{
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
    public static LinkedHashMap<String, Double> sortHashMapByValues(Map<String, Double> passedMap) {
        List<String> mapKeys = new ArrayList<>(passedMap.keySet());
        List<Double> mapValues = new ArrayList<>(passedMap.values());
        Collections.sort(mapValues, Collections.reverseOrder());
        Collections.sort(mapKeys, Collections.reverseOrder());

        LinkedHashMap<String, Double> sortedMap
                = new LinkedHashMap<>();

        Iterator<Double> valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            Double val = valueIt.next();
            Iterator<String> keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                String key = keyIt.next();
                Double comp1 = passedMap.get(key);
                Double comp2 = val;

                if (comp1.equals(comp2)) {
                    keyIt.remove();
                    sortedMap.put(key, val);
                    break;
                }
            }
        }
        return sortedMap;
    }
}
