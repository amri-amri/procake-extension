package de.uni_trier.wi2.parsing.model;

import de.uni_trier.wi2.procake.data.object.DataObject;

public class AndComponent implements LogicalComponent {
    final private LogicalOrConditionComponent[] conditions;

    public AndComponent(LogicalOrConditionComponent[] conditions) {
        this.conditions = conditions;
    }

    @Override
    public Boolean evaluate(DataObject q, DataObject c) {
        for (LogicalOrConditionComponent cond : conditions) {
            boolean val = cond.evaluate(q, c);
            if (!val) return false;
        }
        return true;
    }
}
