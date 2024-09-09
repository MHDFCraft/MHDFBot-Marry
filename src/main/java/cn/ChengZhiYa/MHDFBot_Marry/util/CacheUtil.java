package cn.ChengZhiYa.MHDFBot_Marry.util;

import cn.ChengZhiYa.MHDFBot.entity.YamlConfiguration;
import cn.ChengZhiYa.MHDFBot_Marry.main;
import lombok.Getter;

import java.io.File;

public final class CacheUtil {
    @Getter
    private static final File cacheFile = new File(main.instance.getDataFolder(), "cache.yml");
    @Getter
    private static YamlConfiguration cache;

    public static void reloadCache() {
        cache = YamlConfiguration.loadConfiguration(getCacheFile());
    }
}
