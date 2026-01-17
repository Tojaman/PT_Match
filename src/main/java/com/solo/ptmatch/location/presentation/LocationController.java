package com.solo.ptmatch.location.presentation;

import com.solo.ptmatch.common.response.ApiResponse;
import com.solo.ptmatch.location.application.LocationSearchService;
import com.solo.ptmatch.location.presentation.response.LocationSearchResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Tag(name = "Location", description = "위치 검색 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/location")
public class LocationController {

    private final LocationSearchService locationSearchService;

    @Operation(summary = "검색어 자동 완성", description = "지하철역, 동 주소, 헬스장 이름을 검색합니다.")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<LocationSearchResponse>>> search(
            @Parameter(description = "검색어", example = "강남") @RequestParam String keyword) {
        List<LocationSearchResponse> results = locationSearchService.search(keyword);
        return ResponseEntity.ok(ApiResponse.success(results));
    }
}
