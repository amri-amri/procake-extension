package de.uni_trier.wi2.extension.similarity.measure.collection;

import de.uni_trier.wi2.extension.abstraction.IMethodInvokersFunc;
import de.uni_trier.wi2.extension.abstraction.INESTtoList;
import de.uni_trier.wi2.extension.abstraction.ISimilarityMeasureFunc;
import de.uni_trier.wi2.extension.abstraction.IWeightFunc;
import de.uni_trier.wi2.extension.similarity.valuator.SimilarityValuatorImplExt;
import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.data.object.base.AggregateObject;
import de.uni_trier.wi2.procake.data.object.base.CollectionObject;
import de.uni_trier.wi2.procake.data.object.nest.NESTSequentialWorkflowObject;
import de.uni_trier.wi2.procake.data.objectpool.DataObjectIterator;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.SimilarityValuator;
import de.uni_trier.wi2.procake.similarity.base.collection.impl.SMCollectionIsolatedMappingImpl;
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
 * A similarity measure using the 'Isolated Mapping' algorithm for {@link CollectionObject}s.
 *
 * <p>The 'Isolated Mapping' algorithm assigns to each element of the query collection the case element
 * with which the respective query element has the highest (local) weighted similarity.
 *
 * <p>The overall similarity between query and case collection is the sum of the local weighted
 * similarities divided by the sum of the weights.
 *
 * <p>The weight values depend solely on the characteristics of the query elements and can
 * be defined by a functional interface ({@link WeightFunc}).
 *
 * <p>For more info on the algorithm <a href="https://wi2.pages.gitlab.rlp.net/procake/procake-wiki/sim/collections/#isolated-mapping">click here</a>.
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
 */
public class SMCollectionIsolatedMappingImplExt extends SMCollectionIsolatedMappingImpl implements SMCollectionIsolatedMappingExt, INESTtoList, ISimilarityMeasureFunc, IWeightFunc, IMethodInvokersFunc {

    protected SimilarityMeasureFunc similarityMeasureFunc = (a, b) -> null;
    protected MethodInvokersFunc methodInvokersFunc = (a, b) -> new ArrayList<>();
    protected WeightFunc weightFunc = (a) -> 1;

    @Override
    public void setSimilarityToUse(String similarityToUse) {
        METHOD_CALL.info("public void procake-extension.extension.similarity.measure.collection.SMCollectionIsolatedMappingImplExt.setSimilarityToUse(String similarityToUse={})...", similarityToUse);
        super.setSimilarityToUse(similarityToUse);
        similarityMeasureFunc = (a, b) -> similarityToUse;
    }

    @Override
    public SimilarityMeasureFunc getSimilarityMeasureFunc() {
        METHOD_CALL.info("public SimilarityMeasureFunc procake-extension.extension.similarity.measure.collection.SMCollectionIsolatedMappingImplExt.getSimilarityToUse()...");
        METHOD_CALL.info("procake-extension.extension.similarity.measure.collection.SMCollectionIsolatedMappingImplExt.getSimilarityToUse(): return {}", similarityMeasureFunc);
        return similarityMeasureFunc;
    }

    @Override
    public void setSimilarityMeasureFunc(SimilarityMeasureFunc similarityMeasureFunc) {
        METHOD_CALL.info("public void procake-extension.extension.similarity.measure.collection.SMCollectionIsolatedMappingImplExt.setSimilarityMeasureFunc(SimilarityMeasureFunc similarityMeasureFunc={})...", similarityMeasureFunc);
        this.similarityMeasureFunc = similarityMeasureFunc;
    }

    @Override
    public MethodInvokersFunc getMethodInvokersFunc() {
        METHOD_CALL.info("public MethodInvokersFunc procake-extension.extension.similarity.measure.collection.SMCollectionIsolatedMappingImplExt.getMethodInvokersFunc()...");
        METHOD_CALL.info("procake-extension.extension.similarity.measure.collection.SMCollectionIsolatedMappingImplExt.getMethodInvokersFunc(): return {}", methodInvokersFunc);
        return methodInvokersFunc;
    }

