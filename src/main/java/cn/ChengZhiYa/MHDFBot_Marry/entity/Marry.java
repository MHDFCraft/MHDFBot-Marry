package cn.ChengZhiYa.MHDFBot_Marry.entity;

import lombok.Data;

@Data
public final class Marry {
    Long mr;
    Long mrs;

    public Marry(Long mr, Long mrs) {
        this.mr = mr;
        this.mrs = mrs;
    }
}
