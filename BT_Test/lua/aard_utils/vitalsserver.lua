require("serialize")
			--vitalsWindow = NewWindow("vitals_window",880,577,400,100,"vitalsWindowScript")
		
vitals = {}
vitals.hp = 100
vitals.mana = 100
vitals.enemypct = 0
			
maxes = {}
maxes.hp = 100
maxes.mana = 100
			
enemyPct = 100
			
align = 0
maxAlign = 2500
minAlign = -2500
			
tnl = 0
toLevel = 0
		
fighting = false
			
function updateVitals(newVitals)
	vitals.hp = newVitals.hp
	vitals.mana = newVitals.mana
	if(fighting) then
		vitals.enemypct = enemyPct
	else
		vitals.enemypct = 0
	end
	WindowXCallS("vitals_window","updateVitals",serialize(vitals))
end
			
function updateMaxes(newMaxes)
	maxes.hp = newMaxes.maxhp
	maxes.mana = newMaxes.maxmana
	WindowXCallS("vitals_window","updateMaxes",serialize(maxes))
end
			
function updateStatus(newStatus)
	--debugPrint("player status updated!!!!")
	if(newStatus.enemypct == nil) then
		if(fighting) then
			--debugPrint("combat over")
			fighting = false
		else
			--debugPrint("combat not started")
		end
		WindowXCallS("vitals_window","updateEnemyPercent",0)
	else
		if(not fighting) then
			fighting = true
		end
		enemyPct = newStatus.enemypct
		--debugPrint("enemyPct: "..newStatus.enemypct)
		WindowXCallS("vitals_window","updateEnemyPercent",enemyPct)
	end
				
	if(newStatus.align ~= align) then
		align = newStatus.align
		WindowXCallS("vitals_window","updateAlign",align)
	end
				
	if(newStatus.tnl ~= tnl) then
		tnl = newStatus.tnl
		WindowXCallS("vitals_window","updateTNL",tnl)
	end
end
			
function updateBase(newBase)
	if(newBase.perlevel ~= toLevel) then
		toLevel = newBase.perlevel
		WindowXCallS("vitals_window","updateMaxPerLevel",toLevel)
	end
end
		
function initReady(str)
	info = {}
	info.hp = vitals.hp
	info.mp = vitals.mana
	info.maxhp = maxes.hp
	info.maxmp = maxes.mana
	info.enemypct = enemyPct
	info.tnl = tnl
	info.tolevel = toLevel
	info.align = align
	WindowXCallS("vitals_window","updateAll",serialize(info))
end

function OnBackgroundStartup()			
	Send_GMCP_Packet("request char")
end