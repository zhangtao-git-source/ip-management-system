package com.cmcc.ip.system.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cmcc.ip.system.entity.AddressPool;
import com.cmcc.ip.system.entity.IpAddress;
import com.cmcc.ip.system.entity.IpAllocation;
import com.cmcc.ip.system.mapper.AddressPoolMapper;
import com.cmcc.ip.system.mapper.IpAddressMapper;
import com.cmcc.ip.system.mapper.IpAllocationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class DashboardService {

    private final IpAddressMapper ipAddressMapper;
    private final AddressPoolMapper addressPoolMapper;
    private final IpAllocationMapper ipAllocationMapper;

    public DashboardService(IpAddressMapper ipAddressMapper,
                            AddressPoolMapper addressPoolMapper,
                            IpAllocationMapper ipAllocationMapper) {
        this.ipAddressMapper = ipAddressMapper;
        this.addressPoolMapper = addressPoolMapper;
        this.ipAllocationMapper = ipAllocationMapper;
    }

    public Map<String, Object> getOverview() {
        Map<String, Object> data = new HashMap<>();

        Long totalCount = ipAddressMapper.selectCount(null);
        QueryWrapper<IpAddress> assignedWrapper = new QueryWrapper<>();
        assignedWrapper.eq("status", IpAddress.Status.ASSIGNED.getValue());
        Long assignedCount = ipAddressMapper.selectCount(assignedWrapper);

        QueryWrapper<IpAddress> availableWrapper = new QueryWrapper<>();
        availableWrapper.eq("status", IpAddress.Status.UNASSIGNED.getValue());
        Long availableCount = ipAddressMapper.selectCount(availableWrapper);

        QueryWrapper<IpAllocation> expiringWrapper = new QueryWrapper<>();
        expiringWrapper.eq("status", 1);
        Calendar cal = Calendar.getInstance();
        expiringWrapper.le("expiration_time", new Date(cal.getTimeInMillis() + 7L * 24 * 60 * 60 * 1000));
        expiringWrapper.ge("expiration_time", new Date());
        Long expiringCount = ipAllocationMapper.selectCount(expiringWrapper);

        data.put("totalCount", totalCount);
        data.put("assignedCount", assignedCount);
        data.put("availableCount", availableCount);
        data.put("expiringCount", expiringCount);

        if (totalCount > 0) {
            data.put("utilizationRate", Math.round(assignedCount * 10000.0 / totalCount) / 100.0);
        } else {
            data.put("utilizationRate", 0);
        }

        return data;
    }

    public Map<String, Object> getRegionStats() {
        Map<String, Object> data = new HashMap<>();
        List<AddressPool> pools = addressPoolMapper.selectList(null);

        Map<String, Map<String, Object>> regionMap = new LinkedHashMap<>();
        String[] regions = {"XIAN", "XY", "WN", "BJ", "TC", "HZ", "AK"};
        String[] regionNames = {"西安", "咸阳", "渭南", "宝鸡", "铜川", "汉中", "安康"};

        for (int i = 0; i < regions.length; i++) {
            Map<String, Object> regionData = new HashMap<>();
            regionData.put("regionCode", regions[i]);
            regionData.put("regionName", regionNames[i]);

            QueryWrapper<IpAddress> totalW = new QueryWrapper<>();
            totalW.eq("region_code", regions[i]);
            Long total = ipAddressMapper.selectCount(totalW);

            QueryWrapper<IpAddress> assignedW = new QueryWrapper<>();
            assignedW.eq("region_code", regions[i]).eq("status", IpAddress.Status.ASSIGNED.getValue());
            Long assigned = ipAddressMapper.selectCount(assignedW);

            regionData.put("totalCount", total);
            regionData.put("assignedCount", assigned);
            regionData.put("availableCount", total - assigned);
            regionMap.put(regions[i], regionData);
        }

        data.put("regions", new ArrayList<>(regionMap.values()));
        return data;
    }

    public Map<String, Object> getRecentAllocations(Integer limit) {
        Map<String, Object> data = new HashMap<>();
        QueryWrapper<IpAllocation> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        wrapper.orderByDesc("allocation_time");
        wrapper.last("LIMIT " + (limit != null ? limit : 10));

        List<IpAllocation> list = ipAllocationMapper.selectList(wrapper);
        data.put("list", list);
        data.put("total", list.size());
        return data;
    }
}
