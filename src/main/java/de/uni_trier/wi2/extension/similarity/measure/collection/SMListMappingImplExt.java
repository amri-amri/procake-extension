package de.uni_trier.wi2.extension.similarity.measure.collection;

import de.uni_trier.wi2.extension.abstraction.IMethodInvokersFunc;
import de.uni_trier.wi2.extension.abstraction.INESTtoList;
import de.uni_trier.wi2.extension.abstraction.ISimilarityMeasureFunc;
import de.uni_trier.wi2.extension.abstraction.IWeightFunc;
import de.uni_trier.wi2.extension.similarity.valuator.SimilarityValuatorImplExt;
import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.data.object.base.AggregateObject;
import de.uni_trier.wi2.procake.data.object.base.ListObject;
import de.uni_trier.wi2.procake.data.object.nest.NESTSequentialWorkflowObject;
import de.uni_trier.wi2.procake.data.objectpool.DataObjectIterator;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.SimilarityValuator;
import de.uni_trier.wi2.procake.similarity.base.collection.impl.SMListMappingImpl;
import de.uni_trier.wi2.procake.similarity.impl.SimilarityImpl;
import de.uni_trier.wi2.utils.MethodInvoker;
import de.uni_trier.wi2.utils.MethodInvokersFunc;
import de.uni_trier.wi2.utils.SimilarityMeasureFunc;
import de.uni_trier.wi2.utils.WeightFunc;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import static de.uni_trier.wi2.LoggingUtils.*;
import static de.uni_trier.wi2.extension.abstraction.XESBaseToSystemClass.getXESAggregateAttributesAsSystemCollectionObject;

/**
 * A similarity measure using the 'List Mapping' algorithm for {@link ListObject}s.
 *
 * <p>For more info on the algorithm <a href="https://wi2.pages.gitlab.rlp.net/procake/procake-wiki/sim/collections/#list-mapping">click here</a>.
 *
 *
 * <p>Instead of one single local similarity measure, a functional interface ({@link SimilarityMeasureFunc})
 * can be defined for this similarity measure.
 * This functional interface assigns a similarity measure to each pair of query element
 * and case element.
 *
 * <p>These similarity measures may be defined more precisely by setting their parameters via methods.
 * In order to call these methods another functional interface ({@link MethodInvokersFunc}) can be defined
 * for this similarity measure.
 * This functional interface assigns a list of {@link MethodInvoker} objects to each pair of query element
 * and case element.
 *
 * <p>The given methods are then invoked with given parameters by the respective similarity measures.
 *
 * <p>For the usage of MethodInvoker objects an object of {@link SimilarityValuatorImplExt} has to be used as
 * similarity valuator!
 *
 * <p>In addition, a functional interface ({@link WeightFunc}) can be defined to assign a weight value
 * between 0 and 1 to a query element.
 *
 * <p> The global similarity is a weighted average of the similarity values between the query and the case elements.
 * That means that each similarity is multiplied by the weight of the query element, and the sum of these values
 * is divided by the sum of the weights.
 */
public class SMListMappingImplExt extends SMListMappingImpl implements SMListMappingExt, INESTtoList, ISimilarityMeasureFunc, IWeightFunc, IMethodInvokersFunc {

    protected SimilarityMeasureFunc similarityMeasureFunc = (a, b) -> null;
    protected WeightFunc weightFunc = (a) -> 1;
    protected MethodInvokersFunc methodInvokersFunc = (a, b) -> new ArrayList<MethodInvoker>();

    @Override
    public void setSimilarityToUse(String similarityToUse) {
        METHOD_CALL.info("public void procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.setSimilarityToUse(String similarityToUse={})...", similarityToUse);
        super.setSimilarityToUse(similarityToUse);
        similarityMeasureFunc = (a, b) -> similarityToUse;
    }

    @Override
    public SimilarityMeasureFunc getSimilarityMeasureFunc() {
        METHOD_CALL.info("public SimilarityMeasureFunc procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.getSimilarityToUse()...");
        METHOD_CALL.info("procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.getSimilarityToUse(): return {}", similarityMeasureFunc);
        return similarityMeasureFunc;
    }

    @Override
    public void setSimilarityMeasureFunc(SimilarityMeasureFunc similarityMeasureFunc) {
        METHOD_CALL.info("public void procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.setSimilarityMeasureFunc(SimilarityMeasureFunc similarityMeasureFunc={})...", similarityMeasureFunc);
        this.similarityMeasureFunc = similarityMeasureFunc;
    }

