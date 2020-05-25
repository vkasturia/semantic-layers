/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package l3s.de.ranking_algo_and;

/**
 *
 * @author vaibhav
 * 
 * This class contains the parameters for connecting to Virtuoso
 */

public class VirtuosoConnector{
    
    private final String graph = "http://localhost:8890/MandelaGraph";
    private final String host = "jdbc:virtuoso://localhost:1111";
    private final String username = "dba";
    private final String password = "dba";
    public String getGraph(){
        return graph;
    }
    public String getHost(){
        return host;
    }
    public String getUsername(){
        return username;
    }
    public String getPwd(){
        return password;
    }
}