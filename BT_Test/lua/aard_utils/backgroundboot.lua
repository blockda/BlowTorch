--load scripts

--only one so far.


function testCommandHandler(args)
	--debugPrint("Inside the testCommandHandler yo, peep dis raw deal!"..args)
end

RegisterSpecialCommand("testcommand","testCommandHandler")

function demoCommand(args)
	Note("doing scripts")
	num = tonumber(args)
	if(num ~= nil) then
		WindowXCallS("layout_manager","demo",tostring(num))
	end
end

RegisterSpecialCommand("demo","demoCommand")

--debugPrint("button server functions loaded")
--set up windows.
--MainWindowSize(0,177,880,500)
--modding the main window is much harder now.
function OnBackgroundStartup()
	--debugPrint("backgroundStartup for mainboot.lua")

end

function LoadLayout(layout)
	WindowXCallS("layout_manager","LoadLayout",layout)
end
--NewWindow("lua_window",880,577,400,100,"windowscript")


--debugPrint("startup script loaded!")