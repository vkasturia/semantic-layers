/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package l3s.de.nyt_and_implementation;

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

public class EntityDocuments{
    public List<String> returndocuments(String entity, String fromDate, String toDate){
         ParameterizedSparqlString  rel_documents= new ParameterizedSparqlString("PREFIX oa: <http://www.w3.org/ns/oa#>\n" +
                                                                                 "PREFIX oae: <http://www.ics.forth.gr/isl/oae/core#>\n" +
                                                                                 "PREFIX dc: <http://purl.org/dc/terms/>\n" +
                                                                                 "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                                                                                 "SELECT ?article\n" + 
                                                                                 "WHERE {\n" +
                                                                                 "?article dc:date ?date FILTER(?date >= xsd:date('"+fromDate+"') &&\n" + 
                                                                                 "?date <= xsd:date('"+toDate+"')).\n" +
                                                                                 "?article oae:mentions ?entity .\n" +
                                                                                 "?entity oae:hasMatchedURI <"+entity+"> .\n" + 
                                                                                 "}");
    
         VirtuosoConnector connec = new VirtuosoConnector();
         VirtGraph graph = new VirtGraph(connec.getGraph(),connec.getHost(),connec.getUsername(),connec.getPwd()); 
         VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(rel_documents.toString(), graph); 
         ResultSet results = vqe.execSelect();
         String reldocsxml = ResultSetFormatter.asXMLString(results);
         Document doc = null;
         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
         try{
               DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
               doc = loadXMLFromString(reldocsxml);
               //System.out.println(reldocsxml);
         }catch(Exception e){
             System.out.println("Error in parsing Relevant Docs XML");
         }
         List<String> docsList = new LinkedList<String>();
         NodeList nList = doc.getElementsByTagName("result");
             for (int i = 0; i < nList.getLength(); i++){
                 Node nNode = nList.item(i);
                 if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                      Element eElement = (Element) nNode;
                      String docURI = eElement.getElementsByTagName("binding").item(0).getTextContent();
                      //System.out.println(docURI);
                      docsList.add(docURI);
                  }
             }
        return docsList;     
    }
    protected static Document loadXMLFromString(String xml) throws Exception{
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
    }   
}
