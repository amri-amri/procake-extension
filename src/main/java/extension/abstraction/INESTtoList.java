package extension.abstraction;

import de.uni_trier.wi2.procake.data.model.ModelFactory;
import de.uni_trier.wi2.procake.data.object.base.ListObject;
import de.uni_trier.wi2.procake.data.object.base.impl.ListObjectImpl;
import de.uni_trier.wi2.procake.data.object.nest.NESTSequentialWorkflowObject;
import de.uni_trier.wi2.procake.data.object.nest.NESTTaskNodeObject;
import de.uni_trier.wi2.procake.data.object.nest.utils.impl.NESTSequentialWorkflowValidatorImpl;
import de.uni_trier.wi2.procake.utils.exception.NoSequentialGraphException;

import java.util.Iterator;

/**
 * A simple interface providing a default method for converting {@link NESTSequentialWorkflowObject}s to {@link ListObject}s.
 * Implementing this interface can be useful for similarity measures.
 */
public interface INESTtoList {

    /**
     * converts a valid {@link NESTSequentialWorkflowObject} to a {@link ListObject}
     *
     * <p>In a sequential workflow every task node has exactly one incoming and one outgoing edge,
     * with the first and last ones being the exception.
     *
     * <p>The semantic descriptors of these task nodes are being put in a list in the order of the workflow.
     * This list is then returned.
     *
     * @param workflowObject  the sequential workflow to be converted
     * @return  the list containing the semantic descriptors of the task nodes
     */
    default ListObject toList(NESTSequentialWorkflowObject workflowObject){

        if (!new NESTSequentialWorkflowValidatorImpl(workflowObject).isValidSequentialWorkflow()) {
            throw new NoSequentialGraphException(
                    "NESTSequentialWorkflowObject is not valid.",
                    workflowObject.getId(),
                    workflowObject);
        }

        ListObject workflowList = new ListObjectImpl(ModelFactory.getDefaultModel().getListSystemClass());

        Iterator workflowElementIterator = workflowObject.getTaskNodes().iterator();
        while (workflowElementIterator.hasNext()) {
            workflowList.addValue(  ((NESTTaskNodeObject) workflowElementIterator.next()).getSemanticDescriptor()  );
        }

        return workflowList;
    }
}
