package de.uni_trier.wi2.extension.similarity.measure;

import de.uni_trier.wi2.procake.data.model.DataClass;
import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.data.object.base.AggregateObject;
import de.uni_trier.wi2.procake.data.object.base.StringObject;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.SimilarityValuator;
import de.uni_trier.wi2.procake.similarity.base.string.impl.SMStringLevenshteinImpl;

public class SMStringLevenshteinImplExt extends SMStringLevenshteinImpl implements SMStringLevenshteinExt {

    public String getSystemName() {
        return SMStringLevenshteinExt.NAME;
    }

    @Override
    public boolean isSimilarityFor(DataClass dataclass, String orderName) {
        return super.isSimilarityFor(dataclass, orderName) || dataclass.isSubclassOf(dataclass.getModel().getClass("XESLiteralClass"));
    }

    @Override
    public Similarity compute(DataObject queryObject, DataObject caseObject, SimilarityValuator valuator) {

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

        return super.compute(queryString, caseString, valuator);
    }
}
