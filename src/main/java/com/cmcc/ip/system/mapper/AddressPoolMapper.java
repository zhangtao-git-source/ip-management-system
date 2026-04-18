package com.cmcc.ip.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmcc.ip.system.entity.AddressPool;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AddressPoolMapper extends BaseMapper<AddressPool> {
    
    List<AddressPool> selectByPriority(@Param("ipType") Integer ipType,
                                       @Param("regionCode") String regionCode,
                                       @Param("status") Integer status);
    
    int updateAvailableCount(@Param("id") Long id, @Param("count") Integer count);
}