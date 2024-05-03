package de.uni_trier.wi2.parsing.model;

import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.utils.MethodInvoker;

public class MethodComponent implements Component<MethodInvoker>{
    final private ValueComponent<?>[] arguments;
    final private String name;

    public MethodComponent(String name, ValueComponent<?>[] arguments) {
        this.arguments = arguments;
        this.name = name;
    }

    public MethodInvoker evaluate(){
        Class<?>[] argTypes = new Class[arguments.length];
        Object[] args = new Object[arguments.length];
        for (int i = 0; i < arguments.length; i++){
            Object arg = arguments[i].evaluate();
            Class<?> argType = arg.getClass();

            args[i] = arg;
            argTypes[i] = argType;
        }
        return new MethodInvoker(name, argTypes, args);
    }

    @Override
    public MethodInvoker evaluate(DataObject q, DataObject c) {
        return evaluate();
    }
}
