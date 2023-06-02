package utils;

import de.uni_trier.wi2.procake.data.object.DataObject;

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
}
