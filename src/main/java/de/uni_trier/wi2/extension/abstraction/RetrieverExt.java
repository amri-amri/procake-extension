package de.uni_trier.wi2.extension.abstraction;

import de.uni_trier.wi2.procake.retrieval.impl.LinearRetrieverImpl;
import de.uni_trier.wi2.utils.MethodInvoker;
import de.uni_trier.wi2.utils.MethodInvokersFunc;
import de.uni_trier.wi2.utils.SimilarityMeasureFunc;
import de.uni_trier.wi2.utils.WeightFunc;

import java.util.ArrayList;

/**
 * An interface for every Retriever using {@link IMethodInvokersFunc}-, {@link ISimilarityMeasureFunc}- or
 * {@link IWeightFunc}-objects.
 *
 * <p>A class like {@link LinearRetrieverImpl} does not provide enough functionality to handle different
 * similarity measures, weights and similarity measure parameters on multiple levels/depths
 * (a collection object can again contain collection objects as elements and so forth and
 * one may want to use different similarity measures on different levels etc.).
 *
 * <p>This interface makes sure that a retriever implementing it will be able to do so.
 *
 * <p>It contains setter- and getter- methods for:
 *
 * <p><ul>
 * <li>a global similarity measure</li>
 * <li>a global list of method invokers (in order to set parameters of
 * the global similarity measure</li>
 * <li>a SimilarityMeasureFunc and...</li>
 * <li>a MethodInvokersFunc and...</li>
 * <li>a WeightFunc for defining different similarity measurement & their parameters
 * and weights for different data objects through all depths of computation</li>
 * </ul></p>
 */
public interface RetrieverExt {

    /**
     * sets the globally used similarity measure of the extended retriever
     *
     * <p>The similarity measure whose name is set by this method will be used to compare
     * the query object to all case objects respectively.
     *
     * @param similarityMeasure the name of the globally used similarity measure
     */
    void setGlobalSimilarityMeasure(String similarityMeasure);

    /**
     * @return the globally used similarity measure of the extended retriever
     */
    String getGlobalSimilarityMeasure();

    /**
     * sets the globally used method invokers of the extended retriever
     *
     * <p>The MethodInvoker objects in the list set by this method will be applied to
     * the globally defined similarity measure.
     *
     * @param methodInvokers the list of methods to be invoked by the global similarity measure
     */
    void setGlobalMethodInvokers(ArrayList<MethodInvoker> methodInvokers);

    /**
     * @return the globally used method invokers of the extended retriever
     */
    ArrayList<MethodInvoker> getGlobalMethodInvokers();

    /**
     * sets the locally used SimilarityMeasureFunc of the extended retriever
     *
     * <p>For every data object that is not query or case but a part of such (see collection objects)
     * the SimilarityMeasureFunc is applied to define different similarity measures for
     * different pairs of data objects through all depths of similarity computation.
     *
     * @param similarityMeasureFunc the locally used function assigning similarity measures
     */
    void setLocalSimilarityMeasureFunc(SimilarityMeasureFunc similarityMeasureFunc);

    /**
     * @return the locally used SimilarityMeasureFunc of the extended retriever
     */
    SimilarityMeasureFunc getLocalSimilarityMeasureFunc();

    /**
     * sets the locally used MethodInvokersFunc of the extended retriever
     *
     * <p>A local similarity measure used in the similarity computation may require parameters to
     * be set by invoking methods.
     *
     * <p>The MethodInvokersFunc set by this method will provide a list of methods to be invoked
     * for every pair of data objects which are not query or case objects.
     *
     * @param methodInvokersFunc the locally used function assigning methods to be invoked by similarity measures
     */
    void setLocalMethodInvokersFunc(MethodInvokersFunc methodInvokersFunc);

    /**
     * @return the locally used MethodInvokersFunc of the extended retriever
     */
    MethodInvokersFunc getLocalMethodInvokersFunc();

    /**
     * sets the locally used WeightFunc of the extended retriever
     *
     * <p>For every part of the query object (if it is an object of collection type) a weight can be
     * defined to model its importance in the similarity computation.
     *
     * <p>The 'part of the query object' can simply be an element of the query object or
     * an element of an element of the query object if the query object contains collection
     * objects as elements and so fort.
     *
     * @param weightFunc the locally used weight function
     */
    void setLocalWeightFunc(WeightFunc weightFunc);

    /**
     * @return the locally used WeightFunc of the extended retriever
     */
    WeightFunc getLocalWeightFunc();
}
