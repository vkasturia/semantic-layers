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


package position;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
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
import ranking_algo_category.VirtuosoConnector;
import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

/**
 *
 * @author Vaibhav Kasturia <kasturia at l3s.de>
 * 
 * Class to calculate the Proximity Score 
 */

public class ProximityScore {
    public Object[] proximity_score_calc(String[] entityArray, String fromDate, String toDate, String timePeriod){
        Object[] returnedValues = new Object[3];
        
        ProximityScore proximityScore = new ProximityScore();
        
        Object[] values = proximityScore.average_distance(entityArray, fromDate, toDate, timePeriod);
        Map<String, List<String>> timePeriod_documentList_map = (Map<String, List<String>>) values[0];
        Map<String, Map<String, List<Double>>> document_relatedEntity_averageDistanceList_map = (Map<String, Map<String, List<Double>>>) values[1];
        Map<String, Map<String, List<Integer>>> document_queryEntity_positionList_map = (Map<String, Map<String, List<Integer>>>) values[2];
        Map<String, Map<String, List<Integer>>> document_relatedEntity_positionList_map = (Map<String, Map<String, List<Integer>>>) values[3];        
        
        Map<String, Double> document_relEntityProximityScoreInDoc_map = new LinkedHashMap<>();
        Map<String, Map<String, Double>> relEntity_document_relEntityProximityScoreInDoc_map = new LinkedHashMap<>();
        
        for(Map.Entry<String, Map<String, List<Double>>> outerEntry: document_relatedEntity_averageDistanceList_map.entrySet()){
           String document = outerEntry.getKey();
           
            for (Map.Entry<String, List<Double>> innerEntry : outerEntry.getValue().entrySet()) {
                String relatedEntity = innerEntry.getKey();
                double relatedEntityProximityScore = 0;
                for (Double averageDistance : innerEntry.getValue()) {
                    relatedEntityProximityScore += (1 / averageDistance);
                }
                relatedEntityProximityScore *= (double) document_queryEntity_positionList_map.get(document).size();
                relatedEntityProximityScore /= (double) entityArray.length;
                if(Double.isInfinite(relatedEntityProximityScore))
                    relatedEntityProximityScore = 0;
                //Store the related entities and their proximity scores in the relEntity_document_relEntityProximityScoreInDoc_map 
                if (relEntity_document_relEntityProximityScoreInDoc_map.containsKey(relatedEntity)) {
                    document_relEntityProximityScoreInDoc_map = relEntity_document_relEntityProximityScoreInDoc_map.get(relatedEntity);
                    document_relEntityProximityScoreInDoc_map.put(document, relatedEntityProximityScore);
                    relEntity_document_relEntityProximityScoreInDoc_map.put(relatedEntity, document_relEntityProximityScoreInDoc_map);
                } else {
                    document_relEntityProximityScoreInDoc_map = new LinkedHashMap<>();
                    document_relEntityProximityScoreInDoc_map.put(document, relatedEntityProximityScore);
                    relEntity_document_relEntityProximityScoreInDoc_map.put(relatedEntity, document_relEntityProximityScoreInDoc_map);
                }
            }
        }
        
        Map<String, Double> timePeriod_relEntityProximityScoreInPeriod_map = new LinkedHashMap<>();
        Map<String, Map<String, Double>> relEntity_timePeriod_relEntityProximityScoreInPeriod_map = new LinkedHashMap<>();
        
        for (Map.Entry<String, Map<String, Double>> Entry1 : relEntity_document_relEntityProximityScoreInDoc_map.entrySet()) {
            String related_Entity = Entry1.getKey();
            for (Map.Entry<String, List<String>> Entry2 : timePeriod_documentList_map.entrySet()) {
                String time_Period = Entry2.getKey();
                double relEntityProximityScoreInPeriod = 0;
                for (String article : Entry2.getValue()) {
                    if (Entry1.getValue().containsKey(article)) {
                        relEntityProximityScoreInPeriod += Entry1.getValue().get(article);
                    }
                }
                relEntityProximityScoreInPeriod /= Entry2.getValue().size();
                //Store the related entities and their proximity scores in the relEntity_timePeriod_relEntityProximityScoreInPeriod_map 
                if (relEntity_timePeriod_relEntityProximityScoreInPeriod_map.containsKey(related_Entity)) {
                    timePeriod_relEntityProximityScoreInPeriod_map = relEntity_timePeriod_relEntityProximityScoreInPeriod_map.get(related_Entity);
                    timePeriod_relEntityProximityScoreInPeriod_map.put(time_Period, relEntityProximityScoreInPeriod);
                    relEntity_timePeriod_relEntityProximityScoreInPeriod_map.put(related_Entity, timePeriod_relEntityProximityScoreInPeriod_map);
                } else {
                    timePeriod_relEntityProximityScoreInPeriod_map = new LinkedHashMap<>();
                    timePeriod_relEntityProximityScoreInPeriod_map.put(time_Period, relEntityProximityScoreInPeriod);
                    relEntity_timePeriod_relEntityProximityScoreInPeriod_map.put(related_Entity, timePeriod_relEntityProximityScoreInPeriod_map);
                }
            }
        }
        
        returnedValues[0] = document_queryEntity_positionList_map;
        returnedValues[1] = document_relatedEntity_positionList_map;
        returnedValues[2] = relEntity_timePeriod_relEntityProximityScoreInPeriod_map;
        
        
        return returnedValues;
    }
    
