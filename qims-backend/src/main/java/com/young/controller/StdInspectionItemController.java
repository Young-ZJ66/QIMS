package com.young.controller;

import com.young.pojo.StdInspectionItem;
import com.young.service.StdInspectionItemService;
import com.young.mapper.StdInspectionItemMapper;
import com.young.common.Result;
import com.young.annotation.RequireRole;
import com.young.pojo.enums.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 检验项目接口
 */
@Tag(name = "检验项目")
@RestController
@RequestMapping("/api/std-inspection-item")
public class StdInspectionItemController {

    @Autowired
    private StdInspectionItemService service;

    @Autowired
    private StdInspectionItemMapper itemMapper;

    @Operation(summary = "新增检验项目")
    @RequireRole(UserRole.ADMIN)
    @PostMapping
    public Result<Void> add(@RequestBody StdInspectionItem record) {
        service.add(record);
        return Result.success();
    }

    @Operation(summary = "修改检验项目")
    @RequireRole(UserRole.ADMIN)
    @PutMapping
    public Result<Void> update(@RequestBody StdInspectionItem record) {
        service.update(record);
        return Result.success();
    }

    @Operation(summary = "删除检验项目")
    @RequireRole(UserRole.ADMIN)
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.success();
    }

    @Operation(summary = "根据ID查询检验项目")
    @GetMapping("/{id}")
    public Result<StdInspectionItem> getById(@PathVariable Long id) {
        return Result.success(service.getById(id));
    }

    @Operation(summary = "查询检验项目列表")
    @GetMapping
    public Result<List<StdInspectionItem>> getAll(@RequestParam(required = false) Long standardId) {
        // 使用条件查询
        if (standardId != null) {
            return Result.success(itemMapper.selectByStandardId(standardId));
        }
        return Result.success(service.getAll());
    }
}
