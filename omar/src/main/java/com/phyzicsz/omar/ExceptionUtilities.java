/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.phyzicsz.omar;

/**
 *
 * @author phyzicsz <phyzics.z@gmail.com>
 */
public class ExceptionUtilities {

    /**
     * Safely Ignore a Throwable or rethrow if it is a Throwable that should not
     * be ignored.
     *
     * @param t Throwable to possibly ignore (ThreadDeath and OutOfMemory are
     * not ignored).
     */
    public static void safelyIgnoreException(Throwable t) {
        if (t instanceof ThreadDeath) {
            throw (ThreadDeath) t;
        }

        if (t instanceof OutOfMemoryError) {
            throw (OutOfMemoryError) t;
        }
    }

    /**
     * @return Throwable representing the actual cause (most nested exception).
     */
    public static Throwable getDeepestException(Throwable e) {
        while (e.getCause() != null) {
            e = e.getCause();
        }

        return e;
    }
}
