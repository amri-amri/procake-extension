package de.uni_trier.wi2.parsing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.ValidationMessage;
import de.uni_trier.wi2.utils.SimilarityMeasureFunc;
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

public class JSONtoSimilarityMeasureFuncConverter extends JSONtoFunctionConverter {

    /**
     * <p>returns the SimilarityMeasureFunc of which the passed JSON file is the representation of
     *
     * <p>The actual converting of the nodes contained in the JSON happens everytime the SimilarityMeasureFunc is called.
     *
     * <p>Make sure that the file makes use of the respective JSON schema in order to avoid runtime errors while converting.
     *
     * @param file the JSON file whose content represents the SimilarityMeasureFunc
     * @return the SimilarityMeasureFunc generated from the JSON file
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static SimilarityMeasureFunc getSimilarityMeasureFunc(File file) throws ParserConfigurationException, IOException, SAXException {
        if (file == null) {
            return SimilarityMeasureFunc.getDefault();
        }

        return (getSimilarityMeasureFunc(Files.readString(file.toPath())));
    }

    /**
     * <p>returns the SimilarityMeasureFunc of which the passed JSON file is the representation of
     *
     * <p>The actual converting of the nodes contained in the JSON happens everytime the SimilarityMeasureFunc is called.
     *
     * <p>Make sure that the file makes use of the respective JSON schema in order to avoid runtime errors while converting.
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
    public static SimilarityMeasureFunc getSimilarityMeasureFunc(String str) throws ParserConfigurationException, IOException, SAXException {
        if (str == null) {
            return SimilarityMeasureFunc.getDefault();
        }

        String schema = getResourceAsString("jschema/similarity-measure-function.jschema");

        Set<ValidationMessage> validationMessages = validate(schema, str);
        if (!validationMessages.isEmpty()) {
            throw new RuntimeException(validationMessages.toString());
        }

        ObjectMapper om = new ObjectMapper();
        Map<String, Object> map = om.readValue(str, Map.class);

        SimilarityMeasureFunc similarityMeasureFunc = getSimilarityMeasureFunc(map);

        return similarityMeasureFunc;
    }

    /**
     * <p>Converts the passed {@link Map} to a {@link SimilarityMeasureFunc}
     *
     * @param map the Map which is to be converted into a SimilarityMeasureFunc
     * @return the SimilarityMeasureFunc generated from the Map
     */
    private static SimilarityMeasureFunc getSimilarityMeasureFunc(Map map) {
        // Get root element
        assert (map.get("element-type").equals("similarity-measure-function"));

        List ifStatements = (List) map.get("if-statements");

        // Define the SimilarityMeasureFunc which computes the output according to the JSON
        SimilarityMeasureFunc similarityMeasureFunc = (q, c) -> {


            // It is important that the evaluation of the "if" nodes happens in the order of the
            //  definition in the JSON file. This guarantees that an author of such a file can implicitly define
            //  an "else" or "else if" condition.

            for (int i = 0; i < ifStatements.size(); i++) {

                // If the evaluation of the "condition" property of the "if" node 
                //  returns true, the "return-value" property of the "if" node, a "string" property, is
                //  evaluated and the generated String object is returned.

                Map ifStatement = (Map) ifStatements.get(i);

                Map condition = (Map) ifStatement.get("condition");
                Map returnValue = (Map) ifStatement.get("return-value");

                try {
                    boolean ifStatementEvaluated = true;
                    if (condition != null) ifStatementEvaluated = (boolean) evaluate(condition).evaluate(q,c);

                    if (ifStatementEvaluated) {
                        String similarityMeasure = null;
                        if (returnValue != null) similarityMeasure = (String) evaluate(returnValue).evaluate(q,c);

                        return similarityMeasure;
                    }
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

