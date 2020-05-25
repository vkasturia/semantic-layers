package nyt_and_implement;

import java.io.StringReader;
import java.util.List;
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

public class MaxEntityFreq{
    public double return_maxfreq(String entity, List<String> docsList){
      double highest_freq = 0;
      double entity_frequency = 0; 
      for(String docURI : docsList ){   
         docURI = docURI.replace("\n", "").replaceAll(" ", "");
         ParameterizedSparqlString relatedEntities = new ParameterizedSparqlString("PREFIX oa: <http://www.w3.org/ns/oa#>\n" +
                                                                                   "PREFIX oae: <http://www.ics.forth.gr/isl/oae/core#>\n" +
                                                                                   "PREFIX dc: <http://purl.org/dc/terms/>\n" +
                                                                                   "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                                                                                   "SELECT  (count(?entity) as ?count)  WHERE {\n" +
                                                                                   "<"+docURI+"> oae:mentions ?entity .\n" +
                                                                                   "?entity oae:hasMatchedURI  <"+entity+"> .\n" +
                                                                                   "}");
         VirtuosoConnector connec = new VirtuosoConnector();
         VirtGraph graph = new VirtGraph(connec.getGraph(),connec.getHost(),connec.getUsername(),connec.getPwd()); 
         VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(relatedEntities.toString(), graph); 
         ResultSet results = vqe.execSelect();
         String maxEntityxml = ResultSetFormatter.asXMLString(results);
         //System.out.println(maxEntityxml);
         Document doc = null;
         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
         try{
               DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	       doc = loadXMLFromString(maxEntityxml);
         }catch(Exception e){
             System.out.println("Error in parsing Max Entity Frequency XML");
         }
         Element rootElement = doc.getDocumentElement();
         String entity_count = getString("literal",rootElement);
         entity_frequency = Integer.parseInt(entity_count); 
      } 
         if(entity_frequency > highest_freq) 
             highest_freq = entity_frequency;
      return highest_freq;   
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