
function mult(value)
for t=1,value*3 do
	Note("I AM LUA!")
end
return value*3
end

function auto(cmd,start,stop,target)
for t=start,stop do
	Note(cmd.." "..t.."."..target)
end

end

function dumpDB()
	local rows = row("_id","name")
	local cur = db:query("rooms",rows,nil,nil,nil,nil,nil)
	
	local i = 0
	repeat
		cur:moveToNext()
		local id = cur:getString(0)
		local name = cur:getString(1)
		
		Note("ENTRY "..i.."=> "..id..":"..name);
		i = i + 1
	until cur:isLast()==true

end
