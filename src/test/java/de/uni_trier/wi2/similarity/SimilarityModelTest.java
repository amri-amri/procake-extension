package de.uni_trier.wi2.similarity;

import de.uni_trier.wi2.base.SimilarityModelTestBase;
import de.uni_trier.wi2.procake.data.model.DataClass;
import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.data.object.base.AggregateObject;
import de.uni_trier.wi2.procake.data.object.base.StringObject;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.SimilarityValuator;
import de.uni_trier.wi2.procake.similarity.impl.SimilarityImpl;
import de.uni_trier.wi2.procake.similarity.impl.SimilarityMeasureImpl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SimilarityModelTest extends SimilarityModelTestBase {

    @Test
    public void test_automatic_application_of_similarity_measures() {
        StringObject s1 = utils.createStringObject("s1", "SC1");
        StringObject s2 = utils.createStringObject("s2", "SC1");
        StringObject s3 = utils.createStringObject("s3", "SC2");
        StringObject s4 = utils.createStringObject("s4", "SC2");

        AggregateObject a1 = utils.createAggregateObject("AC1");
        AggregateObject a2 = utils.createAggregateObject("AC1");
        AggregateObject a3 = utils.createAggregateObject("AC2");
        AggregateObject a4 = utils.createAggregateObject("AC2");


        assertEquals(0.2, simVal.computeSimilarity(s1, s2).getValue(), delta);
        assertEquals(0.1, simVal.computeSimilarity(s1, s3).getValue(), delta);
        assertEquals(0.3, simVal.computeSimilarity(s3, s4).getValue(), delta);

        assertEquals(0.4, simVal.computeSimilarity(a1, a2).getValue(), delta);
        assertEquals(0.6, simVal.computeSimilarity(a1, a3).getValue(), delta);
        assertEquals(0.5, simVal.computeSimilarity(a3, a4).getValue(), delta);

        assertEquals(0.0, simVal.computeSimilarity(s1, a1).getValue(), delta);
        assertEquals(0.0, simVal.computeSimilarity(s1, a2).getValue(), delta);
        assertEquals(0.0, simVal.computeSimilarity(s1, a3).getValue(), delta);
        assertEquals(0.0, simVal.computeSimilarity(s2, a1).getValue(), delta);
        assertEquals(0.0, simVal.computeSimilarity(s2, a2).getValue(), delta);
        assertEquals(0.0, simVal.computeSimilarity(s2, a3).getValue(), delta);
        assertEquals(0.0, simVal.computeSimilarity(s3, a1).getValue(), delta);
        assertEquals(0.0, simVal.computeSimilarity(s3, a2).getValue(), delta);
        assertEquals(0.0, simVal.computeSimilarity(s3, a3).getValue(), delta);

        assertEquals(0.2, simVal.computeSimilarity(s1, s1).getValue(), delta);
        assertEquals(0.2, simVal.computeSimilarity(s2, s2).getValue(), delta);
        assertEquals(0.3, simVal.computeSimilarity(s3, s3).getValue(), delta);
        assertEquals(0.3, simVal.computeSimilarity(s4, s4).getValue(), delta);

        assertEquals(0.4, simVal.computeSimilarity(a1, a1).getValue(), delta);
        assertEquals(0.4, simVal.computeSimilarity(a2, a2).getValue(), delta);
        assertEquals(0.5, simVal.computeSimilarity(a3, a3).getValue(), delta);
        assertEquals(0.5, simVal.computeSimilarity(a4, a4).getValue(), delta);


        a1.setAttributeValue("x", utils.createStringObject("s1", "SC1"));
        a1.setAttributeValue("y", utils.createStringObject("s2"));

        a3.setAttributeValue("x", utils.createStringObject("s1", "SC2"));
        a3.setAttributeValue("y", utils.createStringObject("s2"));
        SimilarityMeasureImpl sma3 = new SimilarityMeasureImpl() {
            @Override
            public Similarity compute(DataObject dataObject, DataObject dataObject1, SimilarityValuator similarityValuator) {
                double val = simVal.computeSimilarity(((AggregateObject) dataObject).getAttributeValue("x"), ((AggregateObject) dataObject1).getAttributeValue("x")).getValue();
                val += simVal.computeSimilarity(((AggregateObject) dataObject).getAttributeValue("y"), ((AggregateObject) dataObject1).getAttributeValue("y")).getValue();


                return new SimilarityImpl(this, dataObject, dataObject1, val);
            }

            @Override
            public DataClass getDataClass() {
                return model.getAggregateSystemClass();
            }

            @Override
            public String getName() {
                return "SMA3";
            }

            @Override
            public boolean isSimilarityFor(DataClass dataClass, String s) {
                return dataClass == getDataClass();
            }

            @Override
            public String getSystemName() {
                return "SMA3";
            }

            @Override
            public boolean isForceOverride() {
                return false;
            }

            @Override
            public void setForceOverride(boolean b) {
                super.setForceOverride(b);
            }

            @Override
            public boolean isReusable() {
                return true;
            }
        };

        addSimilarityMeasureToSimilarityModel(sma3, model.getAggregateSystemClass());;

        similarityModel.setDefaultSimilarityMeasure(model.getAggregateSystemClass(), "SMA3");

        assertEquals(0.2, simVal.computeSimilarity(a1, a3).getValue(), delta);
    }

    @Test
    public void test_different_subclass_same_similarity_measure(){
        SimilarityMeasureImpl sms3 = new SimilarityMeasureImpl() {
            @Override
            public boolean isSimilarityFor(DataClass dataClass, String s) {
                return dataClass.isSubclassOf(model.getStringSystemClass());
            }

            @Override
            public Similarity compute(DataObject dataObject, DataObject dataObject1, SimilarityValuator similarityValuator) {
                return new SimilarityImpl(this, dataObject, dataObject1, 0.7);
            }

            @Override
            public String getSystemName() {
                return "SMS3";
            }
        };

        addSimilarityMeasureToSimilarityModel(sms3, model.getClass("SC1"));
        addSimilarityMeasureToSimilarityModel(sms3, model.getClass("SC2"));

        StringObject s5 = model.createObject("SC1");
        StringObject s6 = model.createObject("SC2");

        assertEquals(0.1, simVal.computeSimilarity(s5, s6).getValue(), delta);

        // Even though SMS3 is defined for both classes SC1 and SC2, SMS0 is chosen as similarity measure.
        // That is because the similarity valuator first looks for the next common class of s5 and s6 on which a
        // similarity measure is defined. In this case it is the string system class.
    }

}
