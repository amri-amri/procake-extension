package extension;

import de.uni_trier.wi2.procake.similarity.base.collection.SMListMapping;
import utils.SimFunc;
import utils.WeightFunc;

public interface SMListMappingExt extends SMListMapping {
    void setSimilarityToUse(SimFunc similarityToUse);
    SimFunc getSimilarityToUseFunc();

    void setWeightFunction(WeightFunc weightFunc);
    WeightFunc getWeightFunction();
}
