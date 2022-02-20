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

package resultsretrieverandchecker;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

public class ReadFromWeb {

    public static void main(String[] args) throws UnsupportedEncodingException, IOException, ParserConfigurationException, SAXException, ParseException {
        
        //To read input from a file for Google or Bing
        String file = "query_param";
        
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(file), "UTF8"))) {
            String str;
            while ((str = in.readLine()) != null) {
                //System.out.println(str);
                if (str.startsWith("#")) {
                    continue;
                }

                String[] data = str.split("\t");
                if (data.length != 4) {
                    System.out.println("*** Malformed line! Continuing to next line...");
                }

                int totalNumOfResultsInValidDateRange = 0;
                int totalNumOfResultsExistingInRepo = 0;

                String query = data[0].trim();
                String numOfResultsStr = data[1].trim();
                String fromDate = data[2].trim();
                String toDate = data[3].trim();

                System.out.println("----------------------");
                System.out.println("# Query: " + query);
                System.out.println("# Num of results: " + numOfResultsStr);
                System.out.println("# From date: " + fromDate);
                System.out.println("# To date: " + toDate);
                System.out.println("\n# Start retrieving the results from Bing...");
 
                //Find Results for either Bing, Google or HistDiv
                //BingResults searchResults = new BingResults(query, Integer.parseInt(numOfResultsStr));
                GoogleResults searchResults = new GoogleResults(query, Integer.parseInt(numOfResultsStr), fromDate, toDate);
                //HistDivResults searchResults = new HistDivResults(query, Integer.parseInt(numOfResultsStr), fromDate, toDate);
                
                int rank = 0;
                int numOfResults = 0;
                List<String> SearchEngineResultsList = new LinkedList<String>();
                
                for (Result result : searchResults.getResults()) {
                    numOfResults++;
                    
                    //Check whether the Results are in given Date Range 
                    //boolean inRange = dateRangeQuery(result.getDate(), fromDate, toDate); //FOR BING WE SHOULD CHECK IF THE RESULT IS IN VALID DATE RANGE
                    boolean inRange = true; //FOR GOOGLE, HISTDIV 
                    //System.out.println("===> In valid date period: " + inRange);
                
                    
                    if (inRange) {
                        totalNumOfResultsInValidDateRange++;

                        System.out.println("  - Result number: " + rank);
                        System.out.println("  - URL: " + result.getUrl());
                        System.out.println("  - Title: " + result.getTitle());
                        System.out.println("  - Description: " + result.getDescription());
                        System.out.println("  - Date: " + result.getDate());

                        String titleToAdd = result.getTitle().toLowerCase().replace("nytimes.com", "").replace("the new york times", "").replace("nytimes", "").replace("...", "");
                        titleToAdd = titleToAdd.replace("'", "&apos;").trim();
                        if (titleToAdd.endsWith("-")) {
                            titleToAdd = titleToAdd.substring(0, titleToAdd.length() - 1).trim();
                        }

                        int i1 = titleToAdd.lastIndexOf("- ");
                        int i2 = titleToAdd.lastIndexOf("; ");
                        if (i1 != -1 || i2 != -1) {
                            if (i1 > i2) {
                                titleToAdd = titleToAdd.substring(i1 + 1).trim();
                            } else {
                                titleToAdd = titleToAdd.substring(i2 + 1).trim();
                            }
                        }
                        titleToAdd = titleToAdd.replace("&#39;", "&apos;").replace("?", "\\\\?");
                        
                        SearchEngineResultsList.add(titleToAdd);
                        
                        boolean inCorpus = CheckDocument.sparqlTest(result.getTitle());
                        System.out.println("  => Exists in Corpus: " + inCorpus);
                        if (inCorpus) {
                            totalNumOfResultsExistingInRepo++;
                        }
                        System.out.println("-----------------");
                    }

                    rank++;
                }

                System.out.println("# Total number of results: " + numOfResults);
                System.out.println("# Total number of results in valid date: " + totalNumOfResultsInValidDateRange);
                System.out.println("# Total number of results existing in repository: " + totalNumOfResultsExistingInRepo);
                
                CorpusResults corpResults = new CorpusResults();
                   
                   //Change date from dd-MM-yyyy format to yyyy-MM-dd format for passing to SPARQL endpoint
                   final String OLD_FORMAT = "dd-MM-yyyy";
                   final String NEW_FORMAT = "yyyy-MM-dd";
        
                   SimpleDateFormat sdf = new SimpleDateFormat(OLD_FORMAT);
                   Date d1 = sdf.parse(fromDate);
                   Date d2 = sdf.parse(toDate);
                   sdf.applyPattern(NEW_FORMAT);
                   fromDate = sdf.format(d1);
                   toDate = sdf.format(d2);

                    //Get the total number of Results in NYT Corpus for corresponding SPARQL Query
                    System.out.println("Total number of Results for SPARQL Query in NYT Corpus: " +corpResults.totalCorpusResults(fromDate, toDate));
                    
                    //Get the total number of Matches of Search Engine Results with NYT Corpus
                    CorpusResultsMatcher corpresultsMatcher = new CorpusResultsMatcher();
                    System.out.println("Number of matches of Google Results with NYT Corpus: "  +corpresultsMatcher.commonResults(corpResults.titlesOfCorpusResults(fromDate, toDate), SearchEngineResultsList));
                    
            }
        }
       
    }

    public static boolean dateRangeQuery(String formatted_pubDate, String beginDate, String endDate) {
        boolean lies_in_range = false;
        DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        try {
            Date pubDate = (Date) formatter.parse(formatted_pubDate);
            Date fromDate = (Date) formatter.parse(beginDate);
            Date toDate = (Date) formatter.parse(endDate);

            if (pubDate.after(fromDate) && pubDate.before(toDate)) {
                lies_in_range = true;
            }

        } catch (Exception ex) {
            System.out.println(ex);
        }
        return lies_in_range;
    }

}

