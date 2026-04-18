package com.cmcc.ip.system.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cmcc.ip.system.entity.IpAddress;
import com.cmcc.ip.system.entity.IpAllocation;
import com.cmcc.ip.system.mapper.IpAddressMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class IpAddressService {
    
    private final IpAddressMapper ipAddressMapper;
    
    public IpAddressService(IpAddressMapper ipAddressMapper) {
        this.ipAddressMapper = ipAddressMapper;
    }
    
    public IPage<IpAddress> queryList(IpAddress query, Integer pageNum, Integer pageSize) {
        Page<IpAddress> page = new Page<>(pageNum, pageSize);
        QueryWrapper<IpAddress> wrapper = new QueryWrapper<>();
        if (query.getIpAddress() != null && !query.getIpAddress().isEmpty()) {
            wrapper.like("ip_address", query.getIpAddress());
        }
        if (query.getIpType() != null) {
            wrapper.eq("ip_type", query.getIpType());
        }
        if (query.getAddressPoolId() != null) {
            wrapper.eq("address_pool_id", query.getAddressPoolId());
        }
        if (query.getStatus() != null) {
            wrapper.eq("status", query.getStatus());
        }
        if (query.getRegionCode() != null && !query.getRegionCode().isEmpty()) {
            wrapper.eq("region_code", query.getRegionCode());
        }
        wrapper.orderByDesc("id");
        return ipAddressMapper.selectPage(page, wrapper);
    }
    
    public IpAddress getById(Long id) {
        return ipAddressMapper.selectById(id);
    }
    
    public IpAddress getByIpAddress(String ipAddress) {
        QueryWrapper<IpAddress> wrapper = new QueryWrapper<>();
        wrapper.eq("ip_address", ipAddress);
        return ipAddressMapper.selectOne(wrapper);
    }
    
    @Transactional
    public boolean save(IpAddress ipAddress) {
        ipAddress.setCreatedTime(new Date());
        ipAddress.setUpdatedTime(new Date());
        return ipAddressMapper.insert(ipAddress) > 0;
    }
    
    @Transactional
    public boolean update(IpAddress ipAddress) {
        ipAddress.setUpdatedTime(new Date());
        return ipAddressMapper.updateById(ipAddress) > 0;
    }
    
    public List<IpAddress> selectAvailableList(Integer ipType, String regionCode, 
                                              Long addressPoolId, Integer limitCount) {
        return ipAddressMapper.selectAvailableList(ipType, regionCode, addressPoolId, limitCount);
    }
    
    @Transactional
    public boolean updateStatusBatch(List<Long> ids, Integer status) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        return ipAddressMapper.updateStatusBatch(ids, status) > 0;
    }
}