    @Override
    public MethodInvokersFunc getMethodInvokersFunc() {
        METHOD_CALL.info("public MethodInvokersFunc procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.getMethodInvokersFunc()...");
        METHOD_CALL.info("procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.getMethodInvokersFunc(): return {}", methodInvokersFunc);
        return methodInvokersFunc;
    }

    @Override
    public void setMethodInvokersFunc(MethodInvokersFunc methodInvokersFunc) {
        METHOD_CALL.info("public void procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.setMethodInvokersFunc(MethodInvokersFunc methodInvokersFunc={})...", methodInvokersFunc);
        this.methodInvokersFunc = methodInvokersFunc;
    }

    @Override
    public WeightFunc getWeightFunc() {
        METHOD_CALL.info("public WeightFunc procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.getWeightFunc()...");
        METHOD_CALL.info("procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.getWeightFunc(): return {}", weightFunc);
        return weightFunc;
    }

    @Override
    public void setWeightFunc(WeightFunc weightFunc) {
        METHOD_CALL.info("public void procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.setWeightFunc(WeightFunc weightFunc={})...", weightFunc);
        this.weightFunc = (q) -> {
            Double weight = weightFunc.apply(q);
            if (weight == null) return 1;
            if (weight < 0) return 0;
            if (weight > 1) return 1;
            return weight;
        };
    }

    public String getSystemName() {
        METHOD_CALL.info("public String procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.getSystemName()...");
        METHOD_CALL.info("procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.getSystemName(): return {}", SMListMappingExt.NAME);
        return SMListMappingExt.NAME;
    }


    @Override
    public Similarity compute(DataObject queryObject, DataObject caseObject, SimilarityValuator valuator) {
        METHOD_CALL.info("public Similarity procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.compute(DataObject queryObject={}, DataObject caseObject={}, SimilarityValuator valuator={})...",
                maxSubstring(queryObject), maxSubstring(caseObject), maxSubstring(valuator));

        ListObject queryList, caseList;

        if (queryObject.getDataClass().isSubclassOf(queryObject.getModel().getClass("XESListClass")))
            queryList = (ListObject) getXESAggregateAttributesAsSystemCollectionObject((AggregateObject) queryObject);
        else if (queryObject.isNESTSequentialWorkflow()) queryList = toList((NESTSequentialWorkflowObject) queryObject);
        else queryList = (ListObject) queryObject;

        if (caseObject.getDataClass().isSubclassOf(caseObject.getModel().getClass("XESListClass")))
            caseList = (ListObject) getXESAggregateAttributesAsSystemCollectionObject((AggregateObject) caseObject);
        else if (caseObject.isNESTSequentialWorkflow()) caseList = toList((NESTSequentialWorkflowObject) caseObject);
        else caseList = (ListObject) caseObject;

        DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.compute(DataObject, DataObject, SimilarityValuator): queryList={}, caseList={}",
                maxSubstring(queryList), maxSubstring(caseList));

        DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.compute(DataObject, DataObject, SimilarityValuator): Similarity similarity = de.uni_trier.wi2.procake.similarity.base.collection.impl.SMCollectionImpl.checkStoppingCriteria(queryList, caseList);");

        Similarity similarity = checkStoppingCriteria(queryList, caseList);
        if (similarity != null) {
            METHOD_CALL.info("procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.compute(DataObject, DataObject, SimilarityValuator): return {}", maxSubstring(similarity));
            return similarity;
        }

        if (containsExact()) {
            similarity = computeContainsExact(queryList, caseList, valuator, queryObject, caseObject);
        } else {
            similarity = computeContainsInexact(queryList, caseList, valuator, true, queryObject, caseObject);
        }

        METHOD_CALL.info("procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.compute(DataObject, DataObject, SimilarityValuator): return Similarity");
        return similarity;
    }

    private SimilarityImpl computeContainsExact(ListObject queryList, ListObject caseList, SimilarityValuator valuator, DataObject queryObject, DataObject caseObject) {
        METHOD_CALL.info("public Similarity procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.computeContainsExact(ListObject queryList={}, ListObject caseList={}, SimilarityValuator valuator={}, DataObject queryObject, DataObject caseObject)...",
                maxSubstring(queryList), maxSubstring(caseList), maxSubstring(valuator));

        // if the lists have different sizes, the similarity is 0.0
        if (queryList.size() != caseList.size()) {
            DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.computeContainsExact(ListObject, ListObject, SimilarityValuator, DataObject, DataObject): the lists have different sizes, the similarity is 0.0");
            METHOD_CALL.info("procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.computeContainsExact(ListObject, ListObject, SimilarityValuator, DataObject, DataObject): return {}", maxSubstring(new SimilarityImpl(this, queryObject, caseObject, 0.0)));
            return new SimilarityImpl(this, queryObject, caseObject, 0.0);
        }

        double similaritySum = 0;
        double denominator = 0;

        ArrayList<Similarity> localSimilarities = new ArrayList<>();

        // each query element is compared to the case element at the exact position
        DataObjectIterator queryElementIterator = (queryList).iterator();
        DataObjectIterator caseElementIterator = (caseList).iterator();

        while (queryElementIterator.hasNext() && caseElementIterator.hasNext()) {
            DataObject queryElement = (DataObject) queryElementIterator.next();
            DataObject caseElement = (DataObject) caseElementIterator.next();

            DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.computeContainsExact(ListObject, ListObject, SimilarityValuator, DataObject, DataObject): queryElement={}, caseElement={}", maxSubstring(queryElement), maxSubstring(caseElement));

            String localSimilarityMeasure = getSimilarityMeasureFunc().apply(queryElement, caseElement);
            if (localSimilarityMeasure == null)
                localSimilarityMeasure = valuator.getSimilarityMeasure(queryElement, caseElement).getSystemName();

            DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.computeContainsExact(ListObject, ListObject, SimilarityValuator, DataObject, DataObject): localSimilarityMeasure={}", maxSubstring(localSimilarityMeasure));

            double weight = getWeightFunc().apply(queryElement);

            DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.computeContainsExact(ListObject, ListObject, SimilarityValuator, DataObject, DataObject): weight={}", maxSubstring(weight));

            Similarity similarity;

            if (valuator instanceof SimilarityValuatorImplExt) {
                try {
                    similarity = ((SimilarityValuatorImplExt) valuator).computeSimilarity(queryElement, caseElement, localSimilarityMeasure, getMethodInvokersFunc().apply(queryElement, caseElement));
                } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                    similarity = valuator.computeSimilarity(queryElement, caseElement, localSimilarityMeasure);
                }
            } else similarity = valuator.computeSimilarity(queryElement, caseElement, localSimilarityMeasure);

            DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.computeContainsExact(ListObject, ListObject, SimilarityValuator, DataObject, DataObject): similarity={}", maxSubstring(similarity));

            similarity = new SimilarityImpl(valuator.getSimilarityModel().getSimilarityMeasure(queryElement.getDataClass(), localSimilarityMeasure), queryElement, caseElement, similarity.getValue() * weight);

            DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.computeContainsExact(ListObject, ListObject, SimilarityValuator, DataObject, DataObject): apply weight...");
            DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.computeContainsExact(ListObject, ListObject, SimilarityValuator, DataObject, DataObject): similarity={}", maxSubstring(similarity));

            similaritySum += similarity.getValue();
            denominator += weight;

            DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.computeContainsExact(ListObject, ListObject, SimilarityValuator, DataObject, DataObject): similaritySum={}, denominator={}", similaritySum, denominator);

            localSimilarities.add(similarity);
        }

        if (denominator == 0) {
            // simCount can't be 0.0, because for empty lists this method wouldn't be called
            denominator = 1;
        }

        METHOD_CALL.info("procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.computeContainsExact(ListObject, ListObject, SimilarityValuator, DataObject, DataObject): return {}", maxSubstring(new SimilarityImpl(this, queryObject, caseObject, similaritySum / denominator, localSimilarities)));
        return new SimilarityImpl(this, queryObject, caseObject, similaritySum / denominator, localSimilarities);
    }

    private SimilarityImpl computeContainsInexact(ListObject largerList, ListObject smallerList, SimilarityValuator valuator, boolean queryFirst, DataObject queryObject, DataObject caseObject) {
        METHOD_CALL.info("public Similarity procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.computeContainsInexact(ListObject largerList={}, ListObject smallerList={}, SimilarityValuator valuator={}, DataObject queryObject, DataObject caseObject)...",
                maxSubstring(largerList), maxSubstring(smallerList), maxSubstring(valuator));

        SimilarityImpl similarity = new SimilarityImpl(this, queryObject, caseObject, -1.0);

        if (largerList.size() > smallerList.size()) {
            DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.computeContainsInexact(ListObject, ListObject, SimilarityValuator, DataObject, DataObject): largerList.size() > smallerList.size()");

            double maxSimilarityValue = -1;
            for (int i = 0; i <= (largerList.size() - smallerList.size()); i++) {
                DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.computeContainsInexact(ListObject, ListObject, SimilarityValuator, DataObject, DataObject): i={}", i);

                double similaritySum = 0;
                double denominator = 0;
                ArrayList<Similarity> localSimilarities = new ArrayList<>();
                DataObjectIterator queryElementIterator = (largerList).iterator();
                DataObjectIterator caseElementIterator = (smallerList).iterator();

                // the first elements of the query are ignored, so that there's a possible solution for each element
                for (int j = 0; j < i; j++) {
                    queryElementIterator.next();
                }
                // all possible matches are made
                while (queryElementIterator.hasNext() & caseElementIterator.hasNext()) {
                    DataObject queryElement = (DataObject) queryElementIterator.next();
                    DataObject caseElement = (DataObject) caseElementIterator.next();

                    DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.computeContainsInexact(ListObject, ListObject, SimilarityValuator, DataObject, DataObject): queryElement={}, caseElement={}", maxSubstring(queryElement), maxSubstring(caseElement));

                    // the query has to be at the first position, because the similarity computation can be
                    // asymetric

                    double weight;
                    Similarity currentSimilarity;
                    String localSimilarityMeasure;
                    if (queryFirst) {
                        DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.computeContainsInexact(ListObject, ListObject, SimilarityValuator, DataObject, DataObject): queryFirst={}", queryFirst);

                        localSimilarityMeasure = getSimilarityMeasureFunc().apply(queryElement, caseElement);

                        if (localSimilarityMeasure == null)
                            localSimilarityMeasure = valuator.getSimilarityMeasure(queryElement, caseElement).getSystemName();

                        DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.computeContainsInexact(ListObject, ListObject, SimilarityValuator, DataObject, DataObject): localSimilarityMeasure={}", maxSubstring(localSimilarityMeasure));

                        weight = getWeightFunc().apply(queryElement);

                        DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.computeContainsInexact(ListObject, ListObject, SimilarityValuator, DataObject, DataObject): weight={}", maxSubstring(weight));

                        if (valuator instanceof SimilarityValuatorImplExt) {
                            try {
                                currentSimilarity = ((SimilarityValuatorImplExt) valuator).computeSimilarity(queryElement, caseElement, localSimilarityMeasure, getMethodInvokersFunc().apply(queryElement, caseElement));
                            } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                                currentSimilarity = valuator.computeSimilarity(queryElement, caseElement, localSimilarityMeasure);
                            }
                        } else
                            currentSimilarity = valuator.computeSimilarity(queryElement, caseElement, localSimilarityMeasure);

                        DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.computeContainsInexact(ListObject, ListObject, SimilarityValuator, DataObject, DataObject): currentSimilarity={}", maxSubstring(currentSimilarity));

                        DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.computeContainsInexact(ListObject, ListObject, SimilarityValuator, DataObject, DataObject): apply weight...");

                        currentSimilarity = new SimilarityImpl(valuator.getSimilarityModel().getSimilarityMeasure(queryElement.getDataClass(), localSimilarityMeasure), queryElement, caseElement, currentSimilarity.getValue() * weight);

                        DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.computeContainsInexact(ListObject, ListObject, SimilarityValuator, DataObject, DataObject): currentSimilarity={}", maxSubstring(currentSimilarity));

                    } else {
                        DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.computeContainsInexact(ListObject, ListObject, SimilarityValuator, DataObject, DataObject): queryFirst={}", queryFirst);

                        localSimilarityMeasure = getSimilarityMeasureFunc().apply(caseElement, queryElement);

                        if (localSimilarityMeasure == null)
                            localSimilarityMeasure = valuator.getSimilarityMeasure(queryElement, caseElement).getSystemName();

                        DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.computeContainsInexact(ListObject, ListObject, SimilarityValuator, DataObject, DataObject): localSimilarityMeasure={}", maxSubstring(localSimilarityMeasure));

                        weight = getWeightFunc().apply(caseElement);

                        DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.computeContainsInexact(ListObject, ListObject, SimilarityValuator, DataObject, DataObject): weight={}", maxSubstring(weight));

                        if (valuator instanceof SimilarityValuatorImplExt) {
                            try {
                                currentSimilarity = ((SimilarityValuatorImplExt) valuator).computeSimilarity(caseElement, queryElement, localSimilarityMeasure, getMethodInvokersFunc().apply(caseElement, queryElement));
                            } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                                currentSimilarity = valuator.computeSimilarity(caseElement, queryElement, localSimilarityMeasure);
                            }
                        } else
                            currentSimilarity = valuator.computeSimilarity(caseElement, queryElement, localSimilarityMeasure);

                        DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.computeContainsInexact(ListObject, ListObject, SimilarityValuator, DataObject, DataObject): currentSimilarity={}", maxSubstring(currentSimilarity));

                        DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.computeContainsInexact(ListObject, ListObject, SimilarityValuator, DataObject, DataObject): apply weight...");

                        currentSimilarity = new SimilarityImpl(valuator.getSimilarityModel().getSimilarityMeasure(caseElement.getDataClass(), localSimilarityMeasure), caseElement, queryElement, currentSimilarity.getValue() * weight);

                        DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.computeContainsInexact(ListObject, ListObject, SimilarityValuator, DataObject, DataObject): currentSimilarity={}", maxSubstring(currentSimilarity));

                    }
                    similaritySum += currentSimilarity.getValue();
                    denominator += weight;
                    localSimilarities.add(currentSimilarity);

                    DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.computeContainsInexact(ListObject, ListObject, SimilarityValuator, DataObject, DataObject): similaritySum={}, denominator={}", similaritySum, denominator);
                }

                // adding the difference in lengths
                //                simCount += (queryObject.size()-caseObject.size());

                if (denominator == 0) {
                    denominator = 1;
                }
                // if the new computed similarity is higher than the present one, it's the new maximum value
                if ((similaritySum / denominator) > maxSimilarityValue) {
                    DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.computeContainsInexact(ListObject, ListObject, SimilarityValuator, DataObject, DataObject): (similaritySum / denominator) > maxSimilarityValue");

                    maxSimilarityValue = (similaritySum / denominator);

                    DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.computeContainsInexact(ListObject, ListObject, SimilarityValuator, DataObject, DataObject): maxSimilarityValue={}", maxSimilarityValue);

                    if (queryFirst) {
                        similarity = new SimilarityImpl(this, queryObject, caseObject, maxSimilarityValue, localSimilarities);
                    } else {
                        similarity = new SimilarityImpl(this, caseObject, queryObject, maxSimilarityValue, localSimilarities);
                    }
                    DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.computeContainsInexact(ListObject, ListObject, SimilarityValuator, DataObject, DataObject): similarity={}", maxSubstring(similarity));
                } else {
                    DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.computeContainsInexact(ListObject, ListObject, SimilarityValuator, DataObject, DataObject): (similaritySum / denominator) <= maxSimilarityValue");
                }
            }
            METHOD_CALL.info("procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.computeContainsInexact(ListObject, ListObject, SimilarityValuator, DataObject, DataObject): return {}", maxSubstring(similarity));
            return similarity;

        } else if (largerList.size() < smallerList.size()) {
            DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.computeContainsInexact(ListObject, ListObject, SimilarityValuator, DataObject, DataObject): largerList.size() < smallerList.size()");
            // if the case is bigger than the query, the same method is called again with swapped objects,
            // so the computation was just implemented once
            similarity = computeContainsInexact(smallerList, largerList, valuator, false, caseObject, queryObject);

            METHOD_CALL.info("procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.computeContainsInexact(ListObject, ListObject, SimilarityValuator, DataObject, DataObject): return Similarity");

            return similarity;
        }
        DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.computeContainsInexact(ListObject, ListObject, SimilarityValuator, DataObject, DataObject): largerList.size() == smallerList.size()");
        // if both lists have the same size, they just can match exactly, so the method for the exact
        // contains is called
        similarity = computeContainsExact(largerList, smallerList, valuator, queryObject, caseObject);

        METHOD_CALL.info("procake-extension.extension.similarity.measure.collection.SMListMappingImplExt.computeContainsInexact(ListObject, ListObject, SimilarityValuator, DataObject, DataObject): return Similarity");

        return similarity;
    }

}
