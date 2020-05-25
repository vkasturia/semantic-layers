/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package relativeness;


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
import ranking_algo_category.VirtuosoConnector;
import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

/**
 *
 * @author Vaibhav Kasturia <kasturia at l3s.de>
 * 
 * Class to calculate the Relativeness Score
 * 
 */

public class RelativenessScore {
    public Map<String, Double> relativeness_score(String[] entityArray, String fromDate, String toDate, int counter, Map<String, Map<String, List<Integer>>> document_queryEntity_positionList_map, Map<String, Map<String, List<Integer>>> document_relatedEntity_positionList_map) throws FileNotFoundException{
        RelativenessScore relScore = new RelativenessScore();
        Map<String, Double> document_relativenessScore_map = relScore.relativenessScoreMapReturn(entityArray, fromDate, toDate, document_queryEntity_positionList_map, document_relatedEntity_positionList_map);
            
        Map<String, Double> Ranked_Article_RelativenessScore_Map = relScore.sortHashMapByValues(document_relativenessScore_map);
        
        //Printing each article and the timelinessScore in a csv file
        PrintWriter writer = new PrintWriter("./results/"+ counter +"/ranking_relativeness"+counter+".csv");
	writer.println("Article; RelativenessScore");
        
        Ranked_Article_RelativenessScore_Map.entrySet().stream().forEach((entry) -> {
            String article = entry.getKey();
            //System.out.println("Article "+ article);
            Double relativenessScore = entry.getValue();
            //System.out.println("RelativenessScore "+ relativenessScore);
            writer.println(article + "; "+ relativenessScore);
        });
        
        writer.close();    
        
        return Ranked_Article_RelativenessScore_Map;
    } 
    
