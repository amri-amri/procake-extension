package de.uni_trier.wi2.parsing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.ValidationMessage;
import de.uni_trier.wi2.utils.MethodInvoker;
import de.uni_trier.wi2.utils.MethodInvokersFunc;
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
    public static MethodInvokersFunc getMethodInvokersFunc(File file) throws ParserConfigurationException, IOException, SAXException {
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
    public static MethodInvokersFunc getMethodInvokersFunc(String str) throws ParserConfigurationException, IOException, SAXException {
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
    private static MethodInvokersFunc getMethodInvokersFunc(Map map) {
        // Get root element
        assert (map.get("element-type").equals("method-invokers-function"));

        List ifStatements = (List) map.get("if-statements");

        // Define the MethodInvokersFunc which computes the output according to the JSON
        MethodInvokersFunc methodInvokersFunc = (q, c) -> {


            // It is important that the evaluation of the "if" nodes happens in the order of the
            //  definition in the JSON file. This guarantees that an author of such a file can implicitly define
            //  an "else" or "else if" condition.

            for (int i = 0; i < ifStatements.size(); i++) {

                // If the evaluation of the "condition" property of the "if" node
                //  returns true, the "return-value" property of the "if" node, a "method-list" property, is
                //  evaluated and the generated ArrayList<MethodInvoker> object is returned.

                Map ifStatement = (Map) ifStatements.get(i);

                Map condition = (Map) ifStatement.get("condition");
                Map returnValue = (Map) ifStatement.get("return-value");

                try {
                    boolean ifStatementEvaluated = true;
                    if (condition != null) ifStatementEvaluated = (boolean) evaluate(condition).evaluate(q,c);

                    if (ifStatementEvaluated) {
                        ArrayList<MethodInvoker> methodInvokers = new ArrayList<>();
                        if (returnValue != null)
                            methodInvokers = (ArrayList<MethodInvoker>) evaluate(returnValue).evaluate(q,c);

                        return methodInvokers;
                    }
                } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException |
                         IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

            }

            return new ArrayList<>();
        };

        return methodInvokersFunc;
    }

}

