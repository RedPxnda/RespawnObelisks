package com.redpxnda.respawnobelisks.registry.block.entity;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.redpxnda.respawnobelisks.RespawnObelisks;
import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.data.listener.ObeliskCore;
import com.redpxnda.respawnobelisks.data.listener.ObeliskCore.Instance;
import com.redpxnda.respawnobelisks.data.listener.ObeliskInteraction;
import com.redpxnda.respawnobelisks.data.saved.LimboEntities;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.redpxnda.respawnobelisks.registry.block.entity.theme.ThemeLayout;
import com.redpxnda.respawnobelisks.util.CoreUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.redpxnda.respawnobelisks.registry.block.RespawnObeliskBlock.WILD;
import static com.redpxnda.respawnobelisks.registry.block.entity.theme.RenderTheme.*;

public class RespawnObeliskBlockEntity extends BlockEntity implements GameEventListener {
    public static final List<ResourceLocation> defaultThemes = List.of(defCharge, defDep, defRunes);
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
    private Component obeliskNameComponent = null;
    public boolean hasTeleportingEntity = false;
    public final List<ResourceLocation> themes = new ArrayList<>();
    public final Multimap<GlobalPos, ServerPlayer> respawningPlayers = Multimaps.newMultimap(new ConcurrentHashMap<>(), HashSet::new);

