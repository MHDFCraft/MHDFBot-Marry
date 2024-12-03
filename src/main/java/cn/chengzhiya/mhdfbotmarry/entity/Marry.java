package cn.chengzhiya.mhdfbotmarry.entity;

import lombok.Data;

@Data
public final class Marry {
    private Long mr;
    private Long mrs;

    public Marry(Long mr, Long mrs) {
        this.mr = mr;
        this.mrs = mrs;
    }
}
