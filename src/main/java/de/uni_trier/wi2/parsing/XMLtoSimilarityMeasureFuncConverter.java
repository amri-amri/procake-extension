package de.uni_trier.wi2.parsing;

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

import static de.uni_trier.wi2.ProcakeExtensionLoggingUtils.*;

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
        METHOD_CALL.trace(
                "public static SimilarityMeasureFunc procake-extension.parsing.XMLtoSimilarityMeasureFuncConverter.getSimilarityMeasureFunc" +
                        "(File file={})...", maxSubstring(file));
        
        if (file == null) {
            METHOD_CALL.trace(
                    "procake-extension.parsing.XMLtoSimilarityMeasureFuncConverter.getSimilarityMeasureFunc(File): " +
                            "return SimilarityMeasureFunc.getDefault();");
            return SimilarityMeasureFunc.getDefault();
        }

        // Initialize the Converter if not already initialized
        if (!initialized) initialize();

        // Parse the XML file
        Document doc = dBuilder.parse(file);

        SimilarityMeasureFunc similarityMeasureFunc = getSimilarityMeasureFunc(doc);

        DIAGNOSTICS.trace(
                "procake-extension.parsing.XMLtoSimilarityMeasureFuncConverter.getSimilarityMeasureFunc(File): return");

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
        METHOD_CALL.trace(
                "public static SimilarityMeasureFunc procake-extension.parsing.XMLtoSimilarityMeasureFuncConverter.getSimilarityMeasureFunc" +
                        "(String str={})...", maxSubstring(str));
        
        if (str == null) {
            METHOD_CALL.trace(
                    "procake-extension.parsing.XMLtoSimilarityMeasureFuncConverter.getSimilarityMeasureFunc(String): " +
                            "return SimilarityMeasureFunc.getDefault();");
            return SimilarityMeasureFunc.getDefault();
        }

        // Initialize the Converter if not already initialized
        if (!initialized) initialize();

        // Parse the XML file
        Document doc = dBuilder.parse(IOUtils.toInputStream(str, StandardCharsets.UTF_8));

        SimilarityMeasureFunc similarityMeasureFunc = getSimilarityMeasureFunc(doc);

        DIAGNOSTICS.trace(
                "procake-extension.parsing.XMLtoSimilarityMeasureFuncConverter.getSimilarityMeasureFunc(String): return");

        return similarityMeasureFunc;
    }

    /**
     * <p>Converts the passed {@link Document} to a {@link SimilarityMeasureFunc}
     * @param doc  the Document which is to be converted into a SimilarityMeasureFunc
     * @return  the SimilarityMeasureFunc generated from the Document
     */
    private static SimilarityMeasureFunc getSimilarityMeasureFunc(Document doc){
        METHOD_CALL.trace("private static SimilarityMeasureFunc procake-extension.parsing.XMLtoSimilarityMeasureFuncConverter.getSimilarityMeasureFunc" +
                "(Document doc={})...", maxSubstring(doc));
        
        // Get root element
        Node root = doc.getElementsByTagName("similarity-measure-function").item(0);

        // Get rid of unnecessary whitespace
        root.normalize();

        // Get all the child elements of the root element (should all be "if" nodes)
        NodeList ifStatements = root.getChildNodes();

        // Define the SimilarityMeasureFunc which computes the output according to the DOM
        SimilarityMeasureFunc similarityMeasureFunc = (q,c) -> {

            METHOD_CALL.trace("String procake-extension.utils.SimilarityMeasureFunc.apply" +
                    "(DataObject q={}, DataObject c={})...", q, c);

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

                DIAGNOSTICS.trace("procake-extension.utils.SimilarityMeasureFunc.apply(DataObject, DataObject): " +
                                "ifStatements.item({})={}, condition={}, returnValue={}",
                        i, maxSubstring(ifStatement), maxSubstring(condition), maxSubstring(returnValue));

                try {
                    boolean ifStatementEvaluated = (boolean) evaluate(condition, q, c);

                    DIAGNOSTICS.trace("procake-extension.utils.SimilarityMeasureFunc.apply(DataObject, DataObject): " +
                            "evaluate(condition, q, c))={}", ifStatementEvaluated);
                    
                    if (ifStatementEvaluated) {
                        String similarityMeasure = (String) evaluate(returnValue, q, c);

                        METHOD_CALL.trace("procake-extension.utils.SimilarityMeasureFunc.apply(DataObject, DataObject): " +
                                "return {}", maxSubstring(similarityMeasure));

                        return similarityMeasure;
                    }
                } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException |
                         IllegalAccessException e) {
                    METHOD_CALL.trace("procake-extension.utils.SimilarityMeasureFunc.apply(DataObject, DataObject): " +
                            "throw new RuntimeException(e); e={}", maxSubstring(e));
                    throw new RuntimeException(e);
                }

            }

            METHOD_CALL.trace("procake-extension.utils.SimilarityMeasureFunc.apply(DataObject, DataObject): " +
                    "return null;");
            
            return null;
        };

        METHOD_CALL.trace("procake-extension.parsing.XMLtoSimilarityMeasureFuncConverter.getSimilarityMeasureFunc(Document): " +
                "return similarityMeasureFunc={}", maxSubstring(similarityMeasureFunc));

        return similarityMeasureFunc;
    }



}
