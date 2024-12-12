<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>회원가입</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 0;
            background-color: #f9f9f9;
        }

        .signup-container {
            max-width: 500px;
            margin: 50px auto;
            padding: 20px;
            background-color: #fff;
            border-radius: 8px;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
        }

        h1 {
            text-align: center;
            color: #333;
        }

        form {
            display: flex;
            flex-direction: column;
        }

        label {
            margin-bottom: 5px;
            font-weight: bold;
            color: #555;
        }

        input {
            margin-bottom: 15px;
            padding: 10px;
            border: 1px solid #ccc;
            border-radius: 4px;
        }

        .error {
            color: red;
            font-size: 0.9em;
        }

        button {
            padding: 10px;
            color: white;
            background-color: #007BFF;
            border: none;
            border-radius: 4px;
            cursor: pointer;
        }

        button:hover {
            background-color: #0056b3;
        }

    </style>
    <script>
        async function submitSignupForm(event) {
            event.preventDefault();

            const formData = {
                email: document.getElementById("email").value,
                password: document.getElementById("password").value,
                name: document.getElementById("name").value,
                nickname: document.getElementById("nickname").value,
                profileImage: document.getElementById("profileImage").value
            };

            try {
                const response = await fetch('/auth/signup', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(formData)
                });

                if (response.ok) {
                    alert("회원가입 성공!");
                    // 회원가입 성공 시 원하는 행동 추가
                } else {
                    const error = await response.json();
                    alert(`회원가입 실패: ${error.message}`);
                }
            } catch (error) {
                console.error("회원가입 요청 중 오류 발생:", error);
                alert("서버와 통신 중 문제가 발생했습니다.");
            }
        }
    </script>
</head>
<body>
<div class="signup-container">
    <h1>회원가입</h1>
    <form id="signupForm" onsubmit="submitSignupForm(event)">
        <label for="email">이메일</label>
        <input type="email" id="email" name="email" placeholder="이메일을 입력하세요" required>

        <label for="password">비밀번호</label>
        <input type="password" id="password" name="password" placeholder="비밀번호를 입력하세요"
               pattern="^(?=.*[A-Za-z])(?=.*\\d)(?=.*[$@$!%*#?&])[A-Za-z\\d$@$!%*#?&]{8,16}$"
               title="비밀번호는 8~16자리수여야 합니다. 영문 대소문자, 숫자, 특수문자를 1개 이상 포함해야 합니다." required>

        <label for="name">이름</label>
        <input type="text" id="name" name="name" placeholder="이름을 입력하세요" minlength="2" maxlength="12" required>

        <label for="nickname">닉네임</label>
        <input type="text" id="nickname" name="nickname" placeholder="닉네임을 입력하세요" minlength="2" maxlength="18" required>

        <label for="profileImage">프로필 이미지 URL</label>
        <input type="url" id="profileImage" name="profileImage" placeholder="프로필 이미지 URL을 입력하세요">

        <button type="submit">회원가입</button>
    </form>
</div>
</body>
</html>
