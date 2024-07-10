package de.uni_trier.wi2.extension.similarity.measure.taxonomy;

import de.uni_trier.wi2.extension.similarity.measure.taxonomy.abstraction.SMResnik;
import de.uni_trier.wi2.utils.taxonomy.Taxonomy;
import org.slf4j.LoggerFactory;

/**
 * This class represents an implementation of the "Resnik similarity" (see {@link SMResnik}).
 * <p>
 * The taxonomy it is working on is the ICD_10_CM leaf taxonomy.
 */
public class SMResnikICD10CMImpl extends SMResnik implements SMResnikICD10CM {

    {
        logger = LoggerFactory.getLogger(SMResnikICD10CMImpl.class);
        setName(SMResnikICD10CM.NAME);
        taxonomy = Taxonomy.ICD_10_CM;
    }

}
