package com.cmcc.ip.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmcc.ip.system.entity.Region;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 地市区县Mapper接口
 */
@Mapper
public interface RegionMapper extends BaseMapper<Region> {

    /**
     * 根据地市类型查询
     */
    @Select("SELECT * FROM tb_region WHERE region_type = #{regionType} AND status = 1 ORDER BY sort_order")
    List<Region> selectByType(@Param("regionType") Integer regionType);

    /**
     * 根据父级编码查询区县
     */
    @Select("SELECT * FROM tb_region WHERE parent_code = #{parentCode} AND status = 1 ORDER BY sort_order")
    List<Region> selectByParentCode(@Param("parentCode") String parentCode);

    /**
     * 查询所有地市（不包含区县）
     */
    @Select("SELECT * FROM tb_region WHERE region_type = 1 AND status = 1 ORDER BY sort_order")
    List<Region> selectAllCities();

    /**
     * 根据编码查询地区
     */
    @Select("SELECT * FROM tb_region WHERE region_code = #{regionCode} LIMIT 1")
    Region selectByCode(@Param("regionCode") String regionCode);
}
