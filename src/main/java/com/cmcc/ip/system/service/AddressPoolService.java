package com.cmcc.ip.system.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cmcc.ip.system.entity.AddressPool;
import com.cmcc.ip.system.entity.IpAddress;
import com.cmcc.ip.system.mapper.AddressPoolMapper;
import com.cmcc.ip.system.mapper.IpAddressMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AddressPoolService {
    
    private final AddressPoolMapper addressPoolMapper;
    private final IpAddressMapper ipAddressMapper;

    public AddressPoolService(AddressPoolMapper addressPoolMapper, IpAddressMapper ipAddressMapper) {
        this.addressPoolMapper = addressPoolMapper;
        this.ipAddressMapper = ipAddressMapper;
    }
    
    public IPage<AddressPool> queryList(AddressPool query, Integer pageNum, Integer pageSize) {
        Page<AddressPool> page = new Page<>(pageNum, pageSize);
        QueryWrapper<AddressPool> wrapper = new QueryWrapper<>();
        if (query.getPoolCode() != null && !query.getPoolCode().isEmpty()) {
            wrapper.eq("pool_code", query.getPoolCode());
        }
        if (query.getIpType() != null) {
            wrapper.eq("ip_type", query.getIpType());
        }
        if (query.getRegionCode() != null && !query.getRegionCode().isEmpty()) {
            wrapper.eq("region_code", query.getRegionCode());
        }
        if (query.getStatus() != null) {
            wrapper.eq("status", query.getStatus());
        }
        wrapper.orderByDesc("id");
        return addressPoolMapper.selectPage(page, wrapper);
    }
    
    public AddressPool getById(Long id) {
        return addressPoolMapper.selectById(id);
    }
    
    public AddressPool getByPoolCode(String poolCode) {
        QueryWrapper<AddressPool> wrapper = new QueryWrapper<>();
        wrapper.eq("pool_code", poolCode);
        return addressPoolMapper.selectOne(wrapper);
    }
    
    public List<AddressPool> selectByPriority(Integer ipType, String regionCode, Integer status) {
        return addressPoolMapper.selectByPriority(ipType, regionCode, status);
    }
    
    @Transactional
    public boolean save(AddressPool addressPool) {
        addressPool.setCreatedTime(new Date());
        addressPool.setUpdatedTime(new Date());
        if (addressPool.getTotalCount() == null) {
            addressPool.setTotalCount(0);
        }
        if (addressPool.getAvailableCount() == null) {
            addressPool.setTotalCount(addressPool.getTotalCount());
            addressPool.setAvailableCount(addressPool.getTotalCount());
        }
        return addressPoolMapper.insert(addressPool) > 0;
    }
    
    @Transactional
    public boolean update(AddressPool addressPool) {
        addressPool.setUpdatedTime(new Date());
        return addressPoolMapper.updateById(addressPool) > 0;
    }
    
    @Transactional
    public boolean updateAvailableCount(Long id, Integer count) {
        return addressPoolMapper.updateAvailableCount(id, count) > 0;
    }

    @Transactional
    public void generateIpRecords(AddressPool pool) {
        log.info("为地址池生成IP记录: {}", pool.getPoolCode());
        try {
            String startIp = pool.getStartAddress();
            String endIp = pool.getEndAddress();
            if (startIp == null || endIp == null) return;

            String[] startParts = startIp.split("\\.");
            String[] endParts = endIp.split("\\.");
            if (startParts.length != 4 || endParts.length != 4) return;

            int startLast = Integer.parseInt(startParts[3]);
            int endLast = Integer.parseInt(endParts[3]);
            String prefix = startParts[0] + "." + startParts[1] + "." + startParts[2] + ".";

            int count = 0;
            for (int i = startLast; i <= endLast; i++) {
                String ipStr = prefix + i;
                IpAddress ip = new IpAddress();
                ip.setIpAddress(ipStr);
                ip.setIpType(pool.getIpType() != null ? pool.getIpType() : 1);
                ip.setSubnetMask("255.255.255.0");
                ip.setGateway(prefix + "1");
                ip.setAddressPoolId(pool.getId());
                ip.setStatus(IpAddress.Status.UNASSIGNED.getValue());
                ip.setRegionCode(pool.getRegionCode());
                ip.setCreatedTime(new Date());
                ip.setUpdatedTime(new Date());
                ipAddressMapper.insert(ip);
                count++;
            }
            pool.setTotalCount(count);
            pool.setAvailableCount(count);
            addressPoolMapper.updateById(pool);
            log.info("地址池 {} 生成 {} 条IP记录", pool.getPoolCode(), count);
        } catch (Exception e) {
            log.error("生成IP记录失败", e);
        }
    }

    public Map<String, Object> getAuditOverview() {
        Map<String, Object> data = new HashMap<>();
        data.put("poolCount", addressPoolMapper.selectCount(null));
        return data;
    }
}