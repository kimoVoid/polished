package io.github.kimovoid.polished.mixin.client.resources;

import net.minecraft.client.resource.ResourceDownloader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import java.net.MalformedURLException;
import java.net.URL;

@Mixin(ResourceDownloader.class)
public class ResourceDownloaderMixin {

    @ModifyConstant(method = "run", constant = @Constant(stringValue = "http://s3.amazonaws.com/MinecraftResources/"), remap = false)
    private String getResourcesUrl(String def) {
        try {
            return this.replaceHost(new URL(def), "betacraft.uk", 11705).toString();
        } catch (MalformedURLException ignored) {
            return def;
        }
    }

    @Unique
    private URL replaceHost(URL url, String hostName, int port) {
        try {
            return new URL(url.getProtocol(), hostName, port, url.getFile());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
