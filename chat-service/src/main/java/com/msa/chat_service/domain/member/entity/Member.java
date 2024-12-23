package com.msa.chat_service.domain.member.entity;


import com.msa.chat_service.domain.member.entity.enums.MemberRole;
import com.msa.chat_service.domain.member.entity.enums.OAuthDomain;
import com.msa.chat_service.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(indexes = {
        @Index(name = "idx_email", columnList = "email")
})
public class Member extends BaseEntity {

    @Id
    @Comment("회원 아이디")
    @Column(columnDefinition = "INT UNSIGNED")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("이메일")
    @Column(nullable = false)
    private String email;

    @Comment("비밀번호")
    @Column(columnDefinition = "VARCHAR(80)")
    private String password;

    @Comment("이름")
    @Column(columnDefinition = "VARCHAR(40)")
    private String name;

    @Comment("닉네임")
    @Column(columnDefinition = "VARCHAR(60)", nullable = false)
    private String nickname;

    @Comment("프로필 이미지 URL")
    private String profileImage;

    @Comment("권한")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;

    @Column(name = "provider")
    @Enumerated(EnumType.STRING)
    @Comment("소셜 로그인 제공업체")
    private OAuthDomain oAuthDomain;

    public Member(Long memberId, String email, String nickname, String profileImage, MemberRole user) {
        super();
        this.id = memberId;
        this.email = email;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.role = user;
    }

//    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<ChatMessage> chatMessages = new ArrayList<>();
//
//    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<ChatRoomMember> chatRoomMembers = new ArrayList<>();
//
//    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<DeviceToken> deviceTokens = new ArrayList<>();
//
//    @OneToMany(mappedBy = "writer", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Community> communities = new ArrayList<>();
//
//    @OneToMany(mappedBy = "writer", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Comments> comments = new ArrayList<>();
}
