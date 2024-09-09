package cn.ChengZhiYa.MHDFBot_Marry;

import cn.ChengZhiYa.MHDFBot.api.MHDFBotPlugin;
import cn.ChengZhiYa.MHDFBot_Marry.entity.DatabaseConfig;
import cn.ChengZhiYa.MHDFBot_Marry.listener.GroupMessage;
import cn.ChengZhiYa.MHDFBot_Marry.task.clearMarry;
import cn.ChengZhiYa.MHDFBot_Marry.util.CacheUtil;
import cn.ChengZhiYa.MHDFBot_Marry.util.DatabaseUtil;

import java.time.LocalDate;

import static cn.ChengZhiYa.MHDFBot_Marry.util.CacheUtil.reloadCache;
import static cn.ChengZhiYa.MHDFBot_Marry.util.DatabaseUtil.connectDatabase;
import static cn.ChengZhiYa.MHDFBot_Marry.util.DatabaseUtil.intiDatabase;
import static cn.ChengZhiYa.MHDFBot_Marry.util.LangUtil.reloadLang;

public final class main extends MHDFBotPlugin {
    public static main instance;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        saveResource(getDataFolder().getPath(), "lang.yml", "lang.yml", false);
        reloadLang();
        saveResource(getDataFolder().getPath(), "cache.yml", "cache.yml", false);
        reloadCache();

        connectDatabase(new DatabaseConfig(
                getConfig().getString("DatabaseSettings.Host"),
                getConfig().getString("DatabaseSettings.Database"),
                getConfig().getString("DatabaseSettings.User"),
                getConfig().getString("DatabaseSettings.Password")
        ));
        intiDatabase();

        if (CacheUtil.getCache().getInt("Day") != LocalDate.now().getDayOfMonth()) {
            DatabaseUtil.clearMarryAndRole();
            CacheUtil.getCache().set("Day", LocalDate.now().getDayOfMonth());
            CacheUtil.getCache().save(CacheUtil.getCacheFile());
            reloadCache();
        }

        registerListener(new GroupMessage());

        new clearMarry().runTaskAsynchronouslyTimer(0L, 20L);

        colorLog("&r===========&6梦之结婚&r============");
        colorLog("&a插件加载成功!");
        colorLog("&r===========&6梦之结婚&r============");
    }

    @Override
    public void onDisable() {
        instance = null;

        colorLog("&r===========&6梦之结婚&r============");
        colorLog("&a插件卸载成功!");
        colorLog("&r===========&6梦之结婚&r============");
    }
}