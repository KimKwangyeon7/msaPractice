package com.msa.community_service.domain.community.service;



import com.msa.community_service.domain.community.dto.info.ImageInfo;
import com.msa.community_service.domain.community.dto.info.MemberLoginActive;
import com.msa.community_service.domain.community.dto.request.CommunityListRequest;
import com.msa.community_service.domain.community.dto.request.CreateCommunityRequest;
import com.msa.community_service.domain.community.dto.request.UpdateCommunityRequest;
import com.msa.community_service.domain.community.dto.response.CommunityDetailResponse;
import com.msa.community_service.domain.community.dto.response.CommunityListResponse;
import com.msa.community_service.domain.community.dto.response.PopularCommunityListResponse;
import com.msa.community_service.domain.community.entity.Community;
import com.msa.community_service.domain.community.entity.Image;
import com.msa.community_service.domain.community.exception.CommunityErrorCode;
import com.msa.community_service.domain.community.exception.CommunityException;
import com.msa.community_service.domain.community.repository.CommentRepository;
import com.msa.community_service.domain.community.repository.CommunityRepository;
import com.msa.community_service.domain.community.repository.ImageRepository;
import com.msa.community_service.global.common.dto.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CommunityServiceImpl implements CommunityService {

    private final CommunityRepository communityRepository;
    private final ImageRepository imageRepository;
    private final CommentRepository commentRepository;

    @Override
    public Long createCommunity(Long memberId, CreateCommunityRequest request) {
//        Member writer = memberRepository.findById(memberId)
//                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));
        Community community = request.toEntity();
        community = community.toBuilder().writerId(memberId).build();
        communityRepository.save(community);

        List<String> imageUrls = request.images();
        List<Image> images = new ArrayList<>();
        for (String imageUrl : imageUrls) {
            images.add(Image.builder()
                    .community(community)
                    .url(imageUrl)
                    .build());
        }

        imageRepository.saveAll(images);
        return community.getId();
    }

    @Override
    public List<CommunityListResponse> selectCommunityList(CommunityListRequest request) {
        return communityRepository.selectCommunityList(request.category(), request.lastId());
    }

    @Override
    public List<PopularCommunityListResponse> selectPopularCommunityList() {
        return communityRepository.selectPopularCommunityList();
    }

    @Override
    public CommunityDetailResponse selectCommunity(Long communityId) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.NOT_EXIST_COMMUNITY));

        community.read();

        return communityRepository.selectCommunity(communityId);
    }

    @Override
    public void updateCommunity(Long communityId, UpdateCommunityRequest request, MemberLoginActive memberLoginActive) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.NOT_EXIST_COMMUNITY));

        // 작성자 본인이 아닌 경우
        if (memberLoginActive.role().name().equals("USER") && community.getWriterId() != memberLoginActive.id()){
            System.out.println("권한이 없습니다.");
            return;
        }
        // 제목, 내용 수정
        community.update(request.title(), request.content());

        Map<Long, Image> map = imageRepository.findByCommunityId(communityId)
                .stream()
                .collect(Collectors.toMap(Image::getId, Function.identity()));

        List<Image> images = community.getImages();

        for (ImageInfo imageInfo : request.images()) {
            Long imageId = imageInfo.imageId();
            // 새로 추가된 항목은 새로 저장
            if (imageId == null) {
                community.addImage(Image.builder()
                        .community(community).url(imageInfo.url()).build());
                continue;
            }
            // 기존에 있던 항목 map에서 제거 >> 이후 해당 map에 있는 값들을 한번에 삭제할 예정
            if (map.containsKey(imageId)) {
                map.remove(imageId);
            }
        }
        images.removeAll(map.values());
    }

    @Transactional
    @Override
    public void deleteCommunity(Long communityId, MemberLoginActive memberLoginActive) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.NOT_EXIST_COMMUNITY));

        // 작성자 본인이 아닌 경우
        if (memberLoginActive.role().name().equals("USER") && community.getWriterId() != memberLoginActive.id()){
            System.out.println("권한이 없습니다.");
            return;
        }
        // Image 삭제
        imageRepository.deleteByCommunityId(communityId);
        // Comment 삭제
        commentRepository.deleteByCommunityId(communityId);
        // Community 삭제
        communityRepository.deleteById(communityId);
    }
}
