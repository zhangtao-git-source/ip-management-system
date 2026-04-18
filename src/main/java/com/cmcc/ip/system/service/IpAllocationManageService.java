package com.cmcc.ip.system.service;

import com.cmcc.ip.system.common.AllocationRequest;
import com.cmcc.ip.system.common.AllocationResult;
import com.cmcc.ip.system.entity.AddressPool;
import com.cmcc.ip.system.entity.IpAddress;
import com.cmcc.ip.system.entity.IpAllocation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class IpAllocationManageService {
    
    private final IpAddressService ipAddressService;
    private final AddressPoolService addressPoolService;
    private final IpAllocationService ipAllocationService;
    
    public IpAllocationManageService(IpAddressService ipAddressService,
                                AddressPoolService addressPoolService,
                                IpAllocationService ipAllocationService) {
        this.ipAddressService = ipAddressService;
        this.addressPoolService = addressPoolService;
        this.ipAllocationService = ipAllocationService;
    }
    
    @Transactional
    public AllocationResult allocate(AllocationRequest request) {
        AllocationResult result = new AllocationResult();
        List<AllocationResult.AllocationItem> items = new ArrayList<>();
        
        List<AddressPool> pools = addressPoolService.selectByPriority(
            request.getIpType(),
            request.getRegionCode(),
            AddressPool.PoolStatus.ENABLED.getValue()
        );
        
        if (pools == null || pools.isEmpty()) {
            result.setCode(400);
            result.setMessage("无可用地址池");
            return result;
        }
        
        int remainCount = request.getRequireCount();
        for (AddressPool pool : pools) {
            if (remainCount <= 0) {
                break;
            }
            
            List<IpAddress> availableList = ipAddressService.selectAvailableList(
                request.getIpType(),
                request.getRegionCode(),
                pool.getId(),
                remainCount
            );
            
            if (availableList == null || availableList.isEmpty()) {
                continue;
            }
            
            for (IpAddress ip : availableList) {
                if (remainCount <= 0) {
                    break;
                }
                
                ip.setStatus(IpAddress.Status.ASSIGNED.getValue());
                ipAddressService.update(ip);
                
                IpAllocation allocation = new IpAllocation();
                allocation.setIpAddressId(ip.getId());
                allocation.setIpAddress(ip.getIpAddress());
                allocation.setAddressPoolId(pool.getId());
                allocation.setAllocationType(request.getAllocationType() != null ? 
                    request.getAllocationType() : IpAllocation.AllocationType.AUTO.getValue());
                allocation.setSubscriberId(request.getSubscriberId());
                allocation.setServiceOrderId(request.getServiceOrderId());
                allocation.setExpirationTime(calcExpirationTime(request.getExpireDays()));
                allocation.setStatus(IpAllocation.AllocationStatus.IN_USE.getValue());
                allocation.setOperatorId(request.getOperatorId());
                ipAllocationService.save(allocation);
                
                AllocationResult.AllocationItem item = new AllocationResult.AllocationItem();
                item.setIpAddress(ip.getIpAddress());
                item.setAddressPoolId(pool.getId());
                item.setAllocationId(allocation.getId());
                items.add(item);
                
                addressPoolService.updateAvailableCount(pool.getId(), -1);
                remainCount--;
            }
        }
        
        if (remainCount > 0) {
            result.setCode(201);
            result.setMessage("部分分配成功，剩余" + remainCount + "个地址不足");
        } else {
            result.setCode(200);
            result.setMessage("分配成功");
        }
        
        result.setAllocationList(items);
        return result;
    }
    
    @Transactional
    public AllocationResult release(Long allocationId, Integer releaseType) {
        AllocationResult result = new AllocationResult();
        
        IpAllocation allocation = ipAllocationService.getById(allocationId);
        if (allocation == null) {
            result.setCode(404);
            result.setMessage("分配记录不存在");
            return result;
        }
        
        if (allocation.getStatus() == IpAllocation.AllocationStatus.RELEASED.getValue()) {
            result.setCode(400);
            result.setMessage("地址已释放");
            return result;
        }
        
        boolean releaseSuccess = ipAllocationService.release(allocationId, 
            releaseType != null ? releaseType : IpAllocation.ReleaseType.MANUAL.getValue());
        
        if (!releaseSuccess) {
            result.setCode(500);
            result.setMessage("释放失败");
            return result;
        }
        
        IpAddress ip = ipAddressService.getById(allocation.getIpAddressId());
        if (ip != null) {
            ip.setStatus(IpAddress.Status.UNASSIGNED.getValue());
            ipAddressService.update(ip);
            addressPoolService.updateAvailableCount(ip.getAddressPoolId(), 1);
        }
        
        result.setCode(200);
        result.setMessage("释放成功");
        return result;
    }
    
    @Transactional
    public AllocationResult renew(Long allocationId, Integer extendDays) {
        AllocationResult result = new AllocationResult();
        
        boolean renewSuccess = ipAllocationService.renew(allocationId, extendDays);
        if (!renewSuccess) {
            result.setCode(500);
            result.setMessage("续约失败");
            return result;
        }
        
        result.setCode(200);
        result.setMessage("续约成功");
        return result;
    }
    
    private Date calcExpirationTime(Integer expireDays) {
        if (expireDays == null || expireDays <= 0) {
            return null;
        }
        long expirationTime = System.currentTimeMillis() + expireDays * 24L * 60 * 60 * 1000;
        return new Date(expirationTime);
    }
}