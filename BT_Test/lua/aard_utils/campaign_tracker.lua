
local buffer = luajava.newInstance("com.offsetnull.bt.window.TextTree")

local ansi = "\27["
local dcyan = "\27[0;36m"
local bwhit = "\27[37;1m"
local darkgreen = "\27[0;32m"
local nwhit = "\27[0;37m"

local area_exists = {}
local areas = {}
local mobs = {}

local token = GetWindowTokenByName("campaign_target_window")

function processTarget(name,line,map)
	--Note("\nprocessing line\n")
	local name = map["1"]
	local area = map["2"]
	local dead = false
	if area:find(" - Dead",-7) then
		dead = true
		--zone = zone:sub(1,-7)
	end

	--deal with dead later
	if(not area_exists[area]) then
		table.insert(areas,area)
		area_exists[area] = true
	end
	
	if(not mobs[area]) then
		mobs[area] = {}
	end
	
	table.insert(mobs[area],name)
end

function resetTracker()
	--Note("\nresetting tracker\n")
	areas = {}
	area_exists = {}
	mobs = {}
	buffer:empty()
	token:setBuffer(buffer)
	--InvalidateWindowText(token:getName())
	SendToServer("campaign check")
	DeleteTriggerGroup("mobs")
	EnableTrigger("grabber",true)
	EnableTrigger("end",true)
end

function endCapture(name,line,map)
	--build the string, add it to the tree and send it off to be drawn
		--Note("\nending capture line\n")
	EnableTrigger("end",false)
	EnableTrigger("grabber",false)
    table.sort(areas)
    --for i,n in ipairs(a) do print(n) end
	
	for i,area in ipairs(areas) do
		--get the mobs and load them up
		buffer:addString(string.format("%s%s%s\n",bwhit,dcyan,area))
		local list = mobs[area]
		for i,mob in ipairs(list) do
			buffer:addString(string.format("%s  %s\n",darkgreen,mob))
		end
	end
	
	token:setBuffer(buffer)
	InvalidateWindowText(token:getName())
	WindowXCallS(token:getName(),"requestLayout","now")
	
	--create a new trigger with a background highlight mod for each mob in the list.
	local name = "mob_%d"
	local format = string.format
	local count = 1
	for i,area in ipairs(areas) do
		local list = mobs[area]
		for i,mob in ipairs(list) do
			if(mob:find("^a ") or mob:find("^the ")) then
				mob = mob:gsub("^%l", string.upper)
			end
			NewTrigger(format(name,count),mob,{regexp=false,group="mobs",enabled=true},{type="color",background=18,foreground=247,fire="always"})
			count = count + 1
		end
	end
end

function OnBackgroundStartup()
	Note("campaign tracker installed\n")
	--DeleteTriggerGroup({foo="bar"})
	DeleteTriggerGroup("mobs")
	EnableTrigger("end",false)
	EnableTrigger("grabber",false)
end