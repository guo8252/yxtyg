package com.jscm.yxtyg.service.impl;

import com.jscm.yxtyg.dto.RequirementDTO;
import com.jscm.yxtyg.dto.RequirementExcelDTO;
import com.jscm.yxtyg.entity.Requirement;
import com.jscm.yxtyg.entity.User;
import com.jscm.yxtyg.exception.BusinessException;
import com.jscm.yxtyg.mapper.RequirementMapper;
import com.jscm.yxtyg.mapper.UserMapper;
import com.jscm.yxtyg.util.RequirementExcelParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
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

    @Spy
    @InjectMocks
    private RequirementServiceImpl requirementService;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(requirementService, "baseMapper", requirementMapper);
    }

    private User mockProductManager() {
        User pm = new User();
        pm.setId(1L);
        pm.setUsername("zhangsan");
        pm.setRealName("张三");
        pm.setRole("PRODUCT_MANAGER");
        return pm;
    }

    @Test
    public void importExcel_success() {
        RequirementExcelDTO dto = new RequirementExcelDTO();
        dto.setRow(2);
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
        when(userMapper.selectByUsername(anyString())).thenReturn(mockProductManager());
        when(requirementMapper.selectList(any())).thenReturn(Collections.emptyList());
        doReturn(true).when(requirementService).saveBatch(anyList(), eq(100));

        Map<String, Object> result = requirementService.importExcel(file);

        assertEquals(1, result.get("successCount"));
        assertEquals(0, result.get("failCount"));
    }

    @Test
    public void importExcel_fail_invalid_workload() {
        RequirementExcelDTO dto = new RequirementExcelDTO();
        dto.setRow(2);
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
        when(userMapper.selectByUsername(anyString())).thenReturn(mockProductManager());
        when(requirementMapper.selectList(any())).thenReturn(Collections.emptyList());

        Map<String, Object> result = requirementService.importExcel(file);

        assertEquals(0, result.get("successCount"));
        assertEquals(1, result.get("failCount"));
        @SuppressWarnings("unchecked")
        List<RequirementExcelParser.ParseResult.FailDetail> failDetails =
                (List<RequirementExcelParser.ParseResult.FailDetail>) result.get("failDetails");
        assertEquals("初核工作量格式错误", failDetails.get(0).getReason());
    }

    @Test
    public void importExcel_fail_duplicate_name_in_file() {
        RequirementExcelDTO dto1 = new RequirementExcelDTO();
        dto1.setRow(2);
        dto1.setName("重复需求");
        dto1.setProductManagerName("张三");
        dto1.setSystemName("系统A");
        dto1.setInitialWorkload("5.0");
        dto1.setInitialAmount("1000.00");
        dto1.setStatus("PENDING");

        RequirementExcelDTO dto2 = new RequirementExcelDTO();
        dto2.setRow(3);
        dto2.setName("重复需求");
        dto2.setProductManagerName("张三");
        dto2.setSystemName("系统B");
        dto2.setInitialWorkload("6.0");
        dto2.setInitialAmount("2000.00");
        dto2.setStatus("PENDING");

        RequirementExcelParser.ParseResult parseResult = new RequirementExcelParser.ParseResult();
        parseResult.getSuccessList().addAll(Arrays.asList(dto1, dto2));

        MockMultipartFile file = new MockMultipartFile("file", "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[0]);
        when(excelParser.parse(file)).thenReturn(parseResult);
        when(requirementMapper.selectList(any())).thenReturn(Collections.emptyList());

        Map<String, Object> result = requirementService.importExcel(file);

        assertEquals(0, result.get("successCount"));
        assertEquals(2, result.get("failCount"));
        @SuppressWarnings("unchecked")
        List<RequirementExcelParser.ParseResult.FailDetail> failDetails =
                (List<RequirementExcelParser.ParseResult.FailDetail>) result.get("failDetails");
        assertEquals("需求名称重复", failDetails.get(0).getReason());
        assertEquals("需求名称重复", failDetails.get(1).getReason());
    }

    @Test
    public void importExcel_fail_existing_name_in_db() {
        RequirementExcelDTO dto = new RequirementExcelDTO();
        dto.setRow(2);
        dto.setName("已存在需求");
        dto.setProductManagerName("张三");
        dto.setSystemName("系统A");
        dto.setInitialWorkload("5.0");
        dto.setInitialAmount("1000.00");
        dto.setStatus("PENDING");

        RequirementExcelParser.ParseResult parseResult = new RequirementExcelParser.ParseResult();
        parseResult.getSuccessList().add(dto);

        Requirement existing = new Requirement();
        existing.setName("已存在需求");

        MockMultipartFile file = new MockMultipartFile("file", "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[0]);
        when(excelParser.parse(file)).thenReturn(parseResult);
        when(requirementMapper.selectList(any())).thenReturn(Collections.singletonList(existing));

        Map<String, Object> result = requirementService.importExcel(file);

        assertEquals(0, result.get("successCount"));
        assertEquals(1, result.get("failCount"));
        @SuppressWarnings("unchecked")
        List<RequirementExcelParser.ParseResult.FailDetail> failDetails =
                (List<RequirementExcelParser.ParseResult.FailDetail>) result.get("failDetails");
        assertEquals("需求名称已存在", failDetails.get(0).getReason());
    }

    @Test
    public void importExcel_success_zero_initial_amount() {
        RequirementExcelDTO dto = new RequirementExcelDTO();
        dto.setRow(2);
        dto.setName("零金额需求");
        dto.setDescription("测试描述");
        dto.setProductManagerName("张三");
        dto.setSystemName("测试系统");
        dto.setInitialWorkload("5.0");
        dto.setInitialAmount("0");
        dto.setFinalWorkload("");
        dto.setStatus("PENDING");

        RequirementExcelParser.ParseResult parseResult = new RequirementExcelParser.ParseResult();
        parseResult.getSuccessList().add(dto);

        MockMultipartFile file = new MockMultipartFile("file", "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[0]);
        when(excelParser.parse(file)).thenReturn(parseResult);
        when(userMapper.selectByUsername(anyString())).thenReturn(mockProductManager());
        when(requirementMapper.selectList(any())).thenReturn(Collections.emptyList());
        doReturn(true).when(requirementService).saveBatch(anyList(), eq(100));

        Map<String, Object> result = requirementService.importExcel(file);

        assertEquals(1, result.get("successCount"));
        assertEquals(0, result.get("failCount"));
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

    @Test
    public void fillFinalWorkload_fail_dev_admin_forbidden() {
        Requirement req = new Requirement();
        req.setId(1L);
        req.setProductManagerId(1L);
        req.setStatus("PENDING");
        when(requirementMapper.selectById(1L)).thenReturn(req);

        assertThrows(BusinessException.class, () ->
                requirementService.fillFinalWorkload(1L, 1L, "DEV_ADMIN", new BigDecimal("8.0")));
    }

    @Test
    public void fillFinalWorkload_fail_not_own_requirement() {
        Requirement req = new Requirement();
        req.setId(1L);
        req.setProductManagerId(2L);
        req.setStatus("PENDING");
        when(requirementMapper.selectById(1L)).thenReturn(req);

        assertThrows(BusinessException.class, () ->
                requirementService.fillFinalWorkload(1L, 1L, "PRODUCT_MANAGER", new BigDecimal("8.0")));
    }

    @Test
    public void create_fail_invalid_product_manager() {
        RequirementDTO dto = new RequirementDTO();
        dto.setName("新需求");
        dto.setProductManagerId(99L);
        when(userMapper.selectById(99L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> requirementService.create(dto));
    }

    @Test
    public void approve_success() {
        Requirement req = new Requirement();
        req.setId(1L);
        req.setStatus("FILLED");
        when(requirementMapper.selectById(1L)).thenReturn(req);

        requirementService.approve(1L);

        ArgumentCaptor<Requirement> captor = ArgumentCaptor.forClass(Requirement.class);
        verify(requirementMapper).updateById(captor.capture());
        assertEquals("APPROVED", captor.getValue().getStatus());
    }

    @Test
    public void approve_fail_not_filled() {
        Requirement req = new Requirement();
        req.setId(1L);
        req.setStatus("PENDING");
        when(requirementMapper.selectById(1L)).thenReturn(req);

        assertThrows(BusinessException.class, () -> requirementService.approve(1L));
    }
}
