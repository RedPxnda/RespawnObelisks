package com.redpxnda.respawnobelisks.registry.block.entity;

import com.redpxnda.respawnobelisks.config.ChargeConfig;
import com.redpxnda.respawnobelisks.config.TrustedPlayersConfig;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.redpxnda.respawnobelisks.registry.block.RespawnObeliskBlock;
import com.redpxnda.respawnobelisks.util.CoreUtils;
import com.redpxnda.respawnobelisks.util.ObeliskInventory;
import foundry.veil.color.Color;
import foundry.veil.color.ColorTheme;
import foundry.veil.ui.Tooltippable;
import foundry.veil.ui.VeilUIItemTooltipDataHolder;
import foundry.veil.ui.anim.TooltipKeyframe;
import foundry.veil.ui.anim.TooltipTimeline;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.*;

public class RespawnObeliskBlockEntity extends BlockEntity implements Tooltippable {
    private List<Component> tooltip = Arrays.asList(
            Component.literal("Test!"),
            Component.literal("Testr 2!")
    );
    private static ColorTheme theme = new ColorTheme();
    static {
        theme.addColor("background", Color.VANILLA_TOOLTIP_BACKGROUND);
        theme.addColor("topBorder", Color.VANILLA_TOOLTIP_BORDER_TOP);
        theme.addColor("bottomBorder", Color.VANILLA_TOOLTIP_BORDER_BOTTOM);
    }

    private ItemStack coreItem;
    private CompoundTag playerCharges;
    private boolean hasLimboEntity;
    private long lastRespawn;
    private long lastCharge;
    private String obeliskName = "";
    private Component obeliskNameComponent = null;
    public final Map<UUID, ObeliskInventory> storedItems = new HashMap<>();
    public boolean hasStoredItems = false;
    public boolean hasTeleportingEntity = false;

