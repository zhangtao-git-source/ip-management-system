package com.cmcc.ip.system.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cmcc.ip.system.entity.Device;
import com.cmcc.ip.system.mapper.DeviceMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class DeviceService {
    
    private final DeviceMapper deviceMapper;
    
    public DeviceService(DeviceMapper deviceMapper) {
        this.deviceMapper = deviceMapper;
    }
    
    public List<Device> queryList(Device query) {
        QueryWrapper<Device> wrapper = new QueryWrapper<>();
        if (query.getDeviceCode() != null && !query.getDeviceCode().isEmpty()) {
            wrapper.eq("device_code", query.getDeviceCode());
        }
        if (query.getDeviceType() != null && !query.getDeviceType().isEmpty()) {
            wrapper.eq("device_type", query.getDeviceType());
        }
        if (query.getRegionCode() != null && !query.getRegionCode().isEmpty()) {
            wrapper.eq("region_code", query.getRegionCode());
        }
        if (query.getStatus() != null) {
            wrapper.eq("status", query.getStatus());
        }
        wrapper.orderByDesc("id");
        return deviceMapper.selectList(wrapper);
    }
    
    public Device getById(Long id) {
        return deviceMapper.selectById(id);
    }
    
    public Device getByDeviceCode(String deviceCode) {
        QueryWrapper<Device> wrapper = new QueryWrapper<>();
        wrapper.eq("device_code", deviceCode);
        return deviceMapper.selectOne(wrapper);
    }
    
    @Transactional
    public boolean save(Device device) {
        device.setCreatedTime(new Date());
        device.setUpdatedTime(new Date());
        return deviceMapper.insert(device) > 0;
    }
    
    @Transactional
    public boolean update(Device device) {
        device.setUpdatedTime(new Date());
        return deviceMapper.updateById(device) > 0;
    }
    
    public IPage<Device> queryPage(IPage<Device> page, QueryWrapper<Device> wrapper) {
        return deviceMapper.selectPage(page, wrapper);
    }

    @Transactional
    public boolean deleteById(Long id) {
        return deviceMapper.deleteById(id) > 0;
    }

    @Transactional
    public boolean updateCollectionTime(String deviceCode) {
        Device device = getByDeviceCode(deviceCode);
        if (device == null) {
            return false;
        }
        device.setLastCollectionTime(new Date());
        return deviceMapper.updateById(device) > 0;
    }
}