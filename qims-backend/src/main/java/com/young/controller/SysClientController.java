package com.young.controller;

import com.young.pojo.SysClient;
import com.young.service.SysClientService;
import com.young.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Api(tags = "客户管理接口")
@RestController
@RequestMapping("/api/sys-client")
public class SysClientController {

    @Autowired
    private SysClientService service;

    @ApiOperation("新增")
    @PostMapping
    public Result<Void> add(@RequestBody SysClient record) {
        service.add(record);
        return Result.success();
    }

    @ApiOperation("修改")
    @PutMapping
    public Result<Void> update(@RequestBody SysClient record) {
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
    public Result<SysClient> getById(@PathVariable Long id) {
        return Result.success(service.getById(id));
    }

    @ApiOperation("查询所有")
    @GetMapping
    public Result<List<SysClient>> getAll() {
        return Result.success(service.getAll());
    }
}
