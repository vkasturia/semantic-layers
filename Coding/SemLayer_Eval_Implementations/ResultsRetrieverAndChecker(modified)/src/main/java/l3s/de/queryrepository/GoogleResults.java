/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package resultsretrieverandcheckerlocal;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 * @author Fafalios
 */
public class GoogleResults {

    private ArrayList<Result> results;

    public GoogleResults(String query, int numOfResults, String fromDate, String toDate) throws ParserConfigurationException, SAXException, IOException, ParseException {

        final String OLD_FORMAT = "dd-MM-yyyy";
        final String NEW_FORMAT = "MM-dd-yyyy";
        
        SimpleDateFormat sdf = new SimpleDateFormat(OLD_FORMAT);
        Date d1 = sdf.parse(fromDate);
        Date d2 = sdf.parse(toDate);
        sdf.applyPattern(NEW_FORMAT);
        fromDate = sdf.format(d1);
        toDate = sdf.format(d2);

        results = new ArrayList<>();
        int numOfRequests = numOfResults / 50;
        int n = 0;

        for (int x = 0; x < numOfRequests; x++) {

            int offset = 50 * x;

            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("https://www.google.co.in/search?q=");
            queryBuilder.append(query.replaceAll(" ", "%20"));
            queryBuilder.append("%20site:nytimes.com&num=50&start=").append(offset).append("&biw=1366&bih=638&tbs=cdr%3A1%2Ccd_min%3A").append(fromDate.replace("-", ".")).append("%2Ccd_max%3A").append(toDate.replace("-", ".")).append("");
            String urlStr = queryBuilder.toString();
            System.out.println(urlStr);

            URL googleUrl = new URL(urlStr);
            HTMLTag tagger = new HTMLTag(googleUrl);
            int i1 = tagger.getFirstTagIndexContains("div", "class=\"g\"");
            while (i1 != -1) {
                String resultData = tagger.getFirstTagDataContains("div", "class=\"g\"", i1);
                HTMLTag resTagger = new HTMLTag(resultData);

                String url = resTagger.getFirstTagContent("a");
                url = HTMLTag.getHref(url);

                String title = resTagger.getFirstTagData("h3");
                title = HTMLTag.removeTags(title);

                String description = resTagger.getFirstTagDataContains("span", "class=\"st\"");
                description = description.substring(description.indexOf("</span>") + 7).trim();
                description = HTMLTag.removeTags(description);

                String date = resTagger.getFirstTagDataContains("span", "class=\"f\"").replace("-", "").trim();
                String formattedPubDate = GoogleResults.dateFormatter(date);

                Result result = new Result(url, title, description, formattedPubDate);
                results.add(result);

//                System.out.println("n: " + n++);
//                System.out.println("URL: " + url);
//                System.out.println("Title: " + title);
//                System.out.println("Description: " + description);
//                System.out.println("Date: " + date);
//                System.out.println("Formatted date: " + formattedPubDate);
//                System.out.println("--------");
                i1 = tagger.getFirstTagIndexContains("div", "class=\"g\"", i1 + 1);
            }

//            String link = eElement.getElementsByTagName("link").item(0).getTextContent();
//            String title = eElement.getElementsByTagName("title").item(0).getTextContent();
//            String description = eElement.getElementsByTagName("description").item(0).getTextContent();
//            String publicationDate = eElement.getElementsByTagName("pubDate").item(0).getTextContent();
//            String formattedPubDate = GoogleResults.dateFormatter(publicationDate);
//
//            Result result = new Result(link, title, description, formattedPubDate);
//            results.add(result);
        }
    }

    public static String parseDate(String date) {

        //Change German Format months to English Format months
        String part = date.replace("Mrz", "Mar");
        part = part.replace("Mai", "May");
        part = part.replace("Okt", "Oct");
        part = part.replace("Dez", "Dec");

        part = part.replace("Jan", "01");
        part = part.replace("Feb", "02");
        part = part.replace("Mar", "03");
        part = part.replace("Apr", "04");
        part = part.replace("May", "05");
        part = part.replace("Jun", "06");
        part = part.replace("Jul", "07");
        part = part.replace("Aug", "08");
        part = part.replace("Sep", "09");
        part = part.replace("Oct", "10");
        part = part.replace("Nov", "11");
        part = part.replace("Dec", "12");

        return part;
    }

    public static String dateFormatter(String pubDate) throws ParseException {

        String parsedDate = GoogleResults.parseDate(pubDate);
        DateFormat formatter = new SimpleDateFormat("MM d, yyyy");
        Date date = new Date(formatter.parse(parsedDate).getTime());

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        String formattedDate = cal.get(Calendar.DATE) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.YEAR);
        //System.out.println("formattedDate : " + formattedDate);
        return formattedDate;

    }

    public ArrayList<Result> getResults() {
        return results;
    }

    public void setResults(ArrayList<Result> results) {
        this.results = results;
    }

    /**
     * @param args the command line arguments
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws org.xml.sax.SAXException
     * @throws java.io.IOException
     * @throws java.text.ParseException
     */
    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, ParseException {
        GoogleResults g = new GoogleResults("rajiv gandhi", 100, "01.03.1987", "31.08.1991");
    }

}
