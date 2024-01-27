package de.uni_trier.wi2.extension.similarity.measure.collection;

import de.uni_trier.wi2.extension.abstraction.INESTtoList;
import de.uni_trier.wi2.extension.abstraction.IWeightFunc;
import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.data.object.base.AggregateObject;
import de.uni_trier.wi2.procake.data.object.base.ListObject;
import de.uni_trier.wi2.procake.data.object.nest.NESTSequentialWorkflowObject;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.SimilarityValuator;
import de.uni_trier.wi2.procake.similarity.base.collection.impl.SMListCorrectnessImpl;
import de.uni_trier.wi2.procake.similarity.impl.SimilarityImpl;
import de.uni_trier.wi2.utils.WeightFunc;

import static de.uni_trier.wi2.ProcakeExtensionLoggingUtils.*;
import static de.uni_trier.wi2.extension.abstraction.XESBaseToSystemClass.getXESAggregateAttributesAsSystemCollectionObject;

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
        METHOD_CALL.info("public WeightFunc procake-extension.extension.similarity.measure.collection.SMListCorrectnessImplExt.getWeightFunc()...");
        METHOD_CALL.info("procake-extension.extension.similarity.measure.collection.SMListCorrectnessImplExt.getWeightFunc(): return {}", weightFunc);
        return weightFunc;
    }

    @Override
    public void setWeightFunc(WeightFunc weightFunc) {
        METHOD_CALL.info("public void procake-extension.extension.similarity.measure.collection.SMListCorrectnessImplExt.setWeightFunc(WeightFunc weightFunc={})...", weightFunc);
        this.weightFunc = (q) -> {
            Double weight = weightFunc.apply(q);
            if (weight == null) return 1;
            if (weight < 0) return 0;
            if (weight > 1) return 1;
            return weight;
        };
    }

    public String getSystemName() {
        METHOD_CALL.info("public String procake-extension.extension.similarity.measure.collection.SMListCorrectnessImplExt.getSystemName()...");
        METHOD_CALL.info("procake-extension.extension.similarity.measure.collection.SMListCorrectnessImplExt.getSystemName(): return {}", SMListCorrectnessExt.NAME);
        return SMListCorrectnessExt.NAME;
    }

    @Override
    public Similarity compute(DataObject queryObject, DataObject caseObject, SimilarityValuator valuator) {
        METHOD_CALL.info("public Similarity procake-extension.extension.similarity.measure.collection.SMListCorrectnessImplExt.compute(DataObject queryObject={}, DataObject caseObject={}, SimilarityValuator valuator={})...",
                maxSubstring(queryObject), maxSubstring(caseObject), maxSubstring(valuator));

        // cast query and case object as list objects
        ListObject queryList, caseList;

        if (queryObject.getDataClass().isSubclassOf(queryObject.getModel().getClass("XESListClass")))
            queryList = (ListObject) getXESAggregateAttributesAsSystemCollectionObject((AggregateObject) queryObject);
        else if (queryObject.isNESTSequentialWorkflow()) queryList = toList((NESTSequentialWorkflowObject) queryObject);
        else queryList = (ListObject) queryObject;

        if (caseObject.getDataClass().isSubclassOf(caseObject.getModel().getClass("XESListClass")))
            caseList = (ListObject) getXESAggregateAttributesAsSystemCollectionObject((AggregateObject) caseObject);
        else if (caseObject.isNESTSequentialWorkflow()) caseList = toList((NESTSequentialWorkflowObject) caseObject);
        else caseList = (ListObject) caseObject;

        DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListCorrectnessImplExt.compute(DataObject, DataObject, SimilarityValuator): queryList={}, caseList={}", maxSubstring(queryList), maxSubstring(caseList));

        // if the lists have different sizes, return invalid similarity
        if (queryList.size() != caseList.size()) {
            METHOD_CALL.info("procake-extension.extension.similarity.measure.collection.SMListCorrectnessImplExt.compute(DataObject, DataObject, SimilarityValuator): queryList.size() != caseList.size(), return {}", maxSubstring(new SimilarityImpl(this, queryObject, caseObject)));
            return new SimilarityImpl(this, queryObject, caseObject);
        }

        DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListCorrectnessImplExt.compute(DataObject, DataObject, SimilarityValuator): Similarity similarity = de.uni_trier.wi2.procake.similarity.base.collection.impl.SMCollectionImpl.checkStoppingCriteria(queryList, caseList);");
        // check if case or query are empty
        Similarity similarity = checkStoppingCriteria(queryList, caseList);
        if (similarity != null) {
            METHOD_CALL.info("procake-extension.extension.similarity.measure.collection.SMListCorrectnessImplExt.compute(DataObject, DataObject, SimilarityValuator): return {}", maxSubstring(similarity));
            return similarity;
        }

        int countConcordant = 0;
        int countDiscordant = 0;
        double sumConcordant = 0;
        double sumDiscordant = 0;

        for (int indexAStar1 = 0; indexAStar1 < queryList.size() - 1; indexAStar1++) {
            DataObject aStarResult1 = queryList.elementAt(indexAStar1);
            double weight1 = getWeightFunc().apply(aStarResult1);

            DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListCorrectnessImplExt.compute(DataObject, DataObject, SimilarityValuator): aStarResult1={}", maxSubstring(aStarResult1));
            DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListCorrectnessImplExt.compute(DataObject, DataObject, SimilarityValuator): weight1={}", weight1);

            for (int indexAStar2 = indexAStar1 + 1; indexAStar2 < queryList.size(); indexAStar2++) {
                DataObject aStarResult2 = queryList.elementAt(indexAStar2);
                double weight2 = getWeightFunc().apply(aStarResult2);

                DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListCorrectnessImplExt.compute(DataObject, DataObject, SimilarityValuator): aStarResult2={}", maxSubstring(aStarResult2));
                DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListCorrectnessImplExt.compute(DataObject, DataObject, SimilarityValuator): weight2={}", weight2);

                DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListCorrectnessImplExt.compute(DataObject, DataObject, SimilarityValuator): caseList={}", maxSubstring(caseList.getValues()));

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

                DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListCorrectnessImplExt.compute" +
                                "(DataObject, DataObject, SimilarityValuator): " +
                                "indexCompare1={}, indexCompare2={}, countConcordant={}, sumConcordant={}, countDiscordant={}, sumDiscordant={}",
                        indexCompare1, indexCompare2, countConcordant, sumConcordant, countDiscordant, sumDiscordant);

            }
        }

        // if different elements occur in query and case, return invalid similarity
        // (check, if there are more elements in the list than concordant and discordant pairs)
        if (queryList.size() > countConcordant + countDiscordant) {
            DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListCorrectnessImplExt.compute(DataObject, DataObject, SimilarityValuator): different elements occur in query and case");
            METHOD_CALL.info("procake-extension.extension.similarity.measure.collection.SMListCorrectnessImplExt.compute(DataObject, DataObject, SimilarityValuator): return {}", maxSubstring(new SimilarityImpl(this, queryObject, caseObject)));
            return new SimilarityImpl(this, queryObject, caseObject);
        }

        // compute correctness
        double correctness = (sumConcordant - sumDiscordant) / (sumConcordant + sumDiscordant);

        // if correctness >= 0, return computed value as similarity
        if (correctness >= 0) {
            DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListCorrectnessImplExt.compute(DataObject, DataObject, SimilarityValuator): correctness >= 0");
            METHOD_CALL.info("procake-extension.extension.similarity.measure.collection.SMListCorrectnessImplExt.compute(DataObject, DataObject, SimilarityValuator): return {}", maxSubstring(new SimilarityImpl(this, queryObject, caseObject, correctness)));
            return new SimilarityImpl(this, queryObject, caseObject, correctness);
        }

        DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListCorrectnessImplExt.compute(DataObject, DataObject, SimilarityValuator): correctness < 0");
        METHOD_CALL.info("procake-extension.extension.similarity.measure.collection.SMListCorrectnessImplExt.compute(DataObject, DataObject, SimilarityValuator): return {}", maxSubstring(new SimilarityImpl(this, queryObject, caseObject, Math.abs(correctness) * discordantParameter)));

        // otherwise, use discordant parameter to normalize value and return similarity
        return new SimilarityImpl(this, queryObject, caseObject, Math.abs(correctness) * discordantParameter);
    }

    public void setDiscordantParameter(double discordantParameter) {
        METHOD_CALL.info("public void procake-extension.extension.similarity.measure.collection.SMListCorrectnessImplExt.setDiscordantParameter(double discordantParameter={})", discordantParameter);
        if (discordantParameter > 1.0) {
            this.discordantParameter = 1.0;
        } else if (discordantParameter < 0.0) {
            this.discordantParameter = 0.0;
        } else {
            this.discordantParameter = discordantParameter;
        }
        DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListCorrectnessImplExt.setDiscordantParameter(double): discordantParameter={}", discordantParameter);
    }

}
