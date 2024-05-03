package de.uni_trier.wi2.parsing.model;

import de.uni_trier.wi2.procake.data.object.DataObject;

import java.lang.reflect.InvocationTargetException;

public class MethodReturnValueComponent implements ObjectComponent, StringOrMethodReturnValueComponent{
    final private ObjectComponent object;
    final private MethodComponent method;

    public MethodReturnValueComponent(ObjectComponent object, MethodComponent method) {
        this.object = object;
        this.method = method;
    }

    @Override
    public Object evaluate(DataObject q, DataObject c) {
        try {
            return method.evaluate().invoke(object.evaluate(q, c));
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e){
            //TODO logging
            System.out.println("WARNING: " + e.getMessage());
            return null;
        }
    }
}
