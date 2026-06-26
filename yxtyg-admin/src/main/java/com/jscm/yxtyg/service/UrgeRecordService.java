package com.jscm.yxtyg.service;

import com.jscm.yxtyg.common.PageResult;
import com.jscm.yxtyg.vo.UrgeRecordVO;

public interface UrgeRecordService {
    void urge(Long requirementId, Long operatorId);
    PageResult<UrgeRecordVO> queryPage(Long current, Long size);
}
