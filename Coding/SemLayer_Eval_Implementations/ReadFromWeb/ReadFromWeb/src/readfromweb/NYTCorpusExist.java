/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package readfromweb;


import com.hp.hpl.jena.query.*;
import org.apache.jena.query.ParameterizedSparqlString;
/**
 *
 * @author vaibhav
 */
public class NYTCorpusExist {
   public boolean sparqlTest(String titleString)
       {  
           boolean askresult = false;
           System.out.println(titleString);
           String titleToCheck = titleString.toLowerCase().replace("-", "").replace("nytimes.com", "").replace("the new york times", "").replace("...", "");
           System.out.println(titleToCheck);
           System.out.println("----");
           String i = "i";
           final ParameterizedSparqlString queryString = new ParameterizedSparqlString("PREFIX oa: <http://www.w3.org/ns/oa#> "+
                                                                         "PREFIX oae: <http://www.ics.forth.gr/isl/oae/core#> " +
                                                                         "PREFIX dc: <http://purl.org/dc/terms/> "+
                                                                         "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "+
                                                                         "ASK "+
                                                                         "WHERE { "+
                                                                         " ?article dc:title ?title. "+
                                                                         " filter (contains(lcase(?title), \""+titleToCheck+"\")). "+
                                                                         "}");
           Query query = QueryFactory.create(queryString.toString());        
           QueryExecution qexec =  QueryExecutionFactory.sparqlService("http://localhost:8890/sparql", query);
           try
           {
                askresult = qexec.execAsk();
                //System.out.println(askresult);
        
            }
            finally{
              qexec.close();
            }
           return askresult;
       }
}

