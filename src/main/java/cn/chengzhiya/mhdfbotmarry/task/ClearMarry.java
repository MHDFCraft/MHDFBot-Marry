package cn.chengzhiya.mhdfbotmarry.task;

import cn.chengzhiya.mhdfbot.api.runnable.MHDFBotRunnable;
import cn.chengzhiya.mhdfbotmarry.util.CacheUtil;
import cn.chengzhiya.mhdfbotmarry.util.DatabaseUtil;

import java.time.LocalDate;

public final class ClearMarry extends MHDFBotRunnable {
    @Override
    public void run() {
        if (CacheUtil.getCache().getInt("day") == LocalDate.now().getDayOfMonth()) {
            return;
        }

        DatabaseUtil.clearMarryAndRole();

        CacheUtil.getCache().set("day", LocalDate.now().getDayOfMonth());
        CacheUtil.getCache().save(CacheUtil.getCacheFile());
        CacheUtil.reloadCache();
    }
}
