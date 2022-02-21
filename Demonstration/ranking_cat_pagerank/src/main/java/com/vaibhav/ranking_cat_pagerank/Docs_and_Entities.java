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

package com.vaibhav.ranking_cat_pagerank;

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
import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

/**
 * @author Vaibhav Kasturia
 * 
 * Class to get documents, the entities mentioned in them and the count of the entities for the given query
 */

public class Docs_and_Entities {

    public Map<String, Map<String, Double>> docs_entities_return(String[] entityArray, String fromDate, String toDate) {

        //Data structure to store the documents, their extracted entities and the entity frequencies 
        Map<String, Double> Entities_EntityFrequency_map = new LinkedHashMap<>();
        Map<String, Map<String, Double>> Docs_Entities_Map = new LinkedHashMap<>();

        //Data structures to store intermediate output obtained from the SPARQL Query
        List<Double> entityCountList = new ArrayList();
        Map<String, List<Double>> entity_entityCountList_map = new LinkedHashMap();
        Map<String, Map<String, List<Double>>> articleUrl_entity_map = new LinkedHashMap();
        int n = entityArray.length;
        double[] entityCount = new double[n + 1];
        String[] entityCountArray = new String[n + 1];


        //Setting granularity as specified in timePeriod to take as granularity
        ParameterizedSparqlString Docs_and_Entities = new ParameterizedSparqlString("PREFIX dc: <http://purl.org/dc/terms/>\n" +
                "PREFIX oae: <http://www.ics.forth.gr/isl/oae/core#>\n" +
                "PREFIX dbc: <http://dbpedia.org/resource/Category:>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "PREFIX dbo: <http://dbpedia.org/ontology/>\n" +
                "PREFIX dbr: <http://dbpedia.org/resource/>\n" +
                "PREFIX schema:  <http://schema.org/>\n" +
                "SELECT DISTINCT ?article ?mentionedEntityURI (count(?mentionedEntityURI) as ?mentionedEntityURIcount) (count(distinct ?entity) as ?entitycount)");


        for (int i = 1; i < entityArray.length; i++) {
            ParameterizedSparqlString Docs_and_Entities_toAppend2 = new ParameterizedSparqlString(" (count(distinct ?entity" + i + ") as ?entitycount" + i + ")");
            Docs_and_Entities.append(Docs_and_Entities_toAppend2);
        }

        ParameterizedSparqlString Docs_and_Entities_toAppend3 = new ParameterizedSparqlString("WHERE { \n" +
                "{ \n" +
                "?article dc:date ?date FILTER(?date >= xsd:date('" + fromDate + "') && \n" +
                "?date <= xsd:date('" + toDate + "')). \n" +
                "?article schema:mentions ?entity .\n" +
                "?entity oae:hasMatchedURI <" + entityArray[0] + ">. \n" +
                "?article schema:mentions ?mentionedEntity .\n" +
                "?mentionedEntity oae:hasMatchedURI  ?mentionedEntityURI. \n" +
                "} \n"
        );
        Docs_and_Entities.append(Docs_and_Entities_toAppend3);

        for (int i = 1; i < entityArray.length; i++) {
            ParameterizedSparqlString Docs_and_Entities_toAppend4 = new ParameterizedSparqlString("UNION { \n" +
                    "?article dc:date ?date FILTER(?date >= xsd:date('" + fromDate + "') && \n" +
                    "?date <= xsd:date('" + toDate + "')). \n" +
                    "?article schema:mentions ?entity" + i + " .\n" +
                    "?entity" + i + " oae:hasMatchedURI <" + entityArray[i] + ">. \n" +
                    "?article schema:mentions ?mentionedEntity .\n" +
                    "?mentionedEntity oae:hasMatchedURI  ?mentionedEntityURI. \n" +
                    "} \n");
            Docs_and_Entities.append(Docs_and_Entities_toAppend4);
        }

        ParameterizedSparqlString Docs_and_Entities_endAppend = new ParameterizedSparqlString("}");
        Docs_and_Entities.append(Docs_and_Entities_endAppend);

        VirtuosoConnector connec = new VirtuosoConnector();
        VirtGraph graph = new VirtGraph(connec.getGraph(), connec.getHost(), connec.getUsername(), connec.getPwd());
        VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(Docs_and_Entities.toString(), graph);
        ResultSet results = vqe.execSelect();
        String docs_and_entities_xml = ResultSetFormatter.asXMLString(results);
        System.out.println(docs_and_entities_xml);
        Document doc = null;
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = loadXMLFromString(docs_and_entities_xml);
        } catch (Exception e) {
            System.out.println("Error in parsing DocsByPeriod XML");
        }

