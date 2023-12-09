package de.uni_trier.wi2.parsing;

import de.uni_trier.wi2.utils.IOUtils;
import de.uni_trier.wi2.utils.SimilarityMeasureFunc;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class XMLtoSimilarityMeasureFuncParserTest {

    @Test
    public void test1() throws IOException, ParserConfigurationException, SAXException {

        String similarityMeasureFuncXML = IOUtils.getResourceAsString("/xml/similaritymeasure-func-test.xml");

        SimilarityMeasureFunc similarityMeasureFunc = XMLtoSimilarityMeasureFuncConverter.getSimilarityMeasureFunc(similarityMeasureFuncXML);

    }
}
