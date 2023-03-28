package extension;

import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.data.object.base.CollectionObject;
import de.uni_trier.wi2.procake.data.objectpool.DataObjectIterator;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.SimilarityValuator;
import de.uni_trier.wi2.procake.similarity.base.collection.SMCollectionIsolatedMapping;
import de.uni_trier.wi2.procake.similarity.base.collection.impl.SMCollectionIsolatedMappingImpl;
import de.uni_trier.wi2.procake.similarity.impl.SimilarityImpl;
import utils.SimFunc;
import utils.WeightFunc;

import java.util.ArrayList;

public class SMCollectionIsolatedMappingImplExt extends SMCollectionIsolatedMappingImpl implements SMCollectionIsolatedMapping, ISimFunc, IWeightFunc {

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






    @Override
    public Similarity compute(
            DataObject queryObject, DataObject caseObject, SimilarityValuator valuator) {

        Similarity similarity = checkStoppingCriteria(queryObject, caseObject);
        if (similarity != null) {
            return similarity;
        }

        double similaritySum = 0.0;
        ArrayList<Similarity> localSimilarities = new ArrayList<>();
        DataObjectIterator queryElementIterator = ((CollectionObject) queryObject).iterator();

        double divisor = 0.0;

        // Iterate through all elements of the query collection and find the best possible mapping with
        // the highest possible similarity. Elements from the query may be mapped multiple times to
        // elements from the case.
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
    protected Similarity computeLocalSimilarity(
            DataObject queryElement, CollectionObject caseCollection, SimilarityValuator valuator) {

        String localSimToUse = similarityToUseFunc.apply(queryElement, null);
        double localWeightToUse = weightFunc.apply(queryElement);
        Similarity maxSim = valuator.computeSimilarity(queryElement, null, localSimToUse);
        maxSim = new SimilarityImpl(valuator.getSimilarityModel().getSimilarityMeasure(queryElement.getDataClass(), localSimToUse), queryElement, null, maxSim.getValue()*localWeightToUse, (ArrayList<Similarity>) maxSim.getLocalSimilarities(), maxSim.getInfo());

        DataObjectIterator caseElements = caseCollection.iterator();

        while (caseElements.hasNext()) {

            DataObject caseElement = caseElements.nextDataObject();

            localSimToUse = similarityToUseFunc.apply(queryElement, caseElement);

            Similarity sim = valuator.computeSimilarity(queryElement, caseElement, localSimToUse);
            sim = new SimilarityImpl(valuator.getSimilarityModel().getSimilarityMeasure(queryElement.getDataClass(), localSimToUse), queryElement, caseElement, sim.getValue()*localWeightToUse, (ArrayList<Similarity>) sim.getLocalSimilarities(), sim.getInfo());
            if (sim.isValidValue() && sim.getValue() > maxSim.getValue()) {
                maxSim = sim;
            }
        }
        return maxSim;
    }



}
