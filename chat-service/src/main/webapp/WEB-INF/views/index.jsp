<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>
<%@ page import="com.msa.chat_service.domain.chat.dto.response.ChatRoomListResponse" %>
<%
    // 채팅방 목록 가져오기
    List<ChatRoomListResponse> chatRooms = (List<ChatRoomListResponse>) request.getAttribute("chatRooms");
    String csrfToken = request.getAttribute("csrfToken").toString();
%>
<!DOCTYPE html>
<html>
<head>
    <title>채팅 서비스</title>
    <script>
        const csrfToken = "<%= csrfToken %>";
        function enterChatRoom(button) {
            // data-chat-room-id 속성에서 채팅방 ID를 읽어옵니다.
            const chatRoomId = button.getAttribute("data-chat-room-id");
            console.log("Chat Room ID:", chatRoomId); // 확인
            if (!chatRoomId) {
                alert("올바른 채팅방 ID가 아닙니다.");
                return;
            }

            fetch('/chat/' + chatRoomId, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    'X-CSRF-TOKEN': csrfToken
                },
                credentials: 'include', // 쿠키 포함
            })
                .then(response => {
                    if (response.ok) {
                        // POST 요청 성공 시 GET 방식으로 채팅방 페이지 이동
                        window.location.href = '/chat/room/' + chatRoomId;
                    } else {
                        return response.json().then(err => {
                            console.error('Error:', err);
                            alert("채팅방 입장에 실패했습니다.");
                        });
                    }
                })
                .catch(error => {
                    console.error("Network Error:", error);
                    alert("네트워크 오류가 발생했습니다.");
                });
        }
    </script>
</head>
<body>
<h1>채팅방 목록</h1>

<ul>
    <% if (chatRooms != null && !chatRooms.isEmpty()) {
        for (ChatRoomListResponse room : chatRooms) { %>
    <li>
        <!-- data-chat-room-id 속성을 사용해 채팅방 ID 저장 -->
        <button onclick="enterChatRoom(this)" data-chat-room-id="<%= room.chatRoomId() %>">
            <%= room.name() %> (현재 인원: <%= room.memberCount() %> / 최대 <%= room.limit() %>)
        </button>
    </li>
    <% } } else { %>
    <p>현재 생성된 채팅방이 없습니다.</p>
    <% } %>
</ul>

<button onclick="window.location.href='/chat/create'">채팅방 생성</button>

</body>
</html>
