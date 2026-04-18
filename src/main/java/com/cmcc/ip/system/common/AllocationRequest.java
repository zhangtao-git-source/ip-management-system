package com.cmcc.ip.system.common;

import lombok.Data;

@Data
public class AllocationRequest {
    
    private Integer ipType;
    
    private String regionCode;
    
    private String subscriberId;
    
    private String serviceOrderId;
    
    private Integer allocationType;
    
    private Integer requireCount;
    
    private Integer expireDays;
    
    private String operatorId;
}