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

package l3s.de.ranking_or_pagerank.pagerank;

import java.awt.Dimension;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.HypergraphLayout;
import edu.uci.ics.jung.algorithms.scoring.PageRankWithPriors;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.BasicRenderer;
import l3s.de.ranking_or_pagerank.Triple;

/**
 * @author Pavlos Fafalios, Vaibhav Kasturia
 *         
 *         Class to create the Transition Graph
 */
public class GraphCreator {

	public static Set<Triple> triples = new HashSet<>();
	public static SparseGraph stateTransitionGraph;
	public static HashMap<Object, Double> edge2weight = new HashMap<>();;
	public static HashMap<Object, Double> vertex2weight = new HashMap<>();;
	public static HashMap<Object, Double> vertex2score = new HashMap<>();
	public static ArrayList<Object> rankedVertices = new ArrayList<>();
	public static double decayFactor;
	public static int iterations;
	public static double initPageRankValue;

	public Map<String, Double> createGraph(Map<String, Map<String, Double>> docs_entities_entitycount_map,
			Map<String, Map<String, Double>> entity_docs_entitycount_map,
			Map<String, Double> article_time_relativeness_map, Map<String, Double> relEntities_relatednessScore_Map,
			Map<String, List<String>> docs_entities_map, int num_of_entities, String[] entityArray, int query_num, double dFactor)
			throws FileNotFoundException {

		// System.out.println("# Initializing the triples of the graph");
		iniTriples(docs_entities_entitycount_map);

		// System.out.println("# Creating the State Transition Graph");
		createSTG(article_time_relativeness_map, docs_entities_entitycount_map, entity_docs_entitycount_map,
				relEntities_relatednessScore_Map, docs_entities_map, num_of_entities, entityArray);
		// System.out.println("# STG num of vertices: " +
		// stateTransitionGraph.getVertexCount());
		// System.out.println("# STG num of edges: " +
		// stateTransitionGraph.getEdgeCount());

		initPageRankValue = (double) (1.0 / (double) stateTransitionGraph.getVertexCount());
		iterations = 30;
		decayFactor = dFactor;

		
		// System.out.println("# Running PageRank...");
		PageRankWithPriors pr = new PageRankWithPriors(stateTransitionGraph, new EdgeWeight(edge2weight),
				new JumpToNode(vertex2weight), decayFactor);
		pr.setMaxIterations(iterations);
		pr.initialize();
		pr.evaluate();

		Map<String, Double> articleUrl_score_map = new LinkedHashMap<>();

		// System.out.println("# Ranked vertices with scores:");
		getScoresAndRanking(pr); // vertex2score and rankedVertices are filled

		for (Object vertex : rankedVertices) {
			if (vertex.toString().contains("http://query.nytimes.com/gst/fullpage.html?res=")) {
				articleUrl_score_map.put(vertex.toString(), vertex2score.get(vertex));
			}
		}

//        int counter = 1;
//        for (Object vertex : rankedVertices) {
//            System.out.println(counter++ + ". " + vertex.toString() + " (" + vertex2score.get(vertex) + ")");
//            if (vertex.toString().contains("http://query.nytimes.com/gst/fullpage.html?res=")) {
//                writer.println(vertex.toString() + "; " + vertex2score.get(vertex));
//            }
//        }
//        System.out.println("-----------------");
//
//        writer.close();
		// GRAPH VISUALIZATION //
		// visualize();

		triples = new HashSet<>();
		stateTransitionGraph = new SparseGraph();
		edge2weight = new HashMap<>();
		vertex2weight = new HashMap<>();
		vertex2score = new HashMap<>();
		rankedVertices = new ArrayList<>();
		decayFactor = 0.0;
		iterations = 0;
		initPageRankValue = 0;

		return articleUrl_score_map;
	}

	public static void visualize() {
		HypergraphLayout<Integer, Character> l = new HypergraphLayout<Integer, Character>(stateTransitionGraph,
				FRLayout.class);

		VisualizationViewer<Integer, Character> v = new VisualizationViewer<Integer, Character>(l,
				new Dimension(1400, 800));
		v.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
		v.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller());
		BasicRenderer b = new BasicRenderer<Integer, Character>();
		v.setRenderer(b);

