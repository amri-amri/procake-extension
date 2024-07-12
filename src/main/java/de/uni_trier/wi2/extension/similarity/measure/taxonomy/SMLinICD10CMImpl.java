package de.uni_trier.wi2.extension.similarity.measure.taxonomy;

import de.uni_trier.wi2.extension.similarity.measure.taxonomy.abstraction.SMLin;
import de.uni_trier.wi2.utils.taxonomy.Taxonomy;
import org.slf4j.LoggerFactory;

/**
 * This class represents an implementation of the "Lin similarity" (see {@link SMLin}).
 * <p>
 * The taxonomy it is working on is the ICD_10_CM leaf taxonomy.
 */
public class SMLinICD10CMImpl extends SMLin implements SMLinICD10CM {

    {
        logger = LoggerFactory.getLogger(SMLinICD10CMImpl.class);
        setName(SMLinICD10CM.NAME);
        taxonomy = Taxonomy.ICD_10_CM;
    }

}
