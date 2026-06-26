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

import java.util.List;
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
        List<UrgeRecordVO> voList = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), voList);
    }

    private UrgeRecordVO toVO(UrgeRecord record) {
        UrgeRecordVO vo = new UrgeRecordVO();
        vo.setId(record.getId());
        vo.setRequirementId(record.getRequirementId());
        vo.setOperatorId(record.getOperatorId());
        vo.setCreateTime(record.getCreateTime());
        Requirement req = requirementMapper.selectById(record.getRequirementId());
        if (req != null) {
            vo.setRequirementName(req.getName());
        }
        User user = userMapper.selectById(record.getOperatorId());
        if (user != null) {
            vo.setOperatorName(user.getRealName());
        }
        return vo;
    }
}
