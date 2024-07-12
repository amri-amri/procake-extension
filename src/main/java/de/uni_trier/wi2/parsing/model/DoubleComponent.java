package de.uni_trier.wi2.parsing.model;

import de.uni_trier.wi2.procake.data.object.DataObject;

public class DoubleComponent implements ValueComponent<Double> {
    final private Double value;

    public DoubleComponent(Double value) {
        this.value = value;
    }

    @Override
    public Double evaluate() {
        return value;
    }

    @Override
    public Object evaluate(DataObject q, DataObject c) {
        return evaluate();
    }
}
