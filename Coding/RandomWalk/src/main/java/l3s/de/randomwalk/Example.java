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
package l3s.de.randomwalk;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.HypergraphLayout;
import edu.uci.ics.jung.algorithms.scoring.PageRankWithPriors;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.BasicRenderer;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JFrame;
import org.apache.commons.collections15.Transformer;

/**
 *
 * @author Fafalios
 */
public class Example {

    public static Set<Triple> triples = new HashSet<>();
    public static SparseGraph stateTransitionGraph;
    public static HashMap<Object, Double> edge2weight = new HashMap<>();;
    public static HashMap<Object, Double> vertex2weight = new HashMap<>();;
    public static HashMap<Object, Double> vertex2score = new HashMap<>();
    public static ArrayList<Object> rankedVertices = new ArrayList<>();
    public static double decayFactor;
    public static int iterations;
    public static double initPageRankValue;

    public static void main(String[] args) {

        System.out.println("# Initializing an example of a graph...");
        iniTriples();

        System.out.println("# Creating the STG...");
        createSTG();
        System.out.println("# STG num of vertices: " + stateTransitionGraph.getVertexCount());
        System.out.println("# STG num of edges: " + stateTransitionGraph.getEdgeCount());

        initPageRankValue = (double) (1.0 / (double) stateTransitionGraph.getVertexCount());
        iterations = 20;
        decayFactor = 0.2;

        System.out.println("# Running PageRank...");
        PageRankWithPriors pr = new PageRankWithPriors(stateTransitionGraph, new EdgeWeight(edge2weight), new JumpToNode(vertex2weight), decayFactor);
        pr.setMaxIterations(iterations);
        pr.initialize();
        pr.evaluate();

        System.out.println("# Ranked vertices with scores:");
        getScoresAndRanking(pr); // vertex2score and rankedVertices are filled
       
        int counter = 1;
        for (Object vertex : rankedVertices) {
            System.out.println(counter++ + ". " + vertex.toString() + " ("+vertex2score.get(vertex)+")");
        }
        System.out.println("-----------------");
        
        // GRAPH VISUALIZATION //
        //visualize();
        
    }

    public static void visualize() {
        HypergraphLayout<Integer, Character> l = new HypergraphLayout<Integer, Character>(stateTransitionGraph, FRLayout.class);

        VisualizationViewer<Integer, Character> v = new VisualizationViewer<Integer, Character>(l, new Dimension(1400, 800));
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
    public static void createSTG() {
        stateTransitionGraph = new SparseGraph(); 

        int n = 1;
        for (Triple t : triples) {
            Object subject = t.getSubject();
            Object predicate = t.getPredicate();
            Object object = t.getObject();

            String edge = predicate + "_" + n++;
            stateTransitionGraph.addEdge(edge, new Pair(subject, object), EdgeType.UNDIRECTED);
        }
        
        // Example of specifying uniform vertex weights for the random jumps
        double uniformScore = 1.0 / (double) stateTransitionGraph.getVertexCount();
        for (Object vert : stateTransitionGraph.getVertices()) {
            vertex2weight.put(vert, uniformScore);
        }
        
        // Example of specifying uniform edge weights for the transition probabilities
        for (Object vert : stateTransitionGraph.getVertices()) {
            Collection<Object> outEdges = stateTransitionGraph.getOutEdges(vert);
            double uniformWeight = 1.0 / (double) outEdges.size();
            for (Object edge : outEdges) {
                edge2weight.put(edge, uniformWeight);
            }
        }
    }

    public static void iniTriples() {
        triples.add(new Triple("doc1", "mentions", "ent1"));
        triples.add(new Triple("doc1", "mentions", "ent2"));
        triples.add(new Triple("doc1", "mentions", "ent3"));

        triples.add(new Triple("doc2", "mentions", "ent1"));
        triples.add(new Triple("doc2", "mentions", "ent4"));
        triples.add(new Triple("doc2", "mentions", "ent5"));
        triples.add(new Triple("doc2", "mentions", "ent6"));

        triples.add(new Triple("doc3", "mentions", "ent1"));
        triples.add(new Triple("doc3", "mentions", "ent7"));
        triples.add(new Triple("doc3", "mentions", "ent8"));
        triples.add(new Triple("doc3", "mentions", "ent4"));

        triples.add(new Triple("doc4", "mentions", "ent9"));
        triples.add(new Triple("doc4", "mentions", "ent10"));
        triples.add(new Triple("doc4", "mentions", "ent2"));
        triples.add(new Triple("doc4", "mentions", "ent11"));

        triples.add(new Triple("doc5", "mentions", "ent1"));
        triples.add(new Triple("doc5", "mentions", "ent12"));
        triples.add(new Triple("doc5", "mentions", "ent13"));
        
        triples.add(new Triple("doc6", "mentions", "ent20"));
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
