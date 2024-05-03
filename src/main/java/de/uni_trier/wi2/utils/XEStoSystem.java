package de.uni_trier.wi2.utils;

import de.uni_trier.wi2.naming.Classnames;
import de.uni_trier.wi2.naming.XESorAggregateAttributeNames;
import de.uni_trier.wi2.procake.data.model.DataClass;
import de.uni_trier.wi2.procake.data.object.base.AggregateObject;
import de.uni_trier.wi2.procake.data.object.base.ListObject;
import de.uni_trier.wi2.procake.data.object.base.StringObject;

import java.util.Arrays;

public interface XEStoSystem {

    static ListObject getXESListAsSystemListObject(AggregateObject o) {
        return (ListObject) o.getAttributeValue(XESorAggregateAttributeNames.VALUE);
    }

    static boolean isXESListClass(DataClass dataClass) {
        String listClassName = Classnames.getXESClassName(Classnames.LIST_CLASS);
        if (dataClass.getName().equals(listClassName)) return true;
        return Arrays.stream(dataClass.getSuperClasses()).anyMatch(a -> a.getName().equals(listClassName));
    }

    static boolean isXESStringClass(DataClass dataClass) {
        String stringClassName = Classnames.getXESClassName(Classnames.STRING_CLASS);
        if (dataClass.getName().equals(stringClassName)) return true;
        return Arrays.stream(dataClass.getSuperClasses()).anyMatch(a -> a.getName().equals(stringClassName));
    }


}
