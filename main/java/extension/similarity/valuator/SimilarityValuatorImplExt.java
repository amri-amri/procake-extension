package extension.similarity.valuator;

import de.uni_trier.wi2.procake.data.model.DataClass;
import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.SimilarityMeasure;
import de.uni_trier.wi2.procake.similarity.SimilarityModel;
import de.uni_trier.wi2.procake.similarity.SimilarityValuator;
import de.uni_trier.wi2.procake.similarity.impl.SimilarityImpl;
import de.uni_trier.wi2.procake.similarity.impl.SimilarityMeasureImpl;
import de.uni_trier.wi2.procake.similarity.impl.SimilarityValuatorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.MethodInvoker;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class SimilarityValuatorImplExt extends SimilarityValuatorImpl {

    public SimilarityValuatorImplExt(SimilarityModel simModel) {
        super(simModel);
    }


    private final Logger logger = LoggerFactory.getLogger(SimilarityMeasureImpl.class);

    public Similarity computeSimilarity(
            DataObject queryObject, DataObject caseObject, String similarityMeasureStr, ArrayList<MethodInvoker> methodInvokers) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        if (similarityMeasureStr == null
                || similarityMeasureStr.isEmpty()
                || similarityMeasureStr.equals("\"\"")) {
            return computeSimilarity(queryObject, caseObject);
        }

        SimilarityMeasure measure =
                getSimilarityModel().getSimilarityMeasure(queryObject.getDataClass(), similarityMeasureStr);
        if (measure == null) {
            // invalid similarity
            logger.warn("No applicable similarity measure found! Returning invalid similarity.");
            return new SimilarityImpl(null, queryObject, caseObject);
        }

        if (!measure.isReusable()) {
            measure = createNewInstance(measure);
        }

        if (methodInvokers!= null) for (MethodInvoker methodInvoker : methodInvokers) methodInvoker.invoke(measure);

        return measure.compute(queryObject, caseObject, this);
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
