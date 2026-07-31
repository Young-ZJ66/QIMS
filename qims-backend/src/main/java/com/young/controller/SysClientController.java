package com.young.controller;

import com.young.pojo.SysClient;
import com.young.service.SysClientService;
import com.young.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 客户管理接口
 */
@Tag(name = "客户管理")
@RestController
@RequestMapping("/api/sys-client")
public class SysClientController {

    @Autowired
    private SysClientService service;

    @Operation(summary = "新增客户")
    @PostMapping
    public Result<Void> add(@RequestBody SysClient record) {
        service.add(record);
        return Result.success();
    }

    @Operation(summary = "修改客户信息")
    @PutMapping
    public Result<Void> update(@RequestBody SysClient record) {
        service.update(record);
        return Result.success();
    }

    @Operation(summary = "删除客户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.success();
    }

    @Operation(summary = "根据ID查询客户")
    @GetMapping("/{id}")
    public Result<SysClient> getById(@PathVariable Long id) {
        return Result.success(service.getById(id));
    }

    @Operation(summary = "查询所有客户")
    @GetMapping
    public Result<List<SysClient>> getAll() {
        return Result.success(service.getAll());
    }
}
