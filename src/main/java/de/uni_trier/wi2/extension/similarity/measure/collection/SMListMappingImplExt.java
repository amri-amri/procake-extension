package de.uni_trier.wi2.extension.similarity.measure.collection;

import de.uni_trier.wi2.extension.abstraction.IMethodInvokersFunc;
import de.uni_trier.wi2.extension.abstraction.INESTtoList;
import de.uni_trier.wi2.extension.abstraction.ISimilarityMeasureFunc;
import de.uni_trier.wi2.extension.abstraction.IWeightFunc;
import de.uni_trier.wi2.extension.similarity.valuator.SimilarityValuatorImplExt;
import de.uni_trier.wi2.procake.data.model.DataClass;
import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.data.object.base.AggregateObject;
import de.uni_trier.wi2.procake.data.object.base.ListObject;
import de.uni_trier.wi2.procake.data.object.nest.NESTSequentialWorkflowObject;
import de.uni_trier.wi2.procake.data.objectpool.DataObjectIterator;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.SimilarityValuator;
import de.uni_trier.wi2.procake.similarity.base.collection.impl.SMListMappingImpl;
import de.uni_trier.wi2.procake.similarity.impl.SimilarityImpl;
import de.uni_trier.wi2.utils.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;


import static de.uni_trier.wi2.utils.XEStoSystem.getXESListAsSystemListObject;

/**
 * A similarity measure using the 'List Mapping' algorithm for {@link ListObject}s.
 *
 * <p>For more info on the algorithm <a href="https://wi2.pages.gitlab.rlp.net/procake/procake-wiki/sim/collections/#list-mapping">click here</a>.
 *
 *
 * <p>Instead of one single local similarity measure, a functional interface ({@link SimilarityMeasureFunc})
 * can be defined for this similarity measure.
 * This functional interface assigns a similarity measure to each pair of query element
 * and case element.
 *
 * <p>These similarity measures may be defined more precisely by setting their parameters via methods.
 * In order to call these methods another functional interface ({@link MethodInvokersFunc}) can be defined
 * for this similarity measure.
 * This functional interface assigns a list of {@link MethodInvoker} objects to each pair of query element
 * and case element.
 *
 * <p>The given methods are then invoked with given parameters by the respective similarity measures.
 *
 * <p>For the usage of MethodInvoker objects an object of {@link SimilarityValuatorImplExt} has to be used as
 * similarity valuator!
 *
 * <p>In addition, a functional interface ({@link WeightFunc}) can be defined to assign a weight value
 * between 0 and 1 to a query element.
 *
 * <p> The global similarity is a weighted average of the similarity values between the query and the case elements.
 * That means that each similarity is multiplied by the weight of the query element, and the sum of these values
 * is divided by the sum of the weights.
 */
public class SMListMappingImplExt extends SMListMappingImpl implements SMListMappingExt, INESTtoList, ISimilarityMeasureFunc, IWeightFunc, IMethodInvokersFunc {

    protected SimilarityMeasureFunc similarityMeasureFunc = (a, b) -> null;
    protected WeightFunc weightFunc = (a) -> 1;
    protected MethodInvokersFunc methodInvokersFunc = (a, b) -> new ArrayList<MethodInvoker>();

    @Override
    public void setSimilarityToUse(String similarityToUse) {
        
        super.setSimilarityToUse(similarityToUse);
        similarityMeasureFunc = (a, b) -> similarityToUse;
    }

    @Override
    public SimilarityMeasureFunc getSimilarityMeasureFunc() {
        
        
        return similarityMeasureFunc;
    }

    @Override
    public void setSimilarityMeasureFunc(SimilarityMeasureFunc similarityMeasureFunc) {
        
        this.similarityMeasureFunc = similarityMeasureFunc;
    }

    @Override
    public MethodInvokersFunc getMethodInvokersFunc() {
        
        
        return methodInvokersFunc;
    }

    @Override
    public void setMethodInvokersFunc(MethodInvokersFunc methodInvokersFunc) {
        
        this.methodInvokersFunc = methodInvokersFunc;
    }

    @Override
    public WeightFunc getWeightFunc() {
        
        
        return weightFunc;
    }

    @Override
    public void setWeightFunc(WeightFunc weightFunc) {
        
        this.weightFunc = (q) -> {
            Double weight = weightFunc.apply(q);
            if (weight == null) return 1;
            if (weight < 0) return 0;
            if (weight > 1) return 1;
            return weight;
        };
    }

