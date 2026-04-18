package com.cmcc.ip.system.controller;

import com.cmcc.ip.system.common.Result;
import com.cmcc.ip.system.entity.Device;
import com.cmcc.ip.system.entity.IpAddress;
import com.cmcc.ip.system.service.DeviceService;
import com.cmcc.ip.system.service.IpAddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/collection")
public class CollectionController {
    
    private final IpAddressService ipAddressService;
    private final DeviceService deviceService;
    
    public CollectionController(IpAddressService ipAddressService,
                                  DeviceService deviceService) {
        this.ipAddressService = ipAddressService;
        this.deviceService = deviceService;
    }
    
    @PostMapping("/receive")
    @Transactional
    public Result<Map<String, Object>> receive(@RequestBody Map<String, Object> request) {
        log.info("接收采集数据请求: {}", request);
        
        String deviceCode = (String) request.get("deviceCode");
        List<Map<String, Object>> ipList = (List<Map<String, Object>>) request.get("ipList");
        
        if (deviceCode == null || deviceCode.isEmpty()) {
            return Result.error(400, "设备编码不能为空");
        }
        if (ipList == null || ipList.isEmpty()) {
            return Result.error(400, "IP地址列表不能为空");
        }
        
        Device device = deviceService.getByDeviceCode(deviceCode);
        if (device == null) {
            return Result.error(400, "设备不存在");
        }
        
        int receivedCount = 0;
        List<Map<String, Object>> failList = new ArrayList<>();
        
        for (Map<String, Object> ipData : ipList) {
            String ipAddress = (String) ipData.get("ipAddress");
            try {
                IpAddress ip = ipAddressService.getByIpAddress(ipAddress);
                if (ip != null) {
                    Integer status = (Integer) ipData.get("status");
                    if (status != null) {
                        ip.setStatus(status);
                    }
                    ip.setDeviceId(device.getId());
                    ipAddressService.update(ip);
                    receivedCount++;
                } else {
                    IpAddress newIp = new IpAddress();
                    newIp.setIpAddress(ipAddress);
                    newIp.setIpType((Integer) ipData.get("ipType"));
                    newIp.setSubnetMask((String) ipData.get("subnetMask"));
                    newIp.setGateway((String) ipData.get("gateway"));
                    newIp.setRegionCode((String) ipData.get("regionCode"));
                    newIp.setStatus((Integer) ipData.get("status"));
                    newIp.setDeviceId(device.getId());
                    ipAddressService.save(newIp);
                    receivedCount++;
                }
            } catch (Exception e) {
                Map<String, Object> failItem = new HashMap<>();
                failItem.put("ipAddress", ipAddress);
                failItem.put("reason", e.getMessage());
                failList.add(failItem);
            }
        }
        
        deviceService.updateCollectionTime(deviceCode);
        
        Map<String, Object> data = new HashMap<>();
        data.put("receivedCount", receivedCount);
        data.put("failList", failList);
        
        return Result.success(data);
    }
    
    @PostMapping("/device/register")
    @Transactional
    public Result<Device> registerDevice(@RequestBody Device device) {
        log.info("注册设备请求: {}", device);
        
        if (device.getDeviceCode() == null || device.getDeviceCode().isEmpty()) {
            return Result.error(400, "设备编码不能为空");
        }
        
        Device existingDevice = deviceService.getByDeviceCode(device.getDeviceCode());
        if (existingDevice != null) {
            return Result.error(400, "设备已存在");
        }
        
        device.setStatus(Device.DeviceStatus.ONLINE.getValue());
        boolean success = deviceService.save(device);
        if (success) {
            return Result.success(device);
        }
        return Result.error(500, "注册失败");
    }
    
    @GetMapping("/status/{deviceCode}")
    public Result<Map<String, Object>> getCollectionStatus(@PathVariable String deviceCode) {
        Device device = deviceService.getByDeviceCode(deviceCode);
        if (device == null) {
            return Result.error(404, "设备不存在");
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("deviceCode", device.getDeviceCode());
        data.put("lastCollectionTime", device.getLastCollectionTime());
        data.put("collectionStatus", device.getStatus());
        
        return Result.success(data);
    }
}