package com.msa.alarm_service.global.component.firebase.repository;



import com.msa.alarm_service.global.component.firebase.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, String> {
    @Query("SELECT dt.token FROM DeviceToken dt WHERE dt.memberId = :member")
    Optional<List<String>> findTokenAllByMember(Long member);
    @Query("SELECT dt.token FROM DeviceToken dt")
    Optional<List<String>> findTokenAll();

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM DeviceToken d WHERE d.memberId = :memberId")
    void deleteByMemberId(Long memberId);
}
