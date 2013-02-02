
package.path = package.path..";"..GetPluginInstallDirectory().."/?.lua"
require("serialize")
require("miniwindow")

--make a button.
--debugPrint("in the clock widgetui")

Paint = luajava.bindClass("android.graphics.Paint")
Array = luajava.bindClass("java.lang.reflect.Array")
Color = luajava.bindClass("android.graphics.Color")
LinearGradient = luajava.bindClass("android.graphics.LinearGradient")
PorterDuffXfermode = luajava.bindClass("android.graphics.PorterDuffXfermode")
PorterDuffMode = luajava.bindClass("android.graphics.PorterDuff$Mode")
TileMode = luajava.bindClass("android.graphics.Shader$TileMode")
Paint = luajava.bindClass("android.graphics.Paint")
Style = luajava.bindClass("android.graphics.Paint$Style")
RectF = luajava.bindClass("android.graphics.RectF")
Typeface = luajava.bindClass("android.graphics.Typeface")

Integer = luajava.newInstance("java.lang.Integer",0)
IntegerClass = Integer:getClass()
RawInteger = IntegerClass.TYPE

Float = luajava.newInstance("java.lang.Float",0)
FloatClass = Float:getClass()
RawFloat = FloatClass.TYPE

function makeFloatArray(table)
	newarray = Array:newInstance(RawFloat,#table)
	for i,v in ipairs(table) do
		index = i-1
		floatval = luajava.newInstance("java.lang.Float",v)
		Array:setFloat(newarray,index,floatval:floatValue())
	end
	
	return newarray
end

function makeIntArray(table)
	newarray = Array:newInstance(RawInteger,#table)
	for i,v in ipairs(table) do
		index = i-1
		intval = luajava.newInstance("java.lang.Integer",v)
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

config = {}
config.id = view:getId()
config.width = MATCH_PARENT
config.height = 70*density
InstallWindow(config)

divider_config = {}
divider_config.id = config.id + 1
divider_config.width = MATCH_PARENT
divider_config.height = 3*density
divider_config.type = "linear"

local divider = MakeDivider(context)
divider:setId(divider_config.id)
view:getParent():addView(divider)
InstallWindow(divider_config)

Color = luajava.bindClass("android.graphics.Color")

timeordinal = 0

stats = nil




function OnSizeChanged(w,h,ow,oh)
	Note(string.format("\nstat window ui changed, %s, %s, %s, %s\n",w,h,ow,oh))
	view:invalidate()
end

function updateStats(data)
	
	stats = loadstring(data)()
	
	local dcyan = "\27[36m"
	local bwhit = "\27[37;1m"
	local nwhit = "\27[37m"
	
	view:addText(string.format("%sstr: %s[%4d]%s\n",dcyan,bwhit,stats.str,nwhit),true)
	view:addText(string.format("%swis: %s[%4d]%s",dcyan,bwhit,stats.wis,nwhit),true)
	view:invalidate();
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