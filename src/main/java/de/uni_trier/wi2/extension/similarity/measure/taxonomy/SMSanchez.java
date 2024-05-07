package de.uni_trier.wi2.extension.similarity.measure.taxonomy;

import de.uni_trier.wi2.procake.similarity.SimilarityMeasure;
import de.uni_trier.wi2.procake.similarity.impl.SimilarityMeasureImpl;
import de.uni_trier.wi2.utils.taxonomy.Taxonomy;

import java.io.IOException;

public abstract class SMSanchez extends SimilarityMeasureImpl {
    protected double computeSimilarity(String taxonomy, String code1, String code2) throws IOException {
        String lcs = Taxonomy.getLeastCommonSubsumer(code1, code2);
        double numerator = 2 * Taxonomy.getInformationContent(taxonomy, lcs);
        double denominator = Taxonomy.getInformationContent(taxonomy, code1) + Taxonomy.getInformationContent(taxonomy, code2);
        return 1 - numerator / denominator;
    }
}
