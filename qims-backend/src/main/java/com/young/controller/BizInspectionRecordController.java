package com.young.controller;

import com.young.pojo.BizInspectionRecord;
import com.young.pojo.BizSampleTask;
import com.young.pojo.BizDelegation;
import com.young.pojo.StdInspectionItem;
import com.young.service.BizInspectionRecordService;
import com.young.mapper.BizSampleTaskMapper;
import com.young.mapper.BizDelegationMapper;
import com.young.mapper.StdInspectionItemMapper;
import com.young.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import com.young.mapper.SysOperateLogMapper;
import com.young.pojo.SysOperateLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Api(tags = "检验记录接口")
@RestController
@RequestMapping("/api/biz-inspection-record")
public class BizInspectionRecordController {

    @Autowired
    private BizInspectionRecordService service;

    @Autowired
    private BizSampleTaskMapper taskMapper;

    @Autowired
    private BizDelegationMapper delegationMapper;

    @Autowired
    private StdInspectionItemMapper itemMapper;

    @Autowired
    private SysOperateLogMapper logMapper;

    /**
     * 根据委托单 ID 查询其下的所有盲样检测记录（用于管理员出报告前查看）
     */
    @ApiOperation("根据委托单查询检验记录")
    @GetMapping("/delegation/{delegationId}")
    public Result<List<BizInspectionRecord>> getByDelegationId(@PathVariable Long delegationId) {
        // 查询委托单对应的所有盲样任务
        List<BizSampleTask> tasks = taskMapper.selectAll().stream()
                .filter(t -> delegationId.equals(t.getDelegationId()))
                .collect(Collectors.toList());

        if (tasks.isEmpty()) {
            return Result.success(Collections.emptyList());
        }

        // 收集所有这些任务的 record
        List<Long> taskIds = tasks.stream().map(BizSampleTask::getId).collect(Collectors.toList());
        List<BizInspectionRecord> records = service.getAll().stream()
                .filter(r -> taskIds.contains(r.getTaskId()))
                .collect(Collectors.toList());

        java.util.Map<Long, String> taskCodeMap = tasks.stream()
                .collect(Collectors.toMap(BizSampleTask::getId, BizSampleTask::getBlindSampleCode));

        for (BizInspectionRecord r : records) {
            StdInspectionItem item = itemMapper.selectById(r.getItemId());
            if (item != null) {
                r.setItemName(item.getItemName());
            }
            r.setBlindSampleCode(taskCodeMap.get(r.getTaskId()));
        }

        return Result.success(records);
    }

    /**
     * 3. 检测员批量录入实测数据，系统自动进行合格判定
     */
    @ApiOperation("批量提交检测数据")
    @PostMapping("/submit-batch-data")
    public Result<Void> submitBatchInspectionData(@RequestBody List<BizInspectionRecord> records, HttpServletRequest request) {
        try {
            if (records == null || records.isEmpty()) {
                return Result.error("未提交任何检测数据");
            }
            
            Long taskId = records.get(0).getTaskId();
            BizSampleTask task = taskMapper.selectById(taskId);
            if (task == null) {
                return Result.error("盲样任务不存在");
            }

            // 权限及幂等校验
            Object roleIdObj = request.getAttribute("roleId");
            Object userIdObj = request.getAttribute("userId");
            if (roleIdObj == null || !String.valueOf(roleIdObj).equals("2") || userIdObj == null || !task.getInspectorId().equals(Long.valueOf(String.valueOf(userIdObj)))) {
                return Result.error("无权限：您不是该任务的被指派质检员");
            }
            if (task.getStatus() != null && task.getStatus() == 1) {
                return Result.error("该任务已检测完成，不能重复提交数据");
            }
            
            // 逐条判定并保存
            for (BizInspectionRecord record : records) {
                service.submitInspectionData(record);
            }
            
            // 更新盲样任务状态为已完成 (1)
            task.setStatus(1);
            task.setFinishTime(LocalDateTime.now());
            taskMapper.update(task);

            // 记录系统操作日志
            SysOperateLog operateLog = new SysOperateLog();
            operateLog.setDelegationId(task.getDelegationId());
            operateLog.setOperator("质检员");
            operateLog.setAction("完成检测");
            operateLog.setActionType("info");
            operateLog.setDescription("质检员完成了盲样 " + task.getBlindSampleCode() + " 的实测录入");
            operateLog.setCreateTime(LocalDateTime.now());
            logMapper.insert(operateLog);

            // 联动更新委托单状态为 2 (审核中)
            BizDelegation delegation = delegationMapper.selectById(task.getDelegationId());
            if (delegation != null && delegation.getStatus() != null && delegation.getStatus() == 1) {
                List<BizSampleTask> allTasks = taskMapper.selectAll().stream()
                        .filter(t -> task.getDelegationId().equals(t.getDelegationId()))
                        .collect(Collectors.toList());
                boolean allDone = allTasks.stream().allMatch(t -> t.getStatus() != null && t.getStatus() == 1);
                if (allDone) {
                    delegation.setStatus(2);
                    delegationMapper.update(delegation);
                }
            }
            
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 基础 CRUD

    @ApiOperation("新增")
    @PostMapping
    public Result<Void> add(@RequestBody BizInspectionRecord record) {
        service.add(record);
        return Result.success();
    }

    @ApiOperation("修改")
    @PutMapping
    public Result<Void> update(@RequestBody BizInspectionRecord record) {
        service.update(record);
        return Result.success();
    }

    @ApiOperation("删除")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.success();
    }

    @ApiOperation("根据ID查询")
    @GetMapping("/{id}")
    public Result<BizInspectionRecord> getById(@PathVariable Long id) {
        return Result.success(service.getById(id));
    }

    @ApiOperation("查询所有")
    @GetMapping
    public Result<List<BizInspectionRecord>> getAll() {
        return Result.success(service.getAll());
    }
}
