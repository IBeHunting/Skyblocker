package de.hysky.skyblocker.skyblock.trackers;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.tabhud.widget.ElementBasedWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.LeftRightTextElement;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.SeparatorElement;
import de.hysky.skyblocker.utils.FlexibleItemStack;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.Location;
import net.minecraft.ChatFormatting;
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
	public boolean shouldRender(Location location) {
		TrackedDropGroup group = TrackerManager.getCurrentlyTrackedGroup();

		return super.shouldRender(location) && group != null;
	}

	@Override
	public boolean shouldUpdateBeforeRendering() {
		return true;
	}

	@Override
	public void updateContent() {
		double totalValue = 0;
		double otherValue = 0;
		int index = 0;
		Component left, right;

		TrackedDropGroup group = TrackerManager.getCurrentlyTrackedGroup();
		if (group == null) return;

		for (TrackedGroupData.Entry entry : group.getTrackerData().getDropList(group.getTrackedIds())) {
			FlexibleItemStack stack = ItemRepository.getItemStack(entry.id());
			if (stack == null) continue;

			Component name = stack.get(DataComponents.CUSTOM_NAME);
			if (name == null) continue;

			totalValue += entry.value();

			if (index >= DISPLAYED_ITEMS) {
				otherValue += entry.value();
				continue;
			}
			left = Component.literal(entry.count() + " × ").append(name);
			right = Component.literal(formatNumeric(entry.value()) + " Coins").withStyle(ChatFormatting.GOLD);
			this.addComponent(new LeftRightTextElement(left, right));

			index++;
		}
		if (otherValue > 0) {
			left = Component.literal("Other Items...").withStyle(ChatFormatting.GOLD);
			right = Component.literal(formatNumeric(otherValue) + " Coins").withStyle(ChatFormatting.GOLD);
			this.addComponent(new LeftRightTextElement(left, right));
		}

		this.addComponent(new SeparatorElement(null));

		// Total drop value across all items
		left = Component.literal("Total Coin Value: ").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD);
		right = Component.literal(formatNumeric(totalValue) + " Coins").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
		this.addComponent(new LeftRightTextElement(left, right));

		// Total number of bosses killed
		left = Component.literal("Total Bosses Killed: ").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD);
		right = Component.literal(formatNumeric(group.getTrackerData().getBossKills())).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD);
		this.addComponent(new LeftRightTextElement(left, right));
	}

	private String formatNumeric(double number) {
		return Formatters.SHORT_FLOAT_NUMBERS.format(number);
	}
}
