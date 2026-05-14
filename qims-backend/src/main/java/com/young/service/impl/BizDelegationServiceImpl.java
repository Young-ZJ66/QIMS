package com.young.service.impl;

import com.young.pojo.BizDelegation;
import com.young.pojo.BizSampleTask;
import com.young.mapper.BizDelegationMapper;
import com.young.mapper.BizSampleTaskMapper;
import com.young.service.BizDelegationService;
import com.young.mapper.SysOperateLogMapper;
import com.young.pojo.SysOperateLog;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class BizDelegationServiceImpl implements BizDelegationService {

    @Autowired
    private BizDelegationMapper mapper;

    @Autowired
    private BizSampleTaskMapper taskMapper;

    @Autowired
    private SysOperateLogMapper logMapper;

    @Override
    public int add(BizDelegation record) {
        return mapper.insert(record);
    }

    @Override
    public int update(BizDelegation record) {
        return mapper.update(record);
    }

    @Override
    public int delete(Long id) {
        return mapper.deleteById(id);
    }

    @Override
    public BizDelegation getById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public List<BizDelegation> getAll() {
        return mapper.selectAll();
    }

    @Override
    public List<BizDelegation> getByClientId(Long clientId) {
        return mapper.selectByClientId(clientId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitDelegation(BizDelegation delegation) {
        // 自动生成委托单号 D + 日期 + 随机字符
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomStr = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        String delegationNo = "D" + dateStr + randomStr;
        delegation.setDelegationNo(delegationNo);

        // 设置初始状态：0-待收样
        delegation.setStatus(0);
        delegation.setSubmitTime(LocalDateTime.now());

        mapper.insert(delegation);

        // 记录系统操作日志
        SysOperateLog operateLog = new SysOperateLog();
        operateLog.setDelegationId(delegation.getId());
        operateLog.setOperator("客户");
        operateLog.setAction("新委托");
        operateLog.setActionType("primary");
        operateLog.setDescription("收到新的检验委托单 " + delegationNo);
        operateLog.setCreateTime(LocalDateTime.now());
        logMapper.insert(operateLog);

        return delegationNo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String receiveSampleAndAssign(Long delegationId, Long inspectorId, Long receiverId) {
        BizDelegation delegation = mapper.selectById(delegationId);
        if (delegation == null || delegation.getStatus() != 0) {
            throw new RuntimeException("委托单不存在或当前状态无法收样");
        }

        // 1. 更新委托单状态：1-检测中
        delegation.setStatus(1);
        mapper.update(delegation);

        // 2. 盲样化处理，隐去客户信息生成任务
        int quantity = delegation.getSampleQuantity() != null && delegation.getSampleQuantity() > 0 ? delegation.getSampleQuantity() : 1;
        String firstBlindCode = "";

        for (int i = 1; i <= quantity; i++) {
            BizSampleTask task = new BizSampleTask();
            task.setDelegationId(delegationId);

            // 生成盲样编号: SAM + 日期 + 随机字符 + 序列号
            String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String randomStr = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
            String blindCode = "SAM-" + dateStr + "-" + randomStr + "-" + String.format("%02d", i);

            if (i == 1) {
                firstBlindCode = blindCode;
            }

            task.setBlindSampleCode(blindCode);
            task.setInspectorId(inspectorId);
            task.setReceiverId(receiverId);
            task.setReceiveTime(LocalDateTime.now());
            task.setStatus(0); // 0-待检测

            taskMapper.insert(task);

            // 记录系统操作日志
            SysOperateLog operateLog = new SysOperateLog();
            operateLog.setDelegationId(delegationId);
            operateLog.setOperator("管理员");
            operateLog.setAction("盲样分配");
            operateLog.setActionType("warning");
            operateLog.setDescription("生成并分配盲样任务 " + blindCode);
            operateLog.setCreateTime(LocalDateTime.now());
            logMapper.insert(operateLog);
        }

        // 如果是多样品，返回提示语包含数量
        if (quantity > 1) {
            return "批量生成了 " + quantity + " 个盲样任务，首个编号为: " + firstBlindCode;
        }
        return firstBlindCode;
    }
}
