package com.young.controller;

import com.young.pojo.StdInspectionItem;
import com.young.service.StdInspectionItemService;
import com.young.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Api(tags = "检验项目接口")
@RestController
@RequestMapping("/api/std-inspection-item")
public class StdInspectionItemController {

    @Autowired
    private StdInspectionItemService service;

    @ApiOperation("新增")
    @PostMapping
    public Result<Void> add(@RequestBody StdInspectionItem record) {
        service.add(record);
        return Result.success();
    }

    @ApiOperation("修改")
    @PutMapping
    public Result<Void> update(@RequestBody StdInspectionItem record) {
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
    public Result<StdInspectionItem> getById(@PathVariable Long id) {
        return Result.success(service.getById(id));
    }

    @ApiOperation("查询所有")
    @GetMapping
    public Result<List<StdInspectionItem>> getAll(@RequestParam(required = false) Long standardId) {
        List<StdInspectionItem> all = service.getAll();
        if (standardId != null) {
            all = all.stream()
                    .filter(item -> standardId.equals(item.getStandardId()))
                    .collect(java.util.stream.Collectors.toList());
        }
        return Result.success(all);
    }
}
