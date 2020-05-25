/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package l3s.de.randomwalk;

import edu.uci.ics.jung.algorithms.scoring.HITSWithPriors;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Hypergraph;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import org.apache.commons.collections15.Transformer;

/**
 *
 * @author fafalios
 */
public class HITS {

    private Hypergraph stateTransitionGraph;
    private int iterations;
    private double decayFactor;
    private Transformer entityVertexTransformer;
    private ArrayList<String> orderedVertices;

    public HITS(Hypergraph stateTransitionGraph, int iterations) {
        this.stateTransitionGraph = stateTransitionGraph;
        this.iterations = iterations;

        run();
    }

    public HITS(Hypergraph stateTransitionGraph, int iterations, double decayFactor, Transformer entityVertexTransformer) {
        this.stateTransitionGraph = stateTransitionGraph;
        this.iterations = iterations;
        this.decayFactor = decayFactor;
        this.entityVertexTransformer = entityVertexTransformer;

        run_withPriors();
    }

    private void run() {
        edu.uci.ics.jung.algorithms.scoring.HITS hits = new edu.uci.ics.jung.algorithms.scoring.HITS((Graph) stateTransitionGraph);
        hits.setMaxIterations(iterations);
        hits.initialize();
        hits.evaluate();

        ArrayList<Double> scores_auth = new ArrayList<Double>();
        HashMap<Double, HashSet<String>> maps_auth = new HashMap<Double, HashSet<String>>();

        ArrayList<Double> scores_hub = new ArrayList<Double>();
        HashMap<Double, HashSet<String>> maps_hub = new HashMap<Double, HashSet<String>>();

        for (Object v : stateTransitionGraph.getVertices()) {
            String vs = (String) v;
            edu.uci.ics.jung.algorithms.scoring.HITS.Scores score = (edu.uci.ics.jung.algorithms.scoring.HITS.Scores) hits.getVertexScore(v);

            if (scores_auth.contains(score.authority)) {
                HashSet<String> temp2 = maps_auth.get(score.authority);
                temp2.add(vs);
                maps_auth.put(score.authority, temp2);
            } else {
                scores_auth.add(score.authority);
                HashSet<String> temp2 = new HashSet<String>();
                temp2.add(vs);
                maps_auth.put(score.authority, temp2);
            }

            if (scores_hub.contains(score.hub)) {
                HashSet<String> temp2 = maps_hub.get(score.hub);
                temp2.add(vs);
                maps_hub.put(score.hub, temp2);
            } else {
                scores_hub.add(score.hub);
                HashSet<String> temp2 = new HashSet<String>();
                temp2.add(vs);
                maps_hub.put(score.hub, temp2);
            }
        }


        Collections.sort(scores_auth);
        Collections.sort(scores_hub);

        orderedVertices = new ArrayList<String>();
        for (int j = scores_auth.size() - 1; j >= 0; j--) {
            //boolean toBreak = false;
            double cur_score = scores_auth.get(j);
            HashSet<String> ee = maps_auth.get(cur_score);
            for (String ent : ee) {
                orderedVertices.add(ent);
            }

        }
    }

    private void run_withPriors() {

        HITSWithPriors hitsWithPriors = new HITSWithPriors(stateTransitionGraph, entityVertexTransformer, decayFactor);
        hitsWithPriors.setMaxIterations(iterations);
        hitsWithPriors.initialize();
        hitsWithPriors.evaluate();

        ArrayList<Double> scores_auth = new ArrayList<Double>();
        HashMap<Double, HashSet<String>> maps_auth = new HashMap<Double, HashSet<String>>();

        ArrayList<Double> scores_hub = new ArrayList<Double>();
        HashMap<Double, HashSet<String>> maps_hub = new HashMap<Double, HashSet<String>>();

        for (Object v : stateTransitionGraph.getVertices()) {
            String vs = (String) v;
            edu.uci.ics.jung.algorithms.scoring.HITS.Scores score = (edu.uci.ics.jung.algorithms.scoring.HITS.Scores) hitsWithPriors.getVertexScore(v);

            if (scores_auth.contains(score.authority)) {
                HashSet<String> temp2 = maps_auth.get(score.authority);
                temp2.add(vs);
                maps_auth.put(score.authority, temp2);
            } else {
                scores_auth.add(score.authority);
                HashSet<String> temp2 = new HashSet<String>();
                temp2.add(vs);
                maps_auth.put(score.authority, temp2);
            }

            if (scores_hub.contains(score.hub)) {
                HashSet<String> temp2 = maps_hub.get(score.hub);
                temp2.add(vs);
                maps_hub.put(score.hub, temp2);
            } else {
                scores_hub.add(score.hub);
                HashSet<String> temp2 = new HashSet<String>();
                temp2.add(vs);
                maps_hub.put(score.hub, temp2);
            }
        }


        Collections.sort(scores_auth);
        Collections.sort(scores_hub);

        orderedVertices = new ArrayList<String>();
        for (int j = scores_auth.size() - 1; j >= 0; j--) {
            //boolean toBreak = false;
            double cur_score = scores_auth.get(j);
            HashSet<String> ee = maps_auth.get(cur_score);
            for (String ent : ee) {
                orderedVertices.add(ent);
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

    public ArrayList<String> getOrderedVertices() {
        return orderedVertices;
    }

    public void setOrderedVertices(ArrayList<String> orderedVertices) {
        this.orderedVertices = orderedVertices;
    }
}
