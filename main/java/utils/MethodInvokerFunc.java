package utils;

import de.uni_trier.wi2.procake.data.object.DataObject;

import java.util.ArrayList;

public interface MethodInvokerFunc {
    ArrayList<MethodInvoker> apply(DataObject q, DataObject c);
}
