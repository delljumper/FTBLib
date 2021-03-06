package com.feed_the_beast.ftblib.lib.gui;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftblib.lib.util.text_components.TextComponentCountdown;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ExtendedTextField extends TextField
{
	public ITextComponent textComponent;
	private List<GuiBase.PositionedTextData> textData;
	private long lastUpdate = -1L;

	public ExtendedTextField(GuiBase gui, int x, int y, int w, int h, ITextComponent t, int flags)
	{
		super(gui, x, y, w, h, "", flags);
		textComponent = t;
		setTitle("");
	}

	public ExtendedTextField(GuiBase gui, int x, int y, int w, int h, ITextComponent t)
	{
		this(gui, x, y, w, h, t, 0);
	}

	@Override
	public ExtendedTextField setTitle(String txt)
	{
		lastUpdate = -1L;

		if (textComponent != null)
		{
			for (ITextComponent component : textComponent)
			{
				if (component instanceof TextComponentCountdown)
				{
					lastUpdate = 0L;
				}
			}

			super.setTitle(textComponent.getFormattedText());
			textData = gui.createDataFrom(textComponent, ClientUtils.MC.fontRenderer, width);
		}

		return this;
	}

	@Nullable
	private GuiBase.PositionedTextData getDataAtMouse()
	{
		int ax = getAX();
		int ay = getAY();

		for (GuiBase.PositionedTextData data : textData)
		{
			if (gui.isMouseOver(data.posX + ax, data.posY + ay, data.width, data.height))
			{
				return data;
			}
		}

		return null;
	}

	@Override
	public void addMouseOverText(List<String> list)
	{
		GuiBase.PositionedTextData data = getDataAtMouse();

		if (data != null && data.hoverEvent != null) //TODO: Special handling for each data.hoverEvent.getAction()
		{
			Collections.addAll(list, data.hoverEvent.getValue().getFormattedText().split("\n"));
		}
	}

	@Override
	public boolean mousePressed(MouseButton button)
	{
		if (gui.isMouseOver(this))
		{
			GuiBase.PositionedTextData data = getDataAtMouse();

			if (data != null && data.clickEvent != null && gui.onClickEvent(data.clickEvent))
			{
				GuiHelper.playClickSound();
				return true;
			}
		}

		return false;
	}

	@Override
	public void renderWidget()
	{
		if (lastUpdate != -1L)
		{
			long ms = System.currentTimeMillis();

			if (lastUpdate <= ms)
			{
				lastUpdate = ms + 500L;
				setTitle("");
				getParentPanel().refreshWidgets();
			}
		}

		super.renderWidget();
	}
}