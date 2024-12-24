<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>채팅방 생성</title>
    <script>
        function createChatRoom() {
            const formData = {
                category: document.getElementById("category").value,
                name: document.getElementById("name").value,
                introduction: document.getElementById("introduction").value,
                limit: parseInt(document.getElementById("limit").value, 10),
            };

            fetch('/chat/create', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                credentials: 'include', // 쿠키 포함
                body: JSON.stringify(formData),
            })
                .then(response => {
                    // 응답이 JSON인지 확인
                    if (!response.ok) {
                        throw new Error("HTTP error, status = " + response.status);
                    }
                    return response.json(); // JSON으로 파싱
                })
                .then(data => {
                    console.log("전체 응답 데이터:", data);

                    // 응답 데이터에서 chatRoomId 추출
                    const chatRoomId = data?.dataBody?.chatRoomId; // 응답 구조에 따라 수정 필요
                    console.log("채팅방 ID:", chatRoomId);

                    if (chatRoomId) {
                        alert("채팅방이 생성되었습니다.");
                        // 생성된 채팅방으로 이동
                        window.location.href = "/chat/room/" + chatRoomId;
                    } else {
                        throw new Error("채팅방 ID를 가져올 수 없습니다.");
                    }
                })
                .catch(error => {
                    console.error("에러 발생:", error);
                    alert("채팅방 생성에 실패했습니다. 에러: " + error.message);
                });
        }
    </script>

</head>
<body>
<h1>채팅방 생성</h1>
<form onsubmit="event.preventDefault(); createChatRoom();">
    <label for="category">카테고리:</label>
    <input type="text" id="category" name="category" required><br><br>

    <label for="name">채팅방 이름:</label>
    <input type="text" id="name" name="name" required><br><br>

    <label for="introduction">소개:</label>
    <input type="text" id="introduction" name="introduction" required><br><br>

    <label for="limit">제한 인원:</label>
    <input type="number" id="limit" name="limit" required><br><br>

    <button type="submit">생성</button>
</form>
</body>
</html>
