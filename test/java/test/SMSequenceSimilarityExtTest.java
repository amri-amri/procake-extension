package test;

import de.uni_trier.wi2.procake.CakeInstance;
import de.uni_trier.wi2.procake.data.model.ModelFactory;
import de.uni_trier.wi2.procake.data.model.nest.NESTWorkflowClass;
import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.data.object.DataObjectUtils;
import de.uni_trier.wi2.procake.data.object.base.IntegerObject;
import de.uni_trier.wi2.procake.data.object.base.StringObject;
import de.uni_trier.wi2.procake.data.object.nest.NESTSequentialWorkflowObject;
import de.uni_trier.wi2.procake.data.object.nest.NESTTaskNodeObject;
import de.uni_trier.wi2.procake.data.object.nest.NESTWorkflowObject;
import de.uni_trier.wi2.procake.data.object.nest.utils.NESTWorkflowBuilder;
import de.uni_trier.wi2.procake.data.object.nest.utils.NESTWorkflowModifier;
import de.uni_trier.wi2.procake.data.object.nest.utils.impl.NESTWorkflowBuilderImpl;
import de.uni_trier.wi2.procake.similarity.SimilarityModelFactory;
import de.uni_trier.wi2.procake.similarity.SimilarityValuator;
import de.uni_trier.wi2.procake.similarity.base.numeric.SMNumericLinear;
import de.uni_trier.wi2.procake.similarity.base.string.SMStringEqual;
import de.uni_trier.wi2.procake.similarity.nest.sequence.utils.DTW;
import de.uni_trier.wi2.procake.similarity.nest.sequence.utils.SWA;
import de.uni_trier.wi2.procake.similarity.nest.sequence.utils.impl.DTWImpl;
import de.uni_trier.wi2.procake.similarity.nest.sequence.utils.impl.SWAImpl;
import de.uni_trier.wi2.procake.similarity.nest.sequence.utils.impl.ScoringMatrixImpl;
import de.uni_trier.wi2.procake.utils.io.ResourcePaths;

import javax.naming.directory.InvalidAttributeValueException;

public class SMSequenceSimilarityExtTest {




    public static void main(String[] args) throws InvalidAttributeValueException {
        SWATest();
    }


    static String[] seqInts = {
            "13445223",
            "4321455",
            "12222222223",
            "18987987973"
    };

    static double valBelowZero = 1;

    static Integer min(String x){
        if (x == null || x.length()==0) return null;
        int min = Integer.parseInt(x.substring(0,1));
        for (int i = 1; i<x.length(); i++) {
            min = Math.min(min,Integer.parseInt(x.substring(i,i+1)));
        }
        return min;
    }

    static Integer max(String x){
        if (x == null || x.length()==0) return null;
        int max = Integer.parseInt(x.substring(0,1));
        for (int i = 1; i<x.length(); i++) {
            max = Math.max(max,Integer.parseInt(x.substring(i,i+1)));
        }
        return max;
    }

    static int getMin(){
        int min = min(seqInts[0]);
        for (int i = 1; i < seqInts.length; i++){
            min = Math.min( min , min(seqInts[i]) );
        }
        return min;
    }

    static int getMax(){
        int max = max(seqInts[0]);
        for (int i = 1; i < seqInts.length; i++){
            max = Math.max( max , max(seqInts[i]) );
        }
        return max;
    }

    public static void DTWTest() throws InvalidAttributeValueException {
        seqs = new NESTSequentialWorkflowObject[seqInts.length];

        CakeInstance.start(ResourcePaths.PATH_COMPOSITION);

        utils = new DataObjectUtils();

        simVal = SimilarityModelFactory.newSimilarityValuator();

        //System.out.println(simVal.getSimilarityModel().getSimilarityMeasure(utils.createIntegerObject().getDataClass()));

        SMNumericLinear smNumericLinear = (SMNumericLinear) SimilarityModelFactory.getDefaultSimilarityModel().createSimilarityMeasure(SMNumericLinear.NAME, ModelFactory.getDefaultModel().getIntegerSystemClass());
        smNumericLinear.setMaximum(getMax());
        smNumericLinear.setMinimum(getMin());
        simVal.getSimilarityModel().addSimilarityMeasure(smNumericLinear, "SMNumericLinear");

        //System.out.println(simVal.getSimilarityModel().getSimilarityMeasure(utils.createIntegerObject().getDataClass()));

        for (int i = 0; i<seqInts.length; i++) seqs[i] = intSeq(seqInts[i]);

        for (int i = 0; i<seqInts.length; i++) for (int j = 0; j<seqInts.length; j++)
        {
            // printSim(i,j);
        }
        printDTWSim(2,3);
    }

