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
public class RepositoryCreator {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     * @throws org.openrdf.rio.RDFParseException
     * @throws org.openrdf.repository.RepositoryException
     */
    public static void main(String[] args) throws IOException, RDFParseException, RepositoryException {
        for (int k = 1988; k < 1989; k++) {
            VirtuosoConnector connection = new VirtuosoConnector();
            connection.startConnection();

            String folderPath = "/Users/vaibhav/Desktop/renato_annotations";
            File folder = new File(folderPath);
            File[] listOfFiles = folder.listFiles();

            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                    if (listOfFiles[i].getName().toLowerCase().endsWith(".n3") && listOfFiles[i].getName().toLowerCase().contains(Integer.toString(k))) {
                        System.out.println("- Importing file: " + listOfFiles[i].getAbsolutePath());
                        File tripleFile = new File(listOfFiles[i].getAbsolutePath());
                        FileInputStream fis = new FileInputStream(tripleFile);

                        URI graph = VirtuosoConnector.getConnection().getRepository().getValueFactory().createURI(Resources.virtuosoGraph);
                        RDFFormat format = RDFFormat.N3;
                        VirtuosoConnector.getConnection().add(fis, "", format, graph);
                        System.out.println(" [IMPORTED]");
                    }
                }
            }

            connection.terminateConnection();

        }
    }
}
