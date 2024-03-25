package com.ardaslegends.repository.war.army.unit;

import com.ardaslegends.domain.QFaction;
import com.ardaslegends.domain.QUnitType;
import com.ardaslegends.domain.UnitType;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

@Slf4j
public class UnitTypeRepositoryImpl extends QuerydslRepositorySupport implements UnitTypeRepositoryCustom{
    public UnitTypeRepositoryImpl(){
        super(UnitType.class);
    }
    @Override
    public List<UnitType> queryByFactionNames(List<String> factionNames) {
        log.debug("Querying UnitTypes by faction names: [{}]", StringUtils.join(factionNames, ", "));

        QUnitType qUnitType = QUnitType.unitType;
        QFaction qFaction = new QFaction("factions");

        val result = from(qUnitType)
                .innerJoin(qUnitType.usableBy, qFaction)
                .where(qFaction.name.in(factionNames))
                .stream().toList();

        log.debug("Queried [{}] unitTypes: [{}]", result.size(), StringUtils.join(result, ", "));
        return result;
    }
}
