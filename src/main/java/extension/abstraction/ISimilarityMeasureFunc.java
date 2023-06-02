package extension.abstraction;

import utils.SimilarityMeasureFunc;

/**
 * An interface for every class using the SimilarityMeasureFunc functional interface.
 */
public interface ISimilarityMeasureFunc {

    /**
     * sets the SimilarityMeasureFunc
     *
     * @param similarityMeasureFunc  the WeightFunc to be set
     */
    void setSimilarityMeasureFunc(SimilarityMeasureFunc similarityMeasureFunc);

    /**
     * gets the SimilarityMeasureFunc
     *
     * @return the SimilarityMeasureFunc
     */
    SimilarityMeasureFunc getSimilarityMeasureFunc();
}
