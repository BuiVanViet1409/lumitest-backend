package com.lumitest.util;

import java.util.LinkedHashSet;
import java.util.Set;

public class HeuristicUtils {

    private static String[] deduplicate(String... keywords) {
        Set<String> set = new LinkedHashSet<>();
        for (String k : keywords) {
            if (k != null && !k.trim().isEmpty()) {
                set.add(k.trim().toLowerCase());
            }
        }
        return set.toArray(new String[0]);
    }

    public static String[] getEmailKeywords(String selector) {
        return deduplicate("email", "mail", "username", "login", "tên đăng nhập", selector.replace(" input", ""));
    }

    public static String[] getPasswordKeywords(String selector) {
        return deduplicate("password", "pass", "mat khau", "mật khẩu", selector.replace(" input", ""));
    }

    public static String[] getPhoneKeywords(String selector) {
        return deduplicate("phone", "tel", "mobile", "số điện thoại", "sđt", selector);
    }

    public static String[] getUsernameKeywords(String selector) {
        return deduplicate("username", "user", "tài khoản", "account", selector);
    }

    public static String[] getLoginKeywords(String selector) {
        return deduplicate("login", "sign in", "đăng nhập", "dang nhap", selector);
    }

    public static boolean isCssSelector(String selector) {
        if (selector == null)
            return false;
        return selector.startsWith("#") || selector.startsWith(".") || selector.startsWith("[")
                || selector.contains(">");
    }

    public static boolean isHighPrioritySelector(String selector) {
        if (selector == null)
            return false;
        return selector.startsWith("#") || selector.contains("data-testid");
    }

    public static boolean isEmailField(String selector) {
        String s = selector.toLowerCase();
        return s.contains("email") || s.contains("mail") || s.contains("username");
    }

    public static boolean isPasswordField(String selector) {
        return selector.toLowerCase().contains("password");
    }

    public static boolean isPhoneField(String selector) {
        String s = selector.toLowerCase();
        return s.contains("phone") || s.contains("tel") || s.contains("mobile") || s.contains("sđt");
    }

    public static boolean isUsernameField(String selector) {
        String s = selector.toLowerCase();
        return s.contains("username") || s.contains("user") || s.contains("tài khoản");
    }

    public static boolean isLoginAction(String selector) {
        String s = selector.toLowerCase();
        return s.contains("login") || s.contains("sign in") || s.contains("dang nhap");
    }

    public static String extractDomain(String url) {
        if (url == null || !url.contains("://"))
            return "unknown";
        try {
            java.net.URI uri = new java.net.URI(url);
            String domain = uri.getHost();
            return domain.startsWith("www.") ? domain.substring(4) : domain;
        } catch (Exception e) {
            return "unknown";
        }
    }
}
