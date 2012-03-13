package.path = "/mnt/sdcard/BlowTorch/?.lua"
require("serialize")

TextTree = luajava.bindClass("com.happygoatstudios.bt.window.TextTree")
mapWindow = GetWindowTokenByName("map_window")
buffer = luajava.new(TextTree)
buffer:setLinkify(false)
--buffer:setBold(true)
--buffer:setLineBreakAt(80)
mapstarted = false
function startMapCapture(name,line,map) 
	--Note("STARTING MAP CAPTURE")
	TriggerEnabled("map_capture",true)
	str = TextTree:deColorLine(line)
	--debugPrint("processing line: "..str:toString())
	mapstarted = true
	titlefound = false
	headers.title = ""
	currentline = 19
	buffer:empty()
	return true
end


headers = {}
headers.title = nil
headers.exits = nil

currentline = 19
titlefound = false
function doMapCapture(name,line,map)
	--debugPrint("doing map capture")
	if(mapstarted) then
		local titletmp = TextTree:deColorLine(line)
		if(titlefound) then
			headers.title = headers.title .. titletmp:toString()
			currentline = currentline -1
		else
			headers.title = titletmp:toString()
			titlefound = true
		end
		
		--debugPrint("mapwindowdebug:|"..titletmp:toString().."|")
		if(titletmp:toString() == " ") then
			--debugPrint("found whitespace")
			--titlefound = true
			buffer:appendLine(line)
			mapstarted = false
			--debugPrint("pulling title line:"..headers.title)
			currentline = currentline - 1
		end
		
		
	else
		if(currentline == 0) then
		
			local exitstmp = TextTree:deColorLine(line)
			headers.exits = exitstmp:toString()
			--debugPrint(currentline.."pulling footer line:"..headers.exits)
			
		--elseif(currentline >=2 and currentline <= 3) then
		--actually we just want to gag these for our own purpose.
		--	debugPrint("line gagged"..currentline)
		--elseif(currentline <= 19 and currentline >=16) then
		--gag these too 
		--debugPrint("line gagged"..currentline)
		else
			--local dbgstr = TextTree:deColorLine(line)
			--debugPrint(currentline.."pulling regular map line:"..dbgstr:toString())
			buffer:appendLine(line)
		end
		currentline = currentline -1
	end
	
	
	

end

function endMapCapture() 
	--Note("ENDING MAP CAPTURE")
	TriggerEnabled("map_capture",false)
	
	mapWindow:setBuffer(buffer)
	
	WindowXCallS("map_window","updateHeaders",serialize(headers))
	
	invalidateWindowText("map_window")
	--DrawWindow("map_window")
	debugPrint("ending map capturing and drawing window")
	--buffer:empty()
	return true
end
--NewWindow("map_window",880,177,400,400)
--WindowBuffer("map_window",true)