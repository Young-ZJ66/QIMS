package com.young.service.impl;

import com.young.pojo.BizReport;
import com.young.pojo.BizDelegation;
import com.young.pojo.BizSampleTask;
import com.young.pojo.BizInspectionRecord;
import com.young.pojo.StdInspectionItem;
import com.young.pojo.SysUser;
import com.young.pojo.SysClient;
import com.young.pojo.StdStandard;
import com.young.pojo.SysOperateLog;
import com.young.pojo.enums.DelegationStatus;
import com.young.pojo.enums.InspectionResult;
import com.young.mapper.BizReportMapper;
import com.young.mapper.BizDelegationMapper;
import com.young.mapper.BizSampleTaskMapper;
import com.young.mapper.BizInspectionRecordMapper;
import com.young.mapper.StdInspectionItemMapper;
import com.young.mapper.SysUserMapper;
import com.young.mapper.SysClientMapper;
import com.young.mapper.StdStandardMapper;
import com.young.mapper.SysOperateLogMapper;
import com.young.service.BizReportService;
import com.young.utils.PdfReportHelper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BizReportServiceImpl implements BizReportService {

    @Autowired
    private BizReportMapper mapper;

    @Autowired
    private BizDelegationMapper delegationMapper;

    @Autowired
    private BizSampleTaskMapper taskMapper;

    @Autowired
    private BizInspectionRecordMapper recordMapper;

    @Autowired
    private StdInspectionItemMapper itemMapper;

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysClientMapper clientMapper;

    @Autowired
    private StdStandardMapper standardMapper;

    @Autowired
    private SysOperateLogMapper logMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int add(BizReport record) {
        // 校验：确保该委托单的所有检测项目均已录入
        BizDelegation delegation = delegationMapper.selectById(record.getDelegationId());
        if (delegation == null) {
            throw new RuntimeException("关联的委托单不存在");
        }

        // 获取该国标下规定的所有检测项目数量
        List<StdInspectionItem> allItems = itemMapper.selectAll().stream()
                .filter(item -> delegation.getStandardId().equals(item.getStandardId()))
                .collect(Collectors.toList());

        // 获取该委托单对应的所有盲样任务
        List<BizSampleTask> tasks = taskMapper.selectAll().stream()
                .filter(t -> record.getDelegationId().equals(t.getDelegationId()))
                .collect(Collectors.toList());

        if (tasks.isEmpty()) {
            throw new RuntimeException("该委托单尚未生成任何检测任务，无法签发报告");
        }

        // 获取所有已录入的检测记录
        List<Long> taskIds = tasks.stream().map(BizSampleTask::getId).collect(Collectors.toList());
        List<BizInspectionRecord> inspectionRecords = recordMapper.selectAll().stream()
                .filter(r -> taskIds.contains(r.getTaskId()))
                .collect(Collectors.toList());

        java.util.Set<Long> requiredItemIds = allItems.stream().map(StdInspectionItem::getId).collect(Collectors.toSet());
        java.util.Map<Long, java.util.Set<Long>> taskToItemIds = new java.util.HashMap<>();
        for (BizInspectionRecord r : inspectionRecords) {
            taskToItemIds.computeIfAbsent(r.getTaskId(), k -> new java.util.HashSet<>()).add(r.getItemId());
        }
        for (Long taskId : taskIds) {
            java.util.Set<Long> recordedItemIds = taskToItemIds.getOrDefault(taskId, java.util.Collections.emptySet());
            if (!recordedItemIds.containsAll(requiredItemIds)) {
                throw new RuntimeException("该委托单存在未完成检测的盲样任务，拒绝签发！");
            }
        }

        // 校验总结论：若存在不合格项，则强制将总结论置为不合格
        boolean hasFail = inspectionRecords.stream().anyMatch(r -> r.getResult() != null && r.getResult() == InspectionResult.UNQUALIFIED.getCode());
        if (hasFail) {
            record.setFinalConclusion(InspectionResult.UNQUALIFIED.getCode());
        } else {
            record.setFinalConclusion(InspectionResult.QUALIFIED.getCode());
        }

        if (record.getIssueTime() == null) {
            record.setIssueTime(LocalDateTime.now());
        }

        // 自动生成 PDF 报告文件
        try {
            String uploadDir = System.getProperty("user.dir") + "/uploads/reports/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String pdfFileName = "Report_" + record.getReportNo() + ".pdf";
            String pdfPath = uploadDir + pdfFileName;

            // 准备 PDF 需要的关联实体数据
            SysUser reviewer = record.getReviewerId() != null ? userMapper.selectById(record.getReviewerId()) : null;
            String reviewerName = reviewer != null && reviewer.getRealName() != null ? reviewer.getRealName() : "/";

            SysClient client = delegation.getClientId() != null ? clientMapper.selectById(delegation.getClientId()) : null;
            StdStandard standard = delegation.getStandardId() != null ? standardMapper.selectById(delegation.getStandardId()) : null;

            String inspectorName = "/";
            if (!tasks.isEmpty() && tasks.get(0).getInspectorId() != null) {
                SysUser inspectorUser = userMapper.selectById(tasks.get(0).getInspectorId());
                if (inspectorUser != null && inspectorUser.getRealName() != null) {
                    inspectorName = inspectorUser.getRealName();
                }
            }

            // 调用 PDF 渲染工具类
            PdfReportHelper.generatePdf(pdfPath, record, delegation, standard, client, inspectorName, reviewerName, tasks, inspectionRecords, allItems);

            // 保存生成的 PDF 相对路径到数据库
            record.setReportFileUrl("/uploads/reports/" + pdfFileName);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("生成 PDF 报告失败: " + e.getMessage());
        }

        // 保存报告记录
        mapper.insert(record);

        // 更新委托单状态为 3-已出报告
        delegation.setStatus(DelegationStatus.REPORT_ISSUED.getCode());
        delegationMapper.update(delegation);

        String reviewerName = "/";
        if (record.getReviewerId() != null) {
            SysUser user = userMapper.selectById(record.getReviewerId());
            if (user != null && user.getRealName() != null) {
                reviewerName = user.getRealName();
            }
        }

        // 记录系统操作日志
        SysOperateLog operateLog = new SysOperateLog();
        operateLog.setDelegationId(record.getDelegationId());
        operateLog.setOperator(reviewerName);
        operateLog.setAction("报告签发");
        operateLog.setActionType("success");
        operateLog.setDescription("签发了报告编号 " + record.getReportNo());
        operateLog.setCreateTime(LocalDateTime.now());
        logMapper.insert(operateLog);

        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(BizReport record) {
        // 更新数据库
        int res = mapper.update(record);
        
        // 重新生成 PDF，覆盖旧文件
        try {
            BizReport existReport = mapper.selectById(record.getId());
            if (existReport == null) return res;
            
            BizDelegation delegation = delegationMapper.selectById(existReport.getDelegationId());
            List<StdInspectionItem> allItems = itemMapper.selectAll().stream()
                    .filter(item -> delegation.getStandardId().equals(item.getStandardId()))
                    .collect(Collectors.toList());
            List<BizSampleTask> tasks = taskMapper.selectAll().stream()
                    .filter(t -> existReport.getDelegationId().equals(t.getDelegationId()))
                    .collect(Collectors.toList());
            List<Long> taskIds = tasks.stream().map(BizSampleTask::getId).collect(Collectors.toList());
            List<BizInspectionRecord> inspectionRecords = recordMapper.selectAll().stream()
                    .filter(r -> taskIds.contains(r.getTaskId()))
                    .collect(Collectors.toList());

            String uploadDir = System.getProperty("user.dir") + "/uploads/reports/";
            String pdfFileName = "Report_" + existReport.getReportNo() + ".pdf";
            String pdfPath = uploadDir + pdfFileName;

            SysUser reviewer = existReport.getReviewerId() != null ? userMapper.selectById(existReport.getReviewerId()) : null;
            String reviewerName = reviewer != null && reviewer.getRealName() != null ? reviewer.getRealName() : "/";

            SysClient client = delegation.getClientId() != null ? clientMapper.selectById(delegation.getClientId()) : null;
            StdStandard standard = delegation.getStandardId() != null ? standardMapper.selectById(delegation.getStandardId()) : null;

            String inspectorName = "/";
            if (!tasks.isEmpty() && tasks.get(0).getInspectorId() != null) {
                SysUser inspectorUser = userMapper.selectById(tasks.get(0).getInspectorId());
                if (inspectorUser != null && inspectorUser.getRealName() != null) {
                    inspectorName = inspectorUser.getRealName();
                }
            }

            // 调用 PDF 渲染工具类重新生成
            PdfReportHelper.generatePdf(pdfPath, existReport, delegation, standard, client, inspectorName, reviewerName, tasks, inspectionRecords, allItems);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("更新 PDF 报告失败: " + e.getMessage());
        }

        return res;
    }

    @Override
    public int delete(Long id) {
        return mapper.deleteById(id);
    }

    @Override
    public BizReport getById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public List<BizReport> getAll() {
        return mapper.selectAll();
    }
}
