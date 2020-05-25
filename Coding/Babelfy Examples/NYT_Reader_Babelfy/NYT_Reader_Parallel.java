import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import it.uniroma1.lcl.babelfy.commons.BabelfyParameters;
import it.uniroma1.lcl.babelfy.commons.annotation.SemanticAnnotation;
import it.uniroma1.lcl.babelfy.core.Babelfy;
import it.uniroma1.lcl.jlt.util.Language;

public class NYT_Reader_Parallel {

    public static final String CORPUS_YEAR = "./texts/1990";
    public static int numOfArticles = 0;

    /**
     * @param args the command line arguments
     * @throws java.io.UnsupportedEncodingException
     * @throws java.io.FileNotFoundException
     * @throws InterruptedException 
     */
    public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException, IOException, InterruptedException {

    	int num_of_threads = 20;
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
				
		
		
        List<StringBuilder> contentList = new ArrayList<StringBuilder>();
        StringBuilder content = new StringBuilder();

        File fileDir = new File(CORPUS_YEAR);
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(fileDir), "UTF8"))) {
            String str;
            while ((str = in.readLine()) != null) { // START READING THE BIG FILE LINE BY LINE
                if (str.startsWith("<?")) { // IN THIS LINE, A NEW ARTICLE STARTS
                    content = new StringBuilder();
                    content.append(str);
                } else if (str.startsWith("</nitf")) { // IN THIS LINE, THE ARTICLE ENDS (SO, NOW I HAVE THE WHOLE ARTICLE CONTENT)
                    content.append(str);

                    numOfArticles++;
                    contentList.add(content);
//                    Article article = parseArticle(content); // READ THE ARTICLE'S CONTENT
//                    if (article != null) {
//                        System.out.println("URL: " + article.getUrl());
//                        System.out.println("HEADLINE: " + article.getHeadline());
//                        System.out.println("PUB DATE: " + article.getPubDate());
//                        System.out.println("TITLE: " + article.getTitle());
//                        System.out.println("CONTENT: " + article.getContent());
//                        System.out.println("----");
//                    }

                } else { // CONTINUE READING THE LINES OF THIS ARTICLE
                    content.append(str);
                }
                //System.out.println(numOfArticles);
                //System.out.println(contentList);
            }
        }
        System.out.println(numOfArticles);
        System.out.println("Content List Size :" + contentList.size());
        
        String text1 = "";
		int j = 0;
		int k = 0;
		for (int i = 0; i < num_of_threads; i++) {
			textsToAnnotate = new ArrayList<String>();
			for (k = (contentList.size() / num_of_threads) * j; k < (contentList.size() / num_of_threads) * (j + 1); k++) {
				// Read input from contentList
				StringBuilder sb = contentList.get(k);
				Article article = parseArticle(sb);
				if(article != null){
					text1 = article.getContent();
					//System.out.println(text1);
					textsToAnnotate.add(text1);
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

    public static Article parseArticle(StringBuilder articleContent) throws IOException {

        if (numOfArticles % 10000 == 0) {
            System.out.println("- Reading article number " + numOfArticles);
        }

        HTMLTag tagger = new HTMLTag(articleContent.toString());

        int posType = tagger.getFirstTagIndexContains("classifier", "type=\"types_of_material\"");
        ArrayList<String> typesOfMaterial = new ArrayList<>();
        while (posType != -1) {
            String typ = tagger.getFirstTagDataContains("classifier", "type=\"types_of_material\"", posType - 1);
            typ = typ.replace("\"", "&quot;").replace("'", "&apos;");
            typesOfMaterial.add(typ.trim());
            posType = tagger.getFirstTagIndexContains("classifier", "type=\"types_of_material\"", posType + 1);
        }

        // Skip publications that actually are not articles
        if (typesOfMaterial.contains("Caption") || typesOfMaterial.contains("Caption&lt;br&gt;") || typesOfMaterial.contains("Editors' Note") || typesOfMaterial.contains("Editor's Note") || typesOfMaterial.contains("Schedule") || typesOfMaterial.contains("Letter") || typesOfMaterial.contains("Summary") || typesOfMaterial.contains("Paid Memorial Notice") || typesOfMaterial.contains("Paid Death Notice") || typesOfMaterial.contains("Correction")) {
            return null;
        }

        String content = tagger.getFirstTagDataContains("block", "class=\"full_text\"");  // Read the article's main content
        //content = HTMLTag.removeTags(content); //remove all tags
        if (content == null) {
            content = "";
        }
        content = content.trim();

        String headline = tagger.getFirstTagData("hedline");  // Read the article's headline
        //content = HTMLTag.removeTags(content); //remove all tags
        if (headline == null) {
            headline = "";
        }
        headline = headline.trim();

        //Skip very small articles (that actually are not articles)
        if (content.length() < 100) {
            return null;
        }

        String title = tagger.getFirstTagData("title"); // Read the article's title
        if (title == null) {
            title = "";
        }
        title = title.replace("\n", " ").replace("\r", " ").replace("\t", " ").replace("\"", "&quot;").replace("'", "&apos;").trim();
        if (title.equals("QUOTATION OF THE DAY")) { // Skip this publication (it's not an article)
            return null;
        }

        String pubDate = "";
        String url = "";
        String pubdatastr = tagger.getFirstTagContent("pubdata"); // Read the article's publication 
        if (pubdatastr != null) {
            pubDate = HTMLTag.getContentAttribute("date.publication", pubdatastr).trim(); // Read the article's publication
            url = HTMLTag.getContentAttribute("ex-ref", pubdatastr).trim(); // Read the article's URL
        }

        return new Article(url, title, headline, content, pubDate);

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
            
            if ((i+1) % 10000 == 0) {
                System.out.println(threadName1 + "- Annotated article number " + (i+1));
            }

            //Print the Annotation time
            //System.out.println("\n "+ threadName1 +" ;Annotation time" + estimatedTime/1000000 + "ms for text " + (i+1));
            //System.out.println();
            
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
