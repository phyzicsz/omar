/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.phyzicsz.omar;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author phyzicsz <phyzics.z@gmail.com>
 */
public final class ReflectionUtils {

    private static final ConcurrentMap<Class<?>, Collection<Field>> FIELD_MAP = new ConcurrentHashMap<>();

    /**
     * Get all non static, non transient, fields of the passed in class,
     * including private fields. Note, the special this$ field is also not
     * returned. The result is cached in a static ConcurrentHashMap to benefit
     * execution performance.
     *
     * @param c Class instance
     * @return Collection of only the fields in the passed in class that would
     * need further processing (reference fields). This makes field traversal on
     * a class faster as it does not need to continually process known fields
     * like primitives.
     */
    public static Collection<Field> getDeepDeclaredFields(Class<?> c) {
        if (FIELD_MAP.containsKey(c)) {
            return FIELD_MAP.get(c);
        }
        Collection<Field> fields = new ArrayList<>();
        Class<?> curr = c;

        while (curr != null) {
            getDeclaredFields(curr, fields);
            curr = curr.getSuperclass();
        }
        FIELD_MAP.put(c, fields);
        return fields;
    }

    /**
     * Get all non static, non transient, fields of the passed in class,
     * including private fields. Note, the special this$ field is also not
     * returned. The resulting fields are stored in a Collection.
     *
     * @param c Class instance that would need further processing (reference
     * fields). This makes field traversal on a class faster as it does not need
     * to continually process known fields like primitives.
     */
    public static void getDeclaredFields(Class<?> c, Collection<Field> fields) {
        try {
            Field[] local = c.getDeclaredFields();

            for (Field field : local) {
                try {
                    field.setAccessible(true);
                } catch (Exception ignored) {
                }

                int modifiers = field.getModifiers();
                if (!Modifier.isStatic(modifiers)
                        && !field.getName().startsWith("this$")
                        && !Modifier.isTransient(modifiers)) {   // speed up: do not count static fields, do not go back up to enclosing object in nested case, do not consider transients
                    fields.add(field);
                }
            }
        } catch (Throwable ignored) {
            ExceptionUtilities.safelyIgnoreException(ignored);
        }
    }
}
