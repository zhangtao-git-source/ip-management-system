package com.cmcc.ip.system.controller;

import com.cmcc.ip.system.common.Result;
import com.cmcc.ip.system.entity.IpBinding;
import com.cmcc.ip.system.service.IpBindingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/binding")
public class IpBindingController {

    private final IpBindingService ipBindingService;

    public IpBindingController(IpBindingService ipBindingService) {
        this.ipBindingService = ipBindingService;
    }

    @GetMapping("/list")
    public Result<Map<String, Object>> list(
            @RequestParam(required = false) String ipAddress,
            @RequestParam(required = false) Long deviceId,
            @RequestParam(required = false) Integer bindingType,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        Map<String, Object> params = new HashMap<>();
        params.put("ipAddress", ipAddress);
        params.put("deviceId", deviceId);
        params.put("bindingType", bindingType);
        params.put("status", status);

        Map<String, Object> data = ipBindingService.queryList(params, pageNum, pageSize);
        return Result.success(data);
    }

    @PostMapping("/create")
    public Result<IpBinding> create(@RequestBody Map<String, Object> request) {
        log.info("创建绑定: {}", request);
        Long ipAddressId = request.get("ipAddressId") != null ? Long.valueOf(request.get("ipAddressId").toString()) : null;
        Long deviceId = request.get("deviceId") != null ? Long.valueOf(request.get("deviceId").toString()) : null;
        Integer bindingType = request.get("bindingType") != null ? Integer.valueOf(request.get("bindingType").toString()) : 1;

        if (ipAddressId == null || deviceId == null) {
            return Result.error(400, "IP地址ID和设备ID不能为空");
        }

        IpBinding binding = ipBindingService.createBinding(ipAddressId, deviceId, bindingType);
        if (binding != null) {
            return Result.success(binding);
        }
        return Result.error(500, "绑定失败");
    }

    @PostMapping("/unbind")
    public Result<String> unbind(@RequestParam Long bindingId) {
        log.info("解绑请求: bindingId={}", bindingId);
        boolean success = ipBindingService.unbind(bindingId);
        return success ? Result.success("解绑成功") : Result.error(500, "解绑失败");
    }
}
