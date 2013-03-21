package.path = package.path..";"..GetPluginInstallDirectory().."/?.lua"
require("miniwindow")

local luajava = luajava
local view = view

local TextView = luajava.bindClass("android.widget.TextView")
local Gravity = luajava.bindClass("android.view.Gravity")
local TruncateAt = luajava.bindClass("android.text.TextUtils$TruncateAt")
local MeasureSpec = luajava.bindClass("android.view.View$MeasureSpec")
--parent = view:getParent()
--parent:removeView(view)
--scroller = parent:findViewById(6010)
--holder = scroller:getChildAt(0)
--holder:addView(view)
local measurespec_height = -1
local measurespec_width = -1
local measured_height = -1
local measured_width = -1
local density = view:getResources():getDisplayMetrics().density
function OnMeasure(wspec,hspec)	
	if(wspec == measurespec_width and hspec == measurespec_height) then return measured_width,measured_width end
	--Note(string.format("measurespecs: %d, %d\n",wspec,hspec))
	measurespec_width = wspec
	measurespec_height = hspec
	--assumptions, MeasureSpec:getMode(hspec) == Undefined, MeasureSpec:getMode(wspec) == EXACTLY
	--this will always return a square window
	measured_width = MeasureSpec:getSize(wspec)
	return measured_width,measured_width
end

view:fitFontSize(28)

view:setLinksEnabled(false)
view:setTextSelectionEnabled(false)
view:setScrollingEnabled(false)
view:setBold(true)
--view:setCharacterSizes(21,1)
local parent = view:getParent()

exitsLabel = luajava.new(TextView,view:getContext())
exitsLabel:setTextSize(22)
exitsLabel:setText("BELOW THE MAP")
exitsLabel:setTextColor(Color:argb(255,15,200,15))
exitsLabel:setGravity(Gravity.CENTER)
exitsLabel:setId(6022)
--holder:addView(label)

titleLabel = luajava.new(TextView,view:getContext())
titleLabel:setTextSize(25)
titleLabel:setText("ABOVE THE MAP")
titleLabel:setTextColor(Color:argb(255,15,200,15))
titleLabel:setGravity(Gravity.CENTER)
titleLabel:setId(6021)
titleLabel:setLines(1)
titleLabel:setHorizontallyScrolling(true)
titleLabel:setFocusableInTouchMode(true)
titleLabel:setFocusable(true)
titleLabel:setEllipsize(TruncateAt.END)

toplab = {}
toplab.width = MATCH_PARENT
toplab.height = WRAP_CONTENT
toplab.indexmod = 3
toplab.id = titleLabel:getId()
parent:addView(titleLabel)
InstallWindow(toplab)

mapwin = {}
mapwin.width = MATCH_PARENT
mapwin.height = WRAP_CONTENT
mapwin.indexmod = 4
mapwin.id = view:getId()
InstallWindow(mapwin)

botlab = {}
botlab.width = MATCH_PARENT
botlab.height = WRAP_CONTENT
botlab.id = exitsLabel:getId()
botlab.divider = true
botlab.indexmod = 5
parent:addView(exitsLabel)
InstallWindow(botlab)



			
function updateHeaders(data)
	local tmp = loadstring(data)()
	title = tmp.title
	
	exits = tmp.exits
	
	titleLabel:setText(title)
	exitsLabel:setText(exits)
end

function OnCreate()
	--find the scroller and ask it for some specific children, i actually think we can just ask the root. lets try

	--titleLabel = parent:findViewById(6021)
	--exitsLabel = parent:findViewById(6022)
	--view:clearText()
	--view:addText("\n\n\n\n\n\n\n\n\n\n",true)
end