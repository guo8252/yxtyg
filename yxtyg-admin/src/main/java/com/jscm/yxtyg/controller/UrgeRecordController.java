package com.jscm.yxtyg.controller;

import com.jscm.yxtyg.common.PageResult;
import com.jscm.yxtyg.common.Result;
import com.jscm.yxtyg.security.CurrentUser;
import com.jscm.yxtyg.service.UrgeRecordService;
import com.jscm.yxtyg.vo.UrgeRecordVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/urge")
public class UrgeRecordController {

    @Autowired
    private UrgeRecordService urgeRecordService;

    @PostMapping("/{requirementId}")
    public Result<Void> urge(@PathVariable Long requirementId,
                             @AuthenticationPrincipal CurrentUser currentUser) {
        urgeRecordService.urge(requirementId, currentUser.getUserId());
        return Result.success();
    }

    @GetMapping("/list")
    public Result<PageResult<UrgeRecordVO>> list(@RequestParam(defaultValue = "1") Long current,
                                                 @RequestParam(defaultValue = "10") Long size) {
        return Result.success(urgeRecordService.queryPage(current, size));
    }
}
