<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>
<%
    String chatRoomId = request.getAttribute("chatRoomId").toString();
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
            const socket = new WebSocket('ws://localhost:9003/ws'); // WebSocket 사용 시
            stompClient = Stomp.over(socket);

            stompClient.connect({}, function (frame) {
                console.log('Connected: ' + frame);

                // 메시지 구독
                <%--stompClient.subscribe(`/topic/public/rooms/${<%= roomId %>}`, function (message) {--%>
                <%--    showMessage(JSON.parse(message.body).content);--%>
                <%--});--%>
            });
        }

        // 메시지 전송
        function sendMessage() {
            const message = document.getElementById("message").value;
            const chatMessage = {
                chatRoomId: "<%= chatRoomId %>",
                content: message,
                senderId: 2
            };

            stompClient.send(`/app/chat/message/${chatRoomId}`, {}, JSON.stringify(chatMessage));
        }

        // 메시지 표시
        function showMessage(message) {
            const chatBox = document.getElementById("chatBox");
            const messageElement = document.createElement("div");
            messageElement.textContent = message;
            chatBox.appendChild(messageElement);
        }

        // 페이지 로드 시 WebSocket 연결
        window.onload = function () {
            connect();
        };
    </script>
</head>
<body>
<h1>채팅방 <% out.print(chatRoomId); %></h1>

<div id="chatBox" style="border: 1px solid #ccc; width: 300px; height: 400px; overflow-y: scroll; margin-bottom: 10px;">
    <!-- 채팅 메시지가 여기에 표시됩니다 -->
</div>

<input type="text" id="message" placeholder="메시지를 입력하세요" style="width: 200px;">
<button onclick="sendMessage()">전송</button>

<a href="index.jsp">메인 페이지로 돌아가기</a>
</body>
</html>
