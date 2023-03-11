package extension;

import de.uni_trier.wi2.procake.similarity.base.collection.SMListCorrectness;
import utils.SimFunc;
import utils.WeightFunc;

public interface SMListCorrectnessExt extends SMListCorrectness {
    void setWeightFunction(WeightFunc weightFunc);
    WeightFunc getWeightFunction();
}
