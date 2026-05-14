package com.young.controller;

import com.young.pojo.BizDelegation;
import com.young.service.BizDelegationService;
import com.young.common.Result;
import com.young.pojo.SysClient;
import com.young.mapper.SysClientMapper;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Api(tags = "检验委托接口")
@RestController
@RequestMapping("/api/biz-delegation")
public class BizDelegationController {

    @Autowired
    private BizDelegationService service;

    @Autowired
    private SysClientMapper clientMapper;

    /**
     * 1. 客户提交委托单
     */
    @ApiOperation("提交委托单")
    @PostMapping("/submit")
    public Result<String> submitDelegation(@RequestBody BizDelegation delegation, HttpServletRequest request) {
        try {
            Object roleIdObj = request.getAttribute("roleId");
            if (roleIdObj == null || !String.valueOf(roleIdObj).equals("3")) {
                return Result.error("无权限：只有送检客户可以提交委托单");
            }
            
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
    @ApiOperation("收样并分发任务")
    @PostMapping("/receive")
    public Result<String> receiveSampleAndAssign(
            @RequestParam Long delegationId, 
            @RequestParam Long inspectorId, 
            HttpServletRequest request) {
        try {
            Object roleIdObj = request.getAttribute("roleId");
            if (roleIdObj == null || !String.valueOf(roleIdObj).equals("1")) {
                return Result.error("无权限：只有管理员可以收样并分发");
            }
            
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

    @ApiOperation("新增")
    @PostMapping
    public Result<Void> add(@RequestBody BizDelegation record) {
        service.add(record);
        return Result.success();
    }

    @ApiOperation("修改")
    @PutMapping
    public Result<Void> update(@RequestBody BizDelegation record) {
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
    public Result<BizDelegation> getById(@PathVariable Long id) {
        return Result.success(service.getById(id));
    }

    @ApiOperation("查询所有")
    @GetMapping
    public Result<List<BizDelegation>> getAll(HttpServletRequest request) {
        List<BizDelegation> delegations;
        Object roleIdObj = request.getAttribute("roleId");
        if (roleIdObj != null && "3".equals(String.valueOf(roleIdObj))) {
            Object clientIdObj = request.getAttribute("userId");
            if (clientIdObj == null) {
                Result<List<BizDelegation>> error = Result.error("未获取到客户身份信息");
                error.setCode(401);
                return error;
            }
            delegations = service.getByClientId(Long.valueOf(String.valueOf(clientIdObj)));
        } else {
            delegations = service.getAll();
        }

        // 填充客户名称
        if (delegations != null && !delegations.isEmpty()) {
            List<SysClient> clients = clientMapper.selectAll();
            Map<Long, String> clientMap = clients.stream().collect(
                    Collectors.toMap(SysClient::getId, SysClient::getCompanyName));
            for (BizDelegation d : delegations) {
                if (d.getClientId() != null) {
                    d.setClientName(clientMap.get(d.getClientId()));
                }
            }
        }
        return Result.success(delegations);
    }
}
