package extension.similarity.measure;

import de.uni_trier.wi2.procake.data.model.ModelFactory;
import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.data.object.base.ListObject;
import de.uni_trier.wi2.procake.data.object.base.impl.ListObjectImpl;
import de.uni_trier.wi2.procake.data.object.nest.NESTSequentialWorkflowObject;
import de.uni_trier.wi2.procake.data.object.nest.NESTTaskNodeObject;
import de.uni_trier.wi2.procake.data.object.nest.utils.impl.NESTSequentialWorkflowValidatorImpl;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.SimilarityValuator;
import de.uni_trier.wi2.procake.similarity.impl.SimilarityImpl;
import de.uni_trier.wi2.procake.similarity.nest.sequence.SMGraphDTW;
import de.uni_trier.wi2.procake.similarity.nest.sequence.impl.SMGraphDTWImpl;
import de.uni_trier.wi2.procake.utils.exception.NoSequentialGraphException;
import extension.abstraction.IMethodInvokerFunc;
import extension.abstraction.ISimFunc;
import extension.abstraction.IWeightFunc;
import utils.MethodInvoker;
import utils.MethodInvokerFunc;
import utils.SimFunc;
import utils.WeightFunc;

import java.util.ArrayList;
import java.util.Iterator;

public class SMGraphDTWImplExt extends SMGraphDTWImpl implements SMGraphDTW, ISimFunc, IWeightFunc, IMethodInvokerFunc {
    protected SimFunc similarityToUseFunc;
    protected WeightFunc weightFunc = (a) -> 1;
    protected MethodInvokerFunc methodInvokerFunc = (a, b) -> new ArrayList<MethodInvoker>();

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

    private Similarity similarity;

    @Override
    public Similarity compute(DataObject queryObject, DataObject caseObject, SimilarityValuator valuator) {

        if (!queryObject.isNESTSequentialWorkflow() || !caseObject.isNESTSequentialWorkflow()) {
            throw new NoSequentialGraphException("Query and case graph must be a sequential workflow",
                    this);
        }

        NESTSequentialWorkflowObject queryGraph = ((NESTSequentialWorkflowObject) queryObject);
        NESTSequentialWorkflowObject caseGraph = ((NESTSequentialWorkflowObject) caseObject);

        if (!new NESTSequentialWorkflowValidatorImpl(queryGraph).isValidSequentialWorkflow()) {
            throw new NoSequentialGraphException(
                    "Query graph must be a valid sequential workflow for usage of SWA measure",
                    queryGraph.getId(), queryGraph);
        }
        if (!new NESTSequentialWorkflowValidatorImpl(caseGraph).isValidSequentialWorkflow()) {
            throw new NoSequentialGraphException(
                    "Case graph must be a valid sequential workflow for usage of SWA measure",
                    caseGraph.getId(), caseGraph);
        }

        if (queryGraph.getGraphNodes(DataObject::isNESTTaskNode).isEmpty()) {
            similarity = new SimilarityImpl(this, queryObject, caseObject, 1.0, new ArrayList<>());
            return similarity;
        }

        if (caseGraph.getGraphNodes(DataObject::isNESTTaskNode).isEmpty()) {
            similarity =  new SimilarityImpl(this, queryObject, caseObject, 0.0, new ArrayList<>());
            return similarity;
        }

        ListObject queryList = new ListObjectImpl(ModelFactory.getDefaultModel().getListSystemClass());
        ListObject caseList = new ListObjectImpl(ModelFactory.getDefaultModel().getListSystemClass());

        Iterator queryIt = queryGraph.getTaskNodes().iterator();
        while (queryIt.hasNext()) {
            queryList.addValue(  ((NESTTaskNodeObject) queryIt.next()).getSemanticDescriptor()  );
        }

        Iterator caseIt = caseGraph.getTaskNodes().iterator();
        while (caseIt.hasNext()) {
            caseList.addValue(  ((NESTTaskNodeObject) caseIt.next()).getSemanticDescriptor()  );
        }

        SMListDTWImplExt smListDTWImplExt = new SMListDTWImplExt();
        smListDTWImplExt.setSimilarityToUse(getSimilarityToUseFunc());
        smListDTWImplExt.setWeightFunction(getWeightFunction());
        smListDTWImplExt.setMethodInvokerFunc(getMethodInvokerFunc());
        smListDTWImplExt.setValBelowZero(getValBelowZero());
        smListDTWImplExt.setHalvingDistancePercentage(getHalvingDistancePercentage());
        smListDTWImplExt.setForceAlignmentEndsWithQuery(getForceAlignmentEndsWithQuery());

        this.similarity = smListDTWImplExt.compute(queryList, caseList, valuator);

        return similarity;

    }

}
