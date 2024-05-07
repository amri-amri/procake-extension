package de.uni_trier.wi2.extension.similarity.measure.taxonomy;

import de.uni_trier.wi2.procake.data.model.DataClass;
import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.data.object.base.ListObject;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.SimilarityValuator;
import de.uni_trier.wi2.procake.similarity.impl.SimilarityImpl;
import de.uni_trier.wi2.utils.XEStoSystem;
import de.uni_trier.wi2.utils.taxonomy.InvalidDataClassException;
import de.uni_trier.wi2.utils.taxonomy.Taxonomy;
import de.uni_trier.wi2.utils.taxonomy.ValueNotFoundException;
import de.uni_trier.wi2.utils.taxonomy.XESEventToTaxonomy;

import java.io.IOException;

public class SMSanchezICD10PCSImpl extends SMSanchez implements SMSanchezICD10PCS {
    @Override
    public boolean isSimilarityFor(DataClass dataClass, String s) {
        return XEStoSystem.isXESEventClass(dataClass);
    }

    @Override
    public Similarity compute(DataObject dataObject1, DataObject dataObject2, SimilarityValuator similarityValuator) {
        DataClass dataClass = dataObject1.getDataClass();
        DataClass dataClass1 = dataObject2.getDataClass();
        if (!XEStoSystem.isXESEventClass(dataClass) || !XEStoSystem.isXESEventClass(dataClass1)){
            //todo log
            return new SimilarityImpl(this,dataObject1, dataObject2);
        }

        double value = 0;
        try {
             String code1 = XESEventToTaxonomy.getCode((ListObject) dataObject1);
             String code2 = XESEventToTaxonomy.getCode((ListObject) dataObject2);
             value = computeSimilarity(Taxonomy.ICD_10_PCS, code1, code2);
        } catch (InvalidDataClassException | ValueNotFoundException | IOException e) {
            //todo log
            return new SimilarityImpl(this,dataObject1, dataObject2);
        }

        return new SimilarityImpl(this, dataObject1, dataObject2, value);
    }

    @Override
    public String getSystemName() {
        return SMSanchezICD10PCS.NAME;
    }

}
