package extension.similarity.measure;

import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.data.object.base.ListObject;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.SimilarityValuator;
import de.uni_trier.wi2.procake.similarity.base.collection.SMListDTW;
import de.uni_trier.wi2.procake.similarity.base.collection.impl.SMListDTWImpl;
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

public class SMListDTWImplExt extends SMListDTWImpl implements SMListDTWExt, ISimilarityMeasureFunc, IWeightFunc, IMethodInvokersFunc {

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
        return SMListDTWExt.NAME;
    }



    @Override
    public Similarity compute(DataObject queryObject, DataObject caseObject, SimilarityValuator valuator) {

        return new SimilarityImpl(this, queryObject, caseObject, computeSimilarityValue( queryObject, caseObject, valuator));

    }

    protected double computeSimilarityValue(DataObject queryObject, DataObject caseObject, SimilarityValuator valuator){

        //prepare new arrays containing initial null-elements
        DataObject[] queryList = ((ListObject) queryObject).getValues().toArray(DataObject[]::new);
        DataObject[] caseList = ((ListObject) caseObject).getValues().toArray(DataObject[]::new);

        DataObject[] queryArray = new DataObject[queryList.length + 1];
        DataObject[] caseArray = new DataObject[caseList.length + 1];

        queryArray[0] = null;
        caseArray[0] = null;

        for (int n = 0; n < queryList.length; n++)  queryArray[n+1] = queryList[n];
        for (int n = 0; n < caseList.length; n++)   caseArray[n+1]   = caseList[n];


        // - initializing the matrices -

        //the actual computation matrix
        double[][] matrix = new double[caseArray.length][queryArray.length];

        //the matrix for later normalization
        double[][] normalizingMatrix = new double[caseArray.length][queryArray.length];

        //the matrix keeping track of the origin cells
        int[][][] originMatrix = new int[caseArray.length][queryArray.length][2];

        for (int j = 0; j< queryArray.length; j++){
            matrix[0][j] = 0;
            normalizingMatrix[0][j] = 0;
            originMatrix[0][j][0] = 0;
            originMatrix[0][j][1] = j-1;
        }
        for (int i = 1; i< caseArray.length; i++){
            matrix[i][0] = 0;
            normalizingMatrix[i][0] = 0;
            originMatrix[i][0][0] = i-1;
            originMatrix[i][0][1] = 0;
        }

        //compute matrix values and find maximum
        int maxCell_i = 0;
        int maxCell_j = 0;

        double wTempDenominator = 1;

        if (halvingDistancePercentage>0) {
            for (int j = 1; j < queryArray.length; j++) {
                wTempDenominator += getWeightFunc().apply(queryArray[j]);
            }
        }

        for ( int j = 1; j< queryArray.length; j++ ) {

            double weight = getWeightFunc().apply( queryArray[j] );
            double wTemp = 1;
            if (getHalvingDistancePercentage()>0) {
                wTempDenominator -= weight;
                wTemp = getHalvingDistancePercentage() / (2 * wTempDenominator);
            }

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

                double diagonal     =   matrix[i-1][j-1]    + wTemp     * similarity.getValue()    * weight * 2;
                double horizontal   =   matrix[i][j-1]      + wTemp     * similarity.getValue()    * weight;
                double vertical     =   matrix[i-1][j]      + wTemp     * similarity.getValue()    * weight;

                if (diagonal >= horizontal && diagonal >= vertical) {

                    originMatrix[i][j][0] = i-1;
                    originMatrix[i][j][1] = j-1;

                    matrix[i][j] = diagonal;
                    normalizingMatrix[i][j] = normalizingMatrix[i-1][j-1]   + wTemp     * weight * 2;

                }
                else if (horizontal > diagonal && horizontal >= vertical) {

                    originMatrix[i][j][0] = i;
                    originMatrix[i][j][1] = j-1;

                    matrix[i][j] = horizontal;
                    normalizingMatrix[i][j] = normalizingMatrix[i][j-1]     + wTemp     * weight;

                }
                else if (vertical > diagonal && vertical >= horizontal) {

                    originMatrix[i][j][0] = i-1;
                    originMatrix[i][j][1] = j;

                    matrix[i][j] = vertical;
                    normalizingMatrix[i][j] = normalizingMatrix[i-1][j]     + wTemp     * weight;

                }

                if (matrix[i][j] >= matrix[maxCell_i][maxCell_j]
                        && (!forceAlignmentEndsWithQuery || j == queryArray.length-1)) {
                    maxCell_i = i;
                    maxCell_j = j;
                }

            }
        }



        double maxSimilarityValue = matrix[maxCell_i][maxCell_j];
        double denominator = normalizingMatrix[maxCell_i][maxCell_j];

        return maxSimilarityValue / denominator;
    }

}
