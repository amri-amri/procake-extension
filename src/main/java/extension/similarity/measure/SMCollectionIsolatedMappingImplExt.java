package extension.similarity.measure;

import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.data.object.base.CollectionObject;
import de.uni_trier.wi2.procake.data.object.nest.NESTSequentialWorkflowObject;
import de.uni_trier.wi2.procake.data.objectpool.DataObjectIterator;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.SimilarityValuator;
import de.uni_trier.wi2.procake.similarity.base.collection.impl.SMCollectionIsolatedMappingImpl;
import de.uni_trier.wi2.procake.similarity.impl.SimilarityImpl;
import extension.abstraction.IMethodInvokersFunc;
import extension.abstraction.INESTtoList;
import extension.abstraction.ISimilarityMeasureFunc;
import extension.abstraction.IWeightFunc;
import extension.similarity.valuator.SimilarityValuatorImplExt;
import utils.MethodInvoker;
import utils.MethodInvokersFunc;
import utils.SimilarityMeasureFunc;
import utils.WeightFunc;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

/**
 * A similarity measure using the 'Isolated Mapping' algorithm for {@link CollectionObject}s.
 *
 * <p>The 'Isolated Mapping' algorithm assigns to each element of the query collection the case element
 * with which the respective query element has the highest (local) weighted similarity.
 *
 * <p>The overall similarity between query and case collection is the sum of the local weighted
 * similarities divided by the sum of the weights.
 *
 * <p>The weight values are depending solely on the characteristics of the query elements and can
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

    protected SimilarityMeasureFunc similarityMeasureFunc;
    protected MethodInvokersFunc methodInvokersFunc = (a, b) -> new ArrayList<>();
    protected WeightFunc weightFunc = (a) -> 1;

    @Override
    public void setSimilarityToUse(String similarityToUse) {
        super.setSimilarityToUse(similarityToUse);
        similarityMeasureFunc = (a, b) -> similarityToUse;
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
    public void setMethodInvokersFunc(MethodInvokersFunc methodInvokersFunc) {
        this.methodInvokersFunc = methodInvokersFunc;
    }

    @Override
    public MethodInvokersFunc getMethodInvokersFunc() {
        return methodInvokersFunc;
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

    public String getSystemName() {
        return SMCollectionIsolatedMappingExt.NAME;
    }


    /**
     * Computes the isolated mapping similarity between a given query- and case- object of type Collection.
     *
     * @param queryObject  the query object of type Collection
     * @param caseObject  the case object of type Collection
     * @param valuator  the similarity valuator used for computation
     * @return similarity object containing local similarities
     */
    @Override
    public Similarity compute(DataObject queryObject, DataObject caseObject, SimilarityValuator valuator) {

        CollectionObject queryCollection, caseCollection;

        if (queryObject.isNESTSequentialWorkflow()) queryCollection = toList((NESTSequentialWorkflowObject) queryObject);
        else queryCollection = (CollectionObject) queryObject;

        if (caseObject.isNESTSequentialWorkflow()) caseCollection = toList((NESTSequentialWorkflowObject) caseObject);
        else caseCollection = (CollectionObject) caseObject;



        Similarity similarity = checkStoppingCriteria(queryCollection, caseCollection);

        if (similarity != null) {
            return similarity;
        }

        double similaritySum = 0.0;
        ArrayList<Similarity> localSimilarities = new ArrayList<>();
        DataObjectIterator queryElementIterator = queryCollection.iterator();

        double divisor = 0.0;

        // Iterate through all elements of the query collection and find the best possible mapping with
        // the highest possible similarity.
        // A case element may be mapped multiple times to different query elements.
        while (queryElementIterator.hasNext()) {
            DataObject queryElement = queryElementIterator.nextDataObject();
            Similarity localSimilarity = this.computeLocalSimilarity(queryElement, caseCollection, valuator);
            similaritySum += localSimilarity.getValue();
            divisor += getWeightFunc().apply(queryElement);
            localSimilarities.add(localSimilarity);
        }

        return new SimilarityImpl(
                this,
                queryObject,
                caseObject,
                similaritySum / divisor,
                localSimilarities);
    }

    @Override
    protected Similarity computeLocalSimilarity(DataObject queryElement, CollectionObject caseCollection, SimilarityValuator valuator) {

        String localSimilarityMeasure;
        double weight = getWeightFunc().apply(queryElement);
        Similarity maxSimilarity = new SimilarityImpl(null, queryElement, null, 0.0);

        DataObjectIterator caseElementIterator = caseCollection.iterator();

        while (caseElementIterator.hasNext()) {

            DataObject caseElement = caseElementIterator.nextDataObject();

            localSimilarityMeasure = getSimilarityMeasureFunc().apply(queryElement, caseElement);

            Similarity similarity;

            if (valuator instanceof SimilarityValuatorImplExt) {
                try {
                    similarity = ((SimilarityValuatorImplExt) valuator).computeSimilarity(queryElement, caseElement, localSimilarityMeasure, methodInvokersFunc.apply(queryElement, caseElement));
                } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                    similarity = valuator.computeSimilarity(queryElement, caseElement, localSimilarityMeasure);
                }
            }
            else similarity = valuator.computeSimilarity(queryElement, caseElement, localSimilarityMeasure);

            //Application of the weight function
            similarity = new SimilarityImpl(valuator.getSimilarityModel().getSimilarityMeasure(queryElement.getDataClass(), localSimilarityMeasure), queryElement, caseElement, similarity.getValue()*weight, (ArrayList<Similarity>) similarity.getLocalSimilarities(), similarity.getInfo());

            if (similarity.isValidValue() && similarity.getValue() > maxSimilarity.getValue()) {
                maxSimilarity = similarity;
            }
        }
        return maxSimilarity;
    }
}
