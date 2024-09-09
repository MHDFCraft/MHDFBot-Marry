package cn.ChengZhiYa.MHDFBot_Marry.entity;

import lombok.Data;

@Data
public final class User {
    Long group;
    Long qq;

    public User(Long group, Long qq) {
        this.group = group;
        this.qq = qq;
    }
}
