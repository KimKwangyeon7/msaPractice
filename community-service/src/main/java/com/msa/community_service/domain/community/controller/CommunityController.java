package com.msa.community_service.domain.community.controller;

import com.msa.community_service.domain.community.dto.info.JwtTokenPropsInfo;
import com.msa.community_service.domain.community.dto.info.MemberLoginActive;
import com.msa.community_service.domain.community.dto.request.*;
import com.msa.community_service.domain.community.dto.response.CommentListResponse;
import com.msa.community_service.domain.community.dto.response.CommunityDetailResponse;
import com.msa.community_service.domain.community.dto.response.CommunityListResponse;
import com.msa.community_service.domain.community.dto.response.PopularCommunityListResponse;
import com.msa.community_service.domain.community.entity.enums.MemberRole;
import com.msa.community_service.domain.community.exception.JwtTokenErrorCode;
import com.msa.community_service.domain.community.exception.JwtTokenException;
import com.msa.community_service.domain.community.service.CommentService;
import com.msa.community_service.domain.community.service.CommunityService;
import com.msa.community_service.global.common.dto.Message;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/community")
public class CommunityController {
    private final CommunityService communityService;
    private final CommentService commentService;
    private final JwtTokenPropsInfo jwtTokenPropsInfo;
//    @Operation(
//            summary = "게시글 작성",
//            description = "커뮤니티 게시글을 작성하는 기능입니다."
//    )
    @PostMapping
    //@PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    public ResponseEntity<Message<Void>> createCommunity(@Validated @RequestBody CreateCommunityRequest createCommunityRequest, HttpServletRequest request) {
        String accessToken = getAccessToken(request);
        MemberLoginActive memberLoginActive = parseAccessToken(accessToken);
        if (!hasAnyRole(memberLoginActive, "ADMIN") && !hasAnyRole(memberLoginActive, "USER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Message.success());
        }
        communityService.createCommunity(memberLoginActive.id(), createCommunityRequest);
        return ResponseEntity.ok().body(Message.success());
    }

//    @Operation(
//            summary = "게시글 목록 조회",
//            description = "커뮤니티 게시글 목록을 조회하는 기능입니다."
//    )
    @GetMapping
    public ResponseEntity<Message<List<CommunityListResponse>>> selectCommunityList(CommunityListRequest communityListRequest) {
        return ResponseEntity.ok().body(Message.success(communityService.selectCommunityList(communityListRequest)));
    }

//    @Operation(
//            summary = "인기 게시글 조회",
//            description = "커뮤니티 인기 게시글을 조회하는 기능입니다."
//    )
    @GetMapping("/popular")
    public ResponseEntity<Message<List<PopularCommunityListResponse>>> selectPopularCommunityList() {
        return ResponseEntity.ok().body(Message.success(communityService.selectPopularCommunityList()));
    }

//    @Operation(
//            summary = "게시글 상세 조회",
//            description = "커뮤니티 게시글을 상세 조회하는 기능입니다."
//    )
    @GetMapping("/{communityId}")
    public ResponseEntity<Message<CommunityDetailResponse>> selectCommunity(@PathVariable Long communityId) {
        return ResponseEntity.ok().body(Message.success(communityService.selectCommunity(communityId)));
    }

//    @Operation(
//            summary = "게시글 삭제",
//            description = "커뮤니티 게시글을 삭제하는 기능입니다."
//    )
    @DeleteMapping("/{communityId}")
    public ResponseEntity<Message<Void>> deleteCommunity(@PathVariable Long communityId, HttpServletRequest request) {
        String accessToken = getAccessToken(request);
        MemberLoginActive memberLoginActive = parseAccessToken(accessToken);
        if (!hasAnyRole(memberLoginActive, "ADMIN") && !hasAnyRole(memberLoginActive, "USER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Message.success());
        }
        communityService.deleteCommunity(communityId, memberLoginActive);
        return ResponseEntity.ok().body(Message.success());
    }

//    @Operation(
//            summary = "게시글 수정",
//            description = "커뮤니티 게시글을 수정하는 기능입니다."
//    )
    @PatchMapping("/{communityId}")
    public ResponseEntity<Message<Void>> updateCommunity(@PathVariable Long communityId,
                                                         @Validated @RequestBody UpdateCommunityRequest updateCommunityRequest, HttpServletRequest request) {
        String accessToken = getAccessToken(request);
        MemberLoginActive memberLoginActive = parseAccessToken(accessToken);
        if (!hasAnyRole(memberLoginActive, "ADMIN") && !hasAnyRole(memberLoginActive, "USER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Message.success());
        }
        communityService.updateCommunity(communityId, updateCommunityRequest, memberLoginActive);
        return ResponseEntity.ok().body(Message.success());
    }

//    @Operation(
//            summary = "댓글 작성",
//            description = "커뮤니티 댓글을 작성하는 기능입니다."
//    )
    @PostMapping("/{communityId}/comment")
    public ResponseEntity<Message<Void>> createComment(@PathVariable Long communityId,
                                                       @Validated @RequestBody CreateCommentRequest createCommentRequest, HttpServletRequest request) {
        String accessToken = getAccessToken(request);
        MemberLoginActive memberLoginActive = parseAccessToken(accessToken);
        if (!hasAnyRole(memberLoginActive, "ADMIN") && !hasAnyRole(memberLoginActive, "USER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Message.success());
        }
        commentService.createComment(memberLoginActive.id(), communityId, createCommentRequest.content());
        return ResponseEntity.ok().body(Message.success());
    }

//    @Operation(
//            summary = "댓글 목록 조회",
//            description = "커뮤니티 댓글 목록을 조회하는 기능입니다."
//    )
    @GetMapping("/{communityId}/comment")
    public ResponseEntity<Message<List<CommentListResponse>>> selectCommentList(@PathVariable Long communityId, Long lastId) {
        return ResponseEntity.ok().body(Message.success(commentService.selectCommentList(communityId, lastId)));
    }

//    @Operation(
//            summary = "댓글 삭제 ",
//            description = "커뮤니티 댓글을 삭제하는 기능입니다."
//    )
    @DeleteMapping("/{communityId}/comment/{commentId}")
    //@PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    public ResponseEntity<Message<Void>> deleteComment(@PathVariable Long communityId, @PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok().body(Message.success());
    }

//    @Operation(
//            summary = "댓글 수정 ",
//            description = "커뮤니티 댓글을 수정하는 기능입니다."
//    )
    @PatchMapping("/{communityId}/comment/{commentId}")
    //@PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    public ResponseEntity<Message<Void>> updateComment(@PathVariable Long commentId,
                                                       @Validated @RequestBody UpdateCommentRequest request) {
        commentService.updateComment(commentId, request);
        return ResponseEntity.ok().body(Message.success());
    }

    private boolean hasAnyRole(MemberLoginActive memberLoginActive, String role) {
        try {
            return memberLoginActive.role().name().equals(role);// 모두 일치하지 않으면 false
        } catch (Exception e) {
            e.printStackTrace();
            return false; // 예외 발생 시 권한 없음
        }
    }

    private String getAccessToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) { // JWT 쿠키 이름 확인
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private MemberLoginActive parseAccessToken(String accessToken) {
        // 액세스 토큰 발급
        Claims payload = parseToken(accessToken, jwtTokenPropsInfo.accessKey());

        // 파싱된 데이터를 기반으로 MemberLoginActive 객체 생성 및 반환
        return new MemberLoginActive(
                Long.valueOf(payload.getId()),
                payload.get("email", String.class),
                payload.get("name", String.class),
                payload.get("nickname", String.class),
                MemberRole.fromName(payload.get("role", String.class))
        );
    }

    private Claims parseToken(String token, String secretKey) {
        Claims payload;
        try {
            // 토큰을 파싱하여 payload를 반환합니다. 이 과정에서 토큰의 무결성과 유효성이 검증됩니다.
            payload = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                    .build()
                    .parseSignedClaims(token).getPayload();
        } catch (ExpiredJwtException e) {
            // 토큰 만료 예외 처리
            throw new JwtTokenException(JwtTokenErrorCode.EXPIRED_TOKEN);
        } catch (MalformedJwtException | SecurityException | IllegalArgumentException e) {
            // 토큰 형식 불일치 예외 처리
            throw new JwtTokenException(JwtTokenErrorCode.INVALID_TOKEN);
        }
//        } catch (SignatureException e) {
//            // 토큰 서명 검증 실패 예외 처리
//            throw new JwtTokenException(JwtTokenErrorCode.SIGNATURE_INVALID);
//        }
        return payload;
    }
}
