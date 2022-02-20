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

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 *
 * @author Fafalios, Kasturia
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

                Result result = new Result(url, title);
                results.add(result);

                i1 = tagger.getFirstTagIndexContains("div", "class=\"g\"", i1 + 1);
            }

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
