/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package l3s.de.pagerank;

import com.hp.hpl.jena.rdf.model.RDFNode;
import edu.uci.ics.jung.algorithms.scoring.PageRankWithPriors;
import edu.uci.ics.jung.graph.Hypergraph;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import org.apache.commons.collections15.Transformer;

/**
 *
 * @author Pavlos Fafalios, Vaibhav Kasturia
 * 
 * Class containing PageRank algorithm functions
 */
public class PageRank {

    private Hypergraph stateTransitionGraph;
    private int iterations;
    private double decayFactor;
    private Transformer entityVertexTransformer;
    private ArrayList<RDFNode> orderedVertices;
    private HashMap<RDFNode, Double> vertex2score;

    public PageRank(Hypergraph stateTransitionGraph, int iterations, double decayFactor, Transformer entityVertexTransformer) {
        this.stateTransitionGraph = stateTransitionGraph;
        this.iterations = iterations;
        this.decayFactor = decayFactor;
        this.entityVertexTransformer = entityVertexTransformer;
        this.vertex2score = new HashMap<RDFNode, Double>();

        run();
    }

    private void run() {

        PageRankWithPriors pr = new PageRankWithPriors(stateTransitionGraph, entityVertexTransformer, decayFactor);
        pr.setMaxIterations(iterations);
        pr.initialize();
        pr.evaluate();

        ArrayList<Double> scores = new ArrayList<Double>();
        HashMap<Double, HashSet<Object>> maps = new HashMap<Double, HashSet<Object>>();

        for (Object v : stateTransitionGraph.getVertices()) {

            double score = (Double) pr.getVertexScore(v);
            vertex2score.put((RDFNode) v, score);
            if (scores.contains(score)) {
                HashSet<Object> temp = maps.get(score);
                temp.add(v);
                maps.put(score, temp);
            } else {
                scores.add(score);
                HashSet<Object> temp = new HashSet<Object>();
                temp.add(v);
                maps.put(score, temp);
            }
        }
        Collections.sort(scores);

        orderedVertices = new ArrayList<RDFNode>();
        for (int j = scores.size() - 1; j >= 0; j--) {
            //boolean toBreak = false;
            double cur_score = scores.get(j);
            HashSet<Object> ee = maps.get(cur_score);
            for (Object ent : ee) {
                orderedVertices.add((RDFNode) ent);
            }

        }
    }

    public Hypergraph getStateTransitionGraph() {
        return stateTransitionGraph;
    }

    public void setStateTransitionGraph(Hypergraph stateTransitionGraph) {
        this.stateTransitionGraph = stateTransitionGraph;
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public double getDecayFactor() {
        return decayFactor;
    }

    public void setDecayFactor(double decayFactor) {
        this.decayFactor = decayFactor;
    }

    public Transformer getEntityVertexTransformer() {
        return entityVertexTransformer;
    }

    public void setEntityVertexTransformer(Transformer entityVertexTransformer) {
        this.entityVertexTransformer = entityVertexTransformer;
    }

    public ArrayList<RDFNode> getOrderedVertices() {
        return orderedVertices;
    }

    public void setOrderedVertices(ArrayList<RDFNode> orderedVertices) {
        this.orderedVertices = orderedVertices;
    }

    public HashMap<RDFNode, Double> getVertex2score() {
        return vertex2score;
    }

    public void setVertex2score(HashMap<RDFNode, Double> vertex2score) {
        this.vertex2score = vertex2score;
    }

}
