package extension.similarity.measure;

import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.data.object.base.CollectionObject;
import de.uni_trier.wi2.procake.data.objectpool.DataObjectIterator;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.SimilarityValuator;
import de.uni_trier.wi2.procake.similarity.base.collection.SMCollectionIsolatedMapping;
import de.uni_trier.wi2.procake.similarity.base.collection.impl.SMCollectionIsolatedMappingImpl;
import de.uni_trier.wi2.procake.similarity.impl.SimilarityImpl;
import extension.abstraction.IMethodInvokersFunc;
import extension.abstraction.ISimilarityMeasureFunc;
import extension.abstraction.IWeightFunc;
import extension.similarity.valuator.SimilarityValuatorImplExt;
import utils.MethodInvoker;
import utils.MethodInvokersFunc;
import utils.SimilarityMeasureFunc;
import utils.WeightFunc;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class SMCollectionIsolatedMappingImplExt extends SMCollectionIsolatedMappingImpl implements SMCollectionIsolatedMapping, ISimilarityMeasureFunc, IWeightFunc, IMethodInvokersFunc {

    protected SimilarityMeasureFunc similarityMeasureFunc;
    protected MethodInvokersFunc methodInvokersFunc = (a, b) -> new ArrayList<MethodInvoker>();
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



    @Override
    public Similarity compute(DataObject queryObject, DataObject caseObject, SimilarityValuator valuator) {

        Similarity similarity = checkStoppingCriteria(queryObject, caseObject);

        if (similarity != null) {
            return similarity;
        }

        double similaritySum = 0.0;
        ArrayList<Similarity> localSimilarities = new ArrayList<>();
        DataObjectIterator queryElementIterator = ((CollectionObject) queryObject).iterator();

        double divisor = 0.0;

        // Iterate through all elements of the query collection and find the best possible mapping with
        // the highest possible similarity.
        // A case element may be mapped multiple times to different query elements.
        while (queryElementIterator.hasNext()) {
            DataObject queryElement = queryElementIterator.nextDataObject();
            Similarity localSimilarity = this.computeLocalSimilarity(queryElement, (CollectionObject) caseObject, valuator);
            similaritySum += localSimilarity.getValue();
            divisor += weightFunc.apply(queryElement);
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

            localSimilarityMeasure = similarityMeasureFunc.apply(queryElement, caseElement);

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
