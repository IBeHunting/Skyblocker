package de.hysky.skyblocker.skyblock.trackers;

import java.util.Arrays;
import java.util.List;

public class TrackedDropGroup {

	public static final List<String> PRESET_SPIDER = Arrays.asList(
			"DYE_BRICK_RED",
			"PRIMORDIAL_EYE",
			"SHRIVELED_WASP",
			"ENSNARED_SNAIL",
			"DIGESTED_MOSQUITO",
			"VIAL_OF_VENOM",
			"FLY_SWATTER",
			"TARANTULA_TALISMAN",
			"TARANTULA_CATALYST",
			"BANE_OF_ARTHROPODS;6",
			"SPIDER_CATALYST",
			"DARKNESS_WITHIN_RUNE;1",
			"BITE_RUNE;1",
			"TARANTULA_SILK",
			"TOXIC_ARROW_POISON",
			"TARANTULA_WEB",
			"ATTRIBUTE_SHARD_ARACHNO_RESISTANCE;1",
			"ATTRIBUTE_SHARD_ARACHNO;1",
			"ATTRIBUTE_SHARD_COCOON_CHANCE;1",
			"ATTRIBUTE_SHARD_SLAYER_DISCOUNT;1"
	);

	public static final List<String> PRESET_ZOMBIE = Arrays.asList(
			"DYE_MATCHA",
			"WARDEN_HEART",
			"SEVERED_HAND",
			"SHARD_OF_THE_SHREDDED",
			"SCYTHE_BLADE",
			"FESTERING_MAGGOT",
			"SNAKE_RUNE;1",
			"BEHEADED_HORROR",
			"SMITE;6",
			"ZOMBIE_SLAYER_RUNE;1",
			"REVENANT_CATALYST",
			"SHARD_REVENANT",
			"UNDEAD_CATALYST",
			"FOUL_FLESH",
			"REVENANT_FLESH",
			"ATTRIBUTE_SHARD_MIDAS_TOUCH;1"
	);

	public static final List<String> PRESET_WOLF = Arrays.asList(
			"DYE_CELESTE",
			"OVERFLUX_CAPACITOR",
			"GRIZZLY_BAIT",
			"RED_CLAW_EGG",
			"COUTURE_RUNE;1",
			"CRITICAL;6",
			"FURBALL",
			"SPIRIT_RUNE;1",
			"HAMSTER_WHEEL",
			"WOLF_TOOTH",
			"ATTRIBUTE_SHARD_COMBO;1"
	);

	public static final List<String> PRESET_ENDERMAN = Arrays.asList(
			"DYE_BYZANTIUM",
			"JUDGEMENT_CORE",
			"ENDSTONE_IDOL",
			"EXCEEDINGLY_RARE_ENDER_ARTIFACT_UPGRADER",
			"ENCHANT_RUNE;1",
			"PET_SKIN_ENDERMAN_SLAYER",
			"HANDY_BLOOD_CHALICE",
			"POCKET_ESPRESSO_MACHINE",
			"ETHERWARP_MERGER",
			"SINFUL_DICE",
			"SUMMONING_EYE",
			"HAZMAT_ENDERMAN",
			"SMARTY_PANTS;5",
			"TRANSMISSION_TUNER",
			"MANA_STEAL;3",
			"NULL_ATOM",
			"ENDERSNAKE_RUNE;1",
			"TWILIGHT_ARROW_POISON",
			"NULL_SPHERE"
	);

	public static final List<String> PRESET_BLAZE = Arrays.asList(
			"DYE_FLAME",
			"WILSON_ENGINEERING_PLANS",
			"SUBZERO_INVERTER",
			"FIRE_ASPECT;3",
			"HIGH_CLASS_ARCHFIEND_DICE",
			"ARCHFIEND_DICE",
			"ULTIMATE_REITERATE;1",
			"SCORCHED_BOOKS",
			"LAVATEARS_RUNE;1",
			"POTION_WISP_ICE;1",
			"KELVIN_INVERTER",
			"FLAWED_OPAL_GEM",
			"SCORCHED_POWER_CRYSTAL",
			"MANA_DISINTEGRATOR",
			"CRUDE_GABAGOOL_DISTILLATE",
			"BLAZE_ROD_DISTILLATE",
			"GLOWSTONE_DUST_DISTILLATE",
			"MAGMA_CREAM_DISTILLATE",
			"NETHER_STALK_DISTILLATE",
			"MAGMA_ARROW",
			"DERELICT_ASHE",
			"ATTRIBUTE_SHARD_ATTACK_SPEED;1"
	);

	public static final List<String> PRESET_VAMPIRE = Arrays.asList(
			"DYE_SANGRIA",
			"MCGRUBBER_BURGER",
			"UNFANGED_VAMPIRE_PART",
			"ENCHANTED_BOOK_BUNDLE_THE_ONE",
			"GUARDIAN_LUCKY_BLOCK",
			"BUBBA_BLISTER",
			"CHOCOLATE_CHIP",
			"SOULTWIST_RUNE;1",
			"ENCHANTED_BOOK_BUNDLE_QUANTUM",
			"COVEN_SEAL"
	);

	private final String displayName;
	private final List<String> trackedIds;
	private TrackedGroupData trackerData;

	public TrackedDropGroup(String displayName, List<String> trackedIds) {
		this.displayName = displayName;
		this.trackedIds = trackedIds;
		this.trackerData = new TrackedGroupData();
	}

	public boolean isTracked(String id) {
		return trackedIds.contains(id);
	}

	/**
	 * Swaps in the (mutable) drop count map for the current profile, so that further increments are written
	 * directly into the persisted data. Called by {@link TrackerManager} on profile change.
	 */
	public void setDropCounts(TrackedGroupData data) {
		this.trackerData = data;
	}

	public TrackedGroupData getTrackerData() {
		return this.trackerData;
	}

	public String getDisplayName() {
		return this.displayName;
	}

	public List<String> getTrackedIds() {
		return this.trackedIds;
	}
}
