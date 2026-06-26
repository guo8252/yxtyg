package com.jscm.yxtyg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jscm.yxtyg.common.PageResult;
import com.jscm.yxtyg.entity.Requirement;
import com.jscm.yxtyg.entity.UrgeRecord;
import com.jscm.yxtyg.entity.User;
import com.jscm.yxtyg.exception.BusinessException;
import com.jscm.yxtyg.mapper.RequirementMapper;
import com.jscm.yxtyg.mapper.UrgeRecordMapper;
import com.jscm.yxtyg.mapper.UserMapper;
import com.jscm.yxtyg.service.UrgeRecordService;
import com.jscm.yxtyg.vo.UrgeRecordVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UrgeRecordServiceImpl extends ServiceImpl<UrgeRecordMapper, UrgeRecord> implements UrgeRecordService {

    @Autowired
    private RequirementMapper requirementMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void urge(Long requirementId, Long operatorId) {
        Requirement req = requirementMapper.selectById(requirementId);
        if (req == null) {
            throw new BusinessException("需求不存在");
        }
        UrgeRecord record = new UrgeRecord();
        record.setRequirementId(requirementId);
        record.setOperatorId(operatorId);
        this.save(record);
    }

    @Override
    public PageResult<UrgeRecordVO> queryPage(Long current, Long size) {
        LambdaQueryWrapper<UrgeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(UrgeRecord::getCreateTime);
        Page<UrgeRecord> page = new Page<>(current, size);
        Page<UrgeRecord> result = this.page(page, wrapper);
        Map<Long, Requirement> requirementMap = batchRequirementMap(result.getRecords());
        Map<Long, User> userMap = batchUserMap(result.getRecords());
        List<UrgeRecordVO> voList = result.getRecords().stream()
                .map(r -> toVO(r, requirementMap, userMap))
                .collect(Collectors.toList());
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), voList);
    }

    private Map<Long, Requirement> batchRequirementMap(List<UrgeRecord> records) {
        if (records == null || records.isEmpty()) {
            return new HashMap<>();
        }
        List<Long> requirementIds = records.stream()
                .map(UrgeRecord::getRequirementId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (requirementIds.isEmpty()) {
            return new HashMap<>();
        }
        return requirementMapper.selectBatchIds(requirementIds).stream()
                .collect(Collectors.toMap(Requirement::getId, Function.identity(), (r1, r2) -> r1));
    }

    private Map<Long, User> batchUserMap(List<UrgeRecord> records) {
        if (records == null || records.isEmpty()) {
            return new HashMap<>();
        }
        List<Long> operatorIds = records.stream()
                .map(UrgeRecord::getOperatorId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (operatorIds.isEmpty()) {
            return new HashMap<>();
        }
        return userMapper.selectBatchIds(operatorIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity(), (u1, u2) -> u1));
    }

    private UrgeRecordVO toVO(UrgeRecord record, Map<Long, Requirement> requirementMap, Map<Long, User> userMap) {
        UrgeRecordVO vo = new UrgeRecordVO();
        vo.setId(record.getId());
        vo.setRequirementId(record.getRequirementId());
        vo.setOperatorId(record.getOperatorId());
        vo.setCreateTime(record.getCreateTime());
        Requirement req = requirementMap.get(record.getRequirementId());
        if (req != null) {
            vo.setRequirementName(req.getName());
        }
        User user = userMap.get(record.getOperatorId());
        if (user != null) {
            vo.setOperatorName(user.getRealName());
        }
        return vo;
    }
}
