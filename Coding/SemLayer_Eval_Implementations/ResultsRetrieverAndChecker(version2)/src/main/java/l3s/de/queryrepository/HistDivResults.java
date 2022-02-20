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

package resultsretrieverandchecker;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
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
 * @author vaibhav
 */


public class HistDivResults {

    private ArrayList<Result> results;
    
    public HistDivResults(String query, int numOfResults, String fromDate, String toDate) throws ParserConfigurationException, SAXException, IOException, ParseException{
        
        results = new ArrayList<>();
        
        //WindowSize can either be "MONTH" or "YEAR"
        final String WindowSize = "MONTH";
        final String rows = "1000";
        final String mode = "masptd";
         
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        Date d1 = sdf.parse(fromDate);
        Date d2 = sdf.parse(toDate);
        long minTime = d1.getTime();
        long maxTime = d2.getTime();
        fromDate = Long.toString(minTime);
        toDate = Long.toString(maxTime);
        
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("http://pharos.l3s.uni-hannover.de:7080/ArchiveSearch/rest/nytimesnew/fetchNewspaper?q=");
        queryBuilder.append(query.replaceAll(" ", "%20"));
        queryBuilder.append("&rows=").append(rows).append("&mode=").append(mode);
        queryBuilder.append("&k=").append(numOfResults).append("&dmin=").append(fromDate);
        queryBuilder.append("&dmax=").append(toDate);
        queryBuilder.append("&window_size=").append(WindowSize);
        
        
        String urlStr = queryBuilder.toString();

        URL url = new URL(urlStr);
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.connect();
     
        JsonParser jp = new JsonParser(); 
        JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
        JsonArray json_array = (JsonArray) root;
        
        int i = 0;
        for(JsonElement j: json_array){
            String title = ((JsonObject) json_array.get(i)).get("headline").getAsString();
            String description = ((JsonObject) json_array.get(i)).get("snippet").getAsString();
            String publicationDate = ((JsonObject) json_array.get(i)).get("publicationDate").getAsString();
            String formattedPubDate = HistDivResults.dateFormatter(publicationDate);
           
            Result result = new Result(null, title, description, formattedPubDate);
            results.add(result); 
            i++;
        }
    }
    
    public static String dateFormatter(String pubDate) throws ParseException {

        DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

        long milliSeconds = Long.parseLong(pubDate);
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        String formattedDate = formatter.format(calendar.getTime());
        return formattedDate;

    }
    public ArrayList<Result> getResults() {
        return results;
    }

    public void setResults(ArrayList<Result> results) {
        this.results = results;
    }
}
