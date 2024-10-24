package com.supra.daas.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class EmptyUtils {

    public static boolean isEmpty(Boolean value) {
        return value == null || !value;
    }

    public static boolean isEmpty(String value) {
        return value == null || value.equals("");
    }

    public static boolean isEmpty(Integer value) {
        return value == null || value.intValue() == 0;
    }

    public static boolean isEmpty(Long value) {
        return value == null || value.longValue() == 0;
    }

    public static boolean isEmpty(Double value) {
        return value == null || value.doubleValue() == 0;
    }

    public static boolean isEmpty(Float value) {
        return value == null || value.floatValue() == 0;
    }

    public static boolean isEmpty(List value) {
        return value == null || value.size() == 0;
    }

    public static boolean isNotEmpty(Boolean value) {
        return value != null && value;
    }

    public static boolean isNotEmpty(String value) {
        return value != null && !value.equals("");
    }

    public static boolean isNotEmpty(Integer value) {
        return value != null && !(value.intValue() == 0);
    }

    public static boolean isNotEmpty(Long value) {
        return value != null && !(value.longValue() == 0);
    }

    public static boolean isNotEmpty(Double value) {
        return value != null && !(value.doubleValue() == 0);
    }

    public static boolean isNotEmpty(Float value) {
        return value != null && !(value.floatValue() == 0);
    }

    public static boolean isNotEmpty(Collection value) {
        return value != null && !(value.size() == 0);
    }

    public static boolean isNotEmpty(Map value) {
        return value != null && !(value.size() == 0);
    }

    public static boolean isNotArrayEmpty(String value) {
        return value != null && !value.equals("") && !value.equals("[]");
    }

    public static boolean isNotEmpty(String[] value) {
        return value != null && value.length > 0;
    }

    public static String getNotEmpty(String value) {
        return value != null ? value : "";
    }

    public static Integer getNotEmpty(Integer value) {
        return value != null ? value : 0;
    }

    public static boolean compareString(String oldStr, String newStr) {
        return compareObject(oldStr, newStr);
    }

    public static boolean compareList(List<String> values1, List<String> values2) {
        if (values1 == null && values2 == null) {
            return true;
        }
        else if (values1 == null && values2 != null) {
            return false;
        }
        else if (values1 != null && values2 == null) {
            return false;
        }

        return values1.equals(values2);
    }

    public static boolean compareBoolean(Boolean value1, Boolean value2) {
        return compareObject(value1, value2);
    }

    public static boolean compareObject(Object value1, Object value2) {
        if (value1 == null && value2 == null) {
            return true;
        }
        else if (value1 == null && value2 != null) {
            return false;
        }
        else if (value1 != null && value2 == null) {
            return false;
        }

        return value1.equals(value2);
    }
}
