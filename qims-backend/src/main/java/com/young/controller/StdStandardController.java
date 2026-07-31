package com.young.controller;

import com.young.pojo.StdStandard;
import com.young.service.StdStandardService;
import com.young.common.Result;
import com.young.annotation.RequireRole;
import com.young.pojo.enums.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 检验标准接口
 */
@Tag(name = "检验标准")
@RestController
@RequestMapping("/api/std-standard")
public class StdStandardController {

    @Autowired
    private StdStandardService service;

    @Operation(summary = "新增检验标准")
    @RequireRole(UserRole.ADMIN)
    @PostMapping
    public Result<Void> add(@RequestBody StdStandard record) {
        service.add(record);
        return Result.success();
    }

    @Operation(summary = "修改检验标准")
    @RequireRole(UserRole.ADMIN)
    @PutMapping
    public Result<Void> update(@RequestBody StdStandard record) {
        service.update(record);
        return Result.success();
    }

    @Operation(summary = "删除检验标准")
    @RequireRole(UserRole.ADMIN)
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.success();
    }

    @Operation(summary = "根据ID查询检验标准")
    @GetMapping("/{id}")
    public Result<StdStandard> getById(@PathVariable Long id) {
        return Result.success(service.getById(id));
    }

    @Operation(summary = "查询所有检验标准")
    @GetMapping
    public Result<List<StdStandard>> getAll() {
        return Result.success(service.getAll());
    }
}
