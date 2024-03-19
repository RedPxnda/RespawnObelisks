package com.redpxnda.respawnobelisks.registry.block.entity;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.redpxnda.respawnobelisks.RespawnObelisks;
import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.data.listener.ObeliskCore;
import com.redpxnda.respawnobelisks.data.listener.ObeliskCore.Instance;
import com.redpxnda.respawnobelisks.data.listener.ObeliskInteraction;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.redpxnda.respawnobelisks.registry.block.entity.theme.ThemeLayout;
import com.redpxnda.respawnobelisks.util.CoreUtils;
import com.redpxnda.respawnobelisks.util.ObeliskInventory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.BlockPositionSource;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSource;
import net.minecraft.world.event.listener.GameEventListener;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static com.redpxnda.respawnobelisks.registry.block.RespawnObeliskBlock.WILD;
import static com.redpxnda.respawnobelisks.registry.block.entity.theme.RenderTheme.*;

public class RespawnObeliskBlockEntity extends BlockEntity implements GameEventListener {
    protected final List<Consumer<NbtCompound>> loadConsumers = new ArrayList<>();
    public static final List<Identifier> defaultThemes = List.of(defCharge, defDep, defRunes);
    private static final Logger LOGGER = RespawnObelisks.getLogger();
    private static final Random random = new Random();

    private Instance coreItem;
    private final BlockPositionSource source;
    private boolean hasLimboEntity;
    public ThemeLayout themeLayout = null;
    private long lastRespawn;
    private long lastCharge;
    public double clientCharge;
    public double clientMaxCharge;
    public boolean hasRandomCharge = true;
    private String obeliskName = "";
    private Text obeliskNameComponent = null;
    public final Map<UUID, ObeliskInventory> storedItems = new HashMap<>();
    public boolean hasStoredItems = false;
    public boolean hasTeleportingEntity = false;
    private final List<Identifier> themes = new ArrayList<>();
    public final Multimap<GlobalPos, ServerPlayerEntity> respawningPlayers = Multimaps.newMultimap(new ConcurrentHashMap<>(), HashSet::new);

