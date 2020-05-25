package resultsretrieverandchecker;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author vaibhav
 */
public class CorpusResultsMatcher {
    public int commonResults(List<String> listA, List<String> listB){
        
        //Create a list to retain all the titles which are common to NYT Corpus and Google or Bing Search Engine 
        List<String> Common = new ArrayList<String>(listA);
        Common.retainAll(listB);
        
        //Get the uncommon elements of the Search Engine List
        listB.removeAll(listA);
        System.out.println("\nUncommon elements of Search Engine:\n");
        listB.forEach(System.out::println);
        System.out.println();
        
        //Return the number of common elements to both the Corpus and Search Engine
        return Common.size();
    }
}
