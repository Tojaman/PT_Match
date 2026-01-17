package com.solo.ptmatch.trainer.presentation;

import com.solo.ptmatch.common.response.ApiResponse;
import com.solo.ptmatch.trainer.application.S2CellIdMigrationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * S2 Cell ID 마이그레이션 관리자 API
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/migration")
public class S2MigrationController {

    private final S2CellIdMigrationService migrationService;

    @Operation(summary = "S2 Cell ID 일괄 마이그레이션", description = "s2_cell_id가 null인 프로필만 업데이트")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/s2-cell-id")
    public ResponseEntity<ApiResponse<MigrationResult>> migrateS2CellIds() {
        int updatedCount = migrationService.migrateAllProfiles();
        return ResponseEntity.ok(ApiResponse.success(new MigrationResult(updatedCount, "마이그레이션 완료")));
    }

    @Operation(summary = "S2 Cell ID 전체 재계산", description = "모든 프로필의 s2_cell_id를 강제로 재계산")
    // @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/s2-cell-id/recalculate")
    public ResponseEntity<ApiResponse<MigrationResult>> recalculateS2CellIds() {
        int updatedCount = migrationService.recalculateAllProfiles();
        return ResponseEntity.ok(ApiResponse.success(new MigrationResult(updatedCount, "재계산 완료")));
    }

    public record MigrationResult(int updatedCount, String message) {
    }
}
