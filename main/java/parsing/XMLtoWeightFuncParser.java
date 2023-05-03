package parsing;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import utils.WeightFunc;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class XMLtoWeightFuncParser extends FunctionParser{

    public static WeightFunc getWeightFunc(File file) throws ParserConfigurationException, IOException, SAXException {
        if (!initialized) initialize();

        Document doc = dBuilder.parse(file);
        Node root = doc.getElementsByTagName("weight-function").item(0);
        root.normalize();

        NodeList ifStatements = root.getChildNodes();

        WeightFunc weightFunc = (q) -> {
            for (int i = 0; i < ifStatements.getLength(); i++){
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
