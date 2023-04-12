package test;

import de.uni_trier.wi2.procake.data.model.ModelFactory;
import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.data.object.base.ListObject;
import de.uni_trier.wi2.procake.data.object.base.StringObject;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.base.collection.impl.SMListSWAImpl;
import extension.SMListSWAImplExt;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class SMListSWAImplExtTest extends CollectionSimilarityTest{

    @Test
    public void test1(){
        ListObject queryList = (ListObject) ModelFactory.getDefaultModel().createObject(LIST_CLASS_NAME);

        queryList.addValue(utils.createStringObject("A"));
        queryList.addValue(utils.createStringObject("Y"));
        queryList.addValue(utils.createStringObject("B"));
        queryList.addValue(utils.createStringObject("C"));
        queryList.addValue(utils.createStringObject("M"));
        queryList.addValue(utils.createStringObject("M"));

        ListObject caseList = (ListObject) ModelFactory.getDefaultModel().createObject(LIST_CLASS_NAME);
        caseList.addValue(utils.createStringObject("A"));
        caseList.addValue(utils.createStringObject("B"));
        caseList.addValue(utils.createStringObject("C"));
        caseList.addValue(utils.createStringObject("K"));
        caseList.addValue(utils.createStringObject("M"));
        caseList.addValue(utils.createStringObject("M"));
        caseList.addValue(utils.createStringObject("H"));

        SMListSWAImplExt sm = new SMListSWAImplExt();
        sm.setLocalSimilarityToUse("SMStringEqual");
        sm.setDeletionScheme(a -> -0.5);
        sm.setInsertionScheme(a -> -0.5);
        sm.setHalvingDistancePercentage(-1);

        Similarity sim = sm.compute(queryList, caseList, simVal);

        Assert.assertEquals(4./6, sim.getValue(), delta);

    }

    @Test
    public void test2(){
        ListObject queryList1 = (ListObject) ModelFactory.getDefaultModel().createObject(LIST_CLASS_NAME);

        queryList1.addValue(utils.createStringObject("A"));
        queryList1.addValue(utils.createStringObject("Y"));
        queryList1.addValue(utils.createStringObject("B"));
        queryList1.addValue(utils.createStringObject("C"));
        queryList1.addValue(utils.createStringObject("M"));
        queryList1.addValue(utils.createStringObject("M"));


        ListObject queryList2 = (ListObject) ModelFactory.getDefaultModel().createObject(LIST_CLASS_NAME);

        queryList2.addValue(utils.createStringObject("A"));
        queryList2.addValue(utils.createStringObject("B"));
        queryList2.addValue(utils.createStringObject("C"));
        queryList2.addValue(utils.createStringObject("M"));
        queryList2.addValue(utils.createStringObject("M"));

        ListObject caseList = (ListObject) ModelFactory.getDefaultModel().createObject(LIST_CLASS_NAME);
        caseList.addValue(utils.createStringObject("A"));
        caseList.addValue(utils.createStringObject("B"));
        caseList.addValue(utils.createStringObject("C"));
        caseList.addValue(utils.createStringObject("K"));
        caseList.addValue(utils.createStringObject("M"));
        caseList.addValue(utils.createStringObject("M"));
        caseList.addValue(utils.createStringObject("H"));

        SMListSWAImplExt sm1 = new SMListSWAImplExt();
        sm1.setLocalSimilarityToUse("SMStringEqual");
        sm1.setDeletionScheme(a -> -0.5);
        sm1.setInsertionScheme(a -> -0.5);
        sm1.setHalvingDistancePercentage(0.3);
        sm1.setWeightFunction(a -> {
            if (((StringObject) a).getNativeString().equals("Y")) return 0.;
            return 1.;
        });

        Similarity sim1 = sm1.compute(queryList1, caseList, simVal);


        SMListSWAImplExt sm2 = new SMListSWAImplExt();
        sm2.setLocalSimilarityToUse("SMStringEqual");
        sm2.setDeletionScheme(a -> -0.5);
        sm2.setInsertionScheme(a -> -0.5);
        sm2.setHalvingDistancePercentage(0.3);

        Similarity sim2 = sm2.compute(queryList2, caseList, simVal);

        Assert.assertEquals(sim2.getValue(), sim1.getValue(), delta);

    }
}
