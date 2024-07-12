package de.uni_trier.wi2.parsing.model;

import de.uni_trier.wi2.naming.Classnames;
import de.uni_trier.wi2.naming.XESorAggregateAttributeNames;
import de.uni_trier.wi2.procake.data.model.DataClass;
import de.uni_trier.wi2.procake.data.model.Model;
import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.data.object.base.*;

import java.util.List;

import static de.uni_trier.wi2.naming.Classnames.ATTRIBUTE_CLASS;
import static de.uni_trier.wi2.naming.Classnames.EVENT_CLASS;

public class FunctionComponent implements LogicalOrConditionComponent {
    final String name, arg1, arg2, arg3;

    public FunctionComponent(String name, String firstArgument, String secondArgument, String thirdArgument) {
        this.name = name;
        this.arg1 = firstArgument;
        this.arg2 = secondArgument;
        this.arg3 = thirdArgument;
    }

    private static String stringOfAtomic(DataObject dataObject) {
        if (dataObject.isString()) return ((StringObject) dataObject).getNativeString();
        if (dataObject.isTimestamp()) return ((TimestampObject) dataObject).getNativeTimestamp().toString();
        if (dataObject.isInteger()) return Integer.toString(((IntegerObject) dataObject).getNativeInteger());
        if (dataObject.isDouble()) return Double.toString(((DoubleObject) dataObject).getNativeDouble());
        if (dataObject.isBoolean()) return Boolean.toString(((BooleanObject) dataObject).getNativeBoolean());
        return null;
    }

    @Override
    public Boolean evaluate(DataObject q, DataObject c) {
        switch (name) {
            case "qcAttributesHaveSameKeyAndType":
                return qcAttributesHaveSameKeyAndType(q, c);

            case "qAttributeHasKeyTypeValue":
                return xAttributeHasKeyTypeValue(q);
            case "qEventContainsAttribute":
                return xEventContainsAttributeWithKeyTypeValue(q);

            case "cAttributeHasKeyTypeValue":
                return xAttributeHasKeyTypeValue(c);
            case "cEventContainsAttribute":
                return xEventContainsAttributeWithKeyTypeValue(c);
        }
        System.out.print("WARNING ...");//todo logging
        return null;
    }

    private boolean qcAttributesHaveSameKeyAndType(DataObject q, DataObject c) {
        Model model = q.getModel();

        DataClass qClass = q.getDataClass();
        if (!qClass.isSubclassOf(model.getClass(Classnames.getXESClassName(ATTRIBUTE_CLASS)))) return false;

        DataClass cClass = c.getDataClass();
        if (!cClass.isSubclassOf(model.getClass(Classnames.getXESClassName(ATTRIBUTE_CLASS)))) return false;

        if (!qClass.equals(cClass)) return false;

        AggregateObject qAggr = (AggregateObject) q;
        AggregateObject cAggr = (AggregateObject) c;

        String qKey = ((StringObject) qAggr.getAttributeValue(XESorAggregateAttributeNames.KEY)).getNativeString();
        String cKey = ((StringObject) cAggr.getAttributeValue(XESorAggregateAttributeNames.KEY)).getNativeString();

        if (!qKey.equals(cKey)) return false;

        if (arg1 != null) {
            if (!qKey.equals(arg1)) return false;
        }

        String qType = qClass.getName().replaceFirst("XES", "").replace("Class", "");
        String cType = qClass.getName().replaceFirst("XES", "").replace("Class", "");

        if (!qType.equals(cType)) return false;

        if (arg2 != null) {
            return qType.equals(arg2);
        }

        return true;
    }

    private boolean xAttributeHasKeyTypeValue(DataObject x) {
        Model model = x.getModel();

        DataClass qClass = x.getDataClass();
        if (!qClass.isSubclassOf(model.getClass(Classnames.getXESClassName(ATTRIBUTE_CLASS)))) return false;

        AggregateObject qAggr = (AggregateObject) x;

        if (arg1 != null) {
            String qKey = ((StringObject) qAggr.getAttributeValue(XESorAggregateAttributeNames.KEY)).getNativeString();
            if (!qKey.equals(arg1)) return false;
        }

        if (arg2 != null) {
            String qType = qClass.getName().replace("XES", "").replace("Class", "").toLowerCase();
            if (!arg2.trim().toLowerCase().equals(qType)) return false;
        }

        if (arg3 != null) {
            String qVal = stringOfAtomic(qAggr.getAttributeValue(XESorAggregateAttributeNames.VALUE));
            return qVal == null || qVal.equals(arg3);
        }

        return true;
    }

    private boolean xEventContainsAttributeWithKeyTypeValue(DataObject x) {
        Model model = x.getModel();
        if (!x.getDataClass().equals(model.getClass(Classnames.getXESClassName(EVENT_CLASS)))) return false;
        List<DataObject> attributes = ((ListObject) x).getValues();
        for (DataObject attr : attributes) {
            if (xAttributeHasKeyTypeValue(attr)) return true;
        }
        return false;
    }

    //qcAttributesHaveSameKeyAndType(arg1, arg2)
    //both q and c are XESAttribute and have same key and type

    //qAttributeHasKeyTypeValue(arg1, null, null)
    //q is XESAttribute and has key arg1

    //qAttributeHasKeyTypeValue(null, arg2, null)
    //q is XESAttribute and has type arg1

    //qAttributeHasKeyTypeValue(arg1, arg2, null)
    //q is XESAttribute and has key arg1 and typ arg2

    //qAttributeHasKeyTypeValue(arg1, arg2, arg3)
    //q is XESAttribute and has value arg1

    //qEventContainsAttribute(arg1, arg2, arg3)
    //q is XESEvent and has XESAttribute where key=arg1, type=arg2, value=arg3

}
