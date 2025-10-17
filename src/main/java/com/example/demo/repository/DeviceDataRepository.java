package com.example.demo.repository;

import com.example.demo.model.DeviceData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DeviceDataRepository extends JpaRepository<DeviceData, Long> {
    List<DeviceData> findByDeviceIdOrderByTimestampDesc(Long deviceId);
    DeviceData findTopByDeviceIdOrderByTimestampDesc(Long deviceId);
}