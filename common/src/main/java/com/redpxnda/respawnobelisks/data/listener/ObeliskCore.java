package com.redpxnda.respawnobelisks.data.listener;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import com.redpxnda.respawnobelisks.util.CoreUtils;
import com.redpxnda.respawnobelisks.util.QuadConsumer;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static com.redpxnda.respawnobelisks.data.listener.ObeliskInteraction.*;

@SuppressWarnings("unused")
public class ObeliskCore {
    public static Map<Identifier, ObeliskCore> CORES = new HashMap<>();
    private static final ItemStack ANCIENT_CORE_STACK = ModRegistries.obeliskCore.get().getDefaultStack();
    static {
        NbtCompound tag = new NbtCompound();
        CoreUtils.setMaxCharge(tag, 100);
        CoreUtils.setCharge(tag, 100);
        ANCIENT_CORE_STACK.setNbt(tag);
    }

    public static ObeliskCore ANCIENT_CORE = create(
            ANCIENT_CORE_STACK,
            ModRegistries.OBELISK_CORE_LOC,
            (player, stack, blockEntity) -> CoreUtils.getCharge(stack.getOrCreateNbt()), // get charge
            (player, stack, blockEntity) -> CoreUtils.getMaxCharge(stack.getOrCreateNbt()), // get max charge
            (amnt, player, stack, blockEntity) -> CoreUtils.setCharge(stack.getOrCreateNbt(), amnt), // set charge
            (amnt, player, stack, blockEntity) -> CoreUtils.setMaxCharge(stack.getOrCreateNbt(), amnt), // set max charge
            List.of(DEFAULT_CHARGING, INFINITE_CHARGE, TELEPORT, REVIVE, PROTECT, SAVE_INV),
            RespawnObeliskBlockEntity.defaultThemes,
            Text.literal("TEST"),
            Text.literal("give charge by TESTInG"),
            Text.literal("give max charge by TESTInG"),
            null,
            false
    );

    public final Identifier item;
    public final TriFunction<@Nullable PlayerEntity, ItemStack, RespawnObeliskBlockEntity, Double> chargeProvider, maxChargeProvider;
    public final QuadConsumer<Double, @Nullable PlayerEntity, ItemStack, RespawnObeliskBlockEntity> chargeSetter, maxChargeSetter;
    public final List<Identifier> interactions;
    public final @Nullable Text jeiGeneral, jeiCharge, jeiMaxCharge;
    public final @Nullable List<ItemStack> jeiChargeItems;
    public final boolean alwaysRequiresPlayer;
    private final Instance defaultInstance;
    private final List<Identifier> themes;
    public List<Identifier> themes() {
        return themes;
    }
    public Instance getDefaultInstance() {
        return new Instance(defaultInstance.stack().copy(), this);
    }

    public ObeliskCore(ItemStack instance, Identifier item, TriFunction<PlayerEntity, ItemStack, RespawnObeliskBlockEntity, Double> chargeHandler, TriFunction<PlayerEntity, ItemStack, RespawnObeliskBlockEntity, Double> maxChargeHandler, QuadConsumer<Double, @Nullable PlayerEntity, ItemStack, RespawnObeliskBlockEntity> chargeSetter, QuadConsumer<Double, @Nullable PlayerEntity, ItemStack, RespawnObeliskBlockEntity> maxChargeSetter, List<Identifier> interactions, List<Identifier> themes, @Nullable Text jeiGeneral, @Nullable Text jeiCharge, @Nullable Text jeiMaxCharge, @Nullable List<ItemStack> chargeItems, boolean alwaysRequiresPlayer) {
        this.item                 = item;
        this.chargeProvider       = chargeHandler;
        this.maxChargeProvider    = maxChargeHandler;
        this.chargeSetter         = chargeSetter;
        this.maxChargeSetter      = maxChargeSetter;
        this.interactions         = interactions;
        this.themes               = themes;
        this.jeiGeneral           = jeiGeneral;
        this.jeiCharge            = jeiCharge;
        this.jeiMaxCharge         = jeiMaxCharge;
        this.jeiChargeItems       = chargeItems;
        this.alwaysRequiresPlayer = alwaysRequiresPlayer;
        this.defaultInstance      = new Instance(instance, this);
        CORES.put(item, this);
    }

    public static ObeliskCore create(ItemStack instance, Identifier item, TriFunction<PlayerEntity, ItemStack, RespawnObeliskBlockEntity, Double> chargeHandler, TriFunction<PlayerEntity, ItemStack, RespawnObeliskBlockEntity, Double> maxChargeHandler, QuadConsumer<Double, @Nullable PlayerEntity, ItemStack, RespawnObeliskBlockEntity> chargeSetter, QuadConsumer<Double, @Nullable PlayerEntity, ItemStack, RespawnObeliskBlockEntity> maxChargeSetter, List<ObeliskInteraction> interactions, List<Identifier> themes, @Nullable Text jeiGeneral, @Nullable Text jeiCharge, @Nullable Text jeiMaxCharge, @Nullable List<ItemStack> chargeItems, boolean alwaysRequiresPlayer) {
        return new ObeliskCore(instance, item, chargeHandler, maxChargeHandler, chargeSetter, maxChargeSetter, interactions.stream().map(i -> i.id).toList(), themes, jeiGeneral, jeiCharge, jeiMaxCharge, chargeItems, alwaysRequiresPlayer);
    }

    public record Instance(ItemStack stack, ObeliskCore core) {
        public static Instance EMPTY = new Instance(ItemStack.EMPTY, null);
        public static Codec<Instance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                ItemStack.CODEC.optionalFieldOf("stack").forGetter(i -> i.stack == null || i.stack == ItemStack.EMPTY ? Optional.empty() : Optional.of(i.stack)),
                Identifier.CODEC.optionalFieldOf("core").forGetter(i -> i.core == null ? Optional.empty() : Optional.of(i.core.item))
        ).apply(inst, (stack, core) -> core.isEmpty() || stack.isEmpty() ? EMPTY : new Instance(stack.get(), CORES.get(core.get()))));

        public boolean isEmpty() {
            return this == EMPTY || this.core == null || this.stack == null || this.stack.isEmpty();
        }
    }

    public static class Builder {
        public Identifier item;
        public TriFunction<@Nullable PlayerEntity, ItemStack, RespawnObeliskBlockEntity, Double> chargeProvider, maxChargeProvider;
        public QuadConsumer<Double, @Nullable PlayerEntity, ItemStack, RespawnObeliskBlockEntity> chargeConsumer, maxChargeConsumer;
        public List<Identifier> interactions = new ArrayList<>();
        public @Nullable Text jeiGeneral = null, jeiCharge = null, jeiMaxCharge = null;
        public @Nullable List<ItemStack> jeiChargeItems = null;
        public boolean alwaysRequiresPlayer = false;
        public ItemStack stack = null;
        public List<Identifier> themes = List.of();

        public static Builder create() {
            return new Builder();
        }
        public Builder withItem(Identifier location) {
            item = location;
            return this;
        }
        public Builder withItem(String str) {
            item = new Identifier(str);
            return this;
        }
        public Builder chargeGetter(TriFunction<@Nullable PlayerEntity, ItemStack, RespawnObeliskBlockEntity, Double> handler) {
            this.chargeProvider = handler;
            return this;
        }
        public Builder maxChargeGetter(TriFunction<@Nullable PlayerEntity, ItemStack, RespawnObeliskBlockEntity, Double> handler) {
            this.maxChargeProvider = handler;
            return this;
        }
        public Builder chargeSetter(QuadConsumer<Double, @Nullable PlayerEntity, ItemStack, RespawnObeliskBlockEntity> handler) {
            this.chargeConsumer = handler;
            return this;
        }
        public Builder maxChargeSetter(QuadConsumer<Double, @Nullable PlayerEntity, ItemStack, RespawnObeliskBlockEntity> handler) {
            this.maxChargeConsumer = handler;
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
        public Builder withInteraction(String interaction) {
            interactions.add(new Identifier(interaction));
            return this;
        }
        public Builder jeiGeneralText(Text component) {
            jeiGeneral = component;
            return this;
        }
        public Builder jeiGeneralText(String str) {
            jeiGeneral = Text.literal(str);
            return this;
        }
        public Builder jeiChargeText(Text component) {
            jeiCharge = component;
            return this;
        }
        public Builder jeiChargeText(String str) {
            jeiCharge = Text.literal(str);
            return this;
        }
        public Builder jeiMaxChargeText(Text component) {
            jeiMaxCharge = component;
            return this;
        }
        public Builder jeiMaxChargeText(String str) {
            jeiMaxCharge = Text.literal(str);
            return this;
        }
        public Builder withChargeItem(Item item) {
            if (jeiChargeItems == null)
                jeiChargeItems = new ArrayList<>();
            jeiChargeItems.add(item.getDefaultStack());
            return this;
        }
        public Builder withChargeItem(ItemStack item) {
            if (jeiChargeItems == null)
                jeiChargeItems = new ArrayList<>();
            jeiChargeItems.add(item);
            return this;
        }
        public Builder alwaysRequiresPlayer(boolean bl) {
            alwaysRequiresPlayer = bl;
            return this;
        }
        public Builder setCoreItem(String str) {
            item = new Identifier(str);
            return this;
        }
        public Builder defaultInstance(ItemStack item) {
            stack = item;
            return this;
        }
        public Builder withTheme(String str) {
            themes.add(new Identifier(str));
            return this;
        }
        public Builder withTheme(Identifier loc) {
            themes.add(loc);
            return this;
        }

        public ObeliskCore build() {
            if (stack == null)
                stack = Registries.ITEM.getOrEmpty(item).orElse(Items.AIR).getDefaultStack();
            if (themes.isEmpty()) themes = RespawnObeliskBlockEntity.defaultThemes;
            return new ObeliskCore(stack, item, chargeProvider, maxChargeProvider, chargeConsumer, maxChargeConsumer, interactions, themes, jeiGeneral, jeiCharge, jeiMaxCharge, jeiChargeItems, alwaysRequiresPlayer);
        }

        private Builder() {
        }
    }
}
