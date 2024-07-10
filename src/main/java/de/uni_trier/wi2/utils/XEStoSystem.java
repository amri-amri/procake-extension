package de.uni_trier.wi2.utils;

import de.uni_trier.wi2.naming.Classnames;
import de.uni_trier.wi2.naming.XESorAggregateAttributeNames;
import de.uni_trier.wi2.procake.data.model.DataClass;
import de.uni_trier.wi2.procake.data.object.base.AggregateObject;
import de.uni_trier.wi2.procake.data.object.base.ListObject;

import java.util.Arrays;

public interface XEStoSystem {

    static ListObject getXESListAsSystemListObject(AggregateObject o) {
        return (ListObject) o.getAttributeValue(XESorAggregateAttributeNames.VALUE);
    }

    static boolean isXESListClass(DataClass dataClass) {
        return isXESClass(dataClass, Classnames.LIST_CLASS);
    }

    static boolean isXESStringClass(DataClass dataClass) {
        return isXESClass(dataClass, Classnames.STRING_CLASS);
    }

    static boolean isXESIDClass(DataClass dataClass) {
        return isXESClass(dataClass, Classnames.ID_CLASS);
    }

    static boolean isXESEventClass(DataClass dataClass) {
        return isXESClass(dataClass, Classnames.EVENT_CLASS);
    }

    static boolean isXESClass(DataClass dataClass, String nameOfXESClass) {
        String stringClassName = Classnames.getXESClassName(nameOfXESClass);
        if (dataClass.getName().equals(stringClassName)) return true;
        return Arrays.stream(dataClass.getSuperClasses()).anyMatch(a -> a.getName().equals(stringClassName));
    }


}
