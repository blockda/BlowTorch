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


--install ourselves in the scroll view
--root = view:getParent()
--root:removeView(view)

--scroller = root:findViewById(6010)
--holder = scroller:getChildAt(0)

--holder:addView(view)

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
	--debugPrint("Tick recieved, time ordinal now:"..timeordinal.." black:"..timers.black.." white:"..timers.white.." grey:"..timers.grey)
	--reset the timer.
	CancelCallback(100)
	subcount = 0
	ScheduleCallback(100,"subCount",normalSubDelay)
	view:invalidate()
end

function setTimeOrdinal(str)
	if(str == nil) then
		return
	end
	--debugPrint("Setting time ordinal: "..str)
	timeordinal = tonumber(str)
	updateAlphas()
end

function OnDraw(c)
	--purple = Color:argb(255,200,40,200)
	--c:drawColor(purple)
	if(timeordinal == nil) then
		return
	end
	
	c:drawLine(0,(height/2),tonumber(width),height/2,bgSkyPaint)
	
	--for daylight, see if the timeordinal is between 8am and 8pm, calculate it as a value between 0-12
	updateAlphas()
	
	c:drawRect(0,0,tonumber(width),tonumber(height),fgSkyPaint)
	
	c:drawRect(0,0,tonumber(width),tonumber(height),fgNightPaint)
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
		tmp = tmp * travel.sun
		
		tmp = tmp + axis.sun --shift to left side
		--compute y param
		y = aval.sun*(tmp-axis.sun)*(tmp-axis.sun)+constant.sun
		
		c:drawCircle(tmp,y,radius.sun,sunPaint)
		
		--
		--debugPrint("doing sun at:"..tmp..","..y)
	end
	
	--do the moons
	if(timers.black ~= nil and (timers.black >= 38 and timers.black <= 50)) then

		total = 13
		
		progress = total - (51 - timers.black) + (subcount / 12)
		
		x = (progress/total) - 0.5
		x = (x * travel.black) --remember to add back the axis to the final x
		y = aval.black * x * x + constant.black
		c:drawCircle(x+axis.black,y,radius.black,blackPaint)
		--Note(string.format("\black at: %d,%d, prog: %f,aval: %f, axis: %d,constant: %d\n",x+axis.black,y,progress,aval.black,axis.black,constant.black))
	end
	
	if(timers.white ~= nil and (timers.white >= 49 and timers.white <= 65)) then

		progress = 17 - (66 -timers.white) + (subcount / 12) 
		total = 17
		x = (progress/total)-0.5
		x = (x * travel.white) --remember to add back the axis to the final x
		y = aval.white * x * x + constant.white
		--debugPrint("doing white moon: x:"..(x+axis.white).." y:"..y.." radius:"..radius.white.." whitePaint:"..whitePaint:toString())
		
		c:drawCircle(x+axis.white,y,radius.white,whitePaint)
	end
	
	if(timers.grey ~= nil and (timers.grey >= 23 and timers.grey <= 30)) then

		total = 8
		progress = total - (31-timers.grey)+ (subcount / 12)
		
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
	
	--height = view:getHeight()
	--width = view:getWidth()
	
	textwidth = clockpaint:measureText(str)
	
	x = width/4
	y = height/3
	
	x = x - textwidth/2
	if(x < 0) then
		x = 0
	end
	y = y + clocktextsize/2
	
	timerFrameRect:set(x-(5*density),y-clocktextsize-(5*density),x+textwidth+(5*density),y+(10*density))
	
	c:drawRoundRect(timerFrameRect,6*density,6*density,timerFrameBg)
	c:drawRoundRect(timerFrameRect,6*density,6*density,timerFramePaint)
	
	
	c:drawText(str,x,y,clockpaint)
	
	--draw the moon tick counters timer values
	local black_y = height/4
	local grey_y = height/2
	local white_y = black_y*3
	
	local tmp = {}
	tmp.black = timers.black or "??"
	tmp.grey = timers.grey or "??"
	tmp.white = timers.white or "??"
	
	c:drawText(string.format("%s",tmp.black),5*density,black_y,blackPaint)
	c:drawText(string.format("%s",tmp.grey),5*density,grey_y,greyPaint)
	c:drawText(string.format("%s",tmp.white),5*density,white_y,whitePaint)
	
	if(timers.forecast ~= nil and timers.forecast > -1) then
		c:drawText(string.format("Three moons in: %d ticks, for %d ticks",timers.forecast,timers.duration),10*density,height-(5*density),whitePaint)
	end
	