    public static void printDTWSim(int i, int j){
        NESTSequentialWorkflowObject a = seqs[i];
        NESTSequentialWorkflowObject b = seqs[j];
        String aString = seqInts[i];
        String bString = seqInts[j];

        DTWImpl dtw = DTW.newDTWCalculation( a , b );
        dtw.setSimilarityValuator(simVal);
        dtw.setStretchSim(valBelowZero);
        dtw.setHalvingDistancePercentage(0.0);
        //dtw.setLocalSimilarityToUse("");
        dtw.computeSimilarity();
        double raw = dtw.getRawSimilarityScore();
        double normed = dtw.getNormedSimilarityScore();
        dtw.visualizeAlignment();

        System.out.println(aString + "->" + bString);
        System.out.println("vbz: " + valBelowZero);
        System.out.println("Raw Score: " + raw);
        System.out.println("Max Score: " + raw/normed);
        System.out.println("Normed Score: " + normed);
        System.out.println("Alignment:");
        System.out.println(intAlignmentToString(dtw.getAlignment()));
        System.out.println();
    }







    static String[] seqStrings = {
            "AYBCMM",
            "ABCKMMH"
    };

    static SimilarityValuator simVal;
    static DataObjectUtils utils;

    static NESTSequentialWorkflowObject[] seqs;

    static double deletion = -0.5;
    static double insertion = -0.5;

    public static void SWATest()
    {
        seqs = new NESTSequentialWorkflowObject[seqStrings.length];

        CakeInstance.start(ResourcePaths.PATH_COMPOSITION);

        utils = new DataObjectUtils();

        simVal = SimilarityModelFactory.newSimilarityValuator();

        //System.out.println(simVal.getSimilarityModel().getSimilarityMeasure(utils.createStringObject().getDataClass()));

        SMStringEqual smStringEquals = (SMStringEqual) SimilarityModelFactory.getDefaultSimilarityModel().createSimilarityMeasure(SMStringEqual.NAME, ModelFactory.getDefaultModel().getStringSystemClass());
        smStringEquals.setCaseSensitive();
        simVal.getSimilarityModel().addSimilarityMeasure(smStringEquals, "SMStringEquals");

        //System.out.println(simVal.getSimilarityModel().getSimilarityMeasure(utils.createStringObject().getDataClass()));

        for (int i = 0; i<seqStrings.length; i++) seqs[i] = stringSeq(seqStrings[i]);

        for (int i = 0; i<seqStrings.length; i++) for (int j = 0; j<seqStrings.length; j++)
        {
            printSWASim(i,j);
        }
        printSWASim(0,1);
        deletion = 0.5;
        insertion = 0.5;
        printSWASim(0,1);
        deletion = 1.5;
        insertion = 1.5;
        printSWASim(0,1);
    }

