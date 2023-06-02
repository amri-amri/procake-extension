package extension.abstraction;

import utils.MethodInvokersFunc;

/**
 * An interface for every class using the MethodInvokersFunc functional interface.
 */
public interface IMethodInvokersFunc {

    /**
     * sets the MethodInvokersFunc
     *
     * @param methodInvokersFunc  the MethodInvokersFunc to be set
     */
    void setMethodInvokersFunc(MethodInvokersFunc methodInvokersFunc);

    /**
     * gets the MethodInvokersFunc
     *
     * @return the MethodInvokersFunc
     */
    MethodInvokersFunc getMethodInvokersFunc();
}
