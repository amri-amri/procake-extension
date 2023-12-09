package de.uni_trier.wi2.similarity;

import de.uni_trier.wi2.base.SimpleTestBase;
import de.uni_trier.wi2.procake.data.model.nest.NESTSequentialWorkflowClass;
import de.uni_trier.wi2.procake.data.object.base.ListObject;
import de.uni_trier.wi2.procake.data.object.nest.NESTSequentialWorkflowObject;
import de.uni_trier.wi2.procake.data.object.nest.NESTTaskNodeObject;
import de.uni_trier.wi2.procake.data.object.nest.utils.NESTAbstractWorkflowModifier;
import de.uni_trier.wi2.procake.data.object.nest.utils.NESTWorkflowBuilder;
import de.uni_trier.wi2.procake.data.object.nest.utils.impl.NESTWorkflowBuilderImpl;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.SimilarityModelFactory;
import de.uni_trier.wi2.procake.similarity.SimilarityValuator;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.assertEquals;

public abstract class SimilarityMeasureTest extends SimpleTestBase {


    public String name;
    public String superclassName;

    @Test
    public void empty_lists(){
        ListObject queryList = utils.createListObject();
        ListObject caseList = utils.createListObject();

        simVal.computeSimilarity(queryList, caseList, name);
    }

    @Test
    public void same_as_superclass_weekdays_workdays() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        ListObject queryObject = weekdays();
        ListObject caseObject = workdays();


        SimilarityValuator oldSimVal = SimilarityModelFactory.newSimilarityValuator();
        Similarity oldSim = oldSimVal.computeSimilarity(queryObject, caseObject, superclassName);

        Similarity newSim = simVal.computeSimilarity(queryObject, caseObject, name);

        assertEquals(oldSim.getValue(), newSim.getValue(), delta);
    }

    @Test
    public void same_as_superclass_weekdays_weekdays() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        ListObject queryObject = weekdays();
        ListObject caseObject = workdays();


        SimilarityValuator oldSimVal = SimilarityModelFactory.newSimilarityValuator();
        Similarity oldSim = oldSimVal.computeSimilarity(queryObject, caseObject, superclassName);

        Similarity newSim = simVal.computeSimilarity(queryObject, caseObject, name);

        assertEquals(oldSim.getValue(), newSim.getValue(), delta);
    }

    @Test
    public void graph_as_input() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
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


        simVal.computeSimilarity(queryGraph, caseGraph, name);
    }


}
