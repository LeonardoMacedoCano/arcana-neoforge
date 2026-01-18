package com.example.arcana.util;

import java.util.Set;

public class LanguageUtil {

    private static final String DEFAULT_LANG = "en_us";

    private static final Set<String> SUPPORTED_LANGS = Set.of(
            "en_us",
            "pt_br"
    );

    public static String validate(String code) {
        if (code == null || code.isEmpty()) {
            return DEFAULT_LANG;
        }

        String normalized = code.toLowerCase();
        return SUPPORTED_LANGS.contains(normalized) ? normalized : DEFAULT_LANG;
    }
}