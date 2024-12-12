<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Main Page</title>
    <meta name="csrf-token" content="${csrfToken}">
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script>
        $(document).ready(function () {
            const csrfToken = document.querySelector('meta[name="csrf-token"]').getAttribute('content');

            // jQuery AJAX 설정에 CSRF 토큰 포함
            $.ajaxSetup({
                headers: {
                    'X-CSRF-TOKEN': csrfToken
                }
            });

            // 로그아웃 버튼 클릭 시 요청
            $('#logout').click(function () {
                $.ajax({
                    url: '/auth/logout',
                    method: 'POST',
                    success: function (response) {
                        alert(response.message);
                        // URL 변경 및 페이지 새로고침
                        window.location.replace("/auth/login"); // 원하는 URL로 이동
                    },
                    error: function () {
                        alert('Logout failed!');
                    }
                });
            });
        });

    </script>
</head>
<body>
<h1>Welcome to the Main Page</h1>
<p>This page allows secure actions with CSRF and JWT protection.</p>

<!-- Secure action button -->
<button id="logout">Logout</button>
</body>
</html>
