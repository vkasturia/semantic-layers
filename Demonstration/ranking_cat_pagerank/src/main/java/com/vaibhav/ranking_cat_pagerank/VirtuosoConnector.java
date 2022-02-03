package com.vaibhav.ranking_cat_pagerank;

/**
 * @author Vaibhav Kasturia <kasturia at l3s.de>
 * <p>
 * This class contains the parameters for connecting to Virtuoso
 * Parameters should be set according to Virtuoso Configuration on Local Machine
 */

public class VirtuosoConnector {

    private final String graph = "http://localhost:8890/NYTLayer";
    private final String host = "jdbc:virtuoso://localhost:1111";
    private final String username = "dba";
    private final String password = "dba";

    public String getGraph() {
        return graph;
    }

    public String getHost() {
        return host;
    }

    public String getUsername() {
        return username;
    }

    public String getPwd() {
        return password;
    }
}