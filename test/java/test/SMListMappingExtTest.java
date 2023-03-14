package test;

import de.uni_trier.wi2.procake.data.model.ModelFactory;
import de.uni_trier.wi2.procake.data.object.base.ListObject;
import de.uni_trier.wi2.procake.data.object.base.StringObject;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.base.collection.impl.SMListMappingImpl;
import extension.SMListMappingExt;
import extension.SMListMappingImplExt;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SMListMappingExtTest extends CollectionSimilarityTest {

    public ListObject weekdays2(){
        ListObject workdays = (ListObject) ModelFactory.getDefaultModel().createObject(listname);

        workdays.addValue(utils.createStringObject(days[6]));
        workdays.addValue(utils.createStringObject(days[0]));
        workdays.addValue(utils.createStringObject(days[1]));
        workdays.addValue(utils.createStringObject(days[3]));
        workdays.addValue(utils.createStringObject(days[2]));
        workdays.addValue(utils.createStringObject(days[4]));
        workdays.addValue(utils.createStringObject(days[5]));

        return workdays;
    }

    @Test
    public void testSimilarityMeasureFunction(){
        ListObject list1 = workdays();
        ListObject list2 = weekdays2();

        SMListMappingImpl listMapping = new SMListMappingImpl();
        SMListMappingImplExt listMappingExt = new SMListMappingImplExt();

        listMapping.setSimilarityToUse("SMStringEqual");
        listMappingExt.setSimilarityToUse("SMStringEqual");

        Similarity sim1, sim2;

        sim1 = listMapping.compute(list1, list2, simVal);
        sim2 = listMappingExt.compute(list1, list2, simVal);

        assertEquals(3./5, sim1.getValue(), 0);
        assertEquals(3./5, sim2.getValue(), 0);


        listMappingExt.setSimilarityToUse((a, b) -> {
            if (((StringObject) a).getNativeString().equals(days[2])) return "SMStringLevenshtein";
            return "SMStringEqual";
        });


        sim2 = listMappingExt.compute(list1, list2, simVal);

        assertEquals(true, sim2.getValue() > 3./5);

        double s = sim2.getValue();

        listMappingExt.setSimilarityToUse((a, b) -> {
            if (((StringObject) a).getNativeString().equals(days[2])) return "SMStringLevenshtein";
            if (((StringObject) a).getNativeString().equals(days[3])) return "SMStringLevenshtein";
            return "SMStringEqual";
        });

        sim2 = listMappingExt.compute(list1, list2, simVal);

        assertEquals(true, sim2.getValue() > s);
    }

    @Test
    public void testWeightFunction(){
        ListObject list1 = workdays();
        ListObject list2 = weekdays2();

        SMListMappingImpl listMapping = new SMListMappingImpl();
        SMListMappingImplExt listMappingExt = new SMListMappingImplExt();

        listMapping.setSimilarityToUse("SMStringEqual");
        listMappingExt.setSimilarityToUse("SMStringEqual");

        Similarity sim1, sim2;

        sim1 = listMapping.compute(list1, list2, simVal);
        sim2 = listMappingExt.compute(list1, list2, simVal);

        assertEquals(3./5, sim1.getValue(), 0);
        assertEquals(3./5, sim2.getValue(), 0);


        listMappingExt.setWeightFunction((a, b) -> {
            if (((StringObject) a).getNativeString().equals(days[2])) return 0;
            return 1;
        });

        sim2 = listMappingExt.compute(list1, list2, simVal);

        assertEquals(3./4, sim2.getValue(), 0);


        listMappingExt.setWeightFunction((a, b) -> {
            if (((StringObject) a).getNativeString().equals(days[2])) return 0;
            if (((StringObject) a).getNativeString().equals(days[3])) return 0;
            return 1;
        });

        sim2 = listMappingExt.compute(list1, list2, simVal);

        assertEquals(3./3, sim2.getValue(), 0);
    }

    @Test
    public void testEquality(){
        ListObject[] lists = new ListObject[]{
                workdays(),
                weekdays(),
                weekdays2()
        };

        SMListMappingImpl listMapping = new SMListMappingImpl();
        SMListMappingImplExt listMappingExt = new SMListMappingImplExt();

        listMapping.setSimilarityToUse("SMStringEqual");
        listMappingExt.setSimilarityToUse("SMStringEqual");

        Similarity sim1, sim2;

        for(int i = 0; i<3; i++) for (int j = 0; j<3; j++) {
            sim1 = listMapping.compute(lists[i], lists[j], simVal);
            sim2 = listMappingExt.compute(lists[i], lists[j], simVal);

            assertEquals(sim1.getValue(),sim2.getValue(),0);
        }
    }
    
}
