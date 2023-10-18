package similarity;

import de.uni_trier.wi2.procake.data.object.base.ListObject;
import de.uni_trier.wi2.procake.data.object.base.StringObject;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.base.collection.SMListDTW;
import de.uni_trier.wi2.procake.similarity.base.string.SMStringEqual;
import de.uni_trier.wi2.procake.similarity.base.string.SMStringLevenshtein;
import de.uni_trier.wi2.extension.similarity.measure.collection.SMListDTWExt;
import de.uni_trier.wi2.extension.similarity.measure.collection.SMListDTWImplExt;
import org.junit.Ignore;
import org.junit.Test;
import de.uni_trier.wi2.utils.MethodInvoker;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class SMListDTWImplExtTest extends ISimilarityMeasureFuncTest {

    {
        name = SMListDTWExt.NAME;
        superclassName = SMListDTW.NAME;
    }

    @Test
    public void test1(){
        ListObject queryList = utils.createListObject();

        queryList.addValue(utils.createStringObject("A"));
        queryList.addValue(utils.createStringObject("B"));
        queryList.addValue(utils.createStringObject("C"));

        ListObject caseList = utils.createListObject();
        caseList.addValue(utils.createStringObject("A"));
        caseList.addValue(utils.createStringObject("B"));
        caseList.addValue(utils.createStringObject("B"));
        caseList.addValue(utils.createStringObject("C"));

        SMListDTWImplExt sm = new SMListDTWImplExt();
        sm.setLocalSimilarityToUse(SMStringEqual.NAME);

        Similarity sim = sm.compute(queryList, caseList, simVal);

        assertEquals(1., sim.getValue(), delta);
    }

    @Test
    public void test2(){
        ListObject queryList = utils.createListObject();


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

        ListObject caseList = utils.createListObject();
        caseList.addValue(utils.createStringObject("A"));
        caseList.addValue(utils.createStringObject("A"));
        caseList.addValue(utils.createStringObject("B"));
        caseList.addValue(utils.createStringObject("B"));
        caseList.addValue(utils.createStringObject("B"));
        caseList.addValue(utils.createStringObject("C"));

        SMListDTWImplExt sm = new SMListDTWImplExt();
        sm.setLocalSimilarityToUse(SMStringEqual.NAME);
        sm.setWeightFunc(a -> {
            if (((StringObject) a).getNativeString().equals("D")) return 0.;
            return 1.;
        });
        sm.setHalvingDistancePercentage(0.3);

        Similarity sim = sm.compute(queryList, caseList, simVal);

        assertEquals(1., sim.getValue(), delta);
    }

    @Test
    public void test3(){
        ListObject queryList = utils.createListObject();
        ListObject caseList = utils.createListObject();

        queryList.addValue(utils.createStringObject("Abc"));
        queryList.addValue(utils.createStringObject("dEf"));

        caseList.addValue(utils.createStringObject("abC"));
        caseList.addValue(utils.createStringObject("DeF"));
        caseList.addValue(utils.createStringObject("dEf"));

        SMListDTWImplExt sm = new SMListDTWImplExt();
        sm.setLocalSimilarityToUse(SMStringLevenshtein.NAME);
        sm.setMethodInvokersFunc((a, b)->{
            MethodInvoker mi = new MethodInvoker("setCaseSensitive", new Class[]{}, new Object[]{});
            ArrayList<MethodInvoker> list = new ArrayList<>();
            list.add(mi);
            return list;
        });


        Similarity sim = sm.compute(queryList, caseList, simVal);
        assertEquals(8./15, sim.getValue(), delta);


        sm = new SMListDTWImplExt();
        sm.setLocalSimilarityToUse(SMStringLevenshtein.NAME);
        sm.setMethodInvokersFunc((a, b)->{
            MethodInvoker mi = new MethodInvoker("setCaseInsensitive", new Class[]{}, new Object[]{});
            ArrayList<MethodInvoker> list = new ArrayList<>();
            list.add(mi);
            return list;
        });


        sim = sm.compute(queryList, caseList, simVal);
        assertEquals(1., sim.getValue(), delta);
    }

    @Ignore
    @Test
    @Override
    public void same_as_superclass_weekdays_workdays() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        //todo: the extended algorithm normalizes raw scores differently to the original
        // so here, raw scores should be compared instead of normalized ones

    }
}
