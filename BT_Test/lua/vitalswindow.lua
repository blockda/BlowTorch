--debugPrint("package path:"..package.path)
package.path = "/mnt/sdcard/BlowTorch/?.lua"

--make a button.
debugPrint("in the chat window")

Array = luajava.bindClass("java.lang.reflect.Array")
Rect = luajava.bindClass("android.graphics.Rect")
RectF = luajava.bindClass("android.graphics.RectF")
Color = luajava.bindClass("android.graphics.Color")
LinearGradient = luajava.bindClass("android.graphics.LinearGradient")
TileMode = luajava.bindClass("android.graphics.Shader$TileMode")
Bitmap = luajava.bindClass("android.graphics.Bitmap")
BitmapConfig = luajava.bindClass("android.graphics.Bitmap$Config")

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

alignShader = luajava.new(LinearGradient,zeroFloat:floatValue(),zeroFloat:floatValue(),widthFloat:floatValue(),zeroFloat:floatValue(),gColors,gPositions,TileMode.REPEAT)

--make the hp/mp/tnl "top" gradient
vertGradientColors = {}
table.insert(vertGradientColors,Color:argb(0,180,180,180))
table.insert(vertGradientColors,Color:argb(200,30,30,30))
table.insert(vertGradientColors,Color:argb(0,180,180,180))

vgColors = makeIntArray(vertGradientColors)
--no positions, even spacing is fine.
vertShader = luajava.new(LinearGradient,0,0,0,0,vgColors,nil,TileMode.REPEAT)

hprect = luajava.new(RectF)
manarect = luajava.new(RectF)
enemyrect = luajava.new(RectF)
tnlrect = luajava.new(RectF)

Paint = luajava.bindClass("android.graphics.Paint")
Style = luajava.bindClass("android.graphics.Paint$Style")

hppaint = luajava.new(Paint)
manapaint = luajava.new(Paint)
enemypaint = luajava.new(Paint)
tnlpaint = luajava.new(Paint)
alignpaint = luajava.new(Paint)
topperBorderPaint = luajava.new(Paint)
topperDividerPaint = luajava.new(Paint)
topperGradientPaint = luajava.new(Paint)



hppaint:setStyle(Style.FILL)
hppaint:setAntiAlias(true)
manapaint:setStyle(Style.FILL)
manapaint:setAntiAlias(true)
enemypaint:setStyle(Style.FILL)
enemypaint:setAntiAlias(true)
tnlpaint:setStyle(Style.FILL)
tnlpaint:setAntiAlias(true)
alignpaint:setStyle(Style.STROKE)
alignpaint:setAntiAlias(true)
alignpaint:setShader(gradientShader)
alignpaint:setStrokeWidth(25)

topperBorderPaint:setStyle(Style.STROKE)
topperDividerPaint:setStyle(Style.STROKE)
topperGradientPaint:setStyle(Style.STROKE)

topperBorderPaint:setStrokeWidth(6)
topperDividerPaint:setStrokeWidth(3)
topperGradientPaint:setStrokeWidth(25)
topperGradientPaint:setShader(vertShader)

topperBorderPaint:setARGB(255,200,200,200)
topperDividerPaint:setARGB(255,10,10,10)

hppaint:setARGB(255,0,255,0)
manapaint:setARGB(255,0,0,255)
enemypaint:setARGB(255,255,0,0)
tnlpaint:setARGB(255,230,169,35)

width = 0
height = 0

vitals = {}
vitals.hp = 100
vitals.mp = 100

maxes = {}
maxes.hp = 100
maxes.mp = 100

tnl = 0
tolevel = 0

enemypct = 0

align = 0

topper = nil
topperCanvas = nil

function OnDraw(canvas)
	if(topper == nil) then
		return
	end
	debugPrint("vitals view onDraw")
	canvas:drawLine(zeroFloat:floatValue(),alignPosFloat:floatValue(),widthFloat:floatValue(),alignPosFloat:floatValue(),alignpaint)
	
	r = luajava.newInstance("java.lang.Float",7)
	rf = r:floatValue()
	canvas:drawRoundRect(hprect,rf,rf,hppaint)
	canvas:drawRoundRect(manarect,rf,rf,manapaint)
	canvas:drawRoundRect(enemyrect,rf,rf,enemypaint)
	canvas:drawRoundRect(tnlrect,rf,rf,tnlpaint)
	
	canvas:drawBitmap(topper,0,0,nil)
	--canvas
end

