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

package l3s.de.entity_to_types;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Vaibhav Kasturia <kasturia at l3s.de>
 * 
 * Class to extract types of entities
 * 
 */
public class Entity_to_Types {
    
    public void entity_to_type(List<String> EntityURIList, int counter) throws FileNotFoundException, IOException {


        //PrintWriter writer = new PrintWriter("./results/entity2types_" + counter);

        FileWriter fw = new FileWriter("./results/entity2types_" + counter);
	BufferedWriter bw = new BufferedWriter(fw);
        
        java.util.Collections.sort(EntityURIList);

        for (String inst : EntityURIList) {
            //writer.println(inst + "\t");
            bw.write("\n" + inst + "\t");
            List<String> typeList = new LinkedList();
            String output = getUrlContents("http://dbpedia.org/data/" + inst + ".ntriples/");
            Pattern r = Pattern.compile("#type>\t[\\s\\S]*?> .");
            Matcher m = r.matcher(output);
            int j = 1;
            while (m.find()) {
                String type = m.group().replace("#type>\t", "").replace("> .", ">");
                //System.out.println(type);
                if(j == 1){
                    //writer.print(type);
                    bw.write(type);
                }else{
                    //writer.print("; " + type);
                    bw.write("; "+ type);
                }
                j+=1;
            }
        }

        //writer.close();
    }

    private static String getUrlContents(String theUrl) {
        StringBuilder content = new StringBuilder();

        try {
            // Create a url object
            URL url = new URL(theUrl);

            // Create a urlconnection object
            URLConnection urlConnection = url.openConnection();

            // Wrap the urlconnection in a bufferedreader
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

            String line;

            // Read from the urlconnection via the bufferedreader
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line + "\n");
            }
            bufferedReader.close();
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return content.toString();
    }
}
