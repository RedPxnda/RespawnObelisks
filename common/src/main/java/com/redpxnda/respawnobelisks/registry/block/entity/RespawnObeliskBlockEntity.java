package com.redpxnda.respawnobelisks.registry.block.entity;

import com.mojang.logging.LogUtils;
import com.redpxnda.respawnobelisks.config.ObeliskCoreConfig;
import com.redpxnda.respawnobelisks.config.TrustedPlayersConfig;
import com.redpxnda.respawnobelisks.data.listener.ObeliskCore;
import com.redpxnda.respawnobelisks.data.listener.ObeliskCore.*;
import com.redpxnda.respawnobelisks.data.listener.ObeliskInteraction;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.redpxnda.respawnobelisks.registry.block.entity.theme.ObeliskThemeData;
import com.redpxnda.respawnobelisks.util.CoreUtils;
import com.redpxnda.respawnobelisks.util.ObeliskInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

import static com.redpxnda.respawnobelisks.registry.block.RespawnObeliskBlock.WILD;

public class RespawnObeliskBlockEntity extends BlockEntity implements GameEventListener {
    protected final List<Consumer<CompoundTag>> loadConsumers = new ArrayList<>();
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Random RANDOM = new Random();

    private Instance coreItem;
    private BlockPositionSource source;
    private boolean hasLimboEntity;
    public ObeliskThemeData themeData = null;
    private long lastRespawn;
    private long lastCharge;
    public boolean hasRandomCharge = true;
    private String obeliskName = "";
    private Component obeliskNameComponent = null;
    public final Map<UUID, ObeliskInventory> storedItems = new HashMap<>();
    public boolean hasStoredItems = false;
    public boolean hasTeleportingEntity = false;
    protected final List<String> themes = new ArrayList<>();

