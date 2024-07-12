package de.uni_trier.wi2.parsing.model;

import de.uni_trier.wi2.procake.data.object.DataObject;

public class EqualsComponent implements ConditionComponent {
    final private ObjectOrValueComponent object1, object2;

    public EqualsComponent(ObjectOrValueComponent object1, ObjectOrValueComponent object2) {
        this.object1 = object1;
        this.object2 = object2;
    }

    @Override
    public Boolean evaluate(DataObject q, DataObject c) {
        return object1.evaluate(q, c).equals(object2.evaluate(q, c));
    }
}
