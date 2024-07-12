package de.uni_trier.wi2.parsing;

import de.uni_trier.wi2.parsing.model.LogicalOrConditionComponent;
import de.uni_trier.wi2.parsing.model.MIF_IfComponent;
import de.uni_trier.wi2.parsing.model.MethodListComponent;
import de.uni_trier.wi2.utils.MethodInvokersFunc;
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
     * @param file the XML file whose content represents the MethodInvokersFunc
     * @return the MethodInvokersFunc generated from the XML file
     * @throws IOException                  if any IO error occurs with the file
     * @throws SAXException                 if any parse error occurs with the file
     * @throws ParserConfigurationException
     */
    public static MethodInvokersFunc getMethodInvokersFunc(File file) throws IOException, SAXException, ParserConfigurationException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {


        if (file == null) {

            return MethodInvokersFunc.getDefault();
        }

        // Initialize the Converter if not already initialized
        if (!initialized) initialize();

        // Parse the XML file
        Document doc = dBuilder.parse(file);

        MethodInvokersFunc methodInvokersFunc = getMethodInvokersFunc(doc);


        return methodInvokersFunc;
    }

    /**
     * <p>returns the MethodInvokersFunc of which the passed XML file is the representation of
     *
     * <p>The actual converting of the nodes contained in the DOM happens everytime the MethodInvokersFunc is called.
     *
     * <p>Make sure that the file makes use of the respective DTD in order to avoid runtime errors while converting.
     *
     * @param str the String whose content represents the MethodInvokersFunc
     * @return the MethodInvokersFunc generated from the XML file
     * @throws IOException                  if any IO error occurs with the file
     * @throws SAXException                 if any parse error occurs with the file
     * @throws ParserConfigurationException
     */
    public static MethodInvokersFunc getMethodInvokersFunc(String str) throws IOException, SAXException, ParserConfigurationException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {


        if (str == null) {

            return MethodInvokersFunc.getDefault();
        }

        // Initialize the Converter if not already initialized
        if (!initialized) initialize();

        // Parse the XML file
        Document doc = dBuilder.parse(IOUtils.toInputStream(str, StandardCharsets.UTF_8));

        MethodInvokersFunc methodInvokersFunc = getMethodInvokersFunc(doc);


        return methodInvokersFunc;
    }

    /**
     * <p>Converts the passed {@link Document} to a {@link MethodInvokersFunc}
     *
     * @param doc the Document which is to be converted into a MethodInvokersFunc
     * @return the MethodInvokersFunc generated from the Document
     */
    private static MethodInvokersFunc getMethodInvokersFunc(Document doc) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        // Get root element
        final Node root = doc.getElementsByTagName("method-invokers-function").item(0);

        // Get rid of unnecessary whitespace
        root.normalize();

        // Get all the child elements of the root element (should all be "if" nodes)
        final NodeList ifStatements = root.getChildNodes();

        final int amountOfIfStatements = ifStatements.getLength();

        MIF_IfComponent[] ifComponents = new MIF_IfComponent[amountOfIfStatements];

        for (int i = 0; i < amountOfIfStatements; i++) {
            Node ifStatement = ifStatements.item(i);

            Node condition = ifStatement.getChildNodes().item(0);
            Node returnValue = ifStatement.getChildNodes().item(1);

            LogicalOrConditionComponent conditionComponent = (LogicalOrConditionComponent) evaluate(condition);
            MethodListComponent methodListComponent = (MethodListComponent) evaluate(returnValue);
            MIF_IfComponent ifComponent = new MIF_IfComponent(conditionComponent, methodListComponent);
            ifComponents[i] = ifComponent;
        }

        final MethodInvokersFunc methodInvokersFunc = (q, c) -> {
            for (MIF_IfComponent ifComponent : ifComponents)
                if (ifComponent.isSatisfied(q, c)) return ifComponent.getReturnValue();
            return new ArrayList<>();
        };

        return methodInvokersFunc;
    }
}
