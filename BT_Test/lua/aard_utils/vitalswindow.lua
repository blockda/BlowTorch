package.path = package.path..";"..GetPluginInstallDirectory().."/?.lua"
--make a button.
--debugPrint("in the chat window")
require("miniwindow")

Array = luajava.bindClass("java.lang.reflect.Array")
Rect = luajava.bindClass("android.graphics.Rect")
RectF = luajava.bindClass("android.graphics.RectF")
Color = luajava.bindClass("android.graphics.Color")
LinearGradient = luajava.bindClass("android.graphics.LinearGradient")
TileMode = luajava.bindClass("android.graphics.Shader$TileMode")
Bitmap = luajava.bindClass("android.graphics.Bitmap")
BitmapConfig = luajava.bindClass("android.graphics.Bitmap$Config")
RelativeLayout = luajava.bindClass("android.widget.RelativeLayout")

Integer = luajava.newInstance("java.lang.Integer",0)
IntegerClass = Integer:getClass()
RawInteger = IntegerClass.TYPE

Float = luajava.newInstance("java.lang.Float",0)
FloatClass = Float:getClass()
RawFloat = FloatClass.TYPE

density = view:getContext():getResources():getDisplayMetrics().density

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
alignPercent = 0.5
alignIndicatorX = 0
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
alignIndicatorPaint = luajava.new(Paint)


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

alignIndicatorPaint:setStrokeWidth(4)
alignIndicatorPaint:setStyle(Style.STROKE)
alignIndicatorPaint:setARGB(200,127,127,127)

topperBorderPaint:setStyle(Style.STROKE)
topperDividerPaint:setStyle(Style.STROKE)
topperGradientPaint:setStyle(Style.STROKE)

topperBorderPaint:setStrokeWidth(6)
topperDividerPaint:setStrokeWidth(3)
topperGradientPaint:setStrokeWidth(30*density)
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
	--debugPrint("vitals view onDraw")
	
	r = luajava.newInstance("java.lang.Float",3)
	rf = r:floatValue()
	canvas:drawRoundRect(hprect,rf,rf,hppaint)
	canvas:drawRoundRect(manarect,rf,rf,manapaint)
	canvas:drawRoundRect(enemyrect,rf,rf,enemypaint)
	canvas:drawRoundRect(tnlrect,rf,rf,tnlpaint)
	
	canvas:drawBitmap(topper,0,0,nil)
	
	if(vertical) then
		canvas:drawCircle(alignPosFloat:floatValue(),alignIndicatorX,height/20,alignIndicatorPaint)
	else
		canvas:drawCircle(alignIndicatorX,alignPosFloat:floatValue(),height/20,alignIndicatorPaint)
	end
	--canvas
end

vertical = false
function OnSizeChanged(neww,newh,oldw,oldh)
	if(neww == oldw and newh == oldh) then
		--debugPrint("resize called, but the size is the same, returning")
		return
	end
	width = tonumber(neww)
	height = tonumber(newh)
	if(height > width) then
		vertical = true
	else
		vertical = false
	end
	--debugPrint("changed to height/width:"..width.."x"..height)
	
	--if(vertical) then
		
	--else
		widthFloat = luajava.newInstance("java.lang.Float",width)
		heightOver10 = height/10
		widthOver10 = width/10
		if(vertical) then
			alignPos = widthOver10*9
		else
			alignPos = heightOver10*9
		end
	--end
	
	if(vertical) then
		alignIndicatorX = height * alignPercent
	else
		alignIndicatorX = width * alignPercent
	end
	
	
	alignPosFloat = luajava.newInstance("java.lang.Float",alignPos)
	if(vertical) then
		alignShader = luajava.new(LinearGradient,0,0,0,tonumber(height),gColors,gPositions,TileMode.REPEAT)
		alignpaint:setShader(alignShader)
		alignpaint:setStrokeWidth(width/5)
	else
		alignShader = luajava.new(LinearGradient,0,0,tonumber(width),0,gColors,gPositions,TileMode.REPEAT)
		alignpaint:setShader(alignShader)
		alignpaint:setStrokeWidth(height/5)
	end
	
	heightOver5 = height/5
	widthOver5 = width/5
	
	--rebuild vertical gradient
	if(vertical) then
		vertGradientTop = luajava.newInstance("java.lang.Float",widthOver10)
		vertGradientBottom = luajava.newInstance("java.lang.Float",-1*widthOver10)
		vertShader = luajava.new(LinearGradient,vertGradientBottom:floatValue(),0,vertGradientTop:floatValue(),0,vgColors,nil,TileMode.REPEAT)
		topperGradientPaint:setShader(vertShader)
	else
		vertGradientTop = luajava.newInstance("java.lang.Float",heightOver10)
		vertGradientBottom = luajava.newInstance("java.lang.Float",-1*heightOver10)
		vertShader = luajava.new(LinearGradient,zeroFloat:floatValue(),vertGradientBottom:floatValue(),zeroFloat:floatValue(),vertGradientTop:floatValue(),vgColors,nil,TileMode.REPEAT)
		topperGradientPaint:setShader(vertShader)
	end
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
	PluginXCallS("initReady","now")
