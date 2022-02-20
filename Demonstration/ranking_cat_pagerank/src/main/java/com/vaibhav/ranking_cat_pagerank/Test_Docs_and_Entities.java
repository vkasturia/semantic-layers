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

/*
Copyright 2017-2022 Vaibhav Kasturia <vbh18kas@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package com.vaibhav.ranking_cat_pagerank;

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
 * @author Vaibhav Kasturia
 * <p>
 * Class to get documents, the entities mentioned in them and the count of the entities for the given query
 */

public class Test_Docs_and_Entities {

    public Object[] docs_entities_return(String[] entityArray, String fromDate, String toDate) {

        //Data structure to store the documents, their extracted entities and the entity frequencies 
        Map<String, Double> Entities_EntityFrequency_map = new LinkedHashMap<>();
        Map<String, Map<String, Double>> Docs_Entities_Map = new LinkedHashMap<>();

        //Data structure to store the entities, the documents that mention them and the entity frequencies
        Map<String, Double> Documents_EntityFrequency_map = new LinkedHashMap<>();
        Map<String, Map<String, Double>> Entity_Docs_Map = new LinkedHashMap<>();

        //Variables to store intermediate output obtained from the SPARQL Query
        double entity_Frequency;
        double eoi_Frequency;

        Object[] values = new Object[2];


        for (int u = 0; u < entityArray.length; u++) {
            int counter = Integer.MAX_VALUE;
            int h = 0;

            while (counter > 9999) {

                ParameterizedSparqlString Docs_and_Entities_Count = new ParameterizedSparqlString("PREFIX dc: <http://purl.org/dc/terms/>\n" +
                        "PREFIX oae: <http://www.ics.forth.gr/isl/oae/core#>\n" +
                        "PREFIX dbc: <http://dbpedia.org/resource/Category:>\n" +
                        "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                        "PREFIX dbo: <http://dbpedia.org/ontology/>\n" +
                        "PREFIX dbr: <http://dbpedia.org/resource/>\n" +
                        "PREFIX schema:  <http://schema.org/>\n" +
                        "SELECT COUNT DISTINCT ?article ?mentionedEntityURI (count(?mentionedEntityURI) as ?mentionedEntityURIcount) (count(distinct ?entity) as ?entitycount)");

                ParameterizedSparqlString Docs_and_Entities_Count_toAppend = new ParameterizedSparqlString("WHERE { \n" +
                        "?article dc:date ?date FILTER(?date >= xsd:date('" + fromDate + "') && \n" +
                        "?date <= xsd:date('" + toDate + "')). \n" +
                        "?article schema:mentions ?entity .\n" +
                        "?entity oae:hasMatchedURI <" + entityArray[u] + ">. \n" +
                        "?article schema:mentions ?mentionedEntity .\n" +
                        "?mentionedEntity oae:hasMatchedURI  ?mentionedEntityURI. \n" +
                        "}LIMIT 10000 OFFSET " + h);
                Docs_and_Entities_Count.append(Docs_and_Entities_Count_toAppend);

                VirtuosoConnector connec = new VirtuosoConnector();
                VirtGraph graph = new VirtGraph(connec.getGraph(), connec.getHost(), connec.getUsername(), connec.getPwd());
                VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(Docs_and_Entities_Count.toString(), graph);
                ResultSet results = vqe.execSelect();
                String docs_and_entities_count_xml = ResultSetFormatter.asXMLString(results);
                //System.out.println(docs_and_entities_count_xml);
                Document doc = null;
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                try {
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    doc = loadXMLFromString(docs_and_entities_count_xml);
                } catch (Exception e) {
                    System.out.println("Error in parsing Docs_and_Entities_Count_XML");
                }

                Element rootElement = doc.getDocumentElement();
                String elementcount = getString("literal", rootElement);
                elementcount = elementcount.replaceAll("\n", "").replaceAll(" ", "");

                counter = Integer.parseInt(elementcount);

                //Setting granularity as specified in timePeriod to take as granularity
                ParameterizedSparqlString Docs_and_Entities = new ParameterizedSparqlString("PREFIX dc: <http://purl.org/dc/terms/>\n" +
                        "PREFIX oae: <http://www.ics.forth.gr/isl/oae/core#>\n" +
                        "PREFIX dbc: <http://dbpedia.org/resource/Category:>\n" +
                        "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                        "PREFIX dbo: <http://dbpedia.org/ontology/>\n" +
                        "PREFIX dbr: <http://dbpedia.org/resource/>\n" +
                        "PREFIX schema:  <http://schema.org/>\n" +
                        "SELECT DISTINCT ?article ?mentionedEntityURI (count(?mentionedEntityURI) as ?mentionedEntityURIcount) (count(distinct ?entity) as ?entitycount)");

                ParameterizedSparqlString Docs_and_Entities_toAppend = new ParameterizedSparqlString("WHERE { \n" +
                        "?article dc:date ?date FILTER(?date >= xsd:date('" + fromDate + "') && \n" +
                        "?date <= xsd:date('" + toDate + "')). \n" +
                        "?article schema:mentions ?entity .\n" +
                        "?entity oae:hasMatchedURI <" + entityArray[u] + ">. \n" +
                        "?article schema:mentions ?mentionedEntity .\n" +
                        "?mentionedEntity oae:hasMatchedURI  ?mentionedEntityURI. \n" +
                        "}LIMIT 10000 OFFSET " + h);
                Docs_and_Entities.append(Docs_and_Entities_toAppend);

                connec = new VirtuosoConnector();
                graph = new VirtGraph(connec.getGraph(), connec.getHost(), connec.getUsername(), connec.getPwd());
                vqe = VirtuosoQueryExecutionFactory.create(Docs_and_Entities.toString(), graph);
                results = vqe.execSelect();
                String docs_and_entities_xml = ResultSetFormatter.asXMLString(results);
                //System.out.println(docs_and_entities_xml);
                doc = null;
                dbFactory = DocumentBuilderFactory.newInstance();
                try {
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    doc = loadXMLFromString(docs_and_entities_xml);
                } catch (Exception e) {
                    System.out.println("Error in parsing Docs_and_Entities_XML");
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

                        String entityFrequency = eElement.getElementsByTagName("binding").item(2).getTextContent();
                        entityFrequency = entityFrequency.replaceAll("\n", "").replaceAll(" ", "");
                        entity_Frequency = Double.parseDouble(entityFrequency);

                        String eoiFrequency = eElement.getElementsByTagName("binding").item(3).getTextContent();
                        eoiFrequency = eoiFrequency.replaceAll("\n", "").replaceAll(" ", "");
                        eoi_Frequency = Double.parseDouble(eoiFrequency);

                        entity_Frequency = entity_Frequency / eoi_Frequency;

                        //Store the results in the Map<Docs, Map<Entity, Frequency>> Structure 
                        if (Docs_Entities_Map.containsKey(docs)) {
                            // If the key (docs) is already present in the Docs_Entities_Map,
                            // we proceed to check the Entities_EntityFrequency_Map inside the Docs_Entities_Map
                            Entities_EntityFrequency_map = Docs_Entities_Map.get(docs);
                            if (Entities_EntityFrequency_map.containsKey(entity)) {
                                //If the key (entity) is already present in the Entity_EntityFrequency_Map,
                                //we get the entity and add the entityFrequency and 
                                //then store the frequency for the entity inside Entity_EntityFrequency_Map
                                Entities_EntityFrequency_map.put(entity, entity_Frequency);
                            } else {
                                //If the key (entity) hasn't been used yet,
                                //then we add entity and entityFrequency to Entities_EntityFrequency_Map
                                Entities_EntityFrequency_map.put(entity, entity_Frequency);
                            }
                        } else {
                            // If the key (docs) hasn't been used yet,
                            // then we add all three: docs, entity, entityFrequency
                            // first we add entity and frequency to Entities_EntityFrequency_map
                            // then we add docs and Entities_EntityFrequency_Map to Docs_Entities_Map
                            Entities_EntityFrequency_map = new LinkedHashMap<>();
                            Entities_EntityFrequency_map.put(entity, entity_Frequency);
                            Docs_Entities_Map.put(docs, Entities_EntityFrequency_map);
                        }

                        //Store the results in the Map<Entity, Map<Doc, EntityFrequency>> Structure
                        if (Entity_Docs_Map.containsKey(entity)) {
                            // If the key (entity) is already present in the Entity_Docs_Map,
                            // we check the Documents_EntityFrequency_Map inside the Entity_Docs_Map
                            Documents_EntityFrequency_map = Entity_Docs_Map.get(entity);
                            Documents_EntityFrequency_map.put(docs, entity_Frequency);
                        } else {
                            // If the key (entity) hasn't been used yet,
                            // then we add all three: entity, doc, entityFrequency
                            // first we add doc and frequency to Documents_EntityFrequency_map
                            // then we add entity and Documents_EntityFrequency_Map to Entity_Docs_Map
                            Documents_EntityFrequency_map = new LinkedHashMap<>();
                            Documents_EntityFrequency_map.put(docs, entity_Frequency);
                            Entity_Docs_Map.put(entity, Documents_EntityFrequency_map);
                        }

                    }
                }
                h += 10000;
            }
        }

        Docs_Entities_Map.entrySet().stream().map((entry) -> {
            String article = entry.getKey();
            //System.out.println(article);
            return entry;
        }).forEach((entry) -> {
            Map<String, Double> entity_entityFreq_map = entry.getValue();
            //System.out.println(entity_entityFreq_map.entrySet());
        });

        values[0] = Docs_Entities_Map;
        values[1] = Entity_Docs_Map;
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

}
