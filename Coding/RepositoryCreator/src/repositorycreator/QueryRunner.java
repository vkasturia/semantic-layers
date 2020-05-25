/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package repositorycreator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

/**
 *
 * @author Fafalios
 */
public class QueryRunner {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     * @throws org.openrdf.rio.RDFParseException
     * @throws org.openrdf.repository.RepositoryException
     */
    public static void main(String[] args) throws IOException, RDFParseException, RepositoryException {

        VirtuosoConnector connection = new VirtuosoConnector();
        connection.startConnection();

        

        connection.terminateConnection();

    }

}
