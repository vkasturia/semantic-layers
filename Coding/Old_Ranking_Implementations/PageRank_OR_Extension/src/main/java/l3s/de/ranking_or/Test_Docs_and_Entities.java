/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package l3s.de.ranking_or;

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
 * @author Vaibhav Kasturia
 * 
 * Class to get documents, the entities mentioned in them and the count of the entities for the given query
 */

public class Test_Docs_and_Entities {

    public Map<String, Map<String, Double>> docs_entities_return(String[] entityArray, String fromDate, String toDate) {
        
        //Data structure to store the documents, their extracted entities and the entity frequencies 
        Map<String, Double> Entities_EntityFrequency_map = new LinkedHashMap<>(); 
        Map<String, Map<String, Double>> Docs_Entities_Map = new LinkedHashMap<>();
        
        //Variables to store intermediate output obtained from the SPARQL Query
        double entity_Frequency;
        double eoi_Frequency;
        
        for(int u = 0; u < entityArray.length; u++){
        //Setting granularity as specified in timePeriod to take as granularity
        ParameterizedSparqlString Docs_and_Entities = new ParameterizedSparqlString("PREFIX dc: <http://purl.org/dc/terms/>\n" +
                                                                                    "PREFIX oae: <http://www.ics.forth.gr/isl/oae/core#>\n" +
                                                                                    "PREFIX dbc: <http://dbpedia.org/resource/Category:>\n" +
                                                                                    "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                                                                                    "PREFIX dbo: <http://dbpedia.org/ontology/>\n" +
                                                                                    "PREFIX dbr: <http://dbpedia.org/resource/>\n" +
                                                                                    "PREFIX schema:  <http://schema.org/>\n" +
                                                                                    "SELECT DISTINCT ?article ?mentionedEntityURI (count(?mentionedEntityURI) as ?mentionedEntityURIcount) (count(distinct ?entity) as ?entitycount)");
                      
        ParameterizedSparqlString Docs_and_Entities_toAppend3 = new ParameterizedSparqlString("WHERE { \n" +
                                                                                                    "?article dc:date ?date FILTER(?date >= xsd:date('" + fromDate + "') && \n" +
                                                                                                    "?date <= xsd:date('" + toDate + "')). \n" +
                                                                                                    "?article schema:mentions ?entity .\n" +
                                                                                                    "?entity oae:hasMatchedURI <"+ entityArray[u] +">. \n" +
                                                                                                    "?article schema:mentions ?mentionedEntity .\n" +
                                                                                                    "?mentionedEntity oae:hasMatchedURI  ?mentionedEntityURI. \n" +
                                                                                                    "} \n"
                                                                                                   );
        Docs_and_Entities.append(Docs_and_Entities_toAppend3);
        
        VirtuosoConnector connec = new VirtuosoConnector();
        VirtGraph graph = new VirtGraph(connec.getGraph(), connec.getHost(), connec.getUsername(), connec.getPwd());
        VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(Docs_and_Entities.toString(), graph);
        ResultSet results = vqe.execSelect();
        String docs_and_entities_xml = ResultSetFormatter.asXMLString(results);
        //System.out.println(docs_and_entities_xml);
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
                    }
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


