package com.redpxnda.respawnobelisks.data.listener;

import com.redpxnda.nucleus.datapack.constants.ConstantsAccess;
import com.redpxnda.nucleus.datapack.lua.LuaResourceReloadListener;
import com.redpxnda.nucleus.datapack.references.Statics;
import com.redpxnda.nucleus.util.LuaUtil;
import com.redpxnda.respawnobelisks.config.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.util.Map;

public class ObeliskCoreListener extends LuaResourceReloadListener {
    public static final Globals GLOBALS = ConstantsAccess.completeSetup(ConstantsAccess.readOnly);
    static {
        GLOBALS.set("Interactions", CoerceJavaToLua.coerce(ObeliskInteraction.class));
        GLOBALS.set("Cores", CoerceJavaToLua.coerce(ObeliskCore.class));
        GLOBALS.set("Builder", CoerceJavaToLua.coerce(ObeliskCore.Builder.class));
        GLOBALS.set("Config", new LuaTable(){{
            set("charge", CoerceJavaToLua.coerce(ChargeConfig.class));
            set("curse", CoerceJavaToLua.coerce(CurseConfig.class));
            set("core", CoerceJavaToLua.coerce(ObeliskCoreConfig.class));
            set("root", CoerceJavaToLua.coerce(RespawnObelisksConfig.class));
            set("perk", CoerceJavaToLua.coerce(RespawnPerkConfig.class));
            set("revive", CoerceJavaToLua.coerce(ReviveConfig.class));
            set("teleport", CoerceJavaToLua.coerce(TeleportConfig.class));
            set("trust", CoerceJavaToLua.coerce(TrustedPlayersConfig.class));
        }});
    }

    public ObeliskCoreListener() {
        super(GLOBALS, "obelisk_cores");
    }

    @Override
    protected void apply(Map<ResourceLocation, LuaValue> object, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        object.forEach((k, v) -> v.call());
    }
}
