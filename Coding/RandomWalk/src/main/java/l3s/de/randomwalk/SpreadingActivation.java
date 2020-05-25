/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package l3s.de.randomwalk;

import edu.uci.ics.jung.graph.Hypergraph;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author fafalios
 */
public class SpreadingActivation {

    private Hypergraph stateTransitionGraph;
    private HashMap<String, Double> vertex2score;
    private double firingThreshold;
    private double decayFactor;
    private double edgesWeight;
    private HashMap<String, Double> activationValues;
    private HashMap<String, Boolean> hasFired;
    private HashMap<String, HashSet<String>> links;
    private ArrayList<String> orderedVertices;

    public SpreadingActivation(Hypergraph stateTransitionGraph, HashMap<String, Double> vertex2score, double firingThreshold, double decayFactor, double edgesWeight) {
        this.stateTransitionGraph = stateTransitionGraph;
        this.vertex2score = vertex2score;
        this.firingThreshold = firingThreshold;
        this.decayFactor = decayFactor;
        this.edgesWeight = edgesWeight;

        activationValues = new HashMap<String, Double>();
        hasFired = new HashMap<String, Boolean>();
        links = new HashMap<String, HashSet<String>>();

        run();
    }

    public void run() {

        Collection vertices = stateTransitionGraph.getVertices();
        for (Object vertexObj : vertices) {
            String vertex = (String) vertexObj;

            if (vertex2score.containsKey(vertex)) {
                // thr firing threshold is not 0
                activationValues.put(vertex, vertex2score.get(vertex));
                //System.out.println("====>"+vertex+"="+vertex2score.get(vertex));
            } else {
                // the firing threshold is 0
                activationValues.put(vertex, 0.0);
            }

            hasFired.put(vertex, false);

            HashSet<String> setOfOut = new HashSet<String>();
            //System.out.println("# OF OUT OF '"+vertex+"' is: "+stateTransitionGraph.getOutEdges(vertex).size());
            for (Object outvobj : stateTransitionGraph.getSuccessors(vertex)) {
                String outv = (String) outvobj;
                setOfOut.add(outv);
            }
            //System.out.println("successors # of "+vertex+"="+stateTransitionGraph.getSuccessors(vertex).size());
            //System.out.println("neighbors # of "+vertex+"="+stateTransitionGraph.getNeighbors(vertex).size());
            //System.out.println("----");
            links.put(vertex, setOfOut);
        }

        while (notFiredExist()) {
            boolean in = false;
            for (String i : activationValues.keySet()) {
                if (!hasFired.get(i) && activationValues.get(i) > firingThreshold) {
                    in = true;
                    double i_val = activationValues.get(i);
                    hasFired.put(i, true);
                    for (String j : links.get(i)) {
                        //System.out.println("---::"+j);
                        double j_val = activationValues.get(j);
                        double j_valNew = j_val + (i_val * edgesWeight * decayFactor);
                        if (j_val < firingThreshold && j_valNew >= firingThreshold) {
                            hasFired.put(j, false);
                        }

//                        if (j_valNew > 1.0) {
//                            j_valNew = 1.0;
//                        }
//                        if (j_valNew < 0.0) {
//                            j_valNew = 0.0;
//                        }
                        activationValues.put(j, j_valNew);
                    }
                }
            }
            if (!in) {
                // there is not any node with value greated that the firing threshold 
                // that has not been fired.
                break;
            }
        }


        ArrayList<Double> scores = new ArrayList<Double>();
        HashMap<Double, HashSet<String>> maps = new HashMap<Double, HashSet<String>>();

        for (String vs : activationValues.keySet()) {

            double score = activationValues.get(vs);
            if (scores.contains(score)) {
                HashSet<String> temp = maps.get(score);
                temp.add(vs);
                maps.put(score, temp);
            } else {
                scores.add(score);
                HashSet<String> temp = new HashSet<String>();
                temp.add(vs);
                maps.put(score, temp);
            }
        }
        Collections.sort(scores);

        orderedVertices = new ArrayList<String>();
        for (int j = scores.size() - 1; j >= 0; j--) {
            //boolean toBreak = false;
            double cur_score = scores.get(j);
            HashSet<String> ee = maps.get(cur_score);
            for (String ent : ee) {
                orderedVertices.add(ent);
            }

        }

//        System.out.println("====================");
//        System.out.println("# Activation Spreading Order:");
//        for (int i = 0; i < 50; i++) {
//            String ent = orderedVertices.get(i);
//            System.out.println((i + 1) + ". " + ent + "(" + activationValues.get(ent) + ")");
//        }
//        System.out.println("====================");

    }

    private boolean notFiredExist() {

        for (String e : hasFired.keySet()) {
            if (!hasFired.get(e)) {
                return true;
            }
        }

        return false;
    }

    public HashMap<String, Double> getVertex2score() {
        return vertex2score;
    }

    public void setVertex2score(HashMap<String, Double> vertex2score) {
        this.vertex2score = vertex2score;
    }

    public double getFiringThreshold() {
        return firingThreshold;
    }

    public void setFiringThreshold(double firingThreshold) {
        this.firingThreshold = firingThreshold;
    }

    public double getDecayFactor() {
        return decayFactor;
    }

    public void setDecayFactor(double decayFactor) {
        this.decayFactor = decayFactor;
    }

    public double getEdgesWeight() {
        return edgesWeight;
    }

    public void setEdgesWeight(double edgesWeight) {
        this.edgesWeight = edgesWeight;
    }

    public HashMap<String, Double> getActivationValues() {
        return activationValues;
    }

    public void setActivationValues(HashMap<String, Double> activationValues) {
        this.activationValues = activationValues;
    }

    public ArrayList<String> getOrderedVertices() {
        return orderedVertices;
    }

    public void setOrderedVertices(ArrayList<String> orderedVertices) {
        this.orderedVertices = orderedVertices;
    }
}
