package com.msa.alarm_service.global.component.firebase.controller;


import com.msa.alarm_service.domain.member.entity.JwtTokenPropsInfo;

import com.msa.alarm_service.domain.member.dto.MemberLoginActive;
import com.msa.alarm_service.domain.member.entity.enums.MemberRole;
import com.msa.alarm_service.global.common.dto.Message;
import com.msa.alarm_service.global.component.firebase.dto.request.FcmSubscribeRequest;
import com.msa.alarm_service.global.component.firebase.dto.request.FcmTokenRequest;
import com.msa.alarm_service.global.component.firebase.dto.request.FcmTopicRequest;
import com.msa.alarm_service.global.component.firebase.exception.JwtTokenErrorCode;
import com.msa.alarm_service.global.component.firebase.exception.JwtTokenException;
import com.msa.alarm_service.global.component.firebase.repository.DeviceTokenRepository;
import com.msa.alarm_service.global.component.firebase.service.FirebaseService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/alarm")
public class FirebaseController {
    private final FirebaseService firebaseService;
    private final JwtTokenPropsInfo jwtTokenPropsInfo;
    private final DeviceTokenRepository deviceTokenRepository;

//    @Operation(
//            summary = "이미지 파일 업로드",
//            description = "이미지 파일을 파이어베이스 스토리지에 업로드 합니다."
//    )
//    @PostMapping("/upload")
//    //@PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
//    public ResponseEntity<Message<String>> uploadFile(@RequestParam("file") MultipartFile file,
//                                                      @RequestParam("fileName") String fileName) {
//        String url = firebaseService.uploadFiles(file, fileName);
//        return ResponseEntity.ok().body(Message.success(url));
//    }

//    @Operation(
//            summary = "FCM 디바이스 토큰 저장",
//            description = "FCM 디바이스 토큰을 저장하는 기능입니다."
//    )
    @PostMapping("/message/{deviceToken}")
    //@PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    public ResponseEntity<Message<Void>> createDeviceToken(@PathVariable String deviceToken, HttpServletRequest request) {
        String accessToken = getAccessToken(request);
        MemberLoginActive memberLoginActive = parseAccessToken(accessToken);
        firebaseService.createDeviceToken(memberLoginActive.id(), deviceToken);
        return ResponseEntity.ok().body(Message.success());
    }

//    @Operation(
//            summary = "FCM 디바이스 토큰 삭제",
//            description = "FCM 디바이스 토큰을 삭제하는 기능입니다."
//    )
    @DeleteMapping("/message")
    //@PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    public ResponseEntity<Message<Void>> deleteDeviceToken(HttpServletRequest request) {
        String accessToken = getAccessToken(request);
        MemberLoginActive memberLoginActive = parseAccessToken(accessToken);
        firebaseService.deleteDeviceToken(memberLoginActive.id());
        return ResponseEntity.ok().body(Message.success());
    }
//
//    @Operation(
//            summary = "topic으로 알림 보내기",
//            description = "topic으로 알림을 보내는 기능입니다."
//    )
    @PostMapping("/message/topic")
    //@PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    public ResponseEntity<Message<Void>> sendMessageTopic(@RequestBody FcmTopicRequest fcmTopicRequest) {
        //firebaseService.sendMessageByTopic(fcmTopicRequest);
        return ResponseEntity.ok().body(Message.success());
    }

//    @Operation(
//            summary = "token으로 알림 보내기",
//            description = "token으로 알림을 보내는 기능입니다."
//    )
    @PostMapping("/message/token")
    //@PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    public ResponseEntity<Message<Void>> sendMessageToken(@RequestBody FcmTokenRequest fcmTokenRequest) {
        firebaseService.sendMessageByToken(fcmTokenRequest);
        return ResponseEntity.ok().body(Message.success());
    }

//    @Operation(
//            summary = "특정 topic 구독하기",
//            description = "특정 topic을 구독하는 기능입니다."
//    )
    @PostMapping("/message/subscribe")
    //@PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    public ResponseEntity<Message<Void>> subscribeByTopic(@RequestBody FcmSubscribeRequest fcmSubscribeRequest) {
        firebaseService.subscribeByTopic(fcmSubscribeRequest);
        return ResponseEntity.ok().body(Message.success());
    }

    @PostMapping("/message/unsubscribe")
    //@PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    public ResponseEntity<Message<Void>> unsubscribeByTopic(@RequestBody FcmSubscribeRequest fcmSubscribeRequest) {
        firebaseService.unsubscribeByTopic(fcmSubscribeRequest);
        return ResponseEntity.ok().body(Message.success());
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

    @PostMapping("/get/token")
    public ResponseEntity<String> getTokenListById(@RequestBody Long memberId) {
        List<String> list = deviceTokenRepository.findTokenAllByMember(memberId).orElse(null);
        if (list == null) {
            return ResponseEntity.ok(null);
        }
        String token = list.get(0);
        return ResponseEntity.ok(token);
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

