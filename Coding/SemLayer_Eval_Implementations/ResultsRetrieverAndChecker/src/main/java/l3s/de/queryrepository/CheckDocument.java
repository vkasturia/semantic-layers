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

package l3s.de.queryrepository;

import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;

/**
 *
 * @author Vaibhav Kasturia
 */
public class CheckDocument {

    public static boolean sparqlTest(String titleString) {
        boolean askresult = false;

        String titleToCheck = titleString.toLowerCase().replace("nytimes.com", "").replace("the new york times", "").replace("nytimes", "").replace("...", "");
        titleToCheck = titleToCheck.replace("'", "&apos;").trim();
        if (titleToCheck.endsWith("-")) {
            titleToCheck = titleToCheck.substring(0, titleToCheck.length()-1).trim();
        }
        
        int i1 = titleToCheck.lastIndexOf("- ");
        int i2 = titleToCheck.lastIndexOf("; ");
        if (i1 != -1 || i2 != -1) {
            if (i1 > i2) {
                titleToCheck = titleToCheck.substring(i1+1).trim();
            } else {
                titleToCheck = titleToCheck.substring(i2+1).trim();
            }
        }
        titleToCheck = titleToCheck.replace("&#39;", "&apos;").replace("?", "\\\\?");
        
        //if (titleToCheck.startsWith("the world -")) {
            //titleToCheck = titleToCheck.substring(11).trim();
        //}
        //titleToCheck = titleToCheck.replace(" - ", "; ");
        System.out.println("- Initial 'bing' title: " + titleString);
        System.out.println("- Title to check in repository: " + titleToCheck);

        ParameterizedSparqlString queryString = new ParameterizedSparqlString("PREFIX oa: <http://www.w3.org/ns/oa#> "
                + "PREFIX oae: <http://www.ics.forth.gr/isl/oae/core#> "
                + "PREFIX dc: <http://purl.org/dc/terms/> "
                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
                + "ASK "
                + "WHERE { "
                + " ?article dc:title ?title "
                + " FILTER(regex(str(?title),'"+titleToCheck+"','i')) . "
                + "}");

        Query query = QueryFactory.create(queryString.toString());
        System.out.println("- SPARQL query: " + queryString.toString());
        QueryExecution qexec = QueryExecutionFactory.sparqlService("http://localhost:8890/sparql", query);
        askresult = qexec.execAsk();
        qexec.close();

        return askresult;
    }

    public static void main(String[] args) {
        boolean result = sparqlTest(" AID TO TAMILS IS ");
        System.out.println("===>" + result);
    }
}
