--debugPrint("package path:"..package.path)
package.path = "/mnt/sdcard/BlowTorch/?.lua"

--make a button.
debugPrint("in the chat server")

chatWindow = NewWindow("chats",0,0,1280,177,"chatwindow")
chatWindowName = chatWindow:getName()

currentChannel = "main"
buffers = {}
buffers.currentChannel = chatWindow:getBuffer()

buffers["alt"] = chatWindow:getBuffer()

function updateSelection(newChannel)

	if(newChannel == currentChannel)
		return
	end
	
	--update currenChannel to newChannel
	currentChannel = newChannel
	--get the appropriate channel buffer from the buffers table.
	buffer = buffers.currentChannel
	--update the chat window (hypothetically, through
	chatWindow:setBuffer(buffer)
	
	invalidateWindowText(chatWindowName)

end

function updateUIButtons()
	tmp = {}
	for i,b in pairs(buffers) do
		tmp.i = "foo"
		--table.insert(tmp,i)
		
	end

	WindowXCallS(chatWindowName,"loadButtons",serialize(tmp))
end

function processChat(line,replaceMap)
	
	--get chat channel from replacementMap
	channel = replaceMap.1
	
	--if appropriate channel already has a sub buffer.
	if(channel ~= nil) then
		--append this line to it
		channel:appendLine(line)
	else
		--create a new buffer
		newchannel = luajava.newInstance("com.happygoatstudios.bt.window.TextTree")
		--attatch a copy of this line to the new buffer
		newchannel:appendLine(line)
		--keep track of new buffer in buffer map, under the name of the matched channel.
		buffers.channel = newchannel
		updateUIButtons()
	end

	--if currentWindow is affected by this update, e.g. either main window or the sub-channel selection
	if(currentChannel == "main" or currentChannel == channel) then
		--update the window.
		appendLineToWindow(chatWindowName,line)
	else
		--signal the plugin to make the "new notification" mark show.
	end
	
end











