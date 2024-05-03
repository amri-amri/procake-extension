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



/**
 * Extension of SimilarityValuatorImpl to allow setting parameters of the used similarity measure.
 */
public class SimilarityValuatorImplExt extends SimilarityValuatorImpl {

    /**
     * {@inheritDoc}
     */
    public SimilarityValuatorImplExt(SimilarityModel simModel) {
        super(simModel);

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

        if (similarityMeasureStr == null
                || similarityMeasureStr.isEmpty()
                || similarityMeasureStr.equals("\"\"")) {

            Similarity similarity = computeSimilarity(queryObject, caseObject);

            return similarity;
        }

        SimilarityMeasureImpl similarityMeasure = (SimilarityMeasureImpl) getSimilarityModel().getSimilarityMeasure(queryObject.getDataClass(), similarityMeasureStr);



        if (similarityMeasure == null) {
            // invalid similarity
            logger.warn("No applicable similarity measure found! Returning invalid similarity.");

            return new SimilarityImpl(null, queryObject, caseObject);
        }

        if (!similarityMeasure.isReusable()) {
            similarityMeasure = (SimilarityMeasureImpl) createNewInstance(similarityMeasure);
        }

        if (!similarityMeasure.isSimilarityFor(queryObject.getDataClass(),null)) return new SimilarityImpl(null, queryObject, caseObject);
        if (!similarityMeasure.isSimilarityFor(caseObject.getDataClass(), null)) return new SimilarityImpl(null, queryObject, caseObject);

        if (methodInvokers!= null) {

            for (MethodInvoker methodInvoker : methodInvokers) {
                try {
                    methodInvoker.invoke(similarityMeasure);
                } catch (Exception e){

                }
            }
        }


        Similarity similarity = similarityMeasure.compute(queryObject, caseObject, this);


        return similarity;
    }

    private SimilarityMeasure createNewInstance(SimilarityMeasure base) {


        try {
            SimilarityMeasureImpl copy =
                    (SimilarityMeasureImpl) base.getClass().getDeclaredConstructor().newInstance();

            //copy.initializeBasedOn(base);
            copy.setName(base.getName());
            copy.setDataClass(base.getDataClass());
            copy.setForceOverride(base.isForceOverride());



            return copy;
        } catch (Exception e) {

            e.printStackTrace();
        }



        return null;
    }

}
