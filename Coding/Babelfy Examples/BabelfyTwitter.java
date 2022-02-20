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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.uniroma1.lcl.babelfy.commons.BabelfyParameters;
import it.uniroma1.lcl.babelfy.commons.annotation.SemanticAnnotation;
import it.uniroma1.lcl.babelfy.core.Babelfy;
import it.uniroma1.lcl.jlt.util.Language;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class BabelfyTwitter {
	public static void main(String[] args) throws IOException {
		
		BabelfyTwitter a = new BabelfyTwitter();
		
		String csvFile = "./SampleTweets/tweets_us_nps_2016.csv";
		BufferedReader br = null;
		String line = "";
		String csvSplitBy = ";";
		
		// Set Babelfy parameters
	    BabelfyParameters prs = new BabelfyParameters();
	    prs.setContextSize(3);
	    
	    int i = 0;
		
		try{
			br = new BufferedReader(new FileReader(csvFile));
			PrintWriter writer = new PrintWriter("./SampleTweets/tweets_us_nps_2016_bfyannotated.csv");
			while((line = br.readLine()) != null){
				
				//use semicolon as separator
				String[] tweet = line.split(csvSplitBy);
				
				// Disambiguate the content and retrieve Babelfy annotations
	            List<SemanticAnnotation> babelfyAnnotations = new Babelfy(prs).babelfy(a.removeUrl(tweet[5]), Language.EN);
	            
	            String TwitterAnnotations = "";
	            for (SemanticAnnotation ann : babelfyAnnotations) {
				    if(ann.getDBpediaURL() != null){
				    	TwitterAnnotations = TwitterAnnotations + ann.getDBpediaURL() + "\t";
						//System.out.println(ann.getDBpediaURL() + ann.getScore());
				    }
				}
	            
	            //Print output to a file 
	            writer.println(tweet[0]+";"+tweet[1]+";"+tweet[2]+";"+tweet[3]+";"+tweet[4]+";"+tweet[5]+";"+tweet[6]+";"+tweet[7]+";"+TwitterAnnotations);
	            System.out.println("Tweet " +i);
	            i+=1; 
			}
			writer.close();
		}catch (FileNotFoundException e){
			e.printStackTrace();
		}catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Finished");
	}
	public String removeUrl(String commentstr)
    {
        String urlPattern = "((https?|ftp|gopher|telnet|file|Unsure|http):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern p = Pattern.compile(urlPattern,Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(commentstr);
        int i = 0;
        while (m.find()) {
            commentstr = commentstr.replaceAll(m.group(i),"").trim();
            i++;
        }
        return commentstr;
    }

}