    public RespawnObeliskBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModRegistries.ROBE.get(), pos, blockState);
        coreItem = Instance.EMPTY;
        setupRandomCharge();
        source = new BlockPositionSource(this.getBlockPos());
        lastRespawn = -100;
        lastCharge = -100;
        hasLimboEntity = false;
    }

    public void setupRandomCharge() {
        if (hasRandomCharge && getBlockState().getValue(WILD) && random.nextInt(100) < RespawnObelisksConfig.INSTANCE.cores.wildCoreChance) {
            this.coreItem = RespawnObelisksConfig.INSTANCE.cores.getDefaultCore().getDefaultInstance();
            double charge = (random.nextInt(RespawnObelisksConfig.INSTANCE.cores.wildMaxRadiance -RespawnObelisksConfig.INSTANCE.cores.wildMinRadiance)+RespawnObelisksConfig.INSTANCE.cores.wildMinRadiance) / 100f;
            int maxCharge = random.nextInt(RespawnObelisksConfig.INSTANCE.cores.wildMaxMaxRadiance -RespawnObelisksConfig.INSTANCE.cores.wildMinMaxRadiance)+RespawnObelisksConfig.INSTANCE.cores.wildMinMaxRadiance;
            setCharge(null, Math.round(charge*maxCharge));
            setMaxCharge(null, maxCharge);
        }
        hasRandomCharge = false;
    }

    public boolean isPlayerTrusted(String username) {
        if (!CoreUtils.hasInteraction(coreItem, ObeliskInteraction.PROTECT)) return true;
        if (RespawnObelisksConfig.INSTANCE.playerTrusting.enablePlayerTrust && this.coreItem.stack().getOrCreateTag().contains("RespawnObeliskData")) {
            CompoundTag obeliskData = this.coreItem.stack().getTag().getCompound("RespawnObeliskData");
            if (obeliskData.contains("TrustedPlayers")) {
                ListTag list = obeliskData.getList("TrustedPlayers", 8);
                return list.contains(StringTag.valueOf(username));
            }
        }
        return true;
    }
    public List<ResourceLocation> getThemesOrDefault() {
        if (coreItem.isEmpty()) return List.of();
        if (themes.isEmpty()) return coreItem.core().themes();
        return themes;
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
    public double getClientCharge() {
        return clientCharge;
    }
    public double getClientMaxCharge() {
        return clientMaxCharge;
    }
    public double getCost(ServerPlayer player) {
        return getCost(player, false, true, false);
    }
    public double getCost(ServerPlayer player, boolean curseForced, boolean shouldConsumeCost, boolean isTeleport) {
        double cost = !shouldConsumeCost ? 0 : isTeleport ? RespawnObelisksConfig.INSTANCE.teleportation.teleportationCost : RespawnObelisksConfig.INSTANCE.radiance.respawnCost; // todo update shouldConsumeCost behavior
        ObeliskInteraction.Manager manager = new ObeliskInteraction.Manager(curseForced, shouldConsumeCost, cost, null);
        for (ObeliskInteraction i : ObeliskInteraction.RESPAWN_INTERACTIONS.get(ObeliskInteraction.Injection.START)) {
            i.respawnHandler.accept(player, this, manager);
        }
        return manager.cost;
    }
    public double getCharge(@Nullable Player player) {
        if (level == null || level.isClientSide) return clientCharge;
        if (coreItem.isEmpty() || (coreItem.core().alwaysRequiresPlayer && player == null)) return 0;
        return coreItem.core().chargeProvider.apply(player, coreItem.stack(), this);
    }
    public double getMaxCharge(@Nullable Player player) {
        if (level == null || level.isClientSide) return clientMaxCharge;
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
            else {
                setLastCharge(level.getGameTime());
                if (player instanceof ServerPlayer sp) ModRegistries.chargeCriterion.trigger(sp);
            }
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

    public void syncWithClient() {
        if (level == null || level.isClientSide) return;
        setChanged(this.level, this.getBlockPos(), this.getBlockState());
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
                this.coreItem = Instance.CODEC.parse(NbtOps.INSTANCE, tag.getCompound("Item")).getOrThrow(false, s -> LOGGER.error("Failed to parse Obelisk's 'Item'. " + s));
        } else
            this.hasRandomCharge = false;
        this.lastRespawn = tag.getLong("LastRespawn");
        this.lastCharge = tag.getLong("LastCharge");
        this.hasLimboEntity = tag.getBoolean("HasLimboEntity");
        this.obeliskName = tag.getString("Name");
        if (tag.contains("ClientCharge")) this.clientCharge = tag.getDouble("ClientCharge");
        if (tag.contains("ClientMaxCharge")) this.clientMaxCharge = tag.getDouble("ClientMaxCharge");
        this.obeliskNameComponent = Component.Serializer.fromJson(tag.getString("Name"));
        this.hasTeleportingEntity = tag.getBoolean("HasTeleportingEntity");
        this.themes.clear();
        tag.getList("RenderThemes", 8).forEach(t -> this.themes.add(new ResourceLocation(t.getAsString())));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        this.saveData(tag, true);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        this.saveData(tag, true);
        return tag;
    }

    private void saveData(CompoundTag tag, boolean sendCharge) {
        tag.putBoolean("RandomCharge", hasRandomCharge);
        tag.put("Item", Instance.CODEC.encodeStart(NbtOps.INSTANCE, coreItem).getOrThrow(true, s -> LOGGER.error("Failed to save Obelisk's 'Item'. " + s)));
        tag.putLong("LastRespawn", lastRespawn);
        tag.putLong("LastCharge", lastCharge);
        tag.putBoolean("HasLimboEntity", hasLimboEntity);
        tag.putString("Name", obeliskName);
        if (sendCharge) {
            tag.putDouble("ClientCharge", this.getCharge(null));
            tag.putDouble("ClientMaxCharge", this.getMaxCharge(null));
        }
        tag.putBoolean("HasTeleportingEntity", hasTeleportingEntity);
        ListTag list = new ListTag();
        themes.forEach(t -> list.add(StringTag.valueOf(t.toString())));
        tag.put("RenderThemes", list);
    }

    protected static void setChanged(Level pLevel, BlockPos pPos, BlockState pState) {
        BlockEntity.setChanged(pLevel, pPos, pState);
        pLevel.sendBlockUpdated(pPos, pLevel.getBlockState(pPos), pState, 3);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void tick(Level level, BlockPos blockPos, BlockState state) {
        if (level.getGameTime() % 100 == 0) {
            if (!level.isClientSide) {
                checkLimbo(true);
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
                        LimboEntities limboData = LimboEntities.getCache(serverLevel.getServer().overworld());
                        CompoundTag entityData = limboData.limboEntities.get(compound.getUUID("uuid"));

                        if (entityData != null) {
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
    public PositionSource getListenerSource() {
        return source;
    }

    @Override
    public int getListenerRadius() {
        return 8;
    }

    @Override
    public boolean handleGameEvent(ServerLevel level, GameEvent event, GameEvent.Context context, Vec3 pos) {
        if (this.isRemoved() || coreItem.isEmpty()) return false;
        boolean result = false;
        for (ObeliskInteraction interaction : ObeliskInteraction.EVENT_INTERACTIONS.getOrDefault(event, Map.of()).values()) {
            if (coreItem.core().interactions.contains(interaction.id)) {
                boolean bl = interaction.eventHandler.apply(this, new GameEvent.ListenerInfo(event, pos, context, this, this.getBlockPos().getCenter()));
                result = !result ? bl : result;
            }
        }
        return result;
    }
}
