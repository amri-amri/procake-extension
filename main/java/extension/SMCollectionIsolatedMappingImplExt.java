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

        Similarity similarity = checkStoppingCriteria(queryObject, caseObject);
        if (similarity != null) {
            return similarity;
        }

        double similaritySum = 0.0;
        ArrayList<Similarity> localSimilarities = new ArrayList<>();
        DataObjectIterator queryElementIterator = ((CollectionObject) queryObject).iterator();

        double divisor = 0.0;
        mappings = new ArrayList<>();

        // Iterate through all elements of the query collection and find the best possible mapping with
        // the highest possible similarity. Elements from the query may be mapped multiple times to
        // elements from the case.
        while (queryElementIterator.hasNext()) {
            DataObject queryElement = queryElementIterator.nextDataObject();
            Similarity localSimilarity =
                    this.computeLocalSimilarity(queryElement, ((CollectionObject) caseObject), valuator);
            similaritySum += localSimilarity.getValue();
            localSimilarities.add(localSimilarity);
        }

        for (DataObject[] mapping : mappings) divisor += weightFunc.apply(mapping[0],mapping[1]);


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
        Similarity maxSim = new SimilarityImpl(this, null, null, -1.0);
        DataObjectIterator caseElements = caseCollection.iterator();
        DataObject[] mapping = new DataObject[2];
        mapping[0] = queryElement;
        while (caseElements.hasNext() && maxSim.getValue() < 1.0) {
            DataObject caseElement = caseElements.nextDataObject();
            Similarity sim = valuator.computeSimilarity(queryElement, caseElement, similarityToUseFunc.apply(queryElement, caseElement));
            sim = new SimilarityImpl(this,queryElement,caseElement,sim.getValue()*weightFunc.apply(queryElement,caseElement),(ArrayList<Similarity>) sim.getLocalSimilarities(),sim.getInfo());
            if (sim.isValidValue() && sim.getValue() > maxSim.getValue()) {
                maxSim = sim;
                mapping[1] = caseElement;
            }
        }
        mappings.add(mapping);
        return maxSim;
    }

    ArrayList<DataObject[]> mappings = null;


}
