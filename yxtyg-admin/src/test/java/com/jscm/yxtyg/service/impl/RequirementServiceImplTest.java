package com.jscm.yxtyg.service.impl;

import com.jscm.yxtyg.dto.RequirementExcelDTO;
import com.jscm.yxtyg.entity.Requirement;
import com.jscm.yxtyg.entity.User;
import com.jscm.yxtyg.mapper.RequirementMapper;
import com.jscm.yxtyg.mapper.UserMapper;
import com.jscm.yxtyg.util.RequirementExcelParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RequirementServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private RequirementExcelParser excelParser;

    @Mock
    private RequirementMapper requirementMapper;

    @InjectMocks
    private RequirementServiceImpl requirementService;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(requirementService, "baseMapper", requirementMapper);
    }

    @Test
    public void importExcel_success() {
        RequirementExcelDTO dto = new RequirementExcelDTO();
        dto.setName("测试需求");
        dto.setDescription("测试描述");
        dto.setProductManagerName("张三");
        dto.setSystemName("测试系统");
        dto.setInitialWorkload("5.0");
        dto.setInitialAmount("5000.00");
        dto.setFinalWorkload("");
        dto.setStatus("PENDING");

        RequirementExcelParser.ParseResult parseResult = new RequirementExcelParser.ParseResult();
        parseResult.getSuccessList().add(dto);

        MockMultipartFile file = new MockMultipartFile("file", "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[0]);
        when(excelParser.parse(file)).thenReturn(parseResult);

        User pm = new User();
        pm.setId(1L);
        pm.setUsername("zhangsan");
        pm.setRealName("张三");
        pm.setRole("PRODUCT_MANAGER");
        when(userMapper.selectByUsername(anyString())).thenReturn(pm);

        Map<String, Object> result = requirementService.importExcel(file);

        assertEquals(1, result.get("successCount"));
        assertEquals(0, result.get("failCount"));
    }

    @Test
    public void importExcel_fail_invalid_workload() {
        RequirementExcelDTO dto = new RequirementExcelDTO();
        dto.setName("测试需求");
        dto.setDescription("测试描述");
        dto.setProductManagerName("张三");
        dto.setSystemName("测试系统");
        dto.setInitialWorkload("invalid");
        dto.setInitialAmount("5000.00");
        dto.setFinalWorkload("");
        dto.setStatus("PENDING");

        RequirementExcelParser.ParseResult parseResult = new RequirementExcelParser.ParseResult();
        parseResult.getSuccessList().add(dto);

        MockMultipartFile file = new MockMultipartFile("file", "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[0]);
        when(excelParser.parse(file)).thenReturn(parseResult);

        User pm = new User();
        pm.setId(1L);
        pm.setUsername("zhangsan");
        pm.setRealName("张三");
        pm.setRole("PRODUCT_MANAGER");
        when(userMapper.selectByUsername(anyString())).thenReturn(pm);

        Map<String, Object> result = requirementService.importExcel(file);

        assertEquals(0, result.get("successCount"));
        assertEquals(1, result.get("failCount"));
        @SuppressWarnings("unchecked")
        List<RequirementExcelParser.ParseResult.FailDetail> failDetails =
                (List<RequirementExcelParser.ParseResult.FailDetail>) result.get("failDetails");
        assertEquals("初核工作量格式错误", failDetails.get(0).getReason());
    }

    @Test
    public void fillFinalWorkload_success() {
        Requirement req = new Requirement();
        req.setId(1L);
        req.setName("测试需求");
        req.setProductManagerId(1L);
        req.setInitialWorkload(new BigDecimal("10.0"));
        req.setStatus("PENDING");
        when(requirementMapper.selectById(1L)).thenReturn(req);

        requirementService.fillFinalWorkload(1L, 1L, "PRODUCT_MANAGER", new BigDecimal("8.0"));

        ArgumentCaptor<Requirement> captor = ArgumentCaptor.forClass(Requirement.class);
        verify(requirementMapper).updateById(captor.capture());
        Requirement updated = captor.getValue();
        assertEquals("FILLED", updated.getStatus());
        assertEquals(new BigDecimal("8.0"), updated.getFinalWorkload());
        assertEquals(new BigDecimal("2.0"), updated.getReducedWorkload());
    }
}
