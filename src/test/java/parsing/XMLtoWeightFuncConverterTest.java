package parsing;

import base.SimpleTestBase;
import de.uni_trier.wi2.parsing.XMLtoWeightFuncConverter;
import de.uni_trier.wi2.procake.data.object.DataObject;
import org.junit.Test;
import org.xml.sax.SAXException;
import de.uni_trier.wi2.utils.WeightFunc;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class XMLtoWeightFuncConverterTest extends SimpleTestBase {

    @Test
    public void testAllNodes() throws ParserConfigurationException, IOException, SAXException {
        WeightFunc weightFunc = XMLtoWeightFuncConverter.getWeightFunc(new File("src/test/resources/de/uni_trier/wi2/xml/weight-function-test.xml"));

        DataObject q = utils.createIntegerObject(2);

        assertEquals(1, weightFunc.apply(q), 0);

        q = utils.createDoubleObject(2);

        assertEquals(1, weightFunc.apply(q), 0);

        q = utils.createIntegerObject(1);

        assertEquals(1, weightFunc.apply(q), 0);

        q = utils.createBooleanObject(true);

        assertEquals(0.3, weightFunc.apply(q), 0);

        q = utils.createBooleanObject(false);

        assertEquals(1, weightFunc.apply(q), 0);

        q = utils.createStringObject("abd");

        assertEquals(0.4, weightFunc.apply(q), 0);

        q = utils.createStringObject("abcd");

        assertEquals(0.4, weightFunc.apply(q), 0);

        q = utils.createStringObject("abccd");

        assertEquals(0.4, weightFunc.apply(q), 0);

        q = utils.createStringObject("dcba");

        assertEquals(1, weightFunc.apply(q), 0);




    }
}
