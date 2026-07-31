package com.young.controller;

import com.young.common.Result;
import com.young.mapper.BizDelegationMapper;
import com.young.mapper.BizReportMapper;
import com.young.mapper.BizSampleTaskMapper;
import com.young.mapper.BizInspectionRecordMapper;
import com.young.mapper.StdInspectionItemMapper;
import com.young.pojo.BizDelegation;
import com.young.pojo.BizReport;
import com.young.pojo.BizSampleTask;
import com.young.pojo.BizInspectionRecord;
import com.young.pojo.StdInspectionItem;
import org.springframework.beans.factory.annotation.Autowired;
import com.young.mapper.SysOperateLogMapper;
import com.young.pojo.SysOperateLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 首页大屏数据概览接口
 */
@Tag(name = "数据看板")
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private BizDelegationMapper delegationMapper;

    @Autowired
    private BizSampleTaskMapper taskMapper;

    @Autowired
    private BizReportMapper reportMapper;

    @Autowired
    private BizInspectionRecordMapper recordMapper;

    @Autowired
    private StdInspectionItemMapper itemMapper;

    @Autowired
    private SysOperateLogMapper operateLogMapper;

    @Operation(summary = "获取首页大屏统计数据")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getDashboardStats(jakarta.servlet.http.HttpServletRequest request) {
        Map<String, Object> stats = new HashMap<>();

        Object roleIdObj = request.getAttribute("roleId");
        String roleIdStr = roleIdObj != null ? String.valueOf(roleIdObj) : "";
        Long userId = null;
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj != null) {
            userId = Long.valueOf(String.valueOf(userIdObj));
        }

        // 基础数据源
        List<BizDelegation> allDelegations = delegationMapper.selectAll();
        List<BizSampleTask> allTasks = taskMapper.selectAll();
        List<BizReport> allReports = reportMapper.selectAll();
        List<BizInspectionRecord> allRecords = recordMapper.selectAll();
        if (allRecords == null) allRecords = new ArrayList<>();

        // 根据角色进行数据过滤
        if ("3".equals(roleIdStr)) { // 客户：只能看到自己的委托和报告
            Long finalUserId = userId;
            allDelegations = allDelegations.stream().filter(d -> finalUserId.equals(d.getClientId())).collect(Collectors.toList());
            List<Long> myDelegationIds = allDelegations.stream().map(BizDelegation::getId).collect(Collectors.toList());
            allTasks = allTasks.stream().filter(t -> myDelegationIds.contains(t.getDelegationId())).collect(Collectors.toList());
            allReports = allReports.stream().filter(r -> myDelegationIds.contains(r.getDelegationId())).collect(Collectors.toList());
            List<Long> myTaskIds = allTasks.stream().map(BizSampleTask::getId).collect(Collectors.toList());
            allRecords = allRecords.stream().filter(r -> myTaskIds.contains(r.getTaskId())).collect(Collectors.toList());
        } else if ("2".equals(roleIdStr)) { // 质检员：只能看到分配给自己的盲样任务及相关的记录和报告
            Long finalUserId = userId;
            allTasks = allTasks.stream().filter(t -> finalUserId.equals(t.getInspectorId())).collect(Collectors.toList());
            List<Long> myTaskIds = allTasks.stream().map(BizSampleTask::getId).collect(Collectors.toList());
            List<Long> myDelegationIds = allTasks.stream().map(BizSampleTask::getDelegationId).collect(Collectors.toList());
            allDelegations = allDelegations.stream().filter(d -> myDelegationIds.contains(d.getId())).collect(Collectors.toList());
            allReports = allReports.stream().filter(r -> myDelegationIds.contains(r.getDelegationId())).collect(Collectors.toList());
            allRecords = allRecords.stream().filter(r -> myTaskIds.contains(r.getTaskId())).collect(Collectors.toList());
        }

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        // 1. 今日新增委托
        long todayDelegations = allDelegations.stream()
                .filter(d -> d.getSubmitTime() != null && d.getSubmitTime().toLocalDate().equals(today))
                .count();
        long yesterdayDelegations = allDelegations.stream()
                .filter(d -> d.getSubmitTime() != null && d.getSubmitTime().toLocalDate().equals(yesterday))
                .count();

        // 较昨日计算
        String delegationGrowthStr = "0.0";
        if (yesterdayDelegations == 0) {
            if (todayDelegations > 0) {
                delegationGrowthStr = "new";
            } else {
                delegationGrowthStr = "0.0";
            }
        } else {
            double growth = (double)(todayDelegations - yesterdayDelegations) / yesterdayDelegations * 100;
            delegationGrowthStr = String.format("%.1f", growth);
        }
        stats.put("todayDelegations", todayDelegations);
        stats.put("delegationGrowth", delegationGrowthStr);

        // 2. 待检盲样任务 (状态 = 0)
        long pendingTasks = allTasks.stream()
                .filter(t -> t.getStatus() != null && t.getStatus() == 0)
                .count();
        stats.put("pendingTasks", pendingTasks);

        // 3. 当月已签发报告
        if ("2".equals(roleIdStr)) {
            long monthTasks = allTasks.stream()
                .filter(t -> t.getStatus() != null && t.getStatus() == 1)
                .count();
            stats.put("monthReports", monthTasks);
            stats.put("isInspector", true);
        } else {
            long monthReports = allReports.stream()
                    .filter(r -> r.getIssueTime() != null &&
                            r.getIssueTime().getYear() == today.getYear() &&
                            r.getIssueTime().getMonth() == today.getMonth())
                    .count();
            stats.put("monthReports", monthReports);
            stats.put("isInspector", false);
        }

        // 总体任务/报告完成率
        long completedCount = "2".equals(roleIdStr) ?
            allTasks.stream().filter(t -> t.getStatus() != null && t.getStatus() == 1).count() :
            allReports.size();
        long totalCount = "2".equals(roleIdStr) ? allTasks.size() : allDelegations.size();
        double completionRate = totalCount == 0 ? 0.0 : (double) completedCount / totalCount * 100;
        stats.put("completionRate", String.format("%.1f", completionRate));

        // 4. 总体合格率
        double passRate = 0.0;
        if ("2".equals(roleIdStr)) {
            if (!allRecords.isEmpty()) {
                long itemQualifiedCount = allRecords.stream().filter(r -> r.getResult() != null && r.getResult() == 1).count();
                passRate = (double) itemQualifiedCount / allRecords.size() * 100;
            }
        } else {
            if (!allReports.isEmpty()) {
                long qualifiedCount = allReports.stream().filter(r -> r.getFinalConclusion() != null && r.getFinalConclusion() == 1).count();
                passRate = (double) qualifiedCount / allReports.size() * 100;
            }
        }
        stats.put("passRate", String.format("%.1f", passRate));
        stats.put("passRateGrowth", "0.0");

        // 5. 组装 ECharts 数据：近7日检测委托趋势
        List<String> trendDates = new ArrayList<>();
        List<Long> trendCounts = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i);
            trendDates.add(d.toString());
            long count = allDelegations.stream()
                    .filter(del -> del.getSubmitTime() != null && del.getSubmitTime().toLocalDate().equals(d))
                    .count();
            trendCounts.add(count);
        }
        stats.put("trendDates", trendDates);
        stats.put("trendCounts", trendCounts);

        // 6. 组装 ECharts 数据：不良品缺陷分类分析
        List<Map<String, Object>> defectData = new ArrayList<>();

        // 收集所有不合格记录的 itemId，批量查询
        Set<Long> failedItemIds = allRecords.stream()
                .filter(r -> r.getResult() != null && r.getResult() == 0)
                .map(BizInspectionRecord::getItemId)
                .collect(Collectors.toSet());

        Map<Long, StdInspectionItem> itemMap = Collections.emptyMap();
        if (!failedItemIds.isEmpty()) {
            List<StdInspectionItem> items = itemMapper.selectByIds(new ArrayList<>(failedItemIds));
            itemMap = items.stream().collect(Collectors.toMap(StdInspectionItem::getId, Function.identity()));
        }

        Map<String, Integer> defectCountMap = new HashMap<>();
        for (BizInspectionRecord r : allRecords) {
            if (r.getResult() != null && r.getResult() == 0) {
                String itemName = "未知项目异常";
                StdInspectionItem item = itemMap.get(r.getItemId());
                if (item != null && item.getItemName() != null) {
                    itemName = item.getItemName() + "异常";
                }
                defectCountMap.put(itemName, defectCountMap.getOrDefault(itemName, 0) + 1);
            }
        }

        if (defectCountMap.isEmpty()) {
            Map<String, Object> d1 = new HashMap<>(); d1.put("name", "无不合格项"); d1.put("value", 1);
            defectData.add(d1);
        } else {
            for (Map.Entry<String, Integer> entry : defectCountMap.entrySet()) {
                Map<String, Object> d = new HashMap<>();
                d.put("name", entry.getKey());
                d.put("value", entry.getValue());
                defectData.add(d);
            }
        }

        stats.put("defectData", defectData);

        // 7. 最新系统动态日志
        List<Map<String, Object>> dynamicLogs = new ArrayList<>();

        List<Long> allowedDelegationIds = null;
        if ("3".equals(roleIdStr)) {
            allowedDelegationIds = allDelegations.stream().map(BizDelegation::getId).collect(Collectors.toList());
            if (allowedDelegationIds.isEmpty()) {
                allowedDelegationIds.add(-1L);
            }
        } else if ("2".equals(roleIdStr)) {
            allowedDelegationIds = allTasks.stream().map(BizSampleTask::getDelegationId).collect(Collectors.toList());
            if (allowedDelegationIds.isEmpty()) {
                allowedDelegationIds.add(-1L);
            }
        }

        List<SysOperateLog> recentLogs = operateLogMapper.selectRecentLogs(6, allowedDelegationIds);
        for (SysOperateLog logRecord : recentLogs) {
            Map<String, Object> logMap = new HashMap<>();
            logMap.put("time", logRecord.getCreateTime().toString().replace("T", " "));
            logMap.put("action", logRecord.getAction());
            logMap.put("type", logRecord.getActionType());
            logMap.put("desc", logRecord.getDescription());
            logMap.put("operator", logRecord.getOperator());
            dynamicLogs.add(logMap);
        }

        stats.put("dynamicLogs", dynamicLogs);

        return Result.success(stats);
    }
}
