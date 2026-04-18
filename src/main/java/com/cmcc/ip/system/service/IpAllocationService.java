package com.cmcc.ip.system.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cmcc.ip.system.entity.IpAllocation;
import com.cmcc.ip.system.mapper.IpAllocationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class IpAllocationService {
    
    private final IpAllocationMapper ipAllocationMapper;
    
    public IpAllocationService(IpAllocationMapper ipAllocationMapper) {
        this.ipAllocationMapper = ipAllocationMapper;
    }
    
    public IPage<IpAllocation> queryList(IpAllocation query, Integer pageNum, Integer pageSize) {
        Page<IpAllocation> page = new Page<>(pageNum, pageSize);
        QueryWrapper<IpAllocation> wrapper = new QueryWrapper<>();
        if (query.getSubscriberId() != null && !query.getSubscriberId().isEmpty()) {
            wrapper.eq("subscriber_id", query.getSubscriberId());
        }
        if (query.getServiceOrderId() != null && !query.getServiceOrderId().isEmpty()) {
            wrapper.eq("service_order_id", query.getServiceOrderId());
        }
        if (query.getStatus() != null) {
            wrapper.eq("status", query.getStatus());
        }
        if (query.getIpAddress() != null && !query.getIpAddress().isEmpty()) {
            wrapper.eq("ip_address", query.getIpAddress());
        }
        wrapper.orderByDesc("allocation_time");
        return ipAllocationMapper.selectPage(page, wrapper);
    }
    
    public IpAllocation getById(Long id) {
        return ipAllocationMapper.selectById(id);
    }
    
    public List<IpAllocation> selectBySubscriberId(String subscriberId) {
        return ipAllocationMapper.selectBySubscriberId(subscriberId);
    }
    
    public List<IpAllocation> selectByServiceOrderId(String serviceOrderId) {
        return ipAllocationMapper.selectByServiceOrderId(serviceOrderId);
    }
    
    public List<IpAllocation> selectActiveBySubscriberId(String subscriberId) {
        return ipAllocationMapper.selectActiveBySubscriberId(subscriberId);
    }
    
    @Transactional
    public boolean save(IpAllocation ipAllocation) {
        ipAllocation.setAllocationTime(new Date());
        ipAllocation.setCreatedTime(new Date());
        return ipAllocationMapper.insert(ipAllocation) > 0;
    }
    
    @Transactional
    public boolean update(IpAllocation ipAllocation) {
        return ipAllocationMapper.updateById(ipAllocation) > 0;
    }
    
    @Transactional
    public boolean release(Long allocationId, Integer releaseType) {
        IpAllocation allocation = ipAllocationMapper.selectById(allocationId);
        if (allocation == null) {
            return false;
        }
        allocation.setStatus(2);
        allocation.setReleaseTime(new Date());
        allocation.setReleaseType(releaseType);
        return ipAllocationMapper.updateById(allocation) > 0;
    }
    
    @Transactional
    public boolean renew(Long allocationId, Integer extendDays) {
        IpAllocation allocation = ipAllocationMapper.selectById(allocationId);
        if (allocation == null) {
            return false;
        }
        Date newExpirationTime = new Date(allocation.getExpirationTime().getTime() + extendDays * 24L * 60 * 60 * 1000);
        allocation.setExpirationTime(newExpirationTime);
        return ipAllocationMapper.updateById(allocation) > 0;
    }
}