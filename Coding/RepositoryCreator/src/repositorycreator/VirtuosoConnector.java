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

package repositorycreator;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import virtuoso.sesame2.driver.VirtuosoRepository;

/**
 *
 * @author Fafalios, Kasturia
 */
public class VirtuosoConnector {

    private String virtuosoHost;
    private String virtuosoPort;
    private String virtuosoUserName;
    private String virtuosoPassword;
    private VirtuosoRepository repository;
    public static RepositoryConnection conn;

    /**
     * Constructor Initializes the variables which are necessary for connecting
     * (host,port,username,password)
     */
    public VirtuosoConnector() {
        this.virtuosoHost = Resources.virtuosoHost.replaceAll("http://", "");
        this.virtuosoPort = Resources.virtuosoPort;
        this.virtuosoUserName = Resources.virtuosoUsername;
        this.virtuosoPassword = Resources.virtuosoPassword;

    }

    /**
     * This method starts the connection to the virtuoso repository (openrdf)
     *
     * @throws RepositoryException
     */
    public void startConnection() throws RepositoryException {

        this.repository = new VirtuosoRepository("jdbc:virtuoso://"
                + this.virtuosoHost + ":" + this.virtuosoPort
                + "/charset=UTF-8/log_enable=2",
                this.virtuosoUserName, this.virtuosoPassword);
        VirtuosoConnector.conn = this.repository.getConnection();
    }

    /**
     * Returns the virtuoso Repository Connection (openrdf)
     *
     * @return the virtuoso Repository Connection
     */
    public static RepositoryConnection getConnection() {
        return VirtuosoConnector.conn;
    }

    /**
     * Returns the virtuoso Repository (openrdf)
     *
     * @return the virtuoso Repository
     */
    public VirtuosoRepository getRepository() {
        return this.repository;
    }

    /**
     * Terminates openRDF Connection
     *
     * @throws WarehouseControllerException
     */
    public void terminateConnection() {
        if (VirtuosoConnector.conn != null) {
            try {
                VirtuosoConnector.conn.close();
            } catch (RepositoryException ex) {
            }
        }

    }
}
