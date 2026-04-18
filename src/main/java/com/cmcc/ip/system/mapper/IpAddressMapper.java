package com.cmcc.ip.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmcc.ip.system.entity.IpAddress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface IpAddressMapper extends BaseMapper<IpAddress> {
    
    List<IpAddress> selectAvailableList(@Param("ipType") Integer ipType,
                                        @Param("regionCode") String regionCode,
                                        @Param("addressPoolId") Long addressPoolId,
                                        @Param("limitCount") Integer limitCount);
    
    List<IpAddress> selectByAddressPoolId(@Param("addressPoolId") Long addressPoolId);
    
    int updateStatusBatch(@Param("ids") List<Long> ids, @Param("status") Integer status);
}