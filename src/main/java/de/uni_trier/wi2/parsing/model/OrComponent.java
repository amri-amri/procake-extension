package de.uni_trier.wi2.parsing.model;

import de.uni_trier.wi2.procake.data.object.DataObject;

public class OrComponent implements LogicalComponent {
    final private LogicalOrConditionComponent[] conditions;

    public OrComponent(LogicalOrConditionComponent[] conditions) {
        this.conditions = conditions;
    }

    @Override
    public Boolean evaluate(DataObject q, DataObject c) {
        for (LogicalOrConditionComponent cond : conditions) if (cond.evaluate(q, c)) return true;
        return false;
    }
}
