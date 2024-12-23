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
                    if (response.ok) {
                        alert("채팅방이 생성되었습니다.");
                        window.location.href = '/chat/index'; // 채팅방 목록 페이지로 이동
                    } else {
                        return response.json().then(error => {
                            alert("채팅방 생성 실패: " + error.message);
                        });
                    }
                })
                .catch(error => {
                    console.error("Error:", error);
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
