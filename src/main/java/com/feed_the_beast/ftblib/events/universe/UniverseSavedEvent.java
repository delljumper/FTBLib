package com.feed_the_beast.ftblib.events.universe;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

/**
 * @author LatvianModder
 */
public class UniverseSavedEvent extends UniverseEvent
{
	private NBTTagCompound data;

	public UniverseSavedEvent(NBTTagCompound nbt)
	{
		data = nbt;
	}

	public void setData(ResourceLocation id, NBTTagCompound nbt)
	{
		data.setTag(id.toString(), nbt);
	}
}