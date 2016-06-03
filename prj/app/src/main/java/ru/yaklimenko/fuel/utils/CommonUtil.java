package ru.yaklimenko.fuel.utils;

/**
 * Created by Антон on 02.06.2016.
 */
public class CommonUtil {

    public static boolean equals(Object object1, Object object2) {
        if (object1 == object2) {
            return true;
        }
        if ((object1 == null) || (object2 == null)) {
            return false;
        }
        return object1.equals(object2);
    }

}
