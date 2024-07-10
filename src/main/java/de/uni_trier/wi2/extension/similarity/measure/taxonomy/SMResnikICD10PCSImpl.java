package de.uni_trier.wi2.extension.similarity.measure.taxonomy;

import de.uni_trier.wi2.extension.similarity.measure.taxonomy.abstraction.SMResnik;
import de.uni_trier.wi2.utils.taxonomy.Taxonomy;
import org.slf4j.LoggerFactory;

/**
 * This class represents an implementation of the "Resnik similarity" (see {@link SMResnik}).
 * <p>
 * The taxonomy it is working on is the ICD_10_PCS leaf taxonomy.
 */
public class SMResnikICD10PCSImpl extends SMResnik implements SMResnikICD10PCS {

    {
        logger = LoggerFactory.getLogger(SMResnikICD10PCSImpl.class);
        setName(SMResnikICD10PCS.NAME);
        taxonomy = Taxonomy.ICD_10_PCS;
    }

}
