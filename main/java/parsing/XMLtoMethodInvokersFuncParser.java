package parsing;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import utils.MethodInvoker;
import utils.MethodInvokersFunc;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class XMLtoMethodInvokersFuncParser extends FunctionParser {

    public static MethodInvokersFunc getMethodInvokersFunc(File file) throws ParserConfigurationException, IOException, SAXException, IOException, SAXException, ParserConfigurationException {
        if (!initialized) initialize();

        Document doc = dBuilder.parse(file);
        Node root = doc.getElementsByTagName("weight-function").item(0);
        root.normalize();

        NodeList ifStatements = root.getChildNodes();

        MethodInvokersFunc methodInvokersFunc = (q, c) -> {
            ArrayList<MethodInvoker> methodInvokers = new ArrayList<>();

            for (int i = 0; i < ifStatements.getLength(); i++){
                Node ifStatement = ifStatements.item(i);

                Node condition = ifStatement.getChildNodes().item(0);
                Node returnValue = ifStatement.getChildNodes().item(1);

                try {
                    if ((boolean) evaluate(condition, q, c)) methodInvokers.addAll( (ArrayList<MethodInvoker>)  evaluate(returnValue, q, c));
                } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException |
                         IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

            }
            return methodInvokers;
        };
        return methodInvokersFunc;
    }

}
