package de.uni_trier.wi2.similarity;

import de.uni_trier.wi2.procake.data.object.base.ListObject;
import de.uni_trier.wi2.procake.data.object.base.StringObject;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.base.collection.SMListCorrectness;
import de.uni_trier.wi2.extension.similarity.measure.collection.SMListCorrectnessExt;
import de.uni_trier.wi2.extension.similarity.measure.collection.SMListCorrectnessImplExt;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SMListCorrectnessImplExtTest extends SimilarityMeasureTest {

    {
        name = SMListCorrectnessExt.NAME;
        superclassName = SMListCorrectness.NAME;
    }

    @Test
    public void test1(){
        ListObject queryList = utils.createListObject();;

        queryList.addValue(utils.createStringObject("A"));
        queryList.addValue(utils.createStringObject("B"));
        queryList.addValue(utils.createStringObject("C"));
        queryList.addValue(utils.createStringObject("D"));

        ListObject caseList = utils.createListObject();;
        caseList.addValue(utils.createStringObject("A"));
        caseList.addValue(utils.createStringObject("B"));
        caseList.addValue(utils.createStringObject("D"));
        caseList.addValue(utils.createStringObject("C"));

        SMListCorrectnessImplExt sm = new SMListCorrectnessImplExt();

        Similarity sim = sm.compute(queryList, caseList, simVal);

        assertEquals(4./6, sim.getValue(), delta);


        caseList.addValue(utils.createStringObject("C"));

        sm = new SMListCorrectnessImplExt();

        sim = sm.compute(queryList, caseList, simVal);

        assertEquals(-1., sim.getValue(), delta);
    }

    @Test
    public void test2(){
        ListObject queryList = utils.createListObject();;

        queryList.addValue(utils.createStringObject("A"));
        queryList.addValue(utils.createStringObject("B"));
        queryList.addValue(utils.createStringObject("C"));
        queryList.addValue(utils.createStringObject("D"));

        ListObject caseList = utils.createListObject();;
        caseList.addValue(utils.createStringObject("A"));
        caseList.addValue(utils.createStringObject("B"));
        caseList.addValue(utils.createStringObject("D"));
        caseList.addValue(utils.createStringObject("C"));

        SMListCorrectnessImplExt sm = new SMListCorrectnessImplExt();
        sm.setWeightFunc(a -> {
            if (((StringObject) a).getNativeString().equals("C")) return 0.;
            return 1.;
        });

        Similarity sim = sm.compute(queryList, caseList, simVal);

        assertEquals(1., sim.getValue(), delta);


        sm = new SMListCorrectnessImplExt();
        sm.setWeightFunc(a -> {
            if (((StringObject) a).getNativeString().equals("B")) return 0.;
            return 1.;
        });

        sim = sm.compute(queryList, caseList, simVal);

        assertEquals(1./3, sim.getValue(), delta);


        sm = new SMListCorrectnessImplExt();
        sm.setWeightFunc(a -> {
            if (((StringObject) a).getNativeString().equals("B") || ((StringObject) a).getNativeString().equals("C")) return 0.5;
            return 1.;
        });

        sim = sm.compute(queryList, caseList, simVal);

        assertEquals(2.25/3.25, sim.getValue(), delta);



    }
}
