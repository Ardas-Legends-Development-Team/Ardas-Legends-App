package com.ardaslegends.domain.applications;

import com.ardaslegends.domain.*;
import com.ardaslegends.presentation.discord.commands.ALMessageResponse;
import com.ardaslegends.presentation.discord.utils.ALColor;
import com.ardaslegends.presentation.discord.utils.FactionBanners;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.springframework.core.annotation.Order;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Getter
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "claimbuild_apps")
public class ClaimbuildApplication extends AbstractApplication<ClaimBuild> {

    @NotBlank
    private String claimbuildName;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "owned_by_id", foreignKey = @ForeignKey(name = "fk_claimbuild_apps_owned_by_id"))
    private Faction ownedBy;
    @ManyToOne
    @NotNull
    @JoinColumn(name = "region_id", foreignKey = @ForeignKey(name = "fk_claimbuild_apps_region_id"))
    private Region region;
    @NotNull
    @Enumerated(EnumType.STRING)
    private ClaimBuildType claimBuildType;
    @NotNull
    private Coordinate coordinate;

    @ElementCollection(targetClass = EmbeddedProductionSite.class)
    @CollectionTable(name = "claimbuild_application_production_sites",
            joinColumns = @JoinColumn(name = "claimbuild_id", foreignKey = @ForeignKey(name = "fk_claimbuild_application_production_sites_")))
    private Set<EmbeddedProductionSite> productionSites = new HashSet<>();

    @ElementCollection(targetClass = SpecialBuilding.class)
    @Enumerated(EnumType.STRING)
    private List<SpecialBuilding> specialBuildings = new ArrayList<>();
    private String traders;
    private String siege;
    private String numberOfHouses;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<Player> builtBy = new HashSet<>();


    public ClaimbuildApplication(Player applicant, String claimbuildName, Faction ownedBy, Region region, ClaimBuildType claimBuildType, Coordinate coordinate, Set<EmbeddedProductionSite> productionSites, List<SpecialBuilding> specialBuildings, String traders, String siege, String numberOfHouses, Set<Player> builtBy) {
        super(applicant);
        this.claimbuildName = claimbuildName;
        this.ownedBy = ownedBy;
        this.region = region;
        this.claimBuildType = claimBuildType;
        this.coordinate = coordinate;
        this.productionSites = productionSites;
        this.specialBuildings = specialBuildings;
        this.traders = traders;
        this.siege = siege;
        this.numberOfHouses = numberOfHouses;
        this.builtBy = builtBy;
    }

    @Override
    public EmbedBuilder buildApplicationMessage() {
        String builtByString = builtBy.stream()
                .map(Player::getIgn)
                .collect(Collectors.joining(", "));

        return new EmbedBuilder()
                .setTitle("Claimbuild Application")
                .setColor(ALColor.YELLOW)
                .addInlineField("Name", claimbuildName)
                .addInlineField("Faction", ownedBy.getName())
                .addInlineField("Region", region.getId())
                .addInlineField("Type", claimBuildType.getName())
                .addField("Production Sites", createProductionSiteString())
                .addField("Special Buildings", createSpecialBuildingsString())
                .addInlineField("Traders", traders)
                .addInlineField("Siege", siege)
                .addInlineField("Houses", numberOfHouses)
                .addInlineField("Coordinates", coordinate.toString())
                .addField("Built by", builtByString)
                .setThumbnail(FactionBanners.getBannerUrl(ownedBy.getName()))
                .addField("Upvoted By", getAcceptedBy().stream().map(Player::getIgn).collect(Collectors.joining(", ")))
                .addInlineField("Downvoted By", getDeclinedBy().stream().map(Player::getIgn).collect(Collectors.joining(", ")))
                .setTimestampToNow();
    }

    @Override
    public EmbedBuilder buildAcceptedMessage() {
        String builtByString = builtBy.stream()
                .map(Player::getIgn)
                .collect(Collectors.joining(", "));

        return new EmbedBuilder()
                .setTitle("%s %s has been accepted!".formatted(claimBuildType.getName(), claimbuildName))
                .addInlineField("Name", claimbuildName)
                .addInlineField("Faction", ownedBy.getName())
                .addInlineField("Region", region.getId())
                .addInlineField("Type", claimBuildType.getName())
                .addField("Production Sites", createProductionSiteString())
                .addField("Special Buildings", createSpecialBuildingsString())
                .addInlineField("Traders", traders)
                .addInlineField("Siege", siege)
                .addInlineField("Houses", numberOfHouses)
                .addInlineField("Coordinates", coordinate.toString())
                .addField("Built by", builtByString)
                .setThumbnail(FactionBanners.getBannerUrl(ownedBy.getName()))
                .setColor(ALColor.GREEN)
                .setTimestampToNow();
    }

    @Override
    public ClaimBuild finishApplication() {
        if(state != ApplicationState.ACCEPTED) {
            throw new RuntimeException("Claimbuild Application not yet accepted!");
        }
        val cb = new ClaimBuild(claimbuildName, region, claimBuildType, ownedBy, coordinate, specialBuildings, traders, siege, numberOfHouses, builtBy);
        cb.setProductionSites(mapProductionSites(cb));
        return cb;
    }

    @Override
    protected Short getRequiredVoteCount() {
        return 2;
    }

    public List<ProductionClaimbuild> mapProductionSites(ClaimBuild claimBuild) {
        Objects.requireNonNull(claimBuild);
        return productionSites.stream()
                .map(embeddedProductionSite -> new ProductionClaimbuild(embeddedProductionSite.getProductionSite(), claimBuild, embeddedProductionSite.getCount()))
                .toList();
    }

    public String createProductionSiteString() {
        log.debug("ProductionSiteList Count: {}", productionSites.size());
        StringBuilder prodString = new StringBuilder();
        productionSites.forEach(productionSite -> {
            String resource = productionSite.getProductionSite().getProducedResource().getResourceName();
            String type = productionSite.getProductionSite().getType().getName();
            int count = productionSite.getCount().intValue();
            prodString.append(count).append(" ").append(resource).append(" ").append(type).append("\n");
        });

        String returnProdString = prodString.toString();
        log.debug("CreateProductionSiteString: {}", returnProdString);

        return returnProdString.isBlank() ? "None" : returnProdString;
    }

    private String createSpecialBuildingsString() {
        StringBuilder specialString = new StringBuilder();

        Map<SpecialBuilding, Long> countedSpecialBuildings = specialBuildings.stream()
                .collect(Collectors.groupingBy(specialBuilding -> specialBuilding, Collectors.counting()));

        countedSpecialBuildings.forEach((specialBuilding, aLong) -> specialString.append(aLong + " " + specialBuilding.getName() + ", "));

        String returnSpecialString = specialString.toString();

        return returnSpecialString.isBlank() ? "None" : returnSpecialString;
    }

}
