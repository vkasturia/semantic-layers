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
package l3s.de.relatedness;

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
 */
public class RelatednessScore {
    public void relatedness_score(String[] entityArray, String fromDate, String toDate, String timePeriod) throws FileNotFoundException{
        RelatednessScore relatedScore = new RelatednessScore();
        Object[] values = relatedScore.entitiesreturn(entityArray, fromDate, toDate, timePeriod);
        Map<String, Double> article_idfscore_map = relatedScore.idfscore(entityArray);
        Map<String, Double> Article_RelatednessScore_Map = new LinkedHashMap<String, Double>();

        Map<String, List<String>> articleUrl_relEntities_Map = (Map<String, List<String>>) values[0];
        Map<String, Map<String, List<String>>> timePeriod_relEntities_Map = (Map<String, Map<String, List<String>>>) values[1];

        int totaldocs = articleUrl_relEntities_Map.size();
        System.out.println("Total number of Documents in POI: "+ totaldocs);
        
        double relatednessScore;

        for (Map.Entry<String, List<String>> entry : articleUrl_relEntities_Map.entrySet()) {

            String articleUrl = entry.getKey();
            System.out.println("ArticleURL " + articleUrl);
            List<String> relEntities = entry.getValue();
            relatednessScore = 0;

            //Getting the value of |ents(d)|
            int relEntitiesCount = relEntities.size() + entityArray.length;
            System.out.println("|ents(d)| =" +relEntitiesCount);

            for (String relentity : relEntities) {
                double entity_relatednessScore = 0;
                for (Map.Entry<String, Map<String, List<String>>> timeperiod : timePeriod_relEntities_Map.entrySet()) {
                    double entity_subrelatednessScore;
                    if (timeperiod.getValue().get(relentity) == null) {
                        entity_subrelatednessScore = 0;
                    } else {
                        entity_subrelatednessScore = timeperiod.getValue().get(relentity).size();
                    }
                    entity_relatednessScore += entity_subrelatednessScore;
                }
                double idfValue = article_idfscore_map.get(relentity);
                entity_relatednessScore *= idfValue;
                entity_relatednessScore /= totaldocs;
                relatednessScore += entity_relatednessScore;
            }
            relatednessScore /= relEntitiesCount;
            Article_RelatednessScore_Map.put(articleUrl, relatednessScore);
        }

        //Sort the Article_RelatednessScore_Map
        Map<String, Double> Ranked_Article_RelatednessScore_Map = relatedScore.sortHashMapByValues(Article_RelatednessScore_Map);

        //Printing each article and the timelinessScore in a csv file
        PrintWriter writer = new PrintWriter("./ranking_relatedness.csv");
        writer.println("Article; RelativenessScore");

        for (Map.Entry<String, Double> entry : Ranked_Article_RelatednessScore_Map.entrySet()) {
            String article = entry.getKey();
            System.out.println("Article " + article);
            Double docrelatednessScore = entry.getValue();
            System.out.println("RelatednessScore " + docrelatednessScore);
            writer.println(article + "; " + docrelatednessScore);
        }
        writer.close();
    }

