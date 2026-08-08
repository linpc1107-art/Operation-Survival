package com.operation.survival.dto;

public class BattleRequest {

    private Long monsterId;
    private Boolean useSkill;

    public BattleRequest() {
    }

    public Long getMonsterId() {
        return monsterId;
    }

    public void setMonsterId(Long monsterId) {
        this.monsterId = monsterId;
    }

    public Boolean getUseSkill() {
        return useSkill;
    }

    public void setUseSkill(Boolean useSkill) {
        this.useSkill = useSkill;
    }
}