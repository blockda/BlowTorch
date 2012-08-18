
Button = luajava.bindClass("android.widget.Button")
R_layout = luajava.bindClass("com.offsetnull.bt.R$layout")
R_id = luajava.bindClass("com.offsetnull.bt.R$id")
View = luajava.bindClass("android.view.View");
RelativeLayoutParams = luajava.bindClass("android.widget.RelativeLayout$LayoutParams")
RelativeLayout = luajava.bindClass("android.widget.RelativeLayout")
Color = luajava.bindClass("android.graphics.Color")
MenuItem = luajava.bindClass("android.view.MenuItem")

density = GetDisplayDensity()

function OnSizeChanged()
	checkpoint("OnSizeChanged"..nilglobal)
end

function OnDraw(c)
	c:drawColor(Color:argb(255,180,10,180))
	checkpoint("OnDraw"..nilglobal)
end

function OnCreate()
	ScheduleCallback(100,"scheduledCallback",1000)
	checkpoint("OnCreate"..nilglobal)
end

itemClicker = {}
function itemClicker.onClick(v)
	--local foo = function()
	--	checkpoint("callback protected mode test"..nilglobal)
	--end
	
	--local error = function()
	--	local str = debug.traceback()
	--	Note("\nError in proxy callback onClick"..str.."\n"..nilglobal)
	--end
	
	--if(pcall(foo,error)) then
		checkpoint("proxy callback test point passed"..nilglobal)
	--end
end
itemClicker_cb = luajava.createProxy("android.view.View$OnClickListener",itemClicker)

function PopulateMenu(menu)
	if(menuItem == nil) then
		menuItem = luajava.new(Button,view:getContext())
		--local p = luajava.new(ActionBarLayoutParams,200*density,ActionBarLayoutParams.WRAP_CONTENT)
		--menuItem:setLayoutParams(p)
		menuItem:setText("Test")
		menuItem:setMinWidth(200*density)
		--menuItem:setAdapter(filterAdapter_cb)
		--menuItem:setThreshold(1)
		menuItem:setOnClickListener(itemClicker_cb)
	end
	local item = menu:add(0,403,403,"Error Test")
	item:setActionView(menuItem)
	item:setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
	checkpoint("PopulateMenu"..nilglobal)
end

function OnDestroy()
	checkpoint("OnDestroy"..nilglobal)
end

function scheduledCallback()
	checkpoint("Scheduled Callback function test passed"..nilglobal)
end

function xcallSTest()
	PluginXCallS("PluginXCallSTest","foo")
	checkpoint("WindowXCallS test"..nilglobal)
end

function xcallBTest()
	checkpoint("WindowXCallB test"..nilglobal)
end

function checkpoint(str)
	Note("\n"..str.." checkpoint passed.\n")
end

function onMenuStackBack()
	PopMenuStack()
	checkpoint("Menu Back Stack Pressed Handler"..nilglobal)
end

PushMenuStack("onMenuStackBack")
PluginXCallS("startXCallTest","foo")

checkpoint("end of parsing "..nilglobal)