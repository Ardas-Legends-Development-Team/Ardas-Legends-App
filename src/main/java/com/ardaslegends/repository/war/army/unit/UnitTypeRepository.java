package com.ardaslegends.repository.war.army.unit;

import com.ardaslegends.domain.UnitType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnitTypeRepository extends JpaRepository<UnitType, String>, UnitTypeRepositoryCustom {
}