    public RespawnObeliskBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModRegistries.ROBE.get(), pos, blockState);
        coreItem = Instance.EMPTY;
        setupRandomCharge();
        source = new BlockPositionSource(this.getPos());
        lastRespawn = -100;
        lastCharge = -100;
        hasLimboEntity = false;
    }

    public void setupRandomCharge() {
        if (hasRandomCharge && getCachedState().get(WILD) && random.nextInt(100) < RespawnObelisksConfig.INSTANCE.cores.wildCoreChance) {
            this.coreItem = RespawnObelisksConfig.INSTANCE.cores.getDefaultCore().getDefaultInstance();
            double charge = (random.nextInt(RespawnObelisksConfig.INSTANCE.cores.wildMaxRadiance -RespawnObelisksConfig.INSTANCE.cores.wildMinRadiance)+RespawnObelisksConfig.INSTANCE.cores.wildMinRadiance) / 100f;
            int maxCharge = random.nextInt(RespawnObelisksConfig.INSTANCE.cores.wildMaxMaxRadiancee -RespawnObelisksConfig.INSTANCE.cores.wildMinMaxRadiance)+RespawnObelisksConfig.INSTANCE.cores.wildMinMaxRadiance;
            setCharge(null, Math.round(charge*maxCharge));
            setMaxCharge(null, maxCharge);
        }
        hasRandomCharge = false;
    }

    public boolean isPlayerTrusted(String username) {
        if (!CoreUtils.hasInteraction(coreItem, ObeliskInteraction.PROTECT)) return true;
        if (RespawnObelisksConfig.INSTANCE.playerTrusting.enablePlayerTrust && this.coreItem.stack().getOrCreateNbt().contains("RespawnObeliskData")) {
            NbtCompound obeliskData = this.coreItem.stack().getNbt().getCompound("RespawnObeliskData");
            if (obeliskData.contains("TrustedPlayers")) {
                NbtList list = obeliskData.getList("TrustedPlayers", 8);
                return list.contains(NbtString.of(username));
            }
        }
        return true;
    }
    public List<Identifier> getThemes() {
        if (coreItem.isEmpty()) return List.of();
        if (themes.isEmpty()) return coreItem.core().themes();
        return themes;
    }
    public Text getObeliskNameComponent() {
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
        if (world == null)
            return -100;
        return world.getTime();
    }
    public void setLastCharge(long lastCharge) {
        this.lastCharge = lastCharge;
    }
    public double getClientCharge() {
        return clientCharge;
    }
    public double getClientMaxCharge() {
        return clientMaxCharge;
    }
    public double getCost(ServerPlayerEntity player) {
        return getCost(player, false, true, false);
    }
    public double getCost(ServerPlayerEntity player, boolean curseForced, boolean shouldConsumeCost, boolean isTeleport) {
        double cost = !shouldConsumeCost ? 0 : isTeleport ? RespawnObelisksConfig.INSTANCE.teleportation.teleportationCost : RespawnObelisksConfig.INSTANCE.radiance.respawnCost; // todo update shouldConsumeCost behavior
        ObeliskInteraction.Manager manager = new ObeliskInteraction.Manager(curseForced, shouldConsumeCost, cost, null);
        for (ObeliskInteraction i : ObeliskInteraction.RESPAWN_INTERACTIONS.get(ObeliskInteraction.Injection.START)) {
            i.respawnHandler.accept(player, this, manager);
        }
        return manager.cost;
    }
    public double getCharge(@Nullable PlayerEntity player) {
        if (world == null || world.isClient) return clientCharge;
        if (coreItem.isEmpty() || (coreItem.core().alwaysRequiresPlayer && player == null)) return 0;
        return coreItem.core().chargeProvider.apply(player, coreItem.stack(), this);
    }
    public double getMaxCharge(@Nullable PlayerEntity player) {
        if (world == null || world.isClient) return clientMaxCharge;
        if (coreItem.isEmpty() || (coreItem.core().alwaysRequiresPlayer && player == null)) return 0;
        return coreItem.core().maxChargeProvider.apply(player, coreItem.stack(), this);
    }
    public void setMaxCharge(@Nullable PlayerEntity player, double amnt) {
        if (coreItem.isEmpty() || (coreItem.core().alwaysRequiresPlayer && player == null)) return;
        coreItem.core().maxChargeSetter.call(amnt, player, coreItem.stack(), this);
    }
    public NbtCompound getItemNbt() {
        return coreItem.stack().writeNbt(new NbtCompound());
    }
    public NbtCompound getItemTag() {
        return coreItem.stack().getOrCreateNbt();
    }
    public void setItemFromNbt(NbtCompound tag, ObeliskCore core) {
        coreItem = new Instance(ItemStack.fromNbt(tag), core);
    }
    public void setCharge(PlayerEntity player, double charge) {
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

    public void chargeAndAnimate(PlayerEntity player, double amnt) {
        boolean isNegative = amnt < 0;
        if (hasWorld()) {
            if (isNegative) setLastRespawn(world.getTime());
            else {
                setLastCharge(world.getTime());
                if (player instanceof ServerPlayerEntity sp) ModRegistries.chargeCriterion.trigger(sp);
            }
        }
        increaseCharge(player, amnt);
    }
    public void decreaseCharge(PlayerEntity player, double amnt) {
        this.setCharge(player, MathHelper.clamp(this.getCharge(player) - amnt, 0, this.getMaxCharge(player)));
        this.syncWithClient();
    }

    public void increaseCharge(PlayerEntity player, double amnt) {
        this.setCharge(player, MathHelper.clamp(this.getCharge(player) + amnt, 0, this.getMaxCharge(player)));
        this.syncWithClient();
    }

    public void restoreSavedItems(PlayerEntity player) {
        ObeliskInventory inv = storedItems.get(player.getUuid());
        if (inv == null) return;
        boolean has = false;
        if (!inv.isItemsEmpty()) {
            has = true;
            inv.items.forEach(i -> {
                if (!i.isEmpty()) player.getInventory().offerOrDrop(i);
            });
            inv.items.clear();
        }
        if (!inv.isArmorEmpty()) {
            has = true;
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (slot.getType().equals(EquipmentSlot.Type.ARMOR)) {
                    if (inv.armor.size() <= slot.getEntitySlotId())
                        continue;
                    if (player.getEquippedStack(slot).isEmpty())
                        player.equipStack(slot, inv.armor.get(slot.getEntitySlotId()));
                    else
                        player.getInventory().offerOrDrop(inv.armor.get(slot.getEntitySlotId()));
                }
            }
            inv.armor.clear();
        }
        if (!inv.isOffhandEmpty()) {
            has = true;
            if (player.getEquippedStack(EquipmentSlot.OFFHAND).isEmpty())
                player.equipStack(EquipmentSlot.OFFHAND, inv.offhand.get(0));
            else
                player.getInventory().offerOrDrop(inv.offhand.get(0));
            inv.offhand.clear();
        }
        if (inv.isXpEmpty()) {
            player.addExperience(inv.xp);
            inv.xp = 0;
        }
        if (has && player instanceof ServerPlayerEntity sp) ModRegistries.keepItemsCriterion.trigger(sp);
        syncWithClient();
    }

    public void syncWithClient() {
        if (world == null || world.isClient) return;
        updateHasSavedItems();
        markDirty(this.world, this.getPos(), this.getCachedState());
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
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        this.hasRandomCharge = tag.contains("RandomCharge") && tag.getBoolean("RandomCharge");
        if (!hasRandomCharge) {
            if (tag.getCompound("Item").contains("id")) {
                String str = tag.getCompound("Item").getString("id");
                if (str.equals("respawnobelisks:obelisk_core_nether") || str.equals("respawnobelisks:obelisk_core_end"))
                    tag.getCompound("Item").putString("id", "respawnobelisks:obelisk_core");

                LOGGER.warn("Respawn Obelisk at \"{}\" had an old core. Updating!", this.getPos());
                ItemStack stack = ItemStack.fromNbt(tag.getCompound("Item"));
                this.coreItem = stack == ItemStack.EMPTY ? Instance.EMPTY : new Instance(stack, ObeliskCore.ANCIENT_CORE);
            } else
                this.coreItem = Instance.CODEC.parse(NbtOps.INSTANCE, tag.getCompound("Item")).getOrThrow(false, s -> LOGGER.error("Failed to parse Obelisk's 'Item'. " + s));
        } else
            this.hasRandomCharge = false;
        this.lastRespawn = tag.getLong("LastRespawn");
        this.lastCharge = tag.getLong("LastCharge");
        this.hasLimboEntity = tag.getBoolean("HasLimboEntity");
        this.hasStoredItems = tag.getBoolean("HasStoredItems");
        this.obeliskName = tag.getString("Name");
        if (tag.contains("ClientCharge")) this.clientCharge = tag.getDouble("ClientCharge");
        if (tag.contains("ClientMaxCharge")) this.clientMaxCharge = tag.getDouble("ClientMaxCharge");
        if (tag.contains("StoredItems", 10)) {
            NbtCompound stored = tag.getCompound("StoredItems");
            for (String key : stored.getKeys()) {
                this.storedItems.put(UUID.fromString(key), ObeliskInventory.readFromNbt(stored.getCompound(key)));
            }
        }
        this.obeliskNameComponent = Text.Serializer.fromJson(tag.getString("Name"));
        this.hasTeleportingEntity = tag.getBoolean("HasTeleportingEntity");
        this.themes.clear();
        tag.getList("RenderThemes", 8).forEach(t -> this.themes.add(new Identifier(t.asString())));
        loadConsumers.forEach(c -> c.accept(tag));
    }

    @Override
    protected void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        this.saveData(tag, true, true);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound tag = new NbtCompound();
        this.saveData(tag, false, true);
        return tag;
    }

    private void saveData(NbtCompound tag, boolean saveItems, boolean sendCharge) {
        tag.putBoolean("RandomCharge", hasRandomCharge);
        tag.put("Item", Instance.CODEC.encodeStart(NbtOps.INSTANCE, coreItem).getOrThrow(true, s -> LOGGER.error("Failed to save Obelisk's 'Item'. " + s)));
        tag.putLong("LastRespawn", lastRespawn);
        tag.putLong("LastCharge", lastCharge);
        tag.putBoolean("HasLimboEntity", hasLimboEntity);
        tag.putBoolean("HasStoredItems", hasStoredItems);
        tag.putString("Name", obeliskName);
        if (saveItems) {
            NbtCompound allPlayers = new NbtCompound();
            for (UUID uuid : storedItems.keySet()) {
                allPlayers.put(uuid.toString(), storedItems.get(uuid).saveToNbt());
            }
            tag.put("StoredItems", allPlayers);
        }
        if (sendCharge) {
            tag.putDouble("ClientCharge", this.getCharge(null));
            tag.putDouble("ClientMaxCharge", this.getMaxCharge(null));
        }
        tag.putBoolean("HasTeleportingEntity", hasTeleportingEntity);
        NbtList list = new NbtList();
        themes.forEach(t -> list.add(NbtString.of(t.toString())));
        tag.put("RenderThemes", list);
    }

    protected static void markDirty(World pLevel, BlockPos pPos, BlockState pState) {
        BlockEntity.markDirty(pLevel, pPos, pState);
        pLevel.updateListeners(pPos, pLevel.getBlockState(pPos), pState, 3);
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    public static void tick(World level, BlockPos blockPos, BlockState state, RespawnObeliskBlockEntity blockEntity) {
        if (level.getTime() % 100 == 0) {
            if (!level.isClient) {
                blockEntity.checkLimbo(true);
            }
        }
    }

    public void updateObeliskName() {
        if (this.coreItem.stack().getOrCreateNbt().contains("display")) {
            NbtCompound displayData = this.coreItem.stack().getNbt().getCompound("display");
            if (displayData.contains("Name")) {
                obeliskName = displayData.getString("Name");
                return;
            }
        }
        obeliskName = "";
    }

    public void checkLimbo(boolean shouldSync) {
        if (this.coreItem.stack().getOrCreateNbt().contains("RespawnObeliskData")) {
            NbtCompound obeliskData = this.coreItem.stack().getNbt().getCompound("RespawnObeliskData");
            if (obeliskData.contains("SavedEntities")) {
                NbtList list = obeliskData.getList("SavedEntities", 10);
                for (NbtElement tag : list) {
                    if (
                            world instanceof ServerWorld serverLevel &&
                                    tag instanceof NbtCompound compound &&
                                    compound.contains("uuid") &&
                                    compound.contains("type") &&
                                    compound.contains("data")
                    ) {
                        Entity entity = serverLevel.getEntity(compound.getUuid("uuid"));
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
    public PositionSource getPositionSource() {
        return source;
    }

    @Override
    public int getRange() {
        return 8;
    }

    @Override
    public boolean listen(ServerWorld level, GameEvent event, GameEvent.Emitter context, Vec3d pos) {
        if (this.isRemoved() || coreItem.isEmpty()) return false;
        boolean result = false;
        for (ObeliskInteraction interaction : ObeliskInteraction.EVENT_INTERACTIONS.getOrDefault(event, Map.of()).values()) {
            if (coreItem.core().interactions.contains(interaction.id)) {
                boolean bl = interaction.eventHandler.apply(this, new GameEvent.Message(event, pos, context, this, this.getPos().toCenterPos()));
                result = !result ? bl : result;
            }
        }
        return result;
    }
}
