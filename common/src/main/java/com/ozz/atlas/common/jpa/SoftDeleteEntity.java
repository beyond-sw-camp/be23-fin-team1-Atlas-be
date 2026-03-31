package com.ozz.atlas.common.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

/**
 * 소프트 딜리트(논리 삭제) 기능을 제공하는 공통 엔티티
 */
@Getter
@MappedSuperclass
public abstract class SoftDeleteEntity extends BaseTimeEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "del_yn", nullable = false, length = 1)
    private DelYn delYn = DelYn.N;

    /**
     * 엔티티 삭제 처리 (논리 삭제)
     */
    public void delete() {
        this.delYn = DelYn.Y;
    }

    /**
     * 삭제 여부 확인
     */
    public boolean isDeleted() {
        return this.delYn == DelYn.Y;
    }
}
