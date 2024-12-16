<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>회원가입</title>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f4f4f4;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            margin: 0;
        }
        .signup-container {
            background-color: #fff;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
            width: 100%;
            max-width: 400px;
        }
        .signup-container h1 {
            margin-bottom: 20px;
            text-align: center;
        }
        .signup-container label {
            display: block;
            margin-top: 10px;
            font-weight: bold;
        }
        .signup-container input {
            width: 100%;
            padding: 10px;
            margin-top: 5px;
            border: 1px solid #ccc;
            border-radius: 4px;
            font-size: 14px;
        }
        .signup-container button {
            width: 100%;
            padding: 10px;
            background-color: #007BFF;
            color: #fff;
            border: none;
            border-radius: 4px;
            font-size: 16px;
            cursor: pointer;
            margin-top: 20px;
        }
        .signup-container button:hover {
            background-color: #0056b3;
        }
    </style>
    <script>
        function submitSignupForm(event) {
            event.preventDefault(); // 기본 폼 제출 방지

            const email = $('#email').val();
            const password = $('#password').val();
            const name = $('#name').val();
            const nickname = $('#nickname').val();
            const profileImage = $('#profileImage').val();

            // 클라이언트 측 유효성 검사
            if (!email || !password || !name || !nickname) {
                alert('모든 필수 항목을 입력하세요.');
                return;
            }

            // AJAX 요청을 통해 서버로 데이터 전송
            $.ajax({
                url: 'http://localhost:8443/auth/signup',
                method: 'POST',
                contentType: 'application/json',
                data: JSON.stringify({
                    email: email,
                    password: password,
                    name: name,
                    nickname: nickname,
                    profileImage: profileImage
                }),
                success: function (response) {
                    alert('회원가입이 완료되었습니다!');
                    window.location.href = 'http://localhost:8443/auth/login';
                },
                error: function (xhr) {
                    const errorMessage = xhr.responseJSON?.error || '회원가입에 실패했습니다.';
                    alert(`Error: ${errorMessage}`);
                }
            });
        }
    </script>
</head>
<body>
<div class="signup-container">
    <h1>회원가입</h1>
    <form id="signupForm" onsubmit="submitSignupForm(event)">
        <label for="email">이메일</label>
        <input type="email" id="email" name="email" placeholder="이메일을 입력하세요" required aria-label="이메일">

        <label for="password">비밀번호</label>
        <input type="password" id="password" name="password"
               placeholder="비밀번호를 입력하세요"
               pattern="^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,16}$"
               title="비밀번호는 8~16자리수여야 하며, 영문 대소문자, 숫자, 특수문자를 포함해야 합니다."
               required aria-label="비밀번호">

        <label for="name">이름</label>
        <input type="text" id="name" name="name" placeholder="이름을 입력하세요" minlength="2" maxlength="12" required aria-label="이름">

        <label for="nickname">닉네임</label>
        <input type="text" id="nickname" name="nickname" placeholder="닉네임을 입력하세요" minlength="2" maxlength="18" required aria-label="닉네임">

        <label for="profileImage">프로필 이미지 URL</label>
        <input type="url" id="profileImage" name="profileImage" placeholder="프로필 이미지 URL을 입력하세요" aria-label="프로필 이미지">

        <button type="submit">회원가입</button>
    </form>
</div>
</body>
</html>
