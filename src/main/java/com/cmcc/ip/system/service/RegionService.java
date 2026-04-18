package com.cmcc.ip.system.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmcc.ip.system.entity.Region;
import com.cmcc.ip.system.mapper.RegionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 地市区县Service
 */
@Slf4j
@Service
public class RegionService extends ServiceImpl<RegionMapper, Region> {

    /**
     * 获取所有地市列表
     */
    public List<Region> getAllCities() {
        return baseMapper.selectAllCities();
    }

    /**
     * 根据地市编码获取地市名称
     */
    public String getRegionNameByCode(String regionCode) {
        if (regionCode == null || regionCode.isEmpty()) {
            return null;
        }
        Region region = baseMapper.selectByCode(regionCode);
        return region != null ? region.getRegionName() : null;
    }

    /**
     * 根据地市编码获取下级区县
     */
    public List<Region> getDistrictsByCityCode(String cityCode) {
        return baseMapper.selectByParentCode(cityCode);
    }

    /**
     * 获取所有有效的地市区县
     */
    public List<Region> getAllRegions() {
        return lambdaQuery()
                .eq(Region::getStatus, 1)
                .orderByAsc(Region::getSortOrder)
                .list();
    }
}
