package de.uni_trier.wi2.parsing;

import de.uni_trier.wi2.utils.SimilarityMeasureFunc;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import de.uni_trier.wi2.utils.WeightFunc;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;

public class XMLtoWeightFuncConverter extends XMLtoFunctionConverter {

    /**
     * <p>returns the WeightFunc of which the passed XML file is the representation of
     *
     * <p>The actual converting of the nodes contained in the DOM happens everytime the WeightFunc is called.
     *
     * <p>Make sure that the file makes use of the respective DTD in order to avoid runtime errors while converting.
     *
     * @param file  the XML file whose content represents the WeightFunc
     * @return  the WeightFunc generated from the XML file
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static WeightFunc getWeightFunc(File file) throws ParserConfigurationException, IOException, SAXException {
        // Initialize the Converter if not already initialized
        if (!initialized) initialize();
        if (file == null) WeightFunc.getDefault();

        // Parse the XML file
        Document doc = dBuilder.parse(file);

        // Get root element
        Node root = doc.getElementsByTagName("weight-function").item(0);

        // Get rid of unnecessary whitespace
        root.normalize();

        // Get all the child elements of the root element (should all be "if" nodes)
        NodeList ifStatements = root.getChildNodes();

        // Define the SimilarityMeasureFunc which computes the output according to the DOM
        WeightFunc weightFunc = (q) -> {

            // It is important that the evaluation of the "if" nodes happens in the order of the
            //  definition in the xml file. This guarantees that an author of such a file can implicitly define
            //  an "else" or "else if" condition.

            for (int i = 0; i < ifStatements.getLength(); i++){

                // If the evaluation of the first child element of the "if" node (should be a node which represents a
                //  logical operation/test) returns true, the second child of the "if" node, a "double" node, is
                //  evaluated and the generated Double object is returned.

                Node ifStatement = ifStatements.item(i);

                Node condition = ifStatement.getChildNodes().item(0);
                Node returnValue = ifStatement.getChildNodes().item(1);

                try {
                    if ((boolean) evaluate(condition, q, null)) return (double) evaluate(returnValue, q, null);
                } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException |
                         IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

            }
            return 1.;
        };

        return weightFunc;
    }

    /**
     * <p>returns the WeightFunc of which the passed XML file is the representation of
     *
     * <p>The actual converting of the nodes contained in the DOM happens everytime the WeightFunc is called.
     *
     * <p>Make sure that the file makes use of the respective DTD in order to avoid runtime errors while converting.
     *
     * @param str  the String whose content represents the WeightFunc
     * @return  the WeightFunc generated from the XML file
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static WeightFunc getWeightFunc(String str) throws ParserConfigurationException, IOException, SAXException {
        // Initialize the Converter if not already initialized
        if (!initialized) initialize();
        if (str == null) WeightFunc.getDefault();

        // Parse the XML file
        Document doc = dBuilder.parse(IOUtils.toInputStream(str, StandardCharsets.UTF_8));

        // Get root element
        Node root = doc.getElementsByTagName("weight-function").item(0);

        // Get rid of unnecessary whitespace
        root.normalize();

        // Get all the child elements of the root element (should all be "if" nodes)
        NodeList ifStatements = root.getChildNodes();

        // Define the SimilarityMeasureFunc which computes the output according to the DOM
        WeightFunc weightFunc = (q) -> {

            // It is important that the evaluation of the "if" nodes happens in the order of the
            //  definition in the xml file. This guarantees that an author of such a file can implicitly define
            //  an "else" or "else if" condition.

            for (int i = 0; i < ifStatements.getLength(); i++){

                // If the evaluation of the first child element of the "if" node (should be a node which represents a
                //  logical operation/test) returns true, the second child of the "if" node, a "double" node, is
                //  evaluated and the generated Double object is returned.

                Node ifStatement = ifStatements.item(i);

                Node condition = ifStatement.getChildNodes().item(0);
                Node returnValue = ifStatement.getChildNodes().item(1);

                try {
                    if ((boolean) evaluate(condition, q, null)) return (double) evaluate(returnValue, q, null);
                } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException |
                         IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

            }
            return 1.;
        };

        return weightFunc;
    }
}
