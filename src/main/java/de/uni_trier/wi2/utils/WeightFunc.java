package de.uni_trier.wi2.utils;

import de.uni_trier.wi2.procake.data.object.DataObject;

import static de.uni_trier.wi2.LoggingUtils.METHOD_CALL;

/**
 * Functional interface for assigning weights to data objects.
 */
public interface WeightFunc {

    /**
     * Returns a weight value.
     *
     * @param q  the data object
     * @return the assigned weight value
     */
    double apply(DataObject q);

    static WeightFunc getDefault(){
        METHOD_CALL.info("static WeightFunc procake-extension.utils.WeightFunc.getDefault()...");
        return (q) -> 1;
    }
}
