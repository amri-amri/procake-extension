package utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodInvoker {

    private String methodName;
    private Class[] argTypes;
    private Object[] argValues;

    public void invoke(Object o) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = o.getClass().getMethod(methodName, argTypes);
        method.invoke(o, argValues);
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class[] getArgTypes() {
        return argTypes;
    }

    public void setArgTypes(Class[] argTypes) {
        this.argTypes = argTypes;
    }

    public Object[] getArgValues() {
        return argValues;
    }

    public void setArgValues(Object[] argValues) {
        this.argValues = argValues;
    }

    public MethodInvoker(String methodName, Class[] argTypes, Object[] argValues) {
        this.methodName = methodName;
        this.argTypes = argTypes;
        this.argValues = argValues;
    }
}