end

function updateToppers()
	rect = luajava.new(Rect)
	if(vertical) then
		rect:set(0,0,widthOver5,height)
	else
		rect:set(0,0,width,heightOver5)
	end
	Note(string.format("\nIn update toppers: %f\n",heightOver5))
	--topperCanvas:drawARGB(255,0,0,255)
	topperCanvas:save()
	topperCanvas:clipRect(rect)
	if(vertical) then
		topperCanvas:drawLine(widthOver10,0,widthOver10,height,topperGradientPaint)
	else
		topperCanvas:drawLine(0,heightOver10,width,heightOver10,topperGradientPaint)
	end
	--topperCanvas:drawRect(rect,topperBorderPaint)
	topperCanvas:restore()
	

	if(vertical) then
		rect:offsetTo(widthOver5,0)
		topperCanvas:save()
		topperCanvas:clipRect(rect)
		topperCanvas:drawLine(widthOver10*3,0,widthOver10*3,height,topperGradientPaint)
	
	else
		rect:offsetTo(0,heightOver5)
		topperCanvas:save()
		topperCanvas:clipRect(rect)
		topperCanvas:drawLine(0,heightOver10*3,width,heightOver10*3,topperGradientPaint)
	
	end
	topperCanvas:restore()
	
	--topperCanvas:save()
	--topperCanvas:clipRect(rect)
	if(vertical) then
		rect:offsetTo(widthOver5*2,0)
		topperCanvas:save()
		topperCanvas:clipRect(rect)
		topperCanvas:drawLine(widthOver10*5,0,widthOver10*5,height,topperGradientPaint)
	else
		rect:offsetTo(0,heightOver5*2)
		topperCanvas:save()
		topperCanvas:clipRect(rect)
		topperCanvas:drawLine(0,heightOver10*5,width,heightOver10*5,topperGradientPaint)
	end
	--topperCanvas:drawRect(rect,topperBorderPaint)
	topperCanvas:restore()
	
	--topperCanvas:save()
	--topperCanvas:clipRect(rect)
	if(vertical) then
		rect:offsetTo(widthOver5*3,0)
		topperCanvas:save()
		topperCanvas:clipRect(rect)
		topperCanvas:drawLine(widthOver10*7,0,widthOver10*7,height,topperGradientPaint)
	else
		rect:offsetTo(0,heightOver5*3)
		topperCanvas:save()
		topperCanvas:clipRect(rect)
		topperCanvas:drawLine(0,heightOver10*7,width,heightOver10*7,topperGradientPaint)
	end
	--topperCanvas:drawRect(rect,topperBorderPaint)
	topperCanvas:restore()
	
	--topperCanvas:save()
	--topperCanvas:clipRect(rect)
	if(vertical) then
		rect:offsetTo(widthOver5*4,0)	
		topperCanvas:save()
		topperCanvas:clipRect(rect)
		topperCanvas:drawLine(alignPosFloat:floatValue(),0,alignPosFloat:floatValue(),tonumber(height),alignpaint)
		topperCanvas:drawLine(widthOver10*9,0,widthOver10*9,height,topperGradientPaint)
	
	else
		rect:offsetTo(0,heightOver5*4)	
		topperCanvas:save()
		topperCanvas:clipRect(rect)
		topperCanvas:drawLine(zeroFloat:floatValue(),alignPosFloat:floatValue(),widthFloat:floatValue(),alignPosFloat:floatValue(),alignpaint)
		topperCanvas:drawLine(0,heightOver10*9,width,heightOver10*9,topperGradientPaint)
	end
	--topperCanvas:drawRect(rect,topperBorderPaint)
	topperCanvas:restore()
end

function updateVitals(str)
	--debugPrint("in updateVitals")
	data = loadstring(str)()
	
	vitals.hp = data.hp
	vitals.mp = data.mana
	
	--debugPrint("updateVitals:Report:"..vitals.hp..":"..vitals.mp..":"..enemypct)
	
	updateBarRects()
	view:invalidate()
end

function updateMaxes(str)
	--debugPrint("in updateMaxes")
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
	--calculate align percent
	tmp = align + 2500
	alignPercent = tmp / 5000
	if(vertical) then
		alignIndicatorX = ((height-(2*5))*alignPercent)+5
	else
		alignIndicatorX = ((width-(2*5))*alignPercent)+5
	end
	--alignIndicatorX = width*alignPercent
	--debugPrint("alignIndicatorX now at: "..alignIndicatorX)
	--updateBarRects()
	view:invalidate()
