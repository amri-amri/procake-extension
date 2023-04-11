package extension;

import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.data.object.base.ListObject;
import de.uni_trier.wi2.procake.data.objectpool.DataObjectIterator;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.SimilarityValuator;
import de.uni_trier.wi2.procake.similarity.base.collection.SMCollectionIsolatedMapping;
import de.uni_trier.wi2.procake.similarity.base.collection.impl.SMListMappingImpl;
import de.uni_trier.wi2.procake.similarity.impl.SimilarityImpl;
import utils.SimFunc;
import utils.WeightFunc;

import java.util.ArrayList;

public class SMListMappingImplExt extends SMListMappingImpl implements SMCollectionIsolatedMapping, ISimFunc, IWeightFunc {

    protected SimFunc similarityToUseFunc;
    protected WeightFunc weightFunc = (a) -> 1;

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
        this.weightFunc = (q) -> {
            Double weight = weightFunc.apply(q);
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

    //private boolean containsExact = DEFAULT_CONTAINS_EXACT;

    @Override
    public Similarity compute(
            DataObject queryObject, DataObject caseObject, SimilarityValuator valuator) {

        Similarity similarity = checkStoppingCriteria(queryObject, caseObject);
        if (similarity != null) {
            return similarity;
        }

        if (containsExact()) {
            return computeContainsExact((ListObject) queryObject, (ListObject) caseObject, valuator);
        } else {
            return computeContainsInexact(
                    (ListObject) queryObject, (ListObject) caseObject, valuator, true);
        }
    }

    private SimilarityImpl computeContainsExact(
            ListObject queryObject, ListObject caseObject, SimilarityValuator valuator) {

        // if the lists have different sizes, the similarity is 0.0
        if (queryObject.size() != caseObject.size()) {
            return new SimilarityImpl(this, queryObject, caseObject, 0.0);
        }

        double simSum = 0;
        double simCount = 0;

        ArrayList<Similarity> localSimilarities = new ArrayList<>();

        // each query element is compared to the case element at the exact position
        DataObjectIterator queryIt = (queryObject).iterator();
        DataObjectIterator caseIt = (caseObject).iterator();
        while (queryIt.hasNext() && caseIt.hasNext()) {
            DataObject queryElement = (DataObject) queryIt.next();
            DataObject caseElement = (DataObject) caseIt.next();
            String simToUse = getSimilarityToUseFunc().apply(queryElement, caseElement);
            double weight = getWeightFunction().apply(queryElement);
            Similarity currentSimilarity =
                    valuator.computeSimilarity(queryElement, caseElement, simToUse);
            currentSimilarity = new SimilarityImpl(valuator.getSimilarityModel().getSimilarityMeasure(queryElement.getDataClass(), simToUse), queryElement, caseElement, currentSimilarity.getValue() * weight);
            simSum += currentSimilarity.getValue();
            simCount += weight;
            localSimilarities.add(currentSimilarity);
        }

        if (simCount == 0) {
            // simCount can't be 0.0, because for empty lists this method wouldn't be called
            simCount = 1;
        }
        return new SimilarityImpl(this, queryObject, caseObject, simSum / simCount, localSimilarities);
    }

    private SimilarityImpl computeContainsInexact(

            ListObject largerList,
            ListObject smallerList,
            SimilarityValuator valuator,
            boolean queryFirst) {



        SimilarityImpl collectionSimilarity = new SimilarityImpl(this, largerList, smallerList, -1.0);

        if (largerList.size() > smallerList.size()) {

            double maxSim = collectionSimilarity.getValue();
            for (int i = 0; i <= (largerList.size() - smallerList.size()); i++) {
                double simSum = 0;
                double simCount = 0;
                ArrayList<Similarity> localSimilarities = new ArrayList<>();
                DataObjectIterator queryIt = (largerList).iterator();
                DataObjectIterator caseIt = (smallerList).iterator();
                DataObject queryElement;

                // the first elements of the query are ignored, so that there's a possible solution for each
                // element
                for (int j = 0; j < i; j++) {
                    queryElement = (DataObject) queryIt.next();
                }
                // all possible matches are made
                while (queryIt.hasNext() & caseIt.hasNext()) {
                    queryElement = (DataObject) queryIt.next();
                    DataObject caseElement = (DataObject) caseIt.next();
                    String simToUse = getSimilarityToUseFunc().apply(queryElement,caseElement);
                    double weight;
                    if (queryFirst) weight = getWeightFunction().apply(queryElement);
                    else weight = getWeightFunction().apply(caseElement);

                    // the query has to be at the first position, because the similarity computation can be
                    // asymetric

                    Similarity currentSimilarity;
                    if (queryFirst) {
                        currentSimilarity =
                                valuator.computeSimilarity(queryElement, caseElement, getSimilarityToUse());
                        currentSimilarity = new SimilarityImpl(valuator.getSimilarityModel().getSimilarityMeasure(queryElement.getDataClass(), simToUse), queryElement, caseElement, currentSimilarity.getValue() * weight);
                    } else {
                        currentSimilarity =
                                valuator.computeSimilarity(caseElement, queryElement, getSimilarityToUse());
                        currentSimilarity = new SimilarityImpl(valuator.getSimilarityModel().getSimilarityMeasure(caseElement.getDataClass(), simToUse), caseElement, queryElement, currentSimilarity.getValue() * weight);

                    }
                    simSum += currentSimilarity.getValue();
                    simCount += weight;
                    localSimilarities.add(currentSimilarity);
                }

                // adding the difference in lengths
                //                simCount += (queryObject.size()-caseObject.size());

                if (simCount == 0) {
                    simCount = 1;
                }
                // if the new computed similarity is higher than the present one, it's the new maximum value
                if ((simSum / simCount) > maxSim) {
                    maxSim = (simSum / simCount);
                    if (queryFirst) {
                        collectionSimilarity =
                                new SimilarityImpl(this, largerList, smallerList, maxSim, localSimilarities);
                    } else {
                        collectionSimilarity =
                                new SimilarityImpl(this, smallerList, largerList, maxSim, localSimilarities);
                    }
                }
            }
            return collectionSimilarity;
        } else if (largerList.size() < smallerList.size()) {
            // if the case is bigger than the query, the same method is called again with swapped objects,
            // so the computation was just implemented once
            return computeContainsInexact(smallerList, largerList, valuator, false);
        }
        // if both lists have the same size, they just can match exactly, so the method for the exact
        // contains is called
        return computeContainsExact(largerList, smallerList, valuator);
    }

}
