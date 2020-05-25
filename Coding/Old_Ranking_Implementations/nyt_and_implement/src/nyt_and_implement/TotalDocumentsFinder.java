/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nyt_and_implement;

import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

public class TotalDocumentsFinder{
    public int totaldocsreturn(String entity, String fromDate, String toDate){
         ParameterizedSparqlString totalDocs = new ParameterizedSparqlString("PREFIX oa: <http://www.w3.org/ns/oa#>\n" +
                                                                             "PREFIX oae: <http://www.ics.forth.gr/isl/oae/core#>\n" +
                                                                             "PREFIX dc: <http://purl.org/dc/terms/>\n" +
                                                                             "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                                                                             "SELECT count(?article) as ?count\n" + 
                                                                             "WHERE {\n" +
                                                                             "?article dc:date ?date FILTER(?date >= xsd:date('"+fromDate+"') &&\n" + 
                                                                             "?date <= xsd:date('"+toDate+"')).\n" +
                                                                             "?article oae:mentions ?entity .\n" +
                                                                             "?entity oae:hasMatchedURI <"+entity+"> .\n" + 
                                                                             "}");
    
         VirtuosoConnector connec = new VirtuosoConnector();
         VirtGraph graph = new VirtGraph(connec.getGraph(),connec.getHost(),connec.getUsername(),connec.getPwd()); 
         VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(totalDocs.toString(), graph); 
         ResultSet results = vqe.execSelect();
         String totaldocsxml = ResultSetFormatter.asXMLString(results);
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