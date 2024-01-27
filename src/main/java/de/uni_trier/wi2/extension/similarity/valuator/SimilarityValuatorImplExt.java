package de.uni_trier.wi2.extension.similarity.valuator;

import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.SimilarityMeasure;
import de.uni_trier.wi2.procake.similarity.SimilarityModel;
import de.uni_trier.wi2.procake.similarity.impl.SimilarityImpl;
import de.uni_trier.wi2.procake.similarity.impl.SimilarityMeasureImpl;
import de.uni_trier.wi2.procake.similarity.impl.SimilarityValuatorImpl;
import de.uni_trier.wi2.utils.MethodInvoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import static de.uni_trier.wi2.ProcakeExtensionLoggingUtils.*;

/**
 * Extension of SimilarityValuatorImpl to allow setting parameters of the used similarity measure.
 */
public class SimilarityValuatorImplExt extends SimilarityValuatorImpl {

    /**
     * {@inheritDoc}
     */
    public SimilarityValuatorImplExt(SimilarityModel simModel) {
        super(simModel);
        METHOD_CALL.trace("...public procake-extension.extension.similarity.valuator.SimilarityValuatorImplExt.SimilarityValuatorImplExt" +
                "(SimilarityModel simModel={})", maxSubstring(simModel));
    }


    private final Logger logger = LoggerFactory.getLogger(SimilarityMeasureImpl.class);

    /**
     * Computes the similarity after setting the parameters of the given similarity measure
     * according to the list of method invokers
     *
     * @param queryObject  the query object
     * @param caseObject  the case object
     * @param similarityMeasureStr  the name of the similarity measure to be used
     * @param methodInvokers  list of method invokers to set parameters of the similarity Measure
     * @return
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     */
    public Similarity computeSimilarity(DataObject queryObject, DataObject caseObject, String similarityMeasureStr, ArrayList<MethodInvoker> methodInvokers) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        METHOD_CALL.trace(
                "public Similarity procake-extension.extension.similarity.valuator.SimilarityValuatorImplExt.computeSimilarity" +
                "(DataObject queryObject={}, " +
                "DataObject caseObject={}, " +
                "String similarityMeasureStr={}, " +
                "ArrayList<MethodInvoker> methodInvokers={})...",
                maxSubstring(queryObject),
                maxSubstring(caseObject),
                maxSubstring(similarityMeasureStr),
                maxSubstring(methodInvokers));

        if (similarityMeasureStr == null
                || similarityMeasureStr.isEmpty()
                || similarityMeasureStr.equals("\"\"")) {

            DIAGNOSTICS.trace(
                    "procake-extension.extension.similarity.valuator.SimilarityValuatorImplExt.computeSimilarity" +
                    "(DataObject, DataObject, String, ArrayList<MethodInvoker>): " +
                    "similarityMeasureStr == null || similarityMeasureStr.isEmpty() || similarityMeasureStr.equals(\"\\\"\\\"\")");

            DIAGNOSTICS.trace(
                    "procake-extension.extension.similarity.valuator.SimilarityValuatorImplExt.computeSimilarity" +
                            "(DataObject, DataObject, String, ArrayList<MethodInvoker>): " +
                            "similarity = de.uni_trier.wi2.procake.similarity.impl.SimilarityValuatorImpl.computeSimilarity" +
                            "(queryObject, caseObject)");

            Similarity similarity = computeSimilarity(queryObject, caseObject);

            METHOD_CALL.trace(
                    "procake-extension.extension.similarity.valuator.SimilarityValuatorImplExt.computeSimilarity" +
                    "(DataObject, DataObject, String, ArrayList<MethodInvoker>): return similarity = {}",
                    maxSubstring(similarity));

            return similarity;
        }

        SimilarityMeasure similarityMeasure = getSimilarityModel().getSimilarityMeasure(queryObject.getDataClass(), similarityMeasureStr);

        DIAGNOSTICS.trace(
                "procake-extension.extension.similarity.valuator.SimilarityValuatorImplExt.computeSimilarity" +
                        "(DataObject, DataObject, String, ArrayList<MethodInvoker>): " +
                        "similarityMeasure = getSimilarityModel().getSimilarityMeasure(queryObject.getDataClass(), similarityMeasureStr)" +
                        " = {}", maxSubstring(similarityMeasure));

        if (similarityMeasure == null) {
            // invalid similarity
            logger.warn("No applicable similarity measure found! Returning invalid similarity.");

            METHOD_CALL.trace(
                    "procake-extension.extension.similarity.valuator.SimilarityValuatorImplExt.computeSimilarity" +
                    "(DataObject, DataObject, String, ArrayList<MethodInvoker>): return new SimilarityImpl(null, queryObject, caseObject)");

            return new SimilarityImpl(null, queryObject, caseObject);
        }

        if (!similarityMeasure.isReusable()) {
            similarityMeasure = createNewInstance(similarityMeasure);
        }

        if (methodInvokers!= null) {
            DIAGNOSTICS.trace(
                    "procake-extension.extension.similarity.valuator.SimilarityValuatorImplExt.computeSimilarity" +
                    "(DataObject, DataObject, String, ArrayList<MethodInvoker>): Calling MethodInvokers...");
            for (MethodInvoker methodInvoker : methodInvokers) {
                try {
                    methodInvoker.invoke(similarityMeasure);
                } catch (Exception ignored){
                    DIAGNOSTICS.trace(
                            "procake-extension.extension.similarity.valuator.SimilarityValuatorImplExt.computeSimilarity" +
                            "(DataObject, DataObject, String, ArrayList<MethodInvoker>): Caught Exception: {}",
                            maxSubstring(ignored));
                }
            }
        }

        Similarity similarity = similarityMeasure.compute(queryObject, caseObject, this);

        METHOD_CALL.trace("procake-extension.extension.similarity.valuator.SimilarityValuatorImplExt.computeSimilarity" +
                "(DataObject, DataObject, String, ArrayList<MethodInvoker>): return Similarity");
        return similarity;
    }

    private SimilarityMeasure createNewInstance(SimilarityMeasure base) {
        METHOD_CALL.trace(
                "public SimilarityMeasure procake-extension.extension.similarity.valuator.SimilarityValuatorImplExt.createNewInstance" +
                        "(SimilarityMeasure base={})...", maxSubstring(base));

        try {
            SimilarityMeasureImpl copy =
                    (SimilarityMeasureImpl) base.getClass().getDeclaredConstructor().newInstance();

            //copy.initializeBasedOn(base);
            copy.setName(base.getName());
            copy.setDataClass(base.getDataClass());
            copy.setForceOverride(base.isForceOverride());


            METHOD_CALL.trace(
                    "procake-extension.extension.similarity.valuator.SimilarityValuatorImplExt.createNewInstance" +
                            "(SimilarityMeasure): return copy={}", maxSubstring(copy));
            return copy;
        } catch (Exception e) {
            METHOD_CALL.trace(
                    "procake-extension.extension.similarity.valuator.SimilarityValuatorImplExt.createNewInstance" +
                            "(SimilarityMeasure): Exception e: {}", maxSubstring(e.getMessage()));
            e.printStackTrace();
        }


        METHOD_CALL.trace(
                "procake-extension.extension.similarity.valuator.SimilarityValuatorImplExt.createNewInstance" +
                        "(SimilarityMeasure): return null;");
        return null;
    }

}