		JFrame frame = new JFrame("Example Graph");
		frame.setSize(new Dimension(1500, 900));
		frame.getContentPane().add(v);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

	}

	public static void getScoresAndRanking(PageRankWithPriors pr) {
		ArrayList<Double> scores = new ArrayList<Double>();
		HashMap<Double, HashSet<Object>> maps = new HashMap<Double, HashSet<Object>>();
		vertex2score = new HashMap<Object, Double>();
		for (Object vertex : stateTransitionGraph.getVertices()) {

			double score = (Double) pr.getVertexScore(vertex);
			vertex2score.put(vertex, score);
			if (scores.contains(score)) {
				HashSet<Object> temp = maps.get(score);
				temp.add(vertex);
				maps.put(score, temp);
			} else {
				scores.add(score);
				HashSet<Object> temp = new HashSet<Object>();
				temp.add(vertex);
				maps.put(score, temp);
			}
		}
		Collections.sort(scores);
		rankedVertices = new ArrayList<Object>();
		for (int j = scores.size() - 1; j >= 0; j--) {
			double cur_score = scores.get(j);
			HashSet<Object> vv = maps.get(cur_score);
			for (Object vertex : vv) {
				rankedVertices.add(vertex);
			}
		}
	}

	public static void createSTG(Map<String, Double> article_time_relativeness_map,
			Map<String, Map<String, Double>> docs_entities_entitycount_map,
			Map<String, Map<String, Double>> entity_docs_entitycount_map,
			Map<String, Double> relEntities_relatednessScore_Map, Map<String, List<String>> docs_entities_map,
			int num_of_entities, String[] entityArray) {
		stateTransitionGraph = new SparseGraph();

		Map<String, List<String>> eoi_relEntity_map = new LinkedHashMap();

		int n = 1;
		for (Triple t : triples) {
			Object subject = t.getSubject();
			Object predicate = t.getPredicate();
			Object object = t.getObject();

			// Creating edge from the document node to the entity node
			String edge = predicate + "_" + n;
			stateTransitionGraph.addEdge(edge, new Pair(subject, object), EdgeType.DIRECTED);

			// Creating edge from the entity node to the document node
			String inverse_edge = predicate + "_inv_" + n;
			stateTransitionGraph.addEdge(inverse_edge, new Pair(object, subject), EdgeType.DIRECTED);

			n += 1;
		}

		for (Map.Entry<String, List<String>> entry : docs_entities_map.entrySet()) {
			for (int i = 0; i < entityArray.length; i++) {
				if (entry.getValue().contains(entityArray[i])) {
					List<String> RelEntityList = entry.getValue();
					for (int j = 0; j < RelEntityList.size(); j++) {
						if (!Arrays.asList(entityArray).contains(RelEntityList.get(j))) {
							if (eoi_relEntity_map.containsKey(entityArray[i])) {
								List<String> entityList = eoi_relEntity_map.get(entityArray[i]);
								if (!entityList.contains(RelEntityList.get(j))) {
									entityList.add(RelEntityList.get(j));
									eoi_relEntity_map.put(entityArray[i], entityList);
								}
							} else {
								List<String> entityList = new LinkedList();
								entityList.add(RelEntityList.get(j));
								eoi_relEntity_map.put(entityArray[i], entityList);
							}
						}

					}
				}
			}
		}

		for (Map.Entry<String, List<String>> entry : eoi_relEntity_map.entrySet()) {
			System.out.println(entry.getKey() + " - " + entry.getValue());
		}

		int k = 1;

		for (Map.Entry<String, List<String>> entry : eoi_relEntity_map.entrySet()) {
			List<String> entityList = entry.getValue();
			Object subject = entry.getKey();
			for (int i = 0; i < entityList.size(); i++) {
				Object object = entityList.get(i);
				// Creating a directed edge from an EoI node to another entity node
				// Edge is created if the other entity co-occurs with EoI in atleast one
				// document
				String edge = "related" + k;
				stateTransitionGraph.addEdge(edge, new Pair(subject, object), EdgeType.DIRECTED);
				k += 1;
			}
		}

		// Specifying the Vertex Weights for the Random Jumps
		double eoiScore = 1.0 / (double) num_of_entities;
		for (Object vert : stateTransitionGraph.getVertices()) {
			for (int i = 0; i < entityArray.length; i++) {
				if (vert.equals(entityArray[i]))
					vertex2weight.put(vert, eoiScore);
				else
					vertex2weight.put(vert, Double.MIN_VALUE);
			}
		}

		// Specifying edge weights for outgoing edges from the entity vertices
		// If the entity is EoI, then edge weights will be specified differently
		for (Triple t : triples) {
			Object entity_vertex = t.getObject();
			for (int i = 0; i < entityArray.length; i++) {
				if (!Arrays.asList(entityArray).contains(entity_vertex)) {
					Collection<Object> outEdges = stateTransitionGraph.getOutEdges(entity_vertex);
					double sum_entityFrequency = Double.MIN_VALUE;
					for (Object edge : outEdges) {
						Object document_vertex = stateTransitionGraph.getOpposite(entity_vertex, edge);
						double entityFrequency = entity_docs_entitycount_map.get(entity_vertex).get(document_vertex);
						sum_entityFrequency += entityFrequency;
					}
					for (Object edge : outEdges) {
						Object document_vertex = stateTransitionGraph.getOpposite(entity_vertex, edge);
						double entityFrequency = entity_docs_entitycount_map.get(entity_vertex).get(document_vertex);
						double edge_weight = entityFrequency / sum_entityFrequency;
						edge2weight.put(edge, edge_weight);
					}
				} else {
					Collection<Object> outEdges = stateTransitionGraph.getOutEdges(entity_vertex);

					Double p1 = 0.0;
					Double sum_time_relativeness_score = Double.MIN_VALUE;
					Double sum_relatednessScore = Double.MIN_VALUE;

					for (Object edge : outEdges) {
						Object opposite_vertex = stateTransitionGraph.getOpposite(entity_vertex, edge);
						boolean contains = opposite_vertex.toString()
								.contains("http://query.nytimes.com/gst/fullpage.html?res=");
						if (contains == true) {
							Double time_relativeness_score = article_time_relativeness_map
									.get(opposite_vertex.toString());
							sum_time_relativeness_score += time_relativeness_score;
						} else {
							Double relatednessScore = relEntities_relatednessScore_Map.get(opposite_vertex.toString());
							sum_relatednessScore += relatednessScore;
						}
					}
					for (Object edge : outEdges) {
						Object opposite_vertex = stateTransitionGraph.getOpposite(entity_vertex, edge);
						boolean contains = opposite_vertex.toString()
								.contains("http://query.nytimes.com/gst/fullpage.html?res=");
						if (contains == true) {
							Double time_relativeness_score = article_time_relativeness_map
									.get(opposite_vertex.toString());
							Double edge_weight = p1 * (time_relativeness_score / sum_time_relativeness_score);
							edge2weight.put(edge, edge_weight);
						} else {
							Double relatednessScore = relEntities_relatednessScore_Map.get(opposite_vertex.toString());
							Double edge_weight = (1 - p1) * (relatednessScore / sum_relatednessScore);
							edge2weight.put(edge, edge_weight);
						}
					}
				}
			}
		}

		// Specifying edge weights for the outgoing edges from the document vertices
		for (Triple t : triples) {
			Object document_vertex = t.getSubject();
			Collection<Object> outEdges = stateTransitionGraph.getOutEdges(document_vertex);
			double sum_entityFrequency = Double.MIN_VALUE;
			for (Object edge : outEdges) {
				Object entity_vertex = stateTransitionGraph.getOpposite(document_vertex, edge);
				double entityFrequency = docs_entities_entitycount_map.get(document_vertex).get(entity_vertex);
				sum_entityFrequency += entityFrequency;
			}
			for (Object edge : outEdges) {
				Object entity_vertex = stateTransitionGraph.getOpposite(document_vertex, edge);
				double entityFrequency = docs_entities_entitycount_map.get(document_vertex).get(entity_vertex);
				double edge_weight = entityFrequency / sum_entityFrequency;
				edge2weight.put(edge, edge_weight);
			}
		}
	}

	public static void iniTriples(Map<String, Map<String, Double>> docs_entities_map) {
		docs_entities_map.entrySet().stream().forEach((entry) -> {
			String doc = entry.getKey();
			Map<String, Double> entity_entityFreq_map = entry.getValue();
			entity_entityFreq_map.entrySet().stream().forEach((entry2) -> {
				triples.add(new Triple(doc, "mentions", entry2.getKey()));
			});
		});
	}

	static class JumpToNode<V, Double> implements Transformer<V, Double> {

		HashMap<Object, Double> node2weight;

		public JumpToNode(HashMap<Object, Double> node2weight) {
			this.node2weight = node2weight;
		}

		@Override
		public Object transform(Object node) {

			if (node2weight.containsKey(node)) {
				return node2weight.get(node);
			} else {
				System.out.println("*** THIS NODE IS NOT INCLUDED IN THE SET OF NODES: " + node.toString());
				return 0.0;
			}

		}
	}

	static class EdgeWeight<E, Double> implements Transformer<E, Double> {

		HashMap<Object, Double> edge2weight;

		public EdgeWeight(HashMap<Object, Double> edge2weight) {
			this.edge2weight = edge2weight;
		}

		@Override
		public Object transform(Object edge) {
			if (edge2weight.containsKey(edge.toString())) {
				return edge2weight.get(edge.toString());
			} else {
				System.out.println("*** THIS EDGE IS NOT INCLUDED IN THE SET OF EDGES: " + edge.toString());
				return 0.0;
			}
		}
	}
}
