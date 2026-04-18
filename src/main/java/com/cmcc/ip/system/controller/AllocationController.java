package com.cmcc.ip.system.controller;

import com.cmcc.ip.system.common.AllocationRequest;
import com.cmcc.ip.system.common.AllocationResult;
import com.cmcc.ip.system.common.Result;
import com.cmcc.ip.system.service.IpAllocationManageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/allocation")
public class AllocationController {
    
    private final IpAllocationManageService ipAllocationManageService;
    
    public AllocationController(IpAllocationManageService ipAllocationManageService) {
        this.ipAllocationManageService = ipAllocationManageService;
    }
    
    @PostMapping("/allocate")
    public Result<AllocationResult> allocate(@RequestBody AllocationRequest request) {
        log.info("IP地址分配请求: {}", request);
        
        if (request.getRequireCount() == null || request.getRequireCount() <= 0) {
            return Result.error(400, "分配数量必须大于0");
        }
        if (request.getIpType() == null) {
            return Result.error(400, "地址类型不能为空");
        }
        
        AllocationResult result = ipAllocationManageService.allocate(request);
        return Result.success(result);
    }
    
    @PostMapping("/release")
    public Result<AllocationResult> release(@RequestParam Long allocationId, 
                                            @RequestParam(required = false) Integer releaseType) {
        log.info("IP地址释放请求: allocationId={}, releaseType={}", allocationId, releaseType);
        
        if (allocationId == null) {
            return Result.error(400, "分配记录ID不能为空");
        }
        
        AllocationResult result = ipAllocationManageService.release(allocationId, releaseType);
        return Result.success(result);
    }
    
    @PostMapping("/renew")
    public Result<AllocationResult> renew(@RequestParam Long allocationId, 
                                        @RequestParam Integer extendDays) {
        log.info("IP地址续约请求: allocationId={}, extendDays={}", allocationId, extendDays);
        
        if (allocationId == null) {
            return Result.error(400, "分配记录ID不能为空");
        }
        if (extendDays == null || extendDays <= 0) {
            return Result.error(400, "续约天数必须大于0");
        }
        
        AllocationResult result = ipAllocationManageService.renew(allocationId, extendDays);
        return Result.success(result);
    }
}