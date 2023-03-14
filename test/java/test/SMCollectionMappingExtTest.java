package test;

import de.uni_trier.wi2.procake.data.model.ModelFactory;
import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.data.object.base.ListObject;
import de.uni_trier.wi2.procake.data.object.base.StringObject;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.base.collection.SMCollectionMapping;
import de.uni_trier.wi2.procake.similarity.base.collection.impl.SMCollectionMappingImpl;
import extension.SMCollectionMappingImplExt;
import org.junit.Test;

public class SMCollectionMappingExtTest extends CollectionSimilarityTest{

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
    public void debug(){

        ListObject queryObject = weekdays();
        ListObject caseObject = workdays2();

        SMCollectionMappingImpl collectionMapping = new SMCollectionMappingImpl();
        collectionMapping.setSimilarityToUse("SMStringLevenshtein");
        collectionMapping.setMaxQueueSize(-1);

        Similarity sim = collectionMapping.compute(queryObject, caseObject, simVal);
        System.out.println(sim.getValue());


        SMCollectionMappingImplExt collectionMappingExt = new SMCollectionMappingImplExt();
        collectionMappingExt.setSimilarityToUse("SMStringLevenshtein");
        sim = collectionMappingExt.compute(weekdays(), workdays2(), simVal);
        System.out.println(sim.getValue());

        collectionMappingExt.setWeightFunction((a, b) -> {
            if (((StringObject) a).getNativeString().equals("Saturday")) return 0;
            if (((StringObject) a).getNativeString().equals("Sunday")) return 0;
            return 1.;
        });
        sim = collectionMappingExt.compute(weekdays(), workdays2(), simVal);
        System.out.println(sim.getValue());
    }

}
