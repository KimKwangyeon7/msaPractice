<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>비밀번호 변경</title>
    <meta name="csrf-token" content="${csrfToken}">
    <script src="http://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script>
        $(document).ready(function () {
            $('#passwordChangeForm').on('submit', function (event) {
                event.preventDefault();

                // CSRF 토큰 가져오기
                const csrfToken = document.querySelector('meta[name="csrf-token"]').getAttribute('content');

                const currentPassword = $('#currentPassword').val();
                const newPassword = $('#newPassword').val();

                $.ajax({
                    url: "http://localhost:8443/member/password/change",
                    method: "PATCH",
                    headers: {
                        'X-CSRF-TOKEN': csrfToken
                    },
                    contentType: "application/json",
                    data: JSON.stringify({
                        currentPassword: currentPassword,
                        newPassword: newPassword
                    }),
                    success: function (response) {
                        alert("비밀번호가 성공적으로 변경되었습니다.");
                        window.location.replace("/auth/main");
                    },
                    error: function (xhr) {
                        alert(`비밀번호 변경 실패: ${xhr.status} ${xhr.statusText}`);
                    }
                });
            });
        });
    </script>
</head>
<body>
<h1>비밀번호 변경</h1>
<form id="passwordChangeForm">
    <label for="currentPassword">현재 비밀번호</label>
    <input type="password" id="currentPassword" name="currentPassword" required><br><br>

    <label for="newPassword">새 비밀번호</label>
    <input type="password" id="newPassword" name="newPassword" required><br><br>

    <button type="submit">변경하기</button>
</form>
</body>
</html>
