package de.uni_trier.wi2.parsing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.ValidationMessage;
import de.uni_trier.wi2.parsing.model.DoubleComponent;
import de.uni_trier.wi2.parsing.model.LogicalOrConditionComponent;
import de.uni_trier.wi2.parsing.model.WF_IfComponent;
import de.uni_trier.wi2.utils.WeightFunc;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.uni_trier.wi2.utils.IOUtils.getResourceAsString;

public class JSONtoWeightFuncConverter extends JSONtoFunctionConverter {

    /**
     * <p>returns the WeightFunc of which the passed JSON file is the representation of
     *
     * <p>The actual converting of the nodes contained in the JSON happens everytime the WeightFunc is called.
     *
     * <p>Make sure that the file makes use of the respective JSON schema in order to avoid runtime errors while converting.
     *
     * @param file the JSON file whose content represents the WeightFunc
     * @return the WeightFunc generated from the JSON file
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

        return (getWeightFunc(Files.readString(file.toPath())));
    }

    /**
     * <p>returns the WeightFunc of which the passed JSON file is the representation of
     *
     * <p>The actual converting of the nodes contained in the JSON happens everytime the WeightFunc is called.
     *
     * <p>Make sure that the file makes use of the respective JSON schema in order to avoid runtime errors while converting.
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

        String schema = getResourceAsString("jschema/weight-function.jschema");

        Set<ValidationMessage> validationMessages = validate(schema, str);
        if (!validationMessages.isEmpty()) {
            throw new RuntimeException(validationMessages.toString());
        }

        ObjectMapper om = new ObjectMapper();
        Map<String, Object> map = om.readValue(str, Map.class);

        WeightFunc weightFunc = getWeightFunc(map);

        return weightFunc;
    }

    /**
     * <p>Converts the passed {@link Map} to a {@link WeightFunc}
     *
     * @param map the Map which is to be converted into a WeightFunc
     * @return the WeightFunc generated from the Map
     */
    private static WeightFunc getWeightFunc(Map map) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        // Get root element
        assert (map.get("element-type").equals("weight-function"));

        // Get if statements
        List ifStatements = (List) map.get("if-statements");

        final int amountOfIfStatements = ifStatements.size();

        WF_IfComponent[] ifComponents = new WF_IfComponent[amountOfIfStatements];

        for (int i = 0; i < amountOfIfStatements; i++) {
            Map ifStatement = (Map) ifStatements.get(i);

            Map condition = (Map) ifStatement.get("condition");
            Map returnValue = (Map) ifStatement.get("return-value");

            LogicalOrConditionComponent conditionComponent = (LogicalOrConditionComponent) evaluate(condition);
            DoubleComponent doubleComponent = (DoubleComponent) evaluate(returnValue);
            WF_IfComponent ifComponent = new WF_IfComponent(conditionComponent, doubleComponent);
            ifComponents[i] = ifComponent;
        }

        // Define the WeightFunc which computes the output according to the JSON
        WeightFunc weightFunc = (q) -> {
            for (WF_IfComponent ifComponent : ifComponents)
                if (ifComponent.isSatisfied(q, null)) return ifComponent.getReturnValue();
            return 1;
        };

        return weightFunc;
    }

}

