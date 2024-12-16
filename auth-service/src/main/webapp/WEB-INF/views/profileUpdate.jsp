<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>프로필 이미지/닉네임 변경</title>
    <meta name="csrf-token" content="${csrfToken}">
    <script src="http://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script>
        $(document).ready(function () {
            $('#profileUpdateForm').on('submit', function (event) {
                event.preventDefault();

                // CSRF 토큰 및 이메일 값 가져오기
                const csrfToken = document.querySelector('meta[name="csrf-token"]').getAttribute('content');

                const profileImage = $('#profileImage').val();
                const nickname = $('#nickname').val();

                $.ajax({
                    url: "http://localhost:8443/member/update",
                    method: "PATCH",
                    headers: {
                        'X-CSRF-TOKEN': csrfToken
                    },
                    contentType: "application/json",
                    data: JSON.stringify({
                        profileImage: profileImage,
                        nickname: nickname
                    }),
                    success: function (response) {
                        alert("프로필이 성공적으로 업데이트되었습니다.");
                        window.location.replace("/auth/main");
                    },
                    error: function (xhr) {
                        alert(`프로필 업데이트 실패: ${xhr.status} ${xhr.statusText}`);
                    }
                });
            });
        });
    </script>
</head>
<body>
<h1>프로필 이미지/닉네임 변경</h1>
<form id="profileUpdateForm">
    <label for="profileImage">프로필 이미지 URL</label>
    <input type="url" id="profileImage" name="profileImage" placeholder="프로필 이미지 URL을 입력하세요"><br><br>

    <label for="nickname">닉네임</label>
    <input type="text" id="nickname" name="nickname" placeholder="닉네임을 입력하세요" minlength="2" maxlength="18" required><br><br>

    <button type="submit">변경하기</button>
</form>
</body>
</html>
