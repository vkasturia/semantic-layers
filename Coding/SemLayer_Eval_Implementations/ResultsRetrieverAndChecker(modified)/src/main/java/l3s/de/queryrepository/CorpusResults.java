/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package resultsretrieverandcheckerlocal;

/**
 *
 * @author vaibhav
 */

import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
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

public class CorpusResults {
    //Modify Parameters of Query and SPARQL Query as desired
    public int totalCorpusResults/*(String entity,*/ (String fromDate, String toDate){
         ParameterizedSparqlString totalDocs = new ParameterizedSparqlString("PREFIX oa: <http://www.w3.org/ns/oa#>\n" +
                                                                             "PREFIX oae: <http://www.ics.forth.gr/isl/oae/core#>\n" +
                                                                             "PREFIX dc: <http://purl.org/dc/terms/>\n" +
                                                                             "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                                                                             "PREFIX dbc: <http://dbpedia.org/resource/Category:>\n" +
                                                                             "SELECT count(?article) as ?count\n" + 
                                                                             "WHERE {\n" +
                                                                             "SERVICE <http://dbpedia.org/sparql> {\n" +
                                                                             "?nylawyer dc:subject dbc:African-American_film_producers }\n" +
                                                                             "?article dc:date ?date FILTER(?date >= xsd:date('"+fromDate+"') &&\n" + 
                                                                             "?date <= xsd:date('"+toDate+"')).\n" +
                                                                             "?article oae:mentions ?entity .\n" +
                                                                             "?entity oae:hasMatchedURI ?nylawyer .\n" + 
                                                                             "}");
    
         VirtuosoConnector connec = new VirtuosoConnector();
         VirtGraph graph = new VirtGraph(connec.getHost(),connec.getUsername(),connec.getPwd()); 
         System.out.println(totalDocs.toString());
         VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(totalDocs.toString(), graph); 
         ResultSet results = vqe.execSelect();
         String totaldocsxml = ResultSetFormatter.asXMLString(results);
         System.out.println(totaldocsxml);
         Document doc = null;
         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
         try{
               DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	       doc = loadXMLFromString(totaldocsxml);
         }catch(Exception e){
             System.out.println("Error in parsing TotalDocs XML");
         }
         Element rootElement = doc.getDocumentElement();
         String totaldox = getString("literal",rootElement);
         return Integer.parseInt(totaldox);
    }
    public List<String> titlesOfCorpusResults(String fromDate, String toDate){
         ParameterizedSparqlString corpusResultTitles = new ParameterizedSparqlString("PREFIX oa: <http://www.w3.org/ns/oa#>\n" +
                                                                                      "PREFIX oae: <http://www.ics.forth.gr/isl/oae/core#>\n" +
                                                                                      "PREFIX dc: <http://purl.org/dc/terms/>\n" +
                                                                                      "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                                                                                      "PREFIX dbc: <http://dbpedia.org/resource/Category:>\n" +
                                                                                      "SELECT ?title\n" + 
                                                                                      "WHERE {\n" +
                                                                                      "SERVICE <http://dbpedia.org/sparql> {\n" +
                                                                                      "?nylawyer dc:subject dbc:African-American_film_producers }\n" +
                                                                                      "?article dc:date ?date FILTER(?date >= xsd:date('"+fromDate+"') &&\n" + 
                                                                                      "?date <= xsd:date('"+toDate+"')).\n" +
                                                                                      "?article oae:mentions ?entity .\n" +
                                                                                      "?entity oae:hasMatchedURI ?nylawyer .\n" +
                                                                                      "?article dc:title ?title" +
                                                                                      "}");
    
         VirtuosoConnector connec = new VirtuosoConnector();
         VirtGraph graph = new VirtGraph(connec.getHost(),connec.getUsername(),connec.getPwd()); 
         VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(corpusResultTitles.toString(), graph); 
         ResultSet results = vqe.execSelect();
         String corpustitlesxml = ResultSetFormatter.asXMLString(results);
         System.out.println(corpustitlesxml);
         Document doc = null;
         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
         try{
               DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
               doc = loadXMLFromString(corpustitlesxml);
               //System.out.println(reldocsxml);
         }catch(Exception e){
             System.out.println("Error in parsing Corpus Titles XML");
         }
         List<String> CorpusTitlesList = new LinkedList<String>();
         NodeList nList = doc.getElementsByTagName("result");
             for (int i = 0; i < nList.getLength(); i++){
                 Node nNode = nList.item(i);
                 if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                      Element eElement = (Element) nNode;
                      String titleString = eElement.getElementsByTagName("binding").item(0).getTextContent();
                      String finalTitle = titleString.toLowerCase().replace("nytimes.com", "").replace("the new york times", "").replace("nytimes", "").replace("...", "").replace("@en", "");
                      finalTitle = finalTitle.replace("'", "&apos;").trim();
                      if (finalTitle.endsWith("-")) {
                          finalTitle = finalTitle.substring(0, finalTitle.length() - 1).trim();
                      }

                      int i1 = finalTitle.lastIndexOf("- ");
                      int i2 = finalTitle.lastIndexOf("; ");
                      if (i1 != -1 || i2 != -1) {
                          if (i1 > i2) {
                              finalTitle = finalTitle.substring(i1 + 1).trim();
                          } else {
                              finalTitle = finalTitle.substring(i2 + 1).trim();
                          }
                      }
                      finalTitle = finalTitle.replace("&#39;", "&apos;").replace("?", "\\\\?");
                      CorpusTitlesList.add(finalTitle);
                 }
             }
        return CorpusTitlesList;     
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
