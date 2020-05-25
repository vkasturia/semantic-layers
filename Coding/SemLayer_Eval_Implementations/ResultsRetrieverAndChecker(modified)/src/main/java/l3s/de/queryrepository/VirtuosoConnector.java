package resultsretrieverandcheckerlocal;

/**
 *
 * @author vaibhav
 */

public class VirtuosoConnector{
    private final String graph = "";
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