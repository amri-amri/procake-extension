package extension.abstraction;

import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.data.object.DataObjectUtils;
import de.uni_trier.wi2.procake.data.object.base.AggregateObject;
import de.uni_trier.wi2.procake.data.object.base.CollectionObject;
import de.uni_trier.wi2.procake.data.object.base.ListObject;
import de.uni_trier.wi2.procake.data.object.base.SetObject;
import de.uni_trier.wi2.procake.data.object.base.impl.SetObjectImpl;

import java.util.ArrayList;
import java.util.List;

public interface XESBaseToSystemClass {

    public static CollectionObject getXESAggregateAttributesAsSystemCollectionObject(AggregateObject o){

        if (! (o.getDataClass().isSubclassOf(o.getModel().getClass("XESBaseClass")))) throw new XESBaseToSystemClassException("Aggregate's DataClass is not subclass of \"XESBaseClass\".");


        if (o.getDataClass().isSubclassOf(o.getModel().getClass("XESUnnaturallyNestedClass"))) {
            return (SetObject) o.getAttributeValue("attributes");
        }

        else if (o.getDataClass().isSubclassOf(o.getModel().getClass("XESNaturallyNestedClass"))) {
            if (o.getDataClass().isSubclassOf(o.getModel().getClass("XESListClass"))) {
                return (ListObject) o.getAttributeValue("value");
            }

            else if (o.getDataClass().isSubclassOf(o.getModel().getClass("XESContainerClass"))) {
                return (SetObject) o.getAttributeValue("value");
            }
        }

        return (new DataObjectUtils()).createSetObject();
    }


    class XESBaseToSystemClassException extends RuntimeException {
        XESBaseToSystemClassException(String m){
            super(m);
        }
    }
}
