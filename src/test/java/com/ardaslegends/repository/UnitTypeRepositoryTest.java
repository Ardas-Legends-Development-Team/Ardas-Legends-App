package com.ardaslegends.repository;

import com.ardaslegends.domain.Faction;
import com.ardaslegends.domain.UnitType;
import com.ardaslegends.repository.faction.FactionRepository;
import com.ardaslegends.repository.war.army.unit.UnitTypeRepository;
import io.vavr.collection.List;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {"spring.sql.init.mode=never"})
@ActiveProfiles("test")
public class UnitTypeRepositoryTest {

    @Autowired
    UnitTypeRepository unitTypeRepository;

    @Autowired
    FactionRepository factionRepository;

    private Faction gondor;
    private Faction mordor;
    private Faction lindon;
    private UnitType gondorArcher;
    private UnitType mordorOrc;
    private UnitType elvenWarrior;

    @BeforeEach
    void setup() {
        gondor = Faction.builder().name("Gondor").build();
        mordor = Faction.builder().name("Mordor").build();
        lindon = Faction.builder().name("Lindon").build();

        gondorArcher = new UnitType("Gondor Archer", 1.5, false);
        gondorArcher.setUsableBy(Set.of(gondor));
        mordorOrc = new UnitType("Mordor Orc", 1.0, false);
        mordorOrc.setUsableBy(Set.of(mordor));
        elvenWarrior = new UnitType("Elven Warrior", 1.0, false);
        elvenWarrior.setUsableBy(Set.of(lindon));

        factionRepository.saveAll(List.of(gondor, mordor, lindon));
        unitTypeRepository.saveAll(List.of(gondorArcher, mordorOrc, elvenWarrior));
    }

    @Test
    void ensureQueryByFactionNamesWorks() {
        var factions = Arrays.asList(gondor, mordor);
        val result = unitTypeRepository.queryByFactionNames(factions.stream().map(Faction::getName).toList());

        assertThat(result.size()).isEqualTo(2);
        assertThat(result.contains(gondorArcher)).isTrue();
        assertThat(result.contains(mordorOrc)).isTrue();
        assertThat(result.contains(elvenWarrior)).isFalse();
    }
}