    public static NESTSequentialWorkflowObject stringSeq(String string){
        NESTWorkflowBuilder<NESTWorkflowObject> builder = new NESTWorkflowBuilderImpl();
        NESTWorkflowObject workflow = builder.createNESTWorkflowGraphObject(string, NESTWorkflowClass.CLASS_NAME,utils.createStringObject("seq1"));

        NESTWorkflowModifier modifier = workflow.getModifier();

        NESTTaskNodeObject[] tasks = new NESTTaskNodeObject[string.length()];

        for (int i = 0; i<string.length(); i++){
            tasks[i] = modifier.insertNewTaskNode(utils.createStringObject(string.substring(i,i+1)));
        }

        for (int i = 0; i<string.length()-1; i++){
            modifier.insertNewControlflowEdge(tasks[i], tasks[i+1], null);
        }

        NESTSequentialWorkflowObject sequentialWorkflow=(NESTSequentialWorkflowObject)ModelFactory.getDefaultModel().getNESTSequentialWorkflowClass().newObject();
        sequentialWorkflow.transformNESTGraphToNESTSequentialWorkflow(workflow);

        return sequentialWorkflow;
    }
    public static NESTSequentialWorkflowObject intSeq(String string){
        NESTWorkflowBuilder<NESTWorkflowObject> builder = new NESTWorkflowBuilderImpl();
        NESTWorkflowObject workflow = builder.createNESTWorkflowGraphObject(string, NESTWorkflowClass.CLASS_NAME,utils.createStringObject("seq1"));

        NESTWorkflowModifier modifier = workflow.getModifier();

        NESTTaskNodeObject[] tasks = new NESTTaskNodeObject[string.length()];

        for (int i = 0; i<string.length(); i++){
            tasks[i] = modifier.insertNewTaskNode(utils.createIntegerObject(Integer.parseInt(string.substring(i,i+1))));
        }

        for (int i = 0; i<string.length()-1; i++){
            modifier.insertNewControlflowEdge(tasks[i], tasks[i+1], null);
        }

        NESTSequentialWorkflowObject sequentialWorkflow=(NESTSequentialWorkflowObject)ModelFactory.getDefaultModel().getNESTSequentialWorkflowClass().newObject();
        sequentialWorkflow.transformNESTGraphToNESTSequentialWorkflow(workflow);

        return sequentialWorkflow;
    }

    public static String stringAlignmentToString(DataObject[][] alignment){
        DataObject[] A = alignment[0];
        DataObject[] B = alignment[1];
        String out = "";
        String row = "";
        for (int x = 0; x<A.length; x++){
            if (A[x] != null) {
                StringObject letter = (StringObject) ((NESTTaskNodeObject) A[x]).getSemanticDescriptor();
                row = row + letter.getNativeString();
            }
            else row = row + "-";

            row = row + " ";
        }
        out = out + row + "\n";
        row = "";
        for (int x = 0; x<B.length; x++){
            if (B[x] != null) {
                StringObject letter = (StringObject) ((NESTTaskNodeObject) B[x]).getSemanticDescriptor();
                row = row + letter.getNativeString();
            }
            else row = row + "-";

            row = row + " ";
        }
        out = out + row;
        return out;
    }

    public static String intAlignmentToString(DataObject[][] alignment){
        DataObject[] A = alignment[0];
        DataObject[] B = alignment[1];
        String out = "";
        String row = "";
        for (int x = 0; x<A.length; x++){
            if (A[x] != null) {
                String letter = Integer.toString( ((IntegerObject) ((NESTTaskNodeObject) A[x]).getSemanticDescriptor()).getNativeInteger() );
                row = row + letter;
            }
            else row = row + "-";

            row = row + " ";
        }
        out = out + row + "\n";
        row = "";
        for (int x = 0; x<B.length; x++){
            if (B[x] != null) {
                String letter = Integer.toString( ((IntegerObject) ((NESTTaskNodeObject) B[x]).getSemanticDescriptor()).getNativeInteger() );
                row = row + letter;
            }
            else row = row + "-";

            row = row + " ";
        }
        out = out + row;
        return out;
    }

    public static void printSWASim(int i, int j){
        NESTSequentialWorkflowObject a = seqs[i];
        NESTSequentialWorkflowObject b = seqs[j];
        String aString = seqStrings[i];
        String bString = seqStrings[j];

        SWAImpl swa = SWA.newSWACalculation( a , b );
        swa.setSimilarityValuator(simVal);
        swa.setDeletionPenaltyScheme(t -> deletion);
        swa.setInsertionPenaltyScheme(t -> insertion);
        swa.setHalvingDistancePercentage(0.0);
        swa.setLocalSimilarityToUse("SMStringEquals");
        swa.setBindToLastRow(false);
        swa.computeSimilarity();
        double raw = swa.getRawSimilarityScore();
        double normed = swa.getNormedSimilarityScore();

        System.out.println(aString + "->" + bString);
        System.out.println("del: " + deletion);
        System.out.println("ins: " + insertion);
        System.out.println("Raw Score: " + raw);
        System.out.println("Max Score: " + raw/normed);
        System.out.println("Normed Score: " + normed);
        System.out.println("Alignment:");
        System.out.println(stringAlignmentToString(swa.getAlignment()));
        System.out.println();
    }
}
