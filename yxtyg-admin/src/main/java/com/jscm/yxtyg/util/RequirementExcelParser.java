package com.jscm.yxtyg.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.jscm.yxtyg.dto.RequirementExcelDTO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class RequirementExcelParser {

    @Data
    public static class ParseResult {
        private List<RequirementExcelDTO> successList = new ArrayList<>();
        private List<FailDetail> failDetails = new ArrayList<>();

        @Data
        public static class FailDetail {
            private Integer row;
            private String reason;

            public FailDetail(Integer row, String reason) {
                this.row = row;
                this.reason = reason;
            }
        }
    }

    public ParseResult parse(MultipartFile file) {
        ParseResult result = new ParseResult();
        try {
            EasyExcel.read(file.getInputStream(), RequirementExcelDTO.class, new AnalysisEventListener<RequirementExcelDTO>() {
                private int rowIndex = 1;

                @Override
                public void invoke(RequirementExcelDTO data, AnalysisContext context) {
                    rowIndex++;
                    if (data.getName() == null || data.getName().trim().isEmpty()) {
                        result.getFailDetails().add(new ParseResult.FailDetail(rowIndex, "需求名称不能为空"));
                        return;
                    }
                    result.getSuccessList().add(data);
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    log.info("需求Excel解析完成，共{}行", result.getSuccessList().size());
                }
            }).sheet().doRead();
        } catch (IOException e) {
            log.error("解析需求Excel失败", e);
            result.getFailDetails().add(new ParseResult.FailDetail(0, "文件解析失败：" + e.getMessage()));
        }
        return result;
    }
}
