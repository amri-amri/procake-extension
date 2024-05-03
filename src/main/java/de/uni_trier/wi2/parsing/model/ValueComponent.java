package de.uni_trier.wi2.parsing.model;

public interface ValueComponent<T> extends ObjectOrValueComponent{
    public T evaluate();
}
