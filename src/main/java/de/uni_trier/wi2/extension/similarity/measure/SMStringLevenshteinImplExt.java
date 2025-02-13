package de.uni_trier.wi2.extension.similarity.measure;

import de.uni_trier.wi2.naming.XESorAggregateAttributeNames;
import de.uni_trier.wi2.procake.data.model.DataClass;
import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.data.object.base.AggregateObject;
import de.uni_trier.wi2.procake.data.object.base.StringObject;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.SimilarityValuator;
import de.uni_trier.wi2.procake.similarity.base.string.impl.SMStringLevenshteinImpl;
import de.uni_trier.wi2.procake.similarity.impl.SimilarityImpl;
import de.uni_trier.wi2.utils.XEStoSystem;


public class SMStringLevenshteinImplExt extends SMStringLevenshteinImpl implements SMStringLevenshteinExt {

    public String getSystemName() {
        return SMStringLevenshteinExt.NAME;
    }

    @Override
    public boolean isSimilarityFor(DataClass dataclass, String orderName) {
        if (XEStoSystem.isXESStringClass(dataclass)) return true;
        if (XEStoSystem.isXESIDClass(dataclass)) return true;
        return super.isSimilarityFor(dataclass, orderName);
    }

    @Override
    public Similarity compute(DataObject queryObject, DataObject caseObject, SimilarityValuator valuator) {


        StringObject queryString, caseString;
        DataClass queryClass = queryObject.getDataClass();
        DataClass caseClass = caseObject.getDataClass();

        String queryKey = null;
        String caseKey = null;

        if (XEStoSystem.isXESStringClass(queryClass) || XEStoSystem.isXESStringClass(queryClass)) {
            queryString = (StringObject) ((AggregateObject) queryObject).getAttributeValue(XESorAggregateAttributeNames.VALUE);
            queryKey = ((StringObject) ((AggregateObject) queryObject).getAttributeValue(XESorAggregateAttributeNames.KEY)).getNativeString();
        } else {
            queryString = (StringObject) queryObject;
        }

        if (XEStoSystem.isXESStringClass(caseClass) || XEStoSystem.isXESStringClass(caseClass)) {
            caseString = (StringObject) ((AggregateObject) caseObject).getAttributeValue(XESorAggregateAttributeNames.VALUE);
            caseKey = ((StringObject) ((AggregateObject) caseObject).getAttributeValue(XESorAggregateAttributeNames.KEY)).getNativeString();
        } else {
            caseString = (StringObject) caseObject;
        }

        if (queryKey != null && caseKey != null && !queryKey.equals(caseKey))
            return new SimilarityImpl(this, queryObject, caseObject, 0);

        return super.compute(queryString, caseString, valuator);
    }
}
