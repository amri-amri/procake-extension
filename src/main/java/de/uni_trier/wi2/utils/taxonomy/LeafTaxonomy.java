package de.uni_trier.wi2.utils.taxonomy;

import com.google.common.util.concurrent.AtomicDouble;
import org.dhatim.fastexcel.reader.Row;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A LeafTaxonomy represents a taxonomy where the given *.xlsx file only
 * contains leafs, i.e. codes which do not have any descendants in the
 * taxonomy tree.
 */
public class LeafTaxonomy extends Taxonomy {

    private double maxInformationContent = -1;


    /**
     * @see Taxonomy#Taxonomy(String, String, int, int)
     */
    protected LeafTaxonomy(String name, String path, int sheetNum, int columnNum) {
        super(name, path, sheetNum, columnNum);
    }

    /**
     * @see Taxonomy#Taxonomy(String, String, int, int, Predicate)
     */
    protected LeafTaxonomy(String name, String path, int sheetNum, int columnNum, Predicate<Row> condition) {
        super(name, path, sheetNum, columnNum, condition);
    }

    /**
     * {@inheritDoc}
     *
     * @param code The code for which the information content is to be calculated.
     * @return The information content value.
     * @throws IOException if an error occurs reading the *.xlsx file.
     */
    @Override
    protected double getInformationContent(String code) throws IOException {
        Stream<Row> rows = getRowStream();

        AtomicInteger leaves = new AtomicInteger();
        int subsumers = code.length();
        AtomicInteger maxLeaves = new AtomicInteger();

        rows.forEach(row -> {
            String currentCode = row.getCellAsString(columnNum).get();
            if (currentCode.startsWith(code)) leaves.getAndIncrement();
            maxLeaves.getAndIncrement();
        });

        double p = (((double) leaves.get() / subsumers) + 1) / (maxLeaves.get() + 1);

        return -Math.log(p);
    }

    /**
     * {@inheritDoc}
     *
     * @return The maximum information content value.
     * @throws IOException if an error occurs reading the *.xlsx file.
     */
    @Override
    protected double getMaximumInformationContent() throws IOException {
        if (maxInformationContent == -1) calculateMaximumInformationContent();
        return maxInformationContent;
    }

    /**
     * Calculates the maximum information content.
     *
     * @throws IOException if an error occurs reading the *.xlsx file.
     */
    private void calculateMaximumInformationContent() throws IOException {
        Stream<Row> rows = getRowStream();

        AtomicDouble maxIC = new AtomicDouble(0);

        rows.forEach(row -> {
            String code = row.getCellAsString(columnNum).get();
            double IC = 0;
            try {
                IC = this.getInformationContent(code);
            } catch (IOException ignored) {
            }
            if (IC > maxIC.get()) maxIC.set(IC);
        });

        maxInformationContent = maxIC.get();
    }
}
