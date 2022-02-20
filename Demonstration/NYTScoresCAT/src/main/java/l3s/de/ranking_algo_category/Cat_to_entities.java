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

package l3s.de.ranking_algo_category;

import java.io.StringReader;
import java.util.ArrayList;
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

/**
 * @author Vaibhav Kasturia <kasturia at l3s.de>
 *         <p>
 *         Class to get the entities of interest from specified category
 */
public class Cat_to_entities {

	public String[] entitiesreturn(String Category, String fromDate, String toDate) {

		// Arraylist to store entities
		List<String> entityList = new ArrayList<String>();

		ParameterizedSparqlString entitiesListQuery = new ParameterizedSparqlString(
				"PREFIX oa: <http://www.w3.org/ns/oa#>\n" + "PREFIX oae: <http://www.ics.forth.gr/isl/oae/core#>\n"
						+ "PREFIX dc: <http://purl.org/dc/terms/>\n"
						+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
						+ "PREFIX dbc: <http://dbpedia.org/resource/Category:>\n"
						+ "PREFIX dbo: <http://dbpedia.org/ontology/>\n"
						+ "PREFIX dbr: <http://dbpedia.org/resource/>\n" + "PREFIX schema:  <http://schema.org/>\n"
						+ "SELECT distinct ?entityUri\n" + "WHERE {\n" + "SERVICE <http://dbpedia.org/sparql> {\n"
						+ "?entityUri dc:subject dbc:" + Category + ".}\n"
						+ "?article dc:date ?date FILTER(?date >= xsd:date('" + fromDate + "') &&\n"
						+ "?date <= xsd:date('" + toDate + "')).\n" + "?article schema:mentions ?entity .\n"
						+ "?entity oae:hasMatchedURI ?entityUri .\n" + "?article dc:title ?title.\n" + "}");

		Query query = QueryFactory.create(entitiesListQuery.toString());
		QueryExecution vqe = QueryExecutionFactory.sparqlService("http://localhost:8890/sparql", query);

		ResultSet results = vqe.execSelect();

		String entitieslistxml = ResultSetFormatter.asXMLString(results);

		// Close Virtuoso Query Execution once ResultSet gets stored
		vqe.close();

		// System.out.println(entitieslistxml);
		Document doc = null;
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			doc = loadXMLFromString(entitieslistxml);
			// System.out.println(entitieslistxml);
		} catch (Exception e) {
			System.out.println("Error in parsing Corpus Titles XML");
		}

		NodeList nList = doc.getElementsByTagName("result");
		for (int i = 0; i < nList.getLength(); i++) {
			Node nNode = nList.item(i);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				String entityURI = eElement.getElementsByTagName("binding").item(0).getTextContent();
				entityURI = entityURI.replaceAll(" ", "").replaceAll("\n", "");
				entityList.add(entityURI);
			}
		}

		// entityList.forEach(System.out::println);

		String[] entityArray = new String[entityList.size()];

		for (int z = 0; z < entityList.size(); z++) {
			entityArray[z] = entityList.get(z);
		}

		return entityArray;

	}

	protected static Document loadXMLFromString(String xml) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource is = new InputSource(new StringReader(xml));
		return builder.parse(is);
	}
}
