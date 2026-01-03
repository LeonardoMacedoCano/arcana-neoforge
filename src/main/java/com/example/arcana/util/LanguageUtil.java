package com.example.arcana.util;

import net.minecraft.client.Minecraft;

import java.util.Set;

public class LanguageUtil {

    private static final String DEFAULT_LANG = "en_us";

    private static final Set<String> SUPPORTED_LANGS = Set.of(
            "en_us",
            "pt_br"
    );

    public static String getSupportedLanguage() {

        String code = Minecraft.getInstance()
                .getLanguageManager()
                .getSelected();

        code = code.toLowerCase();

        if (!SUPPORTED_LANGS.contains(code)) {
            return DEFAULT_LANG;
        }

        return code;
    }
}
