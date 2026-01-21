package org.backend.core.util.security.masking;

public class MaskingUtil {
	
	
	private MaskingUtil() {}

    public static String mask(String value, MaskingType type) {
        if (value == null || value.isBlank()) {
            return value;
        }

        return switch (type) {
            case EMAIL -> maskEmail(value);
            case PHONE -> maskPhone(value);
            case NAME  -> maskName(value);
        };
    }

    private static String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return "***@***";
        }

        String local = email.substring(0, atIndex);
        String domain = email.substring(atIndex);

        return local.charAt(0) + "***" + local.charAt(local.length() - 1) + domain;
    }

    private static String maskPhone(String phone) {
        if (phone.length() < 7) {
            return "***";
        }

        return phone.substring(0, 3)
                + "-****-"
                + phone.substring(phone.length() - 4);
    }

    private static String maskName(String name) {
        if (name.length() <= 1) {
            return "*";
        }

        return name.charAt(0) + "*".repeat(name.length() - 1);
    }

}
