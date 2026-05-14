package com.young.controller;

import com.young.pojo.BizReport;
import com.young.service.BizReportService;
import com.young.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import com.young.mapper.BizDelegationMapper;
import com.young.pojo.BizDelegation;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Api(tags = "检验报告接口")
@RestController
@RequestMapping("/api/biz-report")
public class BizReportController {

    @Autowired
    private BizReportService service;

    @Autowired
    private BizDelegationMapper bizDelegationMapper;

    @ApiOperation("新增")
    @PostMapping
    public Result<Void> add(@RequestBody BizReport record, HttpServletRequest request) {
        Object roleIdObj = request.getAttribute("roleId");
        if (roleIdObj == null || !String.valueOf(roleIdObj).equals("1")) {
            return Result.error("仅管理员可签发报告");
        }
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj != null) {
            record.setReviewerId(Long.valueOf(String.valueOf(userIdObj)));
        }
        service.add(record);
        return Result.success();
    }

    @ApiOperation("修改")
    @PutMapping
    public Result<Void> update(@RequestBody BizReport record, HttpServletRequest request) {
        Object roleIdObj = request.getAttribute("roleId");
        if (roleIdObj == null || !String.valueOf(roleIdObj).equals("1")) {
            return Result.error("仅管理员可修改报告");
        }
        service.update(record);
        return Result.success();
    }

    @ApiOperation("删除")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Object roleIdObj = request.getAttribute("roleId");
        if (roleIdObj == null || !String.valueOf(roleIdObj).equals("1")) {
            return Result.error("仅管理员可删除报告");
        }
        service.delete(id);
        return Result.success();
    }

    @ApiOperation("根据ID查询")
    @GetMapping("/{id}")
    public Result<BizReport> getById(@PathVariable Long id) {
        return Result.success(service.getById(id));
    }

    @ApiOperation("查询所有")
    @GetMapping
    public Result<List<BizReport>> getAll(HttpServletRequest request) {
        Object roleIdObj = request.getAttribute("roleId");
        if (roleIdObj != null && String.valueOf(roleIdObj).equals("3")) {
            Object clientIdObj = request.getAttribute("userId");
            if (clientIdObj == null) {
                Result<List<BizReport>> error = Result.error("未获取到客户身份信息");
                error.setCode(401);
                return error;
            }
            List<BizDelegation> delegations = bizDelegationMapper.selectByClientId(Long.valueOf(String.valueOf(clientIdObj)));
            Set<Long> delegationIds = delegations.stream().map(BizDelegation::getId).collect(Collectors.toSet());
            List<BizReport> reports = service.getAll().stream()
                    .filter(r -> delegationIds.contains(r.getDelegationId()))
                    .collect(Collectors.toList());
            return Result.success(reports);
        }
        return Result.success(service.getAll());
    }
}
