package com.young.controller;

import com.young.pojo.BizDelegation;
import com.young.service.BizDelegationService;
import com.young.common.Result;
import com.young.annotation.RequireRole;
import com.young.pojo.enums.UserRole;
import com.young.pojo.SysClient;
import com.young.mapper.SysClientMapper;
import com.young.mapper.BizDelegationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 检验委托接口
 */
@Tag(name = "检验委托")
@RestController
@RequestMapping("/api/biz-delegation")
public class BizDelegationController {

    @Autowired
    private BizDelegationService service;

    @Autowired
    private BizDelegationMapper bizDelegationMapper;

    @Autowired
    private SysClientMapper clientMapper;

    /**
     * 1. 客户提交委托单
     */
    @Operation(summary = "客户提交委托单")
    @RequireRole(UserRole.CLIENT)
    @PostMapping("/submit")
    public Result<String> submitDelegation(@RequestBody BizDelegation delegation, HttpServletRequest request) {
        try {
            Object clientIdObj = request.getAttribute("userId");
            if (clientIdObj != null) {
                delegation.setClientId(Long.valueOf(String.valueOf(clientIdObj)));
            }

            String delegationNo = service.submitDelegation(delegation);
            return Result.success(delegationNo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 2. 管理员收样，生成盲样任务
     */
    @Operation(summary = "管理员收样并分配质检员")
    @RequireRole(UserRole.ADMIN)
    @PostMapping("/receive")
    public Result<String> receiveSampleAndAssign(
            @RequestParam Long delegationId,
            @RequestParam Long inspectorId,
            HttpServletRequest request) {
        try {
            Long receiverId = 1L;
            Object userIdObj = request.getAttribute("userId");
            if (userIdObj != null) {
                receiverId = Long.valueOf(String.valueOf(userIdObj));
            }

            String blindCode = service.receiveSampleAndAssign(delegationId, inspectorId, receiverId);
            return Result.success(blindCode);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 基础 CRUD

    @Operation(summary = "新增委托单")
    @RequireRole(UserRole.ADMIN)
    @PostMapping
    public Result<Void> add(@RequestBody BizDelegation record) {
        service.add(record);
        return Result.success();
    }

    @Operation(summary = "修改委托单")
    @RequireRole(UserRole.ADMIN)
    @PutMapping
    public Result<Void> update(@RequestBody BizDelegation record) {
        service.update(record);
        return Result.success();
    }

    @Operation(summary = "删除委托单")
    @RequireRole(UserRole.ADMIN)
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.success();
    }

    @Operation(summary = "根据ID查询委托单")
    @GetMapping("/{id}")
    public Result<BizDelegation> getById(@PathVariable Long id, HttpServletRequest request) {
        BizDelegation delegation = service.getById(id);
        if (delegation == null) {
            return Result.error("委托单不存在");
        }

        // 客户只能查询自己的委托单
        Object roleIdObj = request.getAttribute("roleId");
        if (roleIdObj != null && "3".equals(String.valueOf(roleIdObj))) {
            Object userIdObj = request.getAttribute("userId");
            if (userIdObj == null || !delegation.getClientId().equals(Long.valueOf(String.valueOf(userIdObj)))) {
                return Result.error("无权限：您只能查看自己的委托单");
            }
        }

        return Result.success(delegation);
    }

    @Operation(summary = "查询委托单列表")
    @GetMapping
    public Result<List<BizDelegation>> getAll(
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        List<BizDelegation> delegations;
        Object roleIdObj = request.getAttribute("roleId");

        // 客户只能看到自己的委托单
        if (roleIdObj != null && "3".equals(String.valueOf(roleIdObj))) {
            Object clientIdObj = request.getAttribute("userId");
            if (clientIdObj == null) {
                Result<List<BizDelegation>> error = Result.error("未获取到客户身份信息");
                error.setCode(401);
                return error;
            }
            delegations = service.getByClientId(Long.valueOf(String.valueOf(clientIdObj)));
            // 如果指定了状态，在服务端过滤
            if (status != null) {
                delegations = delegations.stream()
                        .filter(d -> d.getStatus() != null && d.getStatus() == status)
                        .collect(Collectors.toList());
            }
        } else if (status != null) {
            // 管理员/检测员可以按状态过滤
            delegations = bizDelegationMapper.selectByStatus(status);
        } else {
            delegations = service.getAll();
        }

        // 批量填充客户名称（只查询涉及的客户，而非全表）
        if (delegations != null && !delegations.isEmpty()) {
            List<Long> clientIds = delegations.stream()
                    .map(BizDelegation::getClientId)
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            if (!clientIds.isEmpty()) {
                List<SysClient> clients = clientMapper.selectByIds(clientIds);
                Map<Long, String> clientMap = clients.stream().collect(
                        Collectors.toMap(SysClient::getId, SysClient::getCompanyName));
                for (BizDelegation d : delegations) {
                    if (d.getClientId() != null) {
                        d.setClientName(clientMap.get(d.getClientId()));
                    }
                }
            }
        }
        return Result.success(delegations);
    }
}