    public RespawnObeliskBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModRegistries.RESPAWN_OBELISK_BE.get(), pos, blockState);
        coreItem = Instance.EMPTY;
        setupRandomCharge();
        source = new BlockPositionSource(this.getBlockPos());
        lastRespawn = -100;
        lastCharge = -100;
        hasLimboEntity = false;
        themes.add("defaultCharge");
        themes.add("defaultDeplete");
        themes.add("defaultRunes");
    }

    public void setupRandomCharge() {
        if (hasRandomCharge && getBlockState().getValue(WILD) && RANDOM.nextInt(100) < ObeliskCoreConfig.wildCoreChance) {
            this.coreItem = ObeliskCoreConfig.getDefaultCore().getDefaultInstance();
            double charge = (RANDOM.nextInt(ObeliskCoreConfig.wildMaxCharge-ObeliskCoreConfig.wildMinCharge)+ObeliskCoreConfig.wildMinCharge) / 100f;
            int maxCharge = RANDOM.nextInt(ObeliskCoreConfig.wildMaxMaxCharge-ObeliskCoreConfig.wildMinMaxCharge)+ObeliskCoreConfig.wildMinMaxCharge;
            setCharge(null, Math.round(charge*maxCharge));
            setMaxCharge(null, maxCharge);
        }
        hasRandomCharge = false;
    }

    public boolean isPlayerTrusted(String username) {
        if (!CoreUtils.hasInteraction(coreItem, ObeliskInteraction.PROTECT)) return true;
        if (TrustedPlayersConfig.enablePlayerTrust && this.coreItem.stack().getOrCreateTag().contains("RespawnObeliskData")) {
            CompoundTag obeliskData = this.coreItem.stack().getTag().getCompound("RespawnObeliskData");
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
    public long getGameTime() {
        if (level == null)
            return -100;
        return level.getGameTime();
    }
    public void setLastCharge(long lastCharge) {
        this.lastCharge = lastCharge;
    }
    public double getCharge(@Nullable Player player) {
        if (coreItem.isEmpty() || (coreItem.core().alwaysRequiresPlayer && player == null)) return 0;
        return coreItem.core().chargeProvider.apply(player, coreItem.stack(), this);
    }
    public double getMaxCharge(@Nullable Player player) {
        if (coreItem.isEmpty() || (coreItem.core().alwaysRequiresPlayer && player == null)) return 0;
        return coreItem.core().maxChargeProvider.apply(player, coreItem.stack(), this);
    }
    public void setMaxCharge(@Nullable Player player, double amnt) {
        if (coreItem.isEmpty() || (coreItem.core().alwaysRequiresPlayer && player == null)) return;
        coreItem.core().maxChargeSetter.call(amnt, player, coreItem.stack(), this);
    }
    public CompoundTag getItemNbt() {
        return coreItem.stack().save(new CompoundTag());
    }
    public CompoundTag getItemTag() {
        return coreItem.stack().getOrCreateTag();
    }
    public void setItemFromNbt(CompoundTag tag, ObeliskCore core) {
        coreItem = new Instance(ItemStack.of(tag), core);
    }
    public void setCharge(Player player, double charge) {
        if (coreItem.isEmpty() || (coreItem.core().alwaysRequiresPlayer && player == null)) return;
        coreItem.core().chargeSetter.call(charge, player, coreItem.stack(), this);
    }
    public void setCoreInstance(ItemStack stack, ObeliskCore core) {
        coreItem = new Instance(stack, core);
    }
    public void setCoreInstance(Instance instance) {
        coreItem = instance;
    }
    public ItemStack getItemStack() {
        return coreItem.stack();
    }
    public Instance getCoreInstance() {
        return coreItem;
    }

    public void chargeAndAnimate(Player player, double amnt) {
        boolean isNegative = amnt < 0;
        if (hasLevel()) {
            if (isNegative) setLastRespawn(level.getGameTime());
            else setLastCharge(level.getGameTime());
        }
        increaseCharge(player, amnt);
    }
    public void decreaseCharge(Player player, double amnt) {
        this.setCharge(player, Mth.clamp(this.getCharge(player) - amnt, 0, this.getMaxCharge(player)));
        this.syncWithClient();
    }

    public void increaseCharge(Player player, double amnt) {
        this.setCharge(player, Mth.clamp(this.getCharge(player) + amnt, 0, this.getMaxCharge(player)));
        this.syncWithClient();
    }

    public void restoreSavedItems(Player player) {
        ObeliskInventory inv = storedItems.get(player.getUUID());
        if (inv == null) return;
        if (!inv.isItemsEmpty()) {
            inv.items.forEach(i -> {
                if (!i.isEmpty()) player.getInventory().placeItemBackInInventory(i);
            });
            inv.items.clear();
        }
        if (!inv.isArmorEmpty()) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (slot.getType().equals(EquipmentSlot.Type.ARMOR)) {
                    if (inv.armor.size() <= slot.getIndex())
                        continue;
                    if (player.getItemBySlot(slot).isEmpty())
                        player.setItemSlot(slot, inv.armor.get(slot.getIndex()));
                    else
                        player.getInventory().placeItemBackInInventory(inv.armor.get(slot.getIndex()));
                }
            }
            inv.armor.clear();
        }
        if (!inv.isOffhandEmpty()) {
            if (player.getItemBySlot(EquipmentSlot.OFFHAND).isEmpty())
                player.setItemSlot(EquipmentSlot.OFFHAND, inv.offhand.get(0));
            else
                player.getInventory().placeItemBackInInventory(inv.offhand.get(0));
            inv.offhand.clear();
        }
        if (inv.isXpEmpty()) {
            player.giveExperiencePoints(inv.xp);
            inv.xp = 0;
        }
        syncWithClient();
    }

    public void syncWithClient() {
        if (level == null || level.isClientSide) return;
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
        this.hasRandomCharge = tag.contains("RandomCharge") && tag.getBoolean("RandomCharge");
        if (!hasRandomCharge) {
            if (tag.getCompound("Item").contains("id")) {
                String str = tag.getCompound("Item").getString("id");
                if (str.equals("respawnobelisks:obelisk_core_nether") || str.equals("respawnobelisks:obelisk_core_end"))
                    tag.getCompound("Item").putString("id", "respawnobelisks:obelisk_core");

                LOGGER.warn("Respawn Obelisk at \"{}\" had an old core. Updating!", this.getBlockPos());
                ItemStack stack = ItemStack.of(tag.getCompound("Item"));
                this.coreItem = stack == ItemStack.EMPTY ? Instance.EMPTY : new Instance(stack, ObeliskCore.ANCIENT_CORE);
            } else
                this.coreItem = Instance.CODEC.parse(NbtOps.INSTANCE, tag.getCompound("Item")).getOrThrow(false, s -> {
                    LOGGER.error("Failed to parse Obelisk's 'Item'. " + s);
                });
        } else
            this.hasRandomCharge = false;
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
        this.themes.clear();
        if (!tag.contains("Themes", 9)) {
            this.themes.add("defaultCharge");
            this.themes.add("defaultDeplete");
            this.themes.add("defaultRunes");
        } else
            tag.getList("Themes", 8).forEach(t -> this.themes.add(t.getAsString()));
        loadConsumers.forEach(c -> c.accept(tag));
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
        tag.putBoolean("RandomCharge", hasRandomCharge);
        tag.put("Item", Instance.CODEC.encodeStart(NbtOps.INSTANCE, coreItem).getOrThrow(true, s -> LOGGER.error("Failed to save Obelisk's 'Item'. " + s)));
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
        ListTag list = new ListTag();
        themes.forEach(t -> list.add(StringTag.valueOf(t)));
        tag.put("Themes", list);
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
        if (this.coreItem.stack().getOrCreateTag().contains("display")) {
            CompoundTag displayData = this.coreItem.stack().getTag().getCompound("display");
            if (displayData.contains("Name")) {
                obeliskName = displayData.getString("Name");
                return;
            }
        }
        obeliskName = "";
    }

    public void checkLimbo(boolean shouldSync) {
        if (this.coreItem.stack().getOrCreateTag().contains("RespawnObeliskData")) {
            CompoundTag obeliskData = this.coreItem.stack().getTag().getCompound("RespawnObeliskData");
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
    public boolean handleEventsImmediately() {
        return true;
    }

    @Override
    public PositionSource getListenerSource() {
        return source;
    }

    @Override
    public int getListenerRadius() {
        return 8;
    }

    @Override
    public boolean handleGameEvent(ServerLevel serverLevel, GameEvent.Message message) {
        if (this.isRemoved() || coreItem.isEmpty()) return false;
        boolean result = false;
        for (ObeliskInteraction interaction : ObeliskInteraction.EVENT_INTERACTIONS.getOrDefault(message.gameEvent(), Map.of()).values()) {
            if (coreItem.core().interactions.contains(interaction.id)) {
                boolean bl = interaction.eventHandler.apply(this, message);
                result = !result ? bl : result;
            }
        }
        return result;
    }
}
