package com.jscm.yxtyg.service;

import com.jscm.yxtyg.common.PageResult;
import com.jscm.yxtyg.dto.RequirementDTO;
import com.jscm.yxtyg.dto.RequirementQueryDTO;
import com.jscm.yxtyg.vo.RequirementDetailVO;
import com.jscm.yxtyg.vo.RequirementVO;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Map;

public interface RequirementService {
    PageResult<RequirementVO> queryPage(RequirementQueryDTO queryDTO, Long currentUserId, String role);
    RequirementDetailVO getDetail(Long id, Long currentUserId, String role);
    void create(RequirementDTO dto);
    void update(Long id, RequirementDTO dto, Long currentUserId, String role);
    void delete(Long id);
    Map<String, Object> importExcel(MultipartFile file);
    byte[] downloadTemplate();
    void fillFinalWorkload(Long id, Long currentUserId, String role, BigDecimal finalWorkload);
    void approve(Long id);
}
