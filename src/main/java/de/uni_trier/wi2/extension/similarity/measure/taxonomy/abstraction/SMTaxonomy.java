package de.uni_trier.wi2.extension.similarity.measure.taxonomy.abstraction;

import de.uni_trier.wi2.procake.data.model.DataClass;
import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.data.object.base.ListObject;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.SimilarityValuator;
import de.uni_trier.wi2.procake.similarity.impl.SimilarityImpl;
import de.uni_trier.wi2.procake.similarity.impl.SimilarityMeasureImpl;
import de.uni_trier.wi2.utils.XEStoSystem;
import de.uni_trier.wi2.utils.taxonomy.InvalidDataClassException;
import de.uni_trier.wi2.utils.taxonomy.ValueNotFoundException;
import de.uni_trier.wi2.utils.taxonomy.XESEventToTaxonomy;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Arrays;

/**
 * This similarity measure works on XESEventClass objects and computes similarity values
 * based on taxonomic data.
 * <p>
 * The concept of similarity based on information content of codes stems from the paper
 * "Semantic similarity estimation in the biomedical domain: An ontology-based information-theoretic perspective"
 * by Sánchez et al. (2011).
 */
public abstract class SMTaxonomy extends SimilarityMeasureImpl {
    /**
     * The logger used to important messages.
     * Every non-abstract implementation of this class has to initialize this object.
     */
    protected Logger logger;
    /**
     * The name of the taxonomy (see {@link de.uni_trier.wi2.utils.taxonomy.Taxonomy}).
     */
    protected String taxonomy;

    /**
     * Computes similarity of two codes in the given {@link SMTaxonomy#taxonomy}.
     *
     * @param code1 the first code
     * @param code2 the second code
     * @return A similarity value between 0 and 1.
     * @throws IOException if there was an error reading the *.xlsx file associated with the
     *                     given taxonomy.
     */
    abstract protected double computeSimilarity(String code1, String code2) throws IOException;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSimilarityFor(DataClass dataClass, String s) {
        return XEStoSystem.isXESEventClass(dataClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Similarity compute(DataObject dataObject1, DataObject dataObject2, SimilarityValuator similarityValuator) {
        // check if this similarity measure can be applied to the given objects
        DataClass dataClass1 = dataObject1.getDataClass();
        DataClass dataClass2 = dataObject2.getDataClass();
        if (!isSimilarityFor(dataClass1, null) || !isSimilarityFor(dataClass2, null)) {
            logger.warn("Invalid similarity returned! This similarity measure cannot be applied to classes {} and {}.", dataClass1.getName(), dataClass2.getName());
            return new SimilarityImpl(this, dataObject1, dataObject2);
        }

        // try computing the similarity value
        double value;
        try {
            // retrieve codes from objects
            String code1 = XESEventToTaxonomy.getCode((ListObject) dataObject1);
            String code2 = XESEventToTaxonomy.getCode((ListObject) dataObject2);

            // compute value according to implementation of the following method
            value = computeSimilarity(code1, code2);
        } catch (InvalidDataClassException | ValueNotFoundException | IOException e) {
            logger.info("Invalid similarity returned:\n{}", Arrays.toString(e.getStackTrace()));
            return new SimilarityImpl(this, dataObject1, dataObject2);
        }

        // successfully return new similarity
        return new SimilarityImpl(this, dataObject1, dataObject2, value);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getSystemName() {
        return getName();
    }
}
