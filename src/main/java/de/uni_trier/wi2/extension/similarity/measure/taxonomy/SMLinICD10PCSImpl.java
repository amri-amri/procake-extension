package de.uni_trier.wi2.extension.similarity.measure.taxonomy;

import de.uni_trier.wi2.extension.similarity.measure.taxonomy.abstraction.SMLin;
import de.uni_trier.wi2.utils.taxonomy.Taxonomy;
import org.slf4j.LoggerFactory;

/**
 * This class represents an implementation of the "Lin similarity" (see {@link SMLin}).
 * <p>
 * The taxonomy it is working on is the ICD_10_PCS leaf taxonomy.
 */
public class SMLinICD10PCSImpl extends SMLin implements SMLinICD10PCS {

    {
        logger = LoggerFactory.getLogger(SMLinICD10PCSImpl.class);
        setName(SMLinICD10PCS.NAME);
        taxonomy = Taxonomy.ICD_10_PCS;
    }

}
