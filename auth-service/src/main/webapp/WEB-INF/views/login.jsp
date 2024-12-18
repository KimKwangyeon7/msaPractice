<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Login</title>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script>
        $(document).ready(function () {
            // 로그인 폼 제출 이벤트
            $('#loginForm').on('submit', function (event) {
                event.preventDefault(); // 기본 폼 제출 방지

                const username = $('#username').val();
                const password = $('#password').val();

                // AJAX 요청으로 게이트웨이(8443) 포트로 로그인 요청
                $.ajax({
                    url: "http://localhost:8443/auth/login",
                    method: "POST",
                    contentType: "application/x-www-form-urlencoded",
                    data: { username: username, password: password },
                    xhrFields: {
                        withCredentials: true // 쿠키 허용
                    },
                    success: function (response) {
                        alert("Login successful!");
                        // 메인 페이지로 이동
                        window.location.href = "http://localhost:8443/auth/main";
                    },
                    error: function (xhr) {
                        let errorMessage = 'Unknown error'; // 기본 에러 메시지
                        try {
                            // JSON 파싱 시도
                            const response = JSON.parse(xhr.responseText);
                            errorMessage = response.error || errorMessage;
                        } catch (e) {
                            // 파싱 실패 시 기본 에러 메시지 유지
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
