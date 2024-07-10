package de.uni_trier.wi2.utils.taxonomy;

import de.uni_trier.wi2.procake.data.model.DataClass;

public class InvalidDataClassException extends Exception {
    public InvalidDataClassException(DataClass dataClass) {
        super(String.format("DataClass \"%s\" is invalid here.", dataClass.getName()));
    }
}