end




subcount = 0

normalSubDelay = 2490
fastSubDelay = 500

function subCount()
	subcount = subcount + 1
	docallback = false
	if(subcount > 11) then
		subcount = 0
	elseif(subcount == 11) then
		--debugPrint("TICK INCOMING")
		view:invalidate()
	else
		docallback = true
		--debugPrint("SUB TICKING")
		view:invalidate()
	end
	if(docallback) then
		ScheduleCallback(100,"subCount",normalSubDelay)
		view:invalidate()
	else
		--if(not timertmp) then
		--	timertmp = {}
		--	timertmp.grey = 23
		--	timertmp.black = 37
		--	timertmp.white = 49
		--else
		--	timertmp.grey = timertmp.grey + 1
		--	timertmp.black = timertmp.black + 1
		--	Note(string.format("\nadding black moon, timer tick %d\n",timertmp.black))
		--	timertmp.white = timertmp.white + 1
		--	
		--	if(timertmp.grey == 32) then
		--		timertmp.grey = 1
		--	end
			
		--	if(timertmp.black == 50) then
		--	Note(string.format("\nresetting black moon, timer tick %d\n",timertmp.black))
		--		timertmp.black = 1
		--	end
			
		--	if(timertmp.white == 67) then
		--		timertmp.white = 1
		--	end
		--end
		
		--tickIncoming(serialize(timertmp))
	end
end

--start ticking.
ScheduleCallback(100,"subCount",normalSubDelay)


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
		
		if(timeordinal == 18) then
			dosun = false
		else
			dosun = true
		end
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



height = 1
width = 1

function OnSizeChanged(neww,newh,oldw,oldh)
	height = newh
	width = neww
	
	clocktextsize = newh/2.2
	clockpaint = luajava.newInstance("android.graphics.Paint")
	clockpaint:setTextSize(clocktextsize)
	clockfacecolor = Color:argb(255,0,0,255)
	clockpaint:setColor(clockfacecolor)
	clockpaint:setAntiAlias(true)
	
	rebuildConstants()
end

function rebuildConstants()
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
travel.sun = width/2
travel.black = width/6
travel.white = width/2
travel.grey = width/6

radius={}
--radius.sun = 20
--radius.black = 10
--radius.grey = 14
--radius.white = 8
radius.sun = height/5
radius.black = height/10
radius.grey = height/7
radius.white = height/12.5


constant = {}
constant.sun = radius.sun
constant.white = height/2
constant.black = (height/2)-2*radius.black
constant.grey = (height/2)-radius.grey

axis = {}
axis.sun = width*0.75
axis.grey = axis.sun-(height/10)
axis.black = axis.sun+(height/40)
axis.white = axis.sun-(height/40)

tmpval = {}
tmpval.sun = axis.sun - (travel.sun/2) - axis.sun
tmpval.white = axis.white - (travel.white/2) - axis.white
tmpval.grey = axis.grey - (travel.grey/2) - axis.grey
tmpval.black = axis.black -(travel.black/2) - axis.black


aval = {}
aval.sun = ((height+radius.sun)-constant.sun)/(tmpval.sun*tmpval.sun)
aval.grey = ((height+radius.grey)-constant.grey)/(tmpval.grey*tmpval.grey)
aval.black = ((height+radius.black)-constant.black)/(tmpval.black*tmpval.black)
aval.white = ((height+radius.white)-constant.white)/(tmpval.white*tmpval.white)
end

MeasureSpec = luajava.bindClass("android.view.View$MeasureSpec")
function OnMeasure(wspec,hspec)
	local width = MeasureSpec:getSize(wspec)
	local height = width/4
	return width,height
end

config = {}
config.divider = true
config.height = WRAP_CONTENT
config.width = MATCH_PARENT
config.id = view:getId()
InstallWindow(config)