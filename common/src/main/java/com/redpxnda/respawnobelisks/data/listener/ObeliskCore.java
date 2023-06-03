package com.redpxnda.respawnobelisks.data.listener;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.redpxnda.nucleus.datapack.references.entity.PlayerReference;
import com.redpxnda.nucleus.datapack.references.item.ItemReference;
import com.redpxnda.nucleus.datapack.references.item.ItemStackReference;
import com.redpxnda.nucleus.datapack.references.storage.ComponentReference;
import com.redpxnda.nucleus.datapack.references.storage.ResourceLocationReference;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import com.redpxnda.respawnobelisks.util.CoreUtils;
import com.redpxnda.respawnobelisks.util.QuadConsumer;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.Nullable;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;

import java.util.*;

import static com.redpxnda.respawnobelisks.data.listener.ObeliskInteraction.*;
import static org.luaj.vm2.lib.jse.CoerceJavaToLua.coerce;

@SuppressWarnings("unused")
public class ObeliskCore {
    public static Map<ResourceLocation, ObeliskCore> CORES = new HashMap<>();
    private static final ItemStack ANCIENT_CORE_STACK = ModRegistries.OBELISK_CORE.get().getDefaultInstance();
    static {
        CompoundTag tag = new CompoundTag();
        CoreUtils.setMaxCharge(tag, 100);
        CoreUtils.setCharge(tag, 100);
        ANCIENT_CORE_STACK.setTag(tag);
    }

    public static ObeliskCore ANCIENT_CORE = create(
            ANCIENT_CORE_STACK,
            ModRegistries.OBELISK_CORE_LOC,
            (player, stack, blockEntity) -> CoreUtils.getCharge(stack.getOrCreateTag()), // get charge
            (player, stack, blockEntity) -> CoreUtils.getMaxCharge(stack.getOrCreateTag()), // get max charge
            (amnt, player, stack, blockEntity) -> CoreUtils.setCharge(stack.getOrCreateTag(), amnt), // set charge
            (amnt, player, stack, blockEntity) -> CoreUtils.setMaxCharge(stack.getOrCreateTag(), amnt), // set max charge
            List.of(DEFAULT_CHARGING, INFINITE_CHARGE, TELEPORT, REVIVE, PROTECT, SAVE_INV),
            Component.literal("TEST"),
            Component.literal("give charge by TESTInG"),
            Component.literal("give max charge by TESTInG"),
            null,
            false
    );

    public final ResourceLocation item;
    public final TriFunction<@Nullable Player, ItemStack, RespawnObeliskBlockEntity, Double> chargeProvider, maxChargeProvider;
    public final QuadConsumer<Double, @Nullable Player, ItemStack, RespawnObeliskBlockEntity> chargeSetter, maxChargeSetter;
    public final List<ResourceLocation> interactions;
    public final @Nullable Component jeiGeneral, jeiCharge, jeiMaxCharge;
    public final @Nullable List<ItemStack> jeiChargeItems;
    public final boolean alwaysRequiresPlayer;
    private final Instance defaultInstance;
    public Instance getDefaultInstance() {
        return new Instance(defaultInstance.stack().copy(), this);
    }

    public ObeliskCore(ItemStack instance, ResourceLocation item, TriFunction<Player, ItemStack, RespawnObeliskBlockEntity, Double> chargeHandler, TriFunction<Player, ItemStack, RespawnObeliskBlockEntity, Double> maxChargeHandler, QuadConsumer<Double, @Nullable Player, ItemStack, RespawnObeliskBlockEntity> chargeSetter, QuadConsumer<Double, @Nullable Player, ItemStack, RespawnObeliskBlockEntity> maxChargeSetter, List<ResourceLocation> interactions, @Nullable Component jeiGeneral, @Nullable Component jeiCharge, @Nullable Component jeiMaxCharge, @Nullable List<ItemStack> chargeItems, boolean alwaysRequiresPlayer) {
        this.item = item;
        this.chargeProvider = chargeHandler;
        this.maxChargeProvider = maxChargeHandler;
        this.chargeSetter = chargeSetter;
        this.maxChargeSetter = maxChargeSetter;
        this.interactions = interactions;
        this.jeiGeneral = jeiGeneral;
        this.jeiCharge = jeiCharge;
        this.jeiMaxCharge = jeiMaxCharge;
        this.jeiChargeItems = chargeItems;
        this.alwaysRequiresPlayer = alwaysRequiresPlayer;
        this.defaultInstance = new Instance(instance, this);
        CORES.put(item, this);
    }
    public ObeliskCore(ItemStack instance, ResourceLocation item, TriFunction<Player, ItemStack, RespawnObeliskBlockEntity, Double> chargeHandler, TriFunction<Player, ItemStack, RespawnObeliskBlockEntity, Double> maxChargeHandler, QuadConsumer<Double, @Nullable Player, ItemStack, RespawnObeliskBlockEntity> chargeSetter, QuadConsumer<Double, @Nullable Player, ItemStack, RespawnObeliskBlockEntity> maxChargeSetter, ObeliskInteraction interaction, @Nullable Component jeiGeneral, @Nullable Component jeiCharge, @Nullable Component jeiMaxCharge, @Nullable List<ItemStack> chargeItems, boolean alwaysRequiresPlayer) {
        this(instance, item, chargeHandler, maxChargeHandler, chargeSetter, maxChargeSetter, List.of(interaction.id), jeiGeneral, jeiCharge, jeiMaxCharge, chargeItems, alwaysRequiresPlayer);
    }

