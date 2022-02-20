/*
* Copyright 2017-2022 Vaibhav Kasturia <vbh18kas@gmail.com>
*
* Permission is hereby granted, free of charge, to any person obtaining a copy of this software 
* and associated documentation files (the "Software"), to deal in the Software without restriction, 
* including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
* and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
* subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in all copies or substantial 
* portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT 
* LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
* IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
* WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE 
* OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package l3s.de.nyt_entities;

import java.io.FileNotFoundException;
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

/**
 *
 * @author Vaibhav Kasturia <kasturia at l3s.de>
 *
 * Class to get distinct Entities from a specified Time Period
 */
public class Query_Entities {

    public List<String> entity_finder(String fromDate, String toDate) throws FileNotFoundException {

        Query_Entities query_entities = new Query_Entities();

        int counter = Integer.MAX_VALUE;
        int h = 0;

        List<String> entityURIList = new LinkedList<>();

        while (counter > 9999) {
            
            ParameterizedSparqlString EntitiesCounter = new ParameterizedSparqlString("PREFIX oa: <http://www.w3.org/ns/oa#>\n" +
                                                                                      "PREFIX oae: <http://www.ics.forth.gr/isl/oae/core#>\n" +
                                                                                      "PREFIX dc: <http://purl.org/dc/terms/>\n" +
                                                                                      "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                                                                                      "PREFIX schema:  <http://schema.org/>\n" +
                                                                                      "SELECT COUNT DISTINCT ?entityURI WHERE {\n" +
                                                                                      "?article dc:date ?date FILTER(?date >= xsd:date('" + fromDate + "') && \n" +
                                                                                      "?date <= xsd:date('" + toDate + "')).\n" +
                                                                                      "?article schema:mentions ?entity .\n" +
                                                                                      "?entity oae:hasMatchedURI  ?entityURI. \n" +
                                                                                      "}LIMIT 10000 OFFSET " + h);
            
            
            VirtuosoConnector connec = new VirtuosoConnector();
            VirtGraph graph = new VirtGraph(connec.getGraph(), connec.getHost(), connec.getUsername(), connec.getPwd());

            VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(EntitiesCounter.toString(), graph);
            ResultSet results = vqe.execSelect();
            String EntitiesCountxml = ResultSetFormatter.asXMLString(results);
            //System.out.println(EntitiesCountxml);
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
            
            //Getting the articles and the corresponding time period P in which they were published 
            ParameterizedSparqlString popularEntities = new ParameterizedSparqlString("PREFIX oa: <http://www.w3.org/ns/oa#>\n" +
                                                                                      "PREFIX oae: <http://www.ics.forth.gr/isl/oae/core#>\n" +
                                                                                      "PREFIX dc: <http://purl.org/dc/terms/>\n" +
                                                                                      "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                                                                                      "PREFIX schema:  <http://schema.org/>\n" +
                                                                                      "SELECT DISTINCT ?entityURI WHERE {\n" +
                                                                                      "?article dc:date ?date FILTER(?date >= xsd:date('" + fromDate + "') && \n" +
                                                                                      "?date <= xsd:date('" + toDate + "')).\n" +
                                                                                      "?article schema:mentions ?entity .\n" +
                                                                                      "?entity oae:hasMatchedURI  ?entityURI. \n" +
                                                                                      "}LIMIT 10000 OFFSET " + h);

            vqe = VirtuosoQueryExecutionFactory.create(popularEntities.toString(), graph);
            results = vqe.execSelect();
            String totaldocsxml = ResultSetFormatter.asXMLString(results);
            doc = null;
            dbFactory = DocumentBuilderFactory.newInstance();
            try {
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                doc = loadXMLFromString(totaldocsxml);
            } catch (Exception e) {
                System.out.println("Error in parsing Popular Entities XML");
            }
            NodeList nList = doc.getElementsByTagName("result");
            for (int i = 0; i < nList.getLength(); i++) {
                Node nNode = nList.item(i);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    String entityURI = eElement.getElementsByTagName("binding").item(0).getTextContent();
                    entityURI = entityURI.replaceAll("\n", "").replaceAll(" ", "");
                    entityURI = entityURI.replace("http://dbpedia.org/resource/", "").replace("&amp;", "%26").replace("&quot;", "%22");
                    entityURIList.add(entityURI);
                }
            }
            if(entityURIList.size()%10000==0)
                System.out.println("Entity List Size: "+entityURIList.size());

            h += 10000;
        }
        return entityURIList;
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
