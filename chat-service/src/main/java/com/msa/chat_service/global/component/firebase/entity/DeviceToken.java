package com.msa.chat_service.global.component.firebase.entity;

import com.msa.chat_service.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceToken {
    @Id
    @Comment("디바이스 토큰")
    @Column(name = "device_token", columnDefinition = "VARCHAR(255)")
    private String token;

    @Comment("회원 아이디")
    @Column(columnDefinition = "INT UNSIGNED", nullable = false)
    private Long memberId;
}
