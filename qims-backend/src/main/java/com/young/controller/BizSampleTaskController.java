package com.young.controller;

import com.young.pojo.BizDelegation;
import com.young.pojo.BizSampleTask;
import com.young.pojo.StdInspectionItem;
import com.young.service.BizSampleTaskService;
import com.young.mapper.BizDelegationMapper;
import com.young.mapper.BizSampleTaskMapper;
import com.young.mapper.StdInspectionItemMapper;
import com.young.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 盲样任务接口
 */
@Tag(name = "盲样任务")
@RestController
@RequestMapping("/api/biz-sample-task")
public class BizSampleTaskController {

    @Autowired
    private BizSampleTaskService service;

    @Autowired
    private BizSampleTaskMapper taskMapper;

    @Autowired
    private BizDelegationMapper delegationMapper;

    @Autowired
    private StdInspectionItemMapper itemMapper;

    /**
     * 动态获取该盲样任务对应需要检测的所有项目
     */
    @Operation(summary = "获取盲样任务对应的检测项目")
    @GetMapping("/{id}/items")
    public Result<List<StdInspectionItem>> getItemsByTaskId(@PathVariable Long id) {
        BizSampleTask task = taskMapper.selectById(id);
        if (task == null) {
            return Result.error("任务不存在");
        }
        BizDelegation delegation = delegationMapper.selectById(task.getDelegationId());
        if (delegation == null) {
            return Result.error("委托单不存在");
        }

        List<StdInspectionItem> targetItems = itemMapper.selectByStandardId(delegation.getStandardId());
        return Result.success(targetItems);
    }

    @Operation(summary = "新增盲样任务")
    @PostMapping
    public Result<Void> add(@RequestBody BizSampleTask record) {
        service.add(record);
        return Result.success();
    }

    @Operation(summary = "修改盲样任务")
    @PutMapping
    public Result<Void> update(@RequestBody BizSampleTask record) {
        service.update(record);
        return Result.success();
    }

    @Operation(summary = "删除盲样任务")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.success();
    }

    @Operation(summary = "根据ID查询盲样任务")
    @GetMapping("/{id}")
    public Result<BizSampleTask> getById(@PathVariable Long id) {
        return Result.success(service.getById(id));
    }

    @Operation(summary = "查询盲样任务列表")
    @GetMapping
    public Result<List<BizSampleTask>> getAll(jakarta.servlet.http.HttpServletRequest request) {
        Object roleIdObj = request.getAttribute("roleId");

        // 质检员只能看到分配给自己的任务
        if (roleIdObj != null && "2".equals(String.valueOf(roleIdObj))) {
            Object userIdObj = request.getAttribute("userId");
            if (userIdObj != null) {
                Long inspectorId = Long.valueOf(String.valueOf(userIdObj));
                List<BizSampleTask> inspectorTasks = taskMapper.selectByInspectorId(inspectorId);
                return Result.success(inspectorTasks);
            }
        }

        return Result.success(service.getAll());
    }
}