    public Object[] average_distance(String[] entityArray, String fromDate, String toDate, String timePeriod){
        
        Object[] returnedValues = new Object[4];
        
        ProximityScore proximityScore = new ProximityScore();
        Object[] values = proximityScore.entity_position(entityArray, fromDate, toDate, timePeriod);
        
        Map<String, List<String>> timePeriod_documentList_map = (Map<String, List<String>>) values[0];
        Map<String, Map<String, List<Integer>>> document_queryEntity_positionList_map = (Map<String, Map<String, List<Integer>>>) values[1];
        Map<String, Map<String, List<Integer>>> document_relatedEntity_positionList_map = (Map<String, Map<String, List<Integer>>>) values[2];
        
        //Data Structure to store the average distances for a related entity in a document
        List<Double> averageDistanceList = new LinkedList<>();
        Map<String, List<Double>> relatedEntity_averageDistanceList_map = new LinkedHashMap<>();
        Map<String, Map<String, List<Double>>> document_relatedEntity_averageDistanceList_map = new LinkedHashMap<>();
        
        //Getting the average distances 
        Map<String, List<Integer>> queryEntity_positionList_map = new LinkedHashMap<>();
        Map<String, List<Integer>> relatedEntity_positionList_map = new LinkedHashMap<>();
        
        List<Integer> queryEntityPositionList = new LinkedList<>();
        List<Integer> relatedEntityPositionList = new LinkedList<>();
        
        for(Map.Entry<String, Map<String, List<Integer>>> outerEntry: document_queryEntity_positionList_map.entrySet()){
            String document = outerEntry.getKey();
            queryEntity_positionList_map = outerEntry.getValue();
            
            for(Map.Entry<String, List<Integer>> innerEntry : queryEntity_positionList_map.entrySet()){
                String queryEntity = innerEntry.getKey();
                queryEntityPositionList = innerEntry.getValue();
                
                relatedEntity_positionList_map = document_relatedEntity_positionList_map.get(document);
                for(Map.Entry<String, List<Integer>> innerEntry2: relatedEntity_positionList_map.entrySet()){
                    String relatedEntity = innerEntry2.getKey();
                    relatedEntityPositionList = innerEntry2.getValue();
                    
                    Map<Integer, String> combinedMap = new LinkedHashMap<>();
                    
                    queryEntityPositionList.stream().forEach((i) -> {
                        combinedMap.put(i, queryEntity);
                    });

                    relatedEntityPositionList.stream().forEach((i) -> {
                        combinedMap.put(i, relatedEntity);
                    });

                    combinedMap.put(-1, "");
                    combinedMap.put(Integer.MAX_VALUE, "");

                    NavigableMap<Integer, String> combinedTreeMap = new TreeMap<>(combinedMap);

//                    for (Map.Entry<Integer, String> combinedMapEntry : combinedTreeMap.entrySet()) {
//                        System.out.println(combinedMapEntry.getKey() + " : " + combinedMapEntry.getValue());
//                    }     
                    
                    double differenceSum = 0;
                    double counter = 0;
                    double averageDistance = 0;
                    
                    for (Map.Entry<Integer, String> combinedMapEntry : combinedTreeMap.entrySet()) {
                        try {
                            Map.Entry<Integer, String> previous = combinedTreeMap.lowerEntry(combinedMapEntry.getKey());
                            Map.Entry<Integer, String> next = combinedTreeMap.higherEntry(combinedMapEntry.getKey());

                            //System.out.println(previous.getKey() + " : " + previous.getValue());
                            //System.out.println(combinedMapEntry.getKey() + " : " + combinedMapEntry.getValue());
                            //System.out.println(next.getKey() + " : " + next.getValue() + "\n");
                            
                            if (combinedMapEntry.getValue().equals(queryEntity) && next.getValue().equals(relatedEntity) && previous.getValue().equals(relatedEntity)) {
                                int difference = Math.abs(combinedMapEntry.getKey() - previous.getKey());
                                int otherDifference = Math.abs(combinedMapEntry.getKey() - next.getKey());
                                if (otherDifference < difference) {
                                    difference = otherDifference;
                                }
                                differenceSum += difference;
                                counter += 1;
                            } else if (combinedMapEntry.getValue().equals(queryEntity) && next.getValue().equals(relatedEntity) && !previous.getValue().equals(relatedEntity)) {
                                int difference = Math.abs(combinedMapEntry.getKey() - next.getKey());
                                differenceSum += difference;
                                counter += 1;
                            } else if (combinedMapEntry.getValue().equals(queryEntity) && previous.getValue().equals(relatedEntity) && !next.getValue().equals(relatedEntity)) {
                                int difference = Math.abs(combinedMapEntry.getKey() - previous.getKey());
                                differenceSum += difference;
                                counter += 1;
                            }

                        } catch (NullPointerException e) {
                        }
                    }
                    if (counter != 0) {
                        averageDistance = differenceSum / counter;
                    }

                    //Store the related entities and their average distances in the document_relatedEntity_averageDistanceList_map 
                    if (document_relatedEntity_averageDistanceList_map.containsKey(document)) {
                        // If the key (document) is already present in the document_relatedEntity_averageDistanceList_map,
                        // we proceed to check the relatedEntity_averageDistanceList_map inside the document_relatedEntity_averageDistanceList_map
                        relatedEntity_averageDistanceList_map = document_relatedEntity_averageDistanceList_map.get(document);
                        if (relatedEntity_averageDistanceList_map.containsKey(relatedEntity)) {
                            //If the key (relatedEntity) is already present in the relatedEntity_averageDistanceList_map,
                            //we get the averageDistanceList and add the averageDistance to it and 
                            //then store the new averageDistanceList for the relatedEntity inside document_relatedEntity_averageDistanceList_map
                            averageDistanceList = relatedEntity_averageDistanceList_map.get(relatedEntity);
                            averageDistanceList.add(averageDistance);
                            relatedEntity_averageDistanceList_map.put(relatedEntity, averageDistanceList);
                        } else {
                            //If the key (relatedEntity) hasn't been used yet,
                            //then we add relatedEntity and averageDistance to relatedEntity_averageDistanceList_map
                            averageDistanceList = new ArrayList<>();
                            averageDistanceList.add(averageDistance);
                            relatedEntity_averageDistanceList_map.put(relatedEntity, averageDistanceList);
                        }
                    } else {
                        // If the key (document) hasn't been used yet,
                        // then we add all three: document, relatedEntity, averageDistance
                        // first we add relatedEntity and averageDistance to relatedEntity_averageDistanceList_map
                        // then we add document and relatedEntity_averageDistanceList_map to document_relatedEntity_averageDistanceList_map
                        averageDistanceList = new ArrayList<>();
                        averageDistanceList.add(averageDistance);
                        relatedEntity_averageDistanceList_map = new LinkedHashMap<>();
                        relatedEntity_averageDistanceList_map.put(relatedEntity, averageDistanceList);
                        document_relatedEntity_averageDistanceList_map.put(document, relatedEntity_averageDistanceList_map);
                    }
                }
            }
        }
        returnedValues[0] = timePeriod_documentList_map;
        returnedValues[1] = document_relatedEntity_averageDistanceList_map;
        returnedValues[2] = document_queryEntity_positionList_map;
        returnedValues[3] = document_relatedEntity_positionList_map;
        
        return returnedValues;
    }
    
