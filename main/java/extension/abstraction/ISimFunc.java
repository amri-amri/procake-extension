package extension.abstraction;

import utils.SimFunc;

public interface ISimFunc {
    void setSimilarityToUse(SimFunc similarityToUse);
    SimFunc getSimilarityToUseFunc();
}
