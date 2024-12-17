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

            // 로그아웃 버튼 클릭 시 요청
            $('#logout').click(function () {
                // if (!email) {
                //     alert('이메일 정보가 없습니다!');
                //     return;
                // }
                $.ajax({
                    url: `/member/logout`,
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

            // 회원탈퇴 버튼 클릭 시 요청
            $('#deleteAccount').click(function () {
                // if (!email) {
                //     alert('이메일 정보가 없습니다!');
                //     return;
                // }
                if (!confirm('정말로 회원탈퇴를 진행하시겠습니까?')) {
                    return; // 사용자가 취소를 눌렀을 경우
                }
                $.ajax({
                    url: `http://localhost:8443/member/delete`,
                    method: 'DELETE',
                    headers: {
                        'X-CSRF-TOKEN': csrfToken
                    },
                    success: function (response) {
                        alert('회원탈퇴가 완료되었습니다.');
                        window.location.replace("/auth/login"); // 탈퇴 후 회원가입 페이지로 이동
                    },
                    error: function (xhr) {
                        alert(`Account deletion failed: ${xhr.status} ${xhr.statusText}`);
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
<button id="deleteAccount">Delete Account</button>
<br><br>
<button onclick="window.location.href='/auth/member/password/change'">비밀번호 변경</button>
<button onclick="window.location.href='/auth/member/update'">프로필 변경</button>
</body>
</html>
