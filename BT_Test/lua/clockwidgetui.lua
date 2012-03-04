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

timers = {}

function tickIncoming(data)
	timers = loadstring(data)()
	--debugPrint(
	
	--advance the current time
	timeordinal = tonumber(timeordinal) + 1
	if(timeordinal > 23) then
		timeordinal = 0
	end
	
	--update the sun hue.
	--if(timeordinal >= 6 and timeordinal <= 18) then
		--compute new sun hue.

	
	--end 
	debugPrint("Tick recieved, time ordinal now:"..timeordinal.." black:"..timers.black.." white:"..timers.white.." grey:"..timers.grey)
	--reset the timer.
	cancelCallback(100)
	subcount = 0
	scheduleCallback(100,"subCount",2490)
	view:invalidate()
end

function setTimeOrdinal(str)
	if(str == nil) then
		return
	end
	debugPrint("Setting time ordinal: "..str)
	timeordinal = tonumber(str)
	updateAlphas()
end

function OnDraw(c)
	--purple = Color:argb(255,200,40,200)
	--c:drawColor(purple)
	if(timeordinal == nil) then
		return
	end
	
	c:drawLine(0,50,400,50,bgSkyPaint)
	
	--for daylight, see if the timeordinal is between 8am and 8pm, calculate it as a value between 0-12
	updateAlphas()
	
	c:drawRect(0,0,400,100,fgSkyPaint)
	
	c:drawRect(0,0,400,100,fgNightPaint)
	--updateAlphas()
	if(dosun == true) then
		subcountPercent = subcount / 12
		tmp2 = timeordinal - 12 + subcountPercent
		tmp2 = math.abs(tmp2)
		tmp2 = tmp2 / 6
		tmp2 = tmp2 * 20
		tmp2 = 40 - tmp2
		updateSunHue(tmp2)
		sunPaint:setColor(Color:HSVToColor(sunHueFloats))
		--get time ordinal into 0-12, and interpolate 0-12 to -width/2 to width/2
		subcountPercent = subcount / 12
		tmp = timeordinal - 6
		tmp = tmp - 6
		tmp = (tmp / 12) + subcountPercent/12
		tmp = tmp * 200
		
		tmp = tmp + 300 --shift to left side
		--compute y param
		y = 0.0111*(tmp-300)*(tmp-300)+10
		c:drawCircle(tmp,y,20,sunPaint)
		--debugPrint("doing sun at:"..tmp..","..y)
	end
	
	--do the moons
	if(timers.black ~= nil and (timers.black >= 39 or timers.black == 1)) then
		if(timers.black == 1) then
			timers.black = 52
		end
		total = 52-38
		
		progress = total - (52-timers.black) + (subcount / 12) -0.5
		
		x = (progress/total)-0.5
		x = (x * travel.black) --remember to add back the axis to the final x
		y = aval.black * x * x + constant.black
		c:drawCircle(x+axis.black,y,radius.black,blackPaint)
	end
	
	if(timers.white ~= nil and (timers.white >= 50 or timers.white == 1)) then
		if(timers.white == 1) then
			timers.white = 67
		end
		progress = 67-timers.white + (1.0-(subcount / 12)) -0.5
		total = 67-50
		x = (progress/total)-0.5
		x = (x * travel.white) --remember to add back the axis to the final x
		y = aval.white * x * x + constant.white
		--debugPrint("doing white moon: x:"..(x+axis.white).." y:"..y.." radius:"..radius.white.." whitePaint:"..whitePaint:toString())
		
		c:drawCircle(x+axis.white,y,radius.white,whitePaint)
	end
	
	if(timers.grey ~= nil and (timers.grey >= 24 or timers.grey == 1)) then
		if(timers.grey == 1) then
			timers.grey = 32
		end
		total = 32-24
		progress = total - (32-timers.grey)+ (subcount / 12) -0.5
		
		x = (progress/total)-0.5
		x = (x * travel.grey) --remember to add back the axis to the final x
		y = aval.grey * x * x + constant.grey
		--debugPrint("doing grey moon: x:"..(x+axis.grey).." y:"..y.." radius:"..radius.grey.." greyPaint:"..greyPaint:toString())
		c:drawCircle(x+axis.grey,y,radius.grey,greyPaint)
	end
	
	str = ""
	if(timeordinal < 10) then
		str = "0"
	end
	subcountstr = subcount*5
	if(subcountstr < 10) then
		subcountstr = "0"..subcountstr
	end
	str = str..timeordinal..":"..subcountstr
	
	height = view:getHeight()
	width = view:getWidth()
	
	textwidth = clockpaint:measureText(str)
	
	x = width/4
	y = height/3
	
	x = x - textwidth/2
	if(x < 0) then
		x = 0
	end
	y = y + clocktextsize/2
	
	timerFrameRect:set(x-5,y-clocktextsize-5,x+textwidth+5,y+10)
	
	c:drawRoundRect(timerFrameRect,6,6,timerFrameBg)
	c:drawRoundRect(timerFrameRect,6,6,timerFramePaint)
	
	
	c:drawText(str,x,y,clockpaint)
	
end

clocktextsize = 45
clockpaint = luajava.newInstance("android.graphics.Paint")
clockpaint:setTextSize(clocktextsize)
clockfacecolor = Color:argb(255,0,0,255)
clockpaint:setColor(clockfacecolor)
clockpaint:setAntiAlias(true)


subcount = 0

function subCount()
	subcount = subcount + 1
	docallback = false
	if(subcount > 11) then
		subcount = 0
	elseif(subcount == 11) then
		debugPrint("TICK INCOMING")
		view:invalidate()
	else
		docallback = true
		--debugPrint("SUB TICKING")
		view:invalidate()
	end
	if(docallback) then
		scheduleCallback(100,"subCount",2490)
		view:invalidate()
	end
end

--start ticking.
scheduleCallback(100,"subCount",2490)


--build up the gradient objects we need.
skyGradientColors = {}
table.insert(skyGradientColors,Color:argb(255,35,94,191))
table.insert(skyGradientColors,Color:argb(255,83,118,191))

skyGradientPositions = {}
table.insert(skyGradientPositions,0.0)
table.insert(skyGradientPositions,1.0)

skyColors = makeIntArray(skyGradientColors)
skyPositions = makeFloatArray(skyGradientPositions)

blueGradientShader = luajava.new(LinearGradient,0,0,0,100,skyColors,nil,TileMode.REPEAT)

bgSkyPaint = luajava.new(Paint)
bgSkyPaint:setStyle(Style.STROKE)
bgSkyPaint:setStrokeWidth(100)
bgSkyPaint:setShader(blueGradientShader)

--make the red "darken layer" paint.
porterduff = luajava.new(PorterDuffXfermode,PorterDuffMode.DARKEN)
fgSkyPaint = luajava.new(Paint)
fgSkyPaint:setStyle(Style.FILL)
fgSkyPaint:setXfermode(porterduff)
fgSkyPaint:setColor(Color:argb(255,237,22,22))

porterduff = luajava.new(PorterDuffXfermode,PorterDuffMode.DARKEN)
fgNightPaint = luajava.new(Paint)
fgNightPaint:setStyle(Style.FILL)
fgNightPaint:setXfermode(porterduff)
fgNightPaint:setColor(Color:argb(255,9,24,54))

function updateAlphas()
	subcountPercent = subcount / 12
	if(timeordinal >= 6 and timeordinal <= 18) then
		local tmp = timeordinal - 6 + subcountPercent
		local alpha = 7.08*(tmp-6)*(tmp-6)
		if(alpha > 255) then
			alpha = 255
		elseif(alpha < 0) then
			alpha = 0
		end
		--debugPrint("Setting day alpha to:"..alpha)
		alpha = tonumber(alpha)
		fgSkyPaint:setAlpha(alpha)
		
		dosun = true
	else
		fgSkyPaint:setAlpha(255)
		dosun = false
	end
	
	if(timeordinal >= 18 or timeordinal <= 6) then
		local tmp = 0
		local alpha = 0
		if(timeordinal >= 18) then
			tmp = timeordinal-18 + subcountPercent
		elseif(timeordinal <= 6) then
			tmp = 6+timeordinal + subcountPercent
		end
		alpha = (-7.08*(tmp-6)*(tmp-6))+255
		if(alpha > 255) then
			alpha = 255
		elseif(alpha < 0) then
			alpha = 0
		end
		--debugPrint("Setting night alpha to:"..alpha)
		alpha = tonumber(alpha)
		fgNightPaint:setAlpha(alpha)
	else
		fgNightPaint:setAlpha(0)
	end
end

timerFramePaint = luajava.new(Paint)
timerFramePaint:setStyle(Style.STROKE)
timerFramePaint:setStrokeWidth(3)
timerFramePaint:setColor(Color:argb(255,15,15,127))
timerFramePaint:setAntiAlias(true)

timerFrameBg = luajava.new(Paint)
timerFrameBg:setColor(Color:argb(180,172,172,172))
timerFrameBg:setStyle(Style.FILL)

timerFrameRect = luajava.new(RectF)

sunHues = {}
table.insert(sunHues,20.0)
table.insert(sunHues,0.84)
table.insert(sunHues,0.96)

sunHueFloats = makeFloatArray(sunHues)

sunPaint = luajava.new(Paint)
sunPaint:setStyle(Style.FILL)
sunPaint:setAntiAlias(true)
sunPaint:setColor(Color:HSVToColor(sunHueFloats))

function updateSunHue(val)
	Array:setFloat(sunHueFloats,0,val)
end

dosun = false

blackPaint = luajava.new(Paint)
blackPaint:setStyle(Style.FILL)
blackPaint:setAntiAlias(true)
blackPaint:setColor(Color:argb(255,150,12,150))

whitePaint = luajava.new(Paint)
whitePaint:setStyle(Style.FILL)
whitePaint:setAntiAlias(true)
whitePaint:setColor(Color:argb(255,250,250,250))

greyPaint = luajava.new(Paint)
greyPaint:setStyle(Style.FILL)
greyPaint:setAntiAlias(true)
greyPaint:setColor(Color:argb(255,127,127,127))

travel={}
travel.sun = 400/2
travel.black = 400/6
travel.white = 400/2
travel.grey = 400/6

radius={}
radius.sun = 20
radius.black = 10
radius.grey = 14
radius.white = 8

constant = {}
constant.sun = radius.sun
constant.white = 100/2
constant.black = (100/2)-2*radius.black
constant.grey = (100/2)-radius.grey

axis = {}
axis.sun = 300
axis.grey = 300-40
axis.black = 300+10
axis.white = 300-10

tmpval = {}
tmpval.sun = axis.sun - (travel.sun/2) - axis.sun
tmpval.white = axis.white - (travel.white/2) - axis.white
tmpval.grey = axis.grey - (travel.grey/2) - axis.grey
tmpval.black = axis.black -(travel.black/2) - axis.black


aval = {}
aval.sun = (100-constant.sun)/(tmpval.sun*tmpval.sun)
aval.grey = (100-constant.grey)/(tmpval.grey*tmpval.grey)
aval.black = (100-constant.black)/(tmpval.black*tmpval.black)
aval.white = (100-constant.white)/(tmpval.white*tmpval.white)

