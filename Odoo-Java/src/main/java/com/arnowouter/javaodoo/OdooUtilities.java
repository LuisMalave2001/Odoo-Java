package com.arnowouter.javaodoo;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class OdooUtilities {

    @NotNull
    private static <T> T buildOdooObjectFromMap(Class<T> objectClass, Map<String, Object> odooHashMap) throws NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {
        T genericObject = objectClass.getDeclaredConstructor().newInstance();

        odooHashMap.forEach((fieldName, fieldValue) -> {
            Class<T> auxObjectClass = objectClass;
            while (auxObjectClass != null) {
                try {
                    String cammelCaseField = snakeCaseToCammelCase(fieldName);
                    Field field = auxObjectClass.getDeclaredField(cammelCaseField);
                    field.setAccessible(true);

                    if (fieldValue instanceof String) {
                        fieldValue = fieldValue.toString().trim();
                    } else if (fieldValue instanceof Boolean && !(Boolean)fieldValue) {
                        fieldValue = castNullToFalsy(field.getType());
                    }
                    field.set(genericObject, fieldValue);
                    field.setAccessible(false);
                    break;
                } catch (NoSuchFieldException e) {
                    auxObjectClass = (Class<T>) auxObjectClass.getSuperclass();
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }
        });
        return genericObject;
    }

    public static String snakeCaseToCammelCase(String snakeCaseString) {
        String cammelCaseString = snakeCaseString;
        while(cammelCaseString.contains("_")) {
            cammelCaseString = cammelCaseString.replaceFirst("_[a-z]", String.valueOf(Character.toUpperCase(cammelCaseString.charAt(cammelCaseString.indexOf("_") + 1))));
        }
        return cammelCaseString;
    }

    public static Object castNullToFalsy(Class falsyClass) {

        HashMap<Class, Object> primitiveTypesMap = new HashMap<>();

        primitiveTypesMap.put(Short.TYPE, 0);
        primitiveTypesMap.put(Integer.TYPE, 0);
        primitiveTypesMap.put(Double.TYPE, 0);
        primitiveTypesMap.put(Boolean.TYPE, Boolean.FALSE);
        primitiveTypesMap.put(Character.TYPE, "");
        primitiveTypesMap.put(String.class, "");

        return primitiveTypesMap.get(falsyClass);
    }


}
