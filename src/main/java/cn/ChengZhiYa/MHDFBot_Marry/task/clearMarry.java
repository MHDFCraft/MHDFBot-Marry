package cn.ChengZhiYa.MHDFBot_Marry.task;

import cn.ChengZhiYa.MHDFBot.api.MHDFBotRunnable;
import cn.ChengZhiYa.MHDFBot_Marry.util.CacheUtil;
import cn.ChengZhiYa.MHDFBot_Marry.util.DatabaseUtil;

import java.time.LocalDate;

import static cn.ChengZhiYa.MHDFBot_Marry.util.CacheUtil.reloadCache;

public final class clearMarry extends MHDFBotRunnable {
    @Override
    public void run() {
        if (CacheUtil.getCache().getInt("Day") != LocalDate.now().getDayOfMonth()) {
            DatabaseUtil.clearMarryAndRole();
            CacheUtil.getCache().set("Day", LocalDate.now().getDayOfMonth());
            CacheUtil.getCache().save(CacheUtil.getCacheFile());
            reloadCache();
        }
    }
}
