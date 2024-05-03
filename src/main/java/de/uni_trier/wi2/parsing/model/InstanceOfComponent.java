package de.uni_trier.wi2.parsing.model;

import de.uni_trier.wi2.procake.data.object.DataObject;

public class InstanceOfComponent implements ConditionComponent {
    final private ObjectComponent object;
    final private StringComponent stringC;

    public InstanceOfComponent(ObjectComponent object, StringComponent stringC) {
        this.object = object;
        this.stringC = stringC;
    }

    @Override
    public Boolean evaluate(DataObject q, DataObject c) {
        Class<?> clazz;
        try {
            clazz = Class.forName(stringC.evaluate());
            return clazz.isInstance(object.evaluate(q,c));
        }catch (ClassNotFoundException e){
            //TODO logging
            System.out.print("WARNING: " + e.getMessage());
            return false;
        }
    }
}

