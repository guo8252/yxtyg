package com.jscm.yxtyg.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import lombok.Data;

@Data
@HeadRowHeight(20)
@ContentRowHeight(18)
@HeadStyle(fillForegroundColor = 44)
public class RequirementExcelDTO {

    @ExcelProperty("需求名称")
    @ColumnWidth(30)
    private String name;

    @ExcelProperty("需求描述")
    @ColumnWidth(50)
    private String description;

    @ExcelProperty("产品经理")
    @ColumnWidth(15)
    private String productManagerName;

    @ExcelProperty("归属系统")
    @ColumnWidth(20)
    private String systemName;

    @ExcelProperty("初核工作量（人天）")
    @ColumnWidth(18)
    private String initialWorkload;

    @ExcelProperty("初核金额（元）")
    @ColumnWidth(18)
    private String initialAmount;

    @ExcelProperty("最终核定工作量（人天）")
    @ColumnWidth(20)
    private String finalWorkload;

    @ExcelProperty("需求状态")
    @ColumnWidth(15)
    private String status;

    private Integer row;
}