    public Object[] entitiesreturn(String[] entityArray, String fromDate, String toDate, String timePeriod) {

        Object[] values = new Object[2];

        int counter = Integer.MAX_VALUE;
        int h = 0;

      //Declare data structures to store the query outputs 
      //We first create a list to store all the Articles. We would iterate through
        //this list later to get the Relatedness Score for each document d
        List<String> relEntitiesList = new ArrayList<>();
        Map<String, List<String>> articleUrl_relEntities_Map = new LinkedHashMap<>();

        //We then create a Map<timePeriod, Map<relEntity, List<articleUrl>>> Structure
        List<String> articleUrlList = new ArrayList<>();
        Map<String, List<String>> relEntities_articleUrlList_Map = new LinkedHashMap<>();
        Map<String, Map<String, List<String>>> timePeriod_relEntities_Map = new LinkedHashMap<>();

        while (counter > 10000) {

            //Set the granularity in the queries according to the time period granularity p, that is, week, month or year
            ParameterizedSparqlString EntitiesCounter = new ParameterizedSparqlString("PREFIX oa: <http://www.w3.org/ns/oa#>\n" +
                                                                                      "PREFIX oae: <http://www.ics.forth.gr/isl/oae/core#>\n" +
                                                                                      "PREFIX dc: <http://purl.org/dc/terms/>\n" +
                                                                                      "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                                                                                      "PREFIX schema:  <http://schema.org/>\n" +
                                                                                      "SELECT COUNT DISTINCT ?article  ?entityURI2 ("+timePeriod+"(?date) as ?"+timePeriod+")\n" +
                                                                                      "WHERE {\n" +
                                                                                      "?article dc:date ?date FILTER(?date >= xsd:date('"+fromDate+"') &&\n" +
                                                                                      "?date <= xsd:date('"+ toDate +"')).\n" +
                                                                                      "?article schema:mentions ?entity .\n" +
                                                                                      "?entity oae:hasMatchedURI <"+ entityArray[0] +">.\n" +  
                                                                                      "?article schema:mentions ?entity_rel .\n" +
                                                                                      "?entity_rel oae:hasMatchedURI  ?entityURI2. \n");
            
            for (int i = 1; i < entityArray.length; i++) {
                ParameterizedSparqlString EntitiesCounter_toAppend = new ParameterizedSparqlString("?article schema:mentions ?entity .\n" +
                                                                                                   "?entity oae:hasMatchedURI <"+ entityArray[i] +"> " +   
                                                                                                   "FILTER(<"+ entityArray[i] +"> != ?entityURI2). \n");
                EntitiesCounter.append(EntitiesCounter_toAppend);
            }

            ParameterizedSparqlString EntitiesCounter_endAppend = new ParameterizedSparqlString("}LIMIT 10000 OFFSET " + h);

            EntitiesCounter.append(EntitiesCounter_endAppend);


            VirtuosoConnector connec = new VirtuosoConnector();
            VirtGraph graph = new VirtGraph(connec.getGraph(), connec.getHost(), connec.getUsername(), connec.getPwd());

            VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(EntitiesCounter.toString(), graph);
            ResultSet results = vqe.execSelect();
            String EntitiesCountxml = ResultSetFormatter.asXMLString(results);
            System.out.println(EntitiesCountxml);
            Document doc = null;
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            try {
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                doc = loadXMLFromString(EntitiesCountxml);
            } catch (Exception e) {
                System.out.println("Error in parsing Entities Counter XML in Relatedness Score");
            }

            Element rootElement = doc.getDocumentElement();
            String elementcount = getString("literal", rootElement);
            elementcount = elementcount.replaceAll("\n", "").replaceAll(" ", "");

            counter = Integer.parseInt(elementcount);

            ParameterizedSparqlString Docs_and_Entities = new ParameterizedSparqlString("PREFIX oa: <http://www.w3.org/ns/oa#>\n" +
                                                                                      "PREFIX oae: <http://www.ics.forth.gr/isl/oae/core#>\n" +
                                                                                      "PREFIX dc: <http://purl.org/dc/terms/>\n" +
                                                                                      "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                                                                                      "PREFIX schema:  <http://schema.org/>\n" +
                                                                                      "SELECT DISTINCT ?article  ?entityURI2 ("+timePeriod+"(?date) as ?"+timePeriod+")\n" +
                                                                                      "WHERE {\n" +
                                                                                      "?article dc:date ?date FILTER(?date >= xsd:date('"+fromDate+"') &&\n" +
                                                                                      "?date <= xsd:date('"+ toDate +"')).\n" +
                                                                                      "?article schema:mentions ?entity .\n" +
                                                                                      "?entity oae:hasMatchedURI <"+ entityArray[0] +">.\n" +  
                                                                                      "?article schema:mentions ?entity_rel .\n" +
                                                                                      "?entity_rel oae:hasMatchedURI  ?entityURI2. \n");
            
            for (int i = 1; i < entityArray.length; i++) {
                ParameterizedSparqlString Docs_and_Entities_toAppend = new ParameterizedSparqlString("?article schema:mentions ?entity"+i+" .\n" +
                                                                                                   "?entity"+i+" oae:hasMatchedURI <"+ entityArray[i] +"> " +   
                                                                                                   "FILTER(<"+ entityArray[i] +"> != ?entityURI2). \n");
                Docs_and_Entities.append(Docs_and_Entities_toAppend);
            }

            ParameterizedSparqlString Docs_and_Entities_endAppend = new ParameterizedSparqlString("}LIMIT 10000 OFFSET " + h);

            Docs_and_Entities.append(Docs_and_Entities_endAppend);

            vqe = VirtuosoQueryExecutionFactory.create(Docs_and_Entities.toString(), graph);
            results = vqe.execSelect();
            String DocsEntitiesxml = ResultSetFormatter.asXMLString(results);
            //System.out.println(DocsEntitiesxml);
            doc = null;
            dbFactory = DocumentBuilderFactory.newInstance();
            try {
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                doc = loadXMLFromString(DocsEntitiesxml);
            } catch (Exception e) {
                System.out.println("Error in parsing Related Entities XML");
            }

            NodeList nList = doc.getElementsByTagName("result");
            for (int i = 0; i < nList.getLength(); i++) {
                Node nNode = nList.item(i);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;

                    String articleurl = eElement.getElementsByTagName("binding").item(0).getTextContent();
                    String relEntity = eElement.getElementsByTagName("binding").item(1).getTextContent();
                    String timePeriodP = eElement.getElementsByTagName("binding").item(2).getTextContent();

                    articleurl = articleurl.replaceAll("\n", "").replaceAll(" ", "");
                    relEntity = relEntity.replaceAll("\n", "").replaceAll(" ", "");
                    timePeriodP = timePeriodP.replaceAll("\n", "").replaceAll(" ", "");

                    //Store the Articles and Related Entities in the articleUrl_relEntities_Map 
                    if (articleUrl_relEntities_Map.containsKey(articleurl)) {
                        relEntitiesList = articleUrl_relEntities_Map.get(articleurl);
                        relEntitiesList.add(relEntity);
                        articleUrl_relEntities_Map.put(articleurl, relEntitiesList);
                    } else {
                        relEntitiesList = new ArrayList<>();
                        relEntitiesList.add(relEntity);
                        articleUrl_relEntities_Map.put(articleurl, relEntitiesList);
                    }
                    //Store the results in the Map<timePeriod, Map<relEntity, List<articleUrl>>> Structure 
                    if (timePeriod_relEntities_Map.containsKey(timePeriodP)) {
                         // If the key (timePeriod) is already present in the timePeriod_relEntities_Map,
                        // we proceed to check the relEntities_articleUrlList_Map inside the timePeriod_relEntities_Map
                        relEntities_articleUrlList_Map = timePeriod_relEntities_Map.get(timePeriodP);
                        if (relEntities_articleUrlList_Map.containsKey(relEntity)) {
                             //If the key (relEntity) is already present in the relEntities_articleUrlList_Map,
                            //we get the articleUrlList and add the articleUrl to it and 
                            //then store the new articleUrlList for the relEntity inside relEntities_articleUrlList_Map
                            articleUrlList = relEntities_articleUrlList_Map.get(relEntity);
                            articleUrlList.add(articleurl);
                            relEntities_articleUrlList_Map.put(relEntity, articleUrlList);
                        } else {
                             //If the key (relEntity) hasn't been used yet,
                            //then we add relEntity and articleUrl to relEntities_articleUrlList_Map
                            articleUrlList = new ArrayList<>();
                            articleUrlList.add(articleurl);
                            relEntities_articleUrlList_Map.put(relEntity, articleUrlList);
                        }
                    } else {
                         // If the key (timePeriod) hasn't been used yet,
                        // then we add all three: timePeriod, relEntity, articleUrl
                        // first we add relEntity and articleUrl to relEntities_articleUrlList_Map
                        // then we add timePeriod and relEntities_articleUrlList_Map to timePeriod_relEntities_Map
                        articleUrlList = new ArrayList<>();
                        articleUrlList.add(articleurl);
                        relEntities_articleUrlList_Map = new LinkedHashMap<>();
                        relEntities_articleUrlList_Map.put(relEntity, articleUrlList);
                        timePeriod_relEntities_Map.put(timePeriodP, relEntities_articleUrlList_Map);
                    }
                }
            }
            h += 10000;
        }
        values[0] = articleUrl_relEntities_Map;
        values[1] = timePeriod_relEntities_Map;
        return values;
    }

    public Map<String, Double> idfscore(String[] entityArray) {
        ParameterizedSparqlString idfscorequery = new ParameterizedSparqlString("PREFIX oa: <http://www.w3.org/ns/oa#>\n" +
                                                                                "PREFIX oae: <http://www.ics.forth.gr/isl/oae/core#>\n" +
                                                                                "PREFIX dc: <http://purl.org/dc/terms/>\n" +
                                                                                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                                                                                "PREFIX schema:  <http://schema.org/>\n" +
                                                                                "SELECT ?entityURI2 ?numOfArticles ?totalNumOfArticles\n" +
                                                                                "(1.0 - xsd:double(?numOfArticles)/xsd:double(?totalNumOfArticles) as ?idf)\n" +
                                                                                "WHERE {\n" +
                                                                                "{\n" +
                                                                                "SELECT DISTINCT (count(distinct ?article) as ?totalNumOfArticles)\n" +
                                                                                "WHERE {\n" +
                                                                                "?article schema:mentions ?entity .\n" +
                                                                                "?entity oae:hasMatchedURI  <" + entityArray[0] + ">. \n");
        
        for (int i = 1; i < entityArray.length; i++) {
          ParameterizedSparqlString idfscorequery_toAppend = new ParameterizedSparqlString("?article schema:mentions ?entity"+i+" .\n" +
                                                                                           "?entity"+i+" oae:hasMatchedURI <"+ entityArray[i] +">. \n ");
          idfscorequery.append(idfscorequery_toAppend);
        }
                
        ParameterizedSparqlString idfscorequery_toAppend2 = new ParameterizedSparqlString("}\n" +
                                                                                          "}\n" +
                                                                                          "{\n" +
                                                                                          "SELECT ?entityURI2 (COUNT(DISTINCT ?article) as ?numOfArticles)\n" +
                                                                                          "WHERE {\n" +
                                                                                          "?article schema:mentions ?entity .\n" +
                                                                                          "?entity oae:hasMatchedURI <" + entityArray[0] + "> .\n");
        
        idfscorequery.append(idfscorequery_toAppend2);
        
        for (int i = 1; i < entityArray.length; i++) {
          ParameterizedSparqlString idfscorequery_toAppend3 = new ParameterizedSparqlString("?article schema:mentions ?entity"+i+" .\n" +
                                                                                           "?entity"+i+" oae:hasMatchedURI <"+ entityArray[i] +">. \n ");
          idfscorequery.append(idfscorequery_toAppend3);
        }        
        
        ParameterizedSparqlString idfscorequery_endAppend = new ParameterizedSparqlString("?article schema:mentions ?entity_rel .\n" +
                                                                                          "?entity_rel oae:hasMatchedURI ?entityURI2 .\n" +
                                                                                          "} group by ?entityURI2 order by DESC(?numOfArticles)\n" +
                                                                                          "}\n" +
                                                                                          "}");
        idfscorequery.append(idfscorequery_endAppend);

        VirtuosoConnector connec = new VirtuosoConnector();
        VirtGraph graph = new VirtGraph(connec.getGraph(), connec.getHost(), connec.getUsername(), connec.getPwd());

        VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(idfscorequery.toString(), graph);
        ResultSet results = vqe.execSelect();
        String IdfXml = ResultSetFormatter.asXMLString(results);
        System.out.println(IdfXml);
        Document doc = null;
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = loadXMLFromString(IdfXml);
        } catch (Exception e) {
            System.out.println("Error in parsing IDF Score XML");
        }

        Map<String, Double> relEntity_idfScore_Map = new LinkedHashMap<>();

        NodeList nList = doc.getElementsByTagName("result");
        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;

                String relEntity = eElement.getElementsByTagName("binding").item(0).getTextContent();
                String idf = eElement.getElementsByTagName("binding").item(3).getTextContent();

                relEntity = relEntity.replaceAll("\n", "").replaceAll(" ", "");
                idf = idf.replaceAll("\n", "").replaceAll(" ", "");
                double idfScore = Double.parseDouble(idf);

                relEntity_idfScore_Map.put(relEntity, idfScore);
            }
        }
        return relEntity_idfScore_Map;
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
