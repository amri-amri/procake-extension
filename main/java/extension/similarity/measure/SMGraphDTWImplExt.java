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
import extension.abstraction.IMethodInvokersFunc;
import extension.abstraction.ISimilarityMeasureFunc;
import extension.abstraction.IWeightFunc;
import utils.MethodInvoker;
import utils.MethodInvokersFunc;
import utils.SimilarityMeasureFunc;
import utils.WeightFunc;

import java.util.ArrayList;
import java.util.Iterator;

public class SMGraphDTWImplExt extends SMGraphDTWImpl implements SMGraphDTW, ISimilarityMeasureFunc, IWeightFunc, IMethodInvokersFunc {

    protected SimilarityMeasureFunc similarityToUseFunc;
    protected MethodInvokersFunc methodInvokersFunc = (a, b) -> new ArrayList<MethodInvoker>();
    protected WeightFunc weightFunc = (a) -> 1;

    @Override
    public void setLocalSimilarityToUse(String newValue) {
        super.setLocalSimilarityToUse(newValue);
        similarityToUseFunc = (a, b) -> newValue;
    }

    @Override
    public void setSimilarityMeasureFunc(SimilarityMeasureFunc similarityMeasureFunc){
        similarityToUseFunc = similarityMeasureFunc;
    }

    @Override
    public void setMethodInvokersFunc(MethodInvokersFunc methodInvokersFunc) {
        this.methodInvokersFunc = methodInvokersFunc;
    }

    @Override
    public MethodInvokersFunc getMethodInvokersFunc() {
        return methodInvokersFunc;
    }

    @Override
    public SimilarityMeasureFunc getSimilarityMeasureFunc() {
        return similarityToUseFunc;
    }

    @Override
    public void setWeightFunc(WeightFunc weightFunc) {
        this.weightFunc = (q) -> {
            Double weight = weightFunc.apply(q);
            if (weight==null) return 1;
            if (weight<0) return 0;
            if (weight>1) return 1;
            return weight;
        };
    }

    @Override
    public WeightFunc getWeightFunc() {
        return weightFunc;
    }



    @Override
    public Similarity compute(DataObject queryObject, DataObject caseObject, SimilarityValuator valuator) {

        if (!queryObject.isNESTSequentialWorkflow() || !caseObject.isNESTSequentialWorkflow()) {
            throw new NoSequentialGraphException(
                    "Query and case graph must be a sequential workflow",
                    this);
        }

        NESTSequentialWorkflowObject queryGraph = ((NESTSequentialWorkflowObject) queryObject);
        NESTSequentialWorkflowObject caseGraph = ((NESTSequentialWorkflowObject) caseObject);

        if (!new NESTSequentialWorkflowValidatorImpl(queryGraph).isValidSequentialWorkflow()) {
            throw new NoSequentialGraphException(
                    "Query graph must be a valid sequential workflow for usage of SWA measure",
                    queryGraph.getId(),
                    queryGraph);
        }
        if (!new NESTSequentialWorkflowValidatorImpl(caseGraph).isValidSequentialWorkflow()) {
            throw new NoSequentialGraphException(
                    "Case graph must be a valid sequential workflow for usage of SWA measure",
                    caseGraph.getId(),
                    caseGraph);
        }

        if (queryGraph.getGraphNodes(DataObject::isNESTTaskNode).isEmpty()) {
            return new SimilarityImpl(
                    this,
                    queryObject,
                    caseObject,
                    1.0,
                    new ArrayList<>());
        }

        if (caseGraph.getGraphNodes(DataObject::isNESTTaskNode).isEmpty()) {
            return new SimilarityImpl(
                    this,
                    queryObject,
                    caseObject,
                    0.0,
                    new ArrayList<>());
        }

        ListObject queryList = new ListObjectImpl(ModelFactory.getDefaultModel().getListSystemClass());
        ListObject caseList = new ListObjectImpl(ModelFactory.getDefaultModel().getListSystemClass());

        Iterator queryElementIterator = queryGraph.getTaskNodes().iterator();
        while (queryElementIterator.hasNext()) {
            queryList.addValue(  ((NESTTaskNodeObject) queryElementIterator.next()).getSemanticDescriptor()  );
        }

        Iterator caseElementIterator = caseGraph.getTaskNodes().iterator();
        while (caseElementIterator.hasNext()) {
            caseList.addValue(  ((NESTTaskNodeObject) caseElementIterator.next()).getSemanticDescriptor()  );
        }

        SMListDTWImplExt smListDTWImplExt = new SMListDTWImplExt();
        smListDTWImplExt.setSimilarityMeasureFunc(getSimilarityMeasureFunc());
        smListDTWImplExt.setWeightFunc(getWeightFunc());
        smListDTWImplExt.setMethodInvokersFunc(getMethodInvokersFunc());
        smListDTWImplExt.setValBelowZero(getValBelowZero());
        smListDTWImplExt.setHalvingDistancePercentage(getHalvingDistancePercentage());
        smListDTWImplExt.setForceAlignmentEndsWithQuery(getForceAlignmentEndsWithQuery());

        return new SimilarityImpl(this, queryObject, caseObject, smListDTWImplExt.compute(queryList, caseList, valuator).getValue());
    }

}
