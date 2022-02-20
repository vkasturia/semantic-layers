/*
Copyright 2017-2022 Vaibhav Kasturia <vbh18kas@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package com.vaibhav.ranking_cat_pagerank.pagerank;

import com.hp.hpl.jena.rdf.model.RDFNode;
import edu.uci.ics.jung.algorithms.scoring.PageRankWithPriors;
import edu.uci.ics.jung.graph.Hypergraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.collections15.Transformer;

/**
 * @author Pavlos Fafalios, Vaibhav Kasturia
 * <p>
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
