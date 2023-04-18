package test;

import de.uni_trier.wi2.procake.data.model.nest.NESTSequentialWorkflowClass;
import de.uni_trier.wi2.procake.data.object.nest.NESTSequentialWorkflowObject;
import de.uni_trier.wi2.procake.data.object.nest.NESTTaskNodeObject;
import de.uni_trier.wi2.procake.data.object.nest.utils.NESTAbstractWorkflowModifier;
import de.uni_trier.wi2.procake.data.object.nest.utils.NESTWorkflowBuilder;
import de.uni_trier.wi2.procake.data.object.nest.utils.impl.NESTWorkflowBuilderImpl;
import extension.SimilarityMeasures.SMGraphDTWImplExt;
import extension.SimilarityMeasures.SMGraphSWAImplExt;
import org.junit.Test;

public class SMGraphDPImplExtTest extends CollectionSimilarityTest{

    @Test
    public void test1(){
        NESTWorkflowBuilder<NESTSequentialWorkflowObject> builder = new NESTWorkflowBuilderImpl();



        NESTSequentialWorkflowObject queryGraph = builder.createNESTWorkflowGraphObject("query", NESTSequentialWorkflowClass.CLASS_NAME,null);

        NESTAbstractWorkflowModifier queryModifier = queryGraph.getModifier();

        NESTTaskNodeObject queryTaskA = queryModifier.insertNewTaskNode(utils.createStringObject("A"));
        NESTTaskNodeObject queryTaskB = queryModifier.insertNewTaskNode(utils.createStringObject("B"));
        NESTTaskNodeObject queryTaskC = queryModifier.insertNewTaskNode(utils.createStringObject("C"));

        queryModifier.insertNewControlflowEdge(queryTaskA, queryTaskB, null);
        queryModifier.insertNewControlflowEdge(queryTaskB, queryTaskC, null);

        queryGraph.transformNESTGraphToNESTSequentialWorkflow(queryGraph);



        NESTSequentialWorkflowObject caseGraph = builder.createNESTWorkflowGraphObject("case", NESTSequentialWorkflowClass.CLASS_NAME,null);

        NESTAbstractWorkflowModifier caseModifier = caseGraph.getModifier();

        NESTTaskNodeObject caseTaskA = queryModifier.insertNewTaskNode(utils.createStringObject("D"));
        NESTTaskNodeObject caseTaskB = queryModifier.insertNewTaskNode(utils.createStringObject("E"));
        NESTTaskNodeObject caseTaskC = queryModifier.insertNewTaskNode(utils.createStringObject("F"));

        caseModifier.insertNewControlflowEdge(caseTaskA, caseTaskB, null);
        caseModifier.insertNewControlflowEdge(caseTaskB, caseTaskC, null);

        caseGraph.transformNESTGraphToNESTSequentialWorkflow(caseGraph);




        SMGraphSWAImplExt smGraphSWAImplExt = new SMGraphSWAImplExt();
        smGraphSWAImplExt.setLocalSimilarityToUse("SMStringEqual");

        smGraphSWAImplExt.compute(queryGraph, caseGraph, simVal);



        SMGraphDTWImplExt smGraphDTWImplExt = new SMGraphDTWImplExt();
        smGraphDTWImplExt.setLocalSimilarityToUse("SMStringEqual");

        smGraphDTWImplExt.compute(queryGraph, caseGraph, simVal);






    }
}
