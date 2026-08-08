package de.hysky.skyblocker.skyblock.trackers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import de.hysky.skyblocker.skyblock.slayers.SlayerType;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.utils.mayor.MayorUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.SkyBlockIcons;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.data.ProfiledData;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.item.ItemStack;

public class TrackerManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(TrackerManager.class);
	private static final Pattern RARE_DROP_PATTERN = Pattern.compile("^(?!.*:)(?:RARE|VERY RARE|CRAZY RARE|INSANE) DROP!\\s+\\(?(?:(?<count>\\d+)x\\s+)?(?<item>.+?)\\)?(?:\\s+\\(\\+\\d+%? " + SkyBlockIcons.MAGIC_FIND + " Magic Find\\))?$");
	// Bestiary Attribute Shard charmed off a kill: passive hunting-level proc (CHARM), an active Salt buff (SALT), or the Naga charm passive (NAGA)
	private static final Pattern CHARM_SHARD_PATTERN = Pattern.compile("^(?:CHARM|SALT|NAGA) You charmed an? (?<mob>.+) and captured (?<count>\\d+) Shards? from it\\.$");
	// Bestiary Attribute Shard captured by the Black Hole hunting tool
	private static final Pattern BLACK_HOLE_SHARD_PATTERN = Pattern.compile("^You caught x(?<count>\\d+) (?<mob>.+) Shards!$");

	// TODO: extract into a shared registry; this is now shared by all slayer groups below.
	private static final Map<String, String> NAME_TO_ID = new Object2ObjectArrayMap<>();
	static {
		// Spider (Tarantula Broodfather)
		NAME_TO_ID.put("Brick Red Dye", "DYE_BRICK_RED");
		NAME_TO_ID.put("Primordial Eye", "PRIMORDIAL_EYE");
		NAME_TO_ID.put("Shriveled Wasp", "SHRIVELED_WASP");
		NAME_TO_ID.put("Ensnared Snail", "ENSNARED_SNAIL");
		NAME_TO_ID.put("Digested Mosquito", "DIGESTED_MOSQUITO");
		NAME_TO_ID.put("Vial of Venom", "VIAL_OF_VENOM");
		NAME_TO_ID.put("Fly Swatter", "FLY_SWATTER");
		NAME_TO_ID.put("Tarantula Talisman", "TARANTULA_TALISMAN");
		NAME_TO_ID.put("Tarantula Catalyst", "TARANTULA_CATALYST");
		NAME_TO_ID.put("Enchanted Book (Bane of Arthropods VI)", "BANE_OF_ARTHROPODS;6");
		NAME_TO_ID.put("Spider Catalyst", "SPIDER_CATALYST");
		NAME_TO_ID.put("◆ Darkness Within Rune I", "DARKNESS_WITHIN_RUNE;1");
		NAME_TO_ID.put("◆ Bite Rune I", "BITE_RUNE;1");
		NAME_TO_ID.put("Tarantula Silk", "TARANTULA_SILK");
		NAME_TO_ID.put("Toxic Arrow Poison", "TOXIC_ARROW_POISON");
		NAME_TO_ID.put("Tarantula Web", "TARANTULA_WEB");
		NAME_TO_ID.put("Voracious Spider Shard", "ATTRIBUTE_SHARD_ARACHNO_RESISTANCE;1");
		NAME_TO_ID.put("Flaming Spider Shard", "ATTRIBUTE_SHARD_ARACHNO;1");

		// Zombie (Revenant Horror)
		NAME_TO_ID.put("Matcha Dye", "DYE_MATCHA");
		NAME_TO_ID.put("Warden Heart", "WARDEN_HEART");
		NAME_TO_ID.put("Severed Hand", "SEVERED_HAND");
		NAME_TO_ID.put("Shredded Sinew", "SHARD_OF_THE_SHREDDED");
		NAME_TO_ID.put("Scythe Blade", "SCYTHE_BLADE");
		NAME_TO_ID.put("Festering Maggot", "FESTERING_MAGGOT");
		NAME_TO_ID.put("◆ Snake Rune I", "SNAKE_RUNE;1");
		NAME_TO_ID.put("Beheaded Horror", "BEHEADED_HORROR");
		NAME_TO_ID.put("Enchanted Book (Smite VI)", "SMITE;6");
		NAME_TO_ID.put("◆ Pestilence Rune I", "ZOMBIE_SLAYER_RUNE;1");
		NAME_TO_ID.put("Revenant Catalyst", "REVENANT_CATALYST");
		NAME_TO_ID.put("Revenant Shard", "SHARD_REVENANT");
		NAME_TO_ID.put("Undead Catalyst", "UNDEAD_CATALYST");
		NAME_TO_ID.put("Foul Flesh", "FOUL_FLESH");
		NAME_TO_ID.put("Revenant Flesh", "REVENANT_FLESH");
		NAME_TO_ID.put("Golden Ghoul Shard", "ATTRIBUTE_SHARD_MIDAS_TOUCH;1");

		// Wolf (Sven Packmaster)
		NAME_TO_ID.put("Celeste Dye", "DYE_CELESTE");
		NAME_TO_ID.put("Overflux Capacitor", "OVERFLUX_CAPACITOR");
		NAME_TO_ID.put("Grizzly Salmon", "GRIZZLY_BAIT");
		NAME_TO_ID.put("Red Claw Egg", "RED_CLAW_EGG");
		NAME_TO_ID.put("◆ Couture Rune I", "COUTURE_RUNE;1");
		NAME_TO_ID.put("Enchanted Book (Critical VI)", "CRITICAL;6");
		NAME_TO_ID.put("Furball", "FURBALL");
		NAME_TO_ID.put("◆ Spirit Rune I", "SPIRIT_RUNE;1");
		NAME_TO_ID.put("Hamster Wheel", "HAMSTER_WHEEL");
		NAME_TO_ID.put("Wolf Tooth", "WOLF_TOOTH");
		NAME_TO_ID.put("Soul of the Alpha Shard", "ATTRIBUTE_SHARD_COMBO;1");

		// Enderman (Voidgloom Seraph)
		NAME_TO_ID.put("Byzantium Dye", "DYE_BYZANTIUM");
		NAME_TO_ID.put("Judgement Core", "JUDGEMENT_CORE");
		NAME_TO_ID.put("End Stone Idol", "ENDSTONE_IDOL");
		NAME_TO_ID.put("Exceedingly Rare Ender Artifact Upgrade", "EXCEEDINGLY_RARE_ENDER_ARTIFACT_UPGRADER");
		NAME_TO_ID.put("◆ Enchant Rune I", "ENCHANT_RUNE;1");
		NAME_TO_ID.put("Void Conqueror Enderman Skin", "PET_SKIN_ENDERMAN_SLAYER");
		NAME_TO_ID.put("Handy Blood Chalice", "HANDY_BLOOD_CHALICE");
		NAME_TO_ID.put("Pocket Espresso Machine", "POCKET_ESPRESSO_MACHINE");
		NAME_TO_ID.put("Etherwarp Merger", "ETHERWARP_MERGER");
		NAME_TO_ID.put("Sinful Dice", "SINFUL_DICE");
		NAME_TO_ID.put("Summoning Eye", "SUMMONING_EYE");
		NAME_TO_ID.put("Hazmat Enderman", "HAZMAT_ENDERMAN");
		NAME_TO_ID.put("Enchanted Book (Smarty Pants V)", "SMARTY_PANTS;5");
		NAME_TO_ID.put("Transmission Tuner", "TRANSMISSION_TUNER");
		NAME_TO_ID.put("Enchanted Book (Mana Steal III)", "MANA_STEAL;3");
		NAME_TO_ID.put("Null Atom", "NULL_ATOM");
		NAME_TO_ID.put("◆ Endersnake Rune I", "ENDERSNAKE_RUNE;1");
		NAME_TO_ID.put("Twilight Arrow Poison", "TWILIGHT_ARROW_POISON");
		NAME_TO_ID.put("Null Sphere", "NULL_SPHERE");

		// Blaze (Inferno Demonlord)
		NAME_TO_ID.put("Flame Dye", "DYE_FLAME");
		NAME_TO_ID.put("Wilson's Engineering Plans", "WILSON_ENGINEERING_PLANS");
		NAME_TO_ID.put("Subzero Inverter", "SUBZERO_INVERTER");
		NAME_TO_ID.put("Enchanted Book (Fire Aspect III)", "FIRE_ASPECT;3");
		NAME_TO_ID.put("High Class Archfiend Dice", "HIGH_CLASS_ARCHFIEND_DICE");
		NAME_TO_ID.put("Archfiend Dice", "ARCHFIEND_DICE");
		NAME_TO_ID.put("Enchanted Book (Duplex I)", "ULTIMATE_REITERATE;1");
		NAME_TO_ID.put("Scorched Books", "SCORCHED_BOOKS");
		NAME_TO_ID.put("◆ Lavatears Rune I", "LAVATEARS_RUNE;1");
		NAME_TO_ID.put("Wisp's Ice-Flavored Water I Splash Potion", "POTION_WISP_ICE;1");
		NAME_TO_ID.put("Kelvin Inverter", "KELVIN_INVERTER");
		NAME_TO_ID.put(SkyBlockIcons.TRUE_DEFENSE + " Flawed Opal Gemstone", "FLAWED_OPAL_GEM");
		NAME_TO_ID.put("Scorched Power Crystal", "SCORCHED_POWER_CRYSTAL");
		NAME_TO_ID.put("Mana Disintegrator", "MANA_DISINTEGRATOR");
		NAME_TO_ID.put("Gabagool Distillate", "CRUDE_GABAGOOL_DISTILLATE");
		NAME_TO_ID.put("Blaze Rod Distillate", "BLAZE_ROD_DISTILLATE");
		NAME_TO_ID.put("Glowstone Distillate", "GLOWSTONE_DUST_DISTILLATE");
		NAME_TO_ID.put("Magma Cream Distillate", "MAGMA_CREAM_DISTILLATE");
		NAME_TO_ID.put("Nether Wart Distillate", "NETHER_STALK_DISTILLATE");
		NAME_TO_ID.put("Magma Arrow", "MAGMA_ARROW");
		NAME_TO_ID.put("Derelict Ashe", "DERELICT_ASHE");
		// Dropped directly from the boss itself, not via CHARM/SALT/NAGA/Black Hole
		NAME_TO_ID.put("Inferno Demonlord Shard", "ATTRIBUTE_SHARD_ATTACK_SPEED;1");

		// Vampire (Riftstalker Bloodfiend)
		NAME_TO_ID.put("Sangria Dye", "DYE_SANGRIA");
		NAME_TO_ID.put("McGrubber's Burger", "MCGRUBBER_BURGER");
		NAME_TO_ID.put("Unfanged Vampire Part", "UNFANGED_VAMPIRE_PART");
		NAME_TO_ID.put("Guardian Lucky Block", "GUARDIAN_LUCKY_BLOCK");
		NAME_TO_ID.put("Bubba Blister", "BUBBA_BLISTER");
		NAME_TO_ID.put("Fang-tastic Chocolate Chip", "CHOCOLATE_CHIP");
		NAME_TO_ID.put("◆ Soultwist Rune I", "SOULTWIST_RUNE;1");
		// TODO: ENCHANTED_BOOK_BUNDLE_QUANTUM and ENCHANTED_BOOK_BUNDLE_THE_ONE both render as the plain-text chat
		// name "Enchanted Book Bundle" (only their color code differs), so they collide in this name-keyed map.
		// Mapped to the more common Quantum bundle for now; revisit if the drop chat message's color can be read
		// (message.getString() currently discards it) to disambiguate The One bundle instead.
		NAME_TO_ID.put("Enchanted Book Bundle", "ENCHANTED_BOOK_BUNDLE_QUANTUM");
		NAME_TO_ID.put("Coven Seal", "COVEN_SEAL");
	}

	// The one guaranteed "standard" drop per slayer boss that never produces a chat message on pickup;
	// these instead go straight into the player's inventory or sack and must be detected there.
	private static final Set<String> STANDARD_DROP_IDS = Set.of(
			"TARANTULA_WEB", "REVENANT_FLESH", "WOLF_TOOTH", "NULL_SPHERE", "DERELICT_ASHE", "COVEN_SEAL"
	);

	private static final String SACKS_MESSAGE_START = "[Sacks]";
	private static final Pattern SACK_CHANGE_PATTERN = Pattern.compile("([+-])([\\d,]+) (.+) \\((.+)\\)");

	private static final int LOBBY_CHANGE_DELAY = 60;
	private static volatile boolean changingLobby;

	public static final TrackedDropGroup zombieDrops = new TrackedDropGroup("Revenant", TrackedDropGroup.PRESET_ZOMBIE);
	public static final TrackedDropGroup spiderDrops = new TrackedDropGroup("Tarantula", TrackedDropGroup.PRESET_SPIDER);
	public static final TrackedDropGroup wolfDrops = new TrackedDropGroup("Sven",  TrackedDropGroup.PRESET_WOLF);
	public static final TrackedDropGroup endermanDrops = new TrackedDropGroup("Voidgloom",  TrackedDropGroup.PRESET_ENDERMAN);
	public static final TrackedDropGroup blazeDrops = new TrackedDropGroup("Demonlord", TrackedDropGroup.PRESET_BLAZE);
	public static final TrackedDropGroup vampireDrops = new TrackedDropGroup("Vampire", TrackedDropGroup.PRESET_VAMPIRE);

	public static final List<TrackedDropGroup> slayerGroups = Arrays.asList(
			zombieDrops, spiderDrops, wolfDrops, endermanDrops, blazeDrops, vampireDrops
	);

	// Mojang's map codec produces immutable maps, so xmap them into mutable HashMaps since
	// TrackedGroupData mutates its drop count map in place (see TrackedGroupData#incrementDrops).
	private static final Codec<Map<String, Integer>> ITEM_COUNTS_CODEC =
			Codec.unboundedMap(Codec.STRING, Codec.INT).xmap(HashMap::new, Function.identity());
	private static final Codec<TrackedGroupData> TRACKED_GROUP_DATA_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("bossKills").forGetter(TrackedGroupData::getBossKills),
			Codec.INT.fieldOf("coinsSpent").forGetter(TrackedGroupData::getCoinsSpent),
			ITEM_COUNTS_CODEC.fieldOf("dropCounts").forGetter(TrackedGroupData::getDropCounts)
	).apply(instance, TrackedGroupData::new));
	private static final Codec<Map<String, TrackedGroupData>> GROUP_DATA_CODEC =
			Codec.unboundedMap(Codec.STRING, TRACKED_GROUP_DATA_CODEC).xmap(HashMap::new, Function.identity());

	// Keyed by TrackedDropGroup#getDisplayName(); persisted per-account, per-profile like the reward trackers.
	private static final ProfiledData<Map<String, TrackedGroupData>> TRACKER_DATA =
			new ProfiledData<>(SkyblockerMod.CONFIG_DIR.resolve("reward-trackers").resolve("slayer-drops.json"), GROUP_DATA_CODEC);

	@Init
	public static void init() {
		ClientReceiveMessageEvents.ALLOW_GAME.register(TrackerManager::onChatMessage);
		ClientPlayConnectionEvents.JOIN.register((_, _, _) -> changingLobby = true);
		// Make changingLobby true for a short period while the player loads into a new lobby and their items are loading,
		// so the initial slot-update packets for their existing inventory aren't mistaken for a fresh pickup.
		SkyblockEvents.LOCATION_CHANGE.register(_ -> Scheduler.INSTANCE.schedule(() -> changingLobby = false, LOBBY_CHANGE_DELAY));

		TRACKER_DATA.init();
		SkyblockEvents.PROFILE_CHANGE.register(TrackerManager::onProfileChange);
	}

	private static void onProfileChange(String prevProfileId, String newProfileId) {
		Map<String, TrackedGroupData> allGroupsData = TRACKER_DATA.computeIfAbsent(HashMap::new);
		if (allGroupsData == null) return;
		for (TrackedDropGroup group : slayerGroups) {
			group.setDropCounts(allGroupsData.computeIfAbsent(group.getDisplayName(), _ -> new TrackedGroupData()));
		}
	}

	@Nullable
	private static TrackedDropGroup getGroupFromDrop(String itemId) {
		// An item can belong to more than one group (e.g. Flaming Spider Shard is tracked by both Spider and
		// Blaze), so prefer whichever group is currently being ground if it's one of the matches.
		TrackedDropGroup currentGroup = getCurrentlyTrackedGroup();
		if (currentGroup != null && currentGroup.isTracked(itemId)) return currentGroup;

		return slayerGroups.stream().filter(group -> group.isTracked(itemId)).findFirst().orElse(null);
	}

	private static boolean onChatMessage(Component message, boolean overlay) {
		if (overlay || !Utils.isOnSkyblock() || Minecraft.getInstance().player == null) return true;

		try {
			String plainText = message.getString();
			Matcher rareDropMatcher = RARE_DROP_PATTERN.matcher(plainText);
			Matcher charmShardMatcher = CHARM_SHARD_PATTERN.matcher(plainText);
			Matcher blackHoleShardMatcher = BLACK_HOLE_SHARD_PATTERN.matcher(ChatFormatting.stripFormatting(plainText));

			if (ChatFormatting.stripFormatting(plainText).startsWith(SACKS_MESSAGE_START)) {
				onSackMessage(message);
			}
			else if (plainText.strip().equals("SLAYER QUEST STARTED!")) {
				onSlayerBeginMessage();
			}
			else if (plainText.strip().equals("SLAYER QUEST COMPLETE!")) {
				onSlayerCompleteMessage();
			}
			else if (rareDropMatcher.matches()) {
				onRareDropMessage(rareDropMatcher);
			}
			else if (charmShardMatcher.matches()) {
				trackDrop(charmShardMatcher.group("mob") + " Shard", Integer.parseInt(charmShardMatcher.group("count")));
			}
			else if (blackHoleShardMatcher.matches()) {
				trackDrop(blackHoleShardMatcher.group("mob") + " Shard", Integer.parseInt(blackHoleShardMatcher.group("count")));
			}
		} catch (Exception e) { //In case there's a regex failure or something else bad happens
			LOGGER.error("[Skyblocker Tracker Manager] An unexpected exception was encountered: ", e);
		}

		return true;
	}

	/**
	 * Standard slayer drops that get auto-collected into a sack surface only as a "[Sacks]" hover-chat line
	 * rather than a plain drop message, so they need their own parser here.
	 */
	private static void onSackMessage(Component message) {
		HoverEvent hoverEvent = message.getSiblings().getFirst().getStyle().getHoverEvent();
		if (hoverEvent == null || hoverEvent.action() != HoverEvent.Action.SHOW_TEXT) return;
		String hoverMessage = ((HoverEvent.ShowText) hoverEvent).value().getString();

		Matcher matcher = SACK_CHANGE_PATTERN.matcher(ChatFormatting.stripFormatting(hoverMessage));
		while (matcher.find()) {
			if (!"+".equals(matcher.group(1))) continue;

			String itemId = NAME_TO_ID.get(matcher.group(3));
			if (itemId == null || !STANDARD_DROP_IDS.contains(itemId)) continue;

			TrackedDropGroup group = getGroupFromDrop(itemId);
			if (group == null) continue;

			int amount = Formatters.parseNumber(matcher.group(2)).intValue();
			group.getTrackerData().incrementDrops(itemId, amount);
		}
	}

	private static void onSlayerBeginMessage() {
		int cost = calculateSlayerCost();
		TrackedDropGroup group = getCurrentlyTrackedGroup();
		if (group != null && cost > 0) {
			group.getTrackerData().incrementCoinsSpent(cost);
		}
	}

	private static void onSlayerCompleteMessage() {
		TrackedDropGroup group = getCurrentlyTrackedGroup();
		if (group == null) return;

		group.getTrackerData().incrementKills();
	}

	private static void onRareDropMessage(Matcher matcher) {
		String itemName = matcher.group("item");
		String countGroup = matcher.group("count");
		int amount = countGroup != null ? Integer.parseInt(countGroup) : 1;
		trackDrop(itemName, amount);
	}

	private static void trackDrop(String itemName, int amount) {
		String itemId = NAME_TO_ID.get(itemName);
		if (itemId == null) {
			return;
		}
		TrackedDropGroup group = getGroupFromDrop(itemId);
		if (group == null) {
			return;
		}
		group.getTrackerData().incrementDrops(itemId, amount);
	}

	// TODO: Support motes for vampire slayer
	private static int calculateSlayerCost() {
		SlayerManager.SlayerQuest slayerQuest = SlayerManager.getSlayerQuest();

		if (slayerQuest == null || slayerQuest.slayerType == SlayerType.VAMPIRE) return 0;

		double cost = switch (slayerQuest.slayerTier) {
			case I -> 2000;
			case II -> 7500;
			case III -> 20000;
			case IV -> 50000;
			case V -> 100000;
		};
		// Slayer discount when player is at level 7+
		if (slayerQuest.level >= 7) {
			cost *= 0.96;
		}
		// Slayer reduction cost from mayor Aatrox
		if (MayorUtils.getActivePerks().contains("SLASHED Pricing")) {
			cost *= 0.5;
		}
		return (int) cost;
	}


	/**
	 * Standard slayer drops that land directly in the player's inventory produce no message at all, so they're
	 * detected by diffing the slot contents on every inventory slot-update packet, mirroring
	 * {@link de.hysky.skyblocker.skyblock.ItemPickupWidget#onItemPickup(int, ItemStack)}.
	 */
	public static void onItemPickup(int slot, ItemStack newStack) {
		Minecraft client = Minecraft.getInstance();
		if (changingLobby || client.player == null || !Utils.isOnSkyblock()) return;
		//make sure there is not an inventory open, so shuffling items around a chest/sack menu isn't miscounted
		if (client.gui.screen() != null) return;

		//if the slot is below 9, it is a slot that we do not care about
		//if the slot is equal to or above 45, it is not in the player's inventory
		if (slot < 9 || slot >= 45) return;
		//hotbar slots are at the end of the ids instead of at the start like in the inventory main stacks, so we convert to that indexing
		if (slot >= 36) {
			slot -= 36;
		}
		if (slot == 8) return; // Ignore skyblock menu/quiver slot

		ItemStack oldStack = client.player.getInventory().getNonEquipmentItems().get(slot);
		int countDiff = newStack.getCount() - oldStack.getCount();
		if (countDiff <= 0) return;

		String itemId = newStack.getNeuName();
		if (!STANDARD_DROP_IDS.contains(itemId)) return;

		TrackedDropGroup group = getGroupFromDrop(itemId);
		if (group == null) return;

		group.getTrackerData().incrementDrops(itemId, countDiff);
	}

	@Nullable
	public static TrackedDropGroup getCurrentlyTrackedGroup() {
		SlayerType slayerType;
		if (Minecraft.getInstance().gui.screen() instanceof WidgetsConfigurationScreen) {
			slayerType = SlayerType.REVENANT;
		} else {
			SlayerManager.SlayerQuest slayerQuest = SlayerManager.getSlayerQuest();
			if (Minecraft.getInstance().player == null || slayerQuest == null) return null;

			slayerType = slayerQuest.slayerType;
		}

		return switch (slayerType) {
			case REVENANT -> TrackerManager.zombieDrops;
			case TARANTULA -> TrackerManager.spiderDrops;
			case SVEN -> TrackerManager.wolfDrops;
			case VOIDGLOOM -> TrackerManager.endermanDrops;
			case DEMONLORD ->  TrackerManager.blazeDrops;
			case VAMPIRE ->  TrackerManager.vampireDrops;
		};
	}
}
