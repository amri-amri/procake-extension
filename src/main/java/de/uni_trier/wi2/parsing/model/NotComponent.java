package de.uni_trier.wi2.parsing.model;

import de.uni_trier.wi2.procake.data.object.DataObject;

public class NotComponent implements LogicalComponent {
    final private LogicalOrConditionComponent condition;

    public NotComponent(LogicalOrConditionComponent condition) {
        this.condition = condition;
    }

    @Override
    public Boolean evaluate(DataObject q, DataObject c) {
        return !condition.evaluate(q, c);
    }
}
