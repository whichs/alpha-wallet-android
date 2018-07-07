--
-- Created by IntelliJ IDEA.
-- User: James
-- Date: 14/06/2018
-- Time: 10:50 PM
-- To change this template use File | Settings | File Templates.
--

function fact (n)
    if n == 0 then
        return 1
    else
        return n * fact(n-1)
    end
end

--function trial (n)
--    if n == 0 then
--        return 2
--    else
--        return n * fact(n-1)
--    end
--end