    public RespawnObeliskBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModRegistries.RESPAWN_OBELISK_BE.get(), pPos, pBlockState);
        coreItem = ItemStack.EMPTY;
        playerCharges = new CompoundTag();
        lastRespawn = 0;
        lastCharge = 0;
        hasLimboEntity = false;
    }

    public boolean isPlayerTrusted(String username) {
        if (!CoreUtils.hasCapability(coreItem, CoreUtils.Capability.PROTECT)) return true;
        if (TrustedPlayersConfig.enablePlayerTrust && this.coreItem.getOrCreateTag().contains("RespawnObeliskData")) {
            CompoundTag obeliskData = this.coreItem.getTag().getCompound("RespawnObeliskData");
            if (obeliskData.contains("TrustedPlayers")) {
                ListTag list = obeliskData.getList("TrustedPlayers", 8);
                return list.contains(StringTag.valueOf(username));
            }
        }
        return true;
    }
    public Component getObeliskNameComponent() {
        return obeliskNameComponent;
    }
    public String getObeliskName() {
        return obeliskName;
    }
    public boolean hasLimboEntity() {
        return hasLimboEntity;
    }
    public long getLastRespawn() {
        return lastRespawn;
    }
    public void setLastRespawn(long lastRespawn) {
        this.lastRespawn = lastRespawn;
    }
    public long getLastCharge() {
        return lastCharge;
    }
    public void setLastCharge(long lastCharge) {
        this.lastCharge = lastCharge;
    }
    public double getCharge(@Nullable Player player) {
        if (ChargeConfig.perPlayerCharge && player != null) {
            if (!playerCharges.contains(player.getScoreboardName(), 6))
                playerCharges.putDouble(player.getScoreboardName(), 0);
            return playerCharges.getDouble(player.getScoreboardName());
        }
        if (coreItem.isEmpty()) return 0;
        return coreItem.getOrCreateTag().getCompound("RespawnObeliskData").getDouble("Charge");
    }
    public double getMaxCharge() {
        if (coreItem.isEmpty()) return 0;
        return coreItem.getOrCreateTag().getCompound("RespawnObeliskData").getDouble("MaxCharge");
    }
    public static double getCharge(CompoundTag tag) {
        return tag.getCompound("RespawnObeliskData").getDouble("Charge");
    }
    public static double getMaxCharge(CompoundTag tag) {
        return tag.getCompound("RespawnObeliskData").getDouble("MaxCharge");
    }
    public CompoundTag getItemNbt() {
        return coreItem.save(new CompoundTag());
    }
    public void setItemNbt(CompoundTag tag) {
        coreItem = ItemStack.of(tag);
    }
    public void setCharge(Player player, double charge) {
        if (ChargeConfig.perPlayerCharge) {
            playerCharges.putDouble(player.getScoreboardName(), charge);
            return;
        }
        coreItem.getOrCreateTag().getCompound("RespawnObeliskData").putDouble("Charge", charge);
    }
    public void setItem(ItemStack stack) {
        coreItem = stack;
    }
    public ItemStack getItemStack() {
        return coreItem;
    }

    public BlockEntity decreaseCharge(Player player, double amnt) {
        this.setCharge(player, Mth.clamp(this.getCharge(player) - amnt, 0, this.getMaxCharge()));
        this.syncWithClient();
        return this;
    }

    public BlockEntity increaseCharge(Player player, double amnt) {
        this.setCharge(player, Mth.clamp(this.getCharge(player) + amnt, 0, this.getMaxCharge()));
        this.syncWithClient();
        return this;
    }

    public void restoreSavedItems(Player player) {
        ObeliskInventory inv = storedItems.get(player.getUUID());
        if (inv == null) return;
        if (!inv.items.isEmpty()) {
            inv.items.forEach(i -> player.getInventory().placeItemBackInInventory(i));
            inv.items.clear();
        }
        if (!inv.armor.isEmpty()) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (slot.getType().equals(EquipmentSlot.Type.ARMOR)) {
                    if (player.getItemBySlot(slot).isEmpty())
                        player.setItemSlot(slot, inv.armor.get(slot.getIndex()));
                    else
                        player.getInventory().placeItemBackInInventory(inv.armor.get(slot.getIndex()));
                }
            }
            inv.armor.clear();
        }
        if (!inv.offhand.isEmpty()) {
            if (player.getItemBySlot(EquipmentSlot.OFFHAND).isEmpty())
                player.setItemSlot(EquipmentSlot.OFFHAND, inv.offhand.get(0));
            else
                player.getInventory().placeItemBackInInventory(inv.offhand.get(0));
            inv.offhand.clear();
        }
        syncWithClient();
    }

    public void syncWithClient() {
        if (level.isClientSide) return;
        updateHasSavedItems();
        setChanged(this.level, this.getBlockPos(), this.getBlockState());
    }

    public void updateHasSavedItems() {
        boolean isEmpty = true;
        for (ObeliskInventory inv : storedItems.values()) {
            if (!inv.isEmpty()) {
                isEmpty = false;
                break;
            }
        }
        this.hasStoredItems = !isEmpty;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.coreItem = ItemStack.of(tag.getCompound("Item"));
        this.playerCharges = tag.getCompound("PlayerCharges");
        this.lastRespawn = tag.getLong("LastRespawn");
        this.lastCharge = tag.getLong("LastCharge");
        this.hasLimboEntity = tag.getBoolean("HasLimboEntity");
        this.hasStoredItems = tag.getBoolean("HasStoredItems");
        this.obeliskName = tag.getString("Name");
        if (tag.contains("StoredItems", 10)) {
            CompoundTag stored = tag.getCompound("StoredItems");
            for (String key : stored.getAllKeys()) {
                this.storedItems.put(UUID.fromString(key), ObeliskInventory.readFromNbt(stored.getCompound(key)));
            }
        }
        this.obeliskNameComponent = Component.Serializer.fromJson(tag.getString("Name"));
        this.hasTeleportingEntity = tag.getBoolean("HasTeleportingEntity");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        this.saveData(tag, true);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        this.saveData(tag, false);
        return tag;
    }

    private void saveData(CompoundTag tag, boolean saveItems) {
        tag.put("Item", coreItem.save(new CompoundTag()));
        tag.put("PlayerCharges", playerCharges);
        tag.putLong("LastRespawn", lastRespawn);
        tag.putLong("LastCharge", lastCharge);
        tag.putBoolean("HasLimboEntity", hasLimboEntity);
        tag.putBoolean("HasStoredItems", hasStoredItems);
        tag.putString("Name", obeliskName);
        if (saveItems) {
            CompoundTag allPlayers = new CompoundTag();
            for (UUID uuid : storedItems.keySet()) {
                allPlayers.put(uuid.toString(), storedItems.get(uuid).saveToNbt());
            }
            tag.put("StoredItems", allPlayers);
        }
        tag.putBoolean("HasTeleportingEntity", hasTeleportingEntity);
    }

    protected static void setChanged(Level pLevel, BlockPos pPos, BlockState pState) {
        BlockEntity.setChanged(pLevel, pPos, pState);
        pLevel.sendBlockUpdated(pPos, pLevel.getBlockState(pPos), pState, 3);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public static void tick(Level level, BlockPos blockPos, BlockState state, RespawnObeliskBlockEntity blockEntity) {
        if (level.getGameTime() % 100 == 0) {
            if (!level.isClientSide) {
                blockEntity.checkLimbo(true);
            }
        }
    }

    public void updateObeliskName() {
        if (this.coreItem.getOrCreateTag().contains("display")) {
            CompoundTag displayData = this.coreItem.getTag().getCompound("display");
            if (displayData.contains("Name")) {
                obeliskName = displayData.getString("Name");
                return;
            }
        }
        obeliskName = "";
    }

    public void checkLimbo(boolean shouldSync) {
        if (this.coreItem.getOrCreateTag().contains("RespawnObeliskData")) {
            CompoundTag obeliskData = this.coreItem.getTag().getCompound("RespawnObeliskData");
            if (obeliskData.contains("SavedEntities")) {
                ListTag list = obeliskData.getList("SavedEntities", 10);
                for (Tag tag : list) {
                    if (
                            level instanceof ServerLevel serverLevel &&
                                    tag instanceof CompoundTag compound &&
                                    compound.contains("uuid") &&
                                    compound.contains("type") &&
                                    compound.contains("data")
                    ) {
                        Entity entity = serverLevel.getEntity(compound.getUUID("uuid"));
                        if (entity == null || !entity.isAlive()) {
                            this.hasLimboEntity = true;
                            if (shouldSync) this.syncWithClient();
                            return;
                        }
                    }
                }
            }
        }
        this.hasLimboEntity = false;
        if (shouldSync) this.syncWithClient();
    }

    @Override
    public List<Component> getTooltip() {
        return this.tooltip;
    }

    @Override
    public void setTooltip(List<Component> tooltip) {
        if (tooltip != null) this.tooltip = tooltip;
    }

    @Override
    public void addTooltip(Component tooltip) {
        if (tooltip != null) this.tooltip.add(tooltip);
    }

    @Override
    public void addTooltip(List<Component> tooltip) {
        if (tooltip != null) this.tooltip.addAll(tooltip);
    }

    @Override
    public void addTooltip(String tooltip) {
        if (tooltip != null) this.tooltip.add(Component.literal(tooltip));
    }

    @Override
    public ColorTheme getTheme() {
        return theme;
    }

    @Override
    public void setTheme(ColorTheme newTheme) {
        if (newTheme != null) theme = newTheme;
    }

    @Override
    public void setBackgroundColor(int color) {
        theme.getColors().add(0, Color.of(color));
    }

    @Override
    public void setTopBorderColor(int color) {
        theme.getColors().add(1, Color.of(color));
    }

    @Override
    public void setBottomBorderColor(int color) {
        theme.getColors().add(2, Color.of(color));
    }

    @Override
    public boolean getWorldspace() {
        return true;
    }

    @Override
    public TooltipTimeline getTimeline() {
        return new TooltipTimeline(new TooltipKeyframe[] {}, 1.0f);
    }

    @Override
    public ItemStack getStack() {
        return ItemStack.EMPTY;
    }

    @Override
    public int getTooltipWidth() {
        return 0;
    }

    @Override
    public int getTooltipHeight() {
        return 8;
    }

    @Override
    public int getTooltipXOffset() {
        return -10;
    }

    @Override
    public int getTooltipYOffset() {
        return 15;
    }

    @Override
    public List<VeilUIItemTooltipDataHolder> getItems() {
        return List.of(new VeilUIItemTooltipDataHolder(ItemStack.EMPTY, (f) -> 0f, (f) -> 0f));
    }
}
