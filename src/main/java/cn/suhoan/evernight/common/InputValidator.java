package cn.suhoan.evernight.common;

import org.springframework.util.StringUtils;

public final class InputValidator {

    private InputValidator() {
    }

    public static String requireText(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(fieldName + " 不能为空");
        }
        return value.trim();
    }

    public static int clampPageSize(Integer value, int defaultValue, int maxValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value < 1) {
            return defaultValue;
        }
        return Math.min(value, maxValue);
    }

}