    public String getSystemName() {
        
        
        return SMListMappingExt.NAME;
    }

    @Override
    public boolean isSimilarityFor(DataClass dataclass, String orderName) {
        if (XEStoSystem.isXESListClass(dataclass)) return true;
        if (dataclass.isNESTSequentialWorkflow()) return true;
        return super.isSimilarityFor(dataclass, orderName);
    }

    @Override
    public Similarity compute(DataObject queryObject, DataObject caseObject, SimilarityValuator valuator) {
        

        ListObject queryList, caseList;

        if (XEStoSystem.isXESListClass(queryObject.getDataClass()))
            queryList = getXESListAsSystemListObject((AggregateObject) queryObject);
        else if (queryObject.isNESTSequentialWorkflow()) queryList = toList((NESTSequentialWorkflowObject) queryObject);
        else queryList = (ListObject) queryObject;

        if (XEStoSystem.isXESListClass(caseObject.getDataClass()))
            caseList = getXESListAsSystemListObject((AggregateObject) queryObject);
        else if (caseObject.isNESTSequentialWorkflow()) caseList = toList((NESTSequentialWorkflowObject) caseObject);
        else caseList = (ListObject) caseObject;

        

        

        Similarity similarity = checkStoppingCriteria(queryList, caseList);
        if (similarity != null) {
            
            return similarity;
        }

        if (containsExact()) {
            similarity = computeContainsExact(queryList, caseList, valuator, queryObject, caseObject);
        } else {
            similarity = computeContainsInexact(queryList, caseList, valuator, true, queryObject, caseObject);
        }

        
        return similarity;
    }

    private SimilarityImpl computeContainsExact(ListObject queryList, ListObject caseList, SimilarityValuator valuator, DataObject queryObject, DataObject caseObject) {
        

        // if the lists have different sizes, the similarity is 0.0
        if (queryList.size() != caseList.size()) {
            
            
            return new SimilarityImpl(this, queryObject, caseObject, 0.0);
        }

        double similaritySum = 0;
        double denominator = 0;

        ArrayList<Similarity> localSimilarities = new ArrayList<>();

        // each query element is compared to the case element at the exact position
        DataObjectIterator queryElementIterator = (queryList).iterator();
        DataObjectIterator caseElementIterator = (caseList).iterator();

        while (queryElementIterator.hasNext() && caseElementIterator.hasNext()) {
            DataObject queryElement = (DataObject) queryElementIterator.next();
            DataObject caseElement = (DataObject) caseElementIterator.next();

            

            String localSimilarityMeasure = getSimilarityMeasureFunc().apply(queryElement, caseElement);
            if (localSimilarityMeasure == null)
                localSimilarityMeasure = valuator.getSimilarityMeasure(queryElement, caseElement).getSystemName();

            

            double weight = getWeightFunc().apply(queryElement);

            

            Similarity similarity;

            if (valuator instanceof SimilarityValuatorImplExt) {
                try {
                    similarity = ((SimilarityValuatorImplExt) valuator).computeSimilarity(queryElement, caseElement, localSimilarityMeasure, getMethodInvokersFunc().apply(queryElement, caseElement));
                } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                    similarity = valuator.computeSimilarity(queryElement, caseElement, localSimilarityMeasure);
                }
            } else similarity = valuator.computeSimilarity(queryElement, caseElement, localSimilarityMeasure);

            

            similarity = new SimilarityImpl(valuator.getSimilarityModel().getSimilarityMeasure(queryElement.getDataClass(), localSimilarityMeasure), queryElement, caseElement, similarity.getValue() * weight);

            
            

            similaritySum += similarity.getValue();
            denominator += weight;

            

            localSimilarities.add(similarity);
        }

