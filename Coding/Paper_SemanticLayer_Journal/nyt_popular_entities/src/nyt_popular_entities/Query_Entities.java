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

package nyt_popular_entities;

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
import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

/**
 *
 * @author Vaibhav Kasturia <kasturia at l3s.de>
 * 
 * Class to get Popular Entities 
 */

public class Query_Entities{
    public Map<String, Double> entity_finder(String fromDate, String toDate, int counter) throws FileNotFoundException{
        
        Query_Entities query_entities = new Query_Entities();
        
        //Getting the articles and the corresponding time period P in which they were published 
        ParameterizedSparqlString popularEntities = new ParameterizedSparqlString("PREFIX oa: <http://www.w3.org/ns/oa#>\n" +
                                                                                  "PREFIX oae: <http://www.ics.forth.gr/isl/oae/core#>\n" +
                                                                                  "PREFIX dc: <http://purl.org/dc/terms/>\n" + 
                                                                                  "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                                                                                  "PREFIX schema:  <http://schema.org/>\n" +
                                                                                  "SELECT DISTINCT ?entityURI (count(?article) as ?numOfArticles) WHERE {\n" +
                                                                                  "?article dc:date ?date FILTER(?date >= xsd:date('" + fromDate + "') && \n" + 
                                                                                  "?date <= xsd:date('" + toDate + "')).\n" + 
                                                                                  "?article oae:mentions ?entity .\n" +
                                                                                  "?entity oae:hasMatchedURI  ?entityURI. \n" +
                                                                                  "} GROUP BY ?entityURI ORDER BY DESC(?numOfArticles) LIMIT 200");
        
        
        VirtuosoConnector connec = new VirtuosoConnector();
        VirtGraph graph = new VirtGraph(connec.getGraph(), connec.getHost(), connec.getUsername(), connec.getPwd());
        VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(popularEntities.toString(), graph);
        ResultSet results = vqe.execSelect();
        String totaldocsxml = ResultSetFormatter.asXMLString(results);
        Document doc = null;
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = loadXMLFromString(totaldocsxml);
        } catch (Exception e) {
            System.out.println("Error in parsing Popular Entities XML");
        }
        Map<String, Double> entityURI_entityCount_Map = new LinkedHashMap<>();
        NodeList nList = doc.getElementsByTagName("result");
        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                String entityURI = eElement.getElementsByTagName("binding").item(0).getTextContent();
                entityURI = entityURI.replaceAll("\n", "").replaceAll(" ", "");
                String entityCount = eElement.getElementsByTagName("binding").item(1).getTextContent();
                entityCount = entityCount.replaceAll("\n", "").replaceAll(" ", "");
                double entity_count = Double.parseDouble(entityCount);
                entityURI_entityCount_Map.put(entityURI, entity_count);
            }
        }
        
        Map<String, Double> Ranked_entityURI_entityCount_Map = query_entities.sortHashMapByValues(entityURI_entityCount_Map);
        
        int start_year_count = 1986; 
        int year_counter = start_year_count + counter;
        
        //Printing each article and the timelinessScore in a csv file
        PrintWriter writer = new PrintWriter("./results/popular_entities_"+year_counter+".csv");
	writer.println("Entity; Entity_Count");
        
        for(Map.Entry<String, Double> entry: Ranked_entityURI_entityCount_Map.entrySet()){
            String entityURI = entry.getKey();
            //System.out.println("Article "+ article);
            Double entityCount = entry.getValue();
            //System.out.println("TimelinessScore "+ timelinessScore);
            writer.println(entityURI + "; "+ entityCount);
        }
        
        writer.close();
        
        return Ranked_entityURI_entityCount_Map; 
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
