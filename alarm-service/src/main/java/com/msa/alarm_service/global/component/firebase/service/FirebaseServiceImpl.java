package com.msa.alarm_service.global.component.firebase.service;

import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import com.google.firebase.messaging.*;

import com.msa.alarm_service.global.component.firebase.dto.request.FcmSubscribeRequest;
import com.msa.alarm_service.global.component.firebase.dto.request.FcmTokenRequest;
import com.msa.alarm_service.global.component.firebase.dto.request.FcmTopicRequest;
import com.msa.alarm_service.global.component.firebase.entity.DeviceToken;
import com.msa.alarm_service.global.component.firebase.exception.FcmErrorCode;
import com.msa.alarm_service.global.component.firebase.exception.FcmException;
import com.msa.alarm_service.global.component.firebase.repository.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

/**
 * {@link FirebaseService} 인터페이스의 구현체로, Firebase Storage를 사용하여 파일을 업로드합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseServiceImpl implements FirebaseService {
    //@Value("${app.firebase-bucket}")
    //private String firebaseBucket; // Firebase Storage 버킷 이름
    private final DeviceTokenRepository deviceTokenRepository;
    //private final MemberRepository memberRepository;

//    /**
//     * MultipartFile을 Firebase Storage에 업로드하고, 업로드된 파일의 URL을 반환합니다.
//     *
//     * @param file 업로드할 파일 객체입니다.
//     * @param nameFile 저장될 파일의 이름입니다. 이 이름을 통해 Firebase에서 파일을 식별합니다.
//     * @return 업로드된 파일의 공개 URL입니다. 파일에 공개적으로 접근할 수 있는 URL을 제공합니다.
//     * @throws RuntimeException 파일 업로드 과정에서 오류가 발생한 경우 예외를 발생시킵니다.
//     */
//    @Override
//    public String uploadFiles(MultipartFile file, String nameFile) {
//        Bucket bucket = StorageClient.getInstance().bucket(firebaseBucket); // Firebase Storage 버킷에 접근
//
//        Blob blob = null;
//        try {
//            // 파일을 Firebase Storage에 업로드합니다. 파일 이름과 바이트 배열, 콘텐츠 타입을 지정합니다.
//            blob = bucket.create(nameFile, file.getBytes(), file.getContentType());
//        } catch (Exception e) {
//            log.error("파일 업로드 중 오류 발생", e);
//            throw new RuntimeException("파일 읽기 오류입니다.", e);
//        }
//
//        // 업로드된 파일에 대해 공개 읽기 권한을 부여합니다.
//        blob.createAcl(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));
//
//        // 업로드된 파일의 공개 URL을 반환합니다.
//        return String.format("https://storage.googleapis.com/%s/%s", firebaseBucket, nameFile);
//    }

    @Override
    public void subscribeByTopic(FcmSubscribeRequest fcmSubscribeRequest) {
        try {
            FirebaseMessaging.getInstance().subscribeToTopic(Collections.singletonList(fcmSubscribeRequest.token()), fcmSubscribeRequest.topic());
        } catch (FirebaseMessagingException e) {
            throw new FcmException(FcmErrorCode.SUBSCRIBE_FAIL);
        }
    }

    @Override
    public void unsubscribeByTopic(FcmSubscribeRequest fcmSubscribeRequest) {
        try {
            // Firebase Admin SDK에서 구독 취소 수행
            FirebaseMessaging.getInstance().unsubscribeFromTopic(
                    Collections.singletonList(fcmSubscribeRequest.token()),
                    fcmSubscribeRequest.topic()
            );
        } catch (FirebaseMessagingException e) {
            // 예외 발생 시 사용자 정의 예외로 변환
            throw new FcmException(FcmErrorCode.UNSUBSCRIBE_FAIL);
        }
    }


    // 지정된 topic에 fcm를 보냄
    @Override
    public void sendMessageByTopic(FcmTopicRequest fcmTopicRequest) {
        try {
            FirebaseMessaging.getInstance().send(Message.builder()
                    .setNotification(Notification.builder()
                            .setTitle(fcmTopicRequest.title())
                            .setBody(fcmTopicRequest.body())
                            .build())
                    .setTopic(fcmTopicRequest.topicName())
                    .build());
        } catch (FirebaseMessagingException e) {
            throw new FcmException(FcmErrorCode.CAN_NOT_SEND_NOTIFICATION);
        }
    }

    // 받은 token을 이용하여 fcm를 보냄
    @Transactional(readOnly = true)
    @Override
    public void sendMessageByToken(FcmTokenRequest fcmTokenRequest) {
        //Member member = memberRepository.findById(fcmTokenRequest.memberId()).orElseThrow(() -> new FcmException(FcmErrorCode.NO_EXIST_USER));
//        Long member = 1L;
//        List<String> tokenList = deviceTokenRepository.findTokenAllByMember(member).orElseThrow(() -> new FcmException(FcmErrorCode.NO_EXIST_TOKEN));
//        try {
//            FirebaseMessaging.getInstance().sendMulticast(MulticastMessage.builder()
//                    .setNotification(Notification.builder()
//                            .setTitle(fcmTokenRequest.title())
//                            .setBody(fcmTokenRequest.body())
//                            .build())
//                    .addAllTokens(tokenList)
//                    .build());
//        } catch (FirebaseMessagingException e) {
//            throw new FcmException(FcmErrorCode.CAN_NOT_SEND_NOTIFICATION);
//        }
        List<String> tokenList = deviceTokenRepository.findTokenAllByMember(fcmTokenRequest.memberId())
                .orElseThrow(() -> new FcmException(FcmErrorCode.NO_EXIST_TOKEN));

        if (tokenList == null || tokenList.isEmpty()) {
            // 토큰이 없을 때 예외 처리
            //throw new FcmException(FcmErrorCode.NO_EXIST_TOKEN);
            log.warn("Member with ID {} has no FCM token. Skipping notification.", fcmTokenRequest.memberId());
            return;
        }

        try {
            // 유효한 첫 번째 토큰으로 메시지 전송
            Message message = Message.builder()
                    .setToken(tokenList.get(0)) // 첫 번째 토큰 사용
                    .setNotification(Notification.builder()
                            .setTitle(fcmTokenRequest.title())
                            .setBody(fcmTokenRequest.body())
                            .build())
                    .putData("senderId", fcmTokenRequest.memberId().toString())
                    .putData("chatRoomId",fcmTokenRequest.chatRoomId().toString() )
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("메시지 전송 성공: " + response);

        } catch (FirebaseMessagingException e) {
            // 메시지 전송 실패 시 예외 처리
            //throw new FcmException(FcmErrorCode.CAN_NOT_SEND_NOTIFICATION);
            log.error("Failed to send notification to member ID {}: {}", fcmTokenRequest.memberId(), e.getMessage());
        }
    }

    @Transactional
    @Override
    public void createDeviceToken(Long memberId, String deviceToken) {
        deviceTokenRepository.save(DeviceToken.builder().token(deviceToken).memberId(memberId).build());
    }

    @Transactional
    @Override
    public void deleteDeviceToken(Long memberId) {
        deviceTokenRepository.deleteByMemberId(memberId);
    }
}

