package de.uni_trier.wi2.similarity;

import de.uni_trier.wi2.extension.similarity.measure.collection.SMCollectionIsolatedMappingExt;
import de.uni_trier.wi2.extension.similarity.measure.collection.SMCollectionIsolatedMappingImplExt;
import de.uni_trier.wi2.extension.similarity.valuator.SimilarityValuatorImplExt;
import de.uni_trier.wi2.procake.data.object.base.ListObject;
import de.uni_trier.wi2.procake.data.object.base.StringObject;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.base.collection.SMCollectionIsolatedMapping;
import de.uni_trier.wi2.procake.similarity.base.string.SMStringEqual;
import de.uni_trier.wi2.procake.similarity.base.string.SMStringLevenshtein;
import de.uni_trier.wi2.utils.MethodInvoker;
import de.uni_trier.wi2.utils.WeightFunc;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class SMCollectionIsolatedMappingImplExtTest extends ISimilarityMeasureFuncTest {

    {
        name = SMCollectionIsolatedMappingExt.NAME;
        superclassName = SMCollectionIsolatedMapping.NAME;
    }

    @Test
    public void test1(){
        ListObject queryList = workdays();
        ListObject caseList = weekdays();

        for (int n = 0; n<10; n++) {
            SMCollectionIsolatedMappingImplExt sm = new SMCollectionIsolatedMappingImplExt();
            sm.setSimilarityToUse(SMStringEqual.NAME);

            double[] randomWeights = new double[7];
            for (int r = 0; r < 7; r ++) {
                randomWeights[r] = Math.random();
            }
            WeightFunc wf = a -> {
                String stringValue = ((StringObject) a).getNativeString();
                for (int d = 0; d<7; d++) if (days[d].equals(stringValue)) return randomWeights[d];
                return 1.;
            };
            sm.setWeightFunc(wf);

            Similarity sim = sm.compute(queryList, caseList, simVal);
            assertEquals(1., sim.getValue(), delta);

        }
    }

    @Test
    public void test2(){
        ListObject queryList = weekdays();
        ListObject caseList = workdays();

        SMCollectionIsolatedMappingImplExt sm = new SMCollectionIsolatedMappingImplExt();
        sm.setSimilarityToUse(SMStringEqual.NAME);

        WeightFunc wf = a -> {
            String stringValue = ((StringObject) a).getNativeString();
            if (stringValue.equals("Saturday")) return 0.;
            if (stringValue.equals("Sunday")) return 0.;
            return 1.;
        };
        sm.setWeightFunc(wf);

        Similarity sim = sm.compute(queryList, caseList, simVal);
        assertEquals(1., sim.getValue(), delta);

    }

    @Test
    public void test3(){
        ListObject queryList = weekdays();
        ListObject caseList = workdays();

        SMCollectionIsolatedMappingImplExt sm = new SMCollectionIsolatedMappingImplExt();
        sm.setSimilarityToUse(SMStringEqual.NAME);

        WeightFunc wf = a -> {
            String stringValue = ((StringObject) a).getNativeString();
            if (stringValue.equals("Saturday")) return 0.5;
            if (stringValue.equals("Sunday")) return 0.5;
            return 1.;
        };
        sm.setWeightFunc(wf);

        Similarity sim = sm.compute(queryList, caseList, simVal);
        assertEquals(5./6, sim.getValue(), delta);

    }

    @Test
    public void test4(){
        ListObject queryList = weekdays();
        ListObject caseList = workdays();

        SMCollectionIsolatedMappingImplExt sm = new SMCollectionIsolatedMappingImplExt();
        sm.setSimilarityToUse(SMStringLevenshtein.NAME);

        sm.setWeightFunc(a -> 1.);

        Similarity sim = sm.compute(queryList, caseList, simVal);
        assertEquals(true, sim.getValue()>5./7 && sim.getValue()<1.);

    }

    @Test
    public void test5(){
        ListObject queryList = utils.createListObject();;
        ListObject caseList = workdays();

        SMCollectionIsolatedMappingImplExt sm = new SMCollectionIsolatedMappingImplExt();
        sm.setSimilarityToUse(SMStringEqual.NAME);

        Similarity sim = sm.compute(queryList, caseList, simVal);
        assertEquals(1., sim.getValue(), delta);

    }

    @Test
    public void test6(){
        ListObject queryList = weekdays();
        ListObject caseList = utils.createListObject();;

        SMCollectionIsolatedMappingImplExt sm = new SMCollectionIsolatedMappingImplExt();
        sm.setSimilarityToUse(SMStringEqual.NAME);

        Similarity sim = sm.compute(queryList, caseList, simVal);
        assertEquals(0., sim.getValue(), delta);

    }

    @Test
    public void test7(){
        SimilarityValuatorImplExt simValExt = new SimilarityValuatorImplExt(simVal.getSimilarityModel());

        ListObject queryList = utils.createListObject();;
        ListObject caseList = utils.createListObject();;

        queryList.addValue(utils.createStringObject("Abc"));
        queryList.addValue(utils.createStringObject("dEf"));

        caseList.addValue(utils.createStringObject("abC"));
        caseList.addValue(utils.createStringObject("DeF"));

        SMCollectionIsolatedMappingImplExt sm = new SMCollectionIsolatedMappingImplExt();
        sm.setSimilarityToUse(SMStringEqual.NAME);
        sm.setMethodInvokersFunc((a, b)->{
            MethodInvoker mi = new MethodInvoker("setCaseSensitive", new Class[]{}, new Object[]{});
            ArrayList<MethodInvoker> list = new ArrayList<>();
            list.add(mi);
            return list;
        });


        Similarity sim = sm.compute(queryList, caseList, simValExt);
        assertEquals(0., sim.getValue(), delta);


        sm = new SMCollectionIsolatedMappingImplExt();
        sm.setSimilarityToUse(SMStringEqual.NAME);
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
