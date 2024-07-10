package de.uni_trier.wi2.extension.similarity.measure.collection;

import de.uni_trier.wi2.extension.abstraction.IMethodInvokersFunc;
import de.uni_trier.wi2.extension.abstraction.INESTtoList;
import de.uni_trier.wi2.extension.abstraction.ISimilarityMeasureFunc;
import de.uni_trier.wi2.extension.abstraction.IWeightFunc;
import de.uni_trier.wi2.extension.similarity.valuator.SimilarityValuatorImplExt;
import de.uni_trier.wi2.procake.data.model.DataClass;
import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.data.object.base.AggregateObject;
import de.uni_trier.wi2.procake.data.object.base.ListObject;
import de.uni_trier.wi2.procake.data.object.nest.NESTSequentialWorkflowObject;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.SimilarityValuator;
import de.uni_trier.wi2.procake.similarity.base.collection.impl.SMListSWAImpl;
import de.uni_trier.wi2.procake.similarity.impl.SimilarityImpl;
import de.uni_trier.wi2.utils.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import static de.uni_trier.wi2.utils.XEStoSystem.getXESListAsSystemListObject;

/**
 * A similarity measure using the 'Smith-Waterman' algorithm for {@link ListObject}s.
 *
 * <p>For more info on the algorithm <a href="https://wi2.pages.gitlab.rlp.net/procake/procake-wiki/sim/collections/#smith-waterman-algorithm-swa">click here</a>.
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
public class SMListSWAImplExt extends SMListSWAImpl implements SMListSWAExt, INESTtoList, ISimilarityMeasureFunc, IWeightFunc, IMethodInvokersFunc {

    protected SimilarityMeasureFunc similarityMeasureFunc = (a, b) -> null;
    protected MethodInvokersFunc methodInvokersFunc = (a, b) -> new ArrayList<MethodInvoker>();
    protected WeightFunc weightFunc = (a) -> 1;


    @Override
    public void setLocalSimilarityToUse(String similarityToUse) {

        super.setLocalSimilarityToUse(similarityToUse);
        similarityMeasureFunc = (a, b) -> similarityToUse;
    }

    @Override
    public SimilarityMeasureFunc getSimilarityMeasureFunc() {


        return similarityMeasureFunc;
    }

    @Override
    public void setSimilarityMeasureFunc(SimilarityMeasureFunc similarityMeasureFunc) {

        this.similarityMeasureFunc = similarityMeasureFunc;
    }

    @Override
    public MethodInvokersFunc getMethodInvokersFunc() {


        return methodInvokersFunc;
    }

    @Override
    public void setMethodInvokersFunc(MethodInvokersFunc methodInvokersFunc) {

        this.methodInvokersFunc = methodInvokersFunc;
    }

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


        return SMListSWAExt.NAME;
    }

    @Override
    public boolean isSimilarityFor(DataClass dataclass, String orderName) {
        if (XEStoSystem.isXESListClass(dataclass)) return true;
        if (dataclass.isNESTSequentialWorkflow()) return true;
        return super.isSimilarityFor(dataclass, orderName);
    }

    @Override
    public Similarity compute(DataObject queryObject, DataObject caseObject, SimilarityValuator valuator) {


        Similarity similarity = new SimilarityImpl(this, queryObject, caseObject, computeSimilarityValue(queryObject, caseObject, valuator));


        return similarity;

    }

    protected double computeSimilarityValue(DataObject queryObject, DataObject caseObject, SimilarityValuator valuator) {


        //prepare new arrays containing initial null-elements
        DataObject[] queryList, caseList;

        if (XEStoSystem.isXESListClass(queryObject.getDataClass()))
            queryList = getXESListAsSystemListObject((AggregateObject) queryObject).getCollection().toArray(new DataObject[0]);
        else if (queryObject.isNESTSequentialWorkflow())
            queryList = toList((NESTSequentialWorkflowObject) queryObject).getCollection().toArray(new DataObject[0]);
        else queryList = ((ListObject) queryObject).getValues().toArray(new DataObject[0]);

        if (XEStoSystem.isXESListClass(caseObject.getDataClass()))
            caseList = getXESListAsSystemListObject((AggregateObject) caseObject).getCollection().toArray(new DataObject[0]);
        else if (caseObject.isNESTSequentialWorkflow())
            caseList = toList((NESTSequentialWorkflowObject) caseObject).getCollection().toArray(new DataObject[0]);
        else caseList = ((ListObject) caseObject).getValues().toArray(new DataObject[0]);


        DataObject[] queryArray = new DataObject[queryList.length + 1];
        DataObject[] caseArray = new DataObject[caseList.length + 1];

        queryArray[0] = null;
        caseArray[0] = null;

        System.arraycopy(queryList, 0, queryArray, 1, queryList.length);
        System.arraycopy(caseList, 0, caseArray, 1, caseList.length);


        // - initializing the matrices -

        //the actual computation matrix
        double[][] matrix = new double[caseArray.length][queryArray.length];

        //the matrix keeping track of the origin cells
        int[][][] origins = new int[caseArray.length][queryArray.length][2];

        for (int j = 0; j < queryArray.length; j++) {
            matrix[0][j] = 0;
            origins[0][j][0] = 0;
            origins[0][j][1] = j - 1;
        }
        for (int i = 1; i < caseArray.length; i++) {
            matrix[i][0] = 0;
            origins[i][0][0] = i - 1;
            origins[i][0][1] = 0;
        }

        //compute matrix values and find maximum
        int maxCell_i = 0;
        int maxCell_j = 0;

        double denominator = 0;

        double wTempDenominator = 1;

        if (halvingDistancePercentage > 0) {
            for (int j = 1; j < queryArray.length; j++) {
                wTempDenominator += weightFunc.apply(queryArray[j]);
            }
        }

        for (int j = 1; j < queryArray.length; j++) {

            double weight = getWeightFunc().apply(queryArray[j]);
            double wTemp = 1;
            if (getHalvingDistancePercentage() > 0) {
                wTempDenominator -= weight;
                wTemp = getHalvingDistancePercentage() / (2 * wTempDenominator);
            }

            denominator += weight * wTemp;

            for (int i = 1; i < caseArray.length; i++) {
                String localSimilarityMeasure = getSimilarityMeasureFunc().apply(queryArray[j], caseArray[i]);
                if (localSimilarityMeasure == null)
                    localSimilarityMeasure = valuator.getSimilarityMeasure(queryArray[j], caseArray[i]).getSystemName();

                Similarity similarity;

                if (valuator instanceof SimilarityValuatorImplExt) {
                    try {
                        similarity = ((SimilarityValuatorImplExt) valuator).computeSimilarity(queryArray[j], caseArray[i], localSimilarityMeasure, getMethodInvokersFunc().apply(queryArray[j], caseArray[i]));
                    } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                        similarity = valuator.computeSimilarity(queryArray[j], caseArray[i], localSimilarityMeasure);
                    }
                } else similarity = valuator.computeSimilarity(queryArray[j], caseArray[i], localSimilarityMeasure);

                double diagonal = matrix[i - 1][j - 1] + wTemp * similarity.getValue() * weight;
                double horizontal = matrix[i][j - 1] + wTemp * getInsertionScheme().apply(caseArray[i]) * weight;
                double vertical = matrix[i - 1][j] + wTemp * getDeletionScheme().apply(queryArray[j]) * weight;

                if (diagonal >= horizontal && diagonal >= vertical) {

                    origins[i][j][0] = i - 1;
                    origins[i][j][1] = j - 1;

                    matrix[i][j] = diagonal;

                } else if (horizontal > diagonal && horizontal >= vertical) {

                    origins[i][j][0] = i;
                    origins[i][j][1] = j - 1;

                    matrix[i][j] = horizontal;

                } else if (vertical > diagonal && vertical >= horizontal) {

                    origins[i][j][0] = i - 1;
                    origins[i][j][1] = j;

                    matrix[i][j] = vertical;

                }

                if (matrix[i][j] >= matrix[maxCell_i][maxCell_j]
                        && (!getForceAlignmentEndsWithQuery() || j == queryArray.length - 1)) {
                    maxCell_i = i;
                    maxCell_j = j;
                }

            }
        }

        double maxValue = matrix[maxCell_i][maxCell_j];

        return maxValue / denominator;
    }

}
