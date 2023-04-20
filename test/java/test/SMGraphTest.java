package test;

import de.uni_trier.wi2.procake.data.model.ModelFactory;
import de.uni_trier.wi2.procake.data.model.nest.NESTSequentialWorkflowClass;
import de.uni_trier.wi2.procake.data.object.nest.NESTSequentialWorkflowObject;
import de.uni_trier.wi2.procake.data.object.nest.NESTTaskNodeObject;
import de.uni_trier.wi2.procake.data.object.nest.utils.NESTAbstractWorkflowModifier;
import de.uni_trier.wi2.procake.data.object.nest.utils.NESTWorkflowBuilder;
import de.uni_trier.wi2.procake.data.object.nest.utils.impl.NESTWorkflowBuilderImpl;
import de.uni_trier.wi2.procake.similarity.SimilarityMeasure;
import de.uni_trier.wi2.procake.similarity.base.string.SMStringEqual;
import extension.similarity.measure.*;
import org.junit.Test;
import utils.MethodInvoker;

import java.lang.reflect.InvocationTargetException;

public class SMGraphTest extends CollectionSimilarityTest{

    @Test
    public void test1() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        NESTWorkflowBuilder<NESTSequentialWorkflowObject> builder = new NESTWorkflowBuilderImpl();



        NESTSequentialWorkflowObject queryGraph = builder.createNESTWorkflowGraphObject("query", NESTSequentialWorkflowClass.CLASS_NAME,null);

        NESTAbstractWorkflowModifier queryModifier = queryGraph.getModifier();

        queryGraph.transformNESTGraphToNESTSequentialWorkflow(queryGraph);

        NESTTaskNodeObject queryTaskA = queryModifier.insertNewTaskNode(utils.createStringObject("A"));
        NESTTaskNodeObject queryTaskB = queryModifier.insertNewTaskNode(utils.createStringObject("B"));
        NESTTaskNodeObject queryTaskC = queryModifier.insertNewTaskNode(utils.createStringObject("C"));

        queryModifier.insertNewControlflowEdge(queryTaskA, queryTaskB, null);
        queryModifier.insertNewControlflowEdge(queryTaskB, queryTaskC, null);



        NESTSequentialWorkflowObject caseGraph = builder.createNESTWorkflowGraphObject("case", NESTSequentialWorkflowClass.CLASS_NAME,null);

        NESTAbstractWorkflowModifier caseModifier = caseGraph.getModifier();

        caseGraph.transformNESTGraphToNESTSequentialWorkflow(caseGraph);

        NESTTaskNodeObject caseTaskA = caseModifier.insertNewTaskNode(utils.createStringObject("A"));
        NESTTaskNodeObject caseTaskB = caseModifier.insertNewTaskNode(utils.createStringObject("C"));
        NESTTaskNodeObject caseTaskC = caseModifier.insertNewTaskNode(utils.createStringObject("B"));

        caseModifier.insertNewControlflowEdge(caseTaskA, caseTaskB, null);
        caseModifier.insertNewControlflowEdge(caseTaskB, caseTaskC, null);





        testSimilarityMeasureDP(SMListSWAExt.NAME, queryGraph, caseGraph);
        testSimilarityMeasureDP(SMListDTWExt.NAME, queryGraph, caseGraph);

        testSimilarityMeasure(SMCollectionIsolatedMappingExt.NAME, queryGraph, caseGraph);
        testSimilarityMeasure(SMCollectionMappingExt.NAME, queryGraph, caseGraph);
        testSimilarityMeasure(SMListMappingExt.NAME, queryGraph, caseGraph);

        testSimilarityMeasure(SMListCorrectnessExt.NAME, queryGraph, caseGraph);


    }

    void testSimilarityMeasureDP(String measureName, NESTSequentialWorkflowObject queryGraph, NESTSequentialWorkflowObject caseGraph) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        SimilarityMeasure sm = simVal.getSimilarityModel().getSimilarityMeasure(ModelFactory.getDefaultModel().getDataSystemClass(), measureName);
        MethodInvoker mi = new MethodInvoker("setLocalSimilarityToUse", new Class[]{String.class}, new Object[]{SMStringEqual.NAME});
        mi.invoke(sm);
        sm.compute(queryGraph, caseGraph, simVal).getValue();
    }

    void testSimilarityMeasure(String measureName, NESTSequentialWorkflowObject queryGraph, NESTSequentialWorkflowObject caseGraph) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        SimilarityMeasure sm = simVal.getSimilarityModel().getSimilarityMeasure(ModelFactory.getDefaultModel().getDataSystemClass(), measureName);
        MethodInvoker mi = new MethodInvoker("setSimilarityToUse", new Class[]{String.class}, new Object[]{SMStringEqual.NAME});
        mi.invoke(sm);
        sm.compute(queryGraph, caseGraph, simVal).getValue();
    }
}
