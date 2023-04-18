package test;

import de.uni_trier.wi2.procake.data.model.ModelFactory;
import de.uni_trier.wi2.procake.data.object.base.ListObject;
import de.uni_trier.wi2.procake.data.object.base.StringObject;
import de.uni_trier.wi2.procake.similarity.Similarity;
import extension.SimilarityMeasures.SMCollectionMappingImplExt;
import extension.SimilarityMeasures.SMListDTWImplExt;
import extension.SimilarityValuatorImplExt;
import org.junit.Assert;
import org.junit.Test;
import utils.MethodInvoker;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class SMListDTWImplExtTest extends CollectionSimilarityTest {

    @Test
    public void test1(){
        ListObject queryList = (ListObject) ModelFactory.getDefaultModel().createObject(LIST_CLASS_NAME);

        queryList.addValue(utils.createStringObject("A"));
        queryList.addValue(utils.createStringObject("B"));
        queryList.addValue(utils.createStringObject("C"));

        ListObject caseList = (ListObject) ModelFactory.getDefaultModel().createObject(LIST_CLASS_NAME);
        caseList.addValue(utils.createStringObject("A"));
        caseList.addValue(utils.createStringObject("B"));
        caseList.addValue(utils.createStringObject("B"));
        caseList.addValue(utils.createStringObject("C"));

        SMListDTWImplExt sm = new SMListDTWImplExt();
        sm.setLocalSimilarityToUse("SMStringEqual");

        Similarity sim = sm.compute(queryList, caseList, simVal);

        Assert.assertEquals(1., sim.getValue(), delta);
    }

    @Test
    public void test2(){
        ListObject queryList = (ListObject) ModelFactory.getDefaultModel().createObject(LIST_CLASS_NAME);


        queryList.addValue(utils.createStringObject("D"));
        queryList.addValue(utils.createStringObject("A"));
        queryList.addValue(utils.createStringObject("D"));
        queryList.addValue(utils.createStringObject("D"));
        queryList.addValue(utils.createStringObject("B"));
        queryList.addValue(utils.createStringObject("D"));
        queryList.addValue(utils.createStringObject("D"));
        queryList.addValue(utils.createStringObject("D"));
        queryList.addValue(utils.createStringObject("D"));
        queryList.addValue(utils.createStringObject("C"));
        queryList.addValue(utils.createStringObject("C"));
        queryList.addValue(utils.createStringObject("C"));
        queryList.addValue(utils.createStringObject("D"));
        queryList.addValue(utils.createStringObject("D"));
        queryList.addValue(utils.createStringObject("D"));

        ListObject caseList = (ListObject) ModelFactory.getDefaultModel().createObject(LIST_CLASS_NAME);
        caseList.addValue(utils.createStringObject("A"));
        caseList.addValue(utils.createStringObject("A"));
        caseList.addValue(utils.createStringObject("B"));
        caseList.addValue(utils.createStringObject("B"));
        caseList.addValue(utils.createStringObject("B"));
        caseList.addValue(utils.createStringObject("C"));

        SMListDTWImplExt sm = new SMListDTWImplExt();
        sm.setLocalSimilarityToUse("SMStringEqual");
        sm.setWeightFunction(a -> {
            if (((StringObject) a).getNativeString().equals("D")) return 0.;
            return 1.;
        });
        sm.setHalvingDistancePercentage(0.3);

        Similarity sim = sm.compute(queryList, caseList, simVal);

        Assert.assertEquals(1., sim.getValue(), delta);
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

        SMListDTWImplExt sm = new SMListDTWImplExt();
        sm.setLocalSimilarityToUse("SMStringLevenshtein");
        sm.setMethodInvokerFunc((a,b)->{
            MethodInvoker mi = new MethodInvoker("setCaseSensitive", new Class[]{}, new Object[]{});
            ArrayList<MethodInvoker> list = new ArrayList<>();
            list.add(mi);
            return list;
        });


        Similarity sim = sm.compute(queryList, caseList, simValExt);
        assertEquals(8./15, sim.getValue(), delta);


        sm = new SMListDTWImplExt();
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
