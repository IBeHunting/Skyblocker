package de.hysky.skyblocker.skyblock.tabhud.widget.element;

import de.hysky.skyblocker.skyblock.tabhud.widget.ElementBasedWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

/**
 * Element with one piece of text left-aligned and another right-aligned against the
 * parent widget's packed width (e.g. a label on the left, a value on the right).
 */
public class LeftRightTextElement extends Element {
	private static final int GAP = PAD_L * 3;

	private final Component left;
	private final Component right;

	public LeftRightTextElement(Component left, Component right) {
		this.left = left;
		this.right = right;

		this.width = txtRend.width(left) + GAP + txtRend.width(right);
		this.height = txtRend.lineHeight;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int x, int y) {
		graphics.text(txtRend, left, x + PAD_L, y, CommonColors.WHITE, false);

		// getParent() is only valid once the widget has packed (i.e. during/after rendering, not at construction time).
		int contentRight = x + getParent().getWidth() - ElementBasedWidget.BORDER_SZE_W - ElementBasedWidget.BORDER_SZE_E;
		int rightX = contentRight - PAD_S - txtRend.width(right);
		graphics.text(txtRend, right, rightX, y, CommonColors.WHITE, false);
	}
}
