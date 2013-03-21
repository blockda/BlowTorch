
package.path = package.path..";"..GetPluginInstallDirectory().."/?.lua"
require("serialize")
require("miniwindow")

--make a button.
--debugPrint("in the clock widgetui")

local luajava = luajava
local view = view

local Paint = luajava.bindClass("android.graphics.Paint")
local Array = luajava.bindClass("java.lang.reflect.Array")
local Color = luajava.bindClass("android.graphics.Color")
local LinearGradient = luajava.bindClass("android.graphics.LinearGradient")
local PorterDuffXfermode = luajava.bindClass("android.graphics.PorterDuffXfermode")
local PorterDuffMode = luajava.bindClass("android.graphics.PorterDuff$Mode")
local TileMode = luajava.bindClass("android.graphics.Shader$TileMode")
local Paint = luajava.bindClass("android.graphics.Paint")
local Style = luajava.bindClass("android.graphics.Paint$Style")
local RectF = luajava.bindClass("android.graphics.RectF")
local Typeface = luajava.bindClass("android.graphics.Typeface")
local LinearLayout = luajava.bindClass("android.widget.LinearLayout")

local Integer = luajava.newInstance("java.lang.Integer",0)
local IntegerClass = Integer:getClass()
local RawInteger = IntegerClass.TYPE

local Float = luajava.newInstance("java.lang.Float",0)
local FloatClass = Float:getClass()
local RawFloat = FloatClass.TYPE

