package extension;

import de.uni_trier.wi2.procake.data.model.DataClass;
import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.data.object.base.CollectionObject;
import de.uni_trier.wi2.procake.data.objectpool.DataObjectIterator;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.SimilarityMeasure;
import de.uni_trier.wi2.procake.similarity.SimilarityValuator;
import de.uni_trier.wi2.procake.similarity.base.collection.SMCollectionMapping;
import de.uni_trier.wi2.procake.similarity.base.collection.impl.SMCollectionImpl;
import de.uni_trier.wi2.procake.similarity.base.collection.impl.SMCollectionMappingImpl;
import de.uni_trier.wi2.procake.similarity.impl.SimilarityImpl;
import org.apache.commons.collections4.map.MultiKeyMap;
import utils.SimFunc;
import utils.WeightFunc;

import java.security.InvalidParameterException;
import java.util.*;

public class SMCollectionMappingImplExt extends SMCollectionMappingImpl implements SMCollectionMappingExt {
    protected SimFunc similarityToUseFunc;
    protected WeightFunc weightFunc = (a, b) -> 1;

    @Override
    public void setSimilarityToUse(String newValue) {
        super.setSimilarityToUse(newValue);
        similarityToUseFunc = (a, b) -> newValue;
    }

    @Override
    public void setSimilarityToUse(SimFunc similarityToUse){
        similarityToUseFunc = similarityToUse;
    }

    @Override
    public utils.SimFunc getSimilarityToUseFunc() {
        return similarityToUseFunc;
    }

    @Override
    public void setWeightFunction(WeightFunc weightFunc) {
        this.weightFunc = (a, b) -> {
            Double weight = weightFunc.apply(a, b);
            if (weight==null) return 1;
            if (weight<0) return 0;
            if (weight>1) return 1;
            return weight;
        };
    }

    @Override
    public WeightFunc getWeightFunction() {
        return weightFunc;
    }


    @Override
    public Similarity compute(
            DataObject queryObject, DataObject caseObject, SimilarityValuator valuator) {

        return new SimilarityImpl(
                this, queryObject, caseObject, -1, null);
    }



}
