/*
Copyright 2017-2022 Vaibhav Kasturia <vbh18kas@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import it.uniroma1.lcl.babelfy.commons.BabelfyParameters;
import it.uniroma1.lcl.babelfy.commons.annotation.SemanticAnnotation;
import it.uniroma1.lcl.babelfy.core.Babelfy;
import it.uniroma1.lcl.jlt.util.Language;

public class NYT_LayerCreator_Sequential {


    /**
     * @param args the command line arguments
     * @throws java.io.UnsupportedEncodingException
     * @throws java.io.FileNotFoundException
     * @throws InterruptedException 
     */
    public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException, IOException, InterruptedException, ParseException {
    int blanknode = 1000000000;
    for(int y=1991; y<1994; y++){
    	List<List<List<String>>> textsToAnnotateGroup = new ArrayList<List<List<String>>>();
    	List<List<String>> textsToAnnotate;
		List<String> textdetails;
		
		PrintWriter writer = new PrintWriter("./annotated_texts/"+Integer.toString(y)+"_Annotated.n3");
        writer.println("@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .");
        writer.println("@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .");
        writer.println("@prefix dc: <http://purl.org/dc/terms/> .");
        writer.println("@prefix owa: <http://l3s.de/owa/> .");
        writer.println("@prefix nyt: <http://query.nytimes.com/gst/fullpage.html?res=> .");
        writer.println("@prefix schema: <http://schema.org/> .");
        writer.println("@prefix oae: <http://www.ics.forth.gr/isl/oae/core#> .");
        writer.println();
        
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

        File fileDir = new File("./texts/"+Integer.toString(y));
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

                    contentList.add(content);
                } else { // CONTINUE READING THE LINES OF THIS ARTICLE
                    content.append(str);
                }
                //System.out.println(numOfArticles);
                //System.out.println(contentList);
            }
        }
        System.out.println("Content List Size :" + contentList.size());
        
        String articlecontent = "";
        String articletitle = "";
        String articleurl = "";
        String shortenedurl = "";
        String articledate ="";
        String article_titlecontent_combined ="";
        
		textsToAnnotate = new ArrayList<List<String>>();
        for (int k = 0; k < contentList.size(); k++) {
			// Read input from contentList
        	textdetails = new ArrayList<String>();
			StringBuilder sb = contentList.get(k);
			Article article = parseArticle(sb);
			if (article != null) {
				articleurl = article.getUrl();
				shortenedurl = articleurl.replace("http://query.nytimes.com/gst/fullpage.html?res=", "");
				articletitle = article.getTitle();
				articledate = article.getPubDate();
				
				DateFormat df = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
		        Date d = df.parse(articledate);
		        df = new SimpleDateFormat("yyyy-MM-dd");
		        
				articlecontent = article.getContent();
				article_titlecontent_combined = articletitle;
				article_titlecontent_combined = article_titlecontent_combined.concat(articlecontent);
				
				System.out.println(shortenedurl);
				//System.out.println(articleurl);
				//System.out.println(articletitle);
				//System.out.println(articledate);
				//System.out.println(articlecontent);
				//System.out.println(article_titlecontent_combined);
				
				textdetails.add(shortenedurl);
				textdetails.add(articletitle);
				textdetails.add(df.format(d));
				textdetails.add(article_titlecontent_combined);
				textsToAnnotate.add(textdetails);
			}
		}
		// System.out.println(textsToAnnotate);

		textsToAnnotateGroup.add(textsToAnnotate);

	    //for (List<String> d:textsToAnnotateGroup){
	    //	System.out.println("Now printing" + d);
	    //}
		
		// Set Babelfy parameters
		BabelfyParameters prs = new BabelfyParameters();
		prs.setContextSize(3);
		
		//Annotate DummyTexts to reduce time at a later stage
	    List<SemanticAnnotation> babelfyAnnotations = new Babelfy(prs).babelfy(dummyText, Language.EN);
	    List<SemanticAnnotation> babelfyAnnotations2 = new Babelfy(prs).babelfy(dummyText2, Language.EN);
				
		
	    int i = 0;
		long avgTime = 0;
		
		long mainStartTime = System.nanoTime();
		List<SemanticAnnotation> babelfyAnnotations3 = null;
		for (List<String> textdetails1 : textsToAnnotate) {
             
			long startTime = System.nanoTime();
			
			writer.println();
			writer.println("nyt:"+textdetails1.get(0)+"\t"+"rdf:type"+"\t"+"owa:ArchivedDocument ;");
			writer.println("\t"+"dc:title"+"\t"+'"'+textdetails1.get(1)+'"'+"@en ;");
			writer.println("\t"+"dc:date"+"\t"+'"'+textdetails1.get(2)+'"'+"^^xsd:date .");
			writer.println();

			// Disambiguate the text and retrieve Babelfy annotations
			try{
				babelfyAnnotations3 = new Babelfy(prs).babelfy(textdetails1.get(3), Language.EN);
			}catch(Exception e){
				e.printStackTrace();
			}
            
            //Get the annotation time
            long estimatedTime = System.nanoTime() - startTime;
            
            if (i > 1){
              avgTime += (estimatedTime/1000000);
            }
            i+=1;
            
            if ((i+1) % 10000 == 0) {
                System.out.println("- Annotated article number " + (i+1));
            }

            //Print the Annotation time
            //System.out.println("\nAnnotation time" + estimatedTime/1000000 + "ms");
            //System.out.println();
            
            
			// Print annotations
			for (SemanticAnnotation ann : babelfyAnnotations3) {
			    if(ann.getDBpediaURL() != null){
			    	writer.println("nyt:"+textdetails1.get(0)+"\t"+"schema:mentions"+"\t"+"_:e"+blanknode+" .");
					writer.println("_:e"+blanknode+"\t"+"rdf:type"+"\t"+"oae:Entity ;");
					writer.println("\t"+"oae:confidence"+"\t"+'"'+ann.getScore()+'"'+"^^xsd:double ;");
					String entityName = textdetails1.get(3).substring(ann.getCharOffsetFragment().getStart(), ann.getCharOffsetFragment().getEnd() + 1);
					entityName = entityName.replace("\"", "&quot;");
					writer.println("\t"+"oae:detectedAs"+"\t"+'"'+ entityName +'"'+" ;");
					String dbpediaURL = ann.getDBpediaURL();
					dbpediaURL = dbpediaURL.replaceAll("\\\\", "%5C");
					writer.println("\t"+"oae:hasMatchedURI"+"\t"+"<"+dbpediaURL+"> .");
					blanknode+=1;
					
			    	//System.out.println(
					//  	text.substring(ann.getCharOffsetFragment().getStart(), ann.getCharOffsetFragment().getEnd() + 1)
					//			+ "\tDBPedia URL: " + ann.getDBpediaURL() + "\tScore" + ann.getScore() + "\tCoherence Score" + ann.getCoherenceScore());
			    }
			    
			}
      
		}
		System.out.println("\nAverage Time for " + (i-2) + "Texts: " + avgTime/(i-2) + "ms");

		long mainEstimatedTime = System.nanoTime() - mainStartTime;
		System.out.println("\nTotal Execution Time " + mainEstimatedTime/1000000 + "ms");
		writer.close();
      }
      System.out.println("FINISHED");
	}

    public static Article parseArticle(StringBuilder articleContent) throws IOException {

        
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