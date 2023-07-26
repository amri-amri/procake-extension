package extension.similarity.measure;

import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.data.object.base.AggregateObject;
import de.uni_trier.wi2.procake.data.object.base.StringObject;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.SimilarityValuator;
import de.uni_trier.wi2.procake.similarity.base.string.impl.SMStringLevenshteinImpl;
import extension.similarity.measure.collection.SMCollectionIsolatedMappingExt;

public class SMStringLevenshteinImplExt extends SMStringLevenshteinImpl implements SMStringLevenshteinExt {

    public String getSystemName() {
        return SMStringLevenshteinExt.NAME;
    }

    @Override
    public Similarity compute(DataObject queryObject, DataObject caseObject, SimilarityValuator valuator) {

        StringObject queryString, caseString;

        if (queryObject.getDataClass().isSubclassOf(queryObject.getModel().getClass("XESBaseClass"))) {
            queryString = (StringObject) ((AggregateObject) queryObject).getAttributeValue("value");
        } else {
            queryString = (StringObject) queryObject;
        }

        if (caseObject.getDataClass().isSubclassOf(caseObject.getModel().getClass("XESBaseClass"))) {
            caseString = (StringObject) ((AggregateObject) caseObject).getAttributeValue("value");
        } else {
            caseString = (StringObject) caseObject;
        }

        return super.compute(queryString, caseString, valuator);
    }
}