end

function updateEnemyPercent(str)
	enemypct = str
	updateBarRects()
	view:invalidate()
end

function updateBarRects()

--width = tonumber(width)
		--height = tonumber(height)
		if(height == nil or width == nil) then return end
		if(heightOver5 == nil or widthOver5 == nil) then return end
	hppct = vitals.hp / maxes.hp
	manapct = vitals.mp / maxes.mp
	
	enemyval = 0
	if(enemypct ~= nil) then
		enemyval = enemypct / 100
	end
	
	tnlpercent = 0
	if(tolevel > 0) then
		tnlpercent = tnl/tolevel
		--debugPrint("updateBarRects, tnlpercent is:"..tnlpercent)
	end

	if(vertical) then
		--debugPrint("doing vertical bars")
		
		--widthOver5 = math.floor(width / 5)
		
		hprect:set(0,height*(1-hppct),widthOver5,height)
		manarect:set(widthOver5,height*(1-manapct),widthOver5*2,height)
		enemyrect:set(widthOver5*2,height*(1-enemyval),widthOver5*3,height)	
		tnlrect:set(widthOver5*3,height*(1-tnlpercent),widthOver5*4,height)
	else
		--heightOver5 = math.floor(height / 5)
		hprect:set(0,0,width*hppct,heightOver5)
		--Note(string.format("\nIn update rects: %f\n",heightOver5))
		manarect:set(0,heightOver5,width*manapct,heightOver5*2)
		enemyrect:set(0,heightOver5*2,width*enemyval,heightOver5*3)	
		tnlrect:set(0,heightOver5*3,width*tnlpercent,heightOver5*4)
	end

	
	--debugPrint("hprect:"..hprect:toString())
	--debugPrint("manarect:"..manarect:toString())
	--debugPrint("enemyrect:"..enemyrect:toString())
	--debugPrint("tnlrect:"..tnlrect:toString())
	--debugPrint("hprect:"..hprect:toString())	
end

function updateMaxPerLevel(str)
	--debugPrint("updating maxperlevel")
	tolevel = tonumber(str)
	updateBarRects()
	view:invalidate()
end

function updateTNL(str)
	--debugPrint("updating tnl")
	tnl = tonumber(str)
	updateBarRects()
	view:invalidate()
end

function updateAll(data)
	--debugPrint("updating all")
	--debugPrint("updateAll called:"..data)
	info = loadstring(data)()
	
	vitals.hp = info.hp
	vitals.mp = info.mp
	maxes.hp = info.maxhp
	maxes.mp = info.maxmp
	enemypct = info.enemypct
	tnl = info.tnl
	tolevel = tonumber(info.tolevel)
	align = info.align
	updateAlign(align)
	
	updateBarRects()
	view:invalidate()
end

R = luajava.bindClass("com.offsetnull.bt.R$id")
--modify the text input bar layout to make the bar appear above the vitals window
rootView = view:getParentView()
--inputbar = rootView:findViewById(10)
--inputbarParams = inputbar:getLayoutParams()
--inputbarParams:addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,0) --set this rule to false
--inputbarParams:addRule(RelativeLayout.ABOVE,view:getId())

--scroller = rootView:findViewById(6010)
--rootView:removeView(view)

--holder = scroller:getChildAt(0)
--holder:addView(view)
--view:setHasText(false)

MeasureSpec = luajava.bindClass("android.view.View$MeasureSpec")
local density = view:getResources():getDisplayMetrics().density
function OnMeasure(wspec,hspec)
	--we are going to assume some things here, 1, that MeasureSpec:getMode(wspec) == MeasureSpec.EXACTLY, 2 MeasureSpec:getMode(hspec) == MeasureSpec.UNDEFINED
	--we are just going to pull out the width value, and return a custom height
	local width = MeasureSpec:getSize(wspec)
	
	local barheight = math.floor(10*density)
	local height = 5*barheight
	
	--local mod = height % 1
	--height = height - mod
	--local mod = height % 10
	--if(mod ~= 0) then
	--	height = height - mod
	--end
	--Note(string.format("\nOnMeasure:%d,%d",width,height))
	return width,height
end

config = {}
config.id = view:getId()
config.divider = true
config.width = MATCH_PARENT
config.height = WRAP_CONTENT
InstallWindow(config)

--inputbar:requestLayout()


updateBarRects()
PluginXCallS("initReady","now")

function OnDestroy()
	--debugPrint("destroying vitals window")
	--topper:recycle()
	topperCanvas = nil
	topper = nil
	
end



