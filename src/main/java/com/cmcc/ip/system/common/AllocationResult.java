package com.cmcc.ip.system.common;

import lombok.Data;
import java.util.List;

@Data
public class AllocationResult {
    
    private Integer code;
    private String message;
    private List<AllocationItem> allocationList;
    
    @Data
    public static class AllocationItem {
        private String ipAddress;
        private Long addressPoolId;
        private Long allocationId;
    }
}