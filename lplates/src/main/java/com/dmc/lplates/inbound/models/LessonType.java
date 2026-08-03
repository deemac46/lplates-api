package com.dmc.lplates.inbound.models;

import java.util.Arrays;

/**
 * Valid values for {@link Booking#getLessonType()}.
 */
public enum LessonType {
    LESSON("lesson"),
    EDT("edt"),
    TEST_CAR_HIRE("test_car_hire");

    private final String value;

    LessonType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static boolean isValid(String value) {
        if (value == null) return false;
        return Arrays.stream(values()).anyMatch(t -> t.value.equalsIgnoreCase(value));
    }
}
