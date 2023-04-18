package extension.similarity.measure;

import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.data.object.base.ListObject;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.SimilarityValuator;
import de.uni_trier.wi2.procake.similarity.base.collection.SMListSWA;
import de.uni_trier.wi2.procake.similarity.base.collection.impl.SMListSWAImpl;
import de.uni_trier.wi2.procake.similarity.impl.SimilarityImpl;
import extension.abstraction.IMethodInvokerFunc;
import extension.abstraction.ISimFunc;
import extension.abstraction.IWeightFunc;
import extension.similarity.valuator.SimilarityValuatorImplExt;
import utils.MethodInvoker;
import utils.MethodInvokerFunc;
import utils.SimFunc;
import utils.WeightFunc;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class SMListSWAImplExt extends SMListSWAImpl implements SMListSWA, ISimFunc, IWeightFunc, IMethodInvokerFunc {

    protected SimFunc similarityToUseFunc;
    protected WeightFunc weightFunc = (a) -> 1;
    protected MethodInvokerFunc methodInvokerFunc = (a,b) -> new ArrayList<MethodInvoker>();

    @Override
    public void setLocalSimilarityToUse(String newValue) {
        super.setLocalSimilarityToUse(newValue);
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
    public void setMethodInvokerFunc(MethodInvokerFunc methodInvokerFunc) {
        this.methodInvokerFunc = methodInvokerFunc;
    }

    @Override
    public MethodInvokerFunc getMethodInvokerFunc() {
        return methodInvokerFunc;
    }

    private boolean algorithmFinished = false;
    private Similarity similarity;

    @Override
    public Similarity compute(DataObject queryObject, DataObject caseObject, SimilarityValuator valuator) {

        if (!algorithmFinished) similarity = new SimilarityImpl(this, queryObject, caseObject, computeSimilarityValue( queryObject, caseObject, valuator));

        return similarity;

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


        //initializing the matrix
        double[][] matrix = new double[caseArray.length][queryArray.length];
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

            double weight = getWeightFunction().apply( queryArray[j] );
            double wTemp = 1;
            if (halvingDistancePercentage>0) {
                wTempDenominator -= weight;
                wTemp = halvingDistancePercentage/(2*(wTempDenominator));
            }

            denominator += weight * wTemp;

            for ( int i = 1; i< caseArray.length; i++ ) {
                String localSimToUse = getSimilarityToUseFunc().apply( queryArray[j], caseArray[i] );

                //SimilarityMeasure sm = valuator.getSimilarityModel().getSimilarityMeasure( queryList[j-1].getDataClass(), localSimToUse );

                Similarity localSim;

                if (valuator instanceof SimilarityValuatorImplExt) {
                    try {
                        localSim = ((SimilarityValuatorImplExt) valuator).computeSimilarity(queryArray[j], caseArray[i], localSimToUse, methodInvokerFunc.apply(queryArray[j], caseArray[i]));
                    } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                        localSim = valuator.computeSimilarity( queryArray[j], caseArray[i], localSimToUse );
                    }
                }
                else localSim = valuator.computeSimilarity( queryArray[j], caseArray[i], localSimToUse );

                double diagonal =   matrix[i-1][j-1]    + wTemp     * localSim.getValue()                    * weight;
                double horizontal = matrix[i][j-1]      + wTemp     * insertionScheme.apply(caseArray[i])    * weight;
                double vertical =   matrix[i-1][j]      + wTemp     * deletionScheme.apply(queryArray[j])    * weight;

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
                    && (!forceAlignmentEndsWithQuery || j == queryArray.length-1)) {
                    maxCell_i = i;
                    maxCell_j = j;
                }

            }
        }

        double maxValue = matrix[maxCell_i][maxCell_j];

        algorithmFinished = true;

        return maxValue / denominator;
    }

}
