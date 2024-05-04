package de.uni_trier.wi2.extension.abstraction;

import de.uni_trier.wi2.procake.data.model.ModelFactory;
import de.uni_trier.wi2.procake.data.object.base.ListObject;
import de.uni_trier.wi2.procake.data.object.base.impl.ListObjectImpl;
import de.uni_trier.wi2.procake.data.object.nest.NESTSequenceNodeObject;
import de.uni_trier.wi2.procake.data.object.nest.NESTSequentialWorkflowObject;
import de.uni_trier.wi2.procake.data.object.nest.NESTTaskNodeObject;
import de.uni_trier.wi2.procake.data.object.nest.utils.impl.NESTSequentialWorkflowValidatorImpl;
import de.uni_trier.wi2.procake.utils.exception.NoSequentialGraphException;

import java.util.Set;



/**
 * A simple interface providing a default method for converting {@link NESTSequentialWorkflowObject}s to {@link ListObject}s.
 * Implementing this interface can be useful for similarity measures.
 */
public interface INESTtoList {

    /**
     * converts a valid {@link NESTSequentialWorkflowObject} to a {@link ListObject}
     *
     * <p>In a sequential workflow every task node has exactly one incoming and one outgoing edge,
     * with the first and last ones being exceptions.
     * The semantic descriptors of these task nodes are being put in a list in the order of the workflow.
     * This list is then returned.
     *
     * @param workflowObject  the sequential workflow to be converted
     * @return  the list containing the semantic descriptors of the task nodes
     * @throws NoSequentialGraphException if (1) {@code workflowObject} is not a valid {@code NESTSequentialWorkflow} or
     * (2) {@code workflowObject.getStartNodes().size()} is greater than 1.
     */
    default ListObject toList(NESTSequentialWorkflowObject workflowObject) throws NoSequentialGraphException {
        ListObject workflowList = new ListObjectImpl(ModelFactory.getDefaultModel().getListSystemClass());

        Set<NESTSequenceNodeObject> startNodes = workflowObject.getStartNodes();

        if (startNodes.isEmpty()) return workflowList;

        if (!new NESTSequentialWorkflowValidatorImpl(workflowObject).isValidSequentialWorkflow() || startNodes.size() > 1) {
            NoSequentialGraphException e = new NoSequentialGraphException(
                    "NESTSequentialWorkflowObject is not valid.",
                    workflowObject.getId(),
                    workflowObject);
            throw e;
        }

        NESTSequenceNodeObject node = startNodes.iterator().next();

        while (node != null) {
            workflowList.addValue(  ((NESTTaskNodeObject) node).getSemanticDescriptor()  );
            node = node.getNextNode();
        }

        return workflowList;
    }
}
