/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.phyzicsz.omar;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 *
 * @author phyzicsz <phyzics.z@gmail.com>
 */
public class Introspecter {

    public interface Visitor {

        void onObject(Object o);
        
        void onField(Object o, Field field, Object value);
    }

    private final Map<Object, Object> _objVisited = new IdentityHashMap<>();
    private final Map<Class, ClassInfo> _classCache = new HashMap<>();

    /**
     * @param o Any Java Object
     * @param visitor Visitor is called for every object encountered during the
     * Java object graph traversal.
     */
    public static void traverse(Object o, Visitor visitor) {
        traverse(o, null, visitor);
    }

    /**
     * @param o Any Java Object
     * @param skip String[] of class names to not include in the tally
     * @param visitor Visitor is called for every object encountered during the
     * Java object graph traversal.
     */
    public static void traverse(Object o, Class<?>[] skip, Visitor visitor) {
        Introspecter traverse = new Introspecter();
        traverse.walk(o, skip, visitor);
        traverse._objVisited.clear();
        traverse._classCache.clear();
    }

    /**
     * Traverse the object graph referenced by the passed in root.
     *
     * @param root Any Java object.
     * @param skip Set of classes to skip (ignore). Allowed to be null.
     * @param visitor
     */
    public void walk(Object root, Class<?>[] skip, Visitor visitor) {
        Deque stack = new LinkedList();
        stack.add(root);

        while (!stack.isEmpty()) {
            Object current = stack.removeFirst();

            if (current == null || _objVisited.containsKey(current)) {
                continue;
            }

            final Class clazz = current.getClass();
            ClassInfo classInfo = getClassInfo(clazz, skip);
            if (classInfo._skip) {  // Do not process any classes that are assignableFrom the skip classes list.
                continue;
            }

            _objVisited.put(current, null);
            visitor.onObject(current);

            if (clazz.isArray()) {
                int len = Array.getLength(current);
                Class compType = clazz.getComponentType();

                if (!compType.isPrimitive()) {   // Speed up: do not walk primitives
                    ClassInfo info = getClassInfo(compType, skip);
                    if (!info._skip) {   // Do not walk array elements of a class type that is to be skipped.
                        for (int i = 0; i < len; i++) {
                            Object element = Array.get(current, i);
                            if (element != null) {   // Skip processing null array elements
                                stack.add(Array.get(current, i));
                            }
                        }
                    }
                }
            } else {   // Process fields of an object instance
                if (current instanceof Collection) {
                    walkCollection(stack, (Collection) current);
                } else if (current instanceof Map) {
                    walkMap(stack, (Map) current);
                } else {
                    walkFields(stack, current, skip, visitor);
                }
            }
        }
    }

    private void walkFields(Deque stack, Object current, Class<?>[] skip, Visitor visitor) {
        ClassInfo classInfo = getClassInfo(current.getClass(), skip);

        for (Field field : classInfo._refFields) {
            try {
                Object value = field.get(current);
                visitor.onField(current, field, value);
                if (value == null || value.getClass().isPrimitive()) {
                    continue;
                }
                stack.add(value);
            } catch (IllegalAccessException ignored) {
            }
        }
    }

    private static void walkCollection(Deque stack, Collection<?> col) {
        for (Object o : col) {
            if (o != null && !o.getClass().isPrimitive()) {
                stack.add(o);
            }
        }
    }

    private static void walkMap(Deque stack, Map<?, ?> map) {
        for (Map.Entry entry : map.entrySet()) {
            Object o = entry.getKey();

            if (o != null && !o.getClass().isPrimitive()) {
                stack.add(entry.getKey());
                stack.add(entry.getValue());
            }
        }
    }

    private ClassInfo getClassInfo(Class<?> current, Class<?>[] skip) {
        ClassInfo classCache = _classCache.get(current);
        if (classCache != null) {
            return classCache;
        }

        classCache = new ClassInfo(current, skip);
        _classCache.put(current, classCache);
        return classCache;
    }

    /**
     * This class wraps a class in order to cache the fields so they are only
     * reflectively obtained once.
     */
    public static class ClassInfo {

        private boolean _skip = false;
        private final Collection<Field> _refFields = new ArrayList<>();

        public ClassInfo(Class<?> c, Class<?>[] skip) {
            if (skip != null) {
                for (Class<?> klass : skip) {
                    if (klass.isAssignableFrom(c)) {
                        _skip = true;
                        return;
                    }
                }
            }

            Collection<Field> fields = ReflectionUtils.getDeepDeclaredFields(c);
            for (Field field : fields) {
                Class<?> fc = field.getType();

                if (!fc.isPrimitive()) {
                    _refFields.add(field);
                }
            }
        }
    }
}
