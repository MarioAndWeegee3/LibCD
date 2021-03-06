package io.github.cottonmc.libcd.mixin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.CookingRecipeSerializer;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(CookingRecipeSerializer.class)
public class MixinCookingRecipeSerializer {
	@Shadow @Final private int cookingTime;

	private MixinCookingRecipeFactory invoker;

	//TODO: we need ATs...
	@Inject(method = "<init>", at = @At("RETURN"))
	private void saveInvoker(@Coerce Object invoker, int cookingTime, CallbackInfo info) {
		this.invoker = (MixinCookingRecipeFactory) invoker;
	}

	@Inject(method = "read(Lnet/minecraft/util/Identifier;Lcom/google/gson/JsonObject;)Lnet/minecraft/recipe/AbstractCookingRecipe;", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/JsonHelper;getString(Lcom/google/gson/JsonObject;Ljava/lang/String;)Ljava/lang/String;", ordinal = 0),
			cancellable = true, locals = LocalCapture.CAPTURE_FAILEXCEPTION)
	private void read(Identifier id, JsonObject json, CallbackInfoReturnable info,
					  String group, JsonElement ingElem, Ingredient ingredient) {
		JsonElement elem = json.get("result");
		if (elem instanceof JsonObject) {
			ItemStack stack = ShapedRecipe.getItemStack((JsonObject)elem);
			float experience = JsonHelper.getFloat(json, "experience", 0.0F);
			int cookingtime = JsonHelper.getInt(json, "cookingtime", this.cookingTime);
			info.setReturnValue(invoker.invokeCreate(id, group, ingredient, stack, experience, cookingtime));
		}
	}

	@Mixin(targets = "net.minecraft.recipe.CookingRecipeSerializer$RecipeFactory")
	public interface MixinCookingRecipeFactory<T extends AbstractCookingRecipe> {
		@Invoker
		T invokeCreate(Identifier id, String group, Ingredient ingredient, ItemStack stack, float experience, int cookingTime);
	}
}
