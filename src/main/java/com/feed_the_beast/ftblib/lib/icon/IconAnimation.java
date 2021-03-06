package com.feed_the_beast.ftblib.lib.icon;

import com.feed_the_beast.ftblib.lib.math.MathUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author LatvianModder
 */
public class IconAnimation extends Icon
{
	public static Icon fromIngredient(Ingredient ingredient)
	{
		List<Icon> icons = new ArrayList<>();

		for (ItemStack stack : ingredient.getMatchingStacks())
		{
			if (!stack.isEmpty())
			{
				icons.add(ItemIcon.getItemIcon(stack));
			}
		}

		return icons.isEmpty() ? EMPTY : icons.size() == 1 ? icons.get(0) : new IconAnimation(icons);
	}

	public final List<Icon> list;
	private Icon current = Icon.EMPTY;
	public long timer = 1000L;

	public IconAnimation(Collection<Icon> l)
	{
		list = new ArrayList<>(l.size());

		for (Icon o : l)
		{
			if (!o.isEmpty())
			{
				list.add(o);
			}
		}

		if (!list.isEmpty())
		{
			current = list.get(0);
		}
	}

	@Override
	public boolean isEmpty()
	{
		return list.isEmpty();
	}

	public int getItemCount()
	{
		return list.size();
	}

	public Icon getObject(int index)
	{
		if (index < 0 || index >= list.size())
		{
			return current;
		}

		return list.get(index);
	}

	public void setIndex(int i)
	{
		current = list.get(MathUtils.wrap(i, list.size()));
	}

	@Override
	public void draw(int x, int y, int w, int h, Color4I col)
	{
		if (current != null)
		{
			current.draw(x, y, w, h, col);
		}

		if (!list.isEmpty())
		{
			setIndex((int) (System.currentTimeMillis() / timer));
		}
	}

	@Override
	public JsonElement getJson()
	{
		JsonObject json = new JsonObject();
		json.addProperty("id", "animation");

		if (timer != 1000L)
		{
			json.addProperty("timer", timer);
		}

		JsonArray array = new JsonArray();

		for (Icon o : list)
		{
			array.add(o.getJson());
		}

		json.add("icons", array);
		return json;
	}
}