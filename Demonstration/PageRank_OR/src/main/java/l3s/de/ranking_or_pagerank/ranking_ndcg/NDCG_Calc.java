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

package l3s.de.ranking_or_pagerank.ranking_ndcg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Vaibhav Kasturia <kasturia at l3s.de>
 *         
 *         Program to Calculate NDCG for all ranking files and store output in a
 *         CSV File
 */

public class NDCG_Calc {
	public void ndcg_table_creator(int query_num) throws FileNotFoundException, IOException {
		NDCG_Calc ndcg_calculator = new NDCG_Calc();

		// Ground Truth file to calculate IDCG
		String groundTruth_csvFile = "./ground_truth/" + query_num + "_NEW_SPARQL.csv";

		// Data structure to store DCG for all ranking files
		List<List<Double>> DCG_ListofLists = new LinkedList<>();

		String outputLine = "";

		BufferedReader br = null;
		String line = "";

		// Specify the separator used to split the rest of the csv files
		String restFilesSplitBy = "; ";

		Map<String, Double> groundTruth_Map = new HashMap<String, Double>();

		br = new BufferedReader(new FileReader(groundTruth_csvFile));
		br.readLine();
		while ((line = br.readLine()) != null) {
			String[] article_info = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
			groundTruth_Map.put(article_info[0], Double.parseDouble(article_info[3]));
		}

		Map<String, Double> groundTruth_sortedMap = ndcg_calculator.sortHashMapByValues(groundTruth_Map);

		// Calculate IDCG using sorted ground truth map
		List<Double> IDCG_List = new LinkedList<>();
		IDCG_List = ndcg_calculator.calculateDCG(groundTruth_sortedMap);

		// Files to calculate DCG
		File dir = new File("./results/" + query_num + "/");

		File[] directoryListing = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return !name.equals(".DS_Store");
			}
		});

		if (directoryListing != null) {
			// Create a CSV File for writing NDCG Outputs
			PrintWriter writer = new PrintWriter("./ndcg/NDCG_Output" + query_num + ".csv");
			outputLine += "NDCG";

			int u = 0;

			for (File child : directoryListing) {
				String b = ", " + child.getName();
				outputLine += b;
			}
			outputLine += "\n";
			for (File child : directoryListing) {
				// Store file input in Article_Ranking_map
				Map<String, Double> article_ranking_map = new LinkedHashMap<>();

				br = new BufferedReader(new FileReader(child));
				while ((line = br.readLine()) != null) {
					String[] article_info = line.split(restFilesSplitBy);
					if (groundTruth_Map.get(article_info[0]) != null) {
						article_ranking_map.put(article_info[0], groundTruth_Map.get(article_info[0]));
					}
				}

				List<Double> DCG_List = new LinkedList<>();
				DCG_List = ndcg_calculator.calculateDCG(article_ranking_map);
				DCG_ListofLists.add(DCG_List);

			}

			String[] NDCG_Position = new String[] { "@5", "@10", "@20", "end" };

			for (int y = 0; y < 4; y++) {
				outputLine += NDCG_Position[y];
				for (List<Double> CurrentList : DCG_ListofLists) {
					Double value = (CurrentList.get(y) / IDCG_List.get(y));
					value = (double) Math.round(value * 1000d) / 1000d;
					String c = ", " + value;
					outputLine += c;
				}
				outputLine += "\n";
			}

			writer.println(outputLine);

			writer.close();
		} else {
			// In case where directory is not correctly input
			// Print warning and terminate the program
			if (!dir.isDirectory()) {
				System.out.println("Error in directory path input");
				System.exit(1);
			}
		}
	}

	public static LinkedHashMap<String, Double> sortHashMapByValues(Map<String, Double> passedMap) {
		List<String> mapKeys = new ArrayList<>(passedMap.keySet());
		List<Double> mapValues = new ArrayList<>(passedMap.values());
		Collections.sort(mapValues, Collections.reverseOrder());
		Collections.sort(mapKeys, Collections.reverseOrder());

		LinkedHashMap<String, Double> sortedMap = new LinkedHashMap<>();

		Iterator<Double> valueIt = mapValues.iterator();
		while (valueIt.hasNext()) {
			Double val = valueIt.next();
			Iterator<String> keyIt = mapKeys.iterator();

			while (keyIt.hasNext()) {
				String key = keyIt.next();
				Double comp1 = passedMap.get(key);
				Double comp2 = val;

				if (comp1.equals(comp2)) {
					keyIt.remove();
					sortedMap.put(key, val);
					break;
				}
			}
		}
		return sortedMap;
	}

	public List<Double> calculateDCG(Map<String, Double> article_ranking_map) {

		List<Double> DCG = new LinkedList<>();

		double[] idcg_values = new double[] { 0, 0, 0, 0 };
		double idcg_endposition = 0;
		int[] position = new int[] { 5, 10, 20, 10000 };

		int z = 1;
		for (int j = 0; j < position.length; j++) {
			for (Map.Entry<String, Double> entry : article_ranking_map.entrySet()) {

				idcg_values[j] += (Math.pow(2, entry.getValue()) - 1.0) * (Math.log(2) / Math.log(z + 1));
				z += 1;
				if (position[j] != 10000) {
					if (z == position[j] + 1) {
						DCG.add(idcg_values[j]);
						z = 1;
						break;
					}
				} else {
					idcg_endposition += (Math.pow(2, entry.getValue()) - 1.0) * (Math.log(2) / Math.log(z + 1));
				}
			}
		}
		DCG.add(idcg_endposition);

		return DCG;
	}
}
