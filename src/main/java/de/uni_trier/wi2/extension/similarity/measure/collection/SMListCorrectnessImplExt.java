package de.uni_trier.wi2.extension.similarity.measure.collection;

import de.uni_trier.wi2.extension.abstraction.INESTtoList;
import de.uni_trier.wi2.extension.abstraction.IWeightFunc;
import de.uni_trier.wi2.procake.data.model.DataClass;
import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.data.object.base.AggregateObject;
import de.uni_trier.wi2.procake.data.object.base.ListObject;
import de.uni_trier.wi2.procake.data.object.nest.NESTSequentialWorkflowObject;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.SimilarityValuator;
import de.uni_trier.wi2.procake.similarity.base.collection.impl.SMListCorrectnessImpl;
import de.uni_trier.wi2.procake.similarity.impl.SimilarityImpl;
import de.uni_trier.wi2.utils.WeightFunc;
import de.uni_trier.wi2.utils.XEStoSystem;


import static de.uni_trier.wi2.utils.XEStoSystem.getXESListAsSystemListObject;

/**
 * A similarity measure using the 'List Correctness' algorithm for {@link ListObject}s.
 *
 * <p>For more info on the algorithm <a href="https://wi2.pages.gitlab.rlp.net/procake/procake-wiki/sim/collections/#dynamic-time-warping-dtw">click here</a>.
 *
 * <p>For every data object occurring in the query or case list, a weight can be defined by using
 * a functional interface ({@link WeightFunc}).
 *
 * <p>With
 * c being the sum of all w(a)*w(b) with (a,b) being a concordant pair and
 * d being the sum of all w(a)*w(b) with (a,b) being a discordant pair
 * the correctness is ( c - d ) / ( c + d ).
 */
public class SMListCorrectnessImplExt extends SMListCorrectnessImpl implements SMListCorrectnessExt, INESTtoList, IWeightFunc {

    protected WeightFunc weightFunc = (a) -> 1;
    private double discordantParameter = DEFAULT_DISCORDANT_PARAMETER;

    @Override
    public WeightFunc getWeightFunc() {
        
        
        return weightFunc;
    }

    @Override
    public void setWeightFunc(WeightFunc weightFunc) {
        
        this.weightFunc = (q) -> {
            Double weight = weightFunc.apply(q);
            if (weight == null) return 1;
            if (weight < 0) return 0;
            if (weight > 1) return 1;
            return weight;
        };
    }

    public String getSystemName() {
        
        
        return SMListCorrectnessExt.NAME;
    }

    @Override
    public boolean isSimilarityFor(DataClass dataclass, String orderName) {
        if (XEStoSystem.isXESListClass(dataclass)) return true;
        if (dataclass.isNESTSequentialWorkflow()) return true;
        return super.isSimilarityFor(dataclass, orderName);
    }

    @Override
    public Similarity compute(DataObject queryObject, DataObject caseObject, SimilarityValuator valuator) {
        

        // cast query and case object as list objects
        ListObject queryList, caseList;

        if (XEStoSystem.isXESListClass(queryObject.getDataClass()))
            queryList = getXESListAsSystemListObject((AggregateObject) queryObject);
        else if (queryObject.isNESTSequentialWorkflow()) queryList = toList((NESTSequentialWorkflowObject) queryObject);
        else queryList = (ListObject) queryObject;

        if (XEStoSystem.isXESListClass(caseObject.getDataClass()))
            caseList = getXESListAsSystemListObject((AggregateObject) queryObject);
        else if (caseObject.isNESTSequentialWorkflow()) caseList = toList((NESTSequentialWorkflowObject) caseObject);
        else caseList = (ListObject) caseObject;

        

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
                } else if ((indexAStar1 < indexAStar2 && indexCompare1 < indexCompare2) || (indexAStar1 > indexAStar2 && indexCompare1 > indexCompare2)) {
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
        double correctness = (sumConcordant - sumDiscordant) / (sumConcordant + sumDiscordant);

        // if correctness >= 0, return computed value as similarity
        if (correctness >= 0) {
            
            
            return new SimilarityImpl(this, queryObject, caseObject, correctness);
        }

        
        

        // otherwise, use discordant parameter to normalize value and return similarity
        return new SimilarityImpl(this, queryObject, caseObject, Math.abs(correctness) * discordantParameter);
    }

    public void setDiscordantParameter(double discordantParameter) {
        
        if (discordantParameter > 1.0) {
            this.discordantParameter = 1.0;
        } else if (discordantParameter < 0.0) {
            this.discordantParameter = 0.0;
        } else {
            this.discordantParameter = discordantParameter;
        }
        
    }

}
