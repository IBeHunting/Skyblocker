package de.hysky.skyblocker.skyblock.trackers;

import de.hysky.skyblocker.skyblock.item.tooltip.info.TooltipInfoType;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.BazaarProduct;
import de.hysky.skyblocker.utils.FlexibleItemStack;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

public class TrackedGroupData {

	public record Entry(String id, int count, double value) { }

	private int bossKills;
	private final Map<String, Integer> dropCounts;

	public TrackedGroupData() {
		this.bossKills = 0;
		this.dropCounts = new HashMap<>();
	}

	public TrackedGroupData(int bossKills, Map<String, Integer> dropCounts) {
		this.bossKills = bossKills;
		this.dropCounts = dropCounts;
	}

	public int getBossKills() {
		return this.bossKills;
	}

	public Map<String, Integer> getDropCounts() {
		return this.dropCounts;
	}

	public void incrementDrops(String id, int amount) {
		this.dropCounts.merge(id, amount, Integer::sum);
	}

	public void incrementKills() {
		this.bossKills++;
	}

	public List<Entry> getDropList(List<String> tracked) {
		List<Entry> entries = new ArrayList<>();
		for (String id : tracked) {
			int count = this.getDropCount(id);
			if (count <= 0) continue;

			double coinValue = this.getValue(id);

			entries.add(new Entry(id, count, coinValue));
		}
		entries.sort(Comparator.<Entry>comparingDouble(x -> x.value).reversed());
		return entries;
	}

	public int getDropCount(String id) {
		return this.dropCounts.getOrDefault(id, 0);
	}

	public double getValue(String itemId) {
		if (!dropCounts.containsKey(itemId)) {
			return 0;
		}

		FlexibleItemStack stack = ItemRepository.getItemStack(itemId);
		if (stack == null) return 0;

		String skyblockApiId = stack.getSkyblockApiId();
		OptionalDouble bazaarSellPrice = getBazaarSellPrice(skyblockApiId);
		double unitPrice = bazaarSellPrice.isPresent()
				? bazaarSellPrice.getAsDouble()
				: cheaperOf(getLowestBin(skyblockApiId), getThreeDayAverage(skyblockApiId));

		return unitPrice * dropCounts.get(itemId);
	}

	private OptionalDouble getBazaarSellPrice(String skyblockApiId) {
		Object2ObjectMap<String, BazaarProduct> bazaarPrices = TooltipInfoType.BAZAAR.getData();
		if (bazaarPrices == null || !bazaarPrices.containsKey(skyblockApiId)) return OptionalDouble.empty();

		return bazaarPrices.get(skyblockApiId).sellPrice();
	}

	private OptionalDouble getLowestBin(String skyblockApiId) {
		Object2DoubleMap<String> lowestBins = TooltipInfoType.LOWEST_BINS.getData();
		if (lowestBins == null || !lowestBins.containsKey(skyblockApiId)) return OptionalDouble.empty();

		return OptionalDouble.of(lowestBins.getDouble(skyblockApiId));
	}

	private OptionalDouble getThreeDayAverage(String skyblockApiId) {
		Object2DoubleMap<String> sevenDayAverages = TooltipInfoType.THREE_DAY_AVERAGE.getData();
		if (sevenDayAverages == null || !sevenDayAverages.containsKey(skyblockApiId)) return OptionalDouble.empty();

		return OptionalDouble.of(sevenDayAverages.getDouble(skyblockApiId));
	}

	private double cheaperOf(OptionalDouble a, OptionalDouble b) {
		if (a.isPresent() && b.isPresent()) {
			return Math.min(a.getAsDouble(), b.getAsDouble());
		}
		if (a.isPresent()) {
			return a.getAsDouble();
		}
		if (b.isPresent()) {
			return b.getAsDouble();
		}
		return 0;
	}

}
