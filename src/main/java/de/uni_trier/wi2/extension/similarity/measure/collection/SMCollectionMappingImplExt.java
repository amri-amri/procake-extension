package de.uni_trier.wi2.extension.similarity.measure.collection;

import de.uni_trier.wi2.extension.abstraction.IMethodInvokersFunc;
import de.uni_trier.wi2.extension.abstraction.INESTtoList;
import de.uni_trier.wi2.extension.abstraction.ISimilarityMeasureFunc;
import de.uni_trier.wi2.extension.abstraction.IWeightFunc;
import de.uni_trier.wi2.extension.similarity.valuator.SimilarityValuatorImplExt;
import de.uni_trier.wi2.procake.data.model.DataClass;
import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.data.object.base.AggregateObject;
import de.uni_trier.wi2.procake.data.object.base.CollectionObject;
import de.uni_trier.wi2.procake.data.object.nest.NESTSequentialWorkflowObject;
import de.uni_trier.wi2.procake.data.objectpool.DataObjectIterator;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.SimilarityMeasure;
import de.uni_trier.wi2.procake.similarity.SimilarityValuator;
import de.uni_trier.wi2.procake.similarity.base.collection.SMCollectionMapping;
import de.uni_trier.wi2.procake.similarity.base.collection.impl.SMCollectionMappingImpl;
import de.uni_trier.wi2.procake.similarity.impl.SimilarityImpl;
import de.uni_trier.wi2.utils.*;
import org.apache.commons.collections4.map.MultiKeyMap;

import java.lang.reflect.InvocationTargetException;
import java.util.*;


import static de.uni_trier.wi2.utils.XEStoSystem.getXESListAsSystemListObject;

/**
 * A similarity measure using the 'Mapping' algorithm for {@link CollectionObject}s.
 *
 * <p>The 'Mapping' algorithm assigns to each element of the query collection a different
 * case element so that the overall similarity is the highest.
 *
 * <p>The overall similarity between query and case collection is the sum of the local weighted
 * similarities divided by the sum of the weights.
 *
 * <p>The weight values depend solely on the characteristics of the query elements and can
 * be defined by a functional interface ({@link WeightFunc}).
 *
 * <p>For more info on the algorithm <a href="https://wi2.pages.gitlab.rlp.net/procake/procake-wiki/sim/collections/#mapping">click here</a>.
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
 */
public class SMCollectionMappingImplExt extends SMCollectionMappingImpl implements SMCollectionMappingExt, INESTtoList, ISimilarityMeasureFunc, IWeightFunc, IMethodInvokersFunc {

