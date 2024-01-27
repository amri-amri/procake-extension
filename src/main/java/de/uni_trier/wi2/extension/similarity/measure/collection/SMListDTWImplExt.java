package de.uni_trier.wi2.extension.similarity.measure.collection;

import de.uni_trier.wi2.extension.abstraction.*;
import de.uni_trier.wi2.extension.similarity.valuator.SimilarityValuatorImplExt;
import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.data.object.base.AggregateObject;
import de.uni_trier.wi2.procake.data.object.base.ListObject;
import de.uni_trier.wi2.procake.data.object.nest.NESTSequentialWorkflowObject;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.SimilarityValuator;
import de.uni_trier.wi2.procake.similarity.base.collection.impl.SMListDTWImpl;
import de.uni_trier.wi2.procake.similarity.impl.SimilarityImpl;
import de.uni_trier.wi2.utils.MethodInvoker;
import de.uni_trier.wi2.utils.MethodInvokersFunc;
import de.uni_trier.wi2.utils.SimilarityMeasureFunc;
import de.uni_trier.wi2.utils.WeightFunc;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;

import static de.uni_trier.wi2.ProcakeExtensionLoggingUtils.*;

/**
 * A similarity measure using the 'Dynamic Time Warping' algorithm for {@link ListObject}s.
 *
 * <p>For more info on the algorithm <a href="https://wi2.pages.gitlab.rlp.net/procake/procake-wiki/sim/collections/#dynamic-time-warping-dtw">click here</a>.
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
 * <p>
 * //todo explanation of weighted normalization
 */
public class SMListDTWImplExt extends SMListDTWImpl implements SMListDTWExt, INESTtoList, ISimilarityMeasureFunc, IWeightFunc, IMethodInvokersFunc {

    protected SimilarityMeasureFunc similarityMeasureFunc = (a, b) -> null;
    protected MethodInvokersFunc methodInvokersFunc = (a, b) -> new ArrayList<MethodInvoker>();
    protected WeightFunc weightFunc = (a) -> 1;
    ArrayList<Similarity> localSimilarities;

    @Override
    public void setLocalSimilarityToUse(String similarityToUse) {
        METHOD_CALL.info("public void procake-extension.extension.similarity.measure.collection.SMListDTWImplExt.setSimilarityToUse(String similarityToUse={})...", similarityToUse);
        super.setLocalSimilarityToUse(similarityToUse);
        similarityMeasureFunc = (a, b) -> similarityToUse;
    }

    @Override
    public SimilarityMeasureFunc getSimilarityMeasureFunc() {
        METHOD_CALL.info("public SimilarityMeasureFunc procake-extension.extension.similarity.measure.collection.SMListDTWImplExt.getSimilarityToUse()...");
        METHOD_CALL.info("procake-extension.extension.similarity.measure.collection.SMListDTWImplExt.getSimilarityToUse(): return {}", similarityMeasureFunc);
        return similarityMeasureFunc;
    }

    @Override
    public void setSimilarityMeasureFunc(SimilarityMeasureFunc similarityMeasureFunc) {
        METHOD_CALL.info("public void procake-extension.extension.similarity.measure.collection.SMListDTWImplExt.setSimilarityMeasureFunc(SimilarityMeasureFunc similarityMeasureFunc={})...", similarityMeasureFunc);
        this.similarityMeasureFunc = similarityMeasureFunc;
    }

    @Override
    public MethodInvokersFunc getMethodInvokersFunc() {
        METHOD_CALL.info("public MethodInvokersFunc procake-extension.extension.similarity.measure.collection.SMListDTWImplExt.getMethodInvokersFunc()...");
        METHOD_CALL.info("procake-extension.extension.similarity.measure.collection.SMListDTWImplExt.getMethodInvokersFunc(): return {}", methodInvokersFunc);
        return methodInvokersFunc;
    }

    @Override
    public void setMethodInvokersFunc(MethodInvokersFunc methodInvokersFunc) {
        METHOD_CALL.info("public void procake-extension.extension.similarity.measure.collection.SMListDTWImplExt.setMethodInvokersFunc(MethodInvokersFunc methodInvokersFunc={})...", methodInvokersFunc);
        this.methodInvokersFunc = methodInvokersFunc;
    }

    @Override
    public WeightFunc getWeightFunc() {
        METHOD_CALL.info("public WeightFunc procake-extension.extension.similarity.measure.collection.SMListDTWImplExt.getWeightFunc()...");
        METHOD_CALL.info("procake-extension.extension.similarity.measure.collection.SMListDTWImplExt.getWeightFunc(): return {}", weightFunc);
        return weightFunc;
    }

