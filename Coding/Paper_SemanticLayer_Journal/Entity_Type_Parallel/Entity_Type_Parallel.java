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


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Vaibhav Kasturia <kasturia at l3s.de>
 * 
 * Class to get Entity_to_Types in Parallel
 * 
 */
public class Entity_Type_Parallel {

    public static void main(String[] args) throws InterruptedException, IOException {
        for (int g = 1987; g < 1999; g++) {
            int num_of_threads = 10;
            ThreadDemo[] threadlist = new ThreadDemo[num_of_threads];

            BufferedReader br = null;
            FileReader fr = null;

            fr = new FileReader("./uriLists/entityURIList_"+ g +".txt");
            br = new BufferedReader(fr);

            String sCurrentLine;
            List<String> entityURIList = new LinkedList<>();

            while ((sCurrentLine = br.readLine()) != null) {
                //reSystem.out.println(sCurrentLine);
                entityURIList.add(sCurrentLine);
            }

            List<List<String>> entityURIList_group = new LinkedList<>();

            java.util.Collections.sort(entityURIList);

            System.out.println("Started year: "+ g);
            
            int j = 0;
            int k = 0;
            for (int i = 0; i < num_of_threads; i++) {
                List<String> entityURI_partitionedList = new LinkedList<>();
                for (k = (entityURIList.size() / num_of_threads) * j; k < (entityURIList.size() / num_of_threads) * (j + 1); k++) {
                    String entity = entityURIList.get(k);
                    entityURI_partitionedList.add(entity);
                }
                entityURIList_group.add(entityURI_partitionedList);
                j += 1;
            }

            int p = 0;
            for (List<String> d : entityURIList_group) {

                threadlist[p] = new ThreadDemo("Thread" + p, d, g);
                threadlist[p].start();
                System.out.println("Started Thread:" + p);
                p += 1;

            }

            for (int x = 0; x < num_of_threads; x++) {
                threadlist[x].join();
            }

            List<Path> inputs = new ArrayList<>();

            for (int z = 0; z < p; z++) {
                inputs.add(Paths.get("./results/entity2types_"+ g +"_Thread" + z));
            }

            // Output file
            Path output = Paths.get("./results/entity2types_"+ g);

            // Charset for read and write
            Charset charset = StandardCharsets.UTF_8;

            // Join files (lines)
            for (Path path : inputs) {
                List<String> lines = Files.readAllLines(path, charset);
                Files.write(output, lines, charset, StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND);
            }
        }

    }
}
class ThreadDemo extends Thread {

    private List<String> partitioned_entityURIList1;
    private String threadName1;
    private int year1;

    ThreadDemo(String threadName, List<String> partitioned_entityURIList, int year) {
        threadName1 = threadName;
        partitioned_entityURIList1 = partitioned_entityURIList;
        year1 = year;
    }

    public void run() {
        try{
            FileWriter fw = new FileWriter("./results/entity2types_"+ year1 + "_" + threadName1);
            BufferedWriter bw = new BufferedWriter(fw);
        

        for (String inst : partitioned_entityURIList1) {
            //writer.println(inst + "\t");
            bw.write("\n" + inst + "\t");
            String output = getUrlContents("http://dbpedia.org/data/" + inst + ".ntriples/", inst);
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
            
        }catch(Exception e){
            e.printStackTrace();
        }

    }
    public String getUrlContents(String theUrl, String inst) throws IOException {
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
            e.printStackTrace();
            FileWriter fw = new FileWriter("./results/Error_entities_"+ year1 +".txt", true);
            PrintWriter pw = new PrintWriter(fw); 
            e.printStackTrace(pw);
            pw.close();
       }
        return content.toString();
    }
}
