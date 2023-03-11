package utils;

import de.uni_trier.wi2.procake.data.object.DataObject;

public interface WeightFunc {
    double apply(DataObject a, DataObject b);
}