    public Map<String, Double> relativenessScoreMapReturn(String[] entityArray, String fromDate, String toDate, Map<String, Map<String, List<Integer>>> document_queryEntity_positionList_map, Map<String, Map<String, List<Integer>>> document_relatedEntity_positionList_map){

        RelativenessScore relScore = new RelativenessScore();
        
        //Map structure to store the Document and its Relativeness Score 
        Map<String, Double> document_relativenessScore_map = new LinkedHashMap<>();

        double relativenessScoreSum = 0;

        //Get the maximum position that any entity occurs in the article
        Map<String, Double> article_maxPosition_map = relScore.maxPosition(entityArray, fromDate, toDate);
        
        for (Map.Entry<String, Map<String, List<Integer>>> outerEntry : document_queryEntity_positionList_map.entrySet()) {
            String document = outerEntry.getKey();
            double relativenessScore = 0;
            double queryEntitiesCountPositionSum = 0;
            double relatedEntitiesCountPositionSum = 0;
            for (Map.Entry<String, List<Integer>> innerEntry : outerEntry.getValue().entrySet()) {
                double queryEntityPositionSum = 0;
                //double queryEntityCount = innerEntry.getValue().size();
                for (int position : innerEntry.getValue()) {
                    queryEntityPositionSum += ((article_maxPosition_map.get(document) - position)/article_maxPosition_map.get(document)) ;
                }
                queryEntitiesCountPositionSum += queryEntityPositionSum;
            }
            
            int numOfQueryEntitiesInDoc = outerEntry.getValue().size();
            queryEntitiesCountPositionSum *= ((double) numOfQueryEntitiesInDoc / entityArray.length);

            Map<String, List<Integer>> relatedEntity_positionList_map = document_relatedEntity_positionList_map.get(document);
            for (Map.Entry<String, List<Integer>> entry : relatedEntity_positionList_map.entrySet()) {
                double relatedEntityPositionSum = 0;
                //double relatedEntityCount = entry.getValue().size();
                for (int position : entry.getValue()) {
                    relatedEntityPositionSum += ((article_maxPosition_map.get(document) - position)/article_maxPosition_map.get(document)) ;
                }
                relatedEntitiesCountPositionSum += relatedEntityPositionSum;
            }
            relativenessScore = (queryEntitiesCountPositionSum / (queryEntitiesCountPositionSum + relatedEntitiesCountPositionSum));
            relativenessScoreSum += relativenessScore;
        }

        for (Map.Entry<String, Map<String, List<Integer>>> outerEntry : document_queryEntity_positionList_map.entrySet()) {
            String document = outerEntry.getKey();
            double relativenessScore = 0;
            double queryEntitiesCountPositionSum = 0;
            double relatedEntitiesCountPositionSum = 0;
            double rateFactor = 1e-5;
            for (Map.Entry<String, List<Integer>> innerEntry : outerEntry.getValue().entrySet()) {
                double queryEntityPositionSum = 0;
                //double queryEntityCount = innerEntry.getValue().size();
                for (int position : innerEntry.getValue()) {
                    queryEntityPositionSum += ((article_maxPosition_map.get(document) - position)/article_maxPosition_map.get(document)) ;
                }
                queryEntitiesCountPositionSum += queryEntityPositionSum;
            }
            
            int numOfQueryEntitiesInDoc = outerEntry.getValue().size();
            queryEntitiesCountPositionSum *= ((double) numOfQueryEntitiesInDoc / entityArray.length);

            Map<String, List<Integer>> relatedEntity_positionList_map = document_relatedEntity_positionList_map.get(document);
            for (Map.Entry<String, List<Integer>> entry : relatedEntity_positionList_map.entrySet()) {
                double relatedEntityPositionSum = 0;
                //double relatedEntityCount = entry.getValue().size();
                for (int position : entry.getValue()) {
                    relatedEntityPositionSum += ((article_maxPosition_map.get(document) - position)/article_maxPosition_map.get(document)) ;
                }
                relatedEntitiesCountPositionSum += relatedEntityPositionSum;
            }
            relativenessScore = (queryEntitiesCountPositionSum / (queryEntitiesCountPositionSum + relatedEntitiesCountPositionSum));
            relativenessScore /= relativenessScoreSum;
            document_relativenessScore_map.put(document, relativenessScore);
        }

         // <editor-fold defaultstate="collapsed" desc="stuff to be collapsed">
         /*
         //Variable containing sum of all the Relativeness Scores
         double relativenessScoreSum = 0;
         
         //Declare data structure to store the query outputs
         int n = entityArray.length;
         double[] entityCount = new double[n+1];
         String[] entityCountArray = new String[n+1];
         List<Double> entityCountList = new ArrayList();
         Map <String, List<Double>> articleUrl_entityCount_map = new LinkedHashMap(); 
         
         ParameterizedSparqlString relativenessScoreQuery = new ParameterizedSparqlString("PREFIX oa: <http://www.w3.org/ns/oa#> \n" +
         "PREFIX oae: <http://www.ics.forth.gr/isl/oae/core#> \n" +
         "PREFIX dc: <http://purl.org/dc/terms/> \n" +
         "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n" +
         "PREFIX schema:  <http://schema.org/> \n" +
         "SELECT ?article (count(?entityURI2) as ?relentitycount) (count(distinct ?entity) as ?entitycount)"
         );
         
         for(int i = 1; i< entityArray.length; i++){
         ParameterizedSparqlString relativenessScoreQuery_toAppend = new ParameterizedSparqlString(" count( distinct ?entity"+i+") as ?entitycount"+i+" "); 
         relativenessScoreQuery.append(relativenessScoreQuery_toAppend);
         }
         
         ParameterizedSparqlString relativenessScoreQuery_toAppend2 = new ParameterizedSparqlString("WHERE { \n" +
         "{ \n" +
         "?article dc:date ?date FILTER(?date >= xsd:date('" + fromDate + "') && \n" +
         "?date <= xsd:date('" + toDate + "')). \n" +
         "?article schema:mentions ?entity .\n" +
         "?entity oae:hasMatchedURI <"+ entityArray[0] +">. \n" +
         "?article schema:mentions ?entity_rel .\n" +
         "?entity_rel oae:hasMatchedURI  ?entityURI2. \n" +
         "} \n"
         );
         relativenessScoreQuery.append(relativenessScoreQuery_toAppend2);
         
         for(int i = 1; i< entityArray.length; i++){
         ParameterizedSparqlString relativenessScoreQuery_toAppend3 = new ParameterizedSparqlString("UNION { \n" +
         "?article dc:date ?date FILTER(?date >= xsd:date('" + fromDate + "') && \n" +
         "?date <= xsd:date('" + toDate + "')). \n" +
         "?article schema:mentions ?entity"+i+" .\n" +
         "?entity"+i+" oae:hasMatchedURI <"+ entityArray[i] +">. \n" +
         "?article schema:mentions ?entity_rel .\n" +
         "?entity_rel oae:hasMatchedURI  ?entityURI2. \n" +
         "} \n"); 
         relativenessScoreQuery.append(relativenessScoreQuery_toAppend3);
         }
         
         ParameterizedSparqlString relativenessScoreQuery_toAppend4 = new ParameterizedSparqlString("}");
          
         relativenessScoreQuery.append(relativenessScoreQuery_toAppend4);
         
         VirtuosoConnector connec = new VirtuosoConnector();
         VirtGraph graph = new VirtGraph(connec.getGraph(),connec.getHost(),connec.getUsername(),connec.getPwd());
         
         VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(relativenessScoreQuery.toString(), graph); 
         ResultSet results = vqe.execSelect();
         String relativenessScoreQueryXml = ResultSetFormatter.asXMLString(results);
         //System.out.println(relativenessScoreQueryXml);
         Document doc = null;
         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
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
                      
         entityCountList = new ArrayList();
         String articleurl = eElement.getElementsByTagName("binding").item(0).getTextContent();
         articleurl = articleurl.replaceAll("\n", "").replaceAll(" ", "");
         for(int w = 0; w < entityArray.length+ 1; w++ ){
         entityCountArray[w] = eElement.getElementsByTagName("binding").item(w+1).getTextContent();
         entityCountArray[w] = entityCountArray[w].replaceAll("\n", "").replaceAll(" ", "");
         entityCount[w] = Double.parseDouble(entityCountArray[w]);
         if(w > 1){
         entityCountList.add(entityCount[w]-1);
         }
         else{
         entityCountList.add(entityCount[w]);
         }
         }
                      
         articleUrl_entityCount_map.put(articleurl, entityCountList);
         }
         }
             
         for(Map.Entry<String, List<Double>> article : articleUrl_entityCount_map.entrySet()){
         double eoiSum = 0;
         double eoiCount = 0;
         double relativenessScore = 0;
         for(int w = 1; w < entityArray.length + 1; w++){
         eoiSum += article.getValue().get(w);
         if(article.getValue().get(w) > 0)
         eoiCount +=1;
         }
         relativenessScore =  ((eoiSum / (article.getValue().get(0) / eoiSum))*(eoiCount/entityArray.length));
         relativenessScoreSum += relativenessScore;
         }
       
         for(Map.Entry<String, List<Double>> article : articleUrl_entityCount_map.entrySet()){
         double eoiSum = 0;
         double eoiCount = 0;
         double relativenessScore = 0;
         for(int w = 1; w < entityArray.length + 1; w++){
         eoiSum += article.getValue().get(w);
         if(article.getValue().get(w) > 0)
         eoiCount +=1;
         }
         relativenessScore =  ((eoiSum / (article.getValue().get(0) / eoiSum))*(eoiCount/entityArray.length));
         relativenessScore = relativenessScore / relativenessScoreSum;
         document_relativenessScore_map.put(article.getKey(), relativenessScore);
         } 
         */
       // </editor-fold>  
        return document_relativenessScore_map;
    }
    
