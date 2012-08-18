--debugPrint("package path:"..package.path)
--package.path = "/mnt/sdcard/BlowTorch/?.lua"

require("serialize")
--make a button.
--Note("in the chat server")

chatWindow = GetWindowTokenByName("chats")
AppendWindowSettings("chats")
chatWindowName = "chats"

currentChannel = "main"
buffers = {}
buffers[currentChannel] = chatWindow:getBuffer()

--altbuffer = luajava.newInstance("com.offsetnull.bt.window.TextTree");
--altbuffer:addString("i am the alternate buffer, rock on.")

--foobuffer = luajava.newInstance("com.offsetnull.bt.window.TextTree");
--foobuffer:addString("this is the foo buffer, buffffererere.")

--buffers["alt"] = altbuffer;
--buffers["filler"] = foobuffer;
--buffers["lots"] = foobuffer;
--buffers["and"] = foobuffer;
--buffers["lots"] = foobuffer;
--buffers["more"] = foobuffer;
--buffers["channels"] = foobuffer;
--buffers[""] = foobuffer;
--buffers["channels"] = foobuffer;




function updateSelection(newChannel)

	if(newChannel == currentChannel) then
		return
	end
	
	--update currenChannel to newChannel
	currentChannel = newChannel
	--get the appropriate channel buffer from the buffers table.
	buffer = buffers[currentChannel]
	--update the chat window (hypothetically, through
	chatWindow:setBuffer(buffer)
	
	InvalidateWindowText(chatWindowName)

end

tmpmap = {}
function updateUIButtons()
	tmpmap = {}
	for i,b in pairs(buffers) do
		tmpmap[i] = "foo"
		--table.insert(tmp,i)
		
	end

	WindowXCallS(chatWindowName,"loadButtons",serialize(tmpmap))
end

function initReady(arg)
	--arg is meaningless here.
	updateUIButtons()
end

function processChat(name,line,replaceMap)
	
	--get chat channel from replacementMap
	channel = replaceMap["1"]
	
	if(currentChannel == "main") then
		AppendLineToWindow(chatWindowName,line)
	else
		mainBuffer = buffers["main"]
		mainBuffer:appendLine(line)
	end

	--if appropriate channel already has a sub buffer.
	if(channel ~= nil) then
		--append this line to it
		
		if(currentChannel == channel) then
			AppendLineToWindow(chatWindowName,line)
		else
			channelBuffer = buffers[channel]
			if(channelBuffer == nil) then
				channelBuffer = luajava.newInstance("com.offsetnull.bt.window.TextTree")
				--channelBuffer:appendLine(line)
				buffers[channel] = channelBuffer
				updateUIButtons()
			end
			channelBuffer:appendLine(line)
		end
	else
		--create a new buffer
		newchannel = luajava.newInstance("com.offsetnull.bt.window.TextTree")
		--attatch a copy of this line to the new buffer
		newchannel:appendLine(line)
		--keep track of new buffer in buffer map, under the name of the matched channel.
		buffers[channel] = newchannel
		updateUIButtons()
	end

	--append this line to the main buffer, this always happens.
	--mainBuffer = buffers["main"]
	
end

function OnOptionChanged(key,value)
	--Note("\n chatwindow in on option changed, key:"..key.."\n")
	local func = optionsTable[key]
	if(func ~= nil) then
		func(value)
	end
end

function setWindowSize(size)
	--debugPrint("in setWindowSize()")
	local layouts = chatWindow:getLayouts()
	local keys = layouts:keySet()
	local iterator = keys:iterator()
	while(iterator:hasNext()) do
		local key = iterator:next()
		local layoutGroup = layouts:get(key)
		layoutGroup:setPortraitHeight(tonumber(size))
		layoutGroup:setLandscapeHeight(tonumber(size))
	end
	if(UserPresent()) then
		WindowXCallS(chatWindowName,"setWindowSize",tostring(size))
	end
end

optionsTable = {}
optionsTable.height = setWindowSize









