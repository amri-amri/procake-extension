package extension.similarity.measure.collection;

import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.data.object.base.AggregateObject;
import de.uni_trier.wi2.procake.data.object.base.ListObject;
import de.uni_trier.wi2.procake.data.object.nest.NESTSequentialWorkflowObject;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.SimilarityValuator;
import de.uni_trier.wi2.procake.similarity.base.collection.impl.SMListSWAImpl;
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

import static extension.abstraction.XESBaseToSystemClass.getXESAggregateAttributesAsSystemCollectionObject;

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
 *
 * //todo explanation of weighted normalization
 */
public class SMListSWAImplExt extends SMListSWAImpl implements SMListSWAExt, INESTtoList, ISimilarityMeasureFunc, IWeightFunc, IMethodInvokersFunc {

    protected SimilarityMeasureFunc similarityToUseFunc;
    protected MethodInvokersFunc methodInvokersFunc = (a, b) -> new ArrayList<MethodInvoker>();
    protected WeightFunc weightFunc = (a) -> 1;

    @Override
    public void setLocalSimilarityToUse(String newValue) {
        super.setLocalSimilarityToUse(newValue);
        similarityToUseFunc = (a, b) -> newValue;
    }

    @Override
    public void setSimilarityMeasureFunc(SimilarityMeasureFunc similarityMeasureFunc){
        similarityToUseFunc = similarityMeasureFunc;
    }

    @Override
    public SimilarityMeasureFunc getSimilarityMeasureFunc() {
        return similarityToUseFunc;
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
        return SMListSWAExt.NAME;
    }



    @Override
    public Similarity compute(DataObject queryObject, DataObject caseObject, SimilarityValuator valuator) {

        if (queryObject.getDataClass().isSubclassOf(queryObject.getModel().getClass("XESBaseClass"))) {
            queryObject = getXESAggregateAttributesAsSystemCollectionObject((AggregateObject) queryObject);
        }

        if (caseObject.getDataClass().isSubclassOf(caseObject.getModel().getClass("XESBaseClass"))) {
            caseObject = getXESAggregateAttributesAsSystemCollectionObject((AggregateObject) caseObject);
        }

        return new SimilarityImpl(this, queryObject, caseObject, computeSimilarityValue(queryObject, caseObject, valuator));

    }

    protected double computeSimilarityValue(DataObject queryObject, DataObject caseObject, SimilarityValuator valuator){


        //prepare new arrays containing initial null-elements
        DataObject[] queryList, caseList;

        if (queryObject.isNESTSequentialWorkflow()) queryList = toList((NESTSequentialWorkflowObject) queryObject).getValues().toArray(DataObject[]::new);
        else queryList = ((ListObject) queryObject).getValues().toArray(DataObject[]::new);

        if (caseObject.isNESTSequentialWorkflow()) caseList = toList((NESTSequentialWorkflowObject) caseObject).getValues().toArray(DataObject[]::new);
        else caseList = ((ListObject) caseObject).getValues().toArray(DataObject[]::new);


        DataObject[] queryArray = new DataObject[queryList.length + 1];
        DataObject[] caseArray = new DataObject[caseList.length + 1];

        queryArray[0] = null;
        caseArray[0] = null;

        for (int n = 0; n < queryList.length; n++)  queryArray[n+1] = queryList[n];
        for (int n = 0; n < caseList.length; n++)   caseArray[n+1]   = caseList[n];


        // - initializing the matrices -

        //the actual computation matrix
        double[][] matrix = new double[caseArray.length][queryArray.length];

        //the matrix keeping track of the origin cells
        int[][][] origins = new int[caseArray.length][queryArray.length][2];

        for (int j = 0; j< queryArray.length; j++){
            matrix[0][j] = 0;
            origins[0][j][0] = 0;
            origins[0][j][1] = j-1;
        }
        for (int i = 1; i< caseArray.length; i++){
            matrix[i][0] = 0;
            origins[i][0][0] = i-1;
            origins[i][0][1] = 0;
        }

        //compute matrix values and find maximum
        int maxCell_i = 0;
        int maxCell_j = 0;

        double denominator = 0;

        double wTempDenominator = 1;

        if (halvingDistancePercentage>0) {
            for (int j = 1; j < queryArray.length; j++) {
                wTempDenominator += weightFunc.apply(queryArray[j]);
            }
        }

        for ( int j = 1; j< queryArray.length; j++ ) {

            double weight = getWeightFunc().apply( queryArray[j] );
            double wTemp = 1;
            if (getHalvingDistancePercentage()>0) {
                wTempDenominator -= weight;
                wTemp = getHalvingDistancePercentage() / (2 * wTempDenominator);
            }

            denominator += weight * wTemp;

            for ( int i = 1; i< caseArray.length; i++ ) {
                String localSimilarityMeasure = getSimilarityMeasureFunc().apply( queryArray[j], caseArray[i] );

                Similarity similarity;

                if (valuator instanceof SimilarityValuatorImplExt) {
                    try {
                        similarity = ((SimilarityValuatorImplExt) valuator).computeSimilarity(queryArray[j], caseArray[i], localSimilarityMeasure, getMethodInvokersFunc().apply(queryArray[j], caseArray[i]));
                    } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                        similarity = valuator.computeSimilarity( queryArray[j], caseArray[i], localSimilarityMeasure );
                    }
                }
                else similarity = valuator.computeSimilarity( queryArray[j], caseArray[i], localSimilarityMeasure );

                double diagonal =   matrix[i-1][j-1]    + wTemp     * similarity.getValue()                         * weight;
                double horizontal = matrix[i][j-1]      + wTemp     * getInsertionScheme().apply(caseArray[i])      * weight;
                double vertical =   matrix[i-1][j]      + wTemp     * getDeletionScheme().apply(queryArray[j])      * weight;

                if (diagonal >= horizontal && diagonal >= vertical) {

                    origins[i][j][0] = i-1;
                    origins[i][j][1] = j-1;

                    matrix[i][j] = diagonal;

                } else if (horizontal > diagonal && horizontal >= vertical) {

                    origins[i][j][0] = i;
                    origins[i][j][1] = j-1;

                    matrix[i][j] = horizontal;

                } else if (vertical > diagonal && vertical >= horizontal) {

                    origins[i][j][0] = i-1;
                    origins[i][j][1] = j;

                    matrix[i][j] = vertical;

                }

                if (matrix[i][j] >= matrix[maxCell_i][maxCell_j]
                    && (!getForceAlignmentEndsWithQuery() || j == queryArray.length-1)) {
                    maxCell_i = i;
                    maxCell_j = j;
                }

            }
        }

        double maxValue = matrix[maxCell_i][maxCell_j];

        return maxValue / denominator;
    }

}
