package com.msa.chat_service.domain.chat.controller;

import com.google.api.Http;
import com.msa.chat_service.domain.chat.dto.JwtTokenPropsInfo;
import com.msa.chat_service.domain.chat.dto.request.CreateChatRoomRequest;
import com.msa.chat_service.domain.chat.dto.request.MyChatRoomListRequest;
import com.msa.chat_service.domain.chat.dto.response.*;
import com.msa.chat_service.domain.chat.entity.ChatMessage;
import com.msa.chat_service.domain.chat.entity.ChatRoom;
import com.msa.chat_service.domain.chat.exception.ChatErrorCode;
import com.msa.chat_service.domain.chat.exception.ChatException;
import com.msa.chat_service.domain.chat.exception.JwtTokenErrorCode;
import com.msa.chat_service.domain.chat.exception.JwtTokenException;
import com.msa.chat_service.domain.chat.repository.ChatRoomRepository;
import com.msa.chat_service.domain.chat.service.ChatMessageService;
import com.msa.chat_service.domain.chat.service.ChatRoomService;
import com.msa.chat_service.domain.member.dto.MemberLoginActive;
import com.msa.chat_service.domain.member.entity.enums.MemberRole;
import com.msa.chat_service.global.common.dto.Message;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatRoomController {
    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;
    private final JwtTokenPropsInfo jwtTokenPropsInfo;
    private final ChatRoomRepository chatRoomRepository;
    private final RedisTemplate<String, String> redisTemplate;
    /**
     * 채팅 서비스 메인 페이지로 이동
     */
    @GetMapping("/index")
    public String chatIndex(HttpServletRequest request) {
        String accessToken = getAccessToken(request);
        MemberLoginActive memberLoginActive = parseAccessToken(accessToken);
        String csrfToken = redisTemplate.opsForValue().get("csrfToken::" + memberLoginActive.email());
        log.info("X-CSRF-TOKEN: {}", csrfToken);
        request.setAttribute("csrfToken", csrfToken);
        List<ChatRoomListResponse> chatRooms = chatRoomService.selectChatRooms(null);
        for (ChatRoomListResponse chat: chatRooms){
            System.out.println(chat.chatRoomId() + " " + chat.name());
        }
        request.setAttribute("chatRooms", chatRooms);
        return "index";
    }

    @GetMapping("/create")
    public String createRoom(HttpServletResponse response) {
//        response.addHeader("Set-Cookie",
//                        "accessToken=eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIzIiwiZW1haWwiOiJzc2FmeUBwcmFjdGljZS5jb20iLCJuYW1lIjoi6rmA64-E7JewIiwibmlja25hbWUiOiLrj4Tsl7AiLCJyb2xlIjoiVVNFUiIsImlhdCI6MTczNTAwOTEyMSwiZXhwIjoxNzM1MDM0MzIxfQ.tdLkxfL6bzVdE3O49PLIVdQRVuV9S_ZgbrlALiGcPFU; Max-Age=6000; Expires=Tue, 24 Dec 2024 04:38:41 GMT; Path=/; HttpOnly");
        return "createRoom";
    }

    /**
     * 특정 채팅방 페이지로 이동
     */
    @GetMapping("/room/{chatRoomId}")
    public String chatRoom(@PathVariable Long chatRoomId, Model model, HttpServletRequest request) {
        String accessToken = getAccessToken(request);
        MemberLoginActive memberLoginActive = parseAccessToken(accessToken);
        // 채팅방 ID를 모델에 추가
        model.addAttribute("chatRoomId", chatRoomId);
        List<ChatMessageResponse> chats = chatMessageService.selectChatMessages(chatRoomId, null);
        for (ChatMessageResponse chat: chats){
            System.out.println(chat.getContent() + " " + chat.getSenderNickname());
        }
        request.setAttribute("chatMessages", chats);
        request.setAttribute("senderId", memberLoginActive.id());
        request.setAttribute("senderNickname", memberLoginActive.nickname());
        return "chat-room"; // chatRoom.jsp로 이동
    }

    /**
     * 모든 채팅방 목록 반환 (가상의 메서드)
     * 실제로는 서비스나 DB에서 데이터를 가져와야 함
     */
    private List<ChatRoomListResponse> getChatRooms() {
        // 가상의 데이터. 실제로는 DB나 서비스에서 가져와야 함
        return chatRoomService.selectChatRooms(null);
    }

//    @Operation(
//            summary = "채팅방 목록 조회",
//            description = "채팅방 목록을 조회하는 기능입니다."
//    )
    @GetMapping
    public ResponseEntity<Message<List<ChatRoomListResponse>>> selectChatRooms(Long lastId) {
        List<ChatRoomListResponse> response = chatRoomService.selectChatRooms(lastId);
        return ResponseEntity.ok().body(Message.success(response));
    }

//    @Operation(
//            summary = "내 채팅방 목록 조회",
//            description = "내 채팅방 목록 조회에 필요한 정보를 입력하여 내 채팅방 목록을 조회하는 기능입니다."
//    )
    @GetMapping("/my-rooms")
    //@PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    public ResponseEntity<Message<List<MyChatRoomListResponse>>> selectMyChatRooms(MyChatRoomListRequest request) {
        //List<MyChatRoomListResponse> response = chatRoomService.selectMyChatRooms(loginActive.id(), request);
        //return ResponseEntity.ok().body(Message.success(response));
        return null;
    }