    /**
     * keeps a list of maximum similarities, which can be achieved if there would be no mapping
     * involved
     */
    private final Map<DataObject, Double> maxQueryElementSimilarityValues = new HashMap<>();
    protected SimilarityMeasureFunc similarityMeasureFunc = (a, b) -> null;
    protected MethodInvokersFunc methodInvokersFunc = (a, b) -> new ArrayList<>();
    protected WeightFunc weightFunc = (a) -> 1;
    /**
     * This cache stores calculated mappings of collection items.
     */
    private MultiKeyMap<DataObject, Similarity> mappingCache;
    /**
     * This cache stores calculated weights of collection items.
     */
    private Map<DataObject, Double> weightCache;
    /**
     * uniqueID-counter
     */
    private int IDCounter = 0;

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
        
        
        return SMCollectionMappingExt.NAME;
    }

    @Override
    public boolean isSimilarityFor(DataClass dataclass, String orderName) {
        if (XEStoSystem.isXESListClass(dataclass)) return true;
        if (dataclass.isNESTSequentialWorkflow()) return true;
        return super.isSimilarityFor(dataclass, orderName);
    }

    @Override
    public Similarity compute(DataObject queryObject, DataObject caseObject, SimilarityValuator valuator) {
        

        CollectionObject queryCollection, caseCollection;

        if (XEStoSystem.isXESListClass(queryObject.getDataClass()))
            queryCollection = getXESListAsSystemListObject((AggregateObject) queryObject);
        else if (queryObject.isNESTSequentialWorkflow())
            queryCollection = toList((NESTSequentialWorkflowObject) queryObject);
        else queryCollection = (CollectionObject) queryObject;

        if (XEStoSystem.isXESListClass(caseObject.getDataClass()))
            caseCollection = getXESListAsSystemListObject((AggregateObject) queryObject);
        else if (caseObject.isNESTSequentialWorkflow())
            caseCollection = toList((NESTSequentialWorkflowObject) caseObject);
        else caseCollection = (CollectionObject) caseObject;

        

        // init cache
        mappingCache = new MultiKeyMap<>();
        weightCache = new HashMap<>();

        

        Similarity similarity = checkStoppingCriteria(queryCollection, caseCollection);
        if (similarity != null) {
            
            return similarity;
        }

        // initialize the first solution
        TreeSet<AStarSolution> solutions = new TreeSet<>();
        solutions.add(generateInitialSolution(queryCollection, caseCollection, valuator));


        // iterate as long as we don't find a finished solution
        AStarSolution topSolution; // == open list
        int sizeQueryObject = queryCollection.size();

        

        do {
            //

            topSolution = solutions.pollFirst();

            

            TreeSet<AStarSolution> newSolutions = (TreeSet<AStarSolution>) expandSolution(topSolution);
            solutions.addAll(newSolutions);

            topSolution = solutions.first();
            cutOffQueue(solutions);
        } while (topSolution.mapping.size() < sizeQueryObject);

        // transforming to a SimilarityImpl object
        ArrayList<Similarity> localSimilarities = new ArrayList<>();
        for (AStarMap mapping : topSolution.mapping) {
            localSimilarities.add(mapping.f);
        }
        return new SimilarityImpl(this, queryObject, caseObject, topSolution.f.getValue(), localSimilarities);
    }

    private void cutOffQueue(TreeSet<AStarSolution> solutions) {
        
        Iterator<AStarSolution> aStarSolutionIterator = solutions.descendingIterator();
        AStarSolution toBeRemoved;
        // if maxQueueSize is negative, the pruning of the queue is disabled
        if (getMaxQueueSize() >= 1) {
            while (solutions.size() > getMaxQueueSize()) {
                toBeRemoved = aStarSolutionIterator.next();
                aStarSolutionIterator.remove();

            }
        }
        
    }

    /**
     * expands the next queryItem
     */
    private Set<AStarSolution> expandSolution(AStarSolution solution) {
        

        Set<AStarSolution> newSolutions = new TreeSet<>();
        for (DataObject queryElement : solution.queryElements) {
            // the next queryItem is just the next item in the list

            

            if (!solution.containsQuery(queryElement)) {
                // we remove the previously used approx for this item
                if (solution.caseElements.size() > 0) {

                    

                    for (DataObject caseElement : solution.caseElements) {

                        

                        Similarity similarity = mappingCache.get(queryElement, caseElement);
                        AStarMap newMapping = new AStarMap();
                        newMapping.queryElement = queryElement;
                        newMapping.caseElement = caseElement;
                        newMapping.f = similarity;

                        AStarSolution newSolution = new AStarSolution(solution);
                        newSolution.caseElements.remove(caseElement);
                        newSolution.mapping.add(newMapping);
                        newSolution.g_Numerator += newMapping.f.getValue();
                        newSolution.h_Numerator = solution.h_Numerator - maxQueryElementSimilarityValues.get(newMapping.queryElement);

                        calcFValue(newSolution);
                        newSolutions.add(newSolution);
                    }
                } else {

                    

                    // if the queryItem could not be mapped (due to insufficient caseItems), we just carry on
                    // the current solution
                    AStarMap newMapping = new AStarMap();
                    newMapping.queryElement = queryElement;
                    newMapping.caseElement = null;
                    newMapping.f = new SimilarityImpl(this, newMapping.queryElement, newMapping.caseElement, 0.);
                    solution.mapping.add(newMapping);
                    solution.h_Numerator = solution.h_Numerator - maxQueryElementSimilarityValues.get(newMapping.queryElement);
                    solution.g_Numerator = solution.g_Numerator + 0;

                    calcFValue(solution);

                    newSolutions.add(solution);
                }
            } else {
                
            }
        }

        
        return newSolutions;
    }

    /**
     * creates the starting solution
     */
    private AStarSolution generateInitialSolution(CollectionObject queryObject, CollectionObject caseObject, SimilarityValuator valuator) {
        

        AStarSolution initialSolution = new AStarSolution();

        initialSolution.queryElements = new LinkedList<>();
        initialSolution.caseElements = new LinkedList<>();
        initialSolution.mapping = new HashSet<>();

        // transform CAKE collections to Java-collections (easier handling)
        DataObjectIterator caseElementIterator = caseObject.iterator();
        while (caseElementIterator.hasNext()) {
            initialSolution.caseElements.add((DataObject) caseElementIterator.next());
        }
        DataObjectIterator queryElementIterator = queryObject.iterator();
        while (queryElementIterator.hasNext()) {
            DataObject queryElement = (DataObject) queryElementIterator.next();
            initialSolution.queryElements.add(queryElement);
            double weight = getWeightFunc().apply(queryElement);
            weightCache.put(queryElement, weight);

            // calc max similarity per queryItem (necessary for A* II heuristic approximation)
            // also fill cache
            double maxSimilarityValue = getMaxSimilarity(queryElement, initialSolution.caseElements, valuator);
            maxQueryElementSimilarityValues.put(queryElement, maxSimilarityValue);
            initialSolution.h_Numerator += maxSimilarityValue;
            initialSolution.g_h_Denominator += weight;
        }

        calcFValue(initialSolution);

        

        
        return initialSolution;
    }

    /**
     * calculates the maximum similarity for the queryItem among the given caseItems
     */
    private double getMaxSimilarity(DataObject queryElement, List<DataObject> caseElements, SimilarityValuator valuator) {

        double maxSimilarityValue = 0;

        double weight = weightCache.get(queryElement);

        for (DataObject caseElement : caseElements) {
            String localSimilarityMeasure = getSimilarityMeasureFunc().apply(queryElement, caseElement);
            if (localSimilarityMeasure == null)
                localSimilarityMeasure = valuator.getSimilarityMeasure(queryElement, caseElement).getSystemName();

            Similarity similarity;
            if (valuator instanceof SimilarityValuatorImplExt) {
                try {
                    similarity = ((SimilarityValuatorImplExt) valuator).computeSimilarity(queryElement, caseElement, localSimilarityMeasure, getMethodInvokersFunc().apply(queryElement, caseElement));
                } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                    similarity = valuator.computeSimilarity(queryElement, caseElement, localSimilarityMeasure);
                }
            } else similarity = valuator.computeSimilarity(queryElement, caseElement, localSimilarityMeasure);

            similarity = new SimilarityImpl(valuator.getSimilarityModel().getSimilarityMeasure(queryElement.getDataClass(), localSimilarityMeasure), queryElement, caseElement, weight * similarity.getValue());

            // fill cache
            mappingCache.put(queryElement, caseElement, similarity);
            double similarityValue = similarity.getValue();
            if (similarityValue > maxSimilarityValue) {
                maxSimilarityValue = similarityValue;
            }
        }

        return maxSimilarityValue;
    }

    /**
     * f = g + h
     *
     * @param solution
     */
    private void calcFValue(AStarSolution solution) {
        
        if (solution.g_h_Denominator > 0) {
            

            solution.f = new SimilarityImpl(this, null, null, (solution.g_Numerator + solution.h_Numerator) / solution.g_h_Denominator);

            
        } else {
            
        }
    }

    @Override
    // it's necessary to override this method, because otherwise the maxQueueSize would be returned to
    // the default value
    protected void initializeBasedOn(SimilarityMeasure base) {
        
        super.initializeBasedOn(base);
        this.setMaxQueueSize(((SMCollectionMapping) base).getMaxQueueSize());
    }

    /**
     * retrieves the next ID
     */
    private int getNextID() {
        
        return IDCounter++;
    }

    /**
     * represents an ordered AStar-item
     */
    private abstract class AStarComparable implements Comparable<AStarComparable> {

        /**
         * keeps unique ID for this item
         */
        public int itemID = getNextID();

        /**
         * keeps similarity
         */
        public Similarity f = new SimilarityImpl(null, null, null, 0);

        @Override
        public int compareTo(AStarComparable o) {
            if (o.f.getValue() > f.getValue() || (o.f.getValue() == f.getValue() && o.itemID < itemID)) {
                return 1;
            }

            if (o.f.getValue() < f.getValue() || (o.f.getValue() == f.getValue() && o.itemID > itemID)) {
                return -1;
            }

            return 0;
        }
    }

    /**
     * represents a mapping from a queryItem to a caseItem
     */
    public class AStarMap extends AStarComparable {

        public DataObject queryElement = null;
        public DataObject caseElement = null;
    }

    /**
     * represents a possible solution for a mapping scenario
     */
    public class AStarSolution extends AStarComparable {

        /**
         * list of yet unmapped queryItems
         */
        public List<DataObject> queryElements = null;
        /**
         * list of yet unmapped caseItems
         */
        public List<DataObject> caseElements = null;
        /**
         * list of mappings until now
         */
        public Set<AStarMap> mapping = null;
        /**
         * sim-calc: the numerator of g (refer to A-Star for more detail)
         */
        public double g_Numerator = 0;
        /**
         * sim-calc: the denominator of g and h (refer to A-Star for more detail)
         */
        public double g_h_Denominator = 0;
        /**
         * sim-calc: the numerator of h (refer to A-Star for more detail)
         */
        public double h_Numerator = 0;

        public AStarSolution() {
            super();
        }

        /**
         * copies the content of the submitted solution (convenience method)
         */
        public AStarSolution(AStarSolution solution) {
            this();

            this.queryElements = new LinkedList<>(solution.queryElements);
            this.caseElements = new LinkedList<>(solution.caseElements);
            this.mapping = new TreeSet<>(solution.mapping);
            this.g_h_Denominator = solution.g_h_Denominator;
            this.g_Numerator += solution.g_Numerator;
            this.h_Numerator += solution.h_Numerator;
        }

        private boolean containsQuery(DataObject queryElement) {
            for (AStarMap map : mapping) {
                if (map.queryElement == queryElement) {
                    return true;
                }
            }
            return false;
        }
    }

}
