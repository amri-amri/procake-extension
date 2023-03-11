package extension;

import utils.SimFunc;
import utils.WeightFunc;

public interface SMCollectionIsolatedMappingExt {
    void setSimilarityToUse(SimFunc similarityToUse);
    SimFunc getSimilarityToUseFunc();

    void setWeightFunction(WeightFunc weightFunc);
    WeightFunc getWeightFunction();

}
