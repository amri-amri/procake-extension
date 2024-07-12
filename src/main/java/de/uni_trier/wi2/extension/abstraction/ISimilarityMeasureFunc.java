package de.uni_trier.wi2.extension.abstraction;

import de.uni_trier.wi2.utils.SimilarityMeasureFunc;

/**
 * An interface for every class using the SimilarityMeasureFunc functional interface.
 */
public interface ISimilarityMeasureFunc {

    /**
     * sets the SimilarityMeasureFunc
     *
     * @param similarityMeasureFunc the WeightFunc to be set
     */
    void setSimilarityMeasureFunc(SimilarityMeasureFunc similarityMeasureFunc);

    /**
     * gets the SimilarityMeasureFunc
     *
     * @return the SimilarityMeasureFunc
     */
    SimilarityMeasureFunc getSimilarityMeasureFunc();
}
