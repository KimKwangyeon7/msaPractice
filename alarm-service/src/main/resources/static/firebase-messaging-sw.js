// Firebase SDK 스크립트 가져오기
importScripts('https://www.gstatic.com/firebasejs/9.21.0/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/9.21.0/firebase-messaging-compat.js');

// Firebase 초기화 설정
const firebaseConfig = {
    apiKey: "AIzaSyBD3MGw9uE9Xpw3wvYKA4Ih_wqlmolAWYo",
    authDomain: "msapractice-cecd2.firebaseapp.com",
    projectId: "msapractice-cecd2",
    storageBucket: "msapractice-cecd2.firebasestorage.app",
    messagingSenderId: "335419323377",
    appId: "1:335419323377:web:448f785add14f0dcf88a50",
    measurementId: "G-0MXDBWN85V"
};

// Firebase 초기화
firebase.initializeApp(firebaseConfig);

const messaging = firebase.messaging();

// 백그라운드 메시지 수신 처리
messaging.onBackgroundMessage((payload) => {
    console.log('[firebase-messaging-sw.js] 백그라운드 메시지 수신: ', payload);

    // 알림 데이터 설정
    const notificationTitle = payload.notification?.title || '알림 제목';
    const notificationOptions = {
        body: payload.notification?.body || '알림 내용',
        icon: '/firebase-logo.png' // 알림 아이콘 (옵션)
    };

    // 알림 표시
    self.registration.showNotification(notificationTitle, notificationOptions);
});
