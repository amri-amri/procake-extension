package test;

import de.uni_trier.wi2.procake.data.model.ModelFactory;
import de.uni_trier.wi2.procake.data.object.base.ListObject;
import de.uni_trier.wi2.procake.data.object.base.StringObject;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.base.collection.impl.SMCollectionIsolatedMappingImpl;
import de.uni_trier.wi2.procake.similarity.base.collection.impl.SMListMappingImpl;
import extension.SMCollectionIsolatedMappingImplExt;
import extension.SMListMappingImplExt;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SMCollectionIsolatedMappingExtTest extends CollectionSimilarityTest {

    public ListObject workdays2(){
        ListObject workdays = (ListObject) ModelFactory.getDefaultModel().createObject(listname);

        workdays.addValue(utils.createStringObject(days[2]));
        workdays.addValue(utils.createStringObject(days[4]));
        workdays.addValue(utils.createStringObject(days[0]));
        workdays.addValue(utils.createStringObject(days[3]));
        workdays.addValue(utils.createStringObject(days[1]));

        return workdays;
    }

    @Test
    public void testSimilarityMeasureFunction(){
        ListObject list1 = weekdays();
        ListObject list2 = workdays2();

        SMCollectionIsolatedMappingImpl isolatedMapping = new SMCollectionIsolatedMappingImpl();
        SMCollectionIsolatedMappingImplExt isolatedMappingExt = new SMCollectionIsolatedMappingImplExt();

        isolatedMapping.setSimilarityToUse("SMStringLevenshtein");
        isolatedMappingExt.setSimilarityToUse("SMStringLevenshtein");

        Similarity sim1, sim2;

        sim1 = isolatedMapping.compute(list1, list2, simVal);
        sim2 = isolatedMappingExt.compute(list1, list2, simVal);

        assertEquals((5.5+2./3)/7, sim1.getValue(), delta);
        assertEquals((5.5+2./3)/7, sim2.getValue(), delta);


        isolatedMappingExt.setSimilarityToUse((a, b) -> {
            if (((StringObject) a).getNativeString().equals(days[5])) return "SMStringEqual";
            return "SMStringLevenshtein";
        });


        sim2 = isolatedMappingExt.compute(list1, list2, simVal);

        assertEquals(true, sim2.getValue() < (5.5+2./3)/7);

        double s = sim2.getValue();

        isolatedMappingExt.setSimilarityToUse((a, b) -> {
            if (((StringObject) a).getNativeString().equals(days[5])) return "SMStringEqual";
            if (((StringObject) a).getNativeString().equals(days[6])) return "SMStringEqual";
            return "SMStringLevenshtein";
        });

        sim2 = isolatedMappingExt.compute(list1, list2, simVal);

        assertEquals(true, sim2.getValue() < s);
    }

    @Test
    public void testWeightFunction(){
        ListObject list1 = weekdays();
        ListObject list2 = workdays2();

        SMCollectionIsolatedMappingImpl isolatedMapping = new SMCollectionIsolatedMappingImpl();
        SMCollectionIsolatedMappingImplExt isolatedMappingExt = new SMCollectionIsolatedMappingImplExt();

        isolatedMapping.setSimilarityToUse("SMStringLevenshtein");
        isolatedMappingExt.setSimilarityToUse("SMStringLevenshtein");

        Similarity sim1, sim2;

        sim1 = isolatedMapping.compute(list1, list2, simVal);
        sim2 = isolatedMappingExt.compute(list1, list2, simVal);

        assertEquals((5.5+2./3)/7, sim1.getValue(), delta);
        assertEquals((5.5+2./3)/7, sim2.getValue(), delta);


        isolatedMappingExt.setWeightFunction((a, b) -> {
            if (((StringObject) a).getNativeString().equals(days[5])) return 0;
            if (((StringObject) a).getNativeString().equals(days[6])) return 0.5;
            return 1;
        });

        sim2 = isolatedMappingExt.compute(list1, list2, simVal);

        assertEquals(32./33, sim2.getValue(), delta);


        isolatedMappingExt.setWeightFunction((a, b) -> {
            if (((StringObject) a).getNativeString().equals(days[0])) return 0;
            if (((StringObject) a).getNativeString().equals(days[1])) return 0.5;
            if (((StringObject) a).getNativeString().equals(days[2])) return 0.5;
            if (((StringObject) a).getNativeString().equals(days[3])) return 0;
            return 1;
        });

        sim2 = isolatedMappingExt.compute(list1, list2, simVal);

        assertEquals(19./24, sim2.getValue(), delta);
    }

    @Test
    public void testEquality(){
        ListObject[] lists = new ListObject[]{
                workdays(),
                weekdays(),
                workdays2()
        };

        SMCollectionIsolatedMappingImpl isolatedMapping = new SMCollectionIsolatedMappingImpl();
        SMCollectionIsolatedMappingImplExt isolatedMappingExt = new SMCollectionIsolatedMappingImplExt();

        isolatedMapping.setSimilarityToUse("SMStringEqual");
        isolatedMappingExt.setSimilarityToUse("SMStringEqual");

        Similarity sim1, sim2;

        for(int i = 0; i<3; i++) for (int j = 0; j<3; j++) {
            sim1 = isolatedMapping.compute(lists[i], lists[j], simVal);
            sim2 = isolatedMappingExt.compute(lists[i], lists[j], simVal);

            assertEquals(sim1.getValue(),sim2.getValue(),0);
        }
    }
}
