package test;

import de.uni_trier.wi2.procake.data.model.ModelFactory;
import de.uni_trier.wi2.procake.data.object.base.ListObject;
import de.uni_trier.wi2.procake.data.object.base.StringObject;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.base.collection.impl.SMListCorrectnessImpl;
import de.uni_trier.wi2.procake.similarity.base.collection.impl.SMListMappingImpl;
import extension.SMListCorrectnessImplExt;
import extension.SMListMappingImplExt;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ListCorrectnessTest extends CollectionSimilarityTest{

    protected ListObject list1(){
        ListObject list = (ListObject) ModelFactory.getDefaultModel().createObject(listname);

        list.addValue(utils.createStringObject(days[0]));
        list.addValue(utils.createStringObject(days[1]));
        list.addValue(utils.createStringObject(days[2]));

        return list;
    }

    protected ListObject list2(){
        ListObject list = (ListObject) ModelFactory.getDefaultModel().createObject(listname);

        list.addValue(utils.createStringObject(days[0]));
        list.addValue(utils.createStringObject(days[2]));
        list.addValue(utils.createStringObject(days[1]));

        return list;
    }

    protected ListObject list3(){
        ListObject list = (ListObject) ModelFactory.getDefaultModel().createObject(listname);

        list.addValue(utils.createStringObject(days[2]));
        list.addValue(utils.createStringObject(days[1]));
        list.addValue(utils.createStringObject(days[0]));

        return list;
    }

    @Test
    public void testWeightFunction(){
        ListObject list1 = list1();
        ListObject list2 = list2();
        ListObject list3 = list3();


        SMListCorrectnessImpl listCorrectness = new SMListCorrectnessImpl();
        SMListCorrectnessImplExt listCorrectnessExt = new SMListCorrectnessImplExt();

        Similarity sim1, sim2;

        listCorrectnessExt.setWeightFunction((a, b) -> {
            if (
                    ((StringObject) a).getNativeString().equals("Monday") &&
                    ((StringObject) b).getNativeString().equals("Wednesday")
            ) return 0.5;
            return 1;
        });

        sim1 = listCorrectness.compute(list1, list2, simVal);
        sim2 = listCorrectnessExt.compute(list1, list2, simVal);

        assertEquals(1./3, sim1.getValue(), 0);
        assertEquals(1./5, sim2.getValue(), 0);

        listCorrectnessExt.setWeightFunction((a, b) -> {
            if (
                    ((StringObject) a).getNativeString().equals("Tuesday") &&
                    ((StringObject) b).getNativeString().equals("Wednesday")
            ) return 0;
            return 1;
        });

        sim2 = listCorrectnessExt.compute(list1, list2, simVal);

        assertEquals(1, sim2.getValue(), 0);

        sim1 = listCorrectness.compute(list1, list3, simVal);
        sim2 = listCorrectnessExt.compute(list1, list3, simVal);

        assertEquals(1, sim1.getValue(), 0);
        assertEquals(1, sim2.getValue(), 0);
    }

    @Test
    public void testEquality(){
        ListObject[] lists = new ListObject[4];

        for(int i = 0; i<4; i++){
            lists[i] = (ListObject) ModelFactory.getDefaultModel().createObject(listname);
        }

        for(int i = 0; i<7; i++){
            //first list is all weekdays in order
            lists[0].addValue(utils.createStringObject(days[i]));

            //second list ist all weekdays in reverse order
            lists[1].addValue(utils.createStringObject(days[6-i]));

            //third list is first all weekdays with even index, then all weekdays with odd index
            lists[2].addValue(utils.createStringObject(days[(2*i)/7]));

            //third list is first all weekdays with odd index, then all weekdays with even index
            lists[3].addValue(utils.createStringObject(days[(2*i)/7]));
        }

        SMListCorrectnessImpl listCorrectness = new SMListCorrectnessImpl();
        SMListCorrectnessImplExt listCorrectnessExt = new SMListCorrectnessImplExt();

        Similarity sim1, sim2;

        for(int i = 0; i<4; i++) for (int j = 0; j<4; j++) {
                sim1 = listCorrectness.compute(lists[i], lists[j], simVal);
                sim2 = listCorrectnessExt.compute(lists[i], lists[j], simVal);

                assertEquals(sim1.getValue(),sim2.getValue(),0);
        }
    }
}