function OnSizeChanged(neww,newh,oldw,oldh)
	if(neww == oldw and newh == oldh) then
		debugPrint("resize called, but the size is the same, returning")
		return
	end
	width = neww
	height = newh
	widthFloat = luajava.newInstance("java.lang.Float",width)
	heightOver10 = height/10
	alignPos = heightOver10*9
	
	
	alignPosFloat = luajava.newInstance("java.lang.Float",alignPos)
	alignShader = luajava.new(LinearGradient,zeroFloat:floatValue(),zeroFloat:floatValue(),widthFloat:floatValue(),zeroFloat:floatValue(),gColors,gPositions,TileMode.REPEAT)
	alignpaint:setShader(alignShader)
	alignpaint:setStrokeWidth(height/5)
	
	heightOver5 = height/5
	
	--rebuild vertical gradient
	vertGradientTop = luajava.newInstance("java.lang.Float",heightOver10)
	vertGradientBottom = luajava.newInstance("java.lang.Float",-1*heightOver10)
	vertShader = luajava.new(LinearGradient,zeroFloat:floatValue(),vertGradientBottom:floatValue(),zeroFloat:floatValue(),vertGradientTop:floatValue(),vgColors,nil,TileMode.REPEAT)
	topperGradientPaint:setShader(vertShader)
	
	--update toppers
	if(topper ~= nil) then 
		topper:recycle()
		topper = nil
		topperCanvas = nil
	end
	--if(topper2 ~= nil) then
	--	topper2:recycle()
	--	topper2 = nil
	--end	
	
	topper = Bitmap:createBitmap(tonumber(width),tonumber(height),BitmapConfig.ARGB_8888)
	--topper2 = Bitmap:createBitmap(width,heightOver4);
	topperCanvas = luajava.newInstance("android.graphics.Canvas",topper)
	--topperCanvas2 = luajava.newInstance("android.graphics.Canvas",topper2)
	
	updateToppers()
	
	updateBarRects()
end

function updateToppers()
	rect = luajava.new(Rect)
	rect:set(0,0,tonumber(width),tonumber(height/5))
	--topperCanvas:drawARGB(255,0,0,255)
	topperCanvas:save()
	topperCanvas:clipRect(rect)
	topperCanvas:drawLine(0,tonumber(height/10),tonumber(width),tonumber(height/10),topperGradientPaint)
	--topperCanvas:drawRect(rect,topperBorderPaint)
	topperCanvas:restore()
	
	rect:offsetTo(0,tonumber(height/5))
	topperCanvas:save()
	topperCanvas:clipRect(rect)
	topperCanvas:drawLine(0,tonumber((height/10)*3),tonumber(width),tonumber((height/10)*3),topperGradientPaint)
	--topperCanvas:drawRect(rect,topperBorderPaint)
	topperCanvas:restore()
	
	rect:offsetTo(0,tonumber((height/5)*2))
	topperCanvas:save()
	topperCanvas:clipRect(rect)
	topperCanvas:drawLine(0,tonumber((height/10)*5),tonumber(width),tonumber((height/10)*5),topperGradientPaint)
	--topperCanvas:drawRect(rect,topperBorderPaint)
	topperCanvas:restore()
	
	rect:offsetTo(0,tonumber((height/5)*3))
	topperCanvas:save()
	topperCanvas:clipRect(rect)
	topperCanvas:drawLine(0,tonumber((height/10)*7),tonumber(width),tonumber((height/10)*7),topperGradientPaint)
	--topperCanvas:drawRect(rect,topperBorderPaint)
	topperCanvas:restore()
	
	rect:offsetTo(0,tonumber((height/5)*4))
	topperCanvas:save()
	topperCanvas:clipRect(rect)
	topperCanvas:drawLine(0,tonumber((height/10)*9),tonumber(width),tonumber((height/10)*9),topperGradientPaint)
	--topperCanvas:drawRect(rect,topperBorderPaint)
	topperCanvas:restore()
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
	
	tnlpercent = 0
	if(tolevel > 0) then
		tnlpercent = tnl/tolevel
		debugPrint("updateBarRects, tnlpercent is:"..tnlpercent)
	end

	heightOver5 = height / 5
	hprect:set(0,0,width*hppct,heightOver5)
	manarect:set(0,heightOver5,width*manapct,heightOver5*2)
	enemyrect:set(0,heightOver5*2,width*enemyval,heightOver5*3)	
	tnlrect:set(0,heightOver5*3,width*tnlpercent,heightOver5*4)
	
end

function updateMaxPerLevel(str)
	debugPrint("updating maxperlevel")
	tolevel = tonumber(str)
	updateBarRects()
	view:invalidate()
end

function updateTNL(str)
	debugPrint("updating tnl")
	tnl = tonumber(str)
	updateBarRects()
	view:invalidate()
end

function updateAll(data)
	debugPrint("updating all")
	info = loadstring(data)()
	
	vitals.hp = info.hp
	vitals.mp = info.mp
	maxes.hp = info.maxhp
	maxes.mp = info.maxmp
	enemypct = info.enemypct
	tnl = info.tnl
	tolevel = tonumber(info.tolevel)
	
	updateBarRects()
	view:invalidate()
end

updateBarRects()
PluginXCallS("initReady","now")




