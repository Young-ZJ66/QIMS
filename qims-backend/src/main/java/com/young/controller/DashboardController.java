package com.young.controller;

import com.young.common.Result;
import com.young.mapper.BizDelegationMapper;
import com.young.mapper.BizReportMapper;
import com.young.mapper.BizSampleTaskMapper;
import com.young.mapper.BizInspectionRecordMapper;
import com.young.mapper.SysOperateLogMapper;
import com.young.pojo.SysOperateLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.*;

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
    private SysOperateLogMapper operateLogMapper;

    @Operation(summary = "获取首页大屏统计数据")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getDashboardStats(HttpServletRequest request) {
        Map<String, Object> stats = new HashMap<>();

        Object roleIdObj = request.getAttribute("roleId");
        String roleIdStr = roleIdObj != null ? String.valueOf(roleIdObj) : "";
        Long userId = null;
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj != null) {
            userId = Long.valueOf(String.valueOf(userIdObj));
        }

        // 1. 今日新增委托 - 使用 SQL 聚合
        List<Map<String, Object>> trendData = delegationMapper.countByRecentDays(7);
        Map<String, Long> dateCountMap = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            dateCountMap.put(LocalDate.now().minusDays(i).toString(), 0L);
        }
        for (Map<String, Object> row : trendData) {
            String dateStr = String.valueOf(row.get("submit_date"));
            Long count = ((Number) row.get("cnt")).longValue();
            dateCountMap.put(dateStr, count);
        }

        String todayStr = LocalDate.now().toString();
        String yesterdayStr = LocalDate.now().minusDays(1).toString();
        long todayDelegations = dateCountMap.getOrDefault(todayStr, 0L);
        long yesterdayDelegations = dateCountMap.getOrDefault(yesterdayStr, 0L);

        String delegationGrowthStr = "0.0";
        if (yesterdayDelegations == 0) {
            delegationGrowthStr = todayDelegations > 0 ? "new" : "0.0";
        } else {
            double growth = (double) (todayDelegations - yesterdayDelegations) / yesterdayDelegations * 100;
            delegationGrowthStr = String.format("%.1f", growth);
        }
        stats.put("todayDelegations", todayDelegations);
        stats.put("delegationGrowth", delegationGrowthStr);

        // 2. 待检盲样任务 - 使用 SQL COUNT
        long pendingTasks = taskMapper.countByStatus(0);
        stats.put("pendingTasks", pendingTasks);

        // 3. 当月数据
        if ("2".equals(roleIdStr)) {
            // 质检员：当月已完成任务数
            int monthTasks = taskMapper.countCompletedByInspectorId(userId);
            stats.put("monthReports", monthTasks);
            stats.put("isInspector", true);
        } else {
            // 管理员/客户：当月签发报告数
            int monthReports = reportMapper.countThisMonth();
            stats.put("monthReports", monthReports);
            stats.put("isInspector", false);
        }

        // 完成率
        long completedCount;
        long totalCount;
        if ("2".equals(roleIdStr)) {
            completedCount = taskMapper.countCompletedByInspectorId(userId);
            totalCount = completedCount; // 质检员只看自己的完成率
        } else {
            completedCount = reportMapper.countTotal();
            totalCount = delegationMapper.countTotal();
        }
        double completionRate = totalCount == 0 ? 0.0 : (double) completedCount / totalCount * 100;
        stats.put("completionRate", String.format("%.1f", completionRate));

        // 4. 合格率
        double passRate = 0.0;
        if ("2".equals(roleIdStr)) {
            // 质检员：基于检测记录的合格率
            // 简化：使用报告合格率
            int total = reportMapper.countTotal();
            if (total > 0) {
                int qualified = reportMapper.countQualified();
                passRate = (double) qualified / total * 100;
            }
        } else {
            int total = reportMapper.countTotal();
            if (total > 0) {
                int qualified = reportMapper.countQualified();
                passRate = (double) qualified / total * 100;
            }
        }
        stats.put("passRate", String.format("%.1f", passRate));
        stats.put("passRateGrowth", "0.0");

        // 5. 近7日趋势图数据
        List<String> trendDates = new ArrayList<>(dateCountMap.keySet());
        List<Long> trendCounts = new ArrayList<>(dateCountMap.values());
        stats.put("trendDates", trendDates);
        stats.put("trendCounts", trendCounts);

        // 6. 缺陷分类分析 - 使用 SQL 聚合
        List<Map<String, Object>> defectData = recordMapper.countFailedByItemName();
        if (defectData.isEmpty()) {
            Map<String, Object> d = new HashMap<>();
            d.put("name", "无不合格项");
            d.put("value", 1);
            defectData = Collections.singletonList(d);
        }
        stats.put("defectData", defectData);

        // 7. 最新系统动态日志
        List<Long> allowedDelegationIds = null;
        if ("3".equals(roleIdStr) || "2".equals(roleIdStr)) {
            // 客户和质检员只能看到相关的日志
            allowedDelegationIds = new ArrayList<>();
            // 简化：对于客户和质检员，传递空列表让他们看不到日志（或后续优化）
            allowedDelegationIds.add(-1L);
        }

        List<SysOperateLog> recentLogs = operateLogMapper.selectRecentLogs(6, allowedDelegationIds);
        List<Map<String, Object>> dynamicLogs = new ArrayList<>();
        for (SysOperateLog logRecord : recentLogs) {
            Map<String, Object> logMap = new HashMap<>();
            logMap.put("time", logRecord.getCreateTime() != null ? logRecord.getCreateTime().toString().replace("T", " ") : "");
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