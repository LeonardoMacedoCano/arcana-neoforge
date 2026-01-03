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
        if (isClient()) {
            return getClientLanguage();
        }

        return DEFAULT_LANG;
    }

    private static boolean isClient() {
        try {
            Minecraft.getInstance();
            return true;
        } catch (NoClassDefFoundError e) {
            return false;
        }
    }

    private static String getClientLanguage() {
        String code = Minecraft.getInstance().getLanguageManager().getSelected().toLowerCase();
        if (!SUPPORTED_LANGS.contains(code)) {
            return DEFAULT_LANG;
        }
        return code;
    }

    public static String getSupportedLanguage(String code) {
        if (code == null) return DEFAULT_LANG;
        code = code.toLowerCase();
        return SUPPORTED_LANGS.contains(code) ? code : DEFAULT_LANG;
    }
}
