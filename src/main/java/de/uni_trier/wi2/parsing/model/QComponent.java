package de.uni_trier.wi2.parsing.model;

import de.uni_trier.wi2.procake.data.object.DataObject;

public class QComponent implements ObjectComponent {


    @Override
    public Object evaluate(DataObject q, DataObject c) {
        return q;
    }
}
