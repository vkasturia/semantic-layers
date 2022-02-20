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

package l3s.de.nyt_and_implementation;

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

public class DocEFPScore{
    public Map<String, String> return_docefpscore(String entity, List<String> docsList, Map<String, String> TimePeriodScoreMap, double highest_freq){
      Map<String, String> DocEFPScoreMap = new LinkedHashMap<String, String>();
      for(String docURI : docsList ){  
         String entity_count = null;
         String year = null;
         docURI = docURI.replace("\n", "").replaceAll(" ", "");
         ParameterizedSparqlString relatedEntities = new ParameterizedSparqlString("PREFIX oa: <http://www.w3.org/ns/oa#>\n" +
                                                                                   "PREFIX oae: <http://www.ics.forth.gr/isl/oae/core#>\n" +
                                                                                   "PREFIX dc: <http://purl.org/dc/terms/>\n" +
                                                                                   "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                                                                                   "SELECT  (count(?entity) as ?count) (year(?date) as ?year)  WHERE {\n" +
                                                                                   "<"+docURI+"> oae:mentions ?entity .\n" +
                                                                                   "?entity oae:hasMatchedURI  <"+entity+"> .\n" +
                                                                                   "<"+docURI+"> dc:date ?date. \n" +
                                                                                   "}");
         VirtuosoConnector connec = new VirtuosoConnector();
         VirtGraph graph = new VirtGraph(connec.getGraph(),connec.getHost(),connec.getUsername(),connec.getPwd()); 
         VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(relatedEntities.toString(), graph); 
         ResultSet results = vqe.execSelect();
         String EntityCountxml = ResultSetFormatter.asXMLString(results);
         //System.out.println(EntityCountxml);
         Document doc = null;
         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
         try{
               DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	       doc = loadXMLFromString(EntityCountxml);
         }catch(Exception e){
             System.out.println("Error in parsing Document Entity Frequency XML");
         }
         
         NodeList nList = doc.getElementsByTagName("result");
             for (int i = 0; i < nList.getLength(); i++){
                 Node nNode = nList.item(i);
                 if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                      Element eElement = (Element) nNode;
                      entity_count = eElement.getElementsByTagName("binding").item(0).getTextContent();
                      year = eElement.getElementsByTagName("binding").item(1).getTextContent();
                      entity_count = entity_count.replaceAll("\n", "").replaceAll(" ", "");
                 }
             }
         double entity_frequency = Double.parseDouble(entity_count);
         
         //Calculating the Def score using highest entity frequency in denominator
         //The denominator can be set later to value as desired
         double ScoreDef = entity_frequency / highest_freq;
         
         //Calculating the Def,P Score for a document
         String year_score = TimePeriodScoreMap.get(year);
         year_score = year_score.replaceAll("\n", "").replaceAll(" ", "");
         double timep_score = Double.parseDouble(year_score);
         double ScoreDefP = ScoreDef * timep_score; 
         String docDefPscore = Double.toString(ScoreDefP);
         DocEFPScoreMap.put(docURI, docDefPscore);
      }
      return DocEFPScoreMap;
    }     
    protected static Document loadXMLFromString(String xml) throws Exception{
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
    }
}