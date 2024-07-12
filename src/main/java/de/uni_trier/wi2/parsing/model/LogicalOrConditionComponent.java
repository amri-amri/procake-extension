package de.uni_trier.wi2.parsing.model;


import de.uni_trier.wi2.procake.data.object.DataObject;

public interface LogicalOrConditionComponent extends Component<Boolean> {
    Boolean evaluate(DataObject q, DataObject c);
}
