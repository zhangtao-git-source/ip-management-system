package com.cmcc.ip.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmcc.ip.system.entity.IpAllocation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface IpAllocationMapper extends BaseMapper<IpAllocation> {
    
    List<IpAllocation> selectBySubscriberId(@Param("subscriberId") String subscriberId);
    
    List<IpAllocation> selectByServiceOrderId(@Param("serviceOrderId") String serviceOrderId);
    
    List<IpAllocation> selectActiveBySubscriberId(@Param("subscriberId") String subscriberId);
}