package de.hysky.skyblocker.skyblock.trackers;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import de.hysky.skyblocker.skyblock.slayers.SlayerType;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.skyblock.tabhud.widget.ElementBasedWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.Element;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.LeftRightTextElement;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.SeparatorElement;
import de.hysky.skyblocker.utils.FlexibleItemStack;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.Location;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Set;

@RegisterWidget
public class DropTrackerWidget extends ElementBasedWidget {
	// TODO: Make this configurable?
	private static final int DISPLAYED_ITEMS = 6;
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final Set<Location> AVAILABLE_LOCATIONS = Set.of(Location.CRIMSON_ISLE, Location.HUB, Location.SPIDERS_DEN, Location.THE_END, Location.THE_PARK, Location.THE_RIFT);
	private static @Nullable DropTrackerWidget instance;

	public DropTrackerWidget() {
		super(Component.literal("Slayer Drops").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), TextColor.DARK_PURPLE.getValue(), "hud_slayer_tracker");
		instance = this;
		update();
	}

	public static DropTrackerWidget getInstance() {
		return Objects.requireNonNull(instance, "DropTrackerWidget not initialized");
	}

	@Override
	public Set<Location> availableLocations() {
		return AVAILABLE_LOCATIONS;
	}

	@Override
	public void setEnabledIn(Location location, boolean enabled) {
		if (!availableLocations().contains(location)) return;
		SkyblockerConfigManager.update(config -> config.slayers.enableDropTracker = enabled);
	}

	@Override
	public boolean isEnabledIn(Location location) {
		return availableLocations().contains(location) && SkyblockerConfigManager.get().slayers.enableDropTracker;
	}

	@Override
	public boolean shouldUpdateBeforeRendering() {
		return true;
	}

	@Override
	public void updateContent() {
		SlayerType slayerType;
		if (CLIENT.gui.screen() instanceof WidgetsConfigurationScreen) {
			slayerType = SlayerType.REVENANT;
		} else {
			SlayerManager.SlayerQuest slayerQuest = SlayerManager.getSlayerQuest();
			if (CLIENT.player == null || slayerQuest == null) return;

			slayerType = slayerQuest.slayerType;
		}

		TrackedDropGroup group = switch (slayerType) {
			case REVENANT -> TrackerManager.zombieDrops;
			case TARANTULA -> TrackerManager.spiderDrops;
			case SVEN -> TrackerManager.wolfDrops;
			case VOIDGLOOM -> TrackerManager.endermanDrops;
			case DEMONLORD ->  TrackerManager.blazeDrops;
			case VAMPIRE ->  TrackerManager.vampireDrops;
		};

		double totalValue = 0;
		double otherValue = 0;
		int index = 0;
		for (TrackedDropGroup.Entry entry : group.getDropList()) {
			FlexibleItemStack stack = ItemRepository.getItemStack(entry.id());
			if (stack == null) continue;

			Component name = stack.get(DataComponents.CUSTOM_NAME);
			if (name == null) continue;

			totalValue += entry.value();

			if (index >= DISPLAYED_ITEMS) {
				otherValue += entry.value();
				continue;
			}
			Component left = Component.literal(entry.count() + "x ").append(name);
			Component right = Component.literal(Formatters.SHORT_INTEGER_NUMBERS.format(entry.value()) + " Coins").withStyle(ChatFormatting.GOLD);
			Element line = new LeftRightTextElement(left, right);
			this.addComponent(line);

			index++;
		}
		if (otherValue > 0) {
			Component left = Component.literal("Other Items...").withStyle(ChatFormatting.GOLD);
			Component right = Component.literal(Formatters.SHORT_INTEGER_NUMBERS.format(otherValue) + " Coins").withStyle(ChatFormatting.GOLD);
			Element line = new LeftRightTextElement(left, right);
			this.addComponent(line);
		}

		this.addComponent(new SeparatorElement(null));

		Component left = Component.literal("Total Coin Value: ").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD);
		Component right = Component.literal(Formatters.SHORT_INTEGER_NUMBERS.format(totalValue) + " Coins").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
		Element line = new LeftRightTextElement(left, right);
		this.addComponent(line);
	}
}
