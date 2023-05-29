package com.redpxnda.respawnobelisks.data.listener;

import com.redpxnda.nucleus.datapack.references.Reference;
import com.redpxnda.nucleus.datapack.references.block.BlockEntityReference;
import com.redpxnda.nucleus.datapack.references.entity.PlayerReference;
import com.redpxnda.nucleus.datapack.references.item.ItemStackReference;
import com.redpxnda.nucleus.datapack.references.storage.ComponentReference;
import com.redpxnda.nucleus.datapack.references.tag.CompoundTagReference;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;

@SuppressWarnings("unused")
public class ROBEReference extends BlockEntityReference<RespawnObeliskBlockEntity> {
    static { Reference.register(ROBEReference.class); }

    public ROBEReference(RespawnObeliskBlockEntity instance) {
        super(instance);
    }

    public void chargeAndAnimate(PlayerReference player, double amnt) {
        instance.chargeAndAnimate(player.instance, amnt);
    }
    public void chargeAndAnimate(double amnt) {
        instance.chargeAndAnimate(null, amnt);
    }

    // Generated from RespawnObeliskBlockEntity::updateHasSavedItems
    public void updateHasSavedItems() {
        instance.updateHasSavedItems();
    }

    // Generated from RespawnObeliskBlockEntity::hasLimboEntity
    public boolean hasLimboEntity() {
        return instance.hasLimboEntity();
    }

    // Generated from RespawnObeliskBlockEntity::getCharge
    public double getCharge(PlayerReference param0) {
        return instance.getCharge(param0.instance);
    }
    public double getCharge() {
        return instance.getCharge(null);
    }

    // Generated from RespawnObeliskBlockEntity::setCharge
    public void setCharge(PlayerReference param0, double param1) {
        instance.setCharge(param0.instance, param1);
    }
    public void setCharge(double param1) {
        instance.setCharge(null, param1);
    }

    // Generated from RespawnObeliskBlockEntity::syncWithClient
    public void syncWithClient() {
        instance.syncWithClient();
    }

    // Generated from RespawnObeliskBlockEntity::getMaxCharge
    public double getMaxCharge(PlayerReference reference) {
        return instance.getMaxCharge(reference.instance);
    }
    public double getMaxCharge() {
        return instance.getMaxCharge(null);
    }

    public void setMaxCharge(PlayerReference param0, double param1) {
        instance.setMaxCharge(param0.instance, param1);
    }
    public void setMaxCharge(double param1) {
        instance.setMaxCharge(null, param1);
    }

    public void setCoreInstance(ItemStackReference stack, ObeliskCore core) {
        instance.setCoreInstance(stack.instance, core);
    }
    public void setCoreInstance(ObeliskCore.Instance inst) {
        instance.setCoreInstance(inst);
    }

    // Generated from RespawnObeliskBlockEntity::checkLimbo
    public void checkLimbo(boolean param0) {
        instance.checkLimbo(param0);
    }

    // Generated from RespawnObeliskBlockEntity::getObeliskName
    public String getObeliskName() {
        return instance.getObeliskName();
    }

    // Generated from RespawnObeliskBlockEntity::setLastCharge
    public void setLastCharge(long param0) {
        instance.setLastCharge(param0);
    }

    // Generated from RespawnObeliskBlockEntity::getItemNbt
    public CompoundTagReference getItemNbt() {
        return new CompoundTagReference(instance.getItemNbt());
    }

    // Generated from RespawnObeliskBlockEntity::getLastRespawn
    public long getLastRespawn() {
        return instance.getLastRespawn();
    }

    // Generated from RespawnObeliskBlockEntity::setLastRespawn
    public void setLastRespawn(long param0) {
        instance.setLastRespawn(param0);
    }

    // Generated from RespawnObeliskBlockEntity::setItemNbt
    public void setItemFromNbt(CompoundTagReference tag, ObeliskCore core) {
        instance.setItemFromNbt(tag.instance, core);
    }

    // Generated from RespawnObeliskBlockEntity::isPlayerTrusted
    public boolean isPlayerTrusted(String param0) {
        return instance.isPlayerTrusted(param0);
    }

    // Generated from RespawnObeliskBlockEntity::getLastCharge
    public long getLastCharge() {
        return instance.getLastCharge();
    }

    // Generated from RespawnObeliskBlockEntity::restoreSavedItems
    public void restoreSavedItems(PlayerReference param0) {
        instance.restoreSavedItems(param0.instance);
    }

    // Generated from RespawnObeliskBlockEntity::updateObeliskName
    public void updateObeliskName() {
        instance.updateObeliskName();
    }

    // Generated from RespawnObeliskBlockEntity::decreaseCharge
    public void decreaseCharge(PlayerReference param0, double param1) {
        instance.decreaseCharge(param0.instance, param1);
    }
    public void decreaseCharge(double param1) {
        instance.decreaseCharge(null, param1);
    }

    // Generated from RespawnObeliskBlockEntity::increaseCharge
    public void increaseCharge(PlayerReference param0, double param1) {
        instance.increaseCharge(param0.instance, param1);
    }
    public void increaseCharge(double param1) {
        instance.increaseCharge(null, param1);
    }

    // Generated from RespawnObeliskBlockEntity::getItemStack
    public ItemStackReference getItemStack() {
        return new ItemStackReference(instance.getItemStack());
    }

    // Generated from RespawnObeliskBlockEntity::getObeliskNameComponent
    public ComponentReference<?> getObeliskNameComponent() {
        return new ComponentReference<>(instance.getObeliskNameComponent());
    }
}
