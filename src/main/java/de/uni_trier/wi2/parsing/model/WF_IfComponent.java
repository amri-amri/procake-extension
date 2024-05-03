package de.uni_trier.wi2.parsing.model;

public class WF_IfComponent extends IfComponent<Double>{
    DoubleComponent doubleComponent;


    public WF_IfComponent(LogicalOrConditionComponent conditionComponent, DoubleComponent doubleComponent) {
        super(conditionComponent);
        this.doubleComponent = doubleComponent;
    }


    @Override
    public Double getReturnValue() {
        return doubleComponent.evaluate();
    }
}
