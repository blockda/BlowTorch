--debugPrint("package path:"..package.path)
package.path = "/mnt/sdcard/BlowTorch/?.lua"

require("serialize")
--make a button.
debugPrint("in the chat server")

chatWindow = NewWindow("chats",0,0,1280,177,"chatwindow")
chatWindowName = chatWindow:getName()

currentChannel = "main"
buffers = {}
buffers[currentChannel] = chatWindow:getBuffer()

altbuffer = luajava.newInstance("com.happygoatstudios.bt.window.TextTree");
altbuffer:addString("i am the alternate buffer, rock on.")

foobuffer = luajava.newInstance("com.happygoatstudios.bt.window.TextTree");
foobuffer:addString("this is the foo buffer, buffffererere.")

buffers["alt"] = altbuffer;
buffers["foo"] = foobuffer;
buffers["right"] = foobuffer;
buffers["slap"] = foobuffer;
buffers["biatch"] = foobuffer;
buffers["must"] = foobuffer;
buffers["make"] = foobuffer;
buffers["more"] = foobuffer;
buffers["channels"] = foobuffer;




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
	
	invalidateWindowText(chatWindowName)

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
		appendLineToWindow(chatWindowName,line)
	else
		mainBuffer = buffers["main"]
		mainBuffer:appendLine(line)
	end

	--if appropriate channel already has a sub buffer.
	if(channel ~= nil) then
		--append this line to it
		
		if(currentChannel == channel) then
			appendLineToWindow(chatWindowName,line)
		else
			channelBuffer = buffers[channel]
			if(channelBuffer == nil) then
				channelBuffer = luajava.newInstance("com.happygoatstudios.bt.window.TextTree")
				--channelBuffer:appendLine(line)
				buffers[channel] = channelBuffer
				updateUIButtons()
			end
			channelBuffer:appendLine(line)
		end
	else
		--create a new buffer
		newchannel = luajava.newInstance("com.happygoatstudios.bt.window.TextTree")
		--attatch a copy of this line to the new buffer
		newchannel:appendLine(line)
		--keep track of new buffer in buffer map, under the name of the matched channel.
		buffers[channel] = newchannel
		updateUIButtons()
	end

	--append this line to the main buffer, this always happens.
	--mainBuffer = buffers["main"]
	
end











