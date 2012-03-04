--debugPrint("package path:"..package.path)
package.path = "/mnt/sdcard/BlowTorch/?.lua"

require("serialize")
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
root = view:getParent()
root:removeView(view)

scroller = root:findViewById(6010)
holder = scroller:getChildAt(0)

holder:addView(view)

Color = luajava.bindClass("android.graphics.Color")

timeordinal = 0

stats = nil

function updateStats(data)
	stats = loadstring(data)()
end

function OnDraw(c)
	if(stats == nil) then return end
	
	one = " str:["..stats.str.."] int:["..stats.int.."] wis"..stats.wis.."]"
	two = " dex:["..stats.dex.."] con:["..stats.con.."] luck"..stats.luck.."]"
	
	c:drawText("stat window",30,30,labelPaint)
	--draw the labels.
end

labelPaint = luajava.new(Paint)
labelPaint:setTextSize(15)
labelPaint:setColor(Color:arbg(255,12,12,200))
labelPaint:setAntiAlias(true)

statPaint = luajava.new(Paint)
statPaint:setTextSize(25)
statPaint:setColor(Color:arbg(255,56,190,56))
statPaint:setAntiAlias(true)