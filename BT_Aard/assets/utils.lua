
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

function updateRoomInfo(room)
	--room = gmcpTable("room.info")
	--now we are called directly from java, with an argument in room.
	--check to see if it is a mappable room
	--Note("Staring room save")
	if(room.num == -1) then
		--probably some more cases to check. Kind of looks like this one is the easiest.
		return --room not mappable
	end
	
	--check to see if we already have this room/area
	cur = db:rawQuery("SELECT uid FROM rooms WHERE uid="..room.num,nil)
	count = cur:getCount()
	if(count > 0) then
		Note("MAPPER: already mapped room:"..room.num)
		return
	end
	cur:close()
	cur = nil;
	--Note("Starting room save")
	local vals_tmp = {uid=room.num,name=room.name,area=room.zone,terrain=room.terrain,info=room.details,x=room.coord.x,y=room.coord.y}
	local vals = fields(vals_tmp)
	
	db:insert("rooms",nil,vals)
	--Note("Room Inserted - Doing Exits")
	
	for key,value in pairs(room.exits) do
		if(value == nil or value ~= "") then
			local e_tmp = {fromuid=room.num,dir=key,touid=value}
			local e = fields(e_tmp)
			db:insert("exits",nil,e)
		else 
			Note("Not mapping exits. Room: "..room.num.." is a maze.")
		end
	end
	--Note("exits finished")

end

function dumpRooms()
	local rows = row("_id","name","area","terrain")
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
		sup = rowify(cur)
		cur:moveToNext()
		local id = cur:getString(0)
		local name = cur:getString(1)
		local destination = cur:getString(2)
		Note(id.."=>"..name.." dest:"..destination);
		i = i + 1
	until cur:isLast()==true
	cur:close()

end
  
  
  function cofun()
  	coroutine.yield(10)
  	coroutine.yield(20)
  	coroutine.yield(30)
  end  
  
  
  function nrows(s)
  	
  	local cursor = db:rawQuery(s,nil)
  	local helper = {}
  	setmetatable(helper, {
  		__index=function(t,name)
  			index = cursor:getColumnIndex(name)
  			if(index == -1) then
  				return "No such column: "..name
  			end
  			var = cursor:getString(index)
  			return var
  		end
  	})
  	count = cursor:getCount()
  	if(count < 1) then
  			return "No results returned for query: "..s
  	end
  	return function()
  		moved = cursor:moveToNext()
  		if(not moved) then
  			cursor:close()
  			return
  		end
  		return helper 		
  	end
  
  end
  
  function betterDumpRows()
  
  	
  	for room in nrows("SELECT * FROM rooms") do
  		
  		Note(room["name"].." ["..room["zone"].."]:"..room["terrain"])
  	end
  	
  end
  
  function createTables() 
   --android only supports single queries. this is going to suuuuuck.
   db:rawQuery("PRAGMA foreign_keys = ON",nil)
   
   
   --db:rawQuery("PRAGMA journal_mode=WAL",nil)
 
   db:execSQL([[
   CREATE TABLE IF NOT EXISTS areas (
      _id      INTEGER PRIMARY KEY AUTOINCREMENT,
      uid         TEXT    NOT NULL,   -- vnum or how the MUD identifies the area
      name        TEXT,               -- name of area
      date_added  DATE,               -- date added to database
      texture     TEXT,               -- background area texture
      color       TEXT,               -- ANSI colour code.
      UNIQUE (uid)
   );
   
   
   ]])
   
   db:execSQL("CREATE INDEX IF NOT EXISTS areas_uid_index ON areas (uid);")
   db:execSQL("CREATE INDEX IF NOT EXISTS areas_name_index ON areas (name);")
 
   db:execSQL([[
      CREATE TABLE IF NOT EXISTS environments (
      _id INTEGER PRIMARY KEY AUTOINCREMENT,
      uid           TEXT    NOT NULL,   -- code for the environment
      name          TEXT,               -- name of environment
      color         INTEGER,            -- ANSI colour code
      date_added    DATE,               -- date added to database
      UNIQUE (uid)
   );]])
  
   db:execSQL("CREATE INDEX IF NOT EXISTS name_index ON environments (name);")
   
   db:execSQL([[
      CREATE TABLE IF NOT EXISTS rooms (
      _id        INTEGER PRIMARY KEY AUTOINCREMENT,
      uid           TEXT NOT NULL,   -- vnum or how the MUD identifies the room
      name          TEXT,            -- name of room
      area          TEXT,            -- which area
      building      TEXT,            -- which building it is in
      terrain       TEXT,            -- eg. road OR water
      info          TEXT,            -- eg. shop,healer
      notes         TEXT,            -- player notes
      x             INTEGER,
      y             INTEGER,
      z             INTEGER,
      date_added    DATE,            -- date added to database
      UNIQUE (uid),
      FOREIGN KEY(area) REFERENCES areas(uid)
   );
   ]])
   
   db:execSQL("CREATE INDEX IF NOT EXISTS info_index ON rooms (info);")
   db:execSQL("CREATE INDEX IF NOT EXISTS terrain_index ON rooms (terrain);")
   db:execSQL("CREATE INDEX IF NOT EXISTS area_index ON rooms (area);")
   db:execSQL("CREATE INDEX IF NOT EXISTS rname_index ON rooms (name);")
 
   db:execSQL([[
   CREATE TABLE IF NOT EXISTS exits (
   	  _id		INTEGER PRIMARY KEY AUTOINCREMENT,
      dir         TEXT    NOT NULL, -- direction, eg. "n", "s"
      fromuid     STRING  NOT NULL, -- exit from which room (in rooms table)
      touid       STRING  NOT NULL, -- exit to which room (in rooms table)
      level       STRING  NOT NULL DEFAULT '0', -- minimum level to make use of this exit
      date_added  DATE,             -- date added to database
      --PRIMARY KEY(fromuid, dir),
      FOREIGN KEY(fromuid) REFERENCES rooms(uid));
   ]])
   db:execSQL("CREATE INDEX IF NOT EXISTS fromuid_index ON exits (fromuid);")
   db:execSQL("CREATE INDEX IF NOT EXISTS touid_index   ON exits (touid);")
  end
  
  function dumpMaster() 
  		--nrows may be a bit to flashy to use here.
  		local cur = db:rawQuery("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name",nil)
  		--cur:moveToNext()
  		while (cur:moveToNext()) do
  			local val = cur:getString(0)
  			Note("DB TABLE: "..val)
  		end
  		cur:close()
  		cur=nil;
  end
  
   function dumpIndicies() 
  		--nrows may be a bit to flashy to use here.
  		local cur = db:rawQuery("SELECT name FROM sqlite_master WHERE type='index' ORDER BY name",nil)
  		--cur:moveToNext()
  		while (cur:moveToNext()) do
  			local val = cur:getString(0)
  			Note("DB TABLE: "..val)
  		end
  		cur:close()
  		cur=nil;
  end

