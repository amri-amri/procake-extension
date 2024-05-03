package de.uni_trier.wi2.parsing.model;

import de.uni_trier.wi2.procake.data.object.DataObject;

public class StringComponent implements StringOrMethodReturnValueComponent, ValueComponent<String>{
    final private String value;

    public StringComponent(String value) {
        this.value = value;
    }

    @Override
    public Object evaluate(DataObject q, DataObject c) {
        return evaluate();
    }

    @Override
    public String evaluate() {
        return value;
    }
}
