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
	mainwindow = GetWindowTokenByName("mainDisplay")
	LAYOUT_TYPE = luajava.bindClass("com.offsetnull.bt.service.LayoutGroup$LAYOUT_TYPE")
	layouts = mainwindow:getLayouts()
	layouts:clear()
	newlayoutgroup = luajava.newInstance("com.offsetnull.bt.service.LayoutGroup")
	newlayoutgroup:setType(LAYOUT_TYPE.normal)
	RelativeLayout = luajava.bindClass("android.widget.RelativeLayout")
	LayoutParams = luajava.bindClass("android.widget.RelativeLayout$LayoutParams")
	params = luajava.new(LayoutParams,LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT)
	--params:addRule(RelativeLayout.ABOVE,100)
	params:addRule(RelativeLayout.ABOVE,10)
	params:addRule(RelativeLayout.ALIGN_PARENT_LEFT)
	params:addRule(RelativeLayout.BELOW,5010) --chat window
	params:addRule(RelativeLayout.LEFT_OF,6010) --scroll-holder
	newlayoutgroup:setLandscapeParams(params)
	newlayoutgroup:setPortraitParams(params)
	layouts:put(newlayoutgroup:getType(),newlayoutgroup)
end

--NewWindow("lua_window",880,577,400,100,"windowscript")


--debugPrint("startup script loaded!")