    public static ObeliskCore create(ItemStack instance, ResourceLocation item, TriFunction<Player, ItemStack, RespawnObeliskBlockEntity, Double> chargeHandler, TriFunction<Player, ItemStack, RespawnObeliskBlockEntity, Double> maxChargeHandler, QuadConsumer<Double, @Nullable Player, ItemStack, RespawnObeliskBlockEntity> chargeSetter, QuadConsumer<Double, @Nullable Player, ItemStack, RespawnObeliskBlockEntity> maxChargeSetter, List<ObeliskInteraction> interactions, @Nullable Component jeiGeneral, @Nullable Component jeiCharge, @Nullable Component jeiMaxCharge, @Nullable List<ItemStack> chargeItems, boolean alwaysRequiresPlayer) {
        return new ObeliskCore(instance, item, chargeHandler, maxChargeHandler, chargeSetter, maxChargeSetter, interactions.stream().map(i -> i.id).toList(), jeiGeneral, jeiCharge, jeiMaxCharge, chargeItems, alwaysRequiresPlayer);
    }

    public record Instance(ItemStack stack, ObeliskCore core) {
        public static Instance EMPTY = new Instance(ItemStack.EMPTY, null);
        public static Codec<Instance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                ItemStack.CODEC.optionalFieldOf("stack").forGetter(i -> i.stack == null || i.stack == ItemStack.EMPTY ? Optional.empty() : Optional.of(i.stack)),
                ResourceLocation.CODEC.optionalFieldOf("core").forGetter(i -> i.core == null ? Optional.empty() : Optional.of(i.core.item))
        ).apply(inst, (stack, core) -> core.isEmpty() || stack.isEmpty() ? EMPTY : new Instance(stack.get(), CORES.get(core.get()))));

        public boolean isEmpty() {
            return this == EMPTY;
        }
    }

    public static class Builder {
        public ResourceLocation item;
        public TriFunction<@Nullable Player, ItemStack, RespawnObeliskBlockEntity, Double> chargeProvider, maxChargeProvider;
        public QuadConsumer<Double, @Nullable Player, ItemStack, RespawnObeliskBlockEntity> chargeConsumer, maxChargeConsumer;
        public List<ResourceLocation> interactions = new ArrayList<>();
        public @Nullable Component jeiGeneral = null, jeiCharge = null, jeiMaxCharge = null;
        public @Nullable List<ItemStack> jeiChargeItems = null;
        public boolean alwaysRequiresPlayer = false;
        public ItemStack stack = null;

        public static Builder create() {
            return new Builder();
        }
        public Builder withItem(ResourceLocation location) {
            item = location;
            return this;
        }
        public Builder withItem(String str) {
            item = new ResourceLocation(str);
            return this;
        }
        public Builder withItem(ResourceLocationReference ref) {
            item = ref.instance;
            return this;
        }
        public Builder withItem(ItemReference<?> item) {
            this.item = Registry.ITEM.getKey(item.instance);
            return this;
        }
        public Builder chargeGetter(TriFunction<@Nullable Player, ItemStack, RespawnObeliskBlockEntity, Double> handler) {
            this.chargeProvider = handler;
            return this;
        }
        public Builder chargeGetter(LuaFunction handler) {
            this.chargeProvider = (player, stack, be) -> handler.call(coerce(new PlayerReference(player)), coerce(new ItemStackReference(stack)), coerce(new ROBEReference(be))).todouble();
            return this;
        }
        public Builder maxChargeGetter(TriFunction<@Nullable Player, ItemStack, RespawnObeliskBlockEntity, Double> handler) {
            this.maxChargeProvider = handler;
            return this;
        }
        public Builder maxChargeGetter(LuaFunction handler) {
            this.maxChargeProvider = (player, stack, be) -> handler.call(coerce(new PlayerReference(player)), coerce(new ItemStackReference(stack)), coerce(new ROBEReference(be))).todouble();
            return this;
        }
        public Builder chargeSetter(QuadConsumer<Double, @Nullable Player, ItemStack, RespawnObeliskBlockEntity> handler) {
            this.chargeConsumer = handler;
            return this;
        }
        public Builder chargeSetter(LuaFunction handler) {
            this.chargeConsumer = (amnt, player, stack, be) -> handler.invoke(new LuaValue[]{ LuaValue.valueOf(amnt), coerce(new PlayerReference(player)), coerce(new ItemStackReference(stack)), coerce(new ROBEReference(be)) });
            return this;
        }
        public Builder maxChargeSetter(QuadConsumer<Double, @Nullable Player, ItemStack, RespawnObeliskBlockEntity> handler) {
            this.maxChargeConsumer = handler;
            return this;
        }
        public Builder maxChargeSetter(LuaFunction handler) {
            this.maxChargeConsumer = (amnt, player, stack, be) -> handler.invoke(new LuaValue[]{ LuaValue.valueOf(amnt), coerce(new PlayerReference(player)), coerce(new ItemStackReference(stack)), coerce(new ROBEReference(be)) });
            return this;
        }
        public Builder clearInteractions() {
            interactions.clear();
            return this;
        }
        public Builder withInteraction(ObeliskInteraction interaction) {
            interactions.add(interaction.id);
            return this;
        }
        public Builder withInteraction(ResourceLocationReference interaction) {
            interactions.add(interaction.instance);
            return this;
        }
        public Builder withInteraction(String interaction) {
            interactions.add(new ResourceLocation(interaction));
            return this;
        }
        public Builder jeiGeneralText(Component component) {
            jeiGeneral = component;
            return this;
        }
        public Builder jeiGeneralText(ComponentReference<?> component) {
            jeiGeneral = component.instance;
            return this;
        }
        public Builder jeiGeneralText(String str) {
            jeiGeneral = Component.literal(str);
            return this;
        }
        public Builder jeiChargeText(Component component) {
            jeiCharge = component;
            return this;
        }
        public Builder jeiChargeText(ComponentReference<?> component) {
            jeiCharge = component.instance;
            return this;
        }
        public Builder jeiChargeText(String str) {
            jeiCharge = Component.literal(str);
            return this;
        }
        public Builder jeiMaxChargeText(Component component) {
            jeiMaxCharge = component;
            return this;
        }
        public Builder jeiMaxChargeText(ComponentReference<?> component) {
            jeiMaxCharge = component.instance;
            return this;
        }
        public Builder jeiMaxChargeText(String str) {
            jeiMaxCharge = Component.literal(str);
            return this;
        }
        public Builder withChargeItem(Item item) {
            if (jeiChargeItems == null)
                jeiChargeItems = new ArrayList<>();
            jeiChargeItems.add(item.getDefaultInstance());
            return this;
        }
        public Builder withChargeItem(ItemReference<?> item) {
            if (jeiChargeItems == null)
                jeiChargeItems = new ArrayList<>();
            jeiChargeItems.add(item.instance.getDefaultInstance());
            return this;
        }
        public Builder withChargeItem(ItemStack item) {
            if (jeiChargeItems == null)
                jeiChargeItems = new ArrayList<>();
            jeiChargeItems.add(item);
            return this;
        }
        public Builder withChargeItem(ItemStackReference item) {
            if (jeiChargeItems == null)
                jeiChargeItems = new ArrayList<>();
            jeiChargeItems.add(item.instance);
            return this;
        }
        public Builder alwaysRequiresPlayer(boolean bl) {
            alwaysRequiresPlayer = bl;
            return this;
        }
        public Builder setCoreItem(String str) {
            item = new ResourceLocation(str);
            return this;
        }
        public Builder defaultInstanceHandler(LuaFunction function) {
            stack = (ItemStack) CoerceLuaToJava.coerce(function.call(
                    coerce(new ItemStackReference(Registry.ITEM.getOptional(item).orElse(Items.AIR).getDefaultInstance()))
            ), ItemStack.class);
            return this;
        }

        public ObeliskCore build() {
            if (stack == null)
                stack = Registry.ITEM.getOptional(item).orElse(Items.AIR).getDefaultInstance();
            return new ObeliskCore(stack, item, chargeProvider, maxChargeProvider, chargeConsumer, maxChargeConsumer, interactions, jeiGeneral, jeiCharge, jeiMaxCharge, jeiChargeItems, alwaysRequiresPlayer);
        }

        private Builder() {
        }
    }
}
