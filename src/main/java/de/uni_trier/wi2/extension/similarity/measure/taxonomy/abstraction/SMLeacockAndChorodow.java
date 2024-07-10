package de.uni_trier.wi2.extension.similarity.measure.taxonomy.abstraction;

import de.uni_trier.wi2.utils.taxonomy.Taxonomy;

import java.io.IOException;

/**
 * This abstract class represents an implementation of the "Leacock & Chorodow similarity" in
 * SÁNCHEZ, David; BATET, Montserrat. Semantic similarity estimation in the biomedical domain: An ontology-based information-theoretic perspective. Journal of biomedical informatics, 2011, 44. Jg., Nr. 5, S. 749-759.
 */
public abstract class SMLeacockAndChorodow extends SMTaxonomy {

    /**
     * {@inheritDoc}
     * <p>
     * Here, similarity is defined by the following equation:
     * sim_res(c1,c2) = -log( (IC(c1) + IC(c2) - 2 * IC(LCS(c1,c2)) + 1) / (2 * max_IC) )
     *
     * @param code1 the first code
     * @param code2 the second code
     * @return A similarity value between 0 and 1.
     * @throws IOException if there was an error reading the *.xlsx file associated with the
     *                     given taxonomy.
     */
    protected double computeSimilarity(String code1, String code2) throws IOException {
        //-log( (IC(c1) + IC(c2) - 2 * IC(LCS(c1,c2)) + 1) / (2 * max_IC) )
        double ic1 = Taxonomy.getInformationContent(taxonomy, code1);
        double ic2 = Taxonomy.getInformationContent(taxonomy, code2);
        String lcs = Taxonomy.getLeastCommonSubsumer(code1, code2);
        double ic_lcs = Taxonomy.getInformationContent(taxonomy, lcs);
        double max_ic = Taxonomy.getMaximumInformationContent(taxonomy);
        return -Math.log((ic1 + ic2 - 2 * ic_lcs + 1) / (2 * max_ic));
    }
}
