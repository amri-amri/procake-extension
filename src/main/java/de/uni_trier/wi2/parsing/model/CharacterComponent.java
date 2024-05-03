package de.uni_trier.wi2.parsing.model;

import de.uni_trier.wi2.procake.data.object.DataObject;

public class CharacterComponent  implements ValueComponent<Character>{
    final private Character value;

    public CharacterComponent(Character value) {
        this.value = value;
    }

    @Override
    public Character evaluate() {
        return value;
    }

    @Override
    public Object evaluate(DataObject q, DataObject c) {
        return evaluate();
    }
}
