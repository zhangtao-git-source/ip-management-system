package com.cmcc.ip.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cmcc.ip.system.common.Result;
import com.cmcc.ip.system.entity.Device;
import com.cmcc.ip.system.service.DeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/device")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping("/list")
    public Result<Map<String, Object>> list(
            @RequestParam(required = false) String deviceCode,
            @RequestParam(required = false) String deviceType,
            @RequestParam(required = false) String regionCode,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        Device query = new Device();
        query.setDeviceCode(deviceCode);
        query.setDeviceType(deviceType);
        query.setRegionCode(regionCode);
        query.setStatus(status);

        if (pageNum != null && pageSize != null) {
            Page<Device> page = new Page<>(pageNum, pageSize);
            QueryWrapper<Device> wrapper = new QueryWrapper<>();
            if (deviceCode != null && !deviceCode.isEmpty()) wrapper.eq("device_code", deviceCode);
            if (deviceType != null && !deviceType.isEmpty()) wrapper.eq("device_type", deviceType);
            if (regionCode != null && !regionCode.isEmpty()) wrapper.eq("region_code", regionCode);
            if (status != null) wrapper.eq("status", status);
            wrapper.orderByDesc("id");
            IPage<Device> result = deviceService.queryPage(page, wrapper);

            Map<String, Object> data = new HashMap<>();
            data.put("list", result.getRecords());
            data.put("total", result.getTotal());
            data.put("pageNum", result.getCurrent());
            data.put("pageSize", result.getSize());
            return Result.success(data);
        }

        java.util.List<Device> list = deviceService.queryList(query);
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", list.size());
        return Result.success(data);
    }

    @GetMapping("/{id}")
    public Result<Device> getById(@PathVariable Long id) {
        Device device = deviceService.getById(id);
        if (device == null) {
            return Result.error(404, "设备不存在");
        }
        return Result.success(device);
    }

    @PostMapping("/register")
    public Result<Device> register(@RequestBody Device device) {
        log.info("注册设备: {}", device);
        if (device.getDeviceCode() == null || device.getDeviceCode().isEmpty()) {
            return Result.error(400, "设备编码不能为空");
        }
        Device existing = deviceService.getByDeviceCode(device.getDeviceCode());
        if (existing != null) {
            return Result.error(400, "设备编码已存在");
        }
        if (device.getStatus() == null) {
            device.setStatus(Device.DeviceStatus.ONLINE.getValue());
        }
        boolean success = deviceService.save(device);
        return success ? Result.success(device) : Result.error(500, "注册失败");
    }

    @PutMapping("/update")
    public Result<String> update(@RequestBody Device device) {
        log.info("更新设备: {}", device);
        boolean success = deviceService.update(device);
        return success ? Result.success("更新成功") : Result.error(500, "更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        log.info("删除设备: id={}", id);
        Device device = deviceService.getById(id);
        if (device == null) {
            return Result.error(404, "设备不存在");
        }
        boolean success = deviceService.deleteById(id);
        return success ? Result.success("删除成功") : Result.error(500, "删除失败");
    }

    @GetMapping("/status/{deviceCode}")
    public Result<Map<String, Object>> getStatus(@PathVariable String deviceCode) {
        Device device = deviceService.getByDeviceCode(deviceCode);
        if (device == null) {
            return Result.error(404, "设备不存在");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("deviceCode", device.getDeviceCode());
        data.put("status", device.getStatus());
        data.put("lastCollectionTime", device.getLastCollectionTime());
        return Result.success(data);
    }
}
