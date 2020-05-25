/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package l3s.de.queryrepository;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

public class ReadFromWeb {

    public static void main(String[] args) throws UnsupportedEncodingException, IOException, ParserConfigurationException, SAXException, ParseException {

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
                //BingResults bingResults = new BingResults(query, Integer.parseInt(numOfResultsStr));
                GoogleResults googleResults = new GoogleResults(query, Integer.parseInt(numOfResultsStr), fromDate, toDate);

                int rank = 0;
                int numOfResults = 0;
                for (Result result : googleResults.getResults()) {
                    numOfResults++;
                    //boolean inRange = dateRangeQuery(result.getDate(), fromDate, toDate); //FOR BING WE SHOULD CHECK IF THE RESULT IS IN VALID DATE RANGE
                    boolean inRange = true; //FOR GOOGLE 
                    //System.out.println("===> In valid date period: " + inRange);

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
