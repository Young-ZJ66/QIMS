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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Api(tags = "盲样任务接口")
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
    @ApiOperation("根据任务ID获取检测项目")
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
        
        List<StdInspectionItem> allItems = itemMapper.selectAll();
        List<StdInspectionItem> targetItems = allItems.stream()
                .filter(item -> item.getStandardId().equals(delegation.getStandardId()))
                .collect(Collectors.toList());
                
        return Result.success(targetItems);
    }

    @ApiOperation("新增")
    @PostMapping
    public Result<Void> add(@RequestBody BizSampleTask record) {
        service.add(record);
        return Result.success();
    }

    @ApiOperation("修改")
    @PutMapping
    public Result<Void> update(@RequestBody BizSampleTask record) {
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
    public Result<BizSampleTask> getById(@PathVariable Long id) {
        return Result.success(service.getById(id));
    }

    @ApiOperation("查询所有")
    @GetMapping
    public Result<List<BizSampleTask>> getAll(jakarta.servlet.http.HttpServletRequest request) {
        List<BizSampleTask> all = service.getAll();
        
        Object roleIdObj = request.getAttribute("roleId");
        
        // 如果是质检员，只能看到分配给自己的任务
        if (roleIdObj != null && String.valueOf(roleIdObj).equals("2")) {
            Object userIdObj = request.getAttribute("userId");
            if (userIdObj != null) {
                Long inspectorId = Long.valueOf(String.valueOf(userIdObj));
                List<BizSampleTask> inspectorTasks = all.stream()
                        .filter(t -> inspectorId.equals(t.getInspectorId()))
                        .collect(Collectors.toList());
                return Result.success(inspectorTasks);
            }
        }
        
        return Result.success(all);
    }
}
