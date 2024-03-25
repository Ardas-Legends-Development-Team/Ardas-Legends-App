package com.ardaslegends.repository.war.army;

import com.ardaslegends.domain.Army;
import com.ardaslegends.domain.ArmyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArmyRepository extends JpaRepository<Army, Long> {

    public Optional<Army> findArmyByName(String name);
    public List<Army> findAllByArmyType(ArmyType armyType);
    public List<Army> findArmyByIsHealingTrue();

}
