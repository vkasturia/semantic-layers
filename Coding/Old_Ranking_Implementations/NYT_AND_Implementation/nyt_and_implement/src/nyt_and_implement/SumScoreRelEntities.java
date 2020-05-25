
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

public class SumScoreRelEntities{
    public double return_rel_entity_score_sum(int totaldocs, String entity, String fromDate, String toDate, Map<String, String> relEntitiesMap, Map<String, String> docsByYearMap, Map<String, String> TimePeriodScoreMap){
        double rel_entity_score_sum = 0;
        for(Map.Entry<String, String> entry: relEntitiesMap.entrySet()){  
         
         //The important related entity: e' 
         String rel_entity = entry.getKey();
         rel_entity = rel_entity.replaceAll("\n", "").replaceAll(" ", "");
         //System.out.println(rel_entity);
         
         //Number of occurences of the important related entity: |docs(e') ∩ D(Q)|
         String total_rel_entity_occurrences = entry.getValue();
         total_rel_entity_occurrences = total_rel_entity_occurrences.replaceAll("\n", "").replaceAll(" ", "");
         //System.out.println(total_rel_entity_occurrences);
         double Related_Entity_Occurrences_Total = Double.parseDouble(total_rel_entity_occurrences);
         
         //Calculating number of occurrences of related entities in documents according to year: |docs(p) ∩ docs(e')| 
         ParameterizedSparqlString relatedEntities_timep = new ParameterizedSparqlString("PREFIX dc: <http://purl.org/dc/terms/>\n" +
                                                                                         "PREFIX oae: <http://www.ics.forth.gr/isl/oae/core#>\n" +
                                                                                         "PREFIX dbc: <http://dbpedia.org/resource/Category:>\n" +
                                                                                         "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" + 
                                                                                         "PREFIX dbo: <http://dbpedia.org/ontology/>\n" +
                                                                                         "PREFIX dbr: <http://dbpedia.org/resource/>\n" +
                                                                                         "SELECT ?year (count(distinct ?article) as ?numOfArticles) WHERE {\n" +
                                                                                         "?article oae:mentions ?entity .\n" +
                                                                                         "?entity oae:hasMatchedURI  <"+rel_entity+"> .\n" + 
                                                                                         "?article oae:mentions ?entity2 .\n" +
                                                                                         "?entity2 oae:hasMatchedURI  <"+entity+"> .\n" +                          
                                                                                         "?article dc:date ?date FILTER(?date >= xsd:date('"+fromDate+"') &&\n" + 
                                                                                         "?date <= xsd:date('"+toDate+"')).\n" +
                                                                                         "} GROUP BY (year(?date) AS ?year) order by ?year");
    
         VirtuosoConnector connec = new VirtuosoConnector();
         VirtGraph graph = new VirtGraph(connec.getGraph(),connec.getHost(),connec.getUsername(),connec.getPwd()); 
         VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(relatedEntities_timep.toString(), graph); 
         ResultSet results = vqe.execSelect();
         String relentity_totaldocsxml = ResultSetFormatter.asXMLString(results);
         //System.out.println(relentity_totaldocsxml);
         Document doc = null;
         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
         try{
               DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	       doc = loadXMLFromString(relentity_totaldocsxml);
         }catch(Exception e){
             System.out.println("Error in parsing Sum Score Related Entities XML");
         }
         Map<String, String> relentity_docsByYearMap = new LinkedHashMap<String, String>();
         NodeList nList = doc.getElementsByTagName("result");
             for (int i = 0; i < nList.getLength(); i++){
                 Node nNode = nList.item(i);
                 if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                      Element eElement = (Element) nNode;
                      String year = eElement.getElementsByTagName("binding").item(0).getTextContent();
                      String numDocs = eElement.getElementsByTagName("binding").item(1).getTextContent();
                      relentity_docsByYearMap.put(year, numDocs);
                 }
             }
         
         //Calculating log(|D(Q)| / |docs(e') ∩ D(Q)|)
         double idf = Math.log(totaldocs /Related_Entity_Occurrences_Total);
         
         double sumSecondScore = 0;
         
         //Calculating ScoreP(p)*(|docs(p) ∩ docs(e')|/ docs|p|) 
         for(Map.Entry<String, String> entry2: relentity_docsByYearMap.entrySet()){
             String year = entry2.getKey();
             
             String rel_entitydocs_byYear = entry2.getValue();
             String docs_published_in_year = docsByYearMap.get(year);
             String timeperiodscore = TimePeriodScoreMap.get(year);
             
             rel_entitydocs_byYear = rel_entitydocs_byYear.replaceAll("\n", "").replaceAll(" ", "");
             docs_published_in_year = docs_published_in_year.replaceAll("\n", "").replaceAll(" ", "");
             timeperiodscore = timeperiodscore.replaceAll("\n", "").replaceAll(" ", "");
             
             double Rel_entitydocs_byYear = Double.parseDouble(rel_entitydocs_byYear);
             double Docs_published_in_year = Double.parseDouble(docs_published_in_year);
             double Timeperiodscore = Double.parseDouble(timeperiodscore);
             
             double secondScore = (Timeperiodscore * Rel_entitydocs_byYear) / Docs_published_in_year;
             sumSecondScore += secondScore;
         }
         
         //Calculating ScoreE(e')
         double scoreE_relentity = idf * sumSecondScore;
         rel_entity_score_sum += scoreE_relentity;
     }
     return rel_entity_score_sum; 
    }
    protected static Document loadXMLFromString(String xml) throws Exception{
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
    }
}