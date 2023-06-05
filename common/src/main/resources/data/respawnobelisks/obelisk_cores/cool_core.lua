---@param charge number
---@param player Player
---@param item ItemStack
---@param blockEntity BlockEntity
local chargeGetter = function(player, item, blockEntity)
    if (not item:getOrCreateTag():contains("charge")) then
        item:getTag():putDouble("charge", 0)
    end
    return item:getTag():getDouble("charge")
end

---@param charge number
---@param player Player
---@param item ItemStack
---@param blockEntity BlockEntity
local maxChargeGetter = function(player, item, blockEntity)
    if (not item:getOrCreateTag():contains("maxCharge")) then
        item:getTag():putDouble("maxCharge", 100)
    end
    return item:getTag():getDouble("maxCharge")
end

local chargeSetter = function(charge, player, item, blockEntity)
    item:getTag():putDouble("charge", charge)
end
local maxChargeSetter = function(charge, player, item, blockEntity)
    item:getTag():putDouble("maxCharge", charge)
end


-- Testing core (shulker shell, charge by eating chorus fruit nearby obelisk, or have enderman teleport)
--local builder = Builder:create()
--builder:chargeGetter(chargeGetter)
--builder:maxChargeGetter(maxChargeGetter)
--builder:chargeSetter(chargeSetter)
--builder:maxChargeSetter(maxChargeSetter)
--builder:withItem("minecraft:shulker_shell")
--builder:withInteraction("respawnobelisks:cool_interaction")
--builder:build()