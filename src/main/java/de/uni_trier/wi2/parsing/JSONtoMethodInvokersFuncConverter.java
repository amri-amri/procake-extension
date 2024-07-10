package de.uni_trier.wi2.parsing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.ValidationMessage;
import de.uni_trier.wi2.parsing.model.LogicalOrConditionComponent;
import de.uni_trier.wi2.parsing.model.MIF_IfComponent;
import de.uni_trier.wi2.parsing.model.MethodListComponent;
import de.uni_trier.wi2.parsing.model.StringComponent;
import de.uni_trier.wi2.utils.MethodInvoker;
import de.uni_trier.wi2.utils.MethodInvokersFunc;
import de.uni_trier.wi2.utils.SimilarityMeasureFunc;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.uni_trier.wi2.utils.IOUtils.getResourceAsString;

public class JSONtoMethodInvokersFuncConverter extends JSONtoFunctionConverter {

    /**
     * <p>returns the MethodInvokersFunc of which the passed JSON file is the representation of
     *
     * <p>The actual converting of the nodes contained in the JSON happens everytime the MethodInvokersFunc is called.
     *
     * <p>Make sure that the file makes use of the respective JSON schema in order to avoid runtime errors while converting.
     *
     * @param file the JSON file whose content represents the MethodInvokersFunc
     * @return the MethodInvokersFunc generated from the JSON file
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static MethodInvokersFunc getMethodInvokersFunc(File file) throws ParserConfigurationException, IOException, SAXException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        if (file == null) {
            return MethodInvokersFunc.getDefault();
        }

        return (getMethodInvokersFunc(Files.readString(file.toPath())));
    }

    /**
     * <p>returns the MethodInvokersFunc of which the passed JSON file is the representation of
     *
     * <p>The actual converting of the nodes contained in the JSON happens everytime the MethodInvokersFunc is called.
     *
     * <p>Make sure that the file makes use of the respective JSON schema in order to avoid runtime errors while converting.
     *
     * @param str the String whose content represents the MethodInvokersFunc
     * @return the MethodInvokersFunc generated from the XML file
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static MethodInvokersFunc getMethodInvokersFunc(String str) throws ParserConfigurationException, IOException, SAXException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        if (str == null) {
            return MethodInvokersFunc.getDefault();
        }

        String schema = getResourceAsString("jschema/method-invokers-function.jschema");

        Set<ValidationMessage> validationMessages = validate(schema, str);
        if (!validationMessages.isEmpty()) {
            throw new RuntimeException(validationMessages.toString());
        }

        ObjectMapper om = new ObjectMapper();
        Map<String, Object> map = om.readValue(str, Map.class);

        MethodInvokersFunc methodInvokersFunc = getMethodInvokersFunc(map);

        return methodInvokersFunc;
    }

    /**
     * <p>Converts the passed {@link Map} to a {@link MethodInvokersFunc}
     *
     * @param map the Map which is to be converted into a MethodInvokersFunc
     * @return the MethodInvokersFunc generated from the Map
     */
    private static MethodInvokersFunc getMethodInvokersFunc(Map map) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        // Get root element
        assert (map.get("element-type").equals("method-invokers-function"));

        // Get if statements
        List ifStatements = (List) map.get("if-statements");

        final int amountOfIfStatements = ifStatements.size();

        MIF_IfComponent[] ifComponents = new MIF_IfComponent[amountOfIfStatements];

        for (int i = 0; i < amountOfIfStatements; i++) {
            Map ifStatement = (Map) ifStatements.get(i);

            Map condition = (Map) ifStatement.get("condition");
            Map returnValue = (Map) ifStatement.get("return-value");

            LogicalOrConditionComponent conditionComponent = (LogicalOrConditionComponent) evaluate(condition);
            MethodListComponent methodListComponent = (MethodListComponent) evaluate(returnValue);
            MIF_IfComponent ifComponent = new MIF_IfComponent(conditionComponent, methodListComponent);
            ifComponents[i] = ifComponent;
        }

        // Define the WeightFunc which computes the output according to the JSON
        MethodInvokersFunc methodInvokersFunc = (q, c) -> {
            for (MIF_IfComponent ifComponent : ifComponents)
                if (ifComponent.isSatisfied(q, c)) return ifComponent.getReturnValue();
            return null;
        };


        return methodInvokersFunc;
    }

}

