package com.jscm.yxtyg.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jscm.yxtyg.common.PageResult;
import com.jscm.yxtyg.dto.RequirementDTO;
import com.jscm.yxtyg.dto.RequirementExcelDTO;
import com.jscm.yxtyg.dto.RequirementQueryDTO;
import com.jscm.yxtyg.entity.Requirement;
import com.jscm.yxtyg.entity.User;
import com.jscm.yxtyg.exception.BusinessException;
import com.jscm.yxtyg.mapper.RequirementMapper;
import com.jscm.yxtyg.mapper.UserMapper;
import com.jscm.yxtyg.service.RequirementService;
import com.jscm.yxtyg.util.RequirementExcelParser;
import com.jscm.yxtyg.vo.RequirementDetailVO;
import com.jscm.yxtyg.vo.RequirementVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RequirementServiceImpl extends ServiceImpl<RequirementMapper, Requirement> implements RequirementService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RequirementExcelParser excelParser;

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_FILLED = "FILLED";
    private static final String STATUS_APPROVED = "APPROVED";

    @Override
    public PageResult<RequirementVO> queryPage(RequirementQueryDTO queryDTO, Long currentUserId, String role) {
        LambdaQueryWrapper<Requirement> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getName())) {
            wrapper.like(Requirement::getName, queryDTO.getName());
        }
        if (StringUtils.hasText(queryDTO.getSystemName())) {
            wrapper.like(Requirement::getSystemName, queryDTO.getSystemName());
        }
        if (StringUtils.hasText(queryDTO.getStatus())) {
            wrapper.eq(Requirement::getStatus, queryDTO.getStatus());
        }
        if (queryDTO.getProductManagerId() != null) {
            wrapper.eq(Requirement::getProductManagerId, queryDTO.getProductManagerId());
        }
        if ("PRODUCT_MANAGER".equals(role)) {
            wrapper.eq(Requirement::getProductManagerId, currentUserId);
        }
        wrapper.orderByDesc(Requirement::getCreateTime);

        Page<Requirement> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        Page<Requirement> result = this.page(page, wrapper);
        Map<Long, User> userMap = batchUserMap(result.getRecords());
        List<RequirementVO> voList = result.getRecords().stream()
                .map(req -> toVO(req, userMap))
                .collect(Collectors.toList());
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), voList);
    }

    @Override
    public RequirementDetailVO getDetail(Long id, Long currentUserId, String role) {
        Requirement req = this.getById(id);
        if (req == null) {
            throw new BusinessException("需求不存在");
        }
        if ("PRODUCT_MANAGER".equals(role) && !Objects.equals(req.getProductManagerId(), currentUserId)) {
            throw new BusinessException("无权限查看该需求");
        }
        return (RequirementDetailVO) toVO(req);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(RequirementDTO dto) {
        validateProductManager(dto.getProductManagerId());
        Requirement req = new Requirement();
        BeanUtils.copyProperties(dto, req);
        req.setStatus(STATUS_PENDING);
        req.setReducedWorkload(null);
        this.save(req);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, RequirementDTO dto) {
        Requirement req = this.getById(id);
        if (req == null) {
            throw new BusinessException("需求不存在");
        }
        validateProductManager(dto.getProductManagerId());
        BeanUtils.copyProperties(dto, req, "status");
        req.setId(id);
        recalculateReduced(req);
        this.updateById(req);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        this.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importExcel(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        RequirementExcelParser.ParseResult parseResult = excelParser.parse(file);

        List<RequirementExcelParser.ParseResult.FailDetail> failDetails = new ArrayList<>(parseResult.getFailDetails());
        List<RequirementExcelDTO> candidateList = new ArrayList<>(parseResult.getSuccessList());

        Map<String, Long> nameCount = candidateList.stream()
                .collect(Collectors.groupingBy(d -> d.getName().trim(), Collectors.counting()));
        Set<String> existingNames = queryExistingNames(nameCount.keySet());

        List<RequirementExcelDTO> validList = new ArrayList<>();
        for (RequirementExcelDTO dto : candidateList) {
            String name = dto.getName().trim();
            if (nameCount.get(name) > 1) {
                failDetails.add(new RequirementExcelParser.ParseResult.FailDetail(dto.getRow(), "需求名称重复"));
                continue;
            }
            if (existingNames.contains(name)) {
                failDetails.add(new RequirementExcelParser.ParseResult.FailDetail(dto.getRow(), "需求名称已存在"));
                continue;
            }
            validList.add(dto);
        }

        if (validList.isEmpty() && !failDetails.isEmpty()) {
            result.put("successCount", 0);
            result.put("failCount", failDetails.size());
            result.put("failDetails", failDetails);
            result.put("message", "导入失败，请检查Excel数据");
            return result;
        }

        int successCount = 0;
        Map<String, User> userCache = new HashMap<>();
        List<Requirement> saveList = new ArrayList<>();

        for (RequirementExcelDTO dto : validList) {
            try {
                User pm = userCache.computeIfAbsent(dto.getProductManagerName(),
                        name -> userMapper.selectByUsername(name));
                if (pm == null || !"PRODUCT_MANAGER".equals(pm.getRole())) {
                    failDetails.add(new RequirementExcelParser.ParseResult.FailDetail(dto.getRow(), "产品经理不存在"));
                    continue;
                }
                Requirement req = new Requirement();
                req.setName(dto.getName());
                req.setDescription(dto.getDescription());
                req.setProductManagerId(pm.getId());
                req.setSystemName(dto.getSystemName());
                req.setInitialWorkload(parseDecimal(dto.getInitialWorkload(), "初核工作量"));
                req.setInitialAmount(parseNonNegativeDecimal(dto.getInitialAmount(), "初核金额"));
                req.setFinalWorkload(parseNullableDecimal(dto.getFinalWorkload()));
                req.setStatus(parseStatus(dto.getStatus()));
                recalculateReduced(req);
                saveList.add(req);
                successCount++;
            } catch (BusinessException e) {
                failDetails.add(new RequirementExcelParser.ParseResult.FailDetail(dto.getRow(), e.getMessage()));
            }
        }

        if (!saveList.isEmpty()) {
            this.saveBatch(saveList, 100);
        }

        result.put("successCount", successCount);
        result.put("failCount", failDetails.size());
        result.put("failDetails", failDetails);
        result.put("message", String.format("导入完成，成功%d条，失败%d条", successCount, failDetails.size()));
        return result;
    }

    @Override
    public byte[] downloadTemplate() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<RequirementExcelDTO> data = new ArrayList<>();
        RequirementExcelDTO sample = new RequirementExcelDTO();
        sample.setName("示例需求");
        sample.setDescription("这是示例需求描述");
        sample.setProductManagerName("产品经理姓名");
        sample.setSystemName("归属系统");
        sample.setInitialWorkload("5.0");
        sample.setInitialAmount("5000.00");
        sample.setFinalWorkload("");
        sample.setStatus("PENDING");
        data.add(sample);
        EasyExcel.write(out, RequirementExcelDTO.class).sheet("需求模板").doWrite(data);
        return out.toByteArray();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void fillFinalWorkload(Long id, Long currentUserId, String role, BigDecimal finalWorkload) {
        Requirement req = this.getById(id);
        if (req == null) {
            throw new BusinessException("需求不存在");
        }
        if (!"PRODUCT_MANAGER".equals(role)) {
            throw new BusinessException("无权限");
        }
        if (!Objects.equals(req.getProductManagerId(), currentUserId)) {
            throw new BusinessException("只能填写自己负责的需求");
        }
        if (!STATUS_PENDING.equals(req.getStatus())) {
            throw new BusinessException("当前状态不可填写最终工作量");
        }
        req.setFinalWorkload(finalWorkload);
        req.setStatus(STATUS_FILLED);
        recalculateReduced(req);
        this.updateById(req);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id) {
        Requirement req = this.getById(id);
        if (req == null) {
            throw new BusinessException("需求不存在");
        }
        if (!STATUS_FILLED.equals(req.getStatus())) {
            throw new BusinessException("只能核定已填写状态的需求");
        }
        req.setStatus(STATUS_APPROVED);
        this.updateById(req);
    }

    private void validateProductManager(Long productManagerId) {
        if (productManagerId == null) {
            throw new BusinessException("产品经理不能为空");
        }
        User user = userMapper.selectById(productManagerId);
        if (user == null || !"PRODUCT_MANAGER".equals(user.getRole())) {
            throw new BusinessException("产品经理不存在或角色不正确");
        }
    }

    private Set<String> queryExistingNames(Set<String> names) {
        if (names == null || names.isEmpty()) {
            return new HashSet<>();
        }
        LambdaQueryWrapper<Requirement> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Requirement::getName, names);
        List<Requirement> list = this.list(wrapper);
        if (list == null) {
            return new HashSet<>();
        }
        return list.stream()
                .map(r -> r.getName().trim())
                .collect(Collectors.toSet());
    }

    private Map<Long, User> batchUserMap(List<Requirement> records) {
        if (records == null || records.isEmpty()) {
            return new HashMap<>();
        }
        Set<Long> userIds = records.stream()
                .map(Requirement::getProductManagerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return new HashMap<>();
        }
        return userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity(), (u1, u2) -> u1));
    }

    private void recalculateReduced(Requirement req) {
        if (req.getInitialWorkload() != null && req.getFinalWorkload() != null) {
            req.setReducedWorkload(req.getInitialWorkload().subtract(req.getFinalWorkload()));
        } else {
            req.setReducedWorkload(null);
        }
    }

    private BigDecimal parseDecimal(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(fieldName + "不能为空");
        }
        try {
            BigDecimal d = new BigDecimal(value.trim());
            if (d.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException(fieldName + "必须大于0");
            }
            return d;
        } catch (NumberFormatException e) {
            throw new BusinessException(fieldName + "格式错误");
        }
    }

    private BigDecimal parseNonNegativeDecimal(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(fieldName + "不能为空");
        }
        try {
            BigDecimal d = new BigDecimal(value.trim());
            if (d.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException(fieldName + "不能小于0");
            }
            return d;
        } catch (NumberFormatException e) {
            throw new BusinessException(fieldName + "格式错误");
        }
    }

    private BigDecimal parseNullableDecimal(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            throw new BusinessException("最终核定工作量格式错误");
        }
    }

    private String parseStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return STATUS_PENDING;
        }
        String s = status.trim().toUpperCase();
        if (STATUS_PENDING.equals(s) || STATUS_FILLED.equals(s) || STATUS_APPROVED.equals(s)) {
            return s;
        }
        throw new BusinessException("状态必须是 PENDING、FILLED 或 APPROVED");
    }

    private RequirementVO toVO(Requirement req) {
        return toVO(req, null);
    }

    private RequirementVO toVO(Requirement req, Map<Long, User> userMap) {
        RequirementVO vo = new RequirementDetailVO();
        BeanUtils.copyProperties(req, vo);
        User pm = userMap != null ? userMap.get(req.getProductManagerId()) : userMapper.selectById(req.getProductManagerId());
        if (pm != null) {
            vo.setProductManagerName(pm.getRealName());
        }
        vo.setStatusLabel(mapStatusLabel(req.getStatus()));
        return vo;
    }

    private String mapStatusLabel(String status) {
        if (STATUS_PENDING.equals(status)) return "待填写";
        if (STATUS_FILLED.equals(status)) return "已填写";
        if (STATUS_APPROVED.equals(status)) return "已核定";
        return status;
    }
}