    public Map<String, Double> maxPosition (String[] entityArray, String fromDate, String toDate){
        Map<String, Double> article_maxPosition_map = new LinkedHashMap<>();

        for (int j = 0; j < entityArray.length; j++) {
            ParameterizedSparqlString MaxPositionQuery = new ParameterizedSparqlString("PREFIX oa: <http://www.w3.org/ns/oa#>\n" +
                                                                                       "PREFIX oae: <http://www.ics.forth.gr/isl/oae/core#>\n" +
                                                                                       "PREFIX dc: <http://purl.org/dc/terms/>\n" +
                                                                                       "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                                                                                       "PREFIX schema:  <http://schema.org/>\n" +
                                                                                       "SELECT DISTINCT ?article  (MAX(?position) AS ?max_position) \n" +
                                                                                       "WHERE {\n" +
                                                                                       "?article dc:date ?date FILTER(?date >= xsd:date('" + fromDate + "') &&\n" +
                                                                                       "?date <= xsd:date('" + toDate + "')).\n" +
                                                                                       "BIND (CONCAT(month(?date), \"-\", year(?date)) AS ?month). \n" +
                                                                                       "BIND (CONCAT(day(?date), \"-\",month(?date), \"-\", year(?date)) AS ?day). \n" +
                                                                                       "BIND (CONCAT(year(?date)) AS ?year). \n" +
                                                                                       "?article schema:mentions ?entity .\n" +
                                                                                       "?entity oae:hasMatchedURI <" + entityArray[j] + ">.\n" +
                                                                                       "?article schema:mentions ?entity_rel .\n" +
                                                                                       "?entity_rel oae:hasMatchedURI  ?entityURI2. \n" +
                                                                                       "?entity_rel oae:position ?position.}");

            VirtuosoConnector connec = new VirtuosoConnector();
            VirtGraph graph = new VirtGraph(connec.getGraph(), connec.getHost(), connec.getUsername(), connec.getPwd());

            VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(MaxPositionQuery.toString(), graph);
            ResultSet results = vqe.execSelect();
            String MaxPositionQueryXml = ResultSetFormatter.asXMLString(results);
            //System.out.println(MaxPositionQueryXml);
            Document doc = null;
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            try {
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                doc = loadXMLFromString(MaxPositionQueryXml);
            } catch (Exception e) {
                System.out.println("Error in parsing MaxPositionQuery XML in Relatedness Score");
            }

            NodeList nList = doc.getElementsByTagName("result");
            for (int i = 0; i < nList.getLength(); i++) {
                Node nNode = nList.item(i);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;

                    String articleurl = eElement.getElementsByTagName("binding").item(0).getTextContent();
                    String maxPosition = eElement.getElementsByTagName("binding").item(1).getTextContent();

                    articleurl = articleurl.replaceAll("\n", "").replaceAll(" ", "");
                    maxPosition = maxPosition.replaceAll("\n", "").replaceAll(" ", "");
                    double MaxPosition = Double.valueOf(maxPosition);

                    if(!article_maxPosition_map.containsKey(articleurl))
                        article_maxPosition_map.put(articleurl, MaxPosition);
                }
            }
        }
        return article_maxPosition_map;
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
