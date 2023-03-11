package extension;

import de.uni_trier.wi2.procake.data.model.DataClass;
import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.data.object.base.CollectionObject;
import de.uni_trier.wi2.procake.data.objectpool.DataObjectIterator;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.SimilarityMeasure;
import de.uni_trier.wi2.procake.similarity.SimilarityValuator;
import de.uni_trier.wi2.procake.similarity.base.collection.SMCollectionMapping;
import de.uni_trier.wi2.procake.similarity.base.collection.impl.SMCollectionImpl;
import de.uni_trier.wi2.procake.similarity.base.collection.impl.SMCollectionMappingImpl;
import de.uni_trier.wi2.procake.similarity.impl.SimilarityImpl;
import org.apache.commons.collections4.map.MultiKeyMap;
import utils.SimFunc;
import utils.WeightFunc;

import java.security.InvalidParameterException;
import java.util.*;

public class SMCollectionMappingImplExt extends SMCollectionMappingImpl implements SMCollectionMappingExt {
    protected SimFunc similarityToUseFunc;
    protected WeightFunc weightFunc = (a, b) -> 1;

    @Override
    public void setSimilarityToUse(String newValue) {
        super.setSimilarityToUse(newValue);
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
        this.weightFunc = (a, b) -> {
            Double weight = weightFunc.apply(a, b);
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


    /**
     * uniqueID-counter
     */
    private int IDCounter = 0;

    /**
     * keeps a list of maximum similarities, which can be achieved if there would be no mapping
     * involved
     */
    private Map<DataObject, Double> maxQueryItemSimilarities = new HashMap<>();

    /**
     * Defines the maximum size of the queue that stores the (partial) solutions of the algorithm. If
     * the current queue grows larger than the set maximum, the lowest ranked solutions will be
     * removed. Setting a negative value disables pruning of the queue.
     */
    private int maxQueueSize = DEFAULT_MAX_QUEUE_SIZE;

    /**
     * This cache stores calculated mappings of collection items.
     */
    private MultiKeyMap<DataObject, Similarity> mappingCache;




    @Override
    public Similarity compute(
            DataObject queryObject, DataObject caseObject, SimilarityValuator valuator) {
        // init cache
        mappingCache = new MultiKeyMap<>();

        Similarity similarity = checkStoppingCriteria(queryObject, caseObject);
        if (similarity != null) {
            return similarity;
        }

        // read the QSize set in sim.xml
//    if (!queueSizeChecked) {
//      readQSizeSimilaritySetting();
//    }

        // initialize the first solution
        TreeSet<AStarSolution> solutions = new TreeSet<>();
        solutions.add(
                generateInitialSolution(
                        (CollectionObject) queryObject, (CollectionObject) caseObject, valuator));

        // iterate as long as we dont find a finished solution
        AStarSolution topSolution = solutions.first();
        int sizeQueryObjects = ((CollectionObject) queryObject).size();
        do {
            topSolution = solutions.pollFirst();
            TreeSet<AStarSolution> newSolutions =
                    (TreeSet<AStarSolution>) expandSolution(topSolution, valuator);
            solutions.addAll(newSolutions);
            topSolution = solutions.first();
            cutOffQueue(solutions);
        } while (topSolution.mapping.size() < sizeQueryObjects);

        // transforming to a SimilarityImpl object
        ArrayList<Similarity> localSimilarities = new ArrayList<>();
        for (AStarMap mapping : topSolution.mapping) {
            localSimilarities.add(mapping.f);
        }
        return new SimilarityImpl(
                this, queryObject, caseObject, topSolution.f.getValue(), localSimilarities);
    }

    private void cutOffQueue(TreeSet<AStarSolution> solutions) {
        Iterator<AStarSolution> iter = solutions.descendingIterator();
        // if maxQueueSize is negative, the pruning of the queue is disabled
        if (getMaxQueueSize() >= 1) {
            while (solutions.size() > getMaxQueueSize()) {
                iter.next();
                iter.remove();
            }
        }
    }

    /**
     * expands the next queryItem
     */
    private Set<AStarSolution> expandSolution(AStarSolution solution, SimilarityValuator valuator) {

        Set<AStarSolution> newSolutions = new TreeSet<>();
        for (DataObject queryItemToExpand : solution.queryCollection) {
            // the next queryItem is just next item in the list
            if (!solution.containsQuery(queryItemToExpand)) {
                // we remove the previously used approx for this item
                if (solution.caseCollection.size() > 0) {
                    for (DataObject curCaseDO : solution.caseCollection) {
                        Similarity subSim = mappingCache.get(queryItemToExpand, curCaseDO);
                        AStarMap newMapping = new AStarMap();
                        newMapping.queryItem = queryItemToExpand;
                        newMapping.caseItem = curCaseDO;
                        newMapping.f = subSim;

                        AStarSolution newSolution = new AStarSolution(solution);
                        newSolution.caseCollection.remove(curCaseDO);
                        newSolution.mapping.add(newMapping);
                        newSolution.g_Numerator += newMapping.f.getValue();
                        newSolution.h_Numerator =
                                solution.h_Numerator - maxQueryItemSimilarities.get(newMapping.queryItem);

                        calcFValue(newSolution);
                        newSolutions.add(newSolution);
                    }
                } else {
                    // if the queryItem could not be mapped (due to insufficient caseItems), we just carry on
                    // the current
                    // solution
                    AStarMap newMapping = new AStarMap();
                    newMapping.queryItem = queryItemToExpand;
                    newMapping.caseItem = null;
                    newMapping.f = new SimilarityImpl(this, newMapping.queryItem, newMapping.caseItem, 0.0);
                    solution.mapping.add(newMapping);
                    solution.h_Numerator =
                            solution.h_Numerator - maxQueryItemSimilarities.get(newMapping.queryItem);

                    calcFValue(solution);

                    newSolutions.add(solution);
                }
            }
        }

        return newSolutions;
    }

    /**
     * creates the starting solution
     */
    private AStarSolution generateInitialSolution(
            CollectionObject queryObject, CollectionObject caseObject, SimilarityValuator valuator) {

        AStarSolution initialSolution = new AStarSolution();

        initialSolution.queryCollection = new LinkedList<>();
        initialSolution.caseCollection = new LinkedList<>();
        initialSolution.mapping = new HashSet<>();

        // transform CAKE collections to Java-collections (easier handling)
        DataObjectIterator itC = caseObject.iterator();
        while (itC.hasNext()) {
            initialSolution.caseCollection.add((DataObject) itC.next());
        }
        DataObjectIterator itQ = queryObject.iterator();
        while (itQ.hasNext()) {
            DataObject curQDO = (DataObject) itQ.next();
            initialSolution.queryCollection.add(curQDO);

            // calc max similarity per queryItem (necessary for A* II heuristic approximation)
            // also fill cache
            double maxQSim = getMaxSimilarity(curQDO, initialSolution.caseCollection, valuator);
            maxQueryItemSimilarities.put(curQDO, maxQSim);
            initialSolution.h_Numerator += maxQSim;
        }

        initialSolution.g_h_Denominator = initialSolution.queryCollection.size();

        calcFValue(initialSolution);

        return initialSolution;
    }

    /**
     * retrieves the next ID
     */
    private int getNextID() {
        return IDCounter++;
    }

    /**
     * calculates the maximum similarity for the queryItem among the given caseItems
     */
    private double getMaxSimilarity(
            DataObject queryItem, List<DataObject> amongTheseCaseItems, SimilarityValuator valuator) {

        double maxSim = 0;
        for (DataObject curCaseDO : amongTheseCaseItems) {
            Similarity curSim = valuator.computeSimilarity(queryItem, curCaseDO, getSimilarityToUse());
            // fill cache
            mappingCache.put(queryItem, curCaseDO, curSim);
            double simValue = curSim.getValue();
            if (simValue > maxSim) {
                maxSim = simValue;
            }
        }

        return maxSim;
    }

    /**
     * f = g + h
     *
     * @param solution
     */
    private void calcFValue(AStarSolution solution) {
        if (solution.g_h_Denominator > 0) {
            solution.f =
                    new SimilarityImpl(
                            this,
                            null,
                            null,
                            (solution.g_Numerator + solution.h_Numerator) / solution.g_h_Denominator);
        }
    }

    /**
     * calculates the new h-numerator for the solution (sum of yet unmapped queryItems)
     */
    private double getHNumerator(AStarSolution solution) {
        double newValue = 0;
        for (DataObject curItem : solution.queryCollection) {
            if (!solution.containsQuery(curItem)) {
                newValue +=
                        maxQueryItemSimilarities.get(curItem); // only for the querys, that aren't mapped yet
            }
        }

        return newValue;
    }


    @Override
    // it's necessary to override this method, because otherwise the maxQueueSize would be returned to
    // the default value
    protected void initializeBasedOn(SimilarityMeasure base) {
        super.initializeBasedOn(base);
        this.setMaxQueueSize(((SMCollectionMapping) base).getMaxQueueSize());
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

        public DataObject queryItem = null;
        public DataObject caseItem = null;
    }

    /**
     * represents a possible solution for a mapping scenario
     */
    public class AStarSolution extends AStarComparable {

        /**
         * list of yet unmapped queryItems
         */
        public List<DataObject> queryCollection = null;
        /**
         * list of yet unmapped caseItems
         */
        public List<DataObject> caseCollection = null;
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
        public AStarSolution(AStarSolution oldSolution) {
            this();

            this.queryCollection = new LinkedList<>(oldSolution.queryCollection);
            this.caseCollection = new LinkedList<>(oldSolution.caseCollection);
            this.mapping = new TreeSet<>(oldSolution.mapping);
            this.g_h_Denominator = oldSolution.g_h_Denominator;
            this.g_Numerator += oldSolution.g_Numerator;
            this.h_Numerator += oldSolution.h_Numerator;
        }

        private boolean containsQuery(DataObject queryItem) {
            for (AStarMap map : mapping) {
                if (map.queryItem == queryItem) {
                    return true;
                }
            }
            return false;
        }
    }
}
