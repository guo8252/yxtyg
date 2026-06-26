package com.jscm.yxtyg.controller;

import com.jscm.yxtyg.common.PageResult;
import com.jscm.yxtyg.common.Result;
import com.jscm.yxtyg.dto.RequirementDTO;
import com.jscm.yxtyg.dto.RequirementQueryDTO;
import com.jscm.yxtyg.security.CurrentUser;
import com.jscm.yxtyg.service.RequirementService;
import com.jscm.yxtyg.vo.RequirementDetailVO;
import com.jscm.yxtyg.vo.RequirementVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/requirement")
public class RequirementController {

    @Autowired
    private RequirementService requirementService;

    @GetMapping("/list")
    public Result<PageResult<RequirementVO>> list(RequirementQueryDTO queryDTO,
                                                  @AuthenticationPrincipal CurrentUser currentUser) {
        return Result.success(requirementService.queryPage(queryDTO, currentUser.getUserId(), currentUser.getRole()));
    }

    @GetMapping("/detail/{id}")
    public Result<RequirementDetailVO> detail(@PathVariable Long id) {
        return Result.success(requirementService.getDetail(id));
    }

    @PostMapping
    public Result<Void> create(@RequestBody RequirementDTO dto) {
        requirementService.create(dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody RequirementDTO dto) {
        requirementService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        requirementService.delete(id);
        return Result.success();
    }

    @PostMapping("/import")
    public Result<Map<String, Object>> importExcel(@RequestParam("file") MultipartFile file) {
        return Result.success(requirementService.importExcel(file));
    }

    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        byte[] data = requirementService.downloadTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=requirement_template.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }

    @PostMapping("/{id}/fill")
    public Result<Void> fillFinalWorkload(@PathVariable Long id,
                                          @RequestBody FillRequest request,
                                          @AuthenticationPrincipal CurrentUser currentUser) {
        requirementService.fillFinalWorkload(id, currentUser.getUserId(), currentUser.getRole(), request.getFinalWorkload());
        return Result.success();
    }

    public static class FillRequest {
        private BigDecimal finalWorkload;
        public BigDecimal getFinalWorkload() { return finalWorkload; }
        public void setFinalWorkload(BigDecimal finalWorkload) { this.finalWorkload = finalWorkload; }
    }
}
