package de.uni_trier.wi2.utils.taxonomy;

import de.uni_trier.wi2.naming.XESorAggregateAttributeNames;
import de.uni_trier.wi2.procake.data.model.DataClass;
import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.data.object.base.AggregateObject;
import de.uni_trier.wi2.procake.data.object.base.ListObject;
import de.uni_trier.wi2.procake.data.object.base.StringObject;
import de.uni_trier.wi2.utils.XEStoSystem;

public interface XESEventToTaxonomy {


    String ICD_Code = "ICD-Code";

    static String getCode(ListObject xesEventObject) throws InvalidDataClassException, ValueNotFoundException {
        DataClass dataClass = xesEventObject.getDataClass();
        if (!XEStoSystem.isXESEventClass(dataClass)) throw new InvalidDataClassException(dataClass);

        try {
            for (DataObject attribute : xesEventObject.getValues()) {
                if (!XEStoSystem.isXESStringClass(attribute.getDataClass())) continue;
                AggregateObject attributeAggregateObject = (AggregateObject) attribute;
                StringObject keyObject = (StringObject) attributeAggregateObject.getAttributeValue(XESorAggregateAttributeNames.KEY);
                String key = keyObject.getNativeString();
                if (!key.contains(ICD_Code)) continue;
                StringObject valueObject = (StringObject) attributeAggregateObject.getAttributeValue(XESorAggregateAttributeNames.VALUE);
                String value = valueObject.getNativeString();
                return value.replace(".", "");
            }
        } catch (Exception ignored) {
        }

        throw new ValueNotFoundException(xesEventObject);
    }
}
