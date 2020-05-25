import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import it.uniroma1.lcl.babelfy.commons.BabelfyParameters;
import it.uniroma1.lcl.babelfy.commons.annotation.SemanticAnnotation;
import it.uniroma1.lcl.babelfy.core.Babelfy;
import it.uniroma1.lcl.jlt.util.Language;

import java.io.BufferedReader;
import java.io.FileReader;

public class BabelfyExample2 {
	public static void main(String[] args) throws IOException {
		
		//Keeping ArrayList for the case you want to read input from multiple files 
		ArrayList<String> textsToAnnotate = new ArrayList<String>();
		
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
		
		String text1 = null;
		
		for (int i = 1; i < 1000; i++) {
            //Read input from File
            BufferedReader br = new BufferedReader(new FileReader("./texts/threadtexts/1000files/s"+i+".txt"));
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
	    
		
	    //Read more files and add more inputs if required
	    //textsToAnnotate.add("add text here");
	    
		// Set Babelfy parameters
		BabelfyParameters prs = new BabelfyParameters();
		prs.setContextSize(3);
		
		//Annotate DummyTexts to reduce time at a later stage
		List<SemanticAnnotation> babelfyAnnotations2 = new Babelfy(prs).babelfy(dummyText, Language.EN);
		List<SemanticAnnotation> babelfyAnnotations3 = new Babelfy(prs).babelfy(dummyText2, Language.EN);

		int i = 0;
		long avgTime = 0;
		
		long mainStartTime = System.nanoTime();
		
		for (String text : textsToAnnotate) {
             
			long startTime = System.nanoTime();
			
			// Disambiguate the text and retrieve Babelfy annotations
            List<SemanticAnnotation> babelfyAnnotations = new Babelfy(prs).babelfy(text, Language.EN);
 
            //Get the annotation time
            long estimatedTime = System.nanoTime() - startTime;
            
            if (i > 1){
              avgTime += (estimatedTime/1000000);
            }
            i+=1;
            
            //Print the Annotation time
            System.out.println("\nAnnotation time" + estimatedTime/1000000 + "ms");
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
		System.out.println("\nAverage Time for " + (i-2) + "Texts: " + avgTime/(i-2) + "ms");

		long mainEstimatedTime = System.nanoTime() - mainStartTime;
		System.out.println("\nTotal Execution Time " + mainEstimatedTime/1000000 + "ms");
	}
}