    @Override
    public void setMethodInvokersFunc(MethodInvokersFunc methodInvokersFunc) {
        METHOD_CALL.info("public void procake-extension.extension.similarity.measure.collection.SMCollectionIsolatedMappingImplExt.setMethodInvokersFunc(MethodInvokersFunc methodInvokersFunc={})...", methodInvokersFunc);
        this.methodInvokersFunc = methodInvokersFunc;
    }

    @Override
    public WeightFunc getWeightFunc() {
        METHOD_CALL.info("public WeightFunc procake-extension.extension.similarity.measure.collection.SMCollectionIsolatedMappingImplExt.getWeightFunc()...");
        METHOD_CALL.info("procake-extension.extension.similarity.measure.collection.SMCollectionIsolatedMappingImplExt.getWeightFunc(): return {}", weightFunc);
        return weightFunc;
    }

    @Override
    public void setWeightFunc(WeightFunc weightFunc) {
        METHOD_CALL.info("public void procake-extension.extension.similarity.measure.collection.SMCollectionIsolatedMappingImplExt.setWeightFunc(WeightFunc weightFunc={})...", weightFunc);
        this.weightFunc = (q) -> {
            Double weight = weightFunc.apply(q);
            if (weight == null) return 1;
            if (weight < 0) return 0;
            if (weight > 1) return 1;
            return weight;
        };
    }

    public String getSystemName() {
        METHOD_CALL.info("public String procake-extension.extension.similarity.measure.collection.SMCollectionIsolatedMappingImplExt.getSystemName()...");
        METHOD_CALL.info("procake-extension.extension.similarity.measure.collection.SMCollectionIsolatedMappingImplExt.getSystemName(): return {}", SMCollectionIsolatedMappingExt.NAME);
        return SMCollectionIsolatedMappingExt.NAME;
    }


