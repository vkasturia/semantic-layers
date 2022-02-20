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

package resultsretrieverandchecker;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Vaibhav Kasturia
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
