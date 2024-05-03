package de.uni_trier.wi2.parsing;

import de.uni_trier.wi2.parsing.model.ConditionComponent;
import de.uni_trier.wi2.parsing.model.DoubleComponent;
import de.uni_trier.wi2.parsing.model.LogicalOrConditionComponent;
import de.uni_trier.wi2.parsing.model.WF_IfComponent;
import de.uni_trier.wi2.utils.WeightFunc;
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


public class XMLtoWeightFuncConverter extends XMLtoFunctionConverter {

    /**
     * <p>returns the WeightFunc of which the passed XML file is the representation of
     *
     * <p>The actual converting of the nodes contained in the DOM happens everytime the WeightFunc is called.
     *
     * <p>Make sure that the file makes use of the respective DTD in order to avoid runtime errors while converting.
     *
     * @param file the XML file whose content represents the WeightFunc
     * @return the WeightFunc generated from the XML file
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static WeightFunc getWeightFunc(File file) throws ParserConfigurationException, IOException, SAXException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {


        if (file == null) {

            return WeightFunc.getDefault();
        }

        // Initialize the Converter if not already initialized
        if (!initialized) initialize();

        // Parse the XML file
        Document doc = dBuilder.parse(file);

        WeightFunc weightFunc = getWeightFunc(doc);


        return weightFunc;
    }

    /**
     * <p>returns the WeightFunc of which the passed XML file is the representation of
     *
     * <p>The actual converting of the nodes contained in the DOM happens everytime the WeightFunc is called.
     *
     * <p>Make sure that the file makes use of the respective DTD in order to avoid runtime errors while converting.
     *
     * @param str the String whose content represents the WeightFunc
     * @return the WeightFunc generated from the XML file
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static WeightFunc getWeightFunc(String str) throws ParserConfigurationException, IOException, SAXException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {


        if (str == null) {

            return WeightFunc.getDefault();
        }

        // Initialize the Converter if not already initialized
        if (!initialized) initialize();

        // Parse the XML file
        Document doc = dBuilder.parse(IOUtils.toInputStream(str, StandardCharsets.UTF_8));

        WeightFunc weightFunc = getWeightFunc(doc);


        return weightFunc;
    }

    /**
     * <p>Converts the passed {@link Document} to a {@link WeightFunc}
     *
     * @param doc the Document which is to be converted into a WeightFunc
     * @return the WeightFunc generated from the Document
     */
    private static WeightFunc getWeightFunc(Document doc) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        // Get root element
        Node root = doc.getElementsByTagName("weight-function").item(0);

        // Get rid of unnecessary whitespace
        root.normalize();

        // Get all the child elements of the root element (should all be "if" nodes)
        NodeList ifStatements = root.getChildNodes();

        final int amountOfIfStatements = ifStatements.getLength();

        WF_IfComponent[] ifComponents = new WF_IfComponent[amountOfIfStatements];

        for (int i = 0; i < amountOfIfStatements; i++) {
            Node ifStatement = ifStatements.item(i);

            Node condition = ifStatement.getChildNodes().item(0);
            Node returnValue = ifStatement.getChildNodes().item(1);

            LogicalOrConditionComponent conditionComponent = (LogicalOrConditionComponent) evaluate(condition);
            DoubleComponent doubleComponent = (DoubleComponent) evaluate(returnValue);
            WF_IfComponent ifComponent = new WF_IfComponent(conditionComponent, doubleComponent);
            ifComponents[i] = ifComponent;
        }

        final WeightFunc weightFunc = (q) -> {
            for (WF_IfComponent ifComponent : ifComponents)
                if (ifComponent.isSatisfied(q, null)) return ifComponent.getReturnValue();
            return 1;
        };


        return weightFunc;
    }
}
