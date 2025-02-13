package de.uni_trier.wi2.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * This classes use is to invoke a certain method on an object using java reflections.
 *
 * <p>Given the name of a method, its argument types in an array, and the respective argument values
 * the named method can be invoked on a given object.
 */
public class MethodInvoker {

    /**
     * the name of the method to be invoked
     */
    private final String methodName;

    /**
     * the array of argument types
     */
    private final Class[] argTypes;

    /**
     * the array of argument values
     */
    private final Object[] argValues;

    /**
     * Constructs a new MethodInvoker object.
     *
     * @param methodName the name of the method
     * @param argTypes   the array of argument types
     * @param argValues  the array of argument values
     */
    public MethodInvoker(final String methodName, final Class[] argTypes, final Object[] argValues) {

        this.methodName = methodName;
        this.argTypes = argTypes;
        this.argValues = argValues;
    }

    /**
     * Invokes the method on a given object.
     *
     * @param o the object the method is to be invoked on
     * @throws NoSuchMethodException     if the method does not exist for the object
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public Object invoke(Object o) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = o.getClass().getMethod(methodName, argTypes);

        Object ret = method.invoke(o, argValues);
        return ret;
    }

    public String getMethodName() {
        return methodName;
    }

    public Class[] getArgTypes() {
        return argTypes;
    }

    public Object[] getArgValues() {
        return argValues;
    }
}
