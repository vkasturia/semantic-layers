/*
Copyright 2017-2022 Vaibhav Kasturia <vbh18kas@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import it.uniroma1.lcl.babelfy.commons.BabelfyParameters;
import it.uniroma1.lcl.babelfy.commons.annotation.SemanticAnnotation;
import it.uniroma1.lcl.babelfy.core.Babelfy;
import it.uniroma1.lcl.jlt.util.Language;

import java.io.BufferedReader;
import java.io.FileReader;

public class BabelfyExample {
	
	public static void main(String[] args) throws IOException, InterruptedException {
		
		int num_of_threads = 10;
		List<List<String>> textsToAnnotateGroup = new ArrayList<List<String>>();
		ThreadDemo[] threadlist = new ThreadDemo[num_of_threads];
		List<String> textsToAnnotate;
		
		//Creating two Dummy Texts which would be annotated but discarded later
		
		String dummyText = "The hair has gone gray and the boxer's shoulders are shells under the awkward jacket now."+ 
                "The booming voice that had roared for justice in the 1960's became a gentle echo in his lonely cell long ago."+
	            "But amid the many perils of South Africa's future, one thing seemed clear yesterday: the years of imprisonment had not broken Nelson R. Mandela."+
                "Emerging from Victor Verster Prison near Cape Town, the 71-year-old black nationalist leader - who had not been seen or heard publicly for almost"+
	             "26 years - raised his fist in a triumphant salute and spoke to a sea of cheering followers of their dignity and his dreams of ''peace, democracy"+
                 "and freedom for all'' in a new nation without apartheid.";

       String dummyText2 = "He was the reluctant Gandhi, content on the political sidelines as his mother and brother strained to command an immense and diverse nation."+
                "But his brother's death in 1980 and his mother's assassination in 1984 forced Rajiv Gandhi, the untested and unheralded heir, to gather up the Gandhi mantle."+
	            "His career was turbulent and stained by sectarian bloodshed, and he finally lost the family fief -- the Prime Minister's post -- in 1989."+
                "In recent months, seeking a comeback in Indian politics, he campaigned with vigor, mixing comfortably with the people. It was this taste for proximity that"+ 
	            "led Mr. Gandhi, the last of his fabled political line, toward the deadly trap -- a bomb at a campaign rally";
		
		
		String text1 = "";
		int j = 0;
		int k = 0;
		for (int i = 0; i < num_of_threads; i++) {
			textsToAnnotate = new ArrayList<String>();
			for (k = (1000 / num_of_threads) * j; k < (1000 / num_of_threads) * (j + 1); k++) {
				// Read input from File
				BufferedReader br = new BufferedReader(new FileReader("./texts/threadtexts/1000files/s" + (k+1) + ".txt"));
				try {
					StringBuilder sb = new StringBuilder();
					String line = br.readLine();

					while (line != null) {
						sb.append(line);
						sb.append("\n");
						line = br.readLine();
					}
					text1 = sb.toString();
					textsToAnnotate.add(text1);
				} finally {
					br.close();
				}
			}
			//System.out.println(textsToAnnotate);
			
			textsToAnnotateGroup.add(textsToAnnotate);
			j+=1;
		}
	    //for (List<String> d:textsToAnnotateGroup){
	    //	System.out.println("Now printing" + d);
	    //}
		
		// Set Babelfy parameters
		BabelfyParameters prs = new BabelfyParameters();
		prs.setContextSize(3);
		
		//Annotate DummyTexts to reduce time at a later stage
		List<SemanticAnnotation> babelfyAnnotations = new Babelfy(prs).babelfy(dummyText, Language.EN);
		List<SemanticAnnotation> babelfyAnnotations2 = new Babelfy(prs).babelfy(dummyText2, Language.EN);
		
		
		long startTime = System.nanoTime();

		int  p = 0;
		for( List<String> d: textsToAnnotateGroup ){
			
			threadlist[p] = new ThreadDemo("Thread #" + p, prs, d);
			threadlist[p].start();
			System.out.println("Started Thread:"+p);
			p+=1;
		}
		
		for(int x = 0; x < num_of_threads; x++){
			threadlist[x].join();
		}
		
		long estimatedTime = System.nanoTime() - startTime;
		System.out.println("Total execution time for all threads: " + estimatedTime/1000000 +" ms");
		
		
	}
}
class ThreadDemo extends Thread{
	private BabelfyParameters prs1;
	private List<String> textsToAnnotate1;
	private String threadName1;
	ThreadDemo(String threadName, BabelfyParameters prs, List<String> textsToAnnotate){
		threadName1 = threadName;
		prs1 = prs;
		textsToAnnotate1 = textsToAnnotate;
	}
	public void run(){
		int i = 0;
		long avgTime = 0;
		
		for (String text : textsToAnnotate1) {
             
			long startTime = System.nanoTime();
			
			// Disambiguate the text and retrieve Babelfy annotations
            List<SemanticAnnotation> babelfyAnnotations = new Babelfy(prs1).babelfy(text, Language.EN);
 
            //Get the annotation time
            long estimatedTime = System.nanoTime() - startTime;
            
            if (i > 1){
              avgTime += (estimatedTime/1000000);
            }
            i+=1;
            
            //Print the Annotation time
            System.out.println("\n "+ threadName1 +" ;Annotation time" + estimatedTime/1000000 + "ms for text " + (i+1));
            System.out.println();
            
			// Print annotations
			for (SemanticAnnotation ann : babelfyAnnotations) {
			    if(ann.getDBpediaURL() != null){
				    //System.out.println(
					//  	text.substring(ann.getCharOffsetFragment().getStart(), ann.getCharOffsetFragment().getEnd() + 1)
					//			+ "\tDBPedia URL: " + ann.getDBpediaURL() + "\tScore" + ann.getScore() + "\tCoherence Score" + ann.getCoherenceScore());
			    }
			}
      
		}
		System.out.println("\n "+ threadName1 + " ;Average Time for " + (i-2) + "Texts: " + avgTime/(i-2) + "ms");
	}
}