package extension.abstraction;

import utils.WeightFunc;

public interface IWeightFunc {
    void setWeightFunction(WeightFunc weightFunc);
    WeightFunc getWeightFunction();
}
