package extension;

import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.data.object.base.ListObject;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.SimilarityValuator;
import de.uni_trier.wi2.procake.similarity.base.collection.SMListDTW;
import de.uni_trier.wi2.procake.similarity.base.collection.impl.SMListDTWImpl;
import de.uni_trier.wi2.procake.similarity.impl.SimilarityImpl;
import utils.SimFunc;
import utils.WeightFunc;

public class SMListDTWImplExt extends SMListDTWImpl implements SMListDTW, ISimFunc, IWeightFunc {
    protected SimFunc similarityToUseFunc;
    protected WeightFunc weightFunc = (a) -> 1;

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
        double[][] normalizingMatrix = new double[caseArray.length][queryArray.length];
        int[][][] origins = new int[caseArray.length][queryArray.length][2];
        for (int j = 0; j< queryArray.length; j++){
            matrix[0][j] = 0;
            normalizingMatrix[0][j] = 0;
            origins[0][j][0] = 0;
            origins[0][j][1] = j-1;
        }
        for (int i = 1; i< caseArray.length; i++){
            matrix[i][0] = 0;
            normalizingMatrix[i][0] = 0;
            origins[i][0][0] = i-1;
            origins[i][0][1] = 0;
        }

        //compute matrix values and find maximum
        int maxCell_i = 0;
        int maxCell_j = 0;

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

            for ( int i = 1; i< caseArray.length; i++ ) {
                String localSimToUse = getSimilarityToUseFunc().apply( queryArray[j], caseArray[i] );

                //SimilarityMeasure sm = valuator.getSimilarityModel().getSimilarityMeasure( queryList[j-1].getDataClass(), localSimToUse );

                Similarity localSim = valuator.computeSimilarity( queryArray[j], caseArray[i], localSimToUse );
                //localSim = new SimilarityImpl(sm, queryList[j-1], caseList[i-1],localSim.getValue() * weight );

                double diagonal =   matrix[i-1][j-1]    + wTemp     * localSim.getValue()    * weight * 2;
                double horizontal = matrix[i][j-1]      + wTemp     * localSim.getValue()    * weight;
                double vertical =   matrix[i-1][j]      + wTemp     * localSim.getValue()    * weight;

                if (diagonal >= horizontal && diagonal >= vertical) {
                    origins[i][j][0] = i-1;
                    origins[i][j][1] = j-1;

                    matrix[i][j] = diagonal;
                    normalizingMatrix[i][j] = normalizingMatrix[i-1][j-1]    + wTemp     * weight * 2;

                }
                else if (horizontal > diagonal && horizontal >= vertical) {
                    origins[i][j][0] = i;
                    origins[i][j][1] = j-1;

                    matrix[i][j] = horizontal;
                    normalizingMatrix[i][j] = normalizingMatrix[i][j-1]    + wTemp     * weight;

                }
                else if (vertical > diagonal && vertical >= horizontal) {
                    origins[i][j][0] = i-1;
                    origins[i][j][1] = j;

                    matrix[i][j] = vertical;
                    normalizingMatrix[i][j] = normalizingMatrix[i-1][j]    + wTemp     * weight;

                }

                if (matrix[i][j] >= matrix[maxCell_i][maxCell_j]
                        && (!forceAlignmentEndsWithQuery || j == queryArray.length-1)) {
                    maxCell_i = i;
                    maxCell_j = j;
                }

            }
        }



        double maxValue = matrix[maxCell_i][maxCell_j];
        double denominator = normalizingMatrix[maxCell_i][maxCell_j];

        algorithmFinished = true;

        return maxValue / denominator;
    }

}
