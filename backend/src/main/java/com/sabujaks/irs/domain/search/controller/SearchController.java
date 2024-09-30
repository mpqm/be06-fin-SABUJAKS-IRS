package com.sabujaks.irs.domain.search.controller;

import com.sabujaks.irs.domain.announcement.model.response.AnnouncementReadAllRes;
import com.sabujaks.irs.domain.announcement.model.response.CustomFormCreateRes;
import com.sabujaks.irs.domain.announcement.service.AnnouncementService;
import com.sabujaks.irs.domain.search.model.request.SearchFilterReq;
import com.sabujaks.irs.domain.search.service.SearchService;
import com.sabujaks.irs.global.common.exception.BaseException;
import com.sabujaks.irs.global.common.responses.BaseResponse;
import com.sabujaks.irs.global.common.responses.BaseResponseMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {
//    private final AnnouncementService announcementService;
    private final SearchService searchService;

    // 키워드로 공고 검색
    @GetMapping("/keyword")
    public ResponseEntity<BaseResponse<AnnouncementReadAllRes>> searchByKeyword(String keyword) throws BaseException {

        List<AnnouncementReadAllRes> response = searchService.searchByKeyword(keyword);
        return ResponseEntity.ok(new BaseResponse(BaseResponseMessage.ANNOUNCEMENT_SEARCH_SUCCESS, response));
    }

    @PostMapping("/filter")
    public ResponseEntity<BaseResponse<AnnouncementReadAllRes>> searchByFilter(
            @RequestBody SearchFilterReq dto) throws BaseException {

        // 필터 데이터를 서비스로 전달하여 공고 검색
        List<AnnouncementReadAllRes> response = searchService.searchByFilter(dto.getFilters());

        return ResponseEntity.ok(new BaseResponse(BaseResponseMessage.ANNOUNCEMENT_SEARCH_SUCCESS, response));
    }
}
