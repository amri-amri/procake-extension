package de.uni_trier.wi2.utils.taxonomy;

import de.uni_trier.wi2.procake.data.object.DataObject;

public class ValueNotFoundException extends Exception {
    public ValueNotFoundException(DataObject dataObject) {
        super(String.format("The desired value for the object \"%s\" was not found.", dataObject.toDetailedString()));
    }
}
