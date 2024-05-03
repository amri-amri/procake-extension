package de.uni_trier.wi2.extension.retrieval;

import de.uni_trier.wi2.extension.abstraction.IMethodInvokersFunc;
import de.uni_trier.wi2.extension.abstraction.ISimilarityMeasureFunc;
import de.uni_trier.wi2.extension.abstraction.IWeightFunc;
import de.uni_trier.wi2.extension.abstraction.RetrieverExt;
import de.uni_trier.wi2.extension.similarity.valuator.SimilarityValuatorImplExt;
import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.retrieval.*;
import de.uni_trier.wi2.procake.retrieval.impl.ParallelLinearRetrieverImpl;
import de.uni_trier.wi2.procake.retrieval.impl.QueryImpl;
import de.uni_trier.wi2.procake.retrieval.impl.RetrievalResultImpl;
import de.uni_trier.wi2.procake.retrieval.impl.RetrievalResultListImpl;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.SimilarityCache;
import de.uni_trier.wi2.procake.utils.concurrent.ParallelPoolProcessing;
import de.uni_trier.wi2.utils.MethodInvoker;
import de.uni_trier.wi2.utils.MethodInvokersFunc;
import de.uni_trier.wi2.utils.SimilarityMeasureFunc;
import de.uni_trier.wi2.utils.WeightFunc;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Extension of the {@link ParallelLinearRetrieverImpl} class.
 *
 * <p>This class provides functionality for using extended similarity measures
 * (see {@link ISimilarityMeasureFunc}, {@link IMethodInvokersFunc}, {@link IWeightFunc})
 * they way they are intended to.
 *
 * <p>Using an extended version of the similarity valuator implementation ({@link SimilarityValuatorImplExt})
 * in addition to {@link SimilarityMeasureFunc}-, {@link MethodInvoker}- and {@link WeightFunc}- objects, a more complex
 * parallel retrieval can be performed.
 *
 * <p>In order to properly use this retriever, five methods should be called with fitting arguments before
 * calling {@link #perform(Query)}:
 *
 * <ul>
 * <li>{@link #setGlobalSimilarityMeasure(String)}</li>
 * <li>{@link #setGlobalMethodInvokers(ArrayList)}</li>
 * <li>{@link #setLocalSimilarityMeasureFunc(SimilarityMeasureFunc)}</li>
 * <li>{@link #setLocalMethodInvokersFunc(MethodInvokersFunc)}</li>
 * <li>{@link #setLocalWeightFunc(WeightFunc)}</li>
 * </ul>
 */
public class ParallelLinearRetrieverImplExt extends ParallelLinearRetrieverImpl implements ParallelLinearRetriever<DataObject, Query>, RetrievalFactoryObject, RetrieverExt {

    private static final Logger logger = LoggerFactory.getLogger(ParallelLinearRetrieverImpl.class);
    protected ArrayList<MethodInvoker> globalMethodInvokers;
    protected SimilarityMeasureFunc localSimilarityMeasureFunc;
    protected WeightFunc localWeightFunc = (a) -> 1;
    protected MethodInvokersFunc localMethodInvokersFunc = (a, b) -> new ArrayList<MethodInvoker>();
    /**
     * The similarity cache to use
     */
    private SimilarityCache similarityCache;
    private int numberOfWorkers = DEFAULT_NUMBER_OF_WORKERS;
    private RetrievalResultListImpl retrievalResultList;
    private RetrievalResult worstRetrievalResult;
    private Query query;

    // boolean if queue should be sorted before retrieval
    private boolean sorting = DEFAULT_SORTING;
    // size for the task retrievals
    private int taskSize = DEFAULT_TASK_SIZE;

    public ParallelLinearRetrieverImplExt() {
        super();
    }

    public SimilarityCache getSimilarityCache() {
        return similarityCache;
    }

    public void setSimilarityCache(SimilarityCache similarityCache) {
        this.similarityCache = similarityCache;
    }

    public int getNumberOfWorkers() {
        return numberOfWorkers;
    }

    public void setNumberOfWorkers(int numberOfWorkers) {
        this.numberOfWorkers = numberOfWorkers;
    }

    @Override
    public int getTaskSize() {
        return taskSize;
    }

    @Override
    public void setTaskSize(int taskSize) {
        this.taskSize = taskSize;
    }

    @Override
    public boolean isSorting() {
        return sorting;
    }

    @Override
    public void setSorting(boolean sorting) {
        this.sorting = sorting;
    }

    @Override
    public Query newQuery() {
        return new QueryImpl();
    }

    @Override
    public String getGlobalSimilarityMeasure() {
        return getInternalSimilarityMeasure();
    }

    @Override
    public void setGlobalSimilarityMeasure(String similarityMeasure) {
        setInternalSimilarityMeasure(similarityMeasure);
    }

    @Override
    public ArrayList<MethodInvoker> getGlobalMethodInvokers() {
        return globalMethodInvokers;
    }

    @Override
    public void setGlobalMethodInvokers(ArrayList<MethodInvoker> methodInvokers) {
        globalMethodInvokers = methodInvokers;
    }

    @Override
    public SimilarityMeasureFunc getLocalSimilarityMeasureFunc() {
        return localSimilarityMeasureFunc;
    }

    @Override
    public void setLocalSimilarityMeasureFunc(SimilarityMeasureFunc similarityMeasureFunc) {
        this.localSimilarityMeasureFunc = similarityMeasureFunc;
    }

    @Override
    public MethodInvokersFunc getLocalMethodInvokersFunc() {
        return localMethodInvokersFunc;
    }

    @Override
    public void setLocalMethodInvokersFunc(MethodInvokersFunc methodInvokersFunc) {
        this.localMethodInvokersFunc = methodInvokersFunc;
    }

    @Override
    public WeightFunc getLocalWeightFunc() {
        return localWeightFunc;
    }

    @Override
    public void setLocalWeightFunc(WeightFunc weightFunc) {
        this.localWeightFunc = weightFunc;
    }

    @Override
    public String getRetrieverName() {
        return super.getRetrieverName() + "Ext";
    }


    @Override
    public RetrievalResultList perform(Query query) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // set query and retrieval result list
        retrievalResultList = new RetrievalResultListImpl();
        this.query = query;

        // create instance
        ParallelPoolProcessing poolProcessing = new ParallelPoolProcessing(getObjectPool(), taskSize, sorting, numberOfWorkers);

        if (globalMethodInvokers != null) {
            globalMethodInvokers.add(new MethodInvoker("setSimilarityMeasureFunc", new Class[]{SimilarityMeasureFunc.class}, new Object[]{getLocalSimilarityMeasureFunc()}));
            globalMethodInvokers.add(new MethodInvoker("setMethodInvokersFunc", new Class[]{MethodInvokersFunc.class}, new Object[]{getLocalMethodInvokersFunc()}));
            globalMethodInvokers.add(new MethodInvoker("setWeightFunc", new Class[]{WeightFunc.class}, new Object[]{getLocalWeightFunc()}));
        }

        // add one worker per thread
        for (int i = 0; i < poolProcessing.getNumberOfWorkers(); i++) {
            poolProcessing.addProcessingTask(new PLRProcessingTask(this, query));
        }

        // start parallel processing
        poolProcessing.processAndWait();

        stopWatch.stop();
        retrievalResultList.setRetrievalTime(stopWatch.getNanoTime());

        if (isAddQueryToResults()) {
            retrievalResultList.setQuery(query);
        }

        worstRetrievalResult = null;

        return retrievalResultList;
    }

    private synchronized void addRetrievalResults(List<RetrievalResult> threadRetrievalResults) {

        if (retrievalResultList.size() == 0 && threadRetrievalResults.size() <= query.getNumberOfResults()) {
            retrievalResultList = new RetrievalResultListImpl();
            for (RetrievalResult retrievalResult : threadRetrievalResults) {
                retrievalResultList.add(retrievalResult);
            }
        } else {
            for (RetrievalResult result : threadRetrievalResults) {
                if (retrievalResultList.size() >= query.getNumberOfResults()) {
                    if (retrievalResultList.getLast().getSimilarity().isLessThan(result.getSimilarity())) {
                        retrievalResultList.removeLast();
                    } else {
                        continue;
                    }
                }
                retrievalResultList.add(result);
            }
        }

        // update worst retrieval result
        if (retrievalResultList.size() == query.getNumberOfResults()) {
            worstRetrievalResult = retrievalResultList.getLast();
        }

        logger.trace("Added {} retrieval results", threadRetrievalResults.size());
    }


    private static class PLRProcessingTask implements ParallelPoolProcessing.ProcessingTask {

        final ParallelLinearRetrieverImplExt parallelRetriever;
        Query query;
        SimilarityValuatorImplExt simVal;
        SimilarityCache similarityCache;

        public PLRProcessingTask(ParallelLinearRetrieverImplExt parallelRetriever, Query query) {
            this.parallelRetriever = parallelRetriever;
            this.query = query;
            simVal = new SimilarityValuatorImplExt(parallelRetriever.getSimilarityModel());
            similarityCache = parallelRetriever.getSimilarityCache();
        }

        @Override
        public void process(DataObject[] elements, int iteration) {
            List<RetrievalResult> retrievalResults = new ArrayList<>();

            for (DataObject nextElement : elements) {

                Similarity sim = null;

                if (similarityCache != null) {
                    sim = similarityCache.getSimilarity(query.getQueryObject(), nextElement);
                }
                if (sim == null) {
                    // compute similarity
                    try {
                        String internalSimilarityMeasure;
                        ArrayList<MethodInvoker> globalMethodInvokers;

                        internalSimilarityMeasure = parallelRetriever.getInternalSimilarityMeasure();
                        globalMethodInvokers = parallelRetriever.globalMethodInvokers;

                        sim = simVal.computeSimilarity(query.getQueryObject(), nextElement, internalSimilarityMeasure, globalMethodInvokers);
                    } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                        sim = simVal.computeSimilarity(query.getQueryObject(), nextElement, parallelRetriever.getInternalSimilarityMeasure());
                    }
                    if (similarityCache != null) {
                        similarityCache.setSimilarity(query.getQueryObject(), nextElement, sim);
                    }
                }

                if (sim.isValidValue()) {
                    if (parallelRetriever.worstRetrievalResult == null || parallelRetriever.worstRetrievalResult.getSimilarity().isLessThan(sim)) {
                        RetrievalResultImpl retrievalResult = new RetrievalResultImpl();
                        retrievalResult.setSimilarity(sim);
                        retrievalResult.setResultId(nextElement.getId());

                        if (query.isRetrieveCases()) {
                            retrievalResult.setResultObject(nextElement);
                        }

                        // only add to retrieval results if they can possibly get into the final results
                        retrievalResults.add(retrievalResult);
                    }
                }
            }

            if (!retrievalResults.isEmpty()) {
                parallelRetriever.addRetrievalResults(retrievalResults);
            }
        }
    }


}
