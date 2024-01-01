package de.uni_trier.wi2.extension.similarity.measure;

import de.uni_trier.wi2.procake.data.model.DataClass;
import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.data.object.base.AggregateObject;
import de.uni_trier.wi2.procake.data.object.base.BooleanObject;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.SimilarityValuator;
import de.uni_trier.wi2.procake.similarity.impl.SimilarityImpl;
import de.uni_trier.wi2.procake.similarity.impl.SimilarityMeasureImpl;

import static de.uni_trier.wi2.LoggingUtils.METHOD_CALL;
import static de.uni_trier.wi2.LoggingUtils.maxSubstring;

public class SMBooleanXORImpl extends SimilarityMeasureImpl implements SMBooleanXOR {
    public String getSystemName() {
        return NAME;
    }

    @Override
    public boolean isSimilarityFor(DataClass dataclass, String orderName) {
        METHOD_CALL.info("public boolean extension.similarity.measure.SMBooleanXORImpl.isSimilarityFor" +
                "(DataClass dataclass={}, String orderName={})...", maxSubstring(dataclass), maxSubstring(orderName));

        boolean isSimilarityFor = dataclass.isBoolean() || dataclass.isSubclassOf(dataclass.getModel().getClass("XESBooleanClass"));

        METHOD_CALL.info("extension.similarity.measure.SMBooleanXORImpl.isSimilarityFor" +
                "(DataClass, String): return {}", isSimilarityFor);

        return isSimilarityFor;
    }

    @Override
    public Similarity compute(DataObject queryObject, DataObject caseObject, SimilarityValuator valuator) {
        METHOD_CALL.info(
                "public Similarity extension.similarity.SMBooleanXORImpl.measurecompute" +
                "(DataObject queryObject={}, DataObject caseObject={}, SimilarityValuator valuator={})...",
                maxSubstring(queryObject), maxSubstring(caseObject), maxSubstring(valuator));

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

        METHOD_CALL.info(
                "extension.similarity.SMBooleanXORImpl.measurecompute" +
                "(DataObject, DataObject, SimilarityValuator): " +
                "return new SimilarityImpl(this, queryObject, caseObject, {}});",
                val);

        return new SimilarityImpl(this, queryObject, caseObject, val);
    }
}
