package com.msa.community_service.domain.community.entity;



import com.msa.community_service.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "comment")
public class Comments extends BaseEntity {
    @Id
    @Comment("댓글 아이디")
    @Column(columnDefinition = "INT UNSIGNED")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("커뮤니티 글 아이디")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id", nullable = false)
    private Community community;

    @Comment("작성자 아이디")
    @Column(columnDefinition = "INT UNSIGNED", nullable = false)
    private Long writerId;

    @Comment("댓글 내용")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    public void update(String content) {
        this.content = content;
    }
}
