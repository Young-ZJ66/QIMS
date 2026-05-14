package com.young.controller;

import com.young.pojo.StdStandard;
import com.young.service.StdStandardService;
import com.young.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Api(tags = "检验标准接口")
@RestController
@RequestMapping("/api/std-standard")
public class StdStandardController {

    @Autowired
    private StdStandardService service;

    @ApiOperation("新增")
    @PostMapping
    public Result<Void> add(@RequestBody StdStandard record) {
        service.add(record);
        return Result.success();
    }

    @ApiOperation("修改")
    @PutMapping
    public Result<Void> update(@RequestBody StdStandard record) {
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
    public Result<StdStandard> getById(@PathVariable Long id) {
        return Result.success(service.getById(id));
    }

    @ApiOperation("查询所有")
    @GetMapping
    public Result<List<StdStandard>> getAll() {
        return Result.success(service.getAll());
    }
}
