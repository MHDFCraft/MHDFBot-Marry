package cn.chengzhiya.mhdfbotmarry.util;

import cn.chengzhiya.mhdfbot.api.entity.config.YamlConfiguration;
import cn.chengzhiya.mhdfbotmarry.main;
import lombok.Getter;

import java.io.File;

public final class CacheUtil {
    @Getter
    private static final File cacheFile = new File(main.instance.getDataFolder(), "cache.yml");
    @Getter
    private static YamlConfiguration cache;

    /**
     * 重载缓存文件
     */
    public static void reloadCache() {
        cache = YamlConfiguration.loadConfiguration(getCacheFile());
    }
}
