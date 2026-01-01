package com.example.arcana.systems.dreams;

import com.example.arcana.ArcanaMod;
import com.example.arcana.systems.dreams.types.DiaryGuideDream;

public class DreamRegistry {

    public static void init() {
        ArcanaMod.LOGGER.debug("Inicializando DreamRegistry");
        DreamManager.register(new DiaryGuideDream());
    }
}
