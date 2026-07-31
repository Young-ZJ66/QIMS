package com.young.controller;

import com.young.pojo.BizReport;
import com.young.service.BizReportService;
import com.young.common.Result;
import com.young.annotation.RequireRole;
import com.young.pojo.enums.UserRole;
import com.young.mapper.BizDelegationMapper;
import com.young.pojo.BizDelegation;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 检验报告接口
 */
@Tag(name = "检验报告")
@RestController
@RequestMapping("/api/biz-report")
public class BizReportController {

    @Autowired
    private BizReportService service;

    @Autowired
    private BizDelegationMapper bizDelegationMapper;

    @Operation(summary = "新增报告")
    @RequireRole(UserRole.ADMIN)
    @PostMapping
    public Result<Void> add(@RequestBody BizReport record, HttpServletRequest request) {
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj != null) {
            record.setReviewerId(Long.valueOf(String.valueOf(userIdObj)));
        }
        service.add(record);
        return Result.success();
    }

    @Operation(summary = "修改报告")
    @RequireRole(UserRole.ADMIN)
    @PutMapping
    public Result<Void> update(@RequestBody BizReport record) {
        service.update(record);
        return Result.success();
    }

    @Operation(summary = "删除报告")
    @RequireRole(UserRole.ADMIN)
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.success();
    }

    @Operation(summary = "根据ID查询报告")
    @GetMapping("/{id}")
    public Result<BizReport> getById(@PathVariable Long id) {
        return Result.success(service.getById(id));
    }

    @Operation(summary = "查询报告列表")
    @GetMapping
    public Result<List<BizReport>> getAll(HttpServletRequest request) {
        Object roleIdObj = request.getAttribute("roleId");

        // 客户只能看到自己委托单对应的报告
        if (roleIdObj != null && "3".equals(String.valueOf(roleIdObj))) {
            Object clientIdObj = request.getAttribute("userId");
            if (clientIdObj == null) {
                Result<List<BizReport>> error = Result.error("未获取到客户身份信息");
                error.setCode(401);
                return error;
            }
            List<BizDelegation> delegations = bizDelegationMapper.selectByClientId(Long.valueOf(String.valueOf(clientIdObj)));
            if (delegations.isEmpty()) {
                return Result.success(Collections.emptyList());
            }
            Set<Long> delegationIds = delegations.stream().map(BizDelegation::getId).collect(Collectors.toSet());
            List<BizReport> reports = service.getAll().stream()
                    .filter(r -> delegationIds.contains(r.getDelegationId()))
                    .collect(Collectors.toList());
            return Result.success(reports);
        }
        return Result.success(service.getAll());
    }
}