    @Override
    public void setWeightFunc(WeightFunc weightFunc) {
        METHOD_CALL.info("public void procake-extension.extension.similarity.measure.collection.SMListDTWImplExt.setWeightFunc(WeightFunc weightFunc={})...", weightFunc);
        this.weightFunc = (q) -> {
            Double weight = weightFunc.apply(q);
            if (weight == null) return 1;
            if (weight < 0) return 0;
            if (weight > 1) return 1;
            return weight;
        };
    }

    public String getSystemName() {
        METHOD_CALL.info("public String procake-extension.extension.similarity.measure.collection.SMListDTWImplExt.getSystemName()...");
        METHOD_CALL.info("procake-extension.extension.similarity.measure.collection.SMListDTWImplExt.getSystemName(): return {}", SMListDTWExt.NAME);
        return SMListDTWExt.NAME;
    }

    @Override
    public Similarity compute(DataObject queryObject, DataObject caseObject, SimilarityValuator valuator) {

        METHOD_CALL.info("public String procake-extension.extension.similarity.measure.collection.SMListDTWImplExt.compute(DataObject queryObject={}, DataObject caseObject={}, SimilarityValuator valuator={})", maxSubstring(queryObject), maxSubstring(caseObject), maxSubstring(valuator));

        localSimilarities = new ArrayList<>();

        Similarity similarity = new SimilarityImpl(this, queryObject, caseObject, computeSimilarityValue(queryObject, caseObject, valuator), localSimilarities);

        METHOD_CALL.info("procake-extension.extension.similarity.measure.collection.SMListDTWImplExt.compute(DataObject, DataObject, SimilarityValuator): return Similarity");

        return similarity;
    }

