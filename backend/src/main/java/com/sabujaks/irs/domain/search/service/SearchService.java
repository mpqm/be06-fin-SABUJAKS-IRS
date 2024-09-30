package com.sabujaks.irs.domain.search.service;

import com.sabujaks.irs.domain.announcement.model.entity.Announcement;
import com.sabujaks.irs.domain.announcement.model.response.AnnouncementReadAllRes;
import com.sabujaks.irs.domain.announcement.repository.AnnouncementRepository;
import com.sabujaks.irs.domain.company.repository.CompanyRepository;
import com.sabujaks.irs.domain.search.model.request.FilterDto;
import com.sabujaks.irs.domain.search.model.request.SearchFilterReq;
import com.sabujaks.irs.global.common.exception.BaseException;
import com.sabujaks.irs.global.common.responses.BaseResponseMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final AnnouncementRepository announcementRepository;
    private final CompanyRepository companyRepository;

    // 키워드로 공고 검색
    public List<AnnouncementReadAllRes> searchByKeyword(String keyword) throws BaseException {
        // 공고명 또는 모집분야에 키워드가 포함되어 있는지 여부
        Optional<List<Announcement>> resultAnnounceKeywordList =
                announcementRepository.findAllByTitleContainingOrjobTitleContaining(keyword, keyword);

        if (resultAnnounceKeywordList.isPresent()) {
            // 찾은 공고들이 있으면 리스트로 넣어주기
            List<AnnouncementReadAllRes> resultReadAllResList = new ArrayList<>();
            for (Announcement am : resultAnnounceKeywordList.get()) {
                resultReadAllResList.add(
                        AnnouncementReadAllRes.builder()
                                .announcementIdx(am.getIdx())
                                .companyName(companyRepository.findByRecruiterIdx(am.getRecruiter().getIdx())
                                        .orElseThrow(()-> new BaseException(BaseResponseMessage.COMPANY_INFO_FAIL_NOT_REGISTER))
                                        .getName())
                                .companyInfo(companyRepository.findByRecruiterIdx(am.getRecruiter().getIdx())
                                        .orElseThrow(()-> new BaseException(BaseResponseMessage.COMPANY_INFO_FAIL_NOT_REGISTER))
                                        .getCompanyInfo())
                                .announcementTitle(am.getTitle())
                                .jobTitle(am.getJobTitle())
                                .careerBase(am.getCareerBase())
                                .region(am.getRegion())
                                .announcementEnd(am.getAnnouncementEnd())
                                .build()
                );
            }
            return resultReadAllResList;
        } else {
            return new ArrayList<>();
        }
    }

    // 필터로 공고 검색
    public List<AnnouncementReadAllRes> searchByFilter(List<FilterDto> filters) throws BaseException {
        // 필터 이름에 따라 검색
        // 검색하여나온 공고 다 리스폰스 리스트에 추가

        // 필터 조건을 동적으로 조합하여 쿼리 호출
//        String companyType = null;
        String careerBase = null;
        String jobCategory = null;
        String region = null;

        for (FilterDto filter : filters) {
            switch (filter.getName()) {
//                case "기업형태":
//                    companyType = filter.getValue();
//                    break;
                case "채용형태":
                    careerBase = filter.getValue();
                    break;
                case "모집직무":
                    jobCategory = filter.getValue();
                    break;
                case "근무지역":
                    region = filter.getValue();
                    break;
                // 다른 필터 처리
            }
        }

        // 합칠 리스트
        List<Announcement> resultSearchList = new ArrayList<>();

        // 조건에 맞는 쿼리 호출
        if (careerBase != null && jobCategory == null && region == null) { // 채용형태 필터만 유
            List<Announcement> resultList = announcementRepository.findAllByCareerBase(careerBase);
            resultSearchList.addAll(resultList);

        } else if (careerBase == null && jobCategory != null && region == null) { // 모집직무 필터만 유
            List<Announcement> resultList = announcementRepository.findAllByJobCategoryContaining(jobCategory);
            resultSearchList.addAll(resultList);

        } else if (careerBase == null && jobCategory == null && region != null) { // 지역 필터만 유
            List<Announcement> resultList = announcementRepository.findAllByRegionContaining(region);
            resultSearchList.addAll(resultList);

        } else if (careerBase != null && jobCategory != null && region == null) { // 채용형태와 모집직무 필터만 유
            List<Announcement> resultList = announcementRepository.findAllByCareerBaseAndJobCategoryContaining(careerBase, jobCategory);
            resultSearchList.addAll(resultList);

        } else if (careerBase != null && jobCategory == null && region != null) { // 채용형태와 지역 필터만 유
            List<Announcement> resultList = announcementRepository.findAllByCareerBaseAndRegionContaining(careerBase, region);
            resultSearchList.addAll(resultList);

        } else if (careerBase == null && jobCategory != null && region != null) { // 모집직무와 지역 필터만 유
            List<Announcement> resultList = announcementRepository.findAllByJobCategoryContainingAndRegionContaining(jobCategory, region);
            resultSearchList.addAll(resultList);

        } else if (careerBase != null && jobCategory != null && region != null) { // 세 필터 다 유
            List<Announcement> resultList = announcementRepository.findAllByCareerBaseAndJobCategoryContainingAndRegionContaining(careerBase, jobCategory, region);
            resultSearchList.addAll(resultList);
        }

        List<AnnouncementReadAllRes> resultReadAllResList = new ArrayList<>();
        for (Announcement am : resultSearchList) {
            resultReadAllResList.add(
                    AnnouncementReadAllRes.builder()
                            .announcementIdx(am.getIdx())
                            .companyName(companyRepository.findByRecruiterIdx(am.getRecruiter().getIdx())
                                    .orElseThrow(() -> new BaseException(BaseResponseMessage.COMPANY_INFO_FAIL_NOT_REGISTER))
                                    .getName())
                            .companyInfo(companyRepository.findByRecruiterIdx(am.getRecruiter().getIdx())
                                    .orElseThrow(() -> new BaseException(BaseResponseMessage.COMPANY_INFO_FAIL_NOT_REGISTER))
                                    .getCompanyInfo())
                            .announcementTitle(am.getTitle())
                            .jobTitle(am.getJobTitle())
                            .careerBase(am.getCareerBase())
                            .region(am.getRegion())
                            .announcementEnd(am.getAnnouncementEnd())
                            .build()
            );
        }
        return resultReadAllResList;

    }
}
