package de.uni_trier.wi2.utils;

import de.uni_trier.wi2.procake.data.object.DataObject;

import java.util.ArrayList;

import static de.uni_trier.wi2.ProcakeExtensionLoggingUtils.METHOD_CALL;

/**
 * Functional interface for assigning lists of MethodInvoker objects to pairs of data objects.
 */
public interface MethodInvokersFunc {

    /**
     * Return list of MethodInvoker objects.
     *
     * @param q  the first data object
     * @param c  the second data object
     * @return  the assigned list of MethodInvoker objects
     */
    ArrayList<MethodInvoker> apply(DataObject q, DataObject c);

    static MethodInvokersFunc getDefault(){
        METHOD_CALL.info(
                "static MethodInvokersFunc procake-extension.utils.MethodInvokersFunc.getDefault()...");
        return (q, c) -> new ArrayList<>();
    }
}
