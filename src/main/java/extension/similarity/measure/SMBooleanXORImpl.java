package extension.similarity.measure;

import de.uni_trier.wi2.procake.data.model.DataClass;
import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.data.object.base.AggregateObject;
import de.uni_trier.wi2.procake.data.object.base.BooleanObject;
import de.uni_trier.wi2.procake.data.object.base.StringObject;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.SimilarityMeasure;
import de.uni_trier.wi2.procake.similarity.SimilarityValuator;
import de.uni_trier.wi2.procake.similarity.impl.SimilarityImpl;
import de.uni_trier.wi2.procake.similarity.impl.SimilarityMeasureImpl;

public class SMBooleanXORImpl extends SimilarityMeasureImpl implements SMBooleanXOR {
    public String getSystemName() {
        return SMBooleanXOR.NAME;
    }

    @Override
    public boolean isSimilarityFor(DataClass dataclass, String orderName) {
        return dataclass.isBoolean() || dataclass.isSubclassOf(dataclass.getModel().getClass("XESBooleanClass"));
    }

    @Override
    public Similarity compute(DataObject queryObject, DataObject caseObject, SimilarityValuator valuator) {

        BooleanObject queryBool, caseBool;

        if (queryObject.getDataClass().isSubclassOf(queryObject.getModel().getClass("XESBooleanClass"))) {
            queryBool = (BooleanObject) ((AggregateObject) queryObject).getAttributeValue("value");
        } else {
            queryBool = (BooleanObject) queryObject;
        }

        if (caseObject.getDataClass().isSubclassOf(caseObject.getModel().getClass("XESBooleanClass"))) {
            caseBool = (BooleanObject) ((AggregateObject) caseObject).getAttributeValue("value");
        } else {
            caseBool = (BooleanObject) caseObject;
        }

        boolean x1 = queryBool.getNativeBooleanValue();
        boolean x2 = caseBool.getNativeBooleanValue();
        double val = 0.0;
        if (x1 ^ x2 ) val = 1.0;

        return new SimilarityImpl(this, queryObject, caseObject, val);
    }
}
