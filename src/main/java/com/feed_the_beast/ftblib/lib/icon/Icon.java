package com.feed_the_beast.ftblib.lib.icon;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.gui.Widget;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public abstract class Icon
{
	public static final Color4I EMPTY = new Color4I(255, 255, 255, 255)
	{
		@Override
		public boolean isEmpty()
		{
			return true;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public void draw(int x, int y, int w, int h, Color4I col)
		{
		}

		@Override
		@SideOnly(Side.CLIENT)
		public void draw(Widget widget, Color4I col)
		{
		}

		@Override
		public MutableColor4I mutable()
		{
			return new MutableColor4I.None();
		}
	};

	public static Icon getIcon(JsonElement json)
	{
		if (json.isJsonNull())
		{
			return EMPTY;
		}
		else if (json.isJsonObject())
		{
			JsonObject o = json.getAsJsonObject();

			if (o.has("id"))
			{
				switch (o.get("id").getAsString())
				{
					case "loading":
						return LoadingIcon.INSTANCE;
					case "color":
					{
						Color4I color = Color4I.fromJson(o.get("color"));
						return (o.has("mutable") && o.get("mutable").getAsBoolean()) ? color.mutable() : color;
					}
					case "border":
						return getIcon(o.get("parent")).withBorder(o.has("border") ? o.get("border").getAsInt() : 0);
					case "tint":
						return getIcon(o.get("parent")).withTint(Color4I.fromJson(o.get("color")));
					case "animation":
					{
						List<Icon> icons = new ArrayList<>();

						for (JsonElement e : o.get("icons").getAsJsonArray())
						{
							icons.add(getIcon(e));
						}

						IconAnimation list = new IconAnimation(icons);

						if (o.has("timer"))
						{
							list.timer = o.get("timer").getAsLong();
						}

						return list;
					}
					case "outline":
					{
						Icon icon = EMPTY;
						Color4I outline = EMPTY;
						boolean roundEdges = false;

						if (o.has("icon"))
						{
							icon = getIcon(o.get("icon"));
						}

						if (o.has("color"))
						{
							outline = Color4I.fromJson(o.get("color"));
						}

						if (o.has("round_edges"))
						{
							roundEdges = o.get("round_edges").getAsBoolean();
						}

						return icon.withOutline(outline, roundEdges);
					}
					case "bullet":
					{
						return new BulletIcon().setColor(o.has("color") ? Color4I.fromJson(o.get("color")) : EMPTY);
					}
				}
			}
		}
		else if (json.isJsonArray())
		{
			List<Icon> list = new ArrayList<>();

			for (JsonElement e : json.getAsJsonArray())
			{
				list.add(getIcon(e));
			}

			return CombinedIcon.getCombined(list);
		}

		String s = json.getAsString();

		if (s.isEmpty())
		{
			return EMPTY;
		}

		Icon icon = IconPresets.MAP.get(s);
		return icon == null ? getIcon(s) : icon;
	}

	public static Icon getIcon(String id)
	{
		if (id.isEmpty())
		{
			return EMPTY;
		}
		else if (id.charAt(0) == '#')
		{
			return Color4I.fromJson(new JsonPrimitive(id));
		}
		else if (id.equals("loading"))
		{
			return LoadingIcon.INSTANCE;
		}
		else if (id.startsWith("item:"))
		{
			return ItemIcon.getItemIcon(id.substring(5));
		}
		else if (id.startsWith("http:") || id.startsWith("https:"))
		{
			return new URLImageIcon(id, 0D, 0D, 1D, 1D);
		}

		if (!id.endsWith(".png"))
		{
			return new AtlasSpriteIcon(new ResourceLocation(id));
		}

		return new ImageIcon(id, 0D, 0D, 1D, 1D);
	}

	public boolean isEmpty()
	{
		return false;
	}

	@SideOnly(Side.CLIENT)
	public ITextureObject bindTexture()
	{
		return ClientUtils.bindTexture(ImageIcon.MISSING_IMAGE);
	}

	@SideOnly(Side.CLIENT)
	public abstract void draw(int x, int y, int w, int h, Color4I col);

	@SideOnly(Side.CLIENT)
	public final void draw(int x, int y, int w, int h)
	{
		draw(x, y, w, h, EMPTY);
	}

	@SideOnly(Side.CLIENT)
	public void draw(Widget widget, Color4I col)
	{
		draw(widget.getAX(), widget.getAY(), widget.width, widget.height, col);
	}

	@SideOnly(Side.CLIENT)
	public final void draw(Widget widget)
	{
		draw(widget, EMPTY);
	}

	public JsonElement getJson()
	{
		return JsonNull.INSTANCE;
	}

	public Icon withUV(double u0, double v0, double u1, double v1)
	{
		return this;
	}

	public Icon withUVfromCoords(int x, int y, int w, int h, int tw, int th)
	{
		return withUV(x / (double) tw, y / (double) th, (x + w) / (double) tw, (y + h) / (double) th);
	}

	public final Icon combineWith(Icon icon)
	{
		if (icon.isEmpty())
		{
			return this;
		}
		else if (isEmpty())
		{
			return icon;
		}

		return new CombinedIcon(this, icon);
	}

	public final Icon combineWith(Icon... icons)
	{
		if (icons.length == 0)
		{
			return this;
		}
		else if (icons.length == 1)
		{
			if (isEmpty())
			{
				return icons[0];
			}
			else if (icons[0].isEmpty())
			{
				return this;
			}

			return new CombinedIcon(this, icons[0]);
		}

		List<Icon> list = new ArrayList<>(icons.length + 1);
		list.add(this);

		for (Icon i : icons)
		{
			list.add(i);
		}

		return CombinedIcon.getCombined(list);
	}

	public final Icon withOutline(Color4I color, boolean roundEdges)
	{
		if (color.isEmpty())
		{
			return withBorder(1);
		}

		return new IconWithOutline(this, color, roundEdges);
	}

	public final Icon withBorder(int border)
	{
		return border == 0 ? this : new IconWithBorder(this, border);
	}

	public Icon withTint(Color4I color)
	{
		return (isEmpty() || color == Color4I.WHITE) ? this : new IconWithTint(this, color);
	}
}