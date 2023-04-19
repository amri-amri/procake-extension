package extension.similarity.measure;

import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.data.object.base.ListObject;
import de.uni_trier.wi2.procake.data.objectpool.DataObjectIterator;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.SimilarityValuator;
import de.uni_trier.wi2.procake.similarity.base.collection.SMCollectionIsolatedMapping;
import de.uni_trier.wi2.procake.similarity.base.collection.impl.SMListMappingImpl;
import de.uni_trier.wi2.procake.similarity.impl.SimilarityImpl;
import extension.abstraction.*;
import extension.similarity.valuator.SimilarityValuatorImplExt;
import utils.MethodInvoker;
import utils.MethodInvokersFunc;
import utils.SimilarityMeasureFunc;
import utils.WeightFunc;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class SMListMappingImplExt extends SMListMappingImpl implements SMCollectionIsolatedMapping, ISimilarityMeasureFunc, IWeightFunc, IMethodInvokersFunc {

    protected SimilarityMeasureFunc similarityMeasureFunc;
    protected WeightFunc weightFunc = (a) -> 1;
    protected MethodInvokersFunc methodInvokersFunc = (a, b) -> new ArrayList<MethodInvoker>();

    @Override
    public void setSimilarityToUse(String newValue) {
        super.setSimilarityToUse(newValue);
        this.similarityMeasureFunc = (a, b) -> newValue;
    }

    @Override
    public void setSimilarityMeasureFunc(SimilarityMeasureFunc similarityMeasureFunc){
        this.similarityMeasureFunc = similarityMeasureFunc;
    }

    @Override
    public SimilarityMeasureFunc getSimilarityMeasureFunc() {
        return similarityMeasureFunc;
    }

    @Override
    public void setWeightFunc(WeightFunc weightFunc) {
        this.weightFunc = (q) -> {
            Double weight = weightFunc.apply(q);
            if (weight==null) return 1;
            if (weight<0) return 0;
            if (weight>1) return 1;
            return weight;
        };
    }

    @Override
    public WeightFunc getWeightFunc() {
        return weightFunc;
    }

    @Override
    public void setMethodInvokersFunc(MethodInvokersFunc methodInvokersFunc) {
        this.methodInvokersFunc = methodInvokersFunc;
    }

    @Override
    public MethodInvokersFunc getMethodInvokersFunc() {
        return methodInvokersFunc;
    }



    @Override
    public Similarity compute(DataObject queryObject, DataObject caseObject, SimilarityValuator valuator) {

        Similarity similarity = checkStoppingCriteria(queryObject, caseObject);
        if (similarity != null) {
            return similarity;
        }

        if (containsExact()) {
            return computeContainsExact((ListObject) queryObject, (ListObject) caseObject, valuator);
        } else {
            similarity = computeContainsInexact((ListObject) queryObject, (ListObject) caseObject, valuator, true);
            return new SimilarityImpl(this, queryObject, caseObject, similarity.getValue(), (ArrayList<Similarity>) similarity.getLocalSimilarities(), similarity.getInfo());
        }
    }

    private SimilarityImpl computeContainsExact(ListObject queryObject, ListObject caseObject, SimilarityValuator valuator) {

        // if the lists have different sizes, the similarity is 0.0
        if (queryObject.size() != caseObject.size()) {
            return new SimilarityImpl(this, queryObject, caseObject, 0.0);
        }

        double similaritySum = 0;
        double denominator = 0;

        ArrayList<Similarity> localSimilarities = new ArrayList<>();

        // each query element is compared to the case element at the exact position
        DataObjectIterator queryElementIterator = (queryObject).iterator();
        DataObjectIterator caseElementIterator = (caseObject).iterator();

        while (queryElementIterator.hasNext() && caseElementIterator.hasNext()) {
            DataObject queryElement = (DataObject) queryElementIterator.next();
            DataObject caseElement = (DataObject) caseElementIterator.next();

            String localSimilarityMeasure = getSimilarityMeasureFunc().apply(queryElement, caseElement);
            double weight = getWeightFunc().apply(queryElement);

            Similarity similarity;

            if (valuator instanceof SimilarityValuatorImplExt) {
                try {
                    similarity = ((SimilarityValuatorImplExt) valuator).computeSimilarity(queryElement, caseElement, localSimilarityMeasure, getMethodInvokersFunc().apply(queryElement, caseElement));
                } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                    similarity = valuator.computeSimilarity(queryElement, caseElement, localSimilarityMeasure);
                }
            }
            else similarity = valuator.computeSimilarity(queryElement, caseElement, localSimilarityMeasure);


            similarity = new SimilarityImpl(valuator.getSimilarityModel().getSimilarityMeasure(queryElement.getDataClass(), localSimilarityMeasure), queryElement, caseElement, similarity.getValue() * weight);
            similaritySum += similarity.getValue();
            denominator += weight;
            localSimilarities.add(similarity);
        }

        if (denominator == 0) {
            // simCount can't be 0.0, because for empty lists this method wouldn't be called
            denominator = 1;
        }
        return new SimilarityImpl(this, queryObject, caseObject, similaritySum / denominator, localSimilarities);
    }

    private SimilarityImpl computeContainsInexact(ListObject largerList, ListObject smallerList, SimilarityValuator valuator, boolean queryFirst) {


        if (largerList.size() > smallerList.size()) {

            double maxSimilarityValue = -1;
            for (int i = 0; i <= (largerList.size() - smallerList.size()); i++) {
                double similaritySum = 0;
                double denominator = 0;
                ArrayList<Similarity> localSimilarities = new ArrayList<>();
                DataObjectIterator queryElementIterator = (largerList).iterator();
                DataObjectIterator caseElementIterator = (smallerList).iterator();

                // the first elements of the query are ignored, so that there's a possible solution for each
                // element
                for (int j = 0; j < i; j++) {
                    queryElementIterator.next();
                }
                // all possible matches are made
                while (queryElementIterator.hasNext() & caseElementIterator.hasNext()) {
                    DataObject queryElement = (DataObject) queryElementIterator.next();
                    DataObject caseElement = (DataObject) caseElementIterator.next();

                    // the query has to be at the first position, because the similarity computation can be
                    // asymetric

                    double weight;
                    Similarity currentSimilarity;
                    String localSimilarityMeasure;
                    if (queryFirst) {
                        localSimilarityMeasure = getSimilarityMeasureFunc().apply(queryElement, caseElement);
                        weight = getWeightFunc().apply(queryElement);

                        if (valuator instanceof SimilarityValuatorImplExt) {
                            try {
                                currentSimilarity = ((SimilarityValuatorImplExt) valuator).computeSimilarity(queryElement, caseElement, localSimilarityMeasure, getMethodInvokersFunc().apply(queryElement, caseElement));
                            } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                                currentSimilarity = valuator.computeSimilarity(queryElement, caseElement, localSimilarityMeasure);
                            }
                        }
                        else currentSimilarity = valuator.computeSimilarity(queryElement, caseElement, localSimilarityMeasure);

                        currentSimilarity = new SimilarityImpl(valuator.getSimilarityModel().getSimilarityMeasure(queryElement.getDataClass(), localSimilarityMeasure), queryElement, caseElement, currentSimilarity.getValue() * weight);
                    } else {
                        localSimilarityMeasure = getSimilarityMeasureFunc().apply(caseElement, queryElement);
                        weight = getWeightFunc().apply(caseElement);

                        if (valuator instanceof SimilarityValuatorImplExt) {
                            try {
                                currentSimilarity = ((SimilarityValuatorImplExt) valuator).computeSimilarity(caseElement, queryElement, localSimilarityMeasure, getMethodInvokersFunc().apply(caseElement, queryElement));
                            } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                                currentSimilarity = valuator.computeSimilarity(caseElement, queryElement, localSimilarityMeasure);
                            }
                        }
                        else currentSimilarity = valuator.computeSimilarity(caseElement, queryElement, localSimilarityMeasure);

                        currentSimilarity = new SimilarityImpl(valuator.getSimilarityModel().getSimilarityMeasure(caseElement.getDataClass(), localSimilarityMeasure), caseElement, queryElement, currentSimilarity.getValue() * weight);

                    }
                    similaritySum += currentSimilarity.getValue();
                    denominator += weight;
                    localSimilarities.add(currentSimilarity);
                }

                // adding the difference in lengths
                //                simCount += (queryObject.size()-caseObject.size());

                if (denominator == 0) {
                    denominator = 1;
                }
                // if the new computed similarity is higher than the present one, it's the new maximum value
                if ((similaritySum / denominator) > maxSimilarityValue) {
                    maxSimilarityValue = (similaritySum / denominator);
                    if (queryFirst) {
                        return new SimilarityImpl(this, largerList, smallerList, maxSimilarityValue, localSimilarities);
                    } else {
                        return new SimilarityImpl(this, smallerList, largerList, maxSimilarityValue, localSimilarities);
                    }
                }
            }
            return new SimilarityImpl(this, largerList, smallerList, -1.0);

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
