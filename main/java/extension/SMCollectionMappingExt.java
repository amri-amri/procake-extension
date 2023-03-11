package extension;

import de.uni_trier.wi2.procake.similarity.base.collection.SMCollectionMapping;
import utils.SimFunc;
import utils.WeightFunc;

public interface SMCollectionMappingExt extends SMCollectionMapping {
    void setSimilarityToUse(SimFunc similarityToUse);
    SimFunc getSimilarityToUseFunc();

    void setWeightFunction(WeightFunc weightFunc);
    WeightFunc getWeightFunction();
}