local function makeFloatArray(table)
	local newarray = Array:newInstance(RawFloat,#table)
	for i,v in ipairs(table) do
		local index = i-1
		local floatval = luajava.newInstance("java.lang.Float",v)
		Array:setFloat(newarray,index,floatval:floatValue())
	end
	
	return newarray
end

local function makeIntArray(table)
	local newarray = Array:newInstance(RawInteger,#table)
	for i,v in ipairs(table) do
		local index = i-1
		local intval = luajava.newInstance("java.lang.Integer",v)
		Array:setInt(newarray,index,intval:intValue())
	end
	
	return newarray
end


--install ourselves in the scroll view
--root = view:getParent()
--root:removeView(view)

--scroller = root:findViewById(6010)
--holder = scroller:getChildAt(0)

--holder:addView(view)
view:fitFontSize(36)
function OnCreate()
	
	view:addText("\n\n\n",true)
end

config = {}
config.id = view:getId()
config.divider = true
--config.indexmod = 2
config.width = MATCH_PARENT
config.height = WRAP_CONTENT
InstallWindow(config)

--divider_config = {}
--divider_config.id = config.id + 1
--divider_config.width = MATCH_PARENT
--divider_config.height = 3*density
--divider_config.type = "linear"



--local divider = MakeDivider(context)
--divider:setId(divider_config.id)
--view:getParent():addView(divider)
--InstallWindow(divider_config)

view:setCenterJustify(true)

Color = luajava.bindClass("android.graphics.Color")

timeordinal = 0

stats = nil
local MeasureSpec = luajava.bindClass("android.view.View$MeasureSpec")
local AT_MOST = MeasureSpec.AT_MOST
local EXACTLY = MeasureSpec.EXACTLY
local UNSPECIFIED = MeasureSpec.UNSPECIFIED
local MeasureSpec = luajava.bindClass("android.view.View$MeasureSpec")
local measurespec_height = -1
local measurespec_width = -1
local measured_height = -1
local measured_width = -1
local density = view:getResources():getDisplayMetrics().density
function OnMeasure(wspec,hspec)	
	if(wspec == measurespec_width and hspec == measurespec_height) then return measured_width,measured_height end
	--Note(string.format("measurespecs: %d, %d\n",wspec,hspec))
	measurespec_width = wspec
	measurespec_height = hspec
	measured_width = MeasureSpec:getSize(wspec)
	--local wmode = MeasureSpec:getMode(wspec)
	
	measured_height = MeasureSpec:getSize(hspec)
	--local hmode = MeasureSpec:getMode(hspec)
	
	function test()
		local orientation = view:getParent():getOrientation()
	end
	local ret,err = pcall(test,debug.traceback)
	if(not ret) then
		--there was a problem, but do we care
		--Note("stat widget is relative, width:"..measured_width.." height:"..measured_height.."\n")
		return measured_width,measured_height
	end
	
	
	local orientation = view:getParent():getOrientation()
	if(orientation == LinearLayout.VERTICAL) then
		view:fitFontSize(36)
		view:doFitFontSize(measured_width)
		measured_height = view:getLineSize()*view:getBuffer():getBrokenLineCount()
		
		--Note("stat widget is vertical, width:"..measured_width.." height:"..measured_height.."\n")
		return measured_width,measured_height
	else
		view:setCharacterSizes((measured_height-6)/3,2)
		measured_width = 37*view:measure("a")
		view:fitFontSize(-1)
		measured_height = view:getLineSize()*view:getBuffer():getBrokenLineCount()
		--Note("stat widget is horizontal, width:"..measured_width.." height:"..measured_height.."\n")
		return measured_width,measured_height
	end
	--end
	
	

end

function OnSizeChanged(w,h,ow,oh)
	--Note(string.format("\nstat window ui changed, %s, %s, %s, %s\n",w,h,ow,oh))
	--check out this trick, fit to 36 chars
	--local p = luajava.new(Paint)
	--local extra = view:getLineSpaceExtra()
	--p.setTextSize()
	view:requestLayout()
	--view:invalidate()
end

System = luajava.bindClass("java.lang.System")

local compact_format = 
[[%sstr: %s[%4d]  %sdex: %s[%4d]  %scon: %s[%4d]%s
%sint: %s[%4d]  %swis: %s[%4d] %sluck: %s[%4d]%s
%s hr: %s[%4d]  %s dr: %s[%4d] %ssave: %s[%4d]%s]]

function updateStats(data)
	--Note("\nstat data:"..data.."\n")

	stats = loadstring(data)()
	
	local dcyan = "\27[0;36m"
	local bwhit = "\27[37;1m"
	local nwhit = "\27[0;37m"
	
	--local start_time = System:currentTimeMillis()
	if(not stats.str) then return end
	
	view:clearText()
	view:addText(string.format(compact_format,dcyan,bwhit,stats.str,dcyan,bwhit,stats.dex,dcyan,bwhit,stats.con,nwhit,dcyan,bwhit,stats.int,dcyan,bwhit,stats.wis,dcyan,bwhit,stats.luck,nwhit,dcyan,bwhit,stats.hr,dcyan,bwhit,stats.dr,dcyan,bwhit,stats.saves,nwhit),true)
	--view:addText(string.format("%sstr: %s[%4d]  %sdex: %s[%4d]  %scon: %s[%4d]%s\n",dcyan,bwhit,stats.str,dcyan,bwhit,stats.dex,dcyan,bwhit,stats.con,nwhit),true)
	--view:addText(string.format("%sint: %s[%4d]  %swis: %s[%4d] %sluck: %s[%4d]%s\n",dcyan,bwhit,stats.int,dcyan,bwhit,stats.wis,dcyan,bwhit,stats.luck,nwhit),true)
	--view:addText(string.format("%s hr: %s[%4d]  %s dr: %s[%4d] %ssave: %s[%4d]%s\n",dcyan,bwhit,stats.hr,dcyan,bwhit,stats.dr,dcyan,bwhit,stats.saves,nwhit),true)
	--view:addText("i can just keep adding lines to this\n",true)
	--view:addText("yup, seems to be the case\n",true)
	--view:addText("formatting long lines will blow\n",true)
	--view:addText("but since this is all our control\n",true)
	--view:addText("it won't matter\n",true)
	--view:addText("MORE TEXT!!!!!!!!!",true)
	view:invalidate();
	
	--local end_time = System:currentTimeMillis()
	--local duration = end_time - start_time
	--Note(string.format("\nupdating stat window took: %d millis\n",duration))
end

--function OnDraw(c)
	
--	if(stats == nil) then PluginXCallS("loadStats","") return end
	
--	one = "str["..stats.str.."] int["..stats.int.."]  wis["..stats.wis.."]"
--	two = "dex["..stats.dex.."] con["..stats.con.."] luck["..stats.luck.."]"
	--debugPrint("statwindow drawing")
--	widthone = statPaint:measureText(one)
--	startone = view:getWidth()/2 - widthone/2
--	widthtwo = statPaint:measureText(two)
--	starttwo = view:getWidth()/2 - widthtwo/2
	
	--c:drawColor(Color:argb(255,0,0,50))
	
	--c:drawText(one,startone,40,statPaint)
	--c:drawText(two,starttwo,70,statPaint)
	--draw the labels.
--end

labelPaint = luajava.new(Paint)
labelPaint:setTextSize(25)
labelPaint:setColor(Color:argb(255,12,12,200))
labelPaint:setAntiAlias(true)

statPaint = luajava.new(Paint)
statPaint:setTextSize(25)
statPaint:setColor(Color:argb(255,200,200,200))
statPaint:setAntiAlias(true)
statPaint:setTypeface(Typeface.MONOSPACE)

PluginXCallS("loadStats","")
--debugPrint("statwindow ui startup complete")