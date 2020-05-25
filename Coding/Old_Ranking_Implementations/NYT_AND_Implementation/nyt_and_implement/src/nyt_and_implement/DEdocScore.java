package nyt_and_implement;

import java.io.StringReader;
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

public class DEdocScore{
    public Map<String, String> return_dedoc_score(int totaldocs, String entity, String fromDate, String toDate, List<String> docsList, Map<String, String> docsByYearMap, Map<String, String> TimePeriodScoreMap, double rel_entity_score_sum){
        Map<String, String> DocDEScoreMap = new LinkedHashMap<String, String>();
        for(String docURI : docsList){  
          
           double mentioned_entity_score_sum = 0;
            
           //The important entities mentioned in the documents and their occurrences
           ParameterizedSparqlString mentionedEntities = new ParameterizedSparqlString("PREFIX oa: <http://www.w3.org/ns/oa#>\n" +
                                                                                       "PREFIX oae: <http://www.ics.forth.gr/isl/oae/core#>\n" +
                                                                                       "PREFIX dc: <http://purl.org/dc/terms/>\n" +
                                                                                       "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                                                                                       "SELECT DISTINCT  ?entityURI2 WHERE{\n" +
                                                                                       "<"+docURI+"> oae:mentions ?entity2 .\n" +
                                                                                       "?entity2 oae:hasMatchedURI  ?entityURI2\n" +
                                                                                       "FILTER(<"+entity+"> != ?entityURI2)\n" +
                                                                                       "}");
    
          VirtuosoConnector connec = new VirtuosoConnector();
          VirtGraph graph = new VirtGraph(connec.getGraph(),connec.getHost(),connec.getUsername(),connec.getPwd()); 
          VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(mentionedEntities.toString(), graph); 
          ResultSet results = vqe.execSelect();
          String mentionedEntitiesxml = ResultSetFormatter.asXMLString(results);
          System.out.println(mentionedEntitiesxml);
          Document doc = null;
          DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
          try{
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	        doc = loadXMLFromString(mentionedEntitiesxml);
          }catch(Exception e){
              System.out.println("Error in parsing Mentioned Entities in Document XML");
          }
          NodeList nList = doc.getElementsByTagName("result");
              for (int i = 0; i < nList.getLength(); i++){
                  Node nNode = nList.item(i);
                  if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                       Element eElement = (Element) nNode;
                       String mentionedEntity = eElement.getElementsByTagName("binding").item(0).getTextContent();
                       mentionedEntity = mentionedEntity.replaceAll("\n", "").replaceAll(" ", "");
                       
                       //Number of occurrences of the mentionedEntity in Documents of Interest |docs(e) ∩ D(Q)|
                       ParameterizedSparqlString mentionedEntity_count = new ParameterizedSparqlString("PREFIX dc: <http://purl.org/dc/terms/>\n" +
                                                                                         "PREFIX oae: <http://www.ics.forth.gr/isl/oae/core#>\n" +
                                                                                         "PREFIX dbc: <http://dbpedia.org/resource/Category:>\n" +
                                                                                         "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" + 
                                                                                         "PREFIX dbo: <http://dbpedia.org/ontology/>\n" +
                                                                                         "PREFIX dbr: <http://dbpedia.org/resource/>\n" +
                                                                                         "SELECT count(distinct (?article) as ?numOfArticles) WHERE {\n" +
                                                                                         "?article oae:mentions ?entity .\n" +
                                                                                         "?entity oae:hasMatchedURI  <"+mentionedEntity+"> .\n" + 
                                                                                         "?article oae:mentions ?entity2 .\n" +
                                                                                         "?entity2 oae:hasMatchedURI  <"+entity+"> .\n" +                          
                                                                                         "?article dc:date ?date FILTER(?date >= xsd:date('"+fromDate+"') &&\n" + 
                                                                                         "?date <= xsd:date('"+toDate+"')).\n" +
                                                                                         "}");
                      VirtuosoQueryExecution vqe2 = VirtuosoQueryExecutionFactory.create(mentionedEntity_count.toString(), graph); 
                      ResultSet results2 = vqe2.execSelect();
                      String mentionedEntities_total_xml = ResultSetFormatter.asXMLString(results2);
                      //System.out.println(mentionedEntities_total_xml);
                      Document doc2 = null;
                      DocumentBuilderFactory dbFactory2 = DocumentBuilderFactory.newInstance();
                      try{
                               DocumentBuilder dBuilder2 = dbFactory2.newDocumentBuilder();
	                       doc2 = loadXMLFromString(mentionedEntities_total_xml);
                      }catch(Exception e){
                               System.out.println("Error in parsing Mentioned Entity Count XML");
                      }            
                      Element rootElement = doc2.getDocumentElement();
                      String totaldox = getString("literal",rootElement);
                      totaldox = totaldox.replaceAll("\n", "").replaceAll(" ", "");
                      double mentionedEntity_Count = Double.parseDouble(totaldox);
                      
                      //Calculating log(|D(Q)| / |docs(e) ∩ D(Q)|)
                      double idf = Math.log(totaldocs / mentionedEntity_Count);
                      
                      ParameterizedSparqlString mentionedEntity_timep = new ParameterizedSparqlString("PREFIX dc: <http://purl.org/dc/terms/>\n" +
                                                                                                      "PREFIX oae: <http://www.ics.forth.gr/isl/oae/core#>\n" +
                                                                                                      "PREFIX dbc: <http://dbpedia.org/resource/Category:>\n" +
                                                                                                      "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" + 
                                                                                                      "PREFIX dbo: <http://dbpedia.org/ontology/>\n" +
                                                                                                      "PREFIX dbr: <http://dbpedia.org/resource/>\n" +
                                                                                                      "SELECT ?year (count(distinct ?article) as ?numOfArticles) WHERE {\n" +
                                                                                                      "?article oae:mentions ?entity .\n" +
                                                                                                      "?entity oae:hasMatchedURI  <"+mentionedEntity+"> .\n" + 
                                                                                                      "?article oae:mentions ?entity2 .\n" +
                                                                                                      "?entity2 oae:hasMatchedURI  <"+entity+"> .\n" +                          
                                                                                                      "?article dc:date ?date FILTER(?date >= xsd:date('"+fromDate+"') &&\n" + 
                                                                                                      "?date <= xsd:date('"+toDate+"')).\n" +
                                                                                                      "} GROUP BY (year(?date) AS ?year) order by ?year");
    
                     VirtuosoQueryExecution vqe3 = VirtuosoQueryExecutionFactory.create(mentionedEntity_timep.toString(), graph); 
                     ResultSet results3 = vqe3.execSelect();
                     String mentionedentity_docsbyyearxml = ResultSetFormatter.asXMLString(results3);
                     //System.out.println(mentionedentity_docsbyyearxml);
                     Document doc3 = null;
                     DocumentBuilderFactory dbFactory3 = DocumentBuilderFactory.newInstance();
                     try{
                               DocumentBuilder dBuilder3 = dbFactory3.newDocumentBuilder();
	                       doc3 = loadXMLFromString(mentionedentity_docsbyyearxml);
                     }catch(Exception e){
                               System.out.println("Error in parsing Sum Score Related Entities XML");
                     }
                     Map<String, String> mentionedentity_docsByYearMap = new LinkedHashMap<String, String>();
                     NodeList xList = doc3.getElementsByTagName("result");
                         for (int j = 0; j < xList.getLength(); j++){
                              Node xNode = xList.item(j);
                              if (xNode.getNodeType() == Node.ELEMENT_NODE) {
                                   Element xElement = (Element) xNode;
                                   String year = xElement.getElementsByTagName("binding").item(0).getTextContent();
                                   String numDocs = xElement.getElementsByTagName("binding").item(1).getTextContent();
                                   mentionedentity_docsByYearMap.put(year, numDocs);
                              }
                         }
                    
                    double sumSecondScore = 0;
         
                    //Calculating ScoreP(p)*(|docs(p) ∩ docs(e)|/ docs|p|) 
                    for(Map.Entry<String, String> entry: mentionedentity_docsByYearMap.entrySet()){
                        String year = entry.getKey();
             
                        String mentioned_entitydocs_byYear = entry.getValue();
                        String docs_published_in_year = docsByYearMap.get(year);
                        String timeperiodscore = TimePeriodScoreMap.get(year);
             
                        mentioned_entitydocs_byYear = mentioned_entitydocs_byYear.replaceAll("\n", "").replaceAll(" ", "");
                        docs_published_in_year = docs_published_in_year.replaceAll("\n", "").replaceAll(" ", "");
                        timeperiodscore = timeperiodscore.replaceAll("\n", "").replaceAll(" ", "");
             
                        double Mentioned_entitydocs_byYear = Double.parseDouble(mentioned_entitydocs_byYear);
                        double Docs_published_in_year = Double.parseDouble(docs_published_in_year);
                        double Timeperiodscore = Double.parseDouble(timeperiodscore);
             
                        double secondScore = (Timeperiodscore * Mentioned_entitydocs_byYear) / Docs_published_in_year;
                        sumSecondScore += secondScore;
                    }     
                   
                    //Calculating ScoreE(e)
                    double scoreE_mentionedentity = idf * sumSecondScore;
                    mentioned_entity_score_sum += scoreE_mentionedentity;
                 } 
              }
            //Calculating ScoreDE(d)
            double DEdocScore = mentioned_entity_score_sum / rel_entity_score_sum; 
            String DEDocScore = Double.toString(DEdocScore);
            DocDEScoreMap.put(docURI, DEDocScore);
        }
        return DocDEScoreMap;
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
}