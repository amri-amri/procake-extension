package test;

import de.uni_trier.wi2.procake.data.model.ModelFactory;
import de.uni_trier.wi2.procake.data.object.base.ListObject;
import de.uni_trier.wi2.procake.data.object.base.StringObject;
import de.uni_trier.wi2.procake.similarity.Similarity;
import extension.SMCollectionMappingImplExt;
import org.junit.Test;
import utils.WeightFunc;

import static org.junit.Assert.assertEquals;

public class SMCollectionMappingImplExtTest extends CollectionSimilarityTest{

    @Test
    public void test1(){
        ListObject queryList = workdays();
        ListObject caseList = weekdays();

        for (int n = 0; n<10; n++) {
            SMCollectionMappingImplExt sm = new SMCollectionMappingImplExt();
            sm.setMaxQueueSize(-1);
            sm.setSimilarityToUse("SMStringEqual");

            double[] randomWeights = new double[7];
            for (int r = 0; r < 7; r ++) {
                randomWeights[r] = Math.random();
            }
            WeightFunc wf = a -> {
                String stringValue = ((StringObject) a).getNativeString();
                for (int d = 0; d<7; d++) if (days[d].equals(stringValue)) return randomWeights[d];
                return 1.;
            };
            sm.setWeightFunction(wf);

            Similarity sim = sm.compute(queryList, caseList, simVal);
            assertEquals(1., sim.getValue(), delta);

        }
    }

    @Test
    public void test2(){
        ListObject queryList = weekdays();
        ListObject caseList = workdays();

        SMCollectionMappingImplExt sm = new SMCollectionMappingImplExt();
        sm.setMaxQueueSize(-1);
        sm.setSimilarityToUse("SMStringEqual");

        WeightFunc wf = a -> {
            String stringValue = ((StringObject) a).getNativeString();
            if (stringValue.equals("Saturday")) return 0.;
            if (stringValue.equals("Sunday")) return 0.;
            return 1.;
        };
        sm.setWeightFunction(wf);

        Similarity sim = sm.compute(queryList, caseList, simVal);
        assertEquals(1., sim.getValue(), delta);

    }

    @Test
    public void test3(){
        ListObject queryList = weekdays();
        ListObject caseList = workdays();

        SMCollectionMappingImplExt sm = new SMCollectionMappingImplExt();
        sm.setMaxQueueSize(-1);
        sm.setSimilarityToUse("SMStringEqual");

        WeightFunc wf = a -> {
            String stringValue = ((StringObject) a).getNativeString();
            if (stringValue.equals("Saturday")) return 0.5;
            if (stringValue.equals("Sunday")) return 0.5;
            return 1.;
        };
        sm.setWeightFunction(wf);

        Similarity sim = sm.compute(queryList, caseList, simVal);
        assertEquals(5./6, sim.getValue(), delta);

    }

    @Test
    public void test4(){
        ListObject queryList = weekdays();
        ListObject caseList = workdays();

        SMCollectionMappingImplExt sm = new SMCollectionMappingImplExt();
        sm.setMaxQueueSize(-1);
        sm.setSimilarityToUse("SMStringLevenshtein");

        sm.setWeightFunction(a -> 1.);

        Similarity sim = sm.compute(queryList, caseList, simVal);
        assertEquals(5./7, sim.getValue(), delta);

    }

    @Test
    public void test5(){
        ListObject queryList = (ListObject) ModelFactory.getDefaultModel().createObject(LIST_CLASS_NAME);
        ListObject caseList = workdays();

        SMCollectionMappingImplExt sm = new SMCollectionMappingImplExt();
        sm.setMaxQueueSize(-1);
        sm.setSimilarityToUse("SMStringEqual");

        Similarity sim = sm.compute(queryList, caseList, simVal);
        assertEquals(1., sim.getValue(), delta);

    }

    @Test
    public void test6(){
        ListObject queryList = weekdays();
        ListObject caseList = (ListObject) ModelFactory.getDefaultModel().createObject(LIST_CLASS_NAME);

        SMCollectionMappingImplExt sm = new SMCollectionMappingImplExt();
        sm.setMaxQueueSize(-1);
        sm.setSimilarityToUse("SMStringEqual");

        Similarity sim = sm.compute(queryList, caseList, simVal);
        assertEquals(0., sim.getValue(), delta);

    }
}
