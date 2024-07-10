package de.uni_trier.wi2.parsing;

import de.uni_trier.wi2.parsing.model.LogicalOrConditionComponent;
import de.uni_trier.wi2.parsing.model.SMF_IfComponent;
import de.uni_trier.wi2.parsing.model.StringComponent;
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


public class XMLtoSimilarityMeasureFuncConverter extends XMLtoFunctionConverter {

    /**
     * <p>returns the SimilarityMeasureFunc of which the passed XML file is the representation of
     *
     * <p>The actual converting of the nodes contained in the DOM happens everytime the SimilarityMeasureFunc is called.
     *
     * <p>Make sure that the file makes use of the respective DTD in order to avoid runtime errors while converting.
     *
     * @param file the XML file whose content represents the SimilarityMeasureFunc
     * @return the SimilarityMeasureFunc generated from the XML file
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static SimilarityMeasureFunc getSimilarityMeasureFunc(File file) throws ParserConfigurationException, IOException, SAXException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {


        if (file == null) {

            return SimilarityMeasureFunc.getDefault();
        }

        // Initialize the Converter if not already initialized
        if (!initialized) initialize();

        // Parse the XML file
        Document doc = dBuilder.parse(file);

        SimilarityMeasureFunc similarityMeasureFunc = getSimilarityMeasureFunc(doc);


        return similarityMeasureFunc;
    }

    /**
     * <p>returns the SimilarityMeasureFunc of which the passed XML file is the representation of
     *
     * <p>The actual converting of the nodes contained in the DOM happens everytime the SimilarityMeasureFunc is called.
     *
     * <p>Make sure that the file makes use of the respective DTD in order to avoid runtime errors while converting.
     *
     * @param str the String whose content represents the SimilarityMeasureFunc
     * @return the SimilarityMeasureFunc generated from the XML file
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static SimilarityMeasureFunc getSimilarityMeasureFunc(String str) throws ParserConfigurationException, IOException, SAXException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {


        if (str == null) {

            return SimilarityMeasureFunc.getDefault();
        }

        // Initialize the Converter if not already initialized
        if (!initialized) initialize();

        // Parse the XML file
        Document doc = dBuilder.parse(IOUtils.toInputStream(str, StandardCharsets.UTF_8));

        SimilarityMeasureFunc similarityMeasureFunc = getSimilarityMeasureFunc(doc);


        return similarityMeasureFunc;
    }

    /**
     * <p>Converts the passed {@link Document} to a {@link SimilarityMeasureFunc}
     *
     * @param doc the Document which is to be converted into a SimilarityMeasureFunc
     * @return the SimilarityMeasureFunc generated from the Document
     */
    private static SimilarityMeasureFunc getSimilarityMeasureFunc(Document doc) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        // Get root element
        Node root = doc.getElementsByTagName("similarity-measure-function").item(0);

        // Get rid of unnecessary whitespace
        root.normalize();

        // Get all the child elements of the root element (should all be "if" nodes)
        NodeList ifStatements = root.getChildNodes();

        // Define the SimilarityMeasureFunc which computes the output according to the DOM
        final int amountOfIfStatements = ifStatements.getLength();

        SMF_IfComponent[] ifComponents = new SMF_IfComponent[amountOfIfStatements];

        for (int i = 0; i < amountOfIfStatements; i++) {
            Node ifStatement = ifStatements.item(i);

            Node condition = ifStatement.getChildNodes().item(0);
            Node returnValue = ifStatement.getChildNodes().item(1);

            LogicalOrConditionComponent conditionComponent = (LogicalOrConditionComponent) evaluate(condition);
            StringComponent stringComponent = (StringComponent) evaluate(returnValue);
            SMF_IfComponent ifComponent = new SMF_IfComponent(conditionComponent, stringComponent);
            ifComponents[i] = ifComponent;
        }

        final SimilarityMeasureFunc similarityMeasureFunc = (q, c) -> {
            for (SMF_IfComponent ifComponent : ifComponents)
                if (ifComponent.isSatisfied(q, c)) return ifComponent.getReturnValue();
            return null;
        };

        return similarityMeasureFunc;
    }


}
