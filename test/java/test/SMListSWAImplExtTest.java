package test;

import de.uni_trier.wi2.procake.data.model.ModelFactory;
import de.uni_trier.wi2.procake.data.object.base.ListObject;
import de.uni_trier.wi2.procake.data.object.base.StringObject;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.base.string.impl.SMStringLevenshteinImpl;
import extension.SimilarityMeasures.SMListDTWImplExt;
import extension.SimilarityMeasures.SMListSWAImplExt;
import extension.SimilarityValuatorImplExt;
import org.junit.Assert;
import org.junit.Test;
import utils.MethodInvoker;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

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

    @Test
    public void test3(){
        SimilarityValuatorImplExt simValExt = new SimilarityValuatorImplExt(simVal.getSimilarityModel());

        ListObject queryList = (ListObject) ModelFactory.getDefaultModel().createObject(LIST_CLASS_NAME);
        ListObject caseList = (ListObject) ModelFactory.getDefaultModel().createObject(LIST_CLASS_NAME);

        queryList.addValue(utils.createStringObject("Abc"));
        queryList.addValue(utils.createStringObject("dEf"));

        caseList.addValue(utils.createStringObject("abC"));
        caseList.addValue(utils.createStringObject("DeF"));
        caseList.addValue(utils.createStringObject("dEf"));

        SMListSWAImplExt sm = new SMListSWAImplExt();
        sm.setLocalSimilarityToUse("SMStringLevenshtein");
        sm.setMethodInvokerFunc((a,b)->{
            MethodInvoker mi = new MethodInvoker("setCaseSensitive", new Class[]{}, new Object[]{});
            ArrayList<MethodInvoker> list = new ArrayList<>();
            list.add(mi);
            return list;
        });


        Similarity sim = sm.compute(queryList, caseList, simValExt);
        assertEquals(0.5, sim.getValue(), delta);

        sm = new SMListSWAImplExt();
        sm.setLocalSimilarityToUse("SMStringLevenshtein");
        sm.setMethodInvokerFunc((a,b)->{
            MethodInvoker mi = new MethodInvoker("setCaseInsensitive", new Class[]{}, new Object[]{});
            ArrayList<MethodInvoker> list = new ArrayList<>();
            list.add(mi);
            return list;
        });


        sim = sm.compute(queryList, caseList, simValExt);
        assertEquals(1., sim.getValue(), delta);
    }
}
