package com.cmcc.ip.system.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cmcc.ip.system.entity.IpAddress;
import com.cmcc.ip.system.entity.IpBinding;
import com.cmcc.ip.system.mapper.IpBindingMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
public class IpBindingService {

    private final IpBindingMapper ipBindingMapper;
    private final IpAddressService ipAddressService;

    public IpBindingService(IpBindingMapper ipBindingMapper, IpAddressService ipAddressService) {
        this.ipBindingMapper = ipBindingMapper;
        this.ipAddressService = ipAddressService;
    }

    public Map<String, Object> queryList(Map<String, Object> params, Integer pageNum, Integer pageSize) {
        Page<IpBinding> page = new Page<>(pageNum, pageSize);
        QueryWrapper<IpBinding> wrapper = new QueryWrapper<>();

        if (params.get("ipAddress") != null && !params.get("ipAddress").toString().isEmpty()) {
            wrapper.eq("ip_address", params.get("ipAddress"));
        }
        if (params.get("deviceId") != null) {
            wrapper.eq("device_id", params.get("deviceId"));
        }
        if (params.get("bindingType") != null) {
            wrapper.eq("binding_type", params.get("bindingType"));
        }
        if (params.get("status") != null) {
            wrapper.eq("status", params.get("status"));
        }
        wrapper.orderByDesc("id");

        IPage<IpBinding> result = ipBindingMapper.selectPage(page, wrapper);

        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        data.put("pageNum", result.getCurrent());
        data.put("pageSize", result.getSize());
        return data;
    }

    public IpBinding getById(Long id) {
        return ipBindingMapper.selectById(id);
    }

    @Transactional
    public IpBinding createBinding(Long ipAddressId, Long deviceId, Integer bindingType) {
        IpAddress ip = ipAddressService.getById(ipAddressId);
        if (ip == null) {
            log.error("IP地址不存在: {}", ipAddressId);
            return null;
        }

        IpBinding binding = new IpBinding();
        binding.setIpAddressId(ipAddressId);
        binding.setIpAddress(ip.getIpAddress());
        binding.setDeviceId(deviceId);
        binding.setBindingType(bindingType != null ? bindingType : IpBinding.BindingType.STATIC.getValue());
        binding.setBindingTime(new Date());
        binding.setStatus(IpBinding.BindingStatus.BOUND.getValue());
        binding.setCreatedTime(new Date());

        ipBindingMapper.insert(binding);

        ip.setDeviceId(deviceId);
        ipAddressService.update(ip);

        return binding;
    }

    @Transactional
    public boolean unbind(Long bindingId) {
        IpBinding binding = ipBindingMapper.selectById(bindingId);
        if (binding == null) {
            return false;
        }

        binding.setStatus(IpBinding.BindingStatus.UNBOUND.getValue());
        binding.setUnbindingTime(new Date());
        ipBindingMapper.updateById(binding);

        IpAddress ip = ipAddressService.getById(binding.getIpAddressId());
        if (ip != null && deviceIdEquals(ip.getDeviceId(), binding.getDeviceId())) {
            ip.setDeviceId(null);
            ipAddressService.update(ip);
        }

        return true;
    }

    private boolean deviceIdEquals(Long a, Long b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
}
