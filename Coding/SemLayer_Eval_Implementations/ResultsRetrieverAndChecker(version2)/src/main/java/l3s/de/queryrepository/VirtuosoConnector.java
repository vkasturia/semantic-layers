package resultsretrieverandchecker;

/**
 *
 * @author vaibhav
 * Use this class only in cases where overriding of ARQ Parser is desirable.
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