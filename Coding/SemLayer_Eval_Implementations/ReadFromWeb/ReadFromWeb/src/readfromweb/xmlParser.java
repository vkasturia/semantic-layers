/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package readfromweb;

/**
 *
 * @author vaibhav
 */
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

public class xmlParser {
   public void xmlReader(File inputFile, String beginDate, String endDate){
      
     xmlParser xParser = new xmlParser();
     
     NYTCorpusExist CorpusChecker = new NYTCorpusExist();
     
      try {	
         //File inputFile = new File("/Users/vaibhav/Desktop/input.txt");
         DocumentBuilderFactory dbFactory 
            = DocumentBuilderFactory.newInstance();
         DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
         Document doc = dBuilder.parse(inputFile);
         
         doc.getDocumentElement().normalize();
         
         System.out.println("Root element :" 
            + doc.getDocumentElement().getNodeName());
         
         NodeList nList = doc.getElementsByTagName("item");
         System.out.println("----------------------------");
         for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            //System.out.println("\nSerial No. :" + nNode.getNodeName()+ " " + temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
               Element eElement = (Element) nNode;
               String pubDate = eElement.getElementsByTagName("pubDate").item(0).getTextContent();
               String formatted_pubDate = xParser.DateFormatter(pubDate);
               boolean lies_in_range = xParser.dateRangeQuery(formatted_pubDate, beginDate, endDate);
               if(lies_in_range == true){            
                  System.out.println("\nTitle : " + eElement.getElementsByTagName("title").item(0).getTextContent());
                  System.out.println("Link : "+ eElement.getElementsByTagName("link").item(0).getTextContent());
                  System.out.println("Description : "+ eElement.getElementsByTagName("description").item(0).getTextContent());
                  System.out.println("Publication Date : "+ eElement.getElementsByTagName("pubDate").item(0).getTextContent());
                  
                  String title = eElement.getElementsByTagName("title").item(0).getTextContent();
                  boolean existsInCorpus = CorpusChecker.sparqlTest(title);
                  System.out.println("Exists in Corpus: " + existsInCorpus);
                  
               }
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
   
   public String DateFormatter(String pubDate){
       String formattedDate = "";
       String[] parts = pubDate.split(", ");
       String part2 = parts[1];
       DateFormat formatter = new SimpleDateFormat("dd MMM yyyy HH:mm:ss Z");
       
       try{
           //Change German Format months to English Format months
           if (part2.contains("Mrz")) part2 = part2.replace("Mrz", "Mar");
           if (part2.contains("Mai")) part2 = part2.replace("Mai", "May");
           if (part2.contains("Okt")) part2 = part2.replace("Okt", "Oct");
           if (part2.contains("Dez")) part2 = part2.replace("Dez", "Dec");
        
           Date date = (Date)formatter.parse(part2);
           
           Calendar cal = Calendar.getInstance();
           cal.setTime(date);
           formattedDate = cal.get(Calendar.DATE) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.YEAR);
           
           //Can uncomment the next line to check whether Publication date is getting formatted correctly
           //System.out.println("formattedDate : " + formattedDate);
           
           return formattedDate;
          }catch(Exception ex){
              System.out.println(ex);
          }
       return formattedDate;
   }
   
   public boolean dateRangeQuery(String formatted_pubDate, String beginDate, String endDate){
         boolean lies_in_range = false;
         DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
         try{
              Date pubDate = (Date)formatter.parse(formatted_pubDate);
              Date fromDate = (Date)formatter.parse(beginDate);
              Date toDate = (Date)formatter.parse(endDate);
              
              if(pubDate.after(fromDate) && pubDate.before(toDate)) lies_in_range = true;
              
            }catch(Exception ex){
               System.out.println(ex);
            }
         return lies_in_range;
   }
}