    public Object[] entity_position(String[] entityArray, String fromDate, String toDate, String timePeriod) {

        Object[] values = new Object[3];
        
        
        //Declare data structures to store the query outputs
        List<String> documentList = new LinkedList<>();
        Map<String, List<String>> timePeriod_documentList_map = new LinkedHashMap<>();
        
        List<Integer> positionList = new LinkedList<>();
        
        Map<String, List<Integer>> relatedEntity_positionList_map = new LinkedHashMap<>();
        Map<String, Map<String, List<Integer>>> document_relatedEntity_positionList_map = new LinkedHashMap<>();
        
        Map<String, List<Integer>> queryEntity_positionList_map = new LinkedHashMap<>();
        Map<String, Map<String, List<Integer>>> document_queryEntity_positionList_map = new LinkedHashMap<>();
        
        for (int u = 0; u < entityArray.length; u++) {
            int h = 0;
            int counter = Integer.MAX_VALUE;

            while (counter > 9999) {
                //Set the granularity in the queries according to the time period granularity p, that is, week, month or year
                ParameterizedSparqlString EntityPositionCounter = new ParameterizedSparqlString("PREFIX oa: <http://www.w3.org/ns/oa#>\n" +
                                                                                                "PREFIX oae: <http://www.ics.forth.gr/isl/oae/core#>\n" +
                                                                                                "PREFIX dc: <http://purl.org/dc/terms/>\n" +
                                                                                                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                                                                                                "PREFIX schema:  <http://schema.org/>\n" +
                                                                                                "SELECT COUNT DISTINCT ?article  ?entityURI2 ?entity_rel_position ?" + timePeriod + "\n" +
                                                                                                "WHERE {\n" +
                                                                                                "?article dc:date ?date FILTER(?date >= xsd:date('" + fromDate + "') &&\n" +
                                                                                                "?date <= xsd:date('" + toDate + "')).\n" +
                                                                                                "BIND (CONCAT(month(?date), \"-\", year(?date)) AS ?month). \n" +
                                                                                                "BIND (CONCAT(day(?date), \"-\",month(?date), \"-\", year(?date)) AS ?day). \n" +
                                                                                                "BIND (CONCAT(year(?date)) AS ?year). \n" +
                                                                                                "?article schema:mentions ?entity .\n" +
                                                                                                "?entity oae:hasMatchedURI <" + entityArray[u] + ">.\n" +
                                                                                                "?article schema:mentions ?entity_rel .\n" +
                                                                                                "?entity_rel oae:hasMatchedURI  ?entityURI2. \n" +
                                                                                                "?entity_rel oae:position  ?entity_rel_position. \n" +
                                                                                                "}LIMIT 10000 OFFSET " + h);

                VirtuosoConnector connec = new VirtuosoConnector();
                VirtGraph graph = new VirtGraph(connec.getGraph(), connec.getHost(), connec.getUsername(), connec.getPwd());

                VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(EntityPositionCounter.toString(), graph);
                ResultSet results = vqe.execSelect();
                String EntityPositionCountxml = ResultSetFormatter.asXMLString(results);
                //System.out.println(EntityPositionCountxml);
                Document doc = null;
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                try {
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    doc = loadXMLFromString(EntityPositionCountxml);
                } catch (Exception e) {
                    System.out.println("Error in parsing Entity Position Counter XML in Proximity Score");
                }

                Element rootElement = doc.getDocumentElement();
                String elementcount = getString("literal", rootElement);
                elementcount = elementcount.replaceAll("\n", "").replaceAll(" ", "");

                counter = Integer.parseInt(elementcount);

                ParameterizedSparqlString PositionCountQuery = new ParameterizedSparqlString("PREFIX oa: <http://www.w3.org/ns/oa#>\n" +
                                                                                             "PREFIX oae: <http://www.ics.forth.gr/isl/oae/core#>\n" +
                                                                                             "PREFIX dc: <http://purl.org/dc/terms/>\n" +
                                                                                             "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                                                                                             "PREFIX schema:  <http://schema.org/>\n" +
                                                                                             "SELECT DISTINCT ?article  ?entityURI2 ?entity_rel_position ?" + timePeriod + "\n" +
                                                                                             "WHERE {\n" +
                                                                                             "?article dc:date ?date FILTER(?date >= xsd:date('" + fromDate + "') &&\n" +
                                                                                             "?date <= xsd:date('" + toDate + "')).\n" +
                                                                                             "BIND (CONCAT(month(?date), \"-\", year(?date)) AS ?month). \n" +
                                                                                             "BIND (CONCAT(day(?date), \"-\",month(?date), \"-\", year(?date)) AS ?day). \n" +
                                                                                             "BIND (CONCAT(year(?date)) AS ?year). \n" +
                                                                                             "?article schema:mentions ?entity .\n" +
                                                                                             "?entity oae:hasMatchedURI <" + entityArray[u] + ">.\n" +
                                                                                             "?article schema:mentions ?entity_rel .\n" +
                                                                                             "?entity_rel oae:hasMatchedURI  ?entityURI2. \n" +
                                                                                             "?entity_rel oae:position  ?entity_rel_position. \n" +
                                                                                             "}LIMIT 10000 OFFSET " + h);

                vqe = VirtuosoQueryExecutionFactory.create(PositionCountQuery.toString(), graph);
                results = vqe.execSelect();
                String PositionCountXml = ResultSetFormatter.asXMLString(results);

            //System.out.println(PositionCountXml);
                doc = null;
                dbFactory = DocumentBuilderFactory.newInstance();
                try {
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    doc = loadXMLFromString(PositionCountXml);
                } catch (Exception e) {
                    System.out.println("Error in parsing Position Count XML");
                }

                NodeList nList = doc.getElementsByTagName("result");
                for (int i = 0; i < nList.getLength(); i++) {
                    Node nNode = nList.item(i);
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;

                        String articleurl = eElement.getElementsByTagName("binding").item(0).getTextContent();
                        String relEntity = eElement.getElementsByTagName("binding").item(1).getTextContent();
                        String position = eElement.getElementsByTagName("binding").item(2).getTextContent();
                        String timePeriodP = eElement.getElementsByTagName("binding").item(3).getTextContent();

                        articleurl = articleurl.replaceAll("\n", "").replaceAll(" ", "");
                        relEntity = relEntity.replaceAll("\n", "").replaceAll(" ", "");
                        position = position.replaceAll("\n", "").replaceAll(" ", "");
                        timePeriodP = timePeriodP.replaceAll("\n", "").replaceAll(" ", "");

                        //Store the TimePeriod and the corresponding articles published in that period in the timePeriod_documentList_map
                        if (timePeriod_documentList_map.containsKey(timePeriodP)) {
                            documentList = timePeriod_documentList_map.get(timePeriodP);
                            if (!documentList.contains(articleurl)) {
                                documentList.add(articleurl);
                                timePeriod_documentList_map.put(timePeriodP, documentList);
                            }
                        } else {
                            documentList = new ArrayList<>();
                            documentList.add(articleurl);
                            timePeriod_documentList_map.put(timePeriodP, documentList);
                        }

                        if (Arrays.asList(entityArray).contains(relEntity)) {
                            //Store the query entities in the  document_queryEntity_positionList_map 
                            if (document_queryEntity_positionList_map.containsKey(articleurl)) {
                            // If the key (articleurl) is already present in the document_queryEntity_positionList_map,
                                // we proceed to check the queryEntity_positionList_map inside the document_queryEntity_positionList_map
                                queryEntity_positionList_map = document_queryEntity_positionList_map.get(articleurl);
                                if (queryEntity_positionList_map.containsKey(relEntity)) {
                                //If the key (relEntity) is already present in the queryEntity_positionList_map,
                                    //we get the positionList and add the position to it and 
                                    //then store the new positionList for the relEntity inside document_queryEntity_positionList_map
                                    positionList = queryEntity_positionList_map.get(relEntity);
                                    positionList.add(Integer.parseInt(position));
                                    queryEntity_positionList_map.put(relEntity, positionList);
                                } else {
                                //If the key (relEntity) hasn't been used yet,
                                    //then we add relEntity and position to queryEntity_positionList_map
                                    positionList = new ArrayList<>();
                                    positionList.add(Integer.parseInt(position));
                                    queryEntity_positionList_map.put(relEntity, positionList);
                                }
                            } else {
                            // If the key (articleurl) hasn't been used yet,
                                // then we add all three: articleurl, relEntity, position
                                // first we add relEntity and position to queryEntity_positionList_map
                                // then we add articleurl and queryEntity_positionList_map to document_queryEntity_positionList_map
                                positionList = new ArrayList<>();
                                positionList.add(Integer.parseInt(position));
                                queryEntity_positionList_map = new LinkedHashMap<>();
                                queryEntity_positionList_map.put(relEntity, positionList);
                                document_queryEntity_positionList_map.put(articleurl, queryEntity_positionList_map);
                            }
                        } else {
                            //Store the related entities in the  document_relatedEntity_positionList_map Structure 
                            if (document_relatedEntity_positionList_map.containsKey(articleurl)) {
                            // If the key (articleurl) is already present in the document_relatedEntity_positionList_map,
                                // we proceed to check the relatedEntity_positionList_map inside the document_relatedEntity_positionList_map
                                relatedEntity_positionList_map = document_relatedEntity_positionList_map.get(articleurl);
                                if (relatedEntity_positionList_map.containsKey(relEntity)) {
                                //If the key (relEntity) is already present in the relatedEntity_positionList_map,
                                    //we get the positionList and add the position to it and 
                                    //then store the new positionList for the relEntity inside document_relatedEntity_positionList_map
                                    positionList = relatedEntity_positionList_map.get(relEntity);
                                    positionList.add(Integer.parseInt(position));
                                    relatedEntity_positionList_map.put(relEntity, positionList);
                                } else {
                                //If the key (relEntity) hasn't been used yet,
                                    //then we add relEntity and position to relatedEntity_positionList_map
                                    positionList = new ArrayList<>();
                                    positionList.add(Integer.parseInt(position));
                                    relatedEntity_positionList_map.put(relEntity, positionList);
                                }
                            } else {
                            // If the key (articleurl) hasn't been used yet,
                                // then we add all three: articleurl, relEntity, position
                                // first we add relEntity and position to relatedEntity_positionList_map
                                // then we add articleurl and relatedEntity_positionList_map to document_relatedEntity_positionList_map
                                positionList = new ArrayList<>();
                                positionList.add(Integer.parseInt(position));
                                relatedEntity_positionList_map = new LinkedHashMap<>();
                                relatedEntity_positionList_map.put(relEntity, positionList);
                                document_relatedEntity_positionList_map.put(articleurl, relatedEntity_positionList_map);
                            }
                        }
                    }
                }
                h += 10000;
            }
        }
        values[0] = timePeriod_documentList_map;
        values[1] = document_queryEntity_positionList_map;
        values[2] = document_relatedEntity_positionList_map;
        return values;
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
