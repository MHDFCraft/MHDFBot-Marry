package cn.ChengZhiYa.MHDFBot_Marry.util;

import cn.ChengZhiYa.MHDFBot.entity.YamlConfiguration;
import cn.ChengZhiYa.MHDFBot_Marry.main;

import java.io.File;

public final class LangUtil {
    private static YamlConfiguration lang;

    public static void reloadLang() {
        lang = YamlConfiguration.loadConfiguration(new File(main.instance.getDataFolder(), "lang.yml"));
    }

    public static String i18n(String key) {
        if (lang == null) {
            reloadLang();
        }
        return lang.getString(key);
    }
}
