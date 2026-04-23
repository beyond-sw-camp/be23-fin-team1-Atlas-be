package com.ozz.atlas.auth.domain;

import com.ozz.atlas.common.jpa.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "security_history")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecurityHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long securityHistoryId;

    // 어떤 사용자의 보안 이력인지 연결
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    // 내부 구분용 이벤트 타입
    // enum 대신 문자열로 단순하게 저장
    @Column(nullable = false, length = 50)
    private String eventType;

    // 화면에 보여줄 간단한 설명
    @Column(nullable = false, length = 255)
    private String summary;

    // 요청이 들어온 IP 주소
    @Column(length = 100)
    private String ipAddress;

    // 브라우저 또는 클라이언트 정보
    @Column(length = 500)
    private String userAgent;
}
