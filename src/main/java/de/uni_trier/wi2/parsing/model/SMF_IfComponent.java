package de.uni_trier.wi2.parsing.model;

public class SMF_IfComponent extends IfComponent<String>{
    final private StringComponent string;


    public SMF_IfComponent(LogicalOrConditionComponent conditionComponent, StringComponent string) {
        super(conditionComponent);
        this.string = string;
    }

    @Override
    public String getReturnValue() {
        return string.evaluate();
    }
}
