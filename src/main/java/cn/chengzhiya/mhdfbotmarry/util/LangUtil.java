package cn.chengzhiya.mhdfbotmarry.util;

import cn.chengzhiya.mhdfbot.api.entity.config.YamlConfiguration;
import cn.chengzhiya.mhdfbotmarry.main;

import java.io.File;

public final class LangUtil {
    private static YamlConfiguration lang;

    /**
     * 加载语言文件
     */
    public static void reloadLang() {
        lang = YamlConfiguration.loadConfiguration(new File(main.instance.getDataFolder(), "lang.yml"));
    }

    /**
     * 获取指定key在语言文件中对应的文本
     */
    public static String i18n(String key) {
        if (lang == null) {
            reloadLang();
        }
        return lang.getString(key);
    }
}
