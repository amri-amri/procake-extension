package extension.similarity.measure;

import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.data.object.base.ListObject;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.SimilarityValuator;
import de.uni_trier.wi2.procake.similarity.base.collection.SMListCorrectness;
import de.uni_trier.wi2.procake.similarity.base.collection.impl.SMListCorrectnessImpl;
import de.uni_trier.wi2.procake.similarity.impl.SimilarityImpl;
import extension.abstraction.IWeightFunc;
import utils.WeightFunc;

public class SMListCorrectnessImplExt extends SMListCorrectnessImpl implements SMListCorrectnessExt, IWeightFunc {

    protected WeightFunc weightFunc = (a) -> 1;

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
        return SMListCorrectnessExt.NAME;
    }


    private double discordantParameter = DEFAULT_DISCORDANT_PARAMETER;


    @Override
    public Similarity compute(DataObject queryObject, DataObject caseObject, SimilarityValuator valuator) {

        // cast query and case object as list objects
        ListObject queryList = (ListObject) queryObject;
        ListObject caseList = (ListObject) caseObject;

        // if the lists have different sizes, return invalid similarity
        if (queryList.size() != caseList.size()) {
            return new SimilarityImpl(this, queryObject, caseObject);
        }

        // check if case or query are empty
        Similarity similarity = checkStoppingCriteria(queryList, caseList);
        if (similarity != null) {
            return similarity;
        }

        int countConcordant = 0;
        int countDiscordant = 0;
        double sumConcordant = 0;
        double sumDiscordant = 0;

        for (int indexAStar1 = 0; indexAStar1 < queryList.size() - 1; indexAStar1++) {
            DataObject aStarResult1 = queryList.elementAt(indexAStar1);
            double weight1 = getWeightFunc().apply(aStarResult1);

            for (int indexAStar2 = indexAStar1 + 1; indexAStar2 < queryList.size(); indexAStar2++) {
                DataObject aStarResult2 = queryList.elementAt(indexAStar2);
                double weight2 = getWeightFunc().apply(aStarResult2);

                // get indices to compare
                int indexCompare1 = -1;
                int indexCompare2 = -1;
                for (DataObject compareResult : caseList.getValues()) {

                    if (compareResult.hasSameValueAsIn(aStarResult1)) {
                        indexCompare1 = caseList.indexOf(compareResult);
                    } else if (compareResult.hasSameValueAsIn(aStarResult2)) {
                        indexCompare2 = caseList.indexOf(compareResult);
                    }
                }

                if (indexCompare1 == -1 || indexCompare2 == -1) {
                    // cases not contained at all
                    continue;
                } else if ((indexAStar1 < indexAStar2 && indexCompare1 < indexCompare2)
                        || (indexAStar1 > indexAStar2 && indexCompare1 > indexCompare2)) {
                    // concordant
                    countConcordant++;
                    sumConcordant += weight1 * weight2;
                } else {
                    // discordant
                    countDiscordant++;
                    sumDiscordant += weight1 * weight2;
                }
            }
        }

        // if different elements occur in query and case, return invalid similarity
        // (check, if there are more elements in the list than concordant and discordant pairs)
        if (queryList.size() > countConcordant + countDiscordant) {
            return new SimilarityImpl(this, queryObject, caseObject);
        }

        // compute correctness
        double correctness = (sumConcordant - sumDiscordant) / (double) (sumConcordant + sumDiscordant);

        // if correctness >= 0, return computed value as similarity
        if (correctness >= 0) {
            return new SimilarityImpl(this, queryObject, caseObject, correctness);
        }

        // otherwise, use discordant parameter to normalize value and return similarity
        return new SimilarityImpl(this, queryObject, caseObject, Math.abs(correctness) * discordantParameter);
    }

}
