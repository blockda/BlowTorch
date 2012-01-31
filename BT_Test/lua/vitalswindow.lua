--debugPrint("package path:"..package.path)
package.path = "/mnt/sdcard/BlowTorch/?.lua"

--make a button.
debugPrint("in the chat window")

Array = luajava.bindClass("java.lang.reflect.Array")
Rect = luajava.bindClass("android.graphics.Rect")
Color = luajava.bindClass("android.graphics.Color")
LinearGradient = luajava.bindClass("android.graphics.LinearGradient")
TileMode = luajava.bindClass("android.graphics.Shader$TileMode")

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

gradientColors = {}
table.insert(gradientColors,Color.RED)
table.insert(gradientColors,Color.DKGRAY)
table.insert(gradientColors,Color.LTGRAY)
table.insert(gradientColors,Color.DKGRAY)
table.insert(gradientColors,Color.YELLOW)

gradientPositions = {}
table.insert(gradientPositions,0.0)
table.insert(gradientPositions,0.325)
table.insert(gradientPositions,0.5)
table.insert(gradientPositions,0.675)
table.insert(gradientPositions,1.0)

gColors = makeIntArray(gradientColors)
gPositions = makeFloatArray(gradientPositions)

zeroFloat = luajava.newInstance("java.lang.Float",0)
widthFloat = luajava.newInstance("java.lang.Float",view:getWidth())
alignPosFloat = luajava.newInstance("java.lang.Float",0)

gradientShader = luajava.new(LinearGradient,zeroFloat:floatValue(),zeroFloat:floatValue(),widthFloat:floatValue(),zeroFloat:floatValue(),gColors,gPositions,TileMode.REPEAT)

hprect = luajava.new(Rect)
manarect = luajava.new(Rect)
enemyrect = luajava.new(Rect)

Paint = luajava.bindClass("android.graphics.Paint")
Style = luajava.bindClass("android.graphics.Paint$Style")

hppaint = luajava.new(Paint)
manapaint = luajava.new(Paint)
enemypaint = luajava.new(Paint)
alignpaint = luajava.new(Paint)


hppaint:setStyle(Style.FILL)
manapaint:setStyle(Style.FILL)
enemypaint:setStyle(Style.FILL)
alignpaint:setStyle(Style.STROKE)
alignpaint:setShader(gradientShader)
alignpaint:setStrokeWidth(25)

hppaint:setARGB(255,0,255,0)
manapaint:setARGB(255,0,0,255)
enemypaint:setARGB(255,255,0,0)

width = 0
height = 0

vitals = {}
vitals.hp = 100
vitals.mp = 100

maxes = {}
maxes.hp = 100
maxes.mp = 100

enemypct = 0

align = 0

function OnDraw(canvas)
	debugPrint("vitals view onDraw")
	canvas:drawLine(zeroFloat:floatValue(),alignPosFloat:floatValue(),widthFloat:floatValue(),alignPosFloat:floatValue(),alignpaint)
	
	canvas:drawRect(hprect,hppaint)
	canvas:drawRect(manarect,manapaint)
	canvas:drawRect(enemyrect,enemypaint)
	
	--canvas
end

function OnSizeChanged(neww,newh,oldw,oldh)
	width = neww
	height = newh
	widthFloat = luajava.newInstance("java.lang.Float",width)
	heightOver8 = height/8
	alignPos = heightOver8*7
	alignPosFloat = luajava.newInstance("java.lang.Float",alignPos)
	gradientShader = luajava.new(LinearGradient,zeroFloat:floatValue(),zeroFloat:floatValue(),widthFloat:floatValue(),zeroFloat:floatValue(),gColors,gPositions,TileMode.REPEAT)
	alignpaint:setShader(gradientShader)
	updateBarRects()
end

function updateVitals(str)
	debugPrint("in updateVitals")
	data = loadstring(str)()
	
	vitals.hp = data.hp
	vitals.mp = data.mana
	
	--debugPrint("updateVitals:Report:"..vitals.hp..":"..vitals.mp..":"..enemypct)
	
	updateBarRects()
	view:invalidate()
end

function updateMaxes(str)
	debugPrint("in updateMaxes")
	data = loadstring(str)()
	
	maxes.hp = data.hp
	maxes.mp = data.mana
	--enemypct = data.enemypct
	--debugPrint("updateMaxes:Report:"..maxes.hp..":"..maxes.mp)
	
	updateBarRects()
	view:invalidate()
end

function updateAlign(str)
	align = str
	updateBarRects()
	view:invalidate()
end

function updateEnemyPercent(str)
	enemypct = str
	updateBarRects()
	view:invalidate()
end

function updateAlign(str)
	align = str
	updateBarRects()
	view:invalidate()
end

function updateBarRects()
	hppct = vitals.hp / maxes.hp
	manapct = vitals.mp / maxes.mp
	
	enemyval = 0
	if(enemypct ~= nil) then
		enemyval = enemypct / 100
	end

	heightOver4 = height / 4
	hprect:set(0,0,width*hppct,heightOver4)
	manarect:set(0,heightOver4,width*manapct,heightOver4*2)
	enemyrect:set(0,heightOver4*2,width*enemyval,heightOver4*3)	
end

updateBarRects()
--PluginXCallS("initReady","now")




