package com.lumitest.datainspection.domain.model;

import com.lumitest.util.discovery.DiscoveryResult;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ComparisonResult {
    private String fieldName;
    private DiscoveryResult sourceA;
    private DiscoveryResult sourceB;
    private boolean identical;
    private String diffSummary;
}
