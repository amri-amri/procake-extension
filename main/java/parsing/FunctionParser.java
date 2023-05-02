package parsing;

import de.uni_trier.wi2.procake.data.object.DataObject;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import utils.MethodInvoker;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class FunctionParser {

    protected static boolean initialized = false;
    protected static void initialize() throws ParserConfigurationException, ParserConfigurationException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setValidating(true);
        dbFactory.setIgnoringElementContentWhitespace(true);
        dBuilder = dbFactory.newDocumentBuilder();
        dBuilder.setErrorHandler(new ErrorHandler() {
            @Override
            public void warning(SAXParseException exception) throws SAXException {
                exception.printStackTrace();
            }

            @Override
            public void error(SAXParseException exception) throws SAXException {
                exception.printStackTrace();
            }

            @Override
            public void fatalError(SAXParseException exception) throws SAXException {
                exception.printStackTrace();
            }
        });
    }

    protected static DocumentBuilder dBuilder;

    protected static Object evaluate(Node node, DataObject q, DataObject c) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        switch (node.getNodeName()){
            case "and":
                NodeList conditions = node.getChildNodes();
                boolean value = true;
                for (int i = 0; i<conditions.getLength(); i++){
                    if (! (boolean) evaluate(conditions.item(i), q, c)) {
                        value = false;
                        break;
                    }
                }
                return value;
            case "or":
                conditions = node.getChildNodes();
                value = false;
                for (int i = 0; i<conditions.getLength(); i++){
                    if ((boolean) evaluate(conditions.item(i), q, c)) {
                        value = true;
                        break;
                    }
                }
                return value;
            case "not":
                return ! (boolean) evaluate(node.getFirstChild(), q, c);
            case "equals":
                Object a = evaluate(node.getChildNodes().item(0), q, c);
                Object b = evaluate(node.getChildNodes().item(1), q, c);
                return a.equals(b);
            case "same-object-as":
                a = evaluate(node.getChildNodes().item(0), q, c);
                b = evaluate(node.getChildNodes().item(1), q, c);
                return a == b;
            case "instance-of":
                a = evaluate(node.getChildNodes().item(0), q, c);
                Class clazz = Class.forName((String) evaluate(node.getChildNodes().item(1), q, c));
                //return a.getClass().equals(clazz);
                return clazz.isInstance(a);
            case "regex":
                Pattern pattern = Pattern.compile( (String) evaluate(node.getChildNodes().item(0), q, c) );
                Matcher matcher = pattern.matcher( (String) evaluate(node.getChildNodes().item(1), q, c) );
                return matcher.find();
            case "q":
                return q;
            case "c":
                return c;
            case "string":
                return node.getAttributes().item(0).getNodeValue();
            case "double":
                return Double.parseDouble(node.getAttributes().item(0).getNodeValue());
            case "boolean":
                return Boolean.parseBoolean(node.getAttributes().item(0).getNodeValue());
            case "method-return-value":
                MethodInvoker methodInvoker = (MethodInvoker) evaluate(node.getChildNodes().item(1), q, c);
                return methodInvoker.invoke(evaluate(node.getChildNodes().item(0), q, c));
            case "method":
                String methodName = node.getAttributes().item(0).getNodeValue();
                NodeList children = node.getChildNodes();
                int numParams = children.getLength();

                Class[] classes = new Class[numParams];
                Object[] objects = new Object[numParams];

                clazz = null;

                for (int i = 0; i < numParams; i++){
                    Node child = children.item(i);
                    if (child.getNodeName().equals("string")) clazz = String.class;
                    if (child.getNodeName().equals("double")) clazz = double.class;
                    if (child.getNodeName().equals("boolean")) clazz = boolean.class;
                    classes[i] = clazz;
                    objects[i] = evaluate(child, q, c);
                }

                return new MethodInvoker(methodName, classes, objects);
            case "method-list":
                ArrayList<MethodInvoker> methodInvokers = new ArrayList<>();
                children = node.getChildNodes();
                for (int i = 0; i < children.getLength(); i++ ){
                    methodInvokers.add( (MethodInvoker) evaluate(children.item(i), q, c));
                }
                return methodInvokers;
            default:
                break;
        }
        return null;
    }
}