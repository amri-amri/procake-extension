package de.uni_trier.wi2.parsing.model;

import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.utils.MethodInvoker;

import java.util.ArrayList;
import java.util.List;

public class MIF_IfComponent extends IfComponent<ArrayList<MethodInvoker>> {
    final private MethodListComponent methodList;

    public MIF_IfComponent(LogicalOrConditionComponent conditionComponent, MethodListComponent methodList) {
        super(conditionComponent);
        this.methodList = methodList;
    }

    @Override
    public ArrayList<MethodInvoker> getReturnValue(){
        return methodList.evaluate();
    }
}
