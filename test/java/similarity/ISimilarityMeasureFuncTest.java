package similarity;

import de.uni_trier.wi2.procake.data.model.nest.NESTSequentialWorkflowClass;
import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.data.object.base.ListObject;
import de.uni_trier.wi2.procake.data.object.nest.NESTSequentialWorkflowObject;
import de.uni_trier.wi2.procake.data.object.nest.NESTTaskNodeObject;
import de.uni_trier.wi2.procake.data.object.nest.utils.NESTAbstractWorkflowModifier;
import de.uni_trier.wi2.procake.data.object.nest.utils.NESTWorkflowBuilder;
import de.uni_trier.wi2.procake.data.object.nest.utils.impl.NESTWorkflowBuilderImpl;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.SimilarityModelFactory;
import de.uni_trier.wi2.procake.similarity.SimilarityValuator;
import de.uni_trier.wi2.procake.similarity.base.string.SMStringEqual;
import org.junit.Test;
import utils.MethodInvoker;
import utils.SimilarityMeasureFunc;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public abstract class ISimilarityMeasureFuncTest extends SimilarityMeasureTest {

    @Test
    public void same_as_superclass_weekdays_workdays() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        ListObject queryObject = weekdays();
        ListObject caseObject = workdays();


        SimilarityValuator oldSimVal = SimilarityModelFactory.newSimilarityValuator();
        Similarity oldSim = oldSimVal.computeSimilarity(queryObject, caseObject, superclassName);

        ArrayList<MethodInvoker> methodInvokers = new ArrayList<>();
        methodInvokers.add(new MethodInvoker(
                "setSimilarityMeasureFunc",
                new Class[]{SimilarityMeasureFunc.class},
                new Object[]{new SimilarityMeasureFunc() {
                    @Override
                    public String apply(DataObject q, DataObject c) {
                        return SMStringEqual.NAME;
                    }
                }}
        ));

        Similarity newSim = simVal.computeSimilarity(queryObject, caseObject, name, methodInvokers);

        assertEquals(oldSim.getValue(), newSim.getValue(), delta);
    }

    @Test
    public void same_as_superclass_weekdays_weekdays() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        ListObject queryObject = weekdays();
        ListObject caseObject = weekdays();


        SimilarityValuator oldSimVal = SimilarityModelFactory.newSimilarityValuator();
        Similarity oldSim = oldSimVal.computeSimilarity(queryObject, caseObject, superclassName);

        ArrayList<MethodInvoker> methodInvokers = new ArrayList<>();
        methodInvokers.add(new MethodInvoker(
                "setSimilarityMeasureFunc",
                new Class[]{SimilarityMeasureFunc.class},
                new Object[]{new SimilarityMeasureFunc() {
                    @Override
                    public String apply(DataObject q, DataObject c) {
                        return SMStringEqual.NAME;
                    }
                }}
        ));

        Similarity newSim = simVal.computeSimilarity(queryObject, caseObject, name, methodInvokers);

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



        ArrayList<MethodInvoker> methodInvokers = new ArrayList<>() ;
        methodInvokers.add(new MethodInvoker(
                "setSimilarityMeasureFunc",
                new Class[]{SimilarityMeasureFunc.class},
                new Object[]{new SimilarityMeasureFunc() {
                    @Override
                    public String apply(DataObject q, DataObject c) {
                        return SMStringEqual.NAME;
                    }
                }})
        );

        simVal.computeSimilarity(queryGraph, caseGraph, name, methodInvokers);
    }
}
