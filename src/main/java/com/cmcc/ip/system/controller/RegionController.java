package com.cmcc.ip.system.controller;

import com.cmcc.ip.system.common.Result;
import com.cmcc.ip.system.entity.Region;
import com.cmcc.ip.system.service.RegionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 地市区县管理Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/region")
@CrossOrigin(origins = "*")
public class RegionController {

    @Autowired
    private RegionService regionService;

    /**
     * 获取所有地市列表
     */
    @GetMapping("/cities")
    public Result<List<Region>> getAllCities() {
        log.info("获取所有地市列表");
        List<Region> cities = regionService.getAllCities();
        return Result.success(cities);
    }

    /**
     * 获取所有地市区县（树形结构）
     */
    @GetMapping("/tree")
    public Result<List<Map<String, Object>>> getRegionTree() {
        log.info("获取地市区县树形结构");
        List<Region> allRegions = regionService.getAllRegions();
        
        // 构建树形结构
        List<Map<String, Object>> tree = allRegions.stream()
                .filter(r -> r.getRegionType() == 1) // 只取地市级别
                .map(city -> {
                    Map<String, Object> cityMap = new HashMap<>();
                    cityMap.put("code", city.getRegionCode());
                    cityMap.put("name", city.getRegionName());
                    cityMap.put("type", city.getRegionType());
                    
                    // 获取该地市下的区县
                    List<Map<String, Object>> districts = allRegions.stream()
                            .filter(r -> r.getRegionType() == 2 && city.getRegionCode().equals(r.getParentCode()))
                            .map(d -> {
                                Map<String, Object> dMap = new HashMap<>();
                                dMap.put("code", d.getRegionCode());
                                dMap.put("name", d.getRegionName());
                                dMap.put("type", d.getRegionType());
                                return dMap;
                            })
                            .collect(Collectors.toList());
                    
                    cityMap.put("children", districts);
                    return cityMap;
                })
                .collect(Collectors.toList());
        
        return Result.success(tree);
    }

    /**
     * 根据地市编码获取区县列表
     */
    @GetMapping("/districts/{cityCode}")
    public Result<List<Region>> getDistrictsByCity(@PathVariable String cityCode) {
        log.info("获取地市[{}]的区县列表", cityCode);
        List<Region> districts = regionService.getDistrictsByCityCode(cityCode);
        return Result.success(districts);
    }

    /**
     * 根据地市编码获取地市名称
     */
    @GetMapping("/name/{regionCode}")
    public Result<String> getRegionName(@PathVariable String regionCode) {
        log.info("获取地区编码[{}]的名称", regionCode);
        String name = regionService.getRegionNameByCode(regionCode);
        return name != null ? Result.success(name) : Result.error(404, "地区不存在");
    }

    /**
     * 获取所有地区列表（扁平结构）
     */
    @GetMapping("/list")
    public Result<List<Region>> getAllRegions() {
        log.info("获取所有地区列表");
        List<Region> regions = regionService.getAllRegions();
        return Result.success(regions);
    }
}
