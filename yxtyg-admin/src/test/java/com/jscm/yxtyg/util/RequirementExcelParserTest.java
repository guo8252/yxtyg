package com.jscm.yxtyg.util;

import com.alibaba.excel.EasyExcel;
import com.jscm.yxtyg.dto.RequirementExcelDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
public class RequirementExcelParserTest {

    private RequirementExcelParser parser = new RequirementExcelParser();

    private MockMultipartFile createExcel(List<RequirementExcelDTO> data) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        EasyExcel.write(out, RequirementExcelDTO.class).sheet("需求").doWrite(data);
        return new MockMultipartFile("file", "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", out.toByteArray());
    }

    @Test
    public void parse_fail_empty_system_name() throws Exception {
        RequirementExcelDTO dto = new RequirementExcelDTO();
        dto.setName("测试需求");
        dto.setSystemName("");
        dto.setProductManagerName("张三");
        dto.setInitialWorkload("5.0");
        dto.setInitialAmount("1000.00");
        dto.setStatus("PENDING");

        MockMultipartFile file = createExcel(Arrays.asList(dto));
        RequirementExcelParser.ParseResult result = parser.parse(file);

        assertEquals(0, result.getSuccessList().size());
        assertEquals(1, result.getFailDetails().size());
        assertEquals("归属系统不能为空", result.getFailDetails().get(0).getReason());
    }

    @Test
    public void parse_fail_empty_name() throws Exception {
        RequirementExcelDTO dto = new RequirementExcelDTO();
        dto.setName("");
        dto.setSystemName("测试系统");
        dto.setProductManagerName("张三");
        dto.setInitialWorkload("5.0");
        dto.setInitialAmount("1000.00");
        dto.setStatus("PENDING");

        MockMultipartFile file = createExcel(Arrays.asList(dto));
        RequirementExcelParser.ParseResult result = parser.parse(file);

        assertEquals(0, result.getSuccessList().size());
        assertEquals(1, result.getFailDetails().size());
        assertEquals("需求名称不能为空", result.getFailDetails().get(0).getReason());
    }

    @Test
    public void parse_success() throws Exception {
        RequirementExcelDTO dto = new RequirementExcelDTO();
        dto.setName("测试需求");
        dto.setSystemName("测试系统");
        dto.setProductManagerName("张三");
        dto.setInitialWorkload("5.0");
        dto.setInitialAmount("1000.00");
        dto.setStatus("PENDING");

        MockMultipartFile file = createExcel(Arrays.asList(dto));
        RequirementExcelParser.ParseResult result = parser.parse(file);

        assertEquals(1, result.getSuccessList().size());
        assertEquals(0, result.getFailDetails().size());
        assertEquals(2, result.getSuccessList().get(0).getRow().intValue());
    }
}