        NodeList nList = doc.getElementsByTagName("result");
        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                String docs = eElement.getElementsByTagName("binding").item(0).getTextContent();
                docs = docs.replaceAll("\n", "").replaceAll(" ", "");
                String entity = eElement.getElementsByTagName("binding").item(1).getTextContent();
                entity = entity.replaceAll("\n", "").replaceAll(" ", "");

                entityCountList = new ArrayList();
                for (int w = 0; w < entityArray.length + 1; w++) {
                    entityCountArray[w] = eElement.getElementsByTagName("binding").item(w + 2).getTextContent();
                    entityCountArray[w] = entityCountArray[w].replaceAll("\n", "").replaceAll(" ", "");
                    entityCount[w] = Double.parseDouble(entityCountArray[w]);
                    if (w > 1) {
                        entityCountList.add(entityCount[w] - 1);
                    } else {
                        entityCountList.add(entityCount[w]);
                    }
                }

                //Store the results in the Map<docs, Map<entity, entityCountList>> Structure 
                if (articleUrl_entity_map.containsKey(docs)) {
                    // If the key (docs) is already present in the articleUrl_entity_map,
                    // we proceed to check the entity_entityCount_Map inside the articleUrl_entity_map
                    entity_entityCountList_map = articleUrl_entity_map.get(docs);
                    entity_entityCountList_map.put(entity, entityCountList);
                } else {
                    // If the key (docs) hasn't been used yet,
                    // then we add all three: docs, entity, entityCountList
                    // first we add entity and entityCountList to entity_entityCountList_map
                    // then we add docs and entity_entityCountList_Map to articleUrl_entity_map
                    entity_entityCountList_map = new LinkedHashMap<>();
                    entity_entityCountList_map.put(entity, entityCountList);
                    articleUrl_entity_map.put(docs, entity_entityCountList_map);
                }


                for (Map.Entry<String, Map<String, List<Double>>> documentURL : articleUrl_entity_map.entrySet()) {
                    for (Map.Entry<String, List<Double>> entityURL : documentURL.getValue().entrySet()) {
                        double eoiSum = 0;
                        double entityFrequency = 0;
                        for (int w = 1; w < entityArray.length + 1; w++) {
                            eoiSum += entityURL.getValue().get(w);
                        }
                        entityFrequency = entityURL.getValue().get(0) / eoiSum;

                        //Store the results in the Map<Docs, Map<Entity, Frequency>> Structure 
                        if (Docs_Entities_Map.containsKey(docs)) {
                            // If the key (docs) is already present in the Docs_Entities_Map,
                            // we proceed to check the Entities_EntityFrequency_Map inside the Docs_Entities_Map
                            Entities_EntityFrequency_map = Docs_Entities_Map.get(docs);
                            if (Entities_EntityFrequency_map.containsKey(entity)) {
                                //If the key (entity) is already present in the Entity_EntityFrequency_Map,
                                //we get the entity and add the entityFrequency and 
                                //then store the frequency for the entity inside Entity_EntityFrequency_Map
                                Entities_EntityFrequency_map.put(entity, entityFrequency);
                            } else {
                                //If the key (entity) hasn't been used yet,
                                //then we add entity and entityFrequency to Entities_EntityFrequency_Map
                                Entities_EntityFrequency_map.put(entity, entityFrequency);
                            }
                        } else {
                            // If the key (docs) hasn't been used yet,
                            // then we add all three: docs, entity, entityFrequency
                            // first we add entity and frequency to Entities_EntityFrequency_map
                            // then we add docs and Entities_EntityFrequency_Map to Docs_Entities_Map
                            Entities_EntityFrequency_map = new LinkedHashMap<>();
                            Entities_EntityFrequency_map.put(entity, entityFrequency);
                            Docs_Entities_Map.put(docs, Entities_EntityFrequency_map);
                        }
                    }
                }
            }
        }

        Docs_Entities_Map.entrySet().stream().map((entry) -> {
            String article = entry.getKey();
            System.out.println(article);
            return entry;
        }).forEach((entry) -> {
            Map<String, Double> entity_entityFreq_map = entry.getValue();
            System.out.println(entity_entityFreq_map.entrySet());
        });

        return Docs_Entities_Map;
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
