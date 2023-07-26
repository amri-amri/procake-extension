package parsing;

import de.uni_trier.wi2.procake.data.object.DataObject;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.entity.InputStreamFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import utils.SimilarityMeasureFunc;

import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;

public class XMLtoSimilarityMeasureFuncConverter extends XMLtoFunctionConverter {

    /**
     * <p>returns the SimilarityMeasureFunc of which the passed XML file is the representation of
     *
     * <p>The actual converting of the nodes contained in the DOM happens everytime the SimilarityMeasureFunc is called.
     *
     * <p>Make sure that the file makes use of the respective DTD in order to avoid runtime errors while converting.
     *
     * @param file  the XML file whose content represents the SimilarityMeasureFunc
     * @return  the SimilarityMeasureFunc generated from the XML file
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static SimilarityMeasureFunc getSimilarityMeasureFunc(File file) throws ParserConfigurationException, IOException, SAXException {
        // Initialize the Converter if not already initialized
        if (!initialized) initialize();

        // Parse the XML file
        Document doc = dBuilder.parse(file);

        // Get root element
        Node root = doc.getElementsByTagName("similarity-measure-function").item(0);

        // Get rid of unnecessary whitespace
        root.normalize();

        // Get all the child elements of the root element (should all be "if" nodes)
        NodeList ifStatements = root.getChildNodes();

        // Define the SimilarityMeasureFunc which computes the output according to the DOM
        SimilarityMeasureFunc similarityMeasureFunc = (q,c) -> {

            // It is important that the evaluation of the "if" nodes happens in the order of the
            //  definition in the xml file. This guarantees that an author of such a file can implicitly define
            //  an "else" or "else if" condition.

            for (int i = 0; i < ifStatements.getLength(); i++){

                // If the evaluation of the first child element of the "if" node (should be a node which represents a
                //  logical operation/test) returns true, the second child of the "if" node, a "string" node, is
                //  evaluated and the generated String object is returned.

                Node ifStatement = ifStatements.item(i);

                Node condition = ifStatement.getChildNodes().item(0);
                Node returnValue = ifStatement.getChildNodes().item(1);

                try {
                    if ((boolean) evaluate(condition, q, c)) return (String) evaluate(returnValue, q, c);
                } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException |
                         IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

            }
            return "";
        };

        return similarityMeasureFunc;
    }

    /**
     * <p>returns the SimilarityMeasureFunc of which the passed XML file is the representation of
     *
     * <p>The actual converting of the nodes contained in the DOM happens everytime the SimilarityMeasureFunc is called.
     *
     * <p>Make sure that the file makes use of the respective DTD in order to avoid runtime errors while converting.
     *
     * @param str  the String whose content represents the SimilarityMeasureFunc
     * @return  the SimilarityMeasureFunc generated from the XML file
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static SimilarityMeasureFunc getSimilarityMeasureFunc(String str) throws ParserConfigurationException, IOException, SAXException {
        // Initialize the Converter if not already initialized
        if (!initialized) initialize();
        if (str == null) return (q, c) -> null;

        // Parse the XML file
        Document doc = dBuilder.parse(IOUtils.toInputStream(str, StandardCharsets.UTF_8));

        // Get root element
        Node root = doc.getElementsByTagName("similarity-measure-function").item(0);

        // Get rid of unnecessary whitespace
        root.normalize();

        // Get all the child elements of the root element (should all be "if" nodes)
        NodeList ifStatements = root.getChildNodes();

        // Define the SimilarityMeasureFunc which computes the output according to the DOM
        SimilarityMeasureFunc similarityMeasureFunc = (q,c) -> {

            // It is important that the evaluation of the "if" nodes happens in the order of the
            //  definition in the xml file. This guarantees that an author of such a file can implicitly define
            //  an "else" or "else if" condition.

            for (int i = 0; i < ifStatements.getLength(); i++){

                // If the evaluation of the first child element of the "if" node (should be a node which represents a
                //  logical operation/test) returns true, the second child of the "if" node, a "string" node, is
                //  evaluated and the generated String object is returned.

                Node ifStatement = ifStatements.item(i);

                Node condition = ifStatement.getChildNodes().item(0);
                Node returnValue = ifStatement.getChildNodes().item(1);

                try {
                    if ((boolean) evaluate(condition, q, c)) return (String) evaluate(returnValue, q, c);
                } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException |
                         IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

            }
            return null;
        };

        return similarityMeasureFunc;
    }



}
