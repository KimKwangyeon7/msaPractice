<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>
<%@ page import="com.msa.chat_service.domain.chat.dto.response.ChatMessageResponse" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%
    String chatRoomId = request.getAttribute("chatRoomId").toString();
    String senderId = request.getAttribute("senderId").toString();
    String senderNickname = request.getAttribute("senderNickname").toString();
    List<ChatMessageResponse> chatMessages = (List<ChatMessageResponse>) request.getAttribute("chatMessages");
    // 채팅 내역을 오래된 순으로 정렬
    if (chatMessages != null) {
        chatMessages.sort(Comparator.comparing(ChatMessageResponse::getCreatedAt));
    }
    // 시간 포맷 정의
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
%>
<!DOCTYPE html>
<html>
<head>
    <title>채팅방 <% out.print(chatRoomId); %></title>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.5.1/sockjs.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script>
    <script>
        let stompClient = null;

        // WebSocket 연결
        function connect() {
            const socket = new WebSocket('ws://localhost:9003/ws');
            stompClient = Stomp.over(socket);

            stompClient.connect({}, function (frame) {
                console.log('Connected: ' + frame);

                // 채팅방 메시지 구독
                stompClient.subscribe(`/topic/public/rooms/${chatRoomId}`, function (message) {
                    const chatMessage = JSON.parse(message.body);
                    showMessage(chatMessage);
                });
            });
        }

        // 메시지 전송
        function sendMessage() {
            const message = document.getElementById("message").value;

            // 서버 시간 포맷에 맞춰 현재 시간 생성
            const formatter = new Intl.DateTimeFormat("ko-KR", {
                year: "numeric",
                month: "2-digit",
                day: "2-digit",
                hour: "2-digit",
                minute: "2-digit",
                second: "2-digit",
                hour12: false // 24시간 형식 사용
            });
            const now = new Date();
            const createdAt = formatter.format(now).replace(/\./g, "-").replace(/\s+/g, ""); // 서버 형식에 맞게 변환

            const chatMessage = {
                chatRoomId: "<%= chatRoomId %>",
                content: message,
                senderId: "<%= senderId %>",
                senderNickname: "<%= senderNickname %>",
                createdAt: createdAt
            };
            // 채팅 내역에 즉시 추가
            //showMessage(chatMessage);
            stompClient.send(`/app/chat/message/${chatRoomId}`, {}, JSON.stringify(chatMessage));
            // 입력 필드 초기화
            document.getElementById("message").value = "";
        }

        // 메시지 표시
        function showMessage(message) {
            const chatBox = document.getElementById("chatBox");
            const messageElement = document.createElement("div");

            // 메시지 내용 구성
            const sender = message.senderNickname || "알 수 없는 사용자";
            const content = message.content || "[오류: 내용 없음]";
            const createdAt = message.createdAt || "[시간 정보 없음]";
            messageElement.textContent = "[" + sender + "]" + content + "(" + createdAt + ")";
            chatBox.appendChild(messageElement);

            // 스크롤 자동 아래로 이동
            //chatBox.scrollTop = chatBox.scrollHeight;
        }

        // 채팅방 나가기
        function leaveChatRoom() {
            const chatRoomId = "<%= chatRoomId %>";
            if (confirm("채팅방에서 나가시겠습니까?")) {
                fetch("/chat/" + chatRoomId, {
                    method: "DELETE",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    credentials: "include",
                })
                    .then(response => {
                        if (response.ok) {
                            alert("채팅방에서 나왔습니다.");
                            window.location.href = "/chat/index"; // 채팅방 목록 페이지로 이동
                        } else {
                            return response.json().then(err => {
                                console.error("Error:", err);
                                alert("채팅방 나가기에 실패했습니다.");
                            });
                        }
                    })
                    .catch(error => {
                        console.error("Network Error:", error);
                        alert("네트워크 오류가 발생했습니다.");
                    });
            }
        }

        // 페이지 로드 시 WebSocket 연결
        window.onload = function () {
            connect();
        };
    </script>
</head>
<body>
<h1>채팅방 <% out.print(chatRoomId); %></h1>

<!-- 채팅 내역 표시 -->
<div id="chatBox" style="border: 1px solid #ccc; width: 600px; height: 500px; overflow-y: scroll; margin-bottom: 10px;">
    <% if (chatMessages != null && !chatMessages.isEmpty()) {
        for (ChatMessageResponse message : chatMessages) { %>
    <div>[<%= message.getSenderNickname() %>] <%= message.getContent() %> (<%= message.getCreatedAt().format(formatter) %>)</div>
    <% } } else { %>
    <div>채팅 내역이 없습니다.</div>
    <% } %>
</div>

<input type="text" id="message" placeholder="메시지를 입력하세요" style="width: 200px;">
<button onclick="sendMessage()">전송</button>

<!-- 채팅방 나가기 버튼 -->
<button onclick="leaveChatRoom()">채팅방 나가기</button>

<a href="/chat/index">메인 페이지로 돌아가기</a>
</body>
</html>