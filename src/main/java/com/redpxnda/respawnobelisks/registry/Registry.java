package com.redpxnda.respawnobelisks.registry;

import com.redpxnda.respawnobelisks.registry.blocks.RespawnObeliskBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.redpxnda.respawnobelisks.RespawnObelisks.MODID;

public class Registry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<Block> RESPAWN_OBELISK_BLOCK = BLOCKS.register("respawn_obelisk", () -> new RespawnObeliskBlock(BlockBehaviour.Properties.of(Material.STONE)));
    public static final RegistryObject<Item> RESPAWN_OBELISK_ITEM = ITEMS.register("respawn_obelisk", () -> new BlockItem(RESPAWN_OBELISK_BLOCK.get(), new Item.Properties().tab(CreativeModeTab.TAB_BUILDING_BLOCKS)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        BLOCKS.register(eventBus);
    }
}
