local handler = function(blockEntity, message)
    blockEntity:chargeAndAnimate(10)
    local location = message:source()
    message:context():sourceEntity():teleportTo(location:x(), location:y(), location:z())
    return true;
end

Interactions:create("respawnobelisks:cool_interaction", "teleport", handler)