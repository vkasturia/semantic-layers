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

package twittercomparator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Vaibhav Kasturia <kasturia at l3s.de>
 */
public class TwitterComparator {

    public static void main(String[] args) throws IOException {
        
        TwitterComparator twitterComparator = new TwitterComparator(); 
        
        String csvFile1 = "/Users/vaibhav/Desktop/tweets_us_nps_2016/tweets_us_nps_2016_ANNOTATED.csv";
        String csvFile2 = "/Users/vaibhav/Desktop/tweets_us_nps_2016/tweets_us_nps_2016_bfyannotated.csv";
        
        BufferedReader br = null;
        
        
        String line = "";
        String csvSplitBy = ";";
        String annotationsSplitBy = "\t";
        
        int sameListCount = 0;
        int totaldiffListCount = 0;
        int AsubsetB = 0;
        int BsubsetA = 0;
        int commonEntitiesListCount = 0;
        
        
        List<List<String>> annotationsLists = new ArrayList<>();
        List<List<String>> annotationsLists2 = new ArrayList<>();
        
        br = new BufferedReader(new FileReader(csvFile1));
        while ((line = br.readLine()) != null) {
            //use semicolon as separator
            String[] tweet = line.split(csvSplitBy, -1);

            //Store the annotations in a list
            List<String> felAnnotations = new ArrayList();
            String annotationLine = tweet[8];
            
            String[] annotationsinLine = annotationLine.split(annotationsSplitBy);
            for(String annotations: annotationsinLine){
                felAnnotations.add(annotations);
            }
            annotationsLists.add(felAnnotations);
        }
        
        br = new BufferedReader(new FileReader(csvFile2));
        while ((line = br.readLine()) != null) {
            //use semicolon as separator
            String[] tweet = line.split(csvSplitBy, -1);

            //Store the annotations in a list
            String annotationLine = tweet[8];
            List<String> babelfyAnnotations = new ArrayList();
      
            String[] annotationsinLine = annotationLine.split(annotationsSplitBy);
            for(String annotations: annotationsinLine){
                annotations = annotations.replaceAll("http://dbpedia.org/resource/", "");
                babelfyAnnotations.add(annotations);
            }
            annotationsLists2.add(babelfyAnnotations);
        }
        
        for(int i = 0; i <annotationsLists.size(); i++){
            List<String> felList = annotationsLists.get(i);
            List<String> babelfyList = annotationsLists2.get(i);
           
            if(felList.equals(babelfyList)) 
                sameListCount += 1;
            if(twitterComparator.intersection(felList, babelfyList).isEmpty() && felList.isEmpty()== false && babelfyList.isEmpty() == false)
               totaldiffListCount += 1;
            if(felList.containsAll(babelfyList)) 
                BsubsetA +=1;
            if(babelfyList.containsAll(felList))
                AsubsetB +=1;
            List<String> unionList = twitterComparator.union(felList, babelfyList);
            List<String> intersectionList = twitterComparator.intersection(felList, babelfyList);
            if(unionList.size() > intersectionList.size() && intersectionList.size() > 0)
                commonEntitiesListCount += 1;
        }
        
        System.out.println("Tweets with exactly same entities: Number = " + sameListCount + "; Percentage = " + ((double)sameListCount/annotationsLists.size())*100);
        System.out.println("Tweets with totally different entities: Number = " + totaldiffListCount + "; Percentage = " + ((double)totaldiffListCount/annotationsLists.size())*100);
        System.out.println("Tweets with Babelfy annotations subset of FEL : Number = " + BsubsetA + "; Percentage = " + ((double)BsubsetA/annotationsLists.size())*100);
        System.out.println("Tweets with FEL annotations subset of Babelfy : Number = " + AsubsetB + "; Percentage = " + ((double)AsubsetB/annotationsLists.size())*100);
        System.out.println("Tweets with common entities: Number = " + commonEntitiesListCount + "; Percentage = " + ((double)commonEntitiesListCount/annotationsLists.size())*100);
          
    }
    
    public <T> List<T> union(List<T> list1, List<T> list2) {
        Set<T> set = new HashSet<T>();

        set.addAll(list1);
        set.addAll(list2);

        return new ArrayList<T>(set);
    }

    public <T> List<T> intersection(List<T> list1, List<T> list2) {
        List<T> list = new ArrayList<T>();

        for (T t : list1) {
            if (list2.contains(t)) {
                list.add(t);
            }
        }

        return list;
    }

    public List<String> intersect(List<String> A, List<String> B) {
        List<String> rtnList = new LinkedList<>();
        for (String dto : A) {
            if (B.contains(dto)) {
                rtnList.add(dto);
            }
        }
        return rtnList;
    }
    
}    
