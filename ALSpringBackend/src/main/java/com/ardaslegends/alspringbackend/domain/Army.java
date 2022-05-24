package com.ardaslegends.alspringbackend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder

@Entity
@Table(name = "armies")
public class Army {

    @Id
    private String name; //unique, the army's name

    @Enumerated(EnumType.STRING)
    @Column(name = "army_type")
    @NotEmpty(message = "Army: Army Type must not be null or empty")
    private ArmyType armyType; //type of the army, either ARMY, TRADING_COMPANY or ARMED_TRADERS

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "faction", foreignKey = @ForeignKey(name = "fk_faction"))
    @NotNull(message = "Army: Faction must not be null")
    private Faction faction; //the faction this army belongs to

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "current_Region", foreignKey = @ForeignKey(name = "fk_current_region"))
    @NotNull(message = "Army: Region must not be null")
    private Region currentRegion; //region the army is currently in
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "bound_to", foreignKey = @ForeignKey(name = "fk_bound_to"))
    @NotNull(message = "Army: boundTo must not be null")
    private RPChar boundTo; //rp character the army is currently bound to
    @ElementCollection
    @NotEmpty(message = "Army: units must not be empty")
    private List<Unit> units; //the units in this army contains
    private List<String> sieges; //list of siege equipment this
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "stationed_at", foreignKey = @ForeignKey(name = "fk_stationed_at"))
    private ClaimBuild stationedAt; //claimbuild where this army is stationed
    @NotNull(message = "Army: freeTokens must not be null")
    private Integer freeTokens; //how many free unit tokens this army has left
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "origin_claimbuild", foreignKey = @ForeignKey(name = "fk_origin_claimbuild"))
    @NotNull(message = "Army: originalClaimbuld must not be null")
    private ClaimBuild originalClaimbuild; //claimbuild where this army was created from

    public void setCurrentRegion(Region currentRegion) {
        this.currentRegion = currentRegion;
    }

    public void setBoundTo(RPChar boundTo) {
        this.boundTo = boundTo;
    }

    public void setUnits(List<Unit> units) {
        this.units = units;
    }

    public void setSieges(List<String> sieges) {
        this.sieges = sieges;
    }

    public void setStationedAt(ClaimBuild stationedAt) {
        this.stationedAt = stationedAt;
    }

    public void setFreeTokens(Integer freeTokens) {
        this.freeTokens = freeTokens;
    }

}
