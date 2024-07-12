package de.uni_trier.wi2.extension.abstraction;

import de.uni_trier.wi2.utils.WeightFunc;

/**
 * An interface for every class using the WeightFunc functional interface.
 */
public interface IWeightFunc {

    /**
     * sets the WeightFunc
     *
     * @param weightFunc the WeightFunc to be set
     */
    void setWeightFunc(WeightFunc weightFunc);

    /**
     * gets the WeightFunc
     *
     * @return the WeightFunc
     */
    WeightFunc getWeightFunc();
}
