package com.example.arcana.systems.dreams;

import com.example.arcana.systems.dreams.types.DiaryGuideDream;
import com.example.arcana.util.ArcanaLog;

public class DreamRegistry {
    private static final String MODULE = "DREAM_REGISTRY";

    public static void init() {
        ArcanaLog.debug(MODULE, "Initializing DreamRegistry");
        DreamManager.register(new DiaryGuideDream());
        ArcanaLog.debug(MODULE, "DreamRegistry initialized successfully");
    }
}
