package com.xaiver.cobbleballworks.mixin;

import com.google.gson.JsonElement;
import com.xaiver.cobbleballworks.content.DisabledRecipes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Map;

/**
 * Drops the Cobblemon recipes the forge replaces before they become recipes. Overriding their
 * files does not work: mod data packs are stacked in mod-id order, and "cobblemon" sits above
 * "cobbleballworks".
 */
@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {
	@ModifyVariable(
			method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
			at = @At("HEAD"),
			argsOnly = true
	)
	private Map<ResourceLocation, JsonElement> cobbleballworks$dropReplacedRecipes(Map<ResourceLocation, JsonElement> recipes) {
		return DisabledRecipes.filter(recipes);
	}
}
