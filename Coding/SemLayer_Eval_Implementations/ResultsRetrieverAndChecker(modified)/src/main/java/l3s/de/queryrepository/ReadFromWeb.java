/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package resultsretrieverandcheckerlocal;

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
        /*
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
                BingResults bingResults = new BingResults(query, Integer.parseInt(numOfResultsStr));
                //GoogleResults googleResults = new GoogleResults(query, Integer.parseInt(numOfResultsStr), fromDate, toDate);

                int rank = 0;
                int numOfResults = 0;
                List<String> GoogleResultsList = new LinkedList<String>();
                
                for (Result result : bingResults.getResults()) {
                    numOfResults++;
                    boolean inRange = dateRangeQuery(result.getDate(), fromDate, toDate); //FOR BING WE SHOULD CHECK IF THE RESULT IS IN VALID DATE RANGE
                    //boolean inRange = true; //FOR GOOGLE 
                    //System.out.println("===> In valid date period: " + inRange);
                    if (inRange) {
                        totalNumOfResultsInValidDateRange++;

                        System.out.println("  - Result number: " + rank);
                        System.out.println("  - URL: " + result.getUrl());
                        System.out.println("  - Title: " + result.getTitle());
                        System.out.println("  - Description: " + result.getDescription());
                        System.out.println("  - Date: " + result.getDate());

//                        String titleToAdd = result.getTitle().toLowerCase().replace("nytimes.com", "").replace("the new york times", "").replace("nytimes", "").replace("...", "");
//                        titleToAdd = titleToAdd.replace("'", "&apos;").trim();
//                        if (titleToAdd.endsWith("-")) {
//                            titleToAdd = titleToAdd.substring(0, titleToAdd.length() - 1).trim();
//                        }
//
//                        int i1 = titleToAdd.lastIndexOf("- ");
//                        int i2 = titleToAdd.lastIndexOf("; ");
//                        if (i1 != -1 || i2 != -1) {
//                            if (i1 > i2) {
//                                titleToAdd = titleToAdd.substring(i1 + 1).trim();
//                            } else {
//                                titleToAdd = titleToAdd.substring(i2 + 1).trim();
//                            }
//                        }
//                        titleToAdd = titleToAdd.replace("&#39;", "&apos;").replace("?", "\\\\?");
//                        
//                        GoogleResultsList.add(titleToAdd);
                        
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
                
//                CorpusResults corpResults = new CorpusResults();
//                    
//                    //Get the total number of Results in NYT Corpus for corresponding SPARQL Query
//                    System.out.println("Total number of Results for SPARQL Query in NYT Corpus: " +corpResults.totalCorpusResults("1992-08-01", "1992-08-31"));
//                    
//                    //Get the total number of Matches of Search Engine Results with NYT Corpus
//                    CorpusResultsMatcher corpresultsMatcher = new CorpusResultsMatcher();
//                    System.out.println("Number of matches of Google Results with NYT Corpus: "  +corpresultsMatcher.commonResults(corpResults.titlesOfCorpusResults("1992-08-01", "1992-08-31"), GoogleResultsList));
//                    
            }
        }
       */
       //To read an input from a file for ArchiveSearch and search the contents
       String file2 = "arv_search_queries";
       
       try (BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(file2), "UTF8"))) {
            String str;
            while ((str = in.readLine()) != null) {
                if (str.startsWith("#")) {
                    continue;
                }

                String[] data = str.split("\t");
                if (data.length != 6) {
                    System.out.println("*** Malformed line! Continuing to next line...");
                }

                int totalNumOfResultsInValidDateRange = 0;
                int totalNumOfResultsExistingInRepo = 0;

                String query = data[0].trim();
                String numOfRowsStr = data[1].trim();
                String numOfResultsStr = data[2].trim();
                String fromDate = data[3].trim();
                String toDate = data[4].trim();
                String searchMode = data[5].trim();

                System.out.println("----------------------");
                System.out.println("# Query: " + query);
                System.out.println("# Num of rows:" + numOfRowsStr);
                System.out.println("# Num of results: " + numOfResultsStr);
                System.out.println("# From date: " + fromDate);
                System.out.println("# To date: " + toDate);
                
                switch (searchMode) {
                    case "lm":
                        System.out.println("# Search mode: Textual Relevance");
                        break;
                    case "lmt":
                        System.out.println("# Search mode: Time Focus");
                        break;
                    case "lmtd":
                        System.out.println("# Search mode: Time Diverse");
                        break;
                    case "ntiasel":
                        System.out.println("# Search mode: Topic Diverse");
                        break;
                    case "masptd":
                        System.out.println("# Search mode: HistDiv");
                        break;
                }
                
                System.out.println("\n# Start retrieving the results from HistDiv System...");
                ArchiveSearchResults histdivResults = new ArchiveSearchResults(query, numOfRowsStr, numOfResultsStr, fromDate, toDate, searchMode);
                
                int rank = 0;
                int numOfResults = 0;
                List<String> histdivResultsList = new LinkedList<String>();
                
                for (Result result : histdivResults.getResults()) {
                    numOfResults++;
                    boolean inRange = true; //FOR ARCHIVE SEARCH SYSTEM 
                    if (inRange) {
                        totalNumOfResultsInValidDateRange++;

                        System.out.println("  - Result number: " + rank);
                        System.out.println("  - URL: " + result.getUrl());
                        System.out.println("  - Title: " + result.getTitle());
                        System.out.println("  - Description: " + result.getDescription());
                        System.out.println("  - Date: " + result.getDate());
    
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

