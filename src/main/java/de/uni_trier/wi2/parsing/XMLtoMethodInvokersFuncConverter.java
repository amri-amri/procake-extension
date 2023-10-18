package de.uni_trier.wi2.parsing;

import de.uni_trier.wi2.utils.MethodInvoker;
import de.uni_trier.wi2.utils.MethodInvokersFunc;
import de.uni_trier.wi2.utils.SimilarityMeasureFunc;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class XMLtoMethodInvokersFuncConverter extends XMLtoFunctionConverter {

    /**
     * <p>returns the MethodInvokersFunc of which the passed XML file is the representation of
     *
     * <p>The actual converting of the nodes contained in the DOM happens everytime the MethodInvokersFunc is called.
     *
     * <p>Make sure that the file makes use of the respective DTD in order to avoid runtime errors while converting.
     *
     * @param file  the XML file whose content represents the MethodInvokersFunc
     * @return  the MethodInvokersFunc generated from the XML file
     * @throws IOException if any IO error occurs with the file
     * @throws SAXException if any parse error occurs with the file
     * @throws ParserConfigurationException
     */
    public static MethodInvokersFunc getMethodInvokersFunc(File file) throws IOException, SAXException, ParserConfigurationException {
        if (file == null) MethodInvokersFunc.getDefault();

        // Initialize the Converter if not already initialized
        if (!initialized) initialize();

        // Parse the XML file
        Document doc = dBuilder.parse(file);

        return getMethodInvokersFunc(doc);
    }

    /**
     * <p>returns the MethodInvokersFunc of which the passed XML file is the representation of
     *
     * <p>The actual converting of the nodes contained in the DOM happens everytime the MethodInvokersFunc is called.
     *
     * <p>Make sure that the file makes use of the respective DTD in order to avoid runtime errors while converting.
     *
     * @param str  the String whose content represents the MethodInvokersFunc
     * @return  the MethodInvokersFunc generated from the XML file
     * @throws IOException if any IO error occurs with the file
     * @throws SAXException if any parse error occurs with the file
     * @throws ParserConfigurationException
     */
    public static MethodInvokersFunc getMethodInvokersFunc(String str) throws IOException, SAXException, ParserConfigurationException {
        if (str == null) MethodInvokersFunc.getDefault();

        // Initialize the Converter if not already initialized
        if (!initialized) initialize();

        // Parse the XML file
        Document doc = dBuilder.parse(IOUtils.toInputStream(str, StandardCharsets.UTF_8));

        return getMethodInvokersFunc(doc);
    }

    /**
     * <p>Converts the passed {@link Document} to a {@link MethodInvokersFunc}
     * @param doc  the Document which is to be converted into a MethodInvokersFunc
     * @return  the MethodInvokersFunc generated from the Document
     */
    private static MethodInvokersFunc getMethodInvokersFunc(Document doc){
        // Get root element
        Node root = doc.getElementsByTagName("method-invokers-function").item(0);

        // Get rid of unnecessary whitespace
        root.normalize();

        // Get all the child elements of the root element (should all be "if" nodes)
        NodeList ifStatements = root.getChildNodes();

        // Define the MethodInvokersFunc which computes the output according to the DOM
        MethodInvokersFunc methodInvokersFunc = (q, c) -> {

            // It is important that the evaluation of the "if" nodes happens in the order of the
            //  definition in the xml file. This guarantees that an author of such a file can implicitly define
            //  an "else" or "else if" condition.

            for (int i = 0; i < ifStatements.getLength(); i++){

                // If the evaluation of the first child element of the "if" node (should be a node which represents a
                //  logical operation/test) returns true, the second child of the "if" node, a "method-list" node, is
                //  evaluated and the generated MethodInvoker objects returned.

                Node ifStatement = ifStatements.item(i);

                Node condition = ifStatement.getChildNodes().item(0);
                Node returnValue = ifStatement.getChildNodes().item(1);

                try {
                    if ((boolean) evaluate(condition, q, c)) return (ArrayList<MethodInvoker>)  evaluate(returnValue, q, c);
                } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException |
                         IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

            }
            return new ArrayList<MethodInvoker>();
        };
        return methodInvokersFunc;
    }
}
