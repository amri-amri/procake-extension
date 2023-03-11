package utils;

import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.similarity.SimilarityMeasure;
import org.apache.lucene.search.similarities.Similarity;

public interface SimFunc {
    String apply(DataObject a, DataObject b);
}