//    @Operation(
//            summary = "채팅방 상세정보 조회",
//            description = "채팅방 상세정보를 조회하는 기능입니다."
//    )
    @GetMapping("/{chatRoomId}")
    //@PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    public ResponseEntity<Message<ChatRoomResponse>> selectChatRoomDetail(@PathVariable Long chatRoomId) {
        ChatRoomResponse response = chatRoomService.selectChatRoomDetail(chatRoomId);
        return ResponseEntity.ok().body(Message.success(response));
    }

//    @Operation(
//            summary = "채팅방 생성",
//            description = "채팅방에 필요한 정보를 입력하여 채팅방을 생성하는 기능입니다."
//    )
    @PostMapping("/create")
    //@PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    public ResponseEntity<?> createChatRoom(@Validated @RequestBody CreateChatRoomRequest createChatRoomRequest, HttpServletRequest request) {
        String accessToken = getAccessToken(request);
        MemberLoginActive memberLoginActive = parseAccessToken(accessToken);
        if (!hasAnyRole(memberLoginActive, "ADMIN") && !hasAnyRole(memberLoginActive, "USER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Message.success());
        }
        Long chatRoomId = chatRoomService.createChatRoom(memberLoginActive.id(), createChatRoomRequest);
        CreateChatRoomResponse response = new CreateChatRoomResponse(chatRoomId);

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.NOT_EXIST_CHAT_ROOM));
        ChatMessage enterMessage = ChatMessage.createEnterMessage(memberLoginActive.id(), memberLoginActive.nickname(), chatRoom);
        chatMessageService.processMessage(enterMessage, memberLoginActive.id());
        return ResponseEntity.ok().body(Message.success(response));
    }

//    @Operation(
//            summary = "인기 채팅방 조회",
//            description = "인기 채팅방 조회에 필요한 정보를 입력하여 조회하는 기능입니다."
//    )
    @GetMapping("/popular-room")
    public ResponseEntity<Message<List<ChatRoomResponse>>> selectPopularChatRoom(String category) {
        List<ChatRoomResponse> response = chatRoomService.selectPopularChatRoom(category);
        return ResponseEntity.ok().body(Message.success(response));
    }

//    @Operation(
//            summary = "채팅방 나가기",
//            description = "채팅방을 나가는 기능입니다."
//    )
    @DeleteMapping("/{chatRoomId}")
    //@PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    public ResponseEntity<Message<Void>> exitChatRoom(@PathVariable Long chatRoomId, HttpServletRequest request) {
        String accessToken = getAccessToken(request);
        MemberLoginActive memberLoginActive = parseAccessToken(accessToken);
        if (!hasAnyRole(memberLoginActive, "ADMIN") && !hasAnyRole(memberLoginActive, "USER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Message.success());
        }
        chatRoomService.exitChatRoom(memberLoginActive.id(), memberLoginActive.nickname(), chatRoomId);
        return ResponseEntity.ok().body(Message.success());
    }

//    @Operation(
//            summary = "채팅방 입장",
//            description = "채팅방에 입장하는 기능입니다."
//    )
    @PostMapping("/{chatRoomId}")
    //@PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    public ResponseEntity<?> enterChatRoom(@PathVariable Long chatRoomId, HttpServletRequest request) {
        String accessToken = getAccessToken(request);
        MemberLoginActive memberLoginActive = parseAccessToken(accessToken);
        if (!hasAnyRole(memberLoginActive, "ADMIN") && !hasAnyRole(memberLoginActive, "USER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Message.success());
        }
        EnterChatRoomResponse response = chatRoomService.enterChatRoom(memberLoginActive.id(), memberLoginActive.nickname(), chatRoomId);
        return ResponseEntity.ok().body(Message.success(response));
    }

//    @Operation(
//            summary = "채팅 메시지 내역",
//            description = "채팅 메시지 내역을 불러오는 기능입니다."
//    )
    @GetMapping("/{chatRoomId}/messages")
    //@PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    public ResponseEntity<?> selectChatMessages(@PathVariable Long chatRoomId, Long lastId, HttpServletRequest request) {
        String accessToken = getAccessToken(request);
        MemberLoginActive memberLoginActive = parseAccessToken(accessToken);
        if (!hasAnyRole(memberLoginActive, "ADMIN") && !hasAnyRole(memberLoginActive, "USER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Message.success());
        }
        List<ChatMessageResponse> response = chatMessageService.selectChatMessages(chatRoomId, lastId);
        return ResponseEntity.ok().body(Message.success(response));
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
