package similarity;

import de.uni_trier.wi2.procake.data.object.base.ListObject;
import de.uni_trier.wi2.procake.data.object.base.StringObject;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.base.collection.SMListMapping;
import de.uni_trier.wi2.procake.similarity.base.string.SMStringEqual;
import de.uni_trier.wi2.procake.similarity.base.string.SMStringLevenshtein;
import extension.similarity.measure.collection.SMListMappingExt;
import extension.similarity.measure.collection.SMListMappingImplExt;
import extension.similarity.valuator.SimilarityValuatorImplExt;
import org.junit.Test;
import utils.MethodInvoker;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class SMListMappingImplExtTest extends ISimilarityMeasureFuncTest {

    {
        name = SMListMappingExt.NAME;
        superclassName = SMListMapping.NAME;
    }

    @Test
    public void test1(){
        ListObject queryList = workdays();
        ListObject caseList = weekdays();

        SMListMappingImplExt sm = new SMListMappingImplExt();
        sm.setSimilarityToUse(SMStringEqual.NAME);

        Similarity sim = sm.compute(queryList, caseList, simVal);

        assertEquals(1.0, sim.getValue(),delta);

        sm = new SMListMappingImplExt();
        sm.setSimilarityToUse(SMStringEqual.NAME);

        sim = sm.compute(caseList, queryList, simVal);

        assertEquals(1.0, sim.getValue(),delta);
    }

    @Test
    public void test2(){
        ListObject queryList = utils.createListObject();

        queryList.addValue(utils.createStringObject("A"));
        queryList.addValue(utils.createStringObject("B"));
        queryList.addValue(utils.createStringObject("C"));

        ListObject caseList = utils.createListObject();
        caseList.addValue(utils.createStringObject("A"));
        caseList.addValue(utils.createStringObject("B"));
        caseList.addValue(utils.createStringObject("B"));
        caseList.addValue(utils.createStringObject("C"));

        SMListMappingImplExt sm = new SMListMappingImplExt();
        sm.setSimilarityToUse(SMStringEqual.NAME);

        Similarity sim = sm.compute(queryList, caseList, simVal);

        assertEquals(2./3, sim.getValue(), delta);

        sm = new SMListMappingImplExt();
        sm.setSimilarityToUse(SMStringEqual.NAME);
        sm.setWeightFunc(a -> {
            if (((StringObject) a).getNativeString().equals("C")) return 0.;
            return 1.;
        });

        sim = sm.compute(queryList, caseList, simVal);

        assertEquals(1., sim.getValue(), delta);




        sm = new SMListMappingImplExt();
        sm.setSimilarityToUse(SMStringEqual.NAME);
        sm.setWeightFunc(a -> {
            if (((StringObject) a).getNativeString().equals("C")) return 0.5;
            return 1.;
        });

        sim = sm.compute(queryList, caseList, simVal);

        assertEquals(4./5, sim.getValue(), delta);



        sm = new SMListMappingImplExt();
        sm.setSimilarityToUse(SMStringEqual.NAME);
        sm.setWeightFunc(a -> {
            if (((StringObject) a).getNativeString().equals("C")) return 0.5;
            return 1.;
        });

        sim = sm.compute(caseList, queryList, simVal);

        assertEquals(2./3, sim.getValue(), delta);

    }

    @Test
    public void test3(){
        ListObject queryList = utils.createListObject();

        queryList.addValue(utils.createStringObject("A"));
        queryList.addValue(utils.createStringObject("B"));
        queryList.addValue(utils.createStringObject("C"));
        queryList.addValue(utils.createStringObject("D"));


        ListObject caseList = utils.createListObject();
        caseList.addValue(utils.createStringObject("A"));
        caseList.addValue(utils.createStringObject("B"));
        caseList.addValue(utils.createStringObject("B"));
        caseList.addValue(utils.createStringObject("D"));

        SMListMappingImplExt sm = new SMListMappingImplExt();
        sm.setSimilarityToUse(SMStringEqual.NAME);
        sm.setWeightFunc(a -> {
            if (((StringObject) a).getNativeString().equals("B")) return 0.;
            return 1.;
        });
        sm.setContainsExact();

        Similarity sim = sm.compute(queryList, caseList, simVal);

        assertEquals(2./3, sim.getValue(), delta);


        sm = new SMListMappingImplExt();
        sm.setSimilarityToUse(SMStringEqual.NAME);
        sm.setWeightFunc(a -> {
            if (((StringObject) a).getNativeString().equals("B")) return 0.;
            return 1.;
        });
        sm.setContainsExact();

        sim = sm.compute(caseList, queryList, simVal);

        assertEquals(1., sim.getValue(), delta);



        queryList.addValue(utils.createStringObject("E"));

        sm = new SMListMappingImplExt();
        sm.setSimilarityToUse(SMStringEqual.NAME);
        sm.setWeightFunc(a -> {
            if (((StringObject) a).getNativeString().equals("B")) return 0.;
            return 1.;
        });
        sm.setContainsExact();

        sim = sm.compute(queryList, caseList, simVal);

        assertEquals(0., sim.getValue(), delta);
    }

    @Test
    public void test4(){
        SimilarityValuatorImplExt simValExt = new SimilarityValuatorImplExt(simVal.getSimilarityModel());

        ListObject queryList = utils.createListObject();
        ListObject caseList = utils.createListObject();

        queryList.addValue(utils.createStringObject("Abc"));
        queryList.addValue(utils.createStringObject("dEf"));

        caseList.addValue(utils.createStringObject("abC"));
        caseList.addValue(utils.createStringObject("DeF"));
        caseList.addValue(utils.createStringObject("dEF"));

        SMListMappingImplExt sm = new SMListMappingImplExt();
        sm.setSimilarityToUse(SMStringLevenshtein.NAME);
        sm.setMethodInvokersFunc((a, b)->{
            MethodInvoker mi = new MethodInvoker("setCaseSensitive", new Class[]{}, new Object[]{});
            ArrayList<MethodInvoker> list = new ArrayList<>();
            list.add(mi);
            return list;
        });


        Similarity sim = sm.compute(queryList, caseList, simValExt);
        assertEquals(1./3, sim.getValue(), delta);

        sm = new SMListMappingImplExt();
        sm.setSimilarityToUse(SMStringLevenshtein.NAME);
        sm.setMethodInvokersFunc((a, b)->{
            MethodInvoker mi = new MethodInvoker("setCaseInsensitive", new Class[]{}, new Object[]{});
            ArrayList<MethodInvoker> list = new ArrayList<>();
            list.add(mi);
            return list;
        });


        sim = sm.compute(queryList, caseList, simValExt);
        assertEquals(1., sim.getValue(), delta);
    }
}
