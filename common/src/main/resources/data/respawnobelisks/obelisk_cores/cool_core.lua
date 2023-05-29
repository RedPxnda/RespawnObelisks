local chargeGetter = function(player, item, blockEntity)
    return item:getTag():getDouble("charge")
end
local maxChargeGetter = function(player, item, blockEntity)
    return item:getTag():getDouble("maxCharge")
end
local chargeSetter = function(charge, player, item, blockEntity)
    item:getTag():putDouble("charge", charge)
end
local maxChargeSetter = function(charge, player, item, blockEntity)
    item:getTag():putDouble("maxCharge", charge)
end

local builder = Builder:create()
builder:chargeGetter(chargeGetter)
builder:maxChargeGetter(maxChargeGetter)
builder:chargeSetter(chargeSetter)
builder:maxChargeSetter(maxChargeSetter)
builder:withItem("minecraft:shulker_shell")
builder:withInteraction("respawnobelisks:cool_interaction")
builder:build()