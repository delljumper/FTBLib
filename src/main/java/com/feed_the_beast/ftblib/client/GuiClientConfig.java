package com.feed_the_beast.ftblib.client;

import com.feed_the_beast.ftblib.FTBLibFinals;
import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.data.SharedClientData;
import com.feed_the_beast.ftblib.lib.gui.Button;
import com.feed_the_beast.ftblib.lib.gui.GuiBase;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.SimpleTextButton;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiButtonListBase;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiLoading;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiMessageDialog;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class GuiClientConfig extends GuiButtonListBase
{
	private class GuiCustomConfig extends GuiConfig
	{
		public GuiCustomConfig(String modid, String title)
		{
			super(ClientUtils.MC.currentScreen, modid, title);
		}

		@Override
		protected void actionPerformed(GuiButton button)
		{
			if (button.id == 2000)
			{
				boolean flag = true;
				try
				{
					if ((configID != null || parentScreen == null || !(parentScreen instanceof GuiConfig)) && (entryList.hasChangedEntry(true)))
					{
						boolean requiresMcRestart = entryList.saveConfigElements();

						ConfigChangedEvent event = new ConfigChangedEvent.OnConfigChangedEvent(modID, configID, isWorldRunning, requiresMcRestart);
						MinecraftForge.EVENT_BUS.post(event);
						if (!event.getResult().equals(Event.Result.DENY))
						{
							MinecraftForge.EVENT_BUS.post(new ConfigChangedEvent.PostConfigChangedEvent(modID, configID, isWorldRunning, requiresMcRestart));
						}

						if (requiresMcRestart)
						{
							flag = false;
							mc.displayGuiScreen(new GuiMessageDialog(parentScreen, "fml.configgui.gameRestartTitle", new TextComponentString(StringUtils.translate("fml.configgui.gameRestartRequired")), "fml.configgui.confirmRestartMessage"));
						}

						if (parentScreen instanceof GuiConfig)
						{
							((GuiConfig) parentScreen).needsRefresh = true;
						}
					}
				}
				catch (Throwable e)
				{
					FMLLog.log.error("Error performing GuiConfig action:", e);
				}

				if (flag)
				{
					mc.displayGuiScreen(parentScreen);
				}
			}
			else
			{
				super.actionPerformed(button);
			}
		}
	}

	private class ButtonClientConfig extends SimpleTextButton
	{
		private final String modId;

		public ButtonClientConfig(GuiBase gui, ClientConfig config)
		{
			super(gui, 0, 0, config.name.getFormattedText(), config.icon);
			modId = config.id;
		}

		@Override
		public void onClicked(MouseButton button)
		{
			GuiHelper.playClickSound();
			ClientUtils.MC.displayGuiScreen(new GuiCustomConfig(modId, getTitle()));
		}
	}

	public GuiClientConfig()
	{
		setTitle(StringUtils.translate("sidebar_button.ftblib.settings"));
	}

	@Override
	public void addButtons(Panel panel)
	{
		List<Button> buttons = new ArrayList<>();

		for (ClientConfig config : FTBLibModClient.CLIENT_CONFIG_MAP.values())
		{
			buttons.add(new ButtonClientConfig(gui, config));
		}

		buttons.sort((o1, o2) -> o1.getTitle().compareToIgnoreCase(o2.getTitle()));

		buttons.add(0, new SimpleTextButton(gui, 0, 0, StringUtils.translate("sidebar_button"), Icon.getIcon(FTBLibFinals.MOD_ID + ":textures/gui/teams.png"))
		{
			@Override
			public void onClicked(MouseButton button)
			{
				GuiHelper.playClickSound();
				new GuiSidebarButtonConfig().openGui();
			}
		});

		if (SharedClientData.INSTANCE.optionalServerMods().contains(FTBLibFinals.MOD_ID))
		{
			buttons.add(0, new SimpleTextButton(gui, 0, 0, StringUtils.translate("player_config"), GuiIcons.SETTINGS_RED)
			{
				@Override
				public void onClicked(MouseButton button)
				{
					GuiHelper.playClickSound();
					new GuiLoading().openGui();
					ClientUtils.execClientCommand("/ftb my_settings");
				}
			});
		}

		panel.addAll(buttons);
	}

	@Override
	public void onClosed()
	{
		FTBLibModClient.saveSidebarButtonConfig();
	}
}