package com.cmcc.ip.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cmcc.ip.system.common.Result;
import com.cmcc.ip.system.entity.AddressPool;
import com.cmcc.ip.system.entity.IpAddress;
import com.cmcc.ip.system.entity.IpAllocation;
import com.cmcc.ip.system.service.AddressPoolService;
import com.cmcc.ip.system.service.IpAddressService;
import com.cmcc.ip.system.service.IpAllocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/resource")
public class ResourceController {
    
    private final IpAddressService ipAddressService;
    private final AddressPoolService addressPoolService;
    private final IpAllocationService ipAllocationService;
    
    public ResourceController(IpAddressService ipAddressService,
                           AddressPoolService addressPoolService,
                           IpAllocationService ipAllocationService) {
        this.ipAddressService = ipAddressService;
        this.addressPoolService = addressPoolService;
        this.ipAllocationService = ipAllocationService;
    }
    
    @GetMapping("/ip-address")
    public Result<Map<String, Object>> queryIpAddress(
            @RequestParam(required = false) String ipAddress,
            @RequestParam(required = false) Integer ipType,
            @RequestParam(required = false) Long addressPoolId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String regionCode,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        
        IpAddress query = new IpAddress();
        query.setIpAddress(ipAddress);
        query.setIpType(ipType);
        query.setAddressPoolId(addressPoolId);
        query.setStatus(status);
        query.setRegionCode(regionCode);
        
        IPage<IpAddress> page = ipAddressService.queryList(query, pageNum, pageSize);
        
        Map<String, Object> data = new HashMap<>();
        data.put("list", page.getRecords());
        data.put("total", page.getTotal());
        data.put("pageNum", page.getCurrent());
        data.put("pageSize", page.getSize());
        
        return Result.success(data);
    }
    
    @PostMapping("/ip-address/batch")
    public Result<Map<String, Object>> batchQueryIpAddress(@RequestBody java.util.List<String> ipAddresses) {
        java.util.List<IpAddress> list = new java.util.ArrayList<>();
        for (String ip : ipAddresses) {
            IpAddress address = ipAddressService.getByIpAddress(ip);
            if (address != null) {
                list.add(address);
            }
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        return Result.success(data);
    }
    
    @GetMapping("/address-pool")
    public Result<Map<String, Object>> queryAddressPool(
            @RequestParam(required = false) String poolCode,
            @RequestParam(required = false) Integer ipType,
            @RequestParam(required = false) String regionCode,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        
        AddressPool query = new AddressPool();
        query.setPoolCode(poolCode);
        query.setIpType(ipType);
        query.setRegionCode(regionCode);
        query.setStatus(status);
        
        IPage<AddressPool> page = addressPoolService.queryList(query, pageNum, pageSize);
        
        Map<String, Object> data = new HashMap<>();
        data.put("list", page.getRecords());
        data.put("total", page.getTotal());
        
        return Result.success(data);
    }
}