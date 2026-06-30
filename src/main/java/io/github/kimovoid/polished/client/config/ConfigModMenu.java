package io.github.kimovoid.polished.client.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.kimovoid.silk.SilkConfig;
import io.github.kimovoid.polished.client.PolishedClient;

public class ConfigModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> SilkConfig.getScreen(PolishedClient.CONFIG, "general", parent);
    }
}
