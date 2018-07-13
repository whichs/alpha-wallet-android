--
-- Created by IntelliJ IDEA.
-- User: James
-- Date: 14/06/2018
-- Time: 10:50 PM
-- To change this template use File | Settings | File Templates.
--


function trial (n)
    if n == 0 then
        return 1
    else
        return n * trial(n-1)
    end
end

function ticketElement (n, v)
    Log:i("LuaLog", n.." : "..v)
    local value = Tickfun:getValue(v)
    local file1 = Tickfun:loadResource("https://upload.wikimedia.org/wikipedia/commons/a/a4/Treasure_chest_color.png")
    local file2 = Tickfun:loadResource("https://upload.wikimedia.org/wikipedia/commons/thumb/b/b7/ETHEREUM-YOUTUBE-PROFILE-PIC.png/768px-ETHEREUM-YOUTUBE-PROFILE-PIC.png")
    local file3 = Tickfun:loadResource("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT1w3Xhd4WOJets35yh3NXGJ4ZOUX11X0EsJ377MpLQ_AqXoCXAtQ");
    if n == nil then
        return '';
    elseif n == 'Category 1' then
        Tickfun:defineTargetBackground(file3)
        return 'Workers standing pens '..value
    elseif n == 'Category 2' then
        Tickfun:defineTargetBackground(file1)
        return 'Bourgois luxury seats '..value
    else
        return n
    end
end

-- try downloading an image
function funccaller (n)
    local retval = 'Workers standing pens'
    local value = 4 --ticfun.getValue(4)

    if n == 'Category 1' then
        return retval .. value
    elseif n == 'Category 2' then
        return 'Bourgois luxury seats' .. value
    else
        return n
    end
end

--if name == 'Category 1' then
--    return 'Small peasant stalls'
--elseif name == 'Category 2' then
--    return 'Bourgois luxury seats'
--else
--    return n
--end

