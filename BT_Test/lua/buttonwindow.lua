--debugPrint("package path:"..package.path)
package.path = "/mnt/sdcard/BlowTorch/?.lua"
--debugPrint("package path:"..package.path)
require("button")
require("serialize")
defaults = nil

function loadButtons(args)

	--debugPrint("WindowXCallS Succeeded!")
	--for i,v in pairs(args) do
	--	if(istable(v)) then
	--	debugPrint(i.."=>"..v)
	--end
	--printTable("args",args)
	local tmp = loadstring(args)()
	printTable("defs",tmp.default)
	--set up metatables.
	--if(args.defaults == nil) then
	defaults = BUTTONSET_DATA:new(tmp.default)
	--setmetatable(BUTTON_DATA,defaults)
	--BUTTON_DATA.__index = defaults
	
	--setmetatable(BUTTON_DATA,defaults)
	BUTTON_DATA.__index = defaults
	--defaults.__index = 
	buttons = {}
	--setmetatable(BUTTON_DATA,defaults)
	for i,v in pairs(tmp.set) do
		--debugPrint("PROCESSING NEW BUTTON"..i)
		buttons[i] = BUTTON:new(v)
	end
	debugPrint(string.format("Debuggin:%d,%d",buttons[1].data.primaryColor,defaults.primaryColor))
	--for i,v in pairs(buttons) do
	--	debugPrint("buuuuuton"..i)
	--end
	--buttons = foo
	drawButtons()
	invalidate()
	--else
	--end
	
end

function printTable(key,o)
	for i,v in pairs(o) do
		if(type(v)=="table") then
			printTable(key.."."..i,v)
		else 
			debugPrint(key.."."..i.."<==>"..v)
		end
	end
end

Bitmap = luajava.bindClass("android.graphics.Bitmap")
BitmapConfig = luajava.bindClass("android.graphics.Bitmap$Config")
PorterDuffMode = luajava.bindClass("android.graphics.PorterDuff$Mode")
PaintClass = luajava.bindClass("android.graphics.Paint")
Array = luajava.bindClass("java.lang.reflect.Array")

selectedLayer = nil
selectedCanvas = nil
anySelected = false

moveTouch = {}
touchMoving = false
moveBounds = luajava.newInstance("android.graphics.RectF")
moveBitmap = nil
moveCanvas = nil
moveStart = {}
moveStart.x = 0
moveStart.y = 0
moveDelta = {}
moveDelta.x = 0
moveDelta.y = 0
moveCurrent = {}
moveCurrent.x = 0
moveCurrent.y = 0
moveCapOnce = true
totalDelta = {}
totalDelta.x = 0
totalDelta.y = 0
function moveTouch.onTouch(v,e)
	local x = e:getX()
	local y = e:getY()
	
	if(e:getAction() == MotionEvent.ACTION_DOWN) then
		if(moveBounds:contains(x,y)) then
			touchMoving = true
			
			moveCurrent.x = x
			moveCurrent.y = y
			if(moveCapOnce) then
				moveStart.x = x
				moveStart.y = y
				--boundsStart.x = moveBounds:centerX()
				--boundsStart.y = moveBounds:centerY()
				moveCapOnce = false
			end
			return true
		else 
			touchMoving = false
		end
	end
	
	if(e:getAction() == MotionEvent.ACTION_MOVE) then
		if(touchMoving) then
			moveDelta.x = x - moveCurrent.x
			moveDelta.y = y - moveCurrent.y
			moveCurrent.x = x
			moveCurrent.y = y
			--debugPrint("moving box")
			totalDelta.x = totalDelta.x + moveDelta.x
			totalDelta.y = totalDelta.y + moveDelta.y
			moveBounds:offset(moveDelta.x,moveDelta.y)
			invalidate()
			return true
		else
			return true
		end
	end
	
	if(e:getAction() == MotionEvent.ACTION_UP) then
		if(touchMoving == false) then
			moveCanvas = nil
			moveBitmap = nil
			local dx = totalDelta.x
			local dy = totalDelta.y
			totalDelta.x = 0
			totalDelta.y = 0
			for i,b in pairs(buttons) do
				if(b.selected == true) then
					local r = b.rect
					--r:offset(dx,dy)
					b.data.x = b.data.x + dx
					b.data.y = b.data.y + dy
					b.selected = false
					updateSelected(b,false)
					b:updateRect()
				end
			end
			drawButtons()
			view:setOnTouchListener(managerTouch_cb)
			
			invalidate()
			return true
		else
			touchmoving = false
			return true
		end
	end
	
	return true
end
moveTouch_cb = luajava.createProxy("android.view.View$OnTouchListener",moveTouch)

function enterMoveMode()
	--create a bounding box
	local x1 = 0
	local y1 = 0
	local x2 = 0
	local y2 = 0
	local first = true
	for i,b in pairs(buttons) do
		if(b.selected == true) then
			local r = b.rect
			if(first) then
				debugPrint("here")
				x1 = r.left
				y1 = r.top
				x2 = r.right
				y2 = r.bottom
				first = false
			else
				debugPrint("there")
				if(r.left < x1) then
					x1 = r.left
				end
				if(r.top < y1) then
					y1 = r.top
				end
				if(r.right > x2) then
					x2 = r.right
				end
				if(r.bottom > y2) then
					y2 = r.bottom
				end
				
			end
		end
	end
	debugPrint("nhere")
	moveBounds:set(x1,y1,x2,y2)
	
	moveBounds:inset(-40,-40)
	
	debugPrint("mhere")
	local width = moveBounds.right - moveBounds.left
	local height = moveBounds.bottom - moveBounds.top
	debugPrint("making new bitmap:"..width.."x"..height)
	moveBitmap = Bitmap:createBitmap(width,height,BitmapConfig.ARGB_8888)
	moveCanvas = luajava.newInstance("android.graphics.Canvas",moveBitmap)
	--draw the shiz.
	debugPrint("zhere")
	moveCanvas:drawARGB(0x88,0x88,0x88,0x88)
	view:setOnTouchListener(moveTouch_cb)
	moveCanvas:save()
	moveCanvas:translate(40,40)
	moveCanvas:translate(-1*x1,-1*y1)
	for i,b in pairs(buttons) do
		if(b.selected == true) then
			b:draw(0,moveCanvas)
			--debugPrint("debugprinting:"..x1.."x"..y1)
			--moveCanvas:drawRect(b.rect,b.paintOpts)
		end
	end
	drawButtonsNoSelected()
	moveCanvas:restore()
	invalidate()
end

managerTouch = {}
function managerTouch.onTouch(v,e)
	--debugPrint("ALT TOUCH ROUTINE YEA!"..e:getX().." "..e:getY())
	local x = e:getX()
	local y = e:getY()
	--debugPrint("on touch event started")
	if(e:getAction() == MotionEvent.ACTION_DOWN) then
		debugPrint("manager move down")
		--find if press was in a button
		ret,b,index = buttonTouched(x,y)
		if(ret) then
			fingerdown = true
			touchedbutton = b
			touchedindex = index
			debugPrint(string.format("Button touched @ x:%d y:%d, buttoncenter x:%d,y:%d",x,y,touchedbutton.data.x,touchedbutton.data.y))
			--if(#buttons > 50 and not manage) then
			--	aa = luajava.newInstance("android.view.animation.AlphaAnimation",1.0,0.0)
				--aa = AlphaAnimation.new(1.0,0.0)
			--	aa:setDuration(1500)
			--	aa:setFillAfter(false)
			--	view:startAnimation(aa)
			--end
			return true
		else
			--we are draggin now
			if(manage) then
				dragstart.x = x
				dragstart.y = y
				return true
			end
		end
	end

	if(e:getAction() == MotionEvent.ACTION_MOVE) then

		--if(not manage
		--debugPrint("manager move move")
		if(prevevent == 0) then
			prevevent = e:getEventTime()
		else
			now = e:getEventTime()
			local elapsed = now - prevevent
			if(elapsed > 10) then
			--proceed
				--debugPrint("processing move event")
				prevevent = now
			else
				return true --consume but dont process.
			end
		end




		--debugPrint("ACTION MOVING"..touchedbutton.x)

		if(not fingerdown and manage) then
			--we are drag moving now.
			dragcurrent.x = x
			dragcurrent.y = y
			--compute distance
			distance = math.sqrt(math.pow(dragcurrent.x-dragstart.x,2)+math.pow(dragcurrent.y-dragstart.y,2))
			if(distance < 10) then
				return true
			end
			dragmoving = true
			
			--drawDragBox()
			checkIntersects()
			invalidate()
			return true
		end

		if(fingerdown and manage) then
			local modx = (math.floor(e:getX()/gridwidth)*gridwidth)+(gridwidth/2)
			local mody = (math.floor(e:getY()/gridwidth)*gridwidth)+(gridwidth/2)
			
			touchedbutton.data.x = modx
			touchedbutton.data.y = mody
			touchedbutton:updateRect()
			drawButtons()
			invalidate()
			return true
		else
			return true
		end
	end

	if(e:getAction() == MotionEvent.ACTION_UP) then
		debugPrint("manager move up")
		if(dragmoving) then
		debugPrint("dragmoving")
			--redraw the screen without the drag rect

			--drawButtons()
			dragmoving = false
			invalidate()
			return true
		end
		if(manage and not fingerdown) then
		debugPrint("new button")
			--lawl, make new button
			local modx = (math.floor(x/gridwidth)*gridwidth)+(gridwidth/2)
			local mody = (math.floor(y/gridwidth)*gridwidth)+(gridwidth/2)
			debugPrint("new button at: "..modx..","..mody)
			local butt = addButton(modx,mody)
			--butt.width = gridwidth
			--butt.height = gridwidth
			--canvas:drawRoundRect(butt.rect,5,5,paint)
			butt:draw(0,buttonCanvas)
			invalidate()
			return true
		end
		if(manage and fingerdown and touchedbutton.selected == true) then
			--launch editor selection screen
			debugPrint("selected touched")
			showEditorSelection()
			touchedbutton={}
			fingerdown = false
			return true
		end

		touchedbutton = {}
		fingerdown = false
		return true
	end

	return false
	--return true
end

managerTouch_cb = luajava.createProxy("android.view.View$OnTouchListener",managerTouch)

normalTouch = {}
normalTouchState = 0
function normalTouch.onTouch(v,e)
	
	local retvalue = false
	local x = e:getX()
	local y = e:getY()
	--debugPrint("normal touch, start")
	if(e:getAction() == MotionEvent.ACTION_DOWN) then
		prevevent = 0
		ret,b,index = buttonTouched(x,y)
		if(ret) then
			fingerdown = true
			touchedbutton = b
			touchedindex = index
			clearButton(b)
			normalTouchState = 1
			clearButton(b)
			b:draw(normalTouchState,buttonCanvas)
			invalidate()
			return true
		else
			fingerdown = false;
			--debugPrint("action down, returning false")
			return false
		end
	end
	
	--debugPrint("move0")
			
	if(e:getAction() == MotionEvent.ACTION_MOVE) then
		--debugPrint("move1")
		
		if(fingerdown == false) then
			--debugPrint("action move, no finger down, returning false")
			return false
		end
		--debugPrint("move2")
		
		if(prevevent == 0) then
			prevevent = e:getEventTime()
		else
			now = e:getEventTime()
			local elapsed = now - prevevent
			if(elapsed > 60) then
			--proceed
				--debugPrint("processing move event")
				prevevent = now
			else
				--debugPrint("action move, consuming, returning true")
			
				return true --consume but dont process.
			end
		end
	
		if(fingerdown) then
			local r = touchedbutton.rect
			if(r:contains(x,y)) then
				if(normalTouchState ~= 1) then
					normalTouchState = 1
					clearButton(b)
					b:draw(normalTouchState,buttonCanvas)
				end
			else
				if(normalTouchState ~= 2) then
					normalTouchState = 2
					clearButton(b)
					b:draw(normalTouchState,buttonCanvas)
				end
			end
			invalidate()
			--debugPrint("action move, moving button, returning true")
			
			return true
		else
			--debugPrint("reached end of normal touch handler, returning false")
			return false
		end
	end
	
	if(e:getAction() == MotionEvent.ACTION_UP) then
		if(fingerdown) then
			local r = touchedbutton.rect
			if(r:contains(x,y)) then
				--process primary touch
				debugPrint("primary touch")
			else
				--process secondary touch
				debugPrint("secondary touch")
			end
			normalTouchState = 0
			clearButton(touchedbutton)
			touchedbutton:draw(normalTouchState,buttonCanvas)
			invalidate()
			return true
		else
			--debugPrint("button not touched, returning false")
			return false
		end
	end
	--debugPrint("reached end of normal touch handler, returning false")
	return false
end
normalTouch_cb = luajava.createProxy("android.view.View$OnTouchListener",normalTouch)
view:setOnTouchListener(normalTouch_cb)
--this window is a full screen window, so we don't really need to concern ourselves with bounds and the such, but we do need to create a button class.
RectFClass = luajava.bindClass("android.graphics.RectF")
function updateSelected(b,sel)
	local p = b.paintOpts
	clearButton(b)
	local redrawScreen = false
	if(sel) then
		p:setShadowLayer(1,0,0,Color.WHITE)
		b.selected = true
		b:draw(0,buttonCanvas)
	else
		p:setShadowLayer(0,0,0,Color.WHITE)
		b.selected = false
	end
	
	
	--invalidate()
end

dragRect = luajava.newInstance("android.graphics.RectF")
selectedBounds = luajava.newInstance("android.graphics.RectF")
function checkIntersects()
	--compute new drag rect
	local x1 = 0
	local y1 = 0
	local x2 = 0
	local y2 = 0
	
	if(dragstart.x < dragcurrent.x) then
		x1 = dragstart.x
		x2 = dragcurrent.x
	else 
		x1 = dragcurrent.x
		x2 = dragstart.x
	end
	
	if(dragstart.y < dragcurrent.y) then
		y1 = dragstart.y
		y2 = dragcurrent.y
	else 
		y1 = dragcurrent.y
		y2 = dragstart.y
	end
	
	dragRect:set(x1,y1,x2,y2)
	local redrawscreen = false
	anySelected = false
	for i,b in pairs(buttons) do
		local rect = b.rect
			if(intersectMode == 0) then
				if(RectFClass:intersects(dragRect,rect) or dragRect:contains(rect)) then
				if(b.selected == false) then
					updateSelected(b,true)
					anySelected = true
				end
			else
				if(b.selected == true) then
					updateSelected(b,false)
					redrawscreen = true
				end
			end
		end
		
		if(intersectMode == 1) then
			if(dragRect:contains(rect)) then
				if(b.selected == false) then
					updateSelected(b,true)
					anySelected = true
				end
			else
				if(b.selected == true) then
					updateSelected(b,false)
					redrawscreen = true
				end
			end	
		end
		
		
	end 
	
	if(redrawscreen) then
		drawButtons()
	end
end


buttons = {}

--BitmapFactory = luajava.bindClass("android.graphics.BitmapFactory")
--bmp = BitmapFactory:decodeFile("/mnt/sdcard/BlowTorch/testimage.png")

fingerdown = false
manage = false

paint = luajava.new(PaintClass)
paint:setAntiAlias(true)
bounds = nil


function OnCreate()
	debugPrint("in oncreate")	
	for i,b in ipairs(buttons) do
		updateRect(b)
	end
	paint:setARGB(0xAA,0x00,0x33,0xAA)
	--bounds = getBounds()
	--drawButtons()
	addOptionCallback("buttonOptions","Lua Button Options",nil)
	
end


managerLayer = nil
managerCanvas = nil


cpaint = luajava.new(PaintClass)
cpaint:setARGB(0x00,0x00,0x00,0x00)
cpaint:setXfermode(xferMode)

checkchange = {}
function checkchange.onCheckedChanged(v,ischecked)
	debugPrint("starting check change")
	if(ischecked) then
		managerLayer = Bitmap:createBitmap(view:getWidth(),view:getHeight(),BitmapConfig.ARGB_8888)
		managerCanvas = luajava.newInstance("android.graphics.Canvas",managerLayer)
		debugPrint("drawingManagerLayer")
		drawManagerLayer()
		--paint:setShadowLayer(1,0,0,Color.WHITE)
		view:setOnTouchListener(managerTouch_cb)
		manage = true
	else
		debugPrint("check unset")
		managerCanvas = nil
		managerLayer:recycle()
		managerLayer = nil
		manage = false
		view:setOnTouchListener(normalTouch_cb)
		
		--save settings.
		local tmp = {}
		for i,b in pairs(buttons) do
			tmp[i] = b.data
		end
		
		PluginXCallS("saveButtons",serialize(tmp))
		
		--paint:setShadowLayer(0,0,0,Color.WHITE)
	end
	drawButtons()
	invalidate()
end

function drawManagerLayer()
		local c = managerCanvas
		local width = view:getWidth()
		local height = view:getHeight()
		debugPrint("starting draw")
		c:drawRect(0,0,width,height,cpaint)
		debugPrint("canvas is not null")
		c:drawARGB(0xFF,0x0A,0x0A,0x0A)
		--draw dashed lines.
		local times = width / gridwidth
		for x=1,times do
			c:drawLine(gridwidth*x,0,gridwidth*x,height,dpaint)
		end
		
		times = height / gridwidth
		for y=1,times do
			c:drawLine(0,gridwidth*y,width,gridwidth*y,dpaint)
		end

end

checkchange_cb = luajava.createProxy("android.widget.CompoundButton$OnCheckedChangeListener",checkchange)

gridwidth = 100
seeker = {}
function seeker.onProgressChanged(v,prog,state)
	debugPrint("seekbarchanged:"..prog)
	gridwidth = 50 + prog
	--managerCanvas:clearCanvas()
	drawManagerLayer()
	--drawButtons()
	invalidate()
end

seeker_cb = luajava.createProxy("android.widget.SeekBar$OnSeekBarChangeListener",seeker)

radio = {}
function radio.onCheckedChanged(group,id)
	debugPrint("radio buttons changed")
	if(id == 0) then
	 --intersect
	 debugPrint("intersect selected")
	 intersectMode = id
	end
	if(id == 1) then
	 --contains
	 debugPrint("contains selected")
	 intersectMode = id
	end
end

radio_cb = luajava.createProxy("android.widget.RadioGroup$OnCheckedChangeListener",radio)

intersectMode = 1

function buttonOptions()
	ctex = view:getContext()

	ll = luajava.newInstance("android.widget.LinearLayout",ctex)
	ll:setOrientation(1)
	lp = luajava.newInstance("android.view.ViewGroup$LayoutParams",-1,-2)

	cb = luajava.newInstance("android.widget.CheckBox",ctex)
	cb:setChecked(manage)
	cb:setText("Show Grid/Manage Buttons")
	cb:setOnCheckedChangeListener(checkchange_cb)
	debugPrint("seekbar creation")
	sb = luajava.newInstance("android.widget.SeekBar",ctex)
	sb:setOnSeekBarChangeListener(seeker_cb)
	--sb:setMinimum(10)
	--sb:setMax(200)
	--sb:setProgress(gridwidth)
	
	rg_static = luajava.bindClass("android.widget.RadioGroup")
	
	rg = luajava.newInstance("android.widget.RadioGroup",ctex)
	rg:setOnCheckedChangeListener(radio_cb)
	
	contain = luajava.newInstance("android.widget.RadioButton",ctex)
	contain:setText("Contains")
	contain:setId(1)
	
	intersect = luajava.newInstance("android.widget.RadioButton",ctex)
	intersect:setText("Intersect")
	intersect:setId(0)
	
	rg_lp = luajava.bindClass("android.widget.RadioGroup$LayoutParams")
	
	rg_lp_gen = luajava.new(rg_lp,lp)
	
	
	
	rg:addView(intersect,0,rg_lp_gen)
	rg:addView(contain,1,rg_lp_gen)
	rg:check(intersectMode)
	debugPrint("adding views")
	
	ll:addView(cb,lp)
	ll:addView(sb,lp)
	ll:addView(rg,lp)
	debugPrint("builder alert creation")
	builder = luajava.newInstance("android.app.AlertDialog$Builder",ctex)

	builder:setView(ll)
	alert = builder:create()
	alert:show()
end



tpaint = luajava.new(PaintClass)
tpaint:setTextSize(15)
tpaint:setARGB(0xFF,0xAA,0xAA,0xAA)
tpaint:setAntiAlias(true)
--PorterDuff = luajava.bindClass("android.graphics.PorterDuff.Mode")

bpaint = luajava.new(PaintClass)
dpaint = luajava.new(PaintClass)

Paint = luajava.bindClass("android.graphics.Paint")
Color = luajava.bindClass("android.graphics.Color")
--dpaint:setStyle(Paint.Style.STROKE)
dpaint:setARGB(0xFF,0xFF,0x00,0x00)
--dpaint:setShadowLayer(6,0,0,Color.YELLOW)


Float = luajava.newInstance("java.lang.Float",0)
ten = luajava.newInstance("java.lang.Float",2)
FloatClass = Float:getClass()
rawfloatclass = FloatClass.TYPE
farray = Array:newInstance(rawfloatclass, 2)
Array:setFloat(farray,0,ten:floatValue())
Array:setFloat(farray,1,ten:floatValue())
dash = luajava.newInstance("android.graphics.DashPathEffect",farray,Float:floatValue())

Style = luajava.bindClass("android.graphics.Paint$Style")
dpaint:setStyle(Style.STROKE)

dpaint:setPathEffect(dash)
dpaint:setStrokeWidth(2)

--Style = luajava.bindClass("android.graphics.Paint$Style")
function drawButtons()
	local canvas = buttonCanvas
	height = view:getHeight()
	width = view:getWidth()

	--canvas:clearCanvas()
	canvas:drawRect(0,0,width,height,cpaint)
	
	--if(manage) then
	--	canvas:drawBitmap(managerBitmap,0,0,nil)
	--end
	--local counter = 0
	for i,b in pairs(buttons) do
		--debugPrint("DRAWING BUTTON"..i)
		b:draw(0,buttonCanvas)
		--counter = counter + 1
	end
	--debugPrint("DRAWING "..counter.." BUTTONS")
end

function drawButtonsNoSelected()
	local canvas = buttonCanvas
	height = view:getHeight()
	width = view:getWidth()

	--canvas:clearCanvas()
	canvas:drawRect(0,0,width,height,cpaint)
	
	--if(manage) then
	--	canvas:drawBitmap(managerBitmap,0,0,nil)
	--end

	for i,b in pairs(buttons) do
		if(b.selected ~= true) then
			b:draw(0,buttonCanvas)
		end
	end
end

function clearButton(b)
	local canvas = buttonCanvas
	local p = b.paintOpts
	p:setXfermode(xferMode)
	canvas:drawRoundRect(b.rect,5,5,b.paintOpts)
	p:setXfermode(nil)
	--c:drawBitmap(bmp,b.x,b.y,nil)
	--local tX = b.x - (tpaint:measureText(b.text)/2)
	--local tY = b.y + (tpaint:getTextSize()/2)
	--canvas:drawText(b.text,tX,tY,tpaint)
end


touchedbutton = {}
touchedindex = 0
MotionEvent = luajava.bindClass("android.view.MotionEvent")
prevevent = 0;

dragmoving = false
dragstart = {}
dragstart.x = -1
dragstart.y = -1

dragcurrent = {}
dragcurrent.x = -1
dragcurrent.y = -1

function OldTouchEvent(e)
	


	x = e:getX()
	y = e:getY()
	--debugPrint("on touch event started")
	if(e:getAction() == MotionEvent.ACTION_DOWN) then
		--find if press was in a button
		ret,b,index = buttonTouched(x,y)
		if(ret) then
			fingerdown = true
			touchedbutton = b
			touchedindex = index
			debugPrint(string.format("Button touched @ x:%d y:%d, buttoncenter x:%d,y:%d",x,y,touchedbutton.x,touchedbutton.y))
			if(#buttons > 50 and not manage) then
				aa = luajava.newInstance("android.view.animation.AlphaAnimation",1.0,0.0)
				--aa = AlphaAnimation.new(1.0,0.0)
				aa:setDuration(1500)
				aa:setFillAfter(false)
				view:startAnimation(aa)
			end
			return true
		else
			--we are draggin now
			if(manage) then
				dragstart.x = x
				dragstart.y = y
				return true
			end
		end
	end

	if(e:getAction() == MotionEvent.ACTION_MOVE) then

		--if(not manage

		if(prevevent == 0) then
			prevevent = e:getEventTime()
		else
			now = e:getEventTime()
			local elapsed = now - prevevent
			if(elapsed > 30) then
			--proceed
				--debugPrint("processing move event")
				prevevent = now
			else
				return true --consume but dont process.
			end
		end




		--debugPrint("ACTION MOVING"..touchedbutton.x)

		if(not fingerdown and manage) then
			--we are drag moving now.
			dragmoving = true
			dragcurrent.x = x
			dragcurrent.y = y
			--drawDragBox()
			checkIntersects()
			invalidate()
			return true
		end

		if(fingerdown and manage) then
			local modx = (math.floor(e:getX()/gridwidth)*gridwidth)+(gridwidth/2)
			local mody = (math.floor(e:getY()/gridwidth)*gridwidth)+(gridwidth/2)
			
			touchedbutton.x = modx
			touchedbutton.y = mody
			updateRect(touchedbutton)
			--drawButton(touchedbutton)
			drawButtons()
			invalidate()
			return true
		else
			return false
		end
	end

	if(e:getAction() == MotionEvent.ACTION_UP) then
		if(dragmoving) then
			--redraw the screen without the drag rect

			--drawButtons()
			dragmoving = false
			invalidate()
			return true
		end
		if(manage and not fingerdown) then
			--lawl, make new button
			local modx = (math.floor(x/gridwidth)*gridwidth)+(gridwidth/2)
			local mody = (math.floor(y/gridwidth)*gridwidth)+(gridwidth/2)
			debugPrint("new button at: "..modx..","..mody)
			local butt = addButton(modx,mody)
			--butt.width = gridwidth
			--butt.height = gridwidth
			--canvas:drawRoundRect(butt.rect,5,5,paint)
			butt:draw(0,buttonCanvas)
			invalidate()
			return true
		end
		if(manage and fingerdown and touchedbutton.selected == true) then
			--launch editor selection screen
			showEditorSelection()
			touchedbutton={}
			fingerdown = false
			return true
		end

		touchedbutton = {}
		fingerdown = false
		return false
	end

	return false
	
end

--Float = luajava.newInstance("java.lang.Float",0)
--ten = luajava.newInstance("java.lang.Float",2)
--FloatClass = Float:getClass()
--rawfloatclass = FloatClass.TYPE
--farray = Array:newInstance(rawfloatclass, 2)
--Array:setFloat(farray,0,ten:floatValue())
--Array:setFloat(farray,1,ten:floatValue())

String = luajava.newInstance("java.lang.String")
StringClass = String:getClass()

editorItems = Array:newInstance(StringClass,3)
Array:set(editorItems,0,"Move")
Array:set(editorItems,1,"Edit")
Array:set(editorItems,2,"Delete")

editorListener = {}
function editorListener.onClick(dialog,which)
	debugPrint("Editor: "..Array:get(editorItems,which).." selected.")
	local newbuttons = {}
	if(which == 2) then
		while(table.getn(buttons) > 0) do 
			b = table.remove(buttons)
			if(b.selected == false) then
				table.insert(newbuttons,b)
			else
				b = nil
			end
		end
		buttons=newbuttons
		drawButtons()
		invalidate()
	end
	if(which == 0) then
		enterMoveMode()
	end
end
editorListener_cb = luajava.createProxy("android.content.DialogInterface$OnClickListener",editorListener)
function showEditorSelection()
	local count = 0
	for i,b in ipairs(buttons) do
		if(b.selected == true) then
			count = count + 1
		end
	end
	
	local build = luajava.newInstance("android.app.AlertDialog$Builder",view:getContext())
	
	build:setItems(editorItems,editorListener_cb)
	build:setTitle(count.." buttons selected.")
	alert = build:create()
	alert:show()

end

counter = 0

function addButton(pX,pY) 
	local newb = BUTTON:new({x=pX,y=pY,label="newb"})
	--newb.x = x
	--newb.y = y
	newb.data.width = gridwidth-5
	newb.data.height = gridwidth-5
	newb.data.label = "newb"..counter
	counter = counter+1
	--newb.rect = luajava.newInstance("android.graphics.RectF")
	--newb.paintOpts = luajava.new(PaintClass,paint)
	--newb.selected = false
	newb:updateRect()
	table.insert(buttons,newb)
	return newb
end

function buttonTouched(x,y)
	for i,b in pairs(buttons) do
		local z = b.rect
		if(z:contains(x,y)) then
			return true,b,i
		end
	end
	return false
end

function updateRect(b)
	left = b.x - (b.width/2)
	right = b.x + (b.width/2)
	top = b.y - (b.height/2)
	bottom = b.y + (b.height/2)
	tmp = b.rect
	tmp:set(left,top,right,bottom)
end

buttonLayer = nil
buttonCanvas = nil
draw = false
function OnSizeChanged(w,h,oldw,oldh)
	if(w == 0 and h == 0) then
		draw = false
		return
	end
	local ccl = luajava.bindClass("android.graphics.Color")
	local colord = ccl:argb(0x88,0x00,0x00,0xFF)
	local iclas = luajava.bindClass("java.lang.Integer")
	debugPrint("DebugString: "..string.format("%d,%s",colord,iclas:toHexString(colord)))
	
	debugPrint("Window Sized Changed:"..w.."x"..h)
	
	
	if(buttonLayer) then
		buttonCanvas = nil
		buttonLayer:recycle()
		buttonLayer = nil
		
	end
	
	if(selectedLayer) then
		selectedCanvas = nil
		selectedLayer:recycle()
		selectedLayer = nil
		
	end
	
	buttonLayer = Bitmap:createBitmap(view:getWidth(),view:getHeight(),BitmapConfig.ARGB_8888)
	buttonCanvas = luajava.newInstance("android.graphics.Canvas",buttonLayer)
	
	selectedLayer = Bitmap:createBitmap(view:getWidth(),view:getHeight(),BitmapConfig.ARGB_8888)
	selectedCanvas = luajava.newInstance("android.graphics.Canvas",selectedLayer)
	--managerLayer = Bitmap.create(w,h,BitmapConfig.ARGB_8888)
	drawButtons()
	draw = true
end

dragDashPaint = luajava.new(PaintClass)
dragDashPaint:setARGB(0xFF,0x77,0x00,0x88)
dragDashPaint:setPathEffect(dash)
dragDashPaint:setStyle(Style.STROKE)
dragDashPaint:setStrokeWidth(7)

dragBoxPaint = luajava.new(PaintClass)
dragBoxPaint:setARGB(0x33,0x77,0x00,0x33)

function OnDraw(canvas)
	if(manage) then
		canvas:drawBitmap(managerLayer,0,0,nil)
	end
	
	if(draw) then
		canvas:drawBitmap(buttonLayer,0,0,nil)
	end
	
	if(dragmoving) then
		--debugPrint("I SHOULD BE DRAG MOVING")
		canvas:drawRect(dragstart.x,dragstart.y,dragcurrent.x,dragcurrent.y,dragBoxPaint)
		canvas:drawRect(dragstart.x,dragstart.y,dragcurrent.x,dragcurrent.y,dragDashPaint)
		
	end
	
	if(moveBitmap ~= nil) then
		canvas:drawBitmap(moveBitmap,moveBounds.left,moveBounds.top,nil)
	end
	
end


