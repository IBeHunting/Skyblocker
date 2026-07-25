package de.hysky.skyblocker.skyblock.trackers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.serialization.Codec;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.utils.SkyBlockIcons;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.data.ProfiledData;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class TrackerManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(TrackerManager.class);
	private static final Pattern RARE_DROP_PATTERN = Pattern.compile("^(?!.*:)(?:RARE|VERY RARE|CRAZY RARE|INSANE) DROP!\\s+\\(?(?:(?<count>\\d+)x\\s+)?(?<item>.+?)\\)?(?:\\s+\\(\\+\\d+%? " + SkyBlockIcons.MAGIC_FIND + " Magic Find\\))?$");

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
		// SHARD_REVENANT is absent from the bundled NEU repo data (it postdates that mirror), so ItemRepository
		// lookups for it fall back to a placeholder icon; the chat name below is confirmed from live bazaar sources.
		NAME_TO_ID.put("Revenant Shard", "SHARD_REVENANT");
		NAME_TO_ID.put("Undead Catalyst", "UNDEAD_CATALYST");
		NAME_TO_ID.put("Foul Flesh", "FOUL_FLESH");

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
	}

	public static final TrackedDropGroup zombieDrops = new TrackedDropGroup("Revenant", TrackedDropGroup.PRESET_ZOMBIE);
	public static final TrackedDropGroup spiderDrops = new TrackedDropGroup("Tarantula", TrackedDropGroup.PRESET_SPIDER);
	public static final TrackedDropGroup wolfDrops = new TrackedDropGroup("Sven",  TrackedDropGroup.PRESET_WOLF);
	public static final TrackedDropGroup endermanDrops = new TrackedDropGroup("Voidgloom",  TrackedDropGroup.PRESET_ENDERMAN);
	public static final TrackedDropGroup blazeDrops = new TrackedDropGroup("Demonlord", TrackedDropGroup.PRESET_BLAZE);
	public static final TrackedDropGroup vampireDrops = new TrackedDropGroup("Vampire", TrackedDropGroup.PRESET_VAMPIRE);

	public static final List<TrackedDropGroup> slayerGroups = Arrays.asList(
			zombieDrops, spiderDrops, wolfDrops, endermanDrops, blazeDrops, vampireDrops
	);

	// Mojang's map codec produces immutable maps, so xmap them into mutable HashMaps at both levels since
	// TrackedDropGroup mutates its drop count map in place (see setDropCounts).
	private static final Codec<Map<String, Integer>> ITEM_COUNTS_CODEC =
			Codec.unboundedMap(Codec.STRING, Codec.INT).xmap(HashMap::new, Function.identity());
	private static final Codec<Map<String, Map<String, Integer>>> DROP_COUNTS_CODEC =
			Codec.unboundedMap(Codec.STRING, ITEM_COUNTS_CODEC).xmap(HashMap::new, Function.identity());

	// Keyed by TrackedDropGroup#getDisplayName(); persisted per-account, per-profile like the reward trackers.
	private static final ProfiledData<Map<String, Map<String, Integer>>> DROP_COUNTS_DATA =
			new ProfiledData<>(SkyblockerMod.CONFIG_DIR.resolve("reward-trackers").resolve("slayer-drops.json"), DROP_COUNTS_CODEC);

	@Init
	public static void init() {
		ClientReceiveMessageEvents.ALLOW_GAME.register(TrackerManager::onChatMessage);

		DROP_COUNTS_DATA.init();
		SkyblockEvents.PROFILE_CHANGE.register(TrackerManager::onProfileChange);
	}

	private static void onProfileChange(String prevProfileId, String newProfileId) {
		Map<String, Map<String, Integer>> allGroupsCounts = DROP_COUNTS_DATA.computeIfAbsent(HashMap::new);
		if (allGroupsCounts == null) return;
		for (TrackedDropGroup group : slayerGroups) {
			group.setDropCounts(allGroupsCounts.computeIfAbsent(group.getDisplayName(), _ -> new HashMap<>()));
		}
	}

	@Nullable
	private static TrackedDropGroup getGroupFromDrop(String itemId) {
		return slayerGroups.stream().filter(group -> group.isTracked(itemId)).findFirst().orElse(null);
	}

	private static boolean onChatMessage(Component message, boolean overlay) {
		if (overlay || !Utils.isOnSkyblock() || Minecraft.getInstance().player == null) return true;

		try {
			Matcher matcher = RARE_DROP_PATTERN.matcher(message.getString());
			if (!matcher.matches()) return true;

			String itemName = matcher.group("item");
			String countGroup = matcher.group("count");
			int amount = countGroup != null ? Integer.parseInt(countGroup) : 1;
			String itemId = NAME_TO_ID.get(itemName);
			if (itemId == null) {
				return true;
			}
			TrackedDropGroup group = getGroupFromDrop(itemId);
			if (group == null) {
				return true;
			}
			group.incrementDrops(itemId, amount);
		} catch (Exception e) { //In case there's a regex failure or something else bad happens
			LOGGER.error("[Skyblocker Tracker Manager] An unexpected exception was encountered: ", e);
		}

		return true;
	}
}
