package de.uni_trier.wi2.extension.similarity.measure;

import de.uni_trier.wi2.procake.data.model.DataClass;
import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.data.object.base.AggregateObject;
import de.uni_trier.wi2.procake.data.object.base.StringObject;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.SimilarityValuator;
import de.uni_trier.wi2.procake.similarity.base.string.impl.SMStringLevenshteinImpl;

import static de.uni_trier.wi2.ProcakeExtensionLoggingUtils.*;

public class SMStringLevenshteinImplExt extends SMStringLevenshteinImpl implements SMStringLevenshteinExt {

    public String getSystemName() {
        return SMStringLevenshteinExt.NAME;
    }

    @Override
    public boolean isSimilarityFor(DataClass dataclass, String orderName) {
        METHOD_CALL.trace("public boolean extension.similarity.measure.SMStringLevenshteinImplExt.isSimilarityFor" +
                "(DataClass dataclass={}, String orderName={})...", maxSubstring(dataclass), maxSubstring(orderName));

        boolean isSimilarityFor = super.isSimilarityFor(dataclass, orderName) || dataclass.isSubclassOf(dataclass.getModel().getClass("XESLiteralClass"));

        METHOD_CALL.trace("extension.similarity.measure.SMStringLevenshteinImplExt.isSimilarityFor" +
                "(DataClass, String): return {}", isSimilarityFor);

        return isSimilarityFor;
    }

    @Override
    public Similarity compute(DataObject queryObject, DataObject caseObject, SimilarityValuator valuator) {
        METHOD_CALL.trace("public Similarity extension.similarity.measure.SMStringLevenshteinImplExt.compute" +
                "(DataObject queryObject={}, DataObject caseObject={}, SimilarityValuator valuator={})...",
                maxSubstring(queryObject), maxSubstring(caseObject), maxSubstring(valuator));

        StringObject queryString, caseString;

        if (queryObject.getDataClass().isSubclassOf(queryObject.getModel().getClass("XESLiteralClass"))) {
            queryString = (StringObject) ((AggregateObject) queryObject).getAttributeValue("value");
        } else {
            queryString = (StringObject) queryObject;
        }

        if (caseObject.getDataClass().isSubclassOf(caseObject.getModel().getClass("XESLiteralClass"))) {
            caseString = (StringObject) ((AggregateObject) caseObject).getAttributeValue("value");
        } else {
            caseString = (StringObject) caseObject;
        }

        DIAGNOSTICS.trace(
                "extension.similarity.measure.SMStringLevenshteinImplExt.compute" +
                "(DataObject, DataObject, SimilarityValuator): " +
                "Similarity similarity = super.compute(queryString, caseString, valuator);");

        Similarity similarity = super.compute(queryString, caseString, valuator);

        METHOD_CALL.trace(
                "extension.similarity.measure.SMStringLevenshteinImplExt.compute" +
                "(DataObject, DataObject, SimilarityValuator): return {}",
                similarity);

        return similarity;
    }
}
