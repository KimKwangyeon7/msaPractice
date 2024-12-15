<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Main Page</title>
    <meta name="csrf-token" content="${csrfToken}">
    <script src="http://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script>
        $(document).ready(function () {
            // CSRF 토큰 및 이메일 값 가져오기
            const csrfToken = document.querySelector('meta[name="csrf-token"]').getAttribute('content');
            const email = '${email}';

            // 로그아웃 버튼 클릭 시 요청
            $('#logout').click(function () {
                if (!email) {
                    alert('이메일 정보가 없습니다!');
                    return;
                }
                $.ajax({
                    url: `/member/logout/${email}`,
                    method: 'POST',
                    headers: {
                        'X-CSRF-TOKEN': csrfToken
                    },
                    success: function (response) {
                        alert(response.message);
                        window.location.replace("/auth/login");
                    },
                    error: function (xhr) {
                        alert(`Logout failed: ${xhr.status} ${xhr.statusText}`);
                    }
                });
            });
        });
    </script>
</head>
<body>
<h1>Welcome to the Main Page</h1>
<p>This page allows secure actions with CSRF and JWT protection.</p>
<button id="logout">Logout</button>
</body>
</html>