        if (denominator == 0) {
            // simCount can't be 0.0, because for empty lists this method wouldn't be called
            denominator = 1;
        }

        
        return new SimilarityImpl(this, queryObject, caseObject, similaritySum / denominator, localSimilarities);
    }

    private SimilarityImpl computeContainsInexact(ListObject largerList, ListObject smallerList, SimilarityValuator valuator, boolean queryFirst, DataObject queryObject, DataObject caseObject) {
        

        SimilarityImpl similarity = new SimilarityImpl(this, queryObject, caseObject, -1.0);

        if (largerList.size() > smallerList.size()) {
            

            double maxSimilarityValue = -1;
            for (int i = 0; i <= (largerList.size() - smallerList.size()); i++) {
                

                double similaritySum = 0;
                double denominator = 0;
                ArrayList<Similarity> localSimilarities = new ArrayList<>();
                DataObjectIterator queryElementIterator = (largerList).iterator();
                DataObjectIterator caseElementIterator = (smallerList).iterator();

                // the first elements of the query are ignored, so that there's a possible solution for each element
                for (int j = 0; j < i; j++) {
                    queryElementIterator.next();
                }
                // all possible matches are made
                while (queryElementIterator.hasNext() & caseElementIterator.hasNext()) {
                    DataObject queryElement = (DataObject) queryElementIterator.next();
                    DataObject caseElement = (DataObject) caseElementIterator.next();

                    

                    // the query has to be at the first position, because the similarity computation can be
                    // asymetric

                    double weight;
                    Similarity currentSimilarity;
                    String localSimilarityMeasure;
                    if (queryFirst) {
                        

                        localSimilarityMeasure = getSimilarityMeasureFunc().apply(queryElement, caseElement);

                        if (localSimilarityMeasure == null)
                            localSimilarityMeasure = valuator.getSimilarityMeasure(queryElement, caseElement).getSystemName();

                        

                        weight = getWeightFunc().apply(queryElement);

                        

                        if (valuator instanceof SimilarityValuatorImplExt) {
                            try {
                                currentSimilarity = ((SimilarityValuatorImplExt) valuator).computeSimilarity(queryElement, caseElement, localSimilarityMeasure, getMethodInvokersFunc().apply(queryElement, caseElement));
                            } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                                currentSimilarity = valuator.computeSimilarity(queryElement, caseElement, localSimilarityMeasure);
                            }
                        } else
                            currentSimilarity = valuator.computeSimilarity(queryElement, caseElement, localSimilarityMeasure);

                        

                        

                        currentSimilarity = new SimilarityImpl(valuator.getSimilarityModel().getSimilarityMeasure(queryElement.getDataClass(), localSimilarityMeasure), queryElement, caseElement, currentSimilarity.getValue() * weight);

                        

                    } else {
                        

                        localSimilarityMeasure = getSimilarityMeasureFunc().apply(caseElement, queryElement);

                        if (localSimilarityMeasure == null)
                            localSimilarityMeasure = valuator.getSimilarityMeasure(queryElement, caseElement).getSystemName();

                        

                        weight = getWeightFunc().apply(caseElement);

                        

                        if (valuator instanceof SimilarityValuatorImplExt) {
                            try {
                                currentSimilarity = ((SimilarityValuatorImplExt) valuator).computeSimilarity(caseElement, queryElement, localSimilarityMeasure, getMethodInvokersFunc().apply(caseElement, queryElement));
                            } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                                currentSimilarity = valuator.computeSimilarity(caseElement, queryElement, localSimilarityMeasure);
                            }
                        } else
                            currentSimilarity = valuator.computeSimilarity(caseElement, queryElement, localSimilarityMeasure);

                        

                        

                        currentSimilarity = new SimilarityImpl(valuator.getSimilarityModel().getSimilarityMeasure(caseElement.getDataClass(), localSimilarityMeasure), caseElement, queryElement, currentSimilarity.getValue() * weight);

                        

                    }
                    similaritySum += currentSimilarity.getValue();
                    denominator += weight;
                    localSimilarities.add(currentSimilarity);

                    
                }

                // adding the difference in lengths
                //                simCount += (queryObject.size()-caseObject.size());

                if (denominator == 0) {
                    denominator = 1;
                }
                // if the new computed similarity is higher than the present one, it's the new maximum value
                if ((similaritySum / denominator) > maxSimilarityValue) {
                    

                    maxSimilarityValue = (similaritySum / denominator);

                    

                    if (queryFirst) {
                        similarity = new SimilarityImpl(this, queryObject, caseObject, maxSimilarityValue, localSimilarities);
                    } else {
                        similarity = new SimilarityImpl(this, caseObject, queryObject, maxSimilarityValue, localSimilarities);
                    }
                    
                } else {
                    
                }
            }
            
            return similarity;

        } else if (largerList.size() < smallerList.size()) {
            
            // if the case is bigger than the query, the same method is called again with swapped objects,
            // so the computation was just implemented once
            similarity = computeContainsInexact(smallerList, largerList, valuator, false, caseObject, queryObject);

            

            return similarity;
        }
        
        // if both lists have the same size, they just can match exactly, so the method for the exact
        // contains is called
        similarity = computeContainsExact(largerList, smallerList, valuator, queryObject, caseObject);

        

        return similarity;
    }

}
