package com.arcana.mod.systems.dreams;

import com.arcana.mod.systems.dreams.types.DiaryGuideDream;
import com.arcana.mod.util.common.ArcanaLog;

public class DreamRegistry {
    private static final String MODULE = "DREAM_REGISTRY";

    public static void init() {
        ArcanaLog.debug(MODULE, "Initializing DreamRegistry");
        DreamManager.clear();
        DreamManager.register(new DiaryGuideDream());
        ArcanaLog.debug(MODULE, "DreamRegistry initialized successfully");
    }
}
