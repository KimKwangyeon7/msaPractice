<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Login</title>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script src="https://www.gstatic.com/firebasejs/9.21.0/firebase-app-compat.js"></script>
    <script src="https://www.gstatic.com/firebasejs/9.21.0/firebase-messaging-compat.js"></script>

    <script>
        $(document).ready(function () {
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

            // 브라우저 알림 권한 요청
            Notification.requestPermission().then(permission => {
                if (permission === "granted") {
                    console.log("알림 권한이 부여되었습니다.");
                } else {
                    console.warn("알림 권한이 거부되었습니다.");
                }
            });

            // 로그인 폼 제출 이벤트
            $('#loginForm').on('submit', function (event) {
                event.preventDefault(); // 기본 폼 제출 방지

                const username = $('#username').val();
                const password = $('#password').val();

                // 로그인 요청
                $.ajax({
                    url: "http://localhost:8443/auth/login",
                    method: "POST",
                    contentType: "application/x-www-form-urlencoded",
                    data: { username, password },
                    xhrFields: { withCredentials: true }, // 쿠키 허용
                    success: function (response, textStatus, xhr) {
                        alert("Login successful!");
                        console.log(response);
                        // 응답 헤더에서 CSRF 토큰 가져오기
                        const csrfToken = response
                        if (!csrfToken) {
                            alert("CSRF 토큰을 가져올 수 없습니다.");
                            return;
                        }
                        console.log("CSRF Token:", csrfToken);

                        // FCM 디바이스 토큰 가져오기
                        getDeviceToken(csrfToken);
                    },
                    error: function (xhr) {
                        let errorMessage = 'Unknown error'; // 기본 에러 메시지
                        try {
                            const response = JSON.parse(xhr.responseText);
                            errorMessage = response.error || errorMessage;
                        } catch (e) {
                            console.error("Failed to parse error response", e);
                        }
                        alert(`Login failed: ${errorMessage}`);
                    }
                });
            });

            // 회원가입 버튼 클릭 이벤트
            $('#signupButton').on('click', function () {
                // 회원가입 페이지로 GET 요청
                window.location.href = "http://localhost:8443/auth/signup";
            });
            // 카카오 로그인 버튼 클릭 이벤트
            $('#kakaoLoginButton').on('click', function () {
                // 카카오 로그인 URL 요청
                $.ajax({
                    url: "http://localhost:8443/auth/oauth/KAKAO",
                    method: "GET",
                    success: function (response) {
                        // 응답으로 받은 URL로 이동
                        if (response && response.redirectUrl) {
                            window.location.href = response.redirectUrl;
                        } else {
                            alert("Failed to get Kakao login URL.");
                        }
                    },
                    error: function (xhr) {
                        alert(`Kakao login failed: ${xhr.status} ${xhr.statusText}`);
                    }
                });
            });

            // 네이버 로그인 버튼 클릭 이벤트
            $('#naverLoginButton').on('click', function () {
                // 카카오 로그인 URL 요청
                $.ajax({
                    url: "http://localhost:8443/auth/oauth/NAVER",
                    method: "GET",
                    success: function (response) {
                        // 응답으로 받은 URL로 이동
                        if (response && response.redirectUrl) {
                            window.location.href = response.redirectUrl;
                        } else {
                            alert("Failed to get Naver login URL.");
                        }
                    },
                    error: function (xhr) {
                        alert(`Naver login failed: ${xhr.status} ${xhr.statusText}`);
                    }
                });
            });

            // 구글 로그인 버튼 클릭 이벤트
            $('#googleLoginButton').on('click', function () {
                // 카카오 로그인 URL 요청
                $.ajax({
                    url: "http://localhost:8443/auth/oauth/GOOGLE",
                    method: "GET",
                    success: function (response) {
                        // 응답으로 받은 URL로 이동
                        if (response && response.redirectUrl) {
                            window.location.href = response.redirectUrl;
                        } else {
                            alert("Failed to get Google login URL.");
                        }
                    },
                    error: function (xhr) {
                        alert(`Google login failed: ${xhr.status} ${xhr.statusText}`);
                    }
                });
            });

            // FCM 디바이스 토큰 가져오기
            function getDeviceToken(csrfToken) {
                messaging.getToken({
                    vapidKey: "BIK5TIECOHEq2ylsYsdRQuNT3bJ3JLAYjV7AowqK-5jwOrrsMpnTG8i_yW8ErK3baMKpH-WpfOQAq4cV4bfMl3w",
                }).then(deviceToken => {
                    if (deviceToken) {
                        console.log("FCM Device Token:", deviceToken);
                        // 서버에 디바이스 토큰 등록
                        registerDeviceToken(csrfToken, deviceToken);
                    } else {
                        console.warn("FCM Device Token을 가져올 수 없습니다.");
                    }
                }).catch(error => {
                    console.error("FCM 초기화 중 에러:", error);
                });
            }

            // 디바이스 토큰 등록
            function registerDeviceToken(csrfToken, deviceToken) {
                $.ajax({
                    url: "http://localhost:8443/chat/firebase/message/" + deviceToken,
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                        "X-CSRF-TOKEN": csrfToken,
                    },
                    success: function () {
                        // 메인 페이지로 이동
                        window.location.href = "http://localhost:8443/auth/main";
                        console.log("디바이스 토큰 등록 성공");
                    },
                    error: function (xhr) {
                        console.error("디바이스 토큰 등록 실패:", xhr);
                    }
                });
            }

        });
    </script>
</head>
<body>
<h2>Login Page</h2>
<form id="loginForm">
    <label for="username">Username:</label>
    <input type="text" id="username" name="username" required><br><br>
    <label for="password">Password:</label>
    <input type="password" id="password" name="password" required><br><br>
    <button type="submit">Login</button>
</form>

<!-- 회원가입 버튼 추가 -->
<br>
<button id="signupButton">Sign Up</button>
<br><br>
<button id="kakaoLoginButton" style="
    background-color: #FEE500;
    color: #3A1D1D;
    border: 1px solid #E4BB00;
    padding: 10px 20px;
    font-size: 16px;
    font-weight: bold;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 5px;
    cursor: pointer;
    margin: 10px 0;
">
    카카오 로그인
</button>
<br>

<button id="naverLoginButton" style="
    background-color: #03C75A;
    color: white;
    border: none;
    padding: 10px 20px;
    font-size: 16px;
    font-weight: bold;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 5px;
    cursor: pointer;
    margin: 10px 0;
">
    네이버 로그인
</button>
<br>
<button id="googleLoginButton" style="
    background-color: white;
    color: #4285F4;
    border: 1px solid #DADCE0;
    padding: 10px 20px;
    font-size: 16px;
    font-weight: bold;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 5px;
    cursor: pointer;
    margin: 10px 0;
">
    구글 로그인
</button>
</body>
</html>
