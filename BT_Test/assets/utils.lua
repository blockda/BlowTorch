
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

function updateRoomInfo()
	room = gmcpTable("room.info")
	
	--check to see if it is a mappable room
	if(room.num == -1) then
		--probably some more cases to check. Kind of looks like this one is the easiest.
		return
	end
	
	--check to see if we already have this room.
	--local check = row("_id")
	---Note(room.num)
	--Note("MAPPER ATTEMPTING TO MAP ROOM: "..room.num)
	cur = db:rawQuery("SELECT _id FROM rooms WHERE _id="..room.num,nil)
	--Note("Attempting to query")
	count = cur:getCount()
	--Note("query returned: "..count.." rows.")
	if(count > 0) then
		Note("MAPPER: already mapped room:"..room.num)
		return
	end
	
	local vals_tmp = {_id=room.num,name=room.name,zone=room.zone,terrain=room.terrain,details=room.details,cont_id=room.coord.id,x=room.coord.x,y=room.coord.y,cont_room=room.coord.cont}
	local vals = fields(vals_tmp)
	
	db:insert("rooms",nil,vals)
	
	for key,value in pairs(room.exits) do
		if(value ~= "") then
			local e_tmp = {command=key,destination=value}
			local e = fields(e_tmp)
			db:insert("exits",nil,e)
		else 
			Note("Not mapping exits. Room: "..room.num.." is a maze.")
		end
	end

end

function dumpRooms()
	local rows = row("_id","name","zone","terrain")
	local cur = db:query("rooms",rows,nil,nil,nil,nil,nil)
	
	local i = 0
	repeat
		cur:moveToNext()
		local id = cur:getString(0)
		local name = cur:getString(1)
		local zone = cur:getString(2)
		local terrain = cur:getString(3)
		Note(id.."=> "..name.." zone:"..zone.." terrain:"..terrain);
		i = i + 1
	until cur:isLast()==true
	cur:close()

end

function dumpExits()
	local rows = row("_id","command","destination")
	local cur = db:query("exits",rows,nil,nil,nil,nil,nil)
	
	local i = 0
	repeat
		cur:moveToNext()
		local id = cur:getString(0)
		local name = cur:getString(1)
		local destination = cur:getString(2)
		Note(id.."=>"..name.." dest:"..destination);
		i = i + 1
	until cur:isLast()==true
	cur:close()

end