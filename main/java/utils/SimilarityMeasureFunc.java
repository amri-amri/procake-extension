package utils;

import de.uni_trier.wi2.procake.data.object.DataObject;

/**
 * Functional interface for assigning similarity measures to pairs of data objects.
 */
public interface SimilarityMeasureFunc {

    /**
     * Returns the name of a similarity measure depending on two given data objects.
     *
     * @param q  the first data dbject
     * @param c  the second data object
     * @return the assigned name of the similarity measure
     */
    String apply(DataObject q, DataObject c);
}
