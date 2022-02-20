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

package reverseresultsretrieverandchecker;

/**
 *
 * @author Vaibhav Kasturia
 */

import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
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
    public int totalCorpusResults/*(String entity,*/(String category, String fromDate, String toDate) {
        ParameterizedSparqlString totalDocs = new ParameterizedSparqlString("PREFIX oa: <http://www.w3.org/ns/oa#>\n" +
                                                                            "PREFIX oae: <http://www.ics.forth.gr/isl/oae/core#>\n" +
                                                                            "PREFIX dc: <http://purl.org/dc/terms/>\n" +
                                                                            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                                                                            "PREFIX dbc: <http://dbpedia.org/resource/Category:>\n" +
                                                                            "PREFIX dbo: <http://dbpedia.org/ontology/>\n" +
                                                                            "PREFIX dbr: <http://dbpedia.org/resource/>\n" +
                                                                            "SELECT (count(distinct ?article) as ?count)\n" +
                                                                            "WHERE {\n" +
                                                                            "SERVICE <http://dbpedia.org/sparql> {\n" +
                                                                            "?entityUri dc:subject <"+ category +">}\n" +
                                                                            "?article dc:date ?date FILTER(?date >= xsd:date('" + fromDate + "') &&\n" +
                                                                            "?date <= xsd:date('" + toDate + "')).\n" +
                                                                            "?article oae:mentions ?entity .\n" +
                                                                            "?entity oae:hasMatchedURI ?entityUri .\n" +
                                                                            "}");

        Query query = QueryFactory.create(totalDocs.toString());
        //System.out.println("- SPARQL query:\n " + totalDocs.toString());
        QueryExecution vqe = QueryExecutionFactory.sparqlService("http://localhost:8890/sparql", query);

         //VirtuosoConnector to be used only in cases where overriding ARQ Parser is desirable
//         VirtuosoConnector connec = new VirtuosoConnector();
//         VirtGraph graph = new VirtGraph(connec.getHost(),connec.getUsername(),connec.getPwd()); 
//         System.out.println(totalDocs.toString());
//         VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(totalDocs.toString(), graph); 
        ResultSet results = vqe.execSelect();
        String totaldocsxml = ResultSetFormatter.asXMLString(results);

        //Close Virtuoso Query Execution once ResultSet gets stored
        vqe.close();

        Document doc = null;
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = loadXMLFromString(totaldocsxml);
            //System.out.println(totaldocsxml);
        } catch (Exception e) {
            System.out.println("Error in parsing TotalDocs XML");
        }
        Element rootElement = doc.getDocumentElement();
        String totaldox = getString("literal", rootElement);
        return Integer.parseInt(totaldox);
    }

    public List<String> titlesOfCorpusResults(String category, String fromDate, String toDate) {
        ParameterizedSparqlString corpusResultTitles = new ParameterizedSparqlString("PREFIX oa: <http://www.w3.org/ns/oa#>\n" +
                                                                                     "PREFIX oae: <http://www.ics.forth.gr/isl/oae/core#>\n" +
                                                                                     "PREFIX dc: <http://purl.org/dc/terms/>\n" +
                                                                                     "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                                                                                     "PREFIX dbc: <http://dbpedia.org/resource/Category:>\n" +
                                                                                     "PREFIX dbo: <http://dbpedia.org/ontology/>\n" +
                                                                                     "PREFIX dbr: <http://dbpedia.org/resource/>\n" +
                                                                                     "SELECT distinct ?title\n" +
                                                                                     "WHERE {\n" +
                                                                                     "SERVICE <http://dbpedia.org/sparql> {\n" +
                                                                                     "?entityUri dc:subject <" + category + ">}\n" +
                                                                                     
                                                                                     "?article dc:date ?date FILTER(?date >= xsd:date('" + fromDate + "') &&\n" +
                                                                                     "?date <= xsd:date('" + toDate + "')).\n" +
                                                                                     "?article oae:mentions ?entity .\n" +
                                                                                     "?entity oae:hasMatchedURI ?entityUri .\n" +
                                                                                     "?article dc:title ?title.\n" +
                                                                                     "}");

        Query query = QueryFactory.create(corpusResultTitles.toString());
        //System.out.println("- SPARQL query:\n " + corpusResultTitles.toString());
        QueryExecution vqe = QueryExecutionFactory.sparqlService("http://localhost:8890/sparql", query);

         //VirtuosoConnector to be used only in cases where overriding ARQ Parser is desirable
//         VirtuosoConnector connec = new VirtuosoConnector();
//         VirtGraph graph = new VirtGraph(connec.getHost(),connec.getUsername(),connec.getPwd()); 
//         VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(corpusResultTitles.toString(), graph); 
        
        long startTime = System.nanoTime();
        ResultSet results = vqe.execSelect();
        long estimatedTime = System.nanoTime() - startTime;
        
        System.out.println(("\nSPARQL Query Execution Time: "+ estimatedTime/1000000 + " ms\n"));
        String corpustitlesxml = ResultSetFormatter.asXMLString(results);
        
        
        //Close Virtuoso Query Execution once ResultSet gets stored
        vqe.close();
        
        //System.out.println(corpustitlesxml);
        Document doc = null;
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = loadXMLFromString(corpustitlesxml);
            //System.out.println(corpustitlesxml);
        } catch (Exception e) {
            System.out.println("Error in parsing Corpus Titles XML");
        }
        List<String> CorpusTitlesList = new LinkedList<String>();
        NodeList nList = doc.getElementsByTagName("result");
        for (int i = 0; i < nList.getLength(); i++) {
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
        
        //CorpusTitlesList.forEach(System.out::println);
        return CorpusTitlesList;
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
