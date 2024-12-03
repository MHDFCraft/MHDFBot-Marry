package cn.chengzhiya.mhdfbotmarry;

import cn.chengzhiya.mhdfbot.api.plugin.JavaPlugin;
import cn.chengzhiya.mhdfbotmarry.entity.DatabaseConfig;
import cn.chengzhiya.mhdfbotmarry.listener.GroupMessage;
import cn.chengzhiya.mhdfbotmarry.task.ClearMarry;
import cn.chengzhiya.mhdfbotmarry.util.CacheUtil;
import cn.chengzhiya.mhdfbotmarry.util.DatabaseUtil;
import cn.chengzhiya.mhdfbotmarry.util.LangUtil;

import java.time.LocalDate;

public final class main extends JavaPlugin {
    public static main instance;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        reloadConfig();

        saveResource("lang.yml", "lang.yml", false);
        LangUtil.reloadLang();

        saveResource("cache.yml", "cache.yml", false);
        CacheUtil.reloadCache();

        DatabaseUtil.connectDatabase(new DatabaseConfig(
                getConfig().getString("databaseSettings.host"),
                getConfig().getString("databaseSettings.database"),
                getConfig().getString("databaseSettings.user"),
                getConfig().getString("databaseSettings.password")
        ));
        DatabaseUtil.intiDatabase();

        if (CacheUtil.getCache().getInt("day") != LocalDate.now().getDayOfMonth()) {
            DatabaseUtil.clearMarryAndRole();

            CacheUtil.getCache().set("day", LocalDate.now().getDayOfMonth());
            CacheUtil.getCache().save(CacheUtil.getCacheFile());
            CacheUtil.reloadCache();
        }

        registerListener(new GroupMessage());

        new ClearMarry().runTaskAsynchronouslyTimer(0L, 20L);

        getLogger().info("===========梦之结婚============");
        getLogger().info("插件加载成功!");
        getLogger().info("===========梦之结婚============");
    }

    @Override
    public void onDisable() {
        instance = null;

        getLogger().info("===========梦之结婚============");
        getLogger().info("插件卸载成功!");
        getLogger().info("===========梦之结婚============");
    }
}