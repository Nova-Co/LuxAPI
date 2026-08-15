package com.novaco.luxapi.commons.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Forwards an already-built {@code Object[]} to {@link Method#invoke} directly. Written in
 * Java rather than Kotlin because Java passes an existing array straight through to a vararg
 * parameter, while Kotlin's spread operator ({@code *args}) unconditionally inserts an
 * {@code Arrays.copyOf} defensive copy before every vararg call, even when the array is
 * freshly built and never shared (verified via javap on both call shapes).
 */
public final class DirectInvoker {

    private DirectInvoker() {
    }

    public static Object invoke(Method method, Object target, Object[] args)
            throws InvocationTargetException, IllegalAccessException {
        return method.invoke(target, args);
    }
}
