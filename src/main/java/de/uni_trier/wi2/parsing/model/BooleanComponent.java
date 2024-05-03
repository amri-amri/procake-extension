package de.uni_trier.wi2.parsing.model;

import de.uni_trier.wi2.procake.data.object.DataObject;

public class BooleanComponent  implements ValueComponent<Boolean>{
    final private Boolean value;

    public BooleanComponent(Boolean value) {
        this.value = value;
    }

    @Override
    public Boolean evaluate() {
        return value;
    }

    @Override
    public Object evaluate(DataObject q, DataObject c) {
        return evaluate();
    }
}
