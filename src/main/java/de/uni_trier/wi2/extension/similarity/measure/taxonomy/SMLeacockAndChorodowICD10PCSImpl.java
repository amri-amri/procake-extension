package de.uni_trier.wi2.extension.similarity.measure.taxonomy;

import de.uni_trier.wi2.extension.similarity.measure.taxonomy.abstraction.SMLeacockAndChorodow;
import de.uni_trier.wi2.utils.taxonomy.Taxonomy;
import org.slf4j.LoggerFactory;

/**
 * This class represents an implementation of the "Leacock & Chorodow similarity" (see {@link SMLeacockAndChorodow}).
 * <p>
 * The taxonomy it is working on is the ICD_10_PCS leaf taxonomy.
 */
public class SMLeacockAndChorodowICD10PCSImpl extends SMLeacockAndChorodow implements SMLeacockAndChorodowICD10PCS {

    {
        logger = LoggerFactory.getLogger(SMLeacockAndChorodowICD10PCSImpl.class);
        setName(SMLeacockAndChorodowICD10PCS.NAME);
        taxonomy = Taxonomy.ICD_10_PCS;
    }

}
