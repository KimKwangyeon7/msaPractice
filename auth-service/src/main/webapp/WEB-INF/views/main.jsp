<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String csrfToken = request.getAttribute("csrfToken").toString();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Main Page</title>
    <script src="http://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script src="https://www.gstatic.com/firebasejs/9.21.0/firebase-app-compat.js"></script>
    <script src="https://www.gstatic.com/firebasejs/9.21.0/firebase-messaging-compat.js"></script>
    <script>
        $(document).ready(function () {
            // CSRF 토큰 및 이메일 값 가져오기
            const csrfToken = "<%= csrfToken %>";
            console.log("csrfToken: " + csrfToken);
            // Firebase 초기화 설정
            const firebaseConfig = {
                apiKey: "AIzaSyBD3MGw9uE9Xpw3wvYKA4Ih_wqlmolAWYo",
                authDomain: "msapractice-cecd2.firebaseapp.com",
                projectId: "msapractice-cecd2",
                storageBucket: "msapractice-cecd2.firebasestorage.app",
                messagingSenderId: "335419323377",
                appId: "1:335419323377:web:448f785add14f0dcf88a50",
                measurementId: "G-0MXDBWN85V",
            };

            // Firebase 앱 초기화
            const app = firebase.initializeApp(firebaseConfig);
            const messaging = firebase.messaging();

            // 푸시 알림 수신 처리
            messaging.onMessage((payload) => {
                console.log("푸시 알림 수신:", payload);
                const { title, body } = payload.notification;

                // 알림 표시
                if (Notification.permission === "granted") {
                    new Notification(title, {
                        body: body,
                    });
                } else {
                    alert(`[알림] ${title}: ${body}`);
                }
            });

            // 로그아웃 버튼 클릭 시 요청
            $('#logout').click(function () {
                // 서버에 디바이스 토큰 삭제 요청
                deleteDeviceToken(csrfToken);
                // 로그아웃 요청 실행
                logout(csrfToken);
            });

            // 디바이스 토큰 삭제 함수
            function deleteDeviceToken(csrfToken) {
                $.ajax({
                    url: "http://localhost:8443/alarm/message",
                    method: "DELETE",
                    headers: {
                        "Content-Type": "application/json",
                        "X-CSRF-TOKEN": csrfToken
                    },
                    success: function () {
                        console.log("디바이스 토큰 삭제 성공");
                    },
                    error: function (xhr) {
                        console.error("디바이스 토큰 삭제 실패:", xhr);
                    }
                });
            }
            // 로그아웃 요청 함수
            function logout(csrfToken) {
                $.ajax({
                    url: `/member/logout`,
                    method: 'POST',
                    headers: {
                        'X-CSRF-TOKEN': csrfToken
                    },
                    xhrFields: {
                        withCredentials: true // 쿠키 허용
                    },
                    success: function (response) {
                        try {
                            // JSON 문자열을 파싱
                            const parsedResponse = JSON.parse(response.dataBody);
                            const message = parsedResponse.dataBody; // 내부 dataBody 값 추출
                            alert(message); // Logout successful 출력
                        } catch (e) {
                            console.error("Failed to process response", e);
                            alert("Logout successful"); // 기본 메시지 출력
                        } finally {
                            window.location.replace("/auth/login");
                        }
                    },
                    error: function (xhr) {
                        alert(`Logout failed: ${xhr.status} ${xhr.statusText}`);
                    }
                });
            }

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
                    xhrFields: {
                        withCredentials: true // 쿠키 허용
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

            // 채팅 서비스로 이동
            $('#chat-service').click(function () {
                // 페이지 이동
                window.location.href = "/chat/index";
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
<button id="chat-service">채팅 서비스</button>
</body>
</html>
