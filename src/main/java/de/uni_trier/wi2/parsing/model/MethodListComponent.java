package de.uni_trier.wi2.parsing.model;

import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.utils.MethodInvoker;

import java.util.ArrayList;

public class MethodListComponent implements Component<ArrayList<MethodInvoker>> {
    final private MethodComponent[] methods;

    public MethodListComponent(MethodComponent[] methods) {
        this.methods = methods;
    }

    public ArrayList<MethodInvoker> evaluate() {
        ArrayList<MethodInvoker> invokers = new ArrayList<>();
        for (MethodComponent method : methods) invokers.add(method.evaluate());
        return invokers;
    }

    @Override
    public ArrayList<MethodInvoker> evaluate(DataObject q, DataObject c) {
        return evaluate();
    }
}