    protected double computeSimilarityValue(DataObject queryObject, DataObject caseObject, SimilarityValuator valuator) {

        METHOD_CALL.info("public String procake-extension.extension.similarity.measure.collection.SMListDTWImplExt.computeSimilarityValue(DataObject queryObject={}, DataObject caseObject={}, SimilarityValuator valuator={})", maxSubstring(queryObject), maxSubstring(caseObject), maxSubstring(valuator));

        //prepare new arrays containing initial null-elements
        DataObject[] queryList, caseList;

        if (queryObject.getDataClass().isSubclassOf(queryObject.getModel().getClass("XESListClass")))
            queryList = ((ListObject) XESBaseToSystemClass.getXESAggregateAttributesAsSystemCollectionObject((AggregateObject) queryObject)).getValues().toArray(DataObject[]::new);
        else if (queryObject.isNESTSequentialWorkflow())
            queryList = toList((NESTSequentialWorkflowObject) queryObject).getValues().toArray(DataObject[]::new);
        else queryList = ((ListObject) queryObject).getValues().toArray(DataObject[]::new);

        if (caseObject.getDataClass().isSubclassOf(caseObject.getModel().getClass("XESListClass")))
            caseList = ((ListObject) XESBaseToSystemClass.getXESAggregateAttributesAsSystemCollectionObject((AggregateObject) caseObject)).getValues().toArray(DataObject[]::new);
        else if (caseObject.isNESTSequentialWorkflow())
            caseList = toList((NESTSequentialWorkflowObject) caseObject).getValues().toArray(DataObject[]::new);
        else caseList = ((ListObject) caseObject).getValues().toArray(DataObject[]::new);

        DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListDTWImplExt.computeSimilarityValue(DataObject, DataObject, SimilarityValuator): queryList={}, caseList={}", maxSubstring(queryList), maxSubstring(caseList));

        DataObject[] queryArray = new DataObject[queryList.length + 1];
        DataObject[] caseArray = new DataObject[caseList.length + 1];

        queryArray[0] = null;
        caseArray[0] = null;

        System.arraycopy(queryList, 0, queryArray, 1, queryList.length);
        System.arraycopy(caseList, 0, caseArray, 1, caseList.length);


        // - initializing the matrices -

        //the actual computation matrix
        double[][] matrix = new double[caseArray.length][queryArray.length];

        //the matrix for later normalization
        double[][] normalizationMatrix = new double[caseArray.length][queryArray.length];

        //the matrix keeping track of the origin cells
        int[][][] originMatrix = new int[caseArray.length][queryArray.length][2];

        //the matrix keeping track of the local similarities
        Similarity[][] localSimilarityMatrix = new Similarity[caseArray.length][queryArray.length];

        for (int j = 0; j < queryArray.length; j++) {
            matrix[0][j] = 0;
            normalizationMatrix[0][j] = 0;
            originMatrix[0][j][0] = 0;
            originMatrix[0][j][1] = j - 1;
            localSimilarityMatrix[0][j] = new SimilarityImpl(null, queryArray[j], null, 0);
        }
        for (int i = 0; i < caseArray.length; i++) {
            matrix[i][0] = 0;
            normalizationMatrix[i][0] = 0;
            originMatrix[i][0][0] = i - 1;
            originMatrix[i][0][1] = 0;
            localSimilarityMatrix[i][0] = new SimilarityImpl(null, null, caseArray[i], 0);
        }


        originMatrix[0][0][0] = -1;
        originMatrix[0][0][1] = -1;

        //compute matrix values and find maximum
        int maxCell_i = 0;
        int maxCell_j = 0;

        double wTempDenominator = 1;

        if (getHalvingDistancePercentage() > 0) {
            for (int j = 1; j < queryArray.length; j++) {
                wTempDenominator += getWeightFunc().apply(queryArray[j]);
            }
        }
        
        DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListDTWImplExt.computeSimilarityValue(DataObject, DataObject, SimilarityValuator): wTempDenominator={}", wTempDenominator);
        DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListDTWImplExt.computeSimilarityValue(DataObject, DataObject, SimilarityValuator): halvingDistancePercentage={}", getHalvingDistancePercentage());

        DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListDTWImplExt.computeSimilarityValue(DataObject, DataObject, SimilarityValuator): for (int j = 1; j < queryArray.length; j++){...");
        DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListDTWImplExt.computeSimilarityValue(DataObject, DataObject, SimilarityValuator):     for (int i = 1; i < caseArray.length; j++){...");

        for (int j = 1; j < queryArray.length; j++) {

            double weight = getWeightFunc().apply(queryArray[j]);
            double wTemp = 1;
            if (getHalvingDistancePercentage() > 0) {
                wTempDenominator -= weight;
                wTemp = getHalvingDistancePercentage() / (2 * wTempDenominator);
            }

            DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListDTWImplExt.computeSimilarityValue(DataObject, DataObject, SimilarityValuator): queryArray[j={}]={}", j, queryArray[j]);
            DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListDTWImplExt.computeSimilarityValue(DataObject, DataObject, SimilarityValuator): weight={}", weight);
            DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListDTWImplExt.computeSimilarityValue(DataObject, DataObject, SimilarityValuator): wTemp={}", wTemp);
            DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListDTWImplExt.computeSimilarityValue(DataObject, DataObject, SimilarityValuator): wTempDenominator={}", wTempDenominator);

            for (int i = 1; i < caseArray.length; i++) {
                String localSimilarityMeasure = getSimilarityMeasureFunc().apply(queryArray[j], caseArray[i]);
                if (localSimilarityMeasure == null)
                    localSimilarityMeasure = valuator.getSimilarityMeasure(queryArray[j], caseArray[i]).getSystemName();

                DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListDTWImplExt.computeSimilarityValue(DataObject, DataObject, SimilarityValuator): caseArray[i={}]={}", i, caseArray[i]);
                DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListDTWImplExt.computeSimilarityValue(DataObject, DataObject, SimilarityValuator): localSimilarityMeasure={}", maxSubstring(localSimilarityMeasure));
                
                Similarity similarity;

                if (valuator instanceof SimilarityValuatorImplExt) {
                    try {
                        similarity = ((SimilarityValuatorImplExt) valuator).computeSimilarity(queryArray[j], caseArray[i], localSimilarityMeasure, getMethodInvokersFunc().apply(queryArray[j], caseArray[i]));
                    } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                        similarity = valuator.computeSimilarity(queryArray[j], caseArray[i], localSimilarityMeasure);
                    }
                } else similarity = valuator.computeSimilarity(queryArray[j], caseArray[i], localSimilarityMeasure);

                DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListDTWImplExt.computeSimilarityValue(DataObject, DataObject, SimilarityValuator): similarity={}", maxSubstring(similarity));
                DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListDTWImplExt.computeSimilarityValue(DataObject, DataObject, SimilarityValuator): apply weight...");
                
                // apply weight
                similarity = new SimilarityImpl(
                        valuator.getSimilarityModel().getSimilarityMeasure(queryArray[j].getDataClass(), localSimilarityMeasure),
                        queryArray[j],
                        caseArray[i],
                        similarity.getValue() * weight
                );

                DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListDTWImplExt.computeSimilarityValue(DataObject, DataObject, SimilarityValuator): similarity={}", maxSubstring(similarity));
                
                localSimilarityMatrix[i][j] = similarity;

                double diagonal = matrix[i - 1][j - 1] + wTemp * similarity.getValue() * 2;
                double horizontal = matrix[i][j - 1] + wTemp * similarity.getValue();
                double vertical = matrix[i - 1][j] + wTemp * similarity.getValue();
                
                DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListDTWImplExt.computeSimilarityValue(DataObject, DataObject, SimilarityValuator): diagonal={}, horizontal={}, vertical={}", diagonal, horizontal, vertical);

                if (diagonal >= horizontal && diagonal >= vertical) {

                    originMatrix[i][j][0] = i - 1;
                    originMatrix[i][j][1] = j - 1;

                    matrix[i][j] = diagonal;
                    normalizationMatrix[i][j] = normalizationMatrix[i - 1][j - 1] + wTemp * weight * 2;

                } else if (horizontal > diagonal && horizontal >= vertical) {

                    originMatrix[i][j][0] = i;
                    originMatrix[i][j][1] = j - 1;

                    matrix[i][j] = horizontal;
                    normalizationMatrix[i][j] = normalizationMatrix[i][j - 1] + wTemp * weight;

                } else if (vertical > diagonal && vertical >= horizontal) {

                    originMatrix[i][j][0] = i - 1;
                    originMatrix[i][j][1] = j;

                    matrix[i][j] = vertical;
                    normalizationMatrix[i][j] = normalizationMatrix[i - 1][j] + wTemp * weight;

                }
                
                DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListDTWImplExt.computeSimilarityValue(DataObject, DataObject, SimilarityValuator): computation matrix:\n{}", maxSubstring(get2DMatrixString(matrix)));
                DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListDTWImplExt.computeSimilarityValue(DataObject, DataObject, SimilarityValuator): normalization matrix:\n{}", maxSubstring(get2DMatrixString(normalizationMatrix)));
                DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListDTWImplExt.computeSimilarityValue(DataObject, DataObject, SimilarityValuator): origin matrix:\n{}", maxSubstring(get2DMatrixString(originMatrix)));
                DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListDTWImplExt.computeSimilarityValue(DataObject, DataObject, SimilarityValuator): local similarity matrix:\n{}", maxSubstring(get2DMatrixString(localSimilarityMatrix)));

                if (matrix[i][j] >= matrix[maxCell_i][maxCell_j]
                        && (!forceAlignmentEndsWithQuery || j == queryArray.length - 1)) {
                    maxCell_i = i;
                    maxCell_j = j;
                }
                
                DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListDTWImplExt.computeSimilarityValue(DataObject, DataObject, SimilarityValuator): maxCell_i={}, maxCell_j={}", maxCell_i, maxCell_j);

            }
        }


        int origin_i = maxCell_i;
        int origin_j = maxCell_j;
        int h_i;

        while (origin_i + origin_j > 0) {
            localSimilarities.add(localSimilarityMatrix[origin_i][origin_j]);
            h_i = originMatrix[origin_i][origin_j][0];
            origin_j = originMatrix[origin_i][origin_j][1];
            origin_i = h_i;
        }

        Collections.reverse(localSimilarities);

        DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListDTWImplExt.computeSimilarityValue(DataObject, DataObject, SimilarityValuator): localSimilarities={}", localSimilarities);
        
        double maxSimilarityValue = matrix[maxCell_i][maxCell_j];
        double denominator = normalizationMatrix[maxCell_i][maxCell_j];

        DIAGNOSTICS.trace("procake-extension.extension.similarity.measure.collection.SMListDTWImplExt.computeSimilarityValue(DataObject, DataObject, SimilarityValuator): maxSimilarityValue={}, denominator={}", maxSimilarityValue, denominator);
        
        METHOD_CALL.info("procake-extension.extension.similarity.measure.collection.SMListDTWImplExt.computeSimilarityValue(DataObject, DataObject, SimilarityValuator): return maxSimilarityValue / denominator={}", maxSimilarityValue / denominator);

        return maxSimilarityValue / denominator;
    }

}
