package smartin.miapi.mixin;

import com.google.common.collect.Multimap;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.properties.AttributeProperty;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Shadow @Final @Deprecated private Item item;

    @Inject(method = "getAttributeModifiers", at = @At("RETURN"), cancellable = true)
    public void modifyAttributeModifiers(EquipmentSlot slot, CallbackInfoReturnable<Multimap<EntityAttribute, EntityAttributeModifier>> cir) {
        Multimap<EntityAttribute, EntityAttributeModifier> original = cir.getReturnValue();
        ItemStack stack = (ItemStack) (Object) this;
        if(stack.getItem() instanceof ModularItem){
            Multimap<EntityAttribute, AttributeProperty.EntityAttributeModifierHolder> toMerge = AttributeProperty.getAttributeModifiers(stack);
            toMerge.forEach((entityAttribute, entityAttributeModifier) -> {
                if(entityAttributeModifier.slot().equals(slot)){
                    original.put(entityAttribute,entityAttributeModifier.attributeModifier());
                }
            });
        }
        cir.setReturnValue(original); // Replace the original return value with your modified one
    }
}