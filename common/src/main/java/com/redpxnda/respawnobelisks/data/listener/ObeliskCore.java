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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.Nullable;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.util.*;

@SuppressWarnings("unused")
public class ObeliskCore {
    public static Map<ResourceLocation, ObeliskCore> CORES = new HashMap<>();

    public static ObeliskCore ANCIENT_CORE = new ObeliskCore(
            ModRegistries.OBELISK_CORE_LOC,
            (player, stack, blockEntity) -> stack.getOrCreateTag().getCompound("RespawnObeliskData").getDouble("Charge"), // get charge
            (player, stack, blockEntity) -> stack.getOrCreateTag().getCompound("RespawnObeliskData").getDouble("MaxCharge"), // get max charge
            (amnt, player, stack, blockEntity) -> stack.getOrCreateTag().getCompound("RespawnObeliskData").putDouble("Charge", amnt), // set charge
            (amnt, player, stack, blockEntity) -> stack.getOrCreateTag().getCompound("RespawnObeliskData").putDouble("MaxCharge", amnt), // set max charge
            ObeliskInteraction.DEFAULT_CHARGING,
            CoreUtils.DEFAULT_CAPS,
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
    public final List<CoreUtils.Capability> capabilities;
    public final @Nullable Component jeiGeneral, jeiCharge, jeiMaxCharge;
    public final @Nullable List<ItemStack> jeiChargeItems;
    public final boolean alwaysRequiresPlayer;

    public ObeliskCore(ResourceLocation item, TriFunction<Player, ItemStack, RespawnObeliskBlockEntity, Double> chargeHandler, TriFunction<Player, ItemStack, RespawnObeliskBlockEntity, Double> maxChargeHandler, QuadConsumer<Double, @Nullable Player, ItemStack, RespawnObeliskBlockEntity> chargeSetter, QuadConsumer<Double, @Nullable Player, ItemStack, RespawnObeliskBlockEntity> maxChargeSetter, List<ResourceLocation> interactions, List<CoreUtils.Capability> capabilities, @Nullable Component jeiGeneral, @Nullable Component jeiCharge, @Nullable Component jeiMaxCharge, @Nullable List<ItemStack> chargeItems, boolean alwaysRequiresPlayer) {
        this.item = item;
        this.chargeProvider = chargeHandler;
        this.maxChargeProvider = maxChargeHandler;
        this.chargeSetter = chargeSetter;
        this.maxChargeSetter = maxChargeSetter;
        this.interactions = interactions;
        this.capabilities = capabilities;
        this.jeiGeneral = jeiGeneral;
        this.jeiCharge = jeiCharge;
        this.jeiMaxCharge = jeiMaxCharge;
        this.jeiChargeItems = chargeItems;
        this.alwaysRequiresPlayer = alwaysRequiresPlayer;
        CORES.put(item, this);
    }
    public ObeliskCore(ResourceLocation item, TriFunction<Player, ItemStack, RespawnObeliskBlockEntity, Double> chargeHandler, TriFunction<Player, ItemStack, RespawnObeliskBlockEntity, Double> maxChargeHandler, QuadConsumer<Double, @Nullable Player, ItemStack, RespawnObeliskBlockEntity> chargeSetter, QuadConsumer<Double, @Nullable Player, ItemStack, RespawnObeliskBlockEntity> maxChargeSetter, ObeliskInteraction interaction, List<CoreUtils.Capability> capabilities, @Nullable Component jeiGeneral, @Nullable Component jeiCharge, @Nullable Component jeiMaxCharge, @Nullable List<ItemStack> chargeItems, boolean alwaysRequiresPlayer) {
        this(item, chargeHandler, maxChargeHandler, chargeSetter, maxChargeSetter, List.of(interaction.id), capabilities, jeiGeneral, jeiCharge, jeiMaxCharge, chargeItems, alwaysRequiresPlayer);
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
        public List<CoreUtils.Capability> capabilities = new ArrayList<>(){{
            add(CoreUtils.Capability.CHARGE);
            add(CoreUtils.Capability.PROTECT);
            add(CoreUtils.Capability.SAVE_INV);
        }};
        public @Nullable Component jeiGeneral = null, jeiCharge = null, jeiMaxCharge = null;
        public @Nullable List<ItemStack> jeiChargeItems = null;
        public boolean alwaysRequiresPlayer = false;

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
            this.chargeProvider = (player, stack, be) -> handler.call(CoerceJavaToLua.coerce(new PlayerReference(player)), CoerceJavaToLua.coerce(new ItemStackReference(stack)), CoerceJavaToLua.coerce(new ROBEReference(be))).todouble();
            return this;
        }
        public Builder maxChargeGetter(TriFunction<@Nullable Player, ItemStack, RespawnObeliskBlockEntity, Double> handler) {
            this.maxChargeProvider = handler;
            return this;
        }
        public Builder maxChargeGetter(LuaFunction handler) {
            this.maxChargeProvider = (player, stack, be) -> handler.call(CoerceJavaToLua.coerce(new PlayerReference(player)), CoerceJavaToLua.coerce(new ItemStackReference(stack)), CoerceJavaToLua.coerce(new ROBEReference(be))).todouble();
            return this;
        }
        public Builder chargeSetter(QuadConsumer<Double, @Nullable Player, ItemStack, RespawnObeliskBlockEntity> handler) {
            this.chargeConsumer = handler;
            return this;
        }
        public Builder chargeSetter(LuaFunction handler) {
            this.chargeConsumer = (amnt, player, stack, be) -> handler.invoke(new LuaValue[]{ LuaValue.valueOf(amnt), CoerceJavaToLua.coerce(new PlayerReference(player)), CoerceJavaToLua.coerce(new ItemStackReference(stack)), CoerceJavaToLua.coerce(new ROBEReference(be)) });
            return this;
        }
        public Builder maxChargeSetter(QuadConsumer<Double, @Nullable Player, ItemStack, RespawnObeliskBlockEntity> handler) {
            this.maxChargeConsumer = handler;
            return this;
        }
        public Builder maxChargeSetter(LuaFunction handler) {
            this.maxChargeConsumer = (amnt, player, stack, be) -> handler.invoke(new LuaValue[]{ LuaValue.valueOf(amnt), CoerceJavaToLua.coerce(new PlayerReference(player)), CoerceJavaToLua.coerce(new ItemStackReference(stack)), CoerceJavaToLua.coerce(new ROBEReference(be)) });
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
        public Builder clearCapabilities() {
            capabilities.clear();
            return this;
        }
        public Builder withCapability(CoreUtils.Capability capability) {
            capabilities.add(capability);
            return this;
        }
        public Builder withCapabilities(CoreUtils.Capability... capability) {
            Collections.addAll(capabilities, capability);
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

        public ObeliskCore build() {
            return new ObeliskCore(item, chargeProvider, maxChargeProvider, chargeConsumer, maxChargeConsumer, interactions, capabilities, jeiGeneral, jeiCharge, jeiMaxCharge, jeiChargeItems, alwaysRequiresPlayer);
        }

        private Builder() {
        }
    }
}