    /**
     * Computes the isolated mapping similarity between a given query- and case- object of type Collection.
     *
     * @param queryObject the query object of type Collection
     * @param caseObject  the case object of type Collection
     * @param valuator    the similarity valuator used for computation
     * @return similarity object containing local similarities
     */
    @Override
    public Similarity compute(DataObject queryObject, DataObject caseObject, SimilarityValuator valuator) {
        METHOD_CALL.info("public Similarity procake-extension.extension.similarity.measure.collection.SMCollectionIsolatedMappingImplExt.compute(DataObject queryObject={}, DataObject caseObject={}, SimilarityValuator valuator={})", maxSubstring(queryObject), maxSubstring(caseObject), maxSubstring(valuator));

        CollectionObject queryCollection, caseCollection;

        if (queryObject.getDataClass().isSubclassOf(queryObject.getModel().getClass("XESBaseClass")))
            queryCollection = getXESAggregateAttributesAsSystemCollectionObject((AggregateObject) queryObject);
        else if (queryObject.isNESTSequentialWorkflow())
            queryCollection = toList((NESTSequentialWorkflowObject) queryObject);
        else queryCollection = (CollectionObject) queryObject;

        if (caseObject.getDataClass().isSubclassOf(caseObject.getModel().getClass("XESBaseClass")))
            caseCollection = getXESAggregateAttributesAsSystemCollectionObject((AggregateObject) caseObject);
        else if (caseObject.isNESTSequentialWorkflow())
            caseCollection = toList((NESTSequentialWorkflowObject) caseObject);
        else caseCollection = (CollectionObject) caseObject;

        DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMCollectionIsolatedMappingImplExt.compute(DataObject, DataObject, SimilarityValuator): queryCollection={}, caseCollection={}", maxSubstring(queryCollection), maxSubstring(caseCollection));

        DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMCollectionIsolatedMappingImplExt.compute(DataObject, DataObject, SimilarityValuator): Similarity similarity = de.uni_trier.wi2.procake.similarity.base.collection.impl.SMCollectionImpl.checkStoppingCriteria(queryCollection, caseCollection);");

        Similarity similarity = checkStoppingCriteria(queryCollection, caseCollection);

        if (similarity != null) {
            METHOD_CALL.info("procake-extension.extension.similarity.measure.collection.SMCollectionIsolatedMappingImplExt.compute(DataObject, DataObject, SimilarityValuator): return {}", maxSubstring(similarity));
            return similarity;
        }

        double similaritySum = 0.0;
        ArrayList<Similarity> localSimilarities = new ArrayList<>();
        DataObjectIterator queryElementIterator = queryCollection.iterator();

        double divisor = 0.0;
        double weight;

        // Iterate through all elements of the query collection and find the best possible mapping with
        // the highest possible similarity.
        // A case element may be mapped multiple times to different query elements.
        while (queryElementIterator.hasNext()) {
            DataObject queryElement = queryElementIterator.nextDataObject();

            DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMCollectionIsolatedMappingImplExt.compute(DataObject, DataObject, SimilarityValuator): queryElement={}", maxSubstring(queryElement));

            Similarity localSimilarity = this.computeLocalSimilarity(queryElement, caseCollection, valuator);

            DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMCollectionIsolatedMappingImplExt.compute(DataObject, DataObject, SimilarityValuator): localSimilarity={}", maxSubstring(localSimilarity));

            similaritySum += localSimilarity.getValue();
            weight = getWeightFunc().apply(queryElement);
            divisor += weight;

            DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMCollectionIsolatedMappingImplExt.compute(DataObject, DataObject, SimilarityValuator): similaritySum={}", similaritySum);
            DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMCollectionIsolatedMappingImplExt.compute(DataObject, DataObject, SimilarityValuator): weight={}", weight);
            DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMCollectionIsolatedMappingImplExt.compute(DataObject, DataObject, SimilarityValuator): divisor={}", divisor);

            localSimilarities.add(localSimilarity);
        }

        similarity = new SimilarityImpl(
                this,
                queryObject,
                caseObject,
                similaritySum / divisor,
                localSimilarities);

        METHOD_CALL.info("procake-extension.extension.similarity.measure.collection.SMCollectionIsolatedMappingImplExt.compute(DataObject, DataObject, SimilarityValuator): return Similarity {}", maxSubstring(similarity));

        return similarity;
    }

    @Override
    protected Similarity computeLocalSimilarity(DataObject queryElement, CollectionObject caseCollection, SimilarityValuator valuator) {
        METHOD_CALL.info(
                "protected Similarity procake-extension.extension.similarity.measure.collection.SMCollectionIsolatedMappingImplExt" +
                        ".computeLocalSimilarity(DataObject queryElement={}, CollectionObject caseCollection={}, SimilarityValuator valuator={})",
                maxSubstring(queryElement), maxSubstring(caseCollection), maxSubstring(valuator));

        String localSimilarityMeasure;
        double weight = getWeightFunc().apply(queryElement);
        Similarity maxSimilarity = new SimilarityImpl(null, queryElement, null, 0.0);

        DataObjectIterator caseElementIterator = caseCollection.iterator();

        DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMCollectionIsolatedMappingImplExt." +
                "computeLocalSimilarity(DataObject, CollectionObject , SimilarityValuator): weight = {}", weight);

        while (caseElementIterator.hasNext()) {
            DataObject caseElement = caseElementIterator.nextDataObject();

            DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMCollectionIsolatedMappingImplExt." +
                    "computeLocalSimilarity(DataObject, CollectionObject , SimilarityValuator): " +
                    "caseElement = {}", caseElement);


            localSimilarityMeasure = getSimilarityMeasureFunc().apply(queryElement, caseElement);

            if (localSimilarityMeasure == null)
                localSimilarityMeasure = valuator.getSimilarityMeasure(queryElement, caseElement).getSystemName();

            DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMCollectionIsolatedMappingImplExt." +
                    "computeLocalSimilarity(DataObject, CollectionObject , SimilarityValuator): " +
                    "localSimilarityMeasure = {}", localSimilarityMeasure);

            Similarity similarity;

            if (valuator instanceof SimilarityValuatorImplExt) {
                try {
                    similarity = ((SimilarityValuatorImplExt) valuator).computeSimilarity(queryElement, caseElement, localSimilarityMeasure, methodInvokersFunc.apply(queryElement, caseElement));

                    DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMCollectionIsolatedMappingImplExt." +
                            "computeLocalSimilarity(DataObject, CollectionObject , SimilarityValuator): " +
                            "similarity = ((SimilarityValuatorImplExt) valuator).computeSimilarity(queryElement, caseElement, localSimilarityMeasure, methodInvokersFunc.apply(queryElement, caseElement));");
                } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                    similarity = valuator.computeSimilarity(queryElement, caseElement, localSimilarityMeasure);

                    DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMCollectionIsolatedMappingImplExt." +
                            "computeLocalSimilarity(DataObject, CollectionObject , SimilarityValuator): " +
                            "similarity = valuator.computeSimilarity(queryElement, caseElement, localSimilarityMeasure);");
                }
            } else {
                similarity = valuator.computeSimilarity(queryElement, caseElement, localSimilarityMeasure);

                DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMCollectionIsolatedMappingImplExt." +
                        "computeLocalSimilarity(DataObject, CollectionObject , SimilarityValuator): " +
                        "similarity = valuator.computeSimilarity(queryElement, caseElement, localSimilarityMeasure);");
            }

            DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMCollectionIsolatedMappingImplExt." +
                    "computeLocalSimilarity(DataObject, CollectionObject , SimilarityValuator): " +
                    "similarity = {}", similarity);

            //Application of the weight function
            similarity = new SimilarityImpl(
                    valuator.getSimilarityModel().getSimilarityMeasure(queryElement.getDataClass(), localSimilarityMeasure),
                    queryElement,
                    caseElement,
                    similarity.getValue() * weight,
                    (ArrayList<Similarity>) similarity.getLocalSimilarities(),
                    similarity.getInfo());

            DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMCollectionIsolatedMappingImplExt." +
                    "computeLocalSimilarity(DataObject, CollectionObject , SimilarityValuator): " +
                    "apply weight function...");

            DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMCollectionIsolatedMappingImplExt." +
                    "computeLocalSimilarity(DataObject, CollectionObject , SimilarityValuator): " +
                    "similarity = {}", similarity);

            if (similarity.isValidValue() && similarity.getValue() > maxSimilarity.getValue()) {
                DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMCollectionIsolatedMappingImplExt." +
                        "computeLocalSimilarity(DataObject, CollectionObject , SimilarityValuator): " +
                        "similarity.isValidValue() && similarity.getValue() > maxSimilarity.getValue()");

                DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMCollectionIsolatedMappingImplExt." +
                        "computeLocalSimilarity(DataObject, CollectionObject , SimilarityValuator): " +
                        "maxSimilarity = similarity");

                maxSimilarity = similarity;
            } else {
                DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMCollectionIsolatedMappingImplExt." +
                        "computeLocalSimilarity(DataObject, CollectionObject , SimilarityValuator): " +
                        "!similarity.isValidValue() || similarity.getValue() <= maxSimilarity.getValue()");

                DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMCollectionIsolatedMappingImplExt." +
                        "computeLocalSimilarity(DataObject, CollectionObject , SimilarityValuator): " +
                        "similarity.isValidValue() = {}", similarity.isValidValue());

                DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMCollectionIsolatedMappingImplExt." +
                        "computeLocalSimilarity(DataObject, CollectionObject , SimilarityValuator): " +
                        "similarity.getValue()={}, maxSimilarity.getValue()={}", similarity.getValue(), maxSimilarity.getValue());
            }
        }

        METHOD_CALL.info(
                "procake-extension.extension.similarity.measure.collection.SMCollectionIsolatedMappingImplExt" +
                        ".computeLocalSimilarity(DataObject, CollectionObject, SimilarityValuator): " +
                "return maxSimilarity={}", maxSubstring(maxSimilarity));

        return maxSimilarity;
    }
}
