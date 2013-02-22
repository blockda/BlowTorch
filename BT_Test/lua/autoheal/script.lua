
hp_threshold_low = 0.8
healcmd = "get ballad pbag;quaff ballad"
debug_output = true

sent_heal = false

function incomingdata(name,line,replacemap)
	local curhp = replacemap["1"]
	local maxhp = replacemap["2"]
	local pcthp = curhp / maxhp
	
	if(debug_output) then
		Note(string.format("\nAuto Heal: data = %d/%d %d%%, threshold %d%%\n",curhp,maxhp,pcthp*100,hp_threshold_low*100))
	end
	-- Need to determine whether the command has been sent and received by the server before issuing it again
		        	
	if (pcthp < hp_threshold_low ) then
		if (not sent_heal) then
			SendToServer(healcmd)
			sent_heal = true
		end
	end 
	
end 

function heal_received()
	sent_heal = false
	if(debug_output) then
		Note("\nAuto Heal: Heal recieved\n")
	end
end

-- Everything below here handles the options
function OnOptionChanged(key,value)
	local func = optionsTable[key]
	if(func ~= nil) then
		func(value)
	end
end

function setHealString(val)
	healcmd = val
end

function setHpThresh(val)
	val = tonumber(val)
	--Note(string.format("\nSetting hp threshold, incoming, %d\n",val))
	hp_threshold_low = val / 100.0
	
	--Note(string.format("\nSetting hp threshold, outgoing, %d\n",hp_threshold_low))
end

function setDebug(val)
	if(val == "true") then
		debug_output = true
	else
		debug_output = false
	end
end

optionsTable = {}
optionsTable.hp_threshold = setHpThresh
optionsTable.heal_cmd = setHealString
optionsTable.debug = setDebug