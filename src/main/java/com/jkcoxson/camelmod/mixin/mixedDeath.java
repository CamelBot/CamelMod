package com.jkcoxson.camelmod.mixin;


import com.google.gson.JsonObject;
import com.jkcoxson.camelmod.tcamelp;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageRecord;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DamageTracker.class)
public abstract class mixedDeath {

    @Shadow public abstract Text getDeathMessage();

    @Shadow @Final private List<DamageRecord> recentDamage;

    @Shadow @Nullable protected abstract DamageRecord getBiggestFall();

    @Shadow @Final private LivingEntity entity;

    @Shadow protected abstract String getFallDeathSuffix(DamageRecord damageRecord);

    @Inject(method = "getDeathMessage", at = @At("RETURN"))
    public void onDeath(CallbackInfoReturnable<Text> cir) {
        String toSend = "";
        if (this.recentDamage.isEmpty()) {
            toSend = new TranslatableText("death.attack.generic", new Object[]{this.entity.getDisplayName()}).getString();
        } else {
            DamageRecord damageRecord = this.getBiggestFall();
            DamageRecord damageRecord2 = (DamageRecord)this.recentDamage.get(this.recentDamage.size() - 1);
            Text text = damageRecord2.getAttackerName();
            Entity entity = damageRecord2.getDamageSource().getAttacker();
            Object text9;
            if (damageRecord != null && damageRecord2.getDamageSource() == DamageSource.FALL) {
                Text text2 = damageRecord.getAttackerName();
                if (damageRecord.getDamageSource() != DamageSource.FALL && damageRecord.getDamageSource() != DamageSource.OUT_OF_WORLD) {
                    if (text2 == null || text != null && text2.equals(text)) {
                        if (text != null) {
                            ItemStack itemStack2 = entity instanceof LivingEntity ? ((LivingEntity)entity).getMainHandStack() : ItemStack.EMPTY;
                            if (!itemStack2.isEmpty() && itemStack2.hasCustomName()) {
                                text9 = new TranslatableText("death.fell.finish.item", new Object[]{this.entity.getDisplayName(), text, itemStack2.toHoverableText()});
                            } else {
                                text9 = new TranslatableText("death.fell.finish", new Object[]{this.entity.getDisplayName(), text});
                            }
                        } else {
                            text9 = new TranslatableText("death.fell.killer", new Object[]{this.entity.getDisplayName()});
                        }
                    } else {
                        Entity entity2 = damageRecord.getDamageSource().getAttacker();
                        ItemStack itemStack = entity2 instanceof LivingEntity ? ((LivingEntity)entity2).getMainHandStack() : ItemStack.EMPTY;
                        if (!itemStack.isEmpty() && itemStack.hasCustomName()) {
                            text9 = new TranslatableText("death.fell.assist.item", new Object[]{this.entity.getDisplayName(), text2, itemStack.toHoverableText()});
                        } else {
                            text9 = new TranslatableText("death.fell.assist", new Object[]{this.entity.getDisplayName(), text2});
                        }
                    }
                } else {
                    text9 = new TranslatableText("death.fell.accident." + this.getFallDeathSuffix(damageRecord), new Object[]{this.entity.getDisplayName()});
                }
            } else {
                text9 = damageRecord2.getDamageSource().getDeathMessage(this.entity);
            }

            Text something = (Text) text9;
            toSend = something.getString();
        }

        JsonObject toJson = new JsonObject();
        toJson.addProperty("packet","death");
        toJson.addProperty("message",toSend);
        tcamelp.Yeet(toJson.toString());
    }
}



