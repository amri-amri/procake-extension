package de.uni_trier.wi2.extension.abstraction;

import de.uni_trier.wi2.procake.data.object.DataObjectUtils;
import de.uni_trier.wi2.procake.data.object.base.AggregateObject;
import de.uni_trier.wi2.procake.data.object.base.CollectionObject;
import de.uni_trier.wi2.procake.data.object.base.ListObject;
import de.uni_trier.wi2.procake.data.object.base.SetObject;
import de.uni_trier.wi2.utils.namingUtils.Classnames;

import static de.uni_trier.wi2.ProcakeExtensionLoggingUtils.METHOD_CALL;
import static de.uni_trier.wi2.ProcakeExtensionLoggingUtils.maxSubstring;

public interface XESBaseToSystemClass {

    static CollectionObject getXESAggregateAttributesAsSystemCollectionObject(AggregateObject o) throws XESBaseToSystemClassException {
        METHOD_CALL.trace(
                "static CollectionObject extension.abstraction.XESBaseToSystemClass" +
                        ".getXESAggregateAttributesAsSystemCollectionObject(AggregateObject o={})", maxSubstring(o));

        if (! (o.getDataClass().isSubclassOf(o.getModel().getClass(Classnames.BASE)))) {

            METHOD_CALL.trace(
                    "procake-extension.extension.abstraction.XESBaseToSystemClass.getXESAggregateAttributesAsSystemCollectionObject" +
                            "(AggregateObject): throw new XESBaseToSystemClassException(\"Aggregate's DataClass is not subclass of \\\"XESBaseClass\\\".\")");

            throw new XESBaseToSystemClassException("Aggregate's DataClass is not subclass of \"XESBaseClass\".");
        }


        if (o.getDataClass().isSubclassOf(o.getModel().getClass(Classnames.UNNATURALLY_NESTED))) {

            SetObject setObject = (SetObject) o.getAttributeValue("attributes");

            METHOD_CALL.trace(
                    "procake-extension.extension.abstraction.XESBaseToSystemClass.getXESAggregateAttributesAsSystemCollectionObject" +
                            "(AggregateObject): return {}", maxSubstring(setObject));

            return setObject;
        }

        else if (o.getDataClass().isSubclassOf(o.getModel().getClass(Classnames.NATURALLY_NESTED))) {

            if (o.getDataClass().isSubclassOf(o.getModel().getClass(Classnames.LIST))) {
                ListObject listObject = (ListObject) o.getAttributeValue("value");

                METHOD_CALL.trace(
                        "procake-extension.extension.abstraction.XESBaseToSystemClass.getXESAggregateAttributesAsSystemCollectionObject" +
                                "(AggregateObject): return {}", maxSubstring(listObject.getValues()));

                return listObject;
            }

            else if (o.getDataClass().isSubclassOf(o.getModel().getClass(Classnames.CONTAINER))) {

                SetObject setObject = (SetObject) o.getAttributeValue("attributes");

                METHOD_CALL.trace(
                        "procake-extension.extension.abstraction.XESBaseToSystemClass.getXESAggregateAttributesAsSystemCollectionObject" +
                                "(AggregateObject): return {}", maxSubstring(setObject));

                return (SetObject) o.getAttributeValue("value");
            }
        }

        METHOD_CALL.trace(
                "procake-extension.extension.abstraction.XESBaseToSystemClass.getXESAggregateAttributesAsSystemCollectionObject" +
                        "(AggregateObject): return empty SetObject");

        return (new DataObjectUtils()).createSetObject();
    }


    class XESBaseToSystemClassException extends RuntimeException {
        XESBaseToSystemClassException(String m){
            super(m);
        }
    }
}
