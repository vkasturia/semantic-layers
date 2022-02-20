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
 * @author Fafalios, Kasturia
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
