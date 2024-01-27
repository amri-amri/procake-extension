package de.uni_trier.wi2.utils;

import de.uni_trier.wi2.procake.data.object.DataObject;

import static de.uni_trier.wi2.ProcakeExtensionLoggingUtils.METHOD_CALL;

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
        METHOD_CALL.trace("static WeightFunc procake-extension.utils.WeightFunc.getDefault()...");
        return (q) -> 1;
    }
}
