package com.ardaslegends.service;

import com.ardaslegends.domain.Region;
import com.ardaslegends.repository.region.RegionRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
public class RegionServiceTest {

    private RegionRepository mockRegionRepository;
    private RegionService regionService;
    private List<Region> regionList;

    @BeforeEach
    void setup() {
        mockRegionRepository = mock(RegionRepository.class);
        regionService = new RegionService(mockRegionRepository);

        regionList = new ArrayList<>();

        IntStream.range(0,15).forEach(value -> {
            regionList.add(Region.builder().name(""+value).hasOwnershipChanged(true).build());
        });

        when(mockRegionRepository.findAllByHasOwnershipChangedTrue()).thenReturn(regionList);
        when(mockRegionRepository.saveAll(regionList)).thenReturn(regionList);
    }

    @Test
    void ensureResetHasOwnershipWorksProperly() {

        var result = regionService.resetHasOwnership();

        assertThat(allOwnershipFalse(result)).isTrue();

    }

    boolean allOwnershipFalse(List<Region> regions) {
        return regions.stream().noneMatch(region -> Boolean.TRUE.equals(region.isHasOwnershipChanged()));
    }

}
