package de.uni_trier.wi2.extension.retrieval;

import de.uni_trier.wi2.extension.abstraction.IMethodInvokersFunc;
import de.uni_trier.wi2.extension.abstraction.ISimilarityMeasureFunc;
import de.uni_trier.wi2.extension.abstraction.IWeightFunc;
import de.uni_trier.wi2.extension.abstraction.RetrieverExt;
import de.uni_trier.wi2.extension.similarity.valuator.SimilarityValuatorImplExt;
import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.data.objectpool.DataObjectIterator;
import de.uni_trier.wi2.procake.retrieval.Query;
import de.uni_trier.wi2.procake.retrieval.RetrievalFactoryObject;
import de.uni_trier.wi2.procake.retrieval.RetrievalResultList;
import de.uni_trier.wi2.procake.retrieval.Retriever;
import de.uni_trier.wi2.procake.retrieval.impl.LinearRetrieverImpl;
import de.uni_trier.wi2.procake.retrieval.impl.RetrievalResultImpl;
import de.uni_trier.wi2.procake.retrieval.impl.RetrievalResultListImpl;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.SimilarityCache;
import de.uni_trier.wi2.procake.similarity.SimilarityModel;
import de.uni_trier.wi2.procake.similarity.SimilarityValuator;
import de.uni_trier.wi2.utils.MethodInvoker;
import de.uni_trier.wi2.utils.MethodInvokersFunc;
import de.uni_trier.wi2.utils.SimilarityMeasureFunc;
import de.uni_trier.wi2.utils.WeightFunc;
import org.apache.commons.lang3.time.StopWatch;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

/**
 * Extension of the {@link LinearRetrieverImpl} class.
 *
 * <p>This class provides functionality for using extended similarity measures
 * (see {@link ISimilarityMeasureFunc}, {@link IMethodInvokersFunc}, {@link IWeightFunc})
 * they way they are intended to.
 *
 * <p>Using an extended version of the similarity valuator implementation ({@link SimilarityValuatorImplExt})
 * in addition to {@link SimilarityMeasureFunc}-, {@link MethodInvoker}- and {@link WeightFunc}- objects, a more complex retrieval
 * can be performed.
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
public class LinearRetrieverImplExt extends LinearRetrieverImpl implements Retriever, RetrievalFactoryObject, RetrieverExt {
//todo: implements Retriever<..., ...>
    /**
     * The extended version of the similarity valuator implementation
     */
    protected SimilarityValuatorImplExt valuator;

    /**
     * The list of global {@link MethodInvoker}s to set parameters of the global similarity measure
     */
    protected ArrayList<MethodInvoker> globalMethodInvokers = new ArrayList<MethodInvoker>();

    /**
     * The functional interface assigning similarity measures to pairs of data objects
     */
    protected SimilarityMeasureFunc localSimilarityMeasureFunc = (a, b) -> null;

    /**
     * The functional interface assigning weight values to pairs of data objects
     */
    protected WeightFunc localWeightFunc = (a) -> 1;

    /**
     * The functional interface assigning methods to pairs of data objects. The methods
     * are to be invoked on the similarity measures assigned to the same pairs of data objects.
     */
    protected MethodInvokersFunc localMethodInvokersFunc = (a, b) -> new ArrayList<MethodInvoker>();

    protected SimilarityCache similarityCache;

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
    public SimilarityValuator getValuator() {
        return valuator;
    }

    public SimilarityCache getSimilarityCache() {
        return similarityCache;
    }

    public void setSimilarityCache(SimilarityCache similarityCache) {
        this.similarityCache = similarityCache;
    }

    @Override
    public void setSimilarityModel(SimilarityModel similarityModel) {
        super.setSimilarityModel(similarityModel);
        valuator = new SimilarityValuatorImplExt(getSimilarityModel());
    }

    @Override
    public RetrievalResultList perform(Query query) {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        RetrievalResultListImpl rrl = new RetrievalResultListImpl();

        if (isAddQueryToResults()) {
            rrl.setQuery(query);
        }

        RetrievalResultImpl rr;
        DataObject queryObject = query.getQueryObject();
        StopWatch stopWatchLocal = new StopWatch();

        MethodInvokersFunc methodInvokersFunc = getLocalMethodInvokersFunc();

        setLocalMethodInvokersFunc(new MethodInvokersFunc() {
            @Override
            public ArrayList<MethodInvoker> apply(DataObject q, DataObject c) {
                ArrayList<MethodInvoker> methodInvokers = methodInvokersFunc.apply(q, c);
                if (methodInvokers == null) methodInvokers = new ArrayList<>();
                methodInvokers.add(new MethodInvoker("setSimilarityMeasureFunc", new Class[]{SimilarityMeasureFunc.class}, new Object[]{getLocalSimilarityMeasureFunc()}));
                methodInvokers.add(new MethodInvoker("setMethodInvokersFunc", new Class[]{MethodInvokersFunc.class}, new Object[]{this}));
                methodInvokers.add(new MethodInvoker("setWeightFunc", new Class[]{WeightFunc.class}, new Object[]{getLocalWeightFunc()}));
                return methodInvokers;
            }
        });


        globalMethodInvokers.add(new MethodInvoker("setSimilarityMeasureFunc", new Class[]{SimilarityMeasureFunc.class}, new Object[]{getLocalSimilarityMeasureFunc()}));
        globalMethodInvokers.add(new MethodInvoker("setMethodInvokersFunc", new Class[]{MethodInvokersFunc.class}, new Object[]{getLocalMethodInvokersFunc()}));
        globalMethodInvokers.add(new MethodInvoker("setWeightFunc", new Class[]{WeightFunc.class}, new Object[]{getLocalWeightFunc()}));

        DataObjectIterator iter = getObjectPool().iterator();
        while (iter.hasNext()) {
            DataObject caseObject = iter.nextDataObject();

            Similarity sim = null;
            stopWatchLocal.start();

            if (similarityCache != null) {
                sim = similarityCache.getSimilarity(queryObject, caseObject);
            }
            if (sim == null) {
                try {
                    sim = valuator.computeSimilarity(queryObject, caseObject,
                            this.getInternalSimilarityMeasure(), globalMethodInvokers);
                } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                    sim = valuator.computeSimilarity(queryObject, caseObject,
                            this.getInternalSimilarityMeasure());
                }

                if (similarityCache != null) {
                    similarityCache.setSimilarity(queryObject, caseObject, sim);
                }
            }

            stopWatchLocal.stop();
            if (sim.isValidValue()) {
                if (sim.getValue() >= query.getMinSimilarity()) {
                    if (rrl.size() >= query.getNumberOfResults()) {
                        if (rrl.getLast() != null && rrl.getLast().getSimilarity().isLessThan(sim)) {
                            rrl.removeLast();
                        } else {
                            stopWatchLocal.reset();
                            continue;
                        }
                    }

                    rr = new RetrievalResultImpl();
                    rr.setSimilarity(sim);
                    rr.setRetrievalTime(stopWatchLocal.getNanoTime());

                    rr.setResultId(caseObject.getId());
                    if (query.isRetrieveCases()) {
                        rr.setResultObject(caseObject);
                    }
                    rrl.add(rr);
                }
            }
            stopWatchLocal.reset();
        }

        stopWatch.stop();
        rrl.setRetrievalTime(stopWatch.getNanoTime());
        stopWatch.reset();

        return rrl;

    }


}
