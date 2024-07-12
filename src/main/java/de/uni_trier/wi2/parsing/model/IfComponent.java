package de.uni_trier.wi2.parsing.model;

import de.uni_trier.wi2.procake.data.object.DataObject;


public abstract class IfComponent<T> {
    final private LogicalOrConditionComponent conditionComponent;

    IfComponent(LogicalOrConditionComponent conditionComponent) {
        this.conditionComponent = conditionComponent;
    }

    public boolean isSatisfied(DataObject q, DataObject c) {
        return conditionComponent.evaluate(q, c);
    }

    public abstract T getReturnValue();
}
