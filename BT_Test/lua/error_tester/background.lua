
function got_gmcp_room(room)
	checkPoint("gmcp callback"..nilglobal)
end

function OnOptionChanged(key,value)
	checkPoint("option changed"..nilglobal)
end

function OnBackgroundStartup()
	checkPoint("background startup"..nilglobal)
	--ScheduleCallback()
end

function OnXmlExport()
	checkpoint("xml export"..nilglobal)
end

function OnPrepareXML()
	checkPoint("xml import"..nilglobal)
end

function triggered_line()
	checkPoint("trigger callback"..nilglobal)
end

function callbackTest()
	checkPoint("special command callback"..nilglobal)
end

function checkPoint(str)
	Note("\n.."..str.." checkpoint passed.")
end

function startXCallTest()
	WindowXCallS("error_window","xcallSTest","foo")
	WindowXCallB("error_window","xcallBTest","foo")
	checkPoint("xcall test"..nilglobal)
end

RegisterSpecialCommand("test","callbackTest")
