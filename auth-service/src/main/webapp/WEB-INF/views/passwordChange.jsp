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

                const nowPassword = $('#nowPassword').val();
                const changePassword = $('#changePassword').val();
                const changePasswordCheck = $('#changePasswordCheck').val();

                $.ajax({
                    url: "http://localhost:8443/member/password/change",
                    method: "PATCH",
                    headers: {
                        'X-CSRF-TOKEN': csrfToken
                    },
                    contentType: "application/json",
                    data: JSON.stringify({
                        nowPassword: nowPassword,
                        changePassword: changePassword,
                        changePasswordCheck: changePasswordCheck
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
    <label for="nowPassword">현재 비밀번호</label>
    <input type="password" id="nowPassword" name="nowPassword" required><br><br>

    <label for="changePassword">새 비밀번호</label>
    <input type="password" id="changePassword" name="changePassword" required><br><br>

    <label for="changePasswordCheck">새 비밀번호</label>
    <input type="password" id="changePasswordCheck" name="changePasswordCheck" required><br><br>

    <button type="submit">변경하기</button>
</form>
</body>
</html>
