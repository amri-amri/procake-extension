package de.uni_trier.wi2.parsing.model;

import de.uni_trier.wi2.procake.data.object.DataObject;

public class IntegerComponent implements ValueComponent<Integer> {
    final private Integer value;

    public IntegerComponent(Integer value) {
        this.value = value;
    }

    @Override
    public Integer evaluate() {
        return value;
    }

    @Override
    public Object evaluate(DataObject q, DataObject c) {
        return evaluate();
    }
}
