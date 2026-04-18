package com.cmcc.ip.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cmcc.ip.system.common.Result;
import com.cmcc.ip.system.entity.IpAddress;
import com.cmcc.ip.system.service.IpAddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/ip")
public class IpAddressController {

    private final IpAddressService ipAddressService;

    public IpAddressController(IpAddressService ipAddressService) {
        this.ipAddressService = ipAddressService;
    }

    @GetMapping("/list")
    public Result<Map<String, Object>> list(
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

    @GetMapping("/{id}")
    public Result<IpAddress> getById(@PathVariable Long id) {
        IpAddress ip = ipAddressService.getById(id);
        if (ip == null) {
            return Result.error(404, "IP地址不存在");
        }
        return Result.success(ip);
    }

    @PostMapping("/add")
    public Result<IpAddress> add(@RequestBody IpAddress ip) {
        log.info("新增IP地址: {}", ip);
        if (ip.getIpAddress() == null || ip.getIpAddress().isEmpty()) {
            return Result.error(400, "IP地址不能为空");
        }
        IpAddress existing = ipAddressService.getByIpAddress(ip.getIpAddress());
        if (existing != null) {
            return Result.error(400, "IP地址已存在");
        }
        if (ip.getStatus() == null) {
            ip.setStatus(IpAddress.Status.UNASSIGNED.getValue());
        }
        boolean success = ipAddressService.save(ip);
        return success ? Result.success(ip) : Result.error(500, "新增失败");
    }

    @PutMapping("/update")
    public Result<String> update(@RequestBody IpAddress ip) {
        log.info("更新IP地址: {}", ip);
        boolean success = ipAddressService.update(ip);
        return success ? Result.success("更新成功") : Result.error(500, "更新失败");
    }

    @PutMapping("/{id}/freeze")
    public Result<String> freeze(@PathVariable Long id) {
        IpAddress ip = ipAddressService.getById(id);
        if (ip == null) {
            return Result.error(404, "IP地址不存在");
        }
        ip.setStatus(IpAddress.Status.FROZEN.getValue());
        ipAddressService.update(ip);
        return Result.success("冻结成功");
    }

    @PutMapping("/{id}/unfreeze")
    public Result<String> unfreeze(@PathVariable Long id) {
        IpAddress ip = ipAddressService.getById(id);
        if (ip == null) {
            return Result.error(404, "IP地址不存在");
        }
        ip.setStatus(IpAddress.Status.UNASSIGNED.getValue());
        ipAddressService.update(ip);
        return Result.success("解冻成功");
    }

    @PutMapping("/batch-freeze")
    public Result<String> batchFreeze(@RequestBody List<Long> ids) {
        ipAddressService.updateStatusBatch(ids, IpAddress.Status.FROZEN.getValue());
        return Result.success("批量冻结成功");
    }

    @PutMapping("/batch-unfreeze")
    public Result<String> batchUnfreeze(@RequestBody List<Long> ids) {
        ipAddressService.updateStatusBatch(ids, IpAddress.Status.UNASSIGNED.getValue());
        return Result.success("批量解冻成功");
    }
}
