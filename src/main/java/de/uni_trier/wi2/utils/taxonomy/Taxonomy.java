package de.uni_trier.wi2.utils.taxonomy;

import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A Taxonomy represents a taxonomy of codes.
 *
 * <p>
 * A taxonomy of codes is a tree where each node represents a code.
 * A code is a string of characters. Descendants of a code in the taxonomy
 * contains the code as a prefix. Furthermore, the amount of characters
 * a descendant has more than its ancestor, is the depth of the descendant in
 * the subtree of the ancestor. So every child contains one character more than
 * its parent code.
 *
 * <p>
 * A taxonomy object contains a path to an *.xlsx file containing the codes of
 * the taxonomy. Furthermore, it contains the number of the sheet in the *.xlsx
 * file and the column number.
 * The constructor can be given a Predicate<Row> object serving as a condition
 * for the inclusion in the calculation of information content values.
 */
public abstract class Taxonomy {

    /**
     * The static map containing all implemented taxonomies.
     */
    private final static HashMap<String, Taxonomy> taxonomies;

    public final static String ICD_10_PCS = "ICD-10-PCS";
    public final static String ICD_10_CM = "ICD-10-CM";

    static {
        taxonomies = new HashMap<>();

        new LeafTaxonomy(
                ICD_10_PCS, "src/main/resources/de/uni_trier/wi2/taxonomies/ICD-10-PCS Codes.xlsx",
                1, 8, row -> {
            if (row.getCellAsString(8).get().equals("ICD-10-PCS")) return false;
            return row.getCellAsNumber(9).get().intValue() == 1;
        });

        new LeafTaxonomy(
                ICD_10_CM, "src/main/resources/de/uni_trier/wi2/taxonomies/ICD-10-CM Codes.xlsx",
                0, 1, row -> {
            if (row.getCellAsString(1).get().equals("ICD-10-CM")) return false;
            return row.getCellAsNumber(2).get().intValue() == 1;
        });
    }

    /**
     * The path to the *.xlsx file containing the codes.
     */
    protected final String path;

    /**
     * The index of the sheet in the *.xlsx file.
     */
    protected final int sheetNum;

    /**
     * The index of the column containing the codes.
     */
    protected final int columnNum;

    /**
     * The condition determining a rows inclusion in the calculation
     * of information content values.
     */
    protected final Predicate<Row> condition;


    /**
     * Creates a Taxonomy object and saves it in the static 'taxonmies' map.
     *
     * @param name      The name under which the taxonomy is to be saved.
     * @param path      The path of the *.xlsx file.
     * @param sheetNum  The index of the sheet in the *.xlsx file.
     * @param columnNum The index of the column containing the codes.
     */
    protected Taxonomy(String name, String path, int sheetNum, int columnNum) {
        this(name, path, sheetNum, columnNum, row -> true);
    }

    /**
     * Creates a Taxonomy object and saves it in the static 'taxonmies' map.
     *
     * @param name      The name under which the taxonomy is to be saved.
     * @param path      The path of the *.xlsx file.
     * @param sheetNum  The index of the sheet in the *.xlsx file.
     * @param columnNum The index of the column containing the codes.
     * @param condition The condition determining a rows inclusion in the calculation of information content values.
     */
    protected Taxonomy(String name, String path, int sheetNum, int columnNum, Predicate<Row> condition) {
        this.path = path;
        this.sheetNum = sheetNum;
        this.columnNum = columnNum;
        this.condition = condition;
        taxonomies.put(name, this);
    }

    /**
     * Looks for and returns a taxonomy object of the given name.
     *
     * @param name The name of the requested taxonomy.
     * @return A Taxonomy object with the requested name.
     * @throws TaxonomyDoesNotExistException if the requested taxonomy does not exist in the 'taxonomies' map
     */
    private static Taxonomy getTaxonomy(String name) {
        Taxonomy tax = taxonomies.get(name);
        if (tax == null) throw new TaxonomyDoesNotExistException(name);
        return tax;
    }

    /**
     * Returns information content of given code in given taxonomy.
     *
     * @param taxonomy The taxonomy in which the information content is to be calculated
     * @param code     The code for which the information content is to be calculated
     * @return The information content value.
     * @throws IOException if an error occurs reading the *.xlsx file.
     */
    public static double getInformationContent(String taxonomy, String code) throws IOException {
        Taxonomy tax = getTaxonomy(taxonomy);
        return tax.getInformationContent(code);
    }

    /**
     * Returns maximum information content in taxonomy.
     *
     * @param taxonomy The name of the taxonomy.
     * @return The maximum information content value.
     * @throws IOException if an error occurs reading the *.xlsx file.
     */
    public static double getMaximumInformationContent(String taxonomy) throws IOException {
        Taxonomy tax = getTaxonomy(taxonomy);
        return tax.getMaximumInformationContent();
    }

    /**
     * Returns the least common subsumer of two given codes, independent of any taxonomy.
     *
     * @param code1 The first code.
     * @param code2 The second code.
     * @return The longest string which is a prefix of both codes.
     */
    public static String getLeastCommonSubsumer(String code1, String code2) {
        int maxLen = Math.min(code1.length(), code2.length());
        int len = 1;
        String substr1, substr2;
        while (len <= maxLen) {
            substr1 = code1.substring(0, len);
            substr2 = code2.substring(0, len);
            if (!substr1.equals(substr2)) break;
            len++;
        }
        return code1.substring(0, len - 1);
    }

    /**
     * Returns information content of given code.
     *
     * @param code The code for which the information content is to be calculated.
     * @return The information content value.
     * @throws IOException if an error occurs reading the *.xlsx file.
     */
    protected abstract double getInformationContent(String code) throws IOException;

    /**
     * Returns maximum information content of given code.
     *
     * @return The maximum information content value.
     * @throws IOException if an error occurs reading the *.xlsx file.
     */
    protected abstract double getMaximumInformationContent() throws IOException;

    /**
     * Returns the all Row objects satisfying the condition in a stream.
     *
     * @return All Row objects satisfying the condition.
     * @throws IOException if an error occurs reading the *.xlsx file.
     */
    protected Stream<Row> getRowStream() throws IOException {
        InputStream is = new FileInputStream(path);
        ReadableWorkbook workbook = new ReadableWorkbook(is);
        Sheet sheet = workbook.getSheet(sheetNum).orElseThrow();
        return sheet.openStream().filter(condition);
    }
}
