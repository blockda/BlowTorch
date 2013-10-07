respath = package.path
respath = string.sub(respath,0,string.find(respath,"?")-1).."res"

require("button")
require("serialize")
require("bit")
local marshal = require("marshal")
defaults = nil

debugInfo = false

local function debugString(string)
	if(debugInfo) then
		Note(string.format("\n%s\n",string))
	end
end

debugString("Button Window Script Loading...")

density = GetDisplayDensity()
Configuration = luajava.bindClass("android.content.res.Configuration")

TypedValue = luajava.bindClass("android.util.TypedValue")
Adapter = luajava.bindClass("android.widget.Adapter")
ListView = luajava.bindClass("android.widget.ListView")
ImageButton = luajava.bindClass("android.widget.ImageButton")
LayoutInflater = luajava.bindClass("android.view.LayoutInflater")
Context = luajava.bindClass("android.content.Context")
R_id = luajava.bindClass("com.offsetnull.bt.R$id")
R_layout = luajava.bindClass("com.offsetnull.bt.R$layout")
R_drawable = luajava.bindClass("com.offsetnull.bt.R$drawable")
android_R_id = luajava.bindClass("android.R$id")
ViewGroup = luajava.bindClass("android.view.ViewGroup")
View = luajava.bindClass("android.view.View")
LinearLayoutParams = luajava.bindClass("android.widget.LinearLayout$LayoutParams")
KeyEvent = luajava.bindClass("android.view.KeyEvent")
TranslateAnimation = luajava.bindClass("android.view.animation.TranslateAnimation")
ScrollView = luajava.bindClass("android.widget.ScrollView")
AnimationSet = luajava.bindClass("android.view.animation.AnimationSet")
LayoutAnimationController = luajava.bindClass("android.view.animation.LayoutAnimationController")
HapticFeedbackConstants = luajava.bindClass("android.view.HapticFeedbackConstants")
Validator = luajava.newInstance("com.offsetnull.bt.validator.Validator")
MenuItem = luajava.bindClass("android.view.MenuItem")
Validator_Number = Validator.VALIDATE_NUMBER
Validator_Not_Blank = Validator.VALIDATE_NOT_BLANK
Validator_Number_Not_Blank = bit.bor(Validator_Number,Validator_Not_Blank)
Validator_Number_Or_Blank = Validator.VALIDATE_NUMBER_OR_BLANK
lastLoadedSet = nil

Configuration = luajava.bindClass("android.content.res.Configuration");
InputType = luajava.bindClass("android.text.InputType")
TYPE_TEXT_FLAG_MULTI_LINE = InputType.TYPE_TEXT_FLAG_MULTI_LINE
TYPE_CLASS_NUMBER = InputType.TYPE_CLASS_NUMBER
ORIENTATION_LANDSCAPE = Configuration.ORIENTATION_LANDSCAPE
ORIENTATION_PORTRAIT = Configuration.ORIENTATION_PORTRAIT

DisplayMetrics = view:getContext():getResources():getDisplayMetrics()

suppress_editor = false

function loadButtons(args)


	debugString("Button Window loading buttons...")
	
	local tmp = marshal.decode(args)
	lastLoadedSet = tmp.name
	debugString("Button Window decompressed data, set name: "..lastLoadedSet)
	
	defaults = BUTTONSET_DATA:new(tmp.default)
	
	BUTTON_DATA.__index = defaults
	buttons = {}
	local set = tmp.set
	for i=1,#set do
		buttons[i] = BUTTON:new(set[i],density)
	end
	drawButtons()
	view:invalidate()
	
	debugString(string.format("Button Window loaded button set, %s successfully",lastLoadedSet))
end

function printTable(key,o)
	for i,v in pairs(o) do
		if(type(v)=="table") then
			printTable(key.."."..i,v)
		else 
			--Note(key.."."..i.."<==>"..v)
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
			if(gridsnap) then
				local tmpx = x % (gridXwidth/2)
				local tmpy = y % (gridYwidth/2)
				local gridx = math.floor(x/(gridXwidth/2))
				local gridy = math.floor(y/(gridYwidth/2))
				local wtmp = (moveBounds.right - moveBounds.left)/2
				local htmp = (moveBounds.bottom - moveBounds.top)/2
				totalDelta.x = totalDelta.x - (moveBounds.left - (gridx*(gridXwidth/2)-wtmp))
				totalDelta.y = totalDelta.y - (moveBounds.top - (gridy*(gridYwidth/2)-htmp))
				moveBounds:offsetTo(gridx*(gridXwidth/2)-wtmp,gridy*(gridYwidth/2)-htmp)
			else
				totalDelta.x = totalDelta.x + moveDelta.x
				totalDelta.y = totalDelta.y + moveDelta.y
				moveBounds:offset(moveDelta.x,moveDelta.y)
			end
			view:invalidate()
			return true
		else
			return true
		end
	end
	
	if(e:getAction() == MotionEvent.ACTION_UP) then
		if(touchMoving == false) then
			exitMoveMode()
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
				x1 = r.left
				y1 = r.top
				x2 = r.right
				y2 = r.bottom
				first = false
			else
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
	moveBounds:set(x1,y1,x2,y2)
	
	moveBounds:inset(-40,-40)
	
	local width = moveBounds.right - moveBounds.left
	local height = moveBounds.bottom - moveBounds.top
	moveBitmap = Bitmap:createBitmap(width,height,BitmapConfig.ARGB_8888)
	moveCanvas = luajava.newInstance("android.graphics.Canvas",moveBitmap)
	moveCanvas:drawARGB(0x88,0x88,0x88,0x88)
	view:setOnTouchListener(moveTouch_cb)
	moveCanvas:save()
	moveCanvas:translate(40,40)
	moveCanvas:translate(-1*x1,-1*y1)
	for i,b in pairs(buttons) do
		if(b.selected == true) then
			b:draw(1,moveCanvas)
		end
	end
	drawButtonsNoSelected()
	moveCanvas:restore()
	view:invalidate()
end

function exitMoveMode()
	moveCanvas = nil
	moveBitmap = nil
	local dx = totalDelta.x
	local dy = totalDelta.y
			
	totalDelta.x = 0
	totalDelta.y = 0
	for i,b in pairs(buttons) do
		if(b.selected == true) then
			local r = b.rect
			b.data.x = b.data.x + dx
			b.data.y = b.data.y + dy
			b.selected = false
			updateSelected(b,false)
			b:updateRect(statusoffset)
		end
	end
	drawButtons()
	view:setOnTouchListener(managerTouch_cb)
	
	view:invalidate()
end

touchStartX = 0
touchStartY = 0
managerTouch = {}
gridsnap = true
function managerTouch.onTouch(v,e)
	local x = e:getX()
	local y = e:getY()
	if(e:getAction() == MotionEvent.ACTION_DOWN) then
		--find if press was in a button
		ret,b,index = buttonTouched(x,y)
		if(ret) then
			touchStartX = x
			touchStartY = y
			fingerdown = true
			touchedbutton = b
			touchedindex = index
			b.selected = true
			updateSelected(b,true)
			view:invalidate()
			if(b.selected) then
				selectedtouchstart = true
			else
				selectedtouchstart = false
			end
			return true
		else
			--we are draggin now
			buttoncleared = false
			fingerdown = false
			for i,b in ipairs(buttons) do
				if b.selected then
					b.selected = false
					updateSelected(b,false)
					buttoncleared = true
				end
			end
			if buttoncleared then 
				view:invalidate() 
				
			end
			if(manage) then
				dragstart.x = x
				dragstart.y = y
				return true
			end
		end
	end

	if(e:getAction() == MotionEvent.ACTION_MOVE) then
	
		if(prevevent == 0) then
			prevevent = e:getEventTime()
		else
			now = e:getEventTime()
			local elapsed = now - prevevent
			if(elapsed > 10) then
				prevevent = now
			else
				return true --consume but dont process.
			end
		end


		if(not fingerdown and manage) then
			--we are drag moving now.
			dragcurrent.x = x
			dragcurrent.y = y
			--compute distance
			distance = math.sqrt(math.pow(dragcurrent.x-dragstart.x,2)+math.pow(dragcurrent.y-dragstart.y,2))
			if(distance < 10*density) then
				return true
			end
			dragmoving = true
			
			checkIntersects()
			view:invalidate()
			return true
		
		 end
		 
		 if(fingerdown and selectedtouchstart) then
		 	local diffx = math.abs(x - touchStartX)
		 	local diffy = math.abs(y - touchStartY)
		 	
		 	local dist = math.sqrt(diffx*diffx + diffy*diffy)
		 	if(dist < 10*density) then
		 		return true
		 	end
		 
		 	touchMoving = true
		 	moveCurrent.x = x
			moveCurrent.y = y
			if(moveCapOnce) then
				moveStart.x = x
				moveStart.y = y
				moveCapOnce = false
			end
			fingerdown = false
			selectedtouchstart = false
		 	enterMoveMode()
		 	return true
		 end
	end

	if(e:getAction() == MotionEvent.ACTION_UP) then
		if(dragmoving) then
			dragmoving = false
			view:invalidate()
			return true
		end
		if(manage and not fingerdown and not buttoncleared) then
			local modx = (math.floor(x/gridXwidth)*gridXwidth)+(gridXwidth/2)
			local mody = (math.floor(y/gridYwidth)*gridYwidth)+(gridYwidth/2)
			local butt = addButton(modx,mody-statusoffset)
			butt:draw(0,buttonCanvas)
			view:invalidate()
			return true
		end
		if(manage and fingerdown and touchedbutton.selected == true and selectedtouchstart) then
			showEditorSelection()
			touchedbutton={}
			fingerdown = false
			return true
		elseif(manage and fingerdown and touchedbutton.selected == false) then
			for i,b in ipairs(buttons) do
				if(b.selected) then
					b.selected = false
					updateSelected(b,false)
				end
			end
			touchedbutton.selected = true
			updateSelected(touchedbutton,true)
			view:invalidate()
			fingerdown = false
			touchedbutton = {}
			
		end

		touchedbutton = {}
		fingerdown = false
		return true
	end

	return false
end

managerTouch_cb = luajava.createProxy("android.view.View$OnTouchListener",managerTouch)

normalTouch = {}
normalTouchState = 0
function normalTouch.onTouch(v,e)
	local retvalue = false
	local x = e:getX()
	local y = e:getY()
	--debugPrint("normal touch, start")
	local action = e:getAction()
	if(action == ACTION_DOWN) then
		prevevent = 0
		ret,b,index = buttonTouched(x,y)
		if(ret) then
			if options.auto_launch == "true" then
				ScheduleCallback(100,"doEdit",1000)
			end
			fingerdown = true
			--touchedbutton.selected = false
			touchStartX = x
			touchStartY = y
			touchedbutton = b
			b.selected = true
			touchedindex = index
			--clearButton(b)
			normalTouchState = 1
			--clearButton(b)
			b:draw(normalTouchState,buttonCanvas)
			selectedtouchstart = true
			view:invalidate()
			return true
		else
			fingerdown = false;
			--debugPrint("action down, returning false")
			return false
		end			
	elseif(action == ACTION_MOVE) then
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
			if(elapsed > 5) then
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
					--clearButton(b)
					b:draw(normalTouchState,buttonCanvas)
					view:invalidate()
				end
			else
				CancelCallback(100)
				if(normalTouchState ~= 2) then
					normalTouchState = 2
					--clearButton(b)
					b:draw(normalTouchState,buttonCanvas)
					performHapticFlip()
					view:invalidate()
				end
				
			end
			
			--debugPrint("action move, moving button, returning true")
			
			return true
		else
			--debugPrint("reached end of normal touch handler, returning false")
			return false
		end
	elseif(action == ACTION_UP) then
		if(fingerdown) then
			CancelCallback(100)
			fingerdown = false
			selectedtouchstart = false
			touchedbutton.selected = false
			local r = touchedbutton.rect
			if(r:contains(x,y)) then
				--process primary touch
				if(buttonsCleared) then
					revertButtons()
					return true
				end
				mainwindow:jumpToStart()
				if(touchedbutton.data.switchTo ~= nil and touchedbutton.data.switchTo ~= "") then
					PluginXCallS("loadButtonSet",touchedbutton.data.switchTo)
					
					return true
				end
				performHapticPress()
				SendToServer(touchedbutton.data.command)
				--debugPrint("primary touch")
			else
				--process secondary touch
				if(touchedbutton.data.switchTo ~= nil and touchedbutton.data.switchTo ~= "") then
					PluginXCallS("loadButtonSet",touchedbutton.data.switchTo)
					
					return true
				end
				--debugPrint("secondary touch")
				mainwindow:jumpToStart()
				SendToServer(touchedbutton.data.flipCommand)
			end
			normalTouchState = 0
			--clearButton(touchedbutton)
			touchedbutton:draw(normalTouchState,buttonCanvas)
			view:invalidate()
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

function doEdit()
	--this is launched from the long press
	--Note("EDITING")
	if(suppress_editor) then return end
	
	manage = true
	performHapticEdit()
	enterManagerMode()
	showeditormenu = true
	PushMenuStack("onEditorBackPressed")
end
--this window is a full screen window, so we don't really need to concern ourselves with bounds and the such, but we do need to create a button class.
RectFClass = luajava.bindClass("android.graphics.RectF")
function updateSelected(b,sel)
	local p = b.paintOpts
	--clearButton(b)
	--local redrawScreen = false
	if(sel) then
		--p:setShadowLayer(1,0,0,Color.WHITE)
		b.selected = true
		b:draw(1,buttonCanvas)
	else
		--p:setShadowLayer(0,0,0,Color.WHITE)
		b.selected = false
		b:draw(0,buttonCanvas)
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
					--anySelected = true
				end
			else
				if(b.selected == true) then
					updateSelected(b,false)
					--redrawscreen = true
				end
			end
		end
		
		if(intersectMode == 1) then
			if(dragRect:contains(rect)) then
				if(b.selected == false) then
					updateSelected(b,true)
					--anySelected = true
				end
			else
				if(b.selected == true) then
					updateSelected(b,false)
					--redrawscreen = true
				end
			end	
		end
		
		
	end 
	
	--if(redrawscreen) then
	--	drawButtons()
	--end
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
	--Note("in oncreate, loading "..#buttons.." buttons.")	
	debugString("Button window in View.onCreate()")
	for i,b in ipairs(buttons) do
		updateRect(b)
	end
	paint:setARGB(0xAA,0x00,0x33,0xAA)
	--bounds = getBounds()
	--drawButtons()
	--addOptionCallback("buttonOptions","Lua Button Options",nil)
	AddOptionCallback("buttonList","Ex Button Sets",nil)
	view:bringToFront()
	
	--PluginXCallS("checkImport","blank")
end


managerLayer = nil
managerCanvas = nil


cpaint = luajava.new(PaintClass)
cpaint:setARGB(0x00,0x00,0x00,0x00)
cpaint:setXfermode(xferModeClear)

drawManagerLayer = true
function enterManagerMode()
	gridXwidth = defaults.width*density
	gridYwidth = defaults.height*density
	if(drawManagerLayer) then
		managerLayer = Bitmap:createBitmap(view:getWidth(),view:getHeight(),BitmapConfig.ARGB_8888)
		managerCanvas = luajava.newInstance("android.graphics.Canvas",managerLayer)
		--Note("drawingManagerLayer")
		drawManagerGrid()
	end

	--set up and add the back/options widget.
	--backWidget = makeBackWidget()
	--local parent = view:getParent()
	--parent:addView(backWidget)
	--touchedbutton = nil
		--paint:setShadowLayer(1,0,0,Color.WHITE)
	view:setOnTouchListener(managerTouch_cb)
	manage = true
	drawButtons()
	view:invalidate()
end

function exitManagerMode()
	if(drawManagerLayer ~= nil) then
		managerCanvas = nil
		managerLayer:recycle()
		managerLayer = nil
	end
	view:setOnTouchListener(normalTouch_cb)
	manage = false
	
	local parent = view:getParent()
	parent:removeView(backWidget)
	
	local tmp = {}
	for i,b in pairs(buttons) do
		tmp[i] = b.data
		if(b.selected) then b.selected = false end
	end
		
	PluginXCallS("saveButtons",serialize(tmp))
	drawButtons()
	view:invalidate()
end

function exitManagerModeNoSave()
	if(drawManagerLayer ~= nil) then
		managerCanvas = nil
		managerLayer:recycle()
		managerLayer = nil
	end
	view:setOnTouchListener(normalTouch_cb)
	manage = false
	
	local parent = view:getParent()
	parent:removeView(backWidget)
	
	local tmp = {}
	for i,b in pairs(buttons) do
		tmp[i] = b.data
		if(b.selected) then b.selected = false end
	end
	
	
	PluginXCallS("loadButtonSet",lastLoadedSet)
	view:invalidate()
end

checkchange = {}
function checkchange.onCheckedChanged(v,ischecked)
	--Note("starting check change")
	gridsnap = ischecked
	--drawManagerLayer = ischecked
	--if(manage == true and ischecked == true) then
	--	managerLayer = Bitmap:createBitmap(view:getWidth(),view:getHeight(),BitmapConfig.ARGB_8888)
	--	managerCanvas = luajava.newInstance("android.graphics.Canvas",managerLayer)
	--	debugPrint("drawingManagerLayer")
	--	drawManagerGrid()	
	--	invalidate()
	--else
	--	managerCanvas = nil
	--	managerLayer:recycle()
	--	managerLayer = nil
	--	invalidate()
	--end

end

function drawManagerGrid()
		local c = managerCanvas
		local width = view:getWidth()
		local height = view:getHeight()
		--Note("starting draw")
		c:drawRect(0,0,width,height,cpaint)
		--Note("canvas is not null")
		c:drawARGB(manageropacity,0x0A,0x0A,0x0A)
		--draw dashed lines.
		local times = width / gridXwidth
		for x=1,times do
			c:drawLine(gridXwidth*x,0,gridXwidth*x,height,dpaint)
		end
		
		times = height / gridYwidth
		for y=1,times do
			c:drawLine(0,gridYwidth*y,width,gridYwidth*y,dpaint)
		end

end

checkchange_cb = luajava.createProxy("android.widget.CompoundButton$OnCheckedChangeListener",checkchange)

gridXwidth = 67 * density
gridYwidth = 67 * density
seekerX = {}
function seekerX.onProgressChanged(v,prog,state)
	----Note("seekbarchanged:"..prog)
	local tmp = 32 + prog
	gridXwidth = tmp*density
	gridXSizeLabel:setText("Grid X Spacing: "..tmp)
	--managerCanvas:clearCanvas()
	drawManagerGrid()
	--drawButtons()
	view:invalidate()
end
seekerX_cb = luajava.createProxy("android.widget.SeekBar$OnSeekBarChangeListener",seekerX)

seekerY = {}
function seekerY.onProgressChanged(v,prog,state)
	--debugPrint("seekbarchanged:"..prog)
	local tmp = 32 + prog
	gridYwidth = tmp*density
	gridYSizeLabel:setText("Grid Y Spacing: "..tmp)
	--managerCanvas:clearCanvas()
	drawManagerGrid()
	--drawButtons()
	view:invalidate()
end
seekerY_cb = luajava.createProxy("android.widget.SeekBar$OnSeekBarChangeListener",seekerY)


opacitySeeker = {}
function opacitySeeker.onProgressChanged(v,prog,state)
	dpaint:setAlpha(prog)
	manageropacity = prog
	--Note("manageropacity now:"..manageropacity)
	local opacitypct = math.floor((manageropacity / 255)*100)
	gridOpacityLabel:setText("Grid Opacity: "..opacitypct.."%")
	drawManagerGrid()
	view:invalidate()
end

opacitySeeker_cb = luajava.createProxy("android.widget.SeekBar$OnSeekBarChangeListener",opacitySeeker)

radio = {}
function radio.onCheckedChanged(group,id)
	--debugPrint("radio buttons changed")
	if(id == 0) then
	 --intersect
	 --debugPrint("intersect selected")
	 intersectMode = id
	end
	if(id == 1) then
	 --contains
	 --debugPrint("contains selected")
	 intersectMode = id
	end
end

radio_cb = luajava.createProxy("android.widget.RadioGroup$OnCheckedChangeListener",radio)

intersectMode = 1


mContext = view:getContext()
layoutInflater = mContext:getSystemService(Context.LAYOUT_INFLATER_SERVICE)
	
buttonSetList = {}
table.insert(buttonSetList,"buttonset1")	
table.insert(buttonSetList,"buttonset2")
table.insert(buttonSetList,"buttonset3")	
table.insert(buttonSetList,"buttonset4")	
table.insert(buttonSetList,"buttonset5")

mButtonKeyListener = {}
function mButtonKeyListener.onKey(v,keyCode,event)
	if(keyCode == KeyEvent_KEYCODE_DPAD_UP or keyCode == KeyEvent.KEY_DPAD_DOWN) then
		return true
	else
		return false
	end
	--return false
end
mButtonKeyListener_cb = luajava.createProxy("android.view.View$OnKeyListener",mButtonKeyListener)

toolbarTabFocusChangeListener = {}
function toolbarTabFocusChangeListener.onFocusChange(v,hasFocus)
	if(hasFocus == true) then
		v:setFocusable(true)
		v:setFocusableInTouchMode(true)
	else
		v:setFocusable(false)
		v:setFocusableInTouchMode(false)
	end
end
toolbarTabFocusChangeListener_cb = luajava.createProxy("android.view.View$OnFocusChangeListener",toolbarTabFocusChangeListener)





lastSelectedIndex = -1

toolbarLength = nil

toolbarTabOpenListener = {}
function toolbarTabOpenListener.onClick(v)

	ToolbarTabOpenInAnimation = luajava.new(TranslateAnimation,toolbarLength,0,0,0)
	ToolbarTabOpenInAnimation:setDuration(800)
	ToolbarTabOpenOutAnimation = luajava.new(TranslateAnimation,0,toolbarLength,0,0)
	ToolbarTabOpenOutAnimation:setDuration(800)

	lastSelectedIndex = v:getId()
	
	parent = v:getParent()
	
	flipper = parent:getParent()
	--flipper = parent:findViewById(R_id.flipper)
	
	flipper:setInAnimation(ToolbarTabOpenInAnimation)
	flipper:setOutAnimation(ToolbarTabOpenOutAnimation)
	
	flipper:showNext()
	
	closetab = flipper:findViewById(R_id.toolbar_tab_close)
	closetab:requestFocus()
	
end
toolbarTabOpenListener_cb = luajava.createProxy("android.view.View$OnClickListener",toolbarTabOpenListener)

toolbarTabCloseListener = {}
function toolbarTabCloseListener.onClick(v)
	ToolbarTabCloseOutAnimation = luajava.new(TranslateAnimation,0,toolbarLength,0,0)
	ToolbarTabCloseOutAnimation:setDuration(800)
	ToolbarTabCloseInAnimation = luajava.new(TranslateAnimation,toolbarLength,0,0,0)
	ToolbarTabCloseInAnimation:setDuration(800)

	lastSelectedIndex = v:getId()
	
	parent = v:getParent()
	
	flipper = parent:getParent()
	--flipper = parent:findViewById(R_id.flipper)
	
	flipper:setInAnimation(ToolbarTabCloseInAnimation)
	flipper:setOutAnimation(ToolbarTabCloseOutAnimation)
	
	flipper:showNext()
	
	closetab = flipper:findViewById(R_id.toolbar_tab_close)
	tab = flipper:findViewById(R_id.toolbar_tab)
	tab:setFocusable(true)
	tab:requestFocus()
	
end
toolbarTabCloseListener_cb = luajava.createProxy("android.view.View$OnClickListener",toolbarTabCloseListener)

buttonListAdapter = {}

local TOOLBARHOLDER_ID = R_id.toolbarholder
local ROOT_ID = R_id.root
local EDITOR_SELECTION_LIST_ROW_ID = R_layout.editor_selection_list_row
local INFOTITLE_ID = R_id.infoTitle
local INFOEXTENDED = R_id.infoExtended		
local ICON_ID = R_id.icon
local VIEW_GONE = View.GONE

function buttonListAdapter.getView(pos,v,parent)
	local newview = nil
	if(v ~= nil) then
		newview = v
		
	else
		--Note("inflating view")
		newview = layoutInflater:inflate(R_layout.editor_selection_list_row,nil)
	
		local root = newview:findViewById(R_id.root)
		root:setOnClickListener(rowClicker_cb)
		
	end
	
	newview:setId(157*pos)
	
	local holder = newview:findViewById(R_id.toolbarholder)
	holder:setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS)
	
	if(holder:getChildCount() > 0) then
		holder:removeAllViews()
		lastSelectedIndex = -1
	end
	
	item = buttonSetList[tonumber(pos)+1]
	
	if(item ~= nil) then

		label = newview:findViewById(R_id.infoTitle)
		extra = newview:findViewById(R_id.infoExtended)
		
		icon = newview:findViewById(R_id.icon)
		icon:setVisibility(View.GONE)
		label:setText(item.name)
		extra:setText("Contains: "..item.count.." buttons")
		
		if(selectedIndex == (pos+1)) then
			label:setBackgroundColor(Color:argb(255,70,70,70))
			extra:setBackgroundColor(Color:argb(255,70,70,70))
		else
			label:setBackgroundColor(Color:argb(255,5,5,5))
			extra:setBackgroundColor(Color:argb(255,5,5,5))
		end
		--newview:setId(pos)
	end
	
	--if(newview ~= nil) then
	--	debugPrint("returning newview, it is not null")
	--end
	return newview
	
	
end
function buttonListAdapter.getCount()
	--debugPrint("getting button count:"..#buttonSetList)
	return #buttonSetList
end
function buttonListAdapter.areAllItemsEnabled()
	--debugPrint("areAllItemsEnabled()")
	return true
end
function buttonListAdapter.getItemViewType(pos)
	--debugPrint("getItemViewType()")
	return 0
end
function buttonListAdapter.isEnabled(pos)
	--debugPrint("isEnabled(pos)")
	return true
end
function buttonListAdapter.getItem(pos)
	--debugPrint("getItem(pos)")
	--return luajava.newInstance("java.lang.Object")
	return buttonSetList[pos+1]
end
function buttonListAdapter.isEmpty()
--debugPrint("isEmpty()")
	return false
end
function buttonListAdapter.hasStableIds()
--debugPrint("hasStableIds()")
	return true
end
function buttonListAdapter.getViewTypeCount()
--debugPrint("getViewTypeCount()")
	return 1
end
function buttonListAdapter.getItemId(pos)
	return 1
end

--function buttonListAdapter.
buttonListAdapter_cb = luajava.createProxy("android.widget.ListAdapter",buttonListAdapter)

listViewOnItemSelectedListener = {}
function listViewOnItemSelectedListener.onItemSelected(arg0,arg1,arg2,arg3)
	if(arg2 ~= lastSelectedIndex) then
		if(arg0:getFirstVisiblePosition() <= lastSelectedPosition and arg0:getLastVisiblePosition() >= lastSelectedPosition) then
			local parent = thetoolbar:getParent()
			if(parent ~= nil) then
				thetoolbar:startAnimation(animateOutNoTransition)
			end
		else
			local parent = thetoolbar:getParent()
			if(parent ~= nil) then
				parent:removeAllViews()
			end
		end
	end
end

function listViewOnItemSelectedListener.onNothingSelected(arg0)
	--don't care
end
listViewOnItemSelectedListener_cb = luajava.createProxy("android.widget.AdapterView$OnItemSelectedListener",listViewOnItemSelectedListener)


mListView = nil
function buttonList()
	
	--pull this list of button data.
	PluginXCallS("getButtonSetList","all")
end

newButtonSetButton = {}
function newButtonSetButton.onClick(v)
	--Note("new button pressed")
	mSelectorDialog:dismiss()
	local context = view:getContext()
	--make the new button set text input dialog and show it.
	local linear = luajava.new(LinearLayout,context)
	
	local llparams = luajava.new(LinearLayoutParams,350*density,LinearLayoutParams.WRAP_CONTENT)
	
	local fillparams = luajava.new(LinearLayoutParams,LinearLayoutParams.FILL_PARENT,LinearLayoutParams.WRAP_CONTENT,1)
	
	local buttonholder = luajava.new(LinearLayout,context)
	buttonholder:setLayoutParams(llparams)
	buttonholder:setOrientation(LinearLayout.HORIZONTAL)
	linear:setLayoutParams(llparams)
	linear:setOrientation(LinearLayout.VERTICAL)
	
	newButtonSetEdit = luajava.new(EditText,context)
	newButtonSetEdit:setHint("New Button Set Name")
	
	local done = luajava.new(Button,context)
	done:setText("Done")
	done:setLayoutParams(fillparams)
	done:setOnClickListener(newButtonSetDone_cb)
	
	local cancel = luajava.new(Button,context)
	cancel:setText("Cancel")
	cancel:setLayoutParams(fillparams)
	cancel:setOnClickListener(cancelButtonSetDone_cb)
	
	buttonholder:addView(done)
	buttonholder:addView(cancel)
	
	linear:addView(newButtonSetEdit)
	linear:addView(buttonholder)
	
	newButtonSetDialog = luajava.newInstance("com.offsetnull.bt.window.LuaDialog",context,linear,false,nil)
	newButtonSetDialog:show()
	
	
	
end
newButtonSetButton_cb = luajava.createProxy("android.view.View$OnClickListener",newButtonSetButton)

newButtonSetDone = {}
function newButtonSetDone.onClick(view)
	newButtonSetDialog:dismiss()
	local text = newButtonSetEdit:getText():toString()
	PluginXCallS("makeNewButtonSet",text)
end
newButtonSetDone_cb = luajava.createProxy("android.view.View$OnClickListener",newButtonSetDone)

newButtonSetCancel = {}
function newButtonSetCancel.onClick(view)
	newButtonSetDialog:dismiss()
end
newButtonSetCancel_cb = luajava.createProxy("android.view.View$OnClickListener",newButtonSetCancel)

doneButtonListener = {}
function doneButtonListener.onClick(v)
	mSelectorDialog:dismiss()
end
doneButtonListener_cb = luajava.createProxy("android.view.View$OnClickListener",doneButtonListener)

mSelectorDialog = nil

function showButtonList(data)
	--Note(data)
	setdata = loadstring(data)()

	--makeToolbar()
	lastSelectedIndex = -1
	if(thetoolbar:getParent() ~= nil) then
		thetoolbar:getParent():removeAllViews()
	end
	--sort the list.
	--table.sort(setdata)
	--Note("got "..#setdata.." sets.")
	for k in pairs(buttonSetList) do
		buttonSetList[k] = nil
	end
	
	local counter = 1
	selectedIndex = -1
	for i,k in pairs(setdata) do
		tmp = {}
		tmp.name = i
		tmp.count = k
		table.insert(buttonSetList,tmp)

	end
	
	local sorter = function(a,b) if(a.name < b.name) then return true end return false end
	table.sort(buttonSetList,sorter)
	
	for i,b in ipairs(buttonSetList) do
		if(b.name == lastLoadedSet) then
		selectedIndex = counter
		end
		counter = counter + 1
	end
	--Note("selected Item index is"..selectedIndex.." lastloadedset is:"..lastLoadedSet)
	
	--if(mSelectorDialog == nil) then
		fakeRelativeLayout = luajava.newInstance("android.widget.RelativeLayout",mContext)
		layout = layoutInflater:inflate(R_layout.editor_selection_dialog,fakeRelativeLayout)
		
		mListView = layout:findViewById(R_id.list)
	
		--mListView = luajava.newInstance("android.widget.ListView",mContext)
		mListView:setScrollbarFadingEnabled(false)
		mListView:setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS)
		mListView:setOnItemSelectedListener(listViewOnItemSelectedListener_cb)
		--mListView:setOnFocusChangeListener(listViewFocusFixerListener_cb)
		mListView:setSelector(R_drawable.transparent)
		mListView:setAdapter(buttonListAdapter_cb)
		--buttonListAdapter_cb:notifyDataSetInvalidated()
		emptyView = layout:findViewById(R_id.empty)
		mListView:setEmptyView(emptyView)
		mListView:setSelectionFromTop(selectedIndex -1,10*density)
		
		title = layout:findViewById(R_id.titlebar)
		title:setText("SELECT BUTTON SET")
		
		newbutton = layout:findViewById(R_id.add)
		newbutton:setText("New Set")
		newbutton:setOnClickListener(newButtonSetButton_cb)
		
		donebutton = layout:findViewById(R_id.done)
		donebutton:setOnClickListener(doneButtonListener_cb)
		mSelectorDialog = luajava.newInstance("com.offsetnull.bt.window.LuaDialog",mContext,layout,false,nil)
	
	--end
	mSelectorDialog:show()
end


function buttonOptions()
	ctex = view:getContext()

	ll = luajava.newInstance("android.widget.LinearLayout",ctex)
	ll:setOrientation(1)
	llparams = luajava.new(LinearLayoutParams,350*density,LinearLayoutParams.WRAP_CONTENT,1)
	ll:setLayoutParams(llparams)
	--ll:setGravity(Gravity.CENTER)
	
	local scroller = luajava.new(ScrollView,ctex)
	scroller:setLayoutParams(llparams)
	
	fillparams = luajava.new(LinearLayoutParams,LinearLayoutParams.FILL_PARENT,LinearLayoutParams.WRAP_CONTENT,1)
	wrapparams = luajava.new(LinearLayoutParams,LinearLayoutParams.WRAP_CONTENT,LinearLayoutParams.WRAP_CONTENT,1)
	wrapparams:setMargins(0,15,0,0)
	wrapparamsNoWeight = luajava.new(LinearLayoutParams,LinearLayoutParams.WRAP_CONTENT,LinearLayoutParams.WRAP_CONTENT)
	
	--lp = luajava.newInstance("android.view.ViewGroup$LayoutParams",-1,-2)

	cb = luajava.newInstance("android.widget.CheckBox",ctex)
	cb:setChecked(gridsnap)
	cb:setText("Snap To Grid")
	cb:setTextSize(textSizeSmall)
	cb:setOnCheckedChangeListener(checkchange_cb)
	cb:setLayoutParams(fillparams)
	
	local subrow = luajava.new(LinearLayout,ctex)
	subrow:setLayoutParams(fillparams)
	
	--gridSizeRow = luajava.newInstance("android.widget.LinearLayout",ctex)
	--gridSizeRow:setOrientation(1)
	
	--Note("seekbar creation")
	sbX = luajava.newInstance("android.widget.SeekBar",ctex)
	sbX:setOnSeekBarChangeListener(seekerX_cb)
	sbX:setLayoutParams(fillparams)
	gridXSizeLabel = luajava.newInstance("android.widget.TextView",ctex)
	gridXSizeLabel:setLayoutParams(wrapparams)
	gridXSizeLabel:setTextSize(textSizeSmall)
	gridXSizeLabel:setText("Grid X Spacing: "..gridXwidth)
	sbX:setProgress((gridXwidth/density)-32)
	
	sbY = luajava.newInstance("android.widget.SeekBar",ctex)
	sbY:setOnSeekBarChangeListener(seekerY_cb)
	sbY:setLayoutParams(fillparams)
	gridYSizeLabel = luajava.newInstance("android.widget.TextView",ctex)
	gridYSizeLabel:setLayoutParams(wrapparams)
	gridYSizeLabel:setTextSize(textSizeSmall)
	gridYSizeLabel:setText("Grid Y Spacing: "..gridYwidth)
	sbY:setProgress((gridYwidth/density)-32)
	
	opacity = luajava.newInstance("android.widget.SeekBar",ctex)
	
	opacity:setLayoutParams(fillparams)
	opacity:setMax(255)
	----Note("settings opacity slider to:"..manageropacity)
	opacity:setProgress(manageropacity)
	opacity:setOnSeekBarChangeListener(opacitySeeker_cb)
	
	gridOpacityLabel = luajava.newInstance("android.widget.TextView",ctex)
	gridOpacityLabel:setLayoutParams(wrapparams)
	gridOpacityLabel:setTextSize(textSizeSmall)
	gridOpacityLabel:setText("Grid Opacity: "..manageropacity)
	
	rg_static = luajava.bindClass("android.widget.RadioGroup")
	
	--local subrow2 = luajava.new(LinearLayout,ctex)
	--subrow2:setLayoutParams(fillparams)
	
	rg = luajava.newInstance("android.widget.RadioGroup",ctex)
	rgLayoutParams = luajava.newInstance("android.widget.LinearLayout$LayoutParams",-2,-2)
	rg:setLayoutParams(rgLayoutParams)
	rg:setOnCheckedChangeListener(radio_cb)
	rg:setOrientation(0)
	
	contain = luajava.newInstance("android.widget.RadioButton",ctex)
	contain:setText("Contains")
	contain:setTextSize(textSizeSmall)
	contain:setId(1)
	
	intersect = luajava.newInstance("android.widget.RadioButton",ctex)
	intersect:setText("Intersect")
	intersect:setTextSize(textSizeSmall)
	intersect:setId(0)
	
	
	
	rg_lp = luajava.bindClass("android.widget.RadioGroup$LayoutParams")
	
	rg_lp_gen = luajava.new(rg_lp,fillparams)
	rg_lp_gen2 = luajava.new(rg_lp,fillparams)
	rg_lp_gen2:setMargins(25,0,0,0)
	
	rg:addView(intersect,0,rg_lp_gen)
	rg:addView(contain,1,rg_lp_gen2)
	rg:check(intersectMode)
	
	selectionTextLabel = luajava.newInstance("android.widget.TextView",ctex)
	selectionTextLabel:setLayoutParams(wrapparams)
	selectionTextLabel:setTextSize(textSizeSmall)
	selectionTextLabel:setText("Drag rectangle selection test:")
	
	--subrow2:addView(selectionTextLabel)
	--subrow2:addView(rg)
	
	setSettingsButton = luajava.new(Button,ctex)
	setSettingsButton:setLayoutParams(fillparams)
	setSettingsButton:setText("Edit Defaults")
	setSettingsButton:setOnClickListener(setSettingsButton_cb)
	--Note("adding views")
	
	subrow:addView(cb)
	subrow:addView(setSettingsButton)
	ll:addView(subrow)
	ll:addView(gridXSizeLabel)
	ll:addView(sbX)
	ll:addView(gridYSizeLabel)
	ll:addView(sbY)
	ll:addView(gridOpacityLabel)
	ll:addView(opacity)
	ll:addView(selectionTextLabel)
	ll:addView(rg)
	
	boptHolder = luajava.new(LinearLayout,ctex)
	boptHolder:setLayoutParams(fillparams)
	boptHolder:setGravity(Gravity.CENTER)
	boptDoneButton = luajava.newInstance("android.widget.Button",ctex)
	boptDoneButton:setText("Done")
	boptDoneButton:setLayoutParams(wrapparamsNoWeight)
	boptDoneButton:setOnClickListener(buttonOptionDone_cb)
	boptHolder:addView(boptDoneButton)
	
	ll:addView(boptHolder)
	
	scroller:addView(ll)
	--ll:addView(rg)
	
	--ll:addView(setSettingsButton)
	--ll:addView(subrow)
	--set up the show editor settings button.
	--Note("builder alert creation")
	--builder = luajava.newInstance("android.app.AlertDialog$Builder",ctex)
	alert = luajava.newInstance("com.offsetnull.bt.window.LuaDialog",ctex,scroller,false,nil)
	alert:show()
	--local hiddenview = luajava.new(TextView,view:getContext())
	--hiddenview:setVisibility(View.GONE)
	--builder:setCustomTitle(hiddenview)
	--builder:setTitle("")
	--builder:setMessage("")
	--builder:setView(ll)
	--alert = builder:create()
	--local titleview = alert:findViewById(android_R_id.title)
	--titleview:setVisibility(View.GONE)
	--alert:show()
end

buttonOptionDone = {}
function buttonOptionDone.onClick(view)
	alert:dismiss()
end
buttonOptionDone_cb = luajava.createProxy("android.view.View$OnClickListener",buttonOptionDone)

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
manageropacity = 255
dpaint:setARGB(manageropacity,0xFF,0x00,0x00)
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
	for i=1,#buttons do
	--for i,b in pairs(buttons) do
		local b = buttons[i]
		----Note("DRAWING BUTTON"..i)
		if(b.selected) then
			b:draw(1,canvas)
		else
			b:draw(0,canvas)
		end
		--counter = counter + 1
	end
	--Note("DRAWING "..counter.." BUTTONS")
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
	p:setXfermode(xferModeClear)
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
ACTION_MOVE = MotionEvent.ACTION_MOVE
ACTION_DOWN = MotionEvent.ACTION_DOWN
ACTION_UP = MotionEvent.ACTION_UP
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
	--Note("on touch event started")
	if(e:getAction() == MotionEvent.ACTION_DOWN) then
		--find if press was in a button
		ret,b,index = buttonTouched(x,y)
		if(ret) then
			fingerdown = true
			touchedbutton = b
			touchedindex = index
			--Note(string.format("Button touched @ x:%d y:%d, buttoncenter x:%d,y:%d",x,y,touchedbutton.x,touchedbutton.y))
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
				--Note("processing move event")
				prevevent = now
			else
				return true --consume but dont process.
			end
		end




		--Note("ACTION MOVING"..touchedbutton.x)

		if(not fingerdown and manage) then
			--we are drag moving now.
			dragmoving = true
			dragcurrent.x = x
			dragcurrent.y = y
			--drawDragBox()
			checkIntersects()
			view:invalidate()
			return true
		end

		if(fingerdown and manage) then
			local modx = (math.floor(e:getX()/gridXwidth)*gridXwidth)+(gridXwidth/2)
			local mody = (math.floor(e:getY()/gridYwidth)*gridYwidth)+(gridYwidth/2)
			
			touchedbutton.x = modx
			touchedbutton.y = mody
			updateRect(touchedbutton)
			--drawButton(touchedbutton)
			drawButtons()
			view:invalidate()
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
			view:invalidate()
			return true
		end
		if(manage and not fingerdown) then
			--lawl, make new button
			local modx = (math.floor(x/gridXwidth)*gridXwidth)+(gridXwidth/2)
			local mody = (math.floor(y/gridYwidth)*gridYwidth)+(gridYwidth/2)
			--Note("new button at: "..modx..","..mody)
			local butt = addButton(modx,mody)
			--butt.width = gridwidth
			--butt.height = gridwidth
			--canvas:drawRoundRect(butt.rect,5,5,paint)
			butt:draw(0,buttonCanvas)
			view:invalidate()
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
	--Note("Editor: "..Array:get(editorItems,which).." selected.")
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
		view:invalidate()
	end
	if(which == 0) then
		enterMoveMode()
	end
	if(which == 1) then
		showEditorDialog()
	end
end
editorListener_cb = luajava.createProxy("android.content.DialogInterface$OnClickListener",editorListener)
numediting = 0
lastselectedinex = -1
function showEditorSelection()
	local count = 0
	for i,b in ipairs(buttons) do
		if(b.selected == true) then
			count = count + 1
			lastselectedindex = i
		end
	end
	numediting = count
	local build = luajava.newInstance("android.app.AlertDialog$Builder",view:getContext())
	
	build:setItems(editorItems,editorListener_cb)
	build:setTitle(count.." buttons selected.")
	alert = build:create()
	alert:show()

end

counter = 0

function addButton(pX,pY) 
	local newb = BUTTON:new({x=pX,y=pY,label="newb"},density)
	--newb.x = x
	--newb.y = y
	newb.data.width = (gridXwidth-5)/density
	newb.data.height = (gridYwidth-5)/density
	newb.data.label = "newb"..counter
	counter = counter+1
	--newb.rect = luajava.newInstance("android.graphics.RectF")
	--newb.paintOpts = luajava.new(PaintClass,paint)
	--newb.selected = false
	newb:updateRect(statusoffset)
	table.insert(buttons,newb)
	return newb
end

function buttonTouched(x,y)
	for i=1,#buttons do
	--for i,b in pairs(buttons) do
		local b = buttons[i]
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
	top = b.y - (b.height/2) + statusoffset
	bottom = b.y + (b.height/2) + statusoffset
	tmp = b.rect
	tmp:set(left,top,right,bottom) 
end

buttonLayer = nil
buttonCanvas = nil
draw = false

statusHidden = IsStatusBarHidden()
if(statusHidden) then
	statusoffset = GetStatusBarHeight()
end
Integer = luajava.bindClass("java.lang.Integer")
function OnSizeChanged(w,h,oldw,oldh)
	debugString("Button Window starting View.OnSizeChanged()")
	if(w == 0 and h == 0) then
		draw = false
		return
	end
	
	hiddenNow = IsStatusBarHidden()
	if(statusHidden and not hiddenNow) then
		statusoffset = 0
	end
	
	if(not statusHidden and hiddenNow) then 
		statusoffset = GetStatusBarHeight()
	end
	--Note("status offset is: "..statusoffset)
	
	
	if(statusHidden ~= statusNow) then
		for i=1,#buttons do
			local b = buttons[i]
			b:updateRect(statusoffset)
		end
	
	end
	
	statusHidden = hiddenNow
	
	--Note("status offset is: "..statusoffset)
	local ccl = luajava.bindClass("android.graphics.Color")
	local colord = ccl:argb(0x88,0x00,0x00,0xFF)
	
	--Note("DebugString: "..string.format("%d,%s",colord,Integer:toHexString(colord)))
	
	--Note("Window Sized Changed:"..w.."x"..h)
	
	
	if(buttonLayer) then
		--Note("freeing button layer")
		buttonCanvas = nil
		buttonLayer:recycle()
		buttonLayer = nil
		
	end
	
	if(selectedLayer) then
		selectedCanvas = nil
		selectedLayer:recycle()
		selectedLayer = nil
		
	end
	
	collectgarbage("collect")
	
	buttonLayer = Bitmap:createBitmap(view:getWidth(),view:getHeight(),BitmapConfig.ARGB_8888)
	buttonCanvas = luajava.newInstance("android.graphics.Canvas",buttonLayer)
	
	selectedLayer = Bitmap:createBitmap(view:getWidth(),view:getHeight(),BitmapConfig.ARGB_8888)
	selectedCanvas = luajava.newInstance("android.graphics.Canvas",selectedLayer)
	--managerLayer = Bitmap.create(w,h,BitmapConfig.ARGB_8888)
	

	
	revertButtonData.x = w - revertButtonData.width*2
	revertButtonData.y = h - revertButtonData.height*2
	revertButton:updateRect(statusoffset)
	
	drawButtons()
	draw = true
	
	debugString("Button Window ending View.onSizeChanged()")
end

dragDashPaint = luajava.new(PaintClass)
dragDashPaint:setARGB(0xFF,0x77,0x00,0x88)
dragDashPaint:setPathEffect(dash)
dragDashPaint:setStyle(Style.STROKE)
dragDashPaint:setStrokeWidth(7)

dragBoxPaint = luajava.new(PaintClass)
dragBoxPaint:setARGB(0x33,0x77,0x00,0x33)

function OnDraw(canvas)
	--canvas:save()
	--canvas:translate(0,statusoffset)

	if(manage and drawManagerLayer) then
		canvas:drawBitmap(managerLayer,0,0,nil)
	end
	
	if(draw) then
		canvas:drawBitmap(buttonLayer,0,0,nil)
	end
	
	if(dragmoving) then
		----Note("I SHOULD BE DRAG MOVING")
		startx = 0
		starty = 0
		endx = 0
		endy = 0
		
		if(dragstart.x < dragcurrent.x) then
			startx = dragstart.x	
			endx = dragcurrent.x	
		else
			startx = dragcurrent.x
			endx = dragstart.x
		end
		
		if(dragstart.y < dragcurrent.y) then
			starty = dragstart.y
			endy = dragcurrent.y		
		else
			starty = dragcurrent.y
			endy = dragstart.y
		end
		
		canvas:drawRect(startx,starty,endx,endy,dragBoxPaint)
		canvas:drawRect(startx,starty,endx,endy,dragDashPaint)
		
	end
	
	if(moveBitmap ~= nil) then
		canvas:drawBitmap(moveBitmap,moveBounds.left,moveBounds.top,nil)
	end
	
	--canvas:restore()
	
end



function OnDestroy()
	--Note("destroying button window")
	debugString("Button Window in View.OnDestroy()")
	if(managerLayer ~= nil) then
		managerLayer:recycle()
		managerLayer = nil
		managerCanvas = nil
	end
	--Note("freeing button layer")
	if(buttonLayer ~= nil) then
		--Note("recycle")
		buttonLayer:recycle()
		--Note("layer to nil")
		buttonLayer = nil
		--Note("canvas to nil")
		buttonCanvas = nil
	end
	--Note("finished destroying window")
end

TabHost = luajava.bindClass("android.widget.TabHost")
TabWidget = luajava.bindClass("android.widget.TabWidget")
RelativeLayout = luajava.bindClass("android.widget.RelativeLayout")
RelativeLayoutParams = luajava.bindClass("android.widget.RelativeLayout$LayoutParams")
android_R_id = luajava.bindClass("android.R$id")
TextView = luajava.bindClass("android.widget.TextView")
Gravity = luajava.bindClass("android.view.Gravity")
FrameLayout = luajava.bindClass("android.widget.FrameLayout")
LinearLayout = luajava.bindClass("android.widget.LinearLayout")
LinearLayoutParams = luajava.bindClass("android.widget.LinearLayout$LayoutParams")
Button = luajava.bindClass("android.widget.Button")
EditText = luajava.bindClass("android.widget.EditText")
View = luajava.bindClass("android.view.View")
Color = luajava.bindClass("android.graphics.Color")

GRAVITY_CENTER = Gravity.CENTER
FILL_PARENT = LinearLayoutParams.FILL_PARENT
WRAP_CONTENT = LinearLayoutParams.WRAP_CONTENT

--dialogView = nil

--cancel button callback
editorDialog = nil
editorCancel = {}
function editorCancel.onClick(v)
	editorDialog:dismiss()
	editorDialog = nil
end
editorCancel_cb = luajava.createProxy("android.view.View$OnClickListener",editorCancel)

editorDone = {}
function editorDone.onClick(v)
	--apply the settings out.
	
	local str = Validator:validate()
	if(str ~= nil) then
		Validator:showMessage(view:getContext(),str)
		return
	end
	
	labeltmp = clickLabelEdit:getText()
	label = labeltmp:toString()
	cmdtmp = clickCmdEdit:getText()
	cmd = cmdtmp:toString()
	fliplabeltmp = flipLabelEdit:getText()
	fliplabel = fliplabeltmp:toString()
	flipcmdtmp = flipCmdEdit:getText()
	flipcmd = flipcmdtmp:toString()
	nametmp = buttonNameEdit:getText()
	name = nametmp:toString()
	
	xcoordtmp = xcoordEdit:getText()
	xcoord = tonumber(xcoordtmp:toString())
	ycoordtmp = ycoordEdit:getText()
	ycoord = tonumber(ycoordtmp:toString())
	labelsizetmp = labelSizeEdit:getText()
	labelsize = tonumber(labelsizetmp:toString())
	----Note(
	heighttmp = heightEdit:getText()
	
	height = tonumber(heighttmp:toString())
	--Note("height read from editor"..height)
	widthtmp = widthEdit:getText()
	width = tonumber(widthtmp:toString())
	
	if(numediting == 1) then
		tmp = buttons[lastselectedindex]

		
		--Note("EDITING SINGLE BUTTON BEFORE BUTTON:"..tmp.data.height)
		--printTable("button",tmp)
		
		
		tmp.data.x = xcoord
		tmp.data.y = ycoord
		tmp.data.height = height
		tmp.data.width = width
		tmp.data.labelSize = labelsize
		
		tmp.data.primaryColor = theNormalColor
		tmp.data.flipColor = theFlipColor
		tmp.data.selectedColor = thePressedColor
		tmp.data.labelColor = theNormalLabelColor
		tmp.data.flipLabelColor = theFlipLabelColor
		
		tmp.data.command = cmd
		tmp.data.label = label
		tmp.data.flipLabel = fliplabel
		tmp.data.flipCommand = flipcmd
		
		tmp:updateRect(statusoffset)
		--Note("EDITING SINGLE BUTTON AFTER BUTTON:"..tmp.data.height)
		--printTable("edited",tmp)
		
	elseif(numediting > 1) then
		for i,b in ipairs(buttons) do
			if(b.selected == true) then
				--do the settings update for relevent data
				if(width ~= nil and width ~= editorValues.width) then
					b.data.width = width
				end
				
				if(height ~= nil and height ~= editorValues.height) then
					b.data.height = height
				end
				
				if(xcoord ~= nil and xcoord ~= editorValues.x) then
					b.data.x = xcoord
				end
				
				if(ycoord ~= nil and ycoord ~= editorValues.y) then
					b.data.y = ycoord
				end
				
				if(labelsize ~= nil and labelsize ~= editorValues.labelSize) then
					b.data.labelSize = labelsize
				end
				
				if(theNormalColor ~= editorValues.primaryColor) then
					b.data.primaryColor = theNormalColor
				end
				
				if(thePressedColor ~= editorValues.selectedColor) then
					b.data.selectedColor = thePressedColor
				end
				
				if(theFlipColor ~= editorValues.flipColor) then
					b.data.flipColor = theFlipColor
				end
				
				if(theNormalLabelColor ~= editorValues.labelColor) then
					b.data.labelColor = theNormalLabelColor
				end
				
				if(theFlipLabelColor ~= editorValues.flipLabelColor) then
					b.data.flipLabelColor = theFlipLabelColor
				end
				
				b:updateRect(statusoffset)
			end
		end
	end
	
	
	--local tmp = {}
	--for i,b in pairs(buttons) do
	--	tmp[i] = b.data
	--end
		
	--PluginXCallS("saveButtons",serialize(tmp))
	
	editorDialog:dismiss()
	editorDialog = nil
	drawButtons()
	view:invalidate()
end
editorDone_cb = luajava.createProxy("android.view.View$OnClickListener",editorDone)

clickLabelEdit = nil
clickCmdEdit = nil

flipLabelEdit = nil
flipCmdEdit = nil

buttonNameEdit = nil

normalColor = nil
flipColor = nil
pressedColor = nil
normalLabelColor = nil
flipLabelColor = nil

xcoordEdit = nil
ycoordEdit = nil
widthEdit = nil
heightEdit = nil
labelSizeEdit = nil

editorValues = {}

textSize = 12
textSizeSmall = 8
screenlayout = view:getContext():getResources():getConfiguration().screenLayout
local test = bit.band(screenlayout,Configuration.SCREENLAYOUT_SIZE_MASK)

local function foo()
	--Note(test)
	--Note(Configuration.SCREENLAYOUT_SIZE_XLARGE)
	if(test == Configuration.SCREENLAYOUT_SIZE_XLARGE) then
		textSize = 26
		textSizeSmall = 17
	end
end
pcall(foo)

function getDialogDimensions()
	local context = view:getContext()
	local wm = context:getSystemService(Context.WINDOW_SERVICE)
	local display = wm:getDefaultDisplay()
	local displayWidth = display:getWidth()
	local displayHeight = display:getHeight()
	local use = displayWidth
	local orientation = context:getResources():getConfiguration().orientation
	
	if(displayHeight < displayWidth) then
		use = displayHeight
	end
	
	local dpi_bucket = use / density
	
	local height_param = LinearLayoutParams.WRAP_CONTENT
	local width_param = 450*density
	
	if(orientation == ORIENTATION_LANDSCAPE) then
		--landscape
		if(dpi_bucket >= 600) then
			height_param = 300*density
		end
		
		if(width_param > displayWidth) then
			width_param = displayHeight
		end
	else
		--portrait
				--landscape
		if(dpi_bucket >= 600) then
			height_param = 300*density
		end
		
		if(width_param > displayWidth) then
			width_param = displayWidth-(5*density)
		end
	end
	
	return width_param,height_param
end

function showEditorDialog()
	--make the parent view.
	--local button = nil
	editorValues = {}
	--if(dialogView == nil) then
	if(numediting == 1) then
		button = buttons[lastselectedindex]
		editorValues.label = button.data.label
		editorValues.command = button.data.command
		editorValues.flipLabel = button.data.flipLabel
		editorValues.flipCommand = button.data.flipCommand
		editorValues.name = button.data.name
		--editorValues.name = "OMGANYTHING"
		if(not editorValues.name) then editorValues.name = "" end
		editorValues.primaryColor = button.data.primaryColor
		editorValues.labelColor = button.data.labelColor
		editorValues.selectedColor = button.data.selectedColor
		editorValues.flipColor = button.data.flipColor
		editorValues.flipLabelColor = button.data.flipLabelColor
		editorValues.height = button.data.height
		editorValues.switchTo = button.data.switchTo
		editorValues.width = button.data.width
		
		editorValues.labelSize = button.data.labelSize
		editorValues.x = button.data.x
		editorValues.y = button.data.y
		--Note("single editor loading:"..editorValues.x)
		--Note("single editor loading:"..editorValues.y)
	else 
		for i,b in pairs(buttons) do
			if(b.selected == true) then
				--start comparing values
				if(editorValues.primaryColor ~= b.data.primaryColor) then
					editorValues.primaryColor = b.data.primaryColor
				end
			
				if(editorValues.labelColor ~= b.data.labelColor) then
					editorValues.labelColor = b.data.labelColor
				end
				
				if(editorValues.selectedColor ~= b.data.selectedColor) then
					editorValues.selectedColor = b.data.selectedColor
				end
				
				if(editorValues.flipColor ~= b.data.flipColor) then
					editorValues.flipColor = b.data.flipColor
				end
				
				if(editorValues.flipLabelColor ~= b.data.flipLabelColor) then
					editorValues.flipLabelColor = b.data.flipLabelColor
				end
				
				if(editorValues.labelSize == nil) then
					editorValues.labelSize = tonumber(b.data.labelSize)
				elseif(editorValues.labelSize ~= tonumber(b.data.labelSize)) then
					editorValues.labelSize = "MULTI"
				end
				
				if(editorValues.height == nil) then
					editorValues.height = tonumber(b.data.height)
				elseif(editorValues.height ~= tonumber(b.data.height)) then
					editorValues.height = "MULTI"
				end
				
				if(editorValues.width == nil) then
					editorValues.width = tonumber(b.data.width)
					--Note("editorValue set to "..b.data.width)
				elseif(editorValues.width ~= tonumber(b.data.width)) then
					editorValues.width = "MULTI"
					--Note("editorValue set to multi because "..b.data.width)
				end
				
				if(editorValues.x == nil) then
					editorValues.x = tonumber(b.data.x)
				elseif(editorValues.x ~= tonumber(b.data.x)) then
					editorValues.x = "MULTI"
				end
				
				if(editorValues.y == nil) then
					editorValues.y = tonumber(b.data.y)
				elseif(editorValues.y ~= tonumber(b.data.y)) then
					editorValues.y = "MULTI"
				end
			end
		end
	end
	
	local context = view:getContext()

	local width_param,height_param = getDialogDimensions()
	
	top = luajava.new(LinearLayout,context)
	topparams = luajava.new(LinearLayoutParams,width_param,height_param)
	

	
	top:setLayoutParams(topparams)
	--top:setOrientation(LinearLayout.VERTICAL)
	titletext = luajava.new(TextView,context)
	top:setOrientation(LinearLayout.VERTICAL)
	titletextParams = luajava.new(LinearLayoutParams,FILL_PARENT,WRAP_CONTENT)
	--titletextParams:addRule(RelativeLayout.ALIGN_PARENT_TOP)
	
	titletext:setLayoutParams(titletextParams)
	titletext:setTextSize(TypedValue:applyDimension(TypedValue.COMPLEX_UNIT_SP,textSize,DisplayMetrics))
	titletext:setText("EDIT BUTTON")
	titletext:setGravity(GRAVITY_CENTER)
	titletext:setTextColor(Color:argb(255,0x33,0x33,0x33))
	titletext:setBackgroundColor(Color:argb(255,0x99,0x99,0x99))
	titletext:setId(1)
	top:addView(titletext)

	--make the new tabhost.	
	params = luajava.new(LinearLayoutParams,WRAP_CONTENT,WRAP_CONTENT)
	fillparams = luajava.new(LinearLayoutParams,FILL_PARENT,WRAP_CONTENT,1)
	contentparams = luajava.new(LinearLayoutParams,FILL_PARENT,WRAP_CONTENT)
	--fillparams:setGravity(Gravity.FILL_HORIZONTAL)
	--hostparams = luajava.new(LinearLayoutParams,FILL_PARENT,FILL_PARENT)
	
	hostparams = luajava.new(LinearLayoutParams,FILL_PARENT,WRAP_CONTENT,2)
	host = luajava.new(TabHost,context)
	--hostparams:addRule(RelativeLayout.BELOW,1)
	--hostparams:addRule(RelativeLayout.ABOVE,2)
	host:setId(3)
	host:setLayoutParams(hostparams)
	
	
	
	--make the done and cancel buttons.
	--have to stuff them in linearlayout.
	finishHolderParams = luajava.new(LinearLayoutParams,FILL_PARENT,WRAP_CONTENT)
	--finishHolderParams:addRule(RelativeLayout.BELOW,3)
	finishHolder = luajava.new(LinearLayout,context)
	finishHolder:setLayoutParams(finishHolderParams)
	finishHolder:setId(2)
	
	--finishbuttonParams = luajava.new(RelativeLayoutParams,RLayoutParams.FILL_PARENT,WRAP_CONTENT)
	done = luajava.new(Button,context)
	done:setLayoutParams(fillparams)
	done:setText("Done")
	done:setOnClickListener(editorDone_cb)
	
	cancel = luajava.new(Button,context)
	cancel:setLayoutParams(fillparams)
	cancel:setText("Cancel")
	cancel:setOnClickListener(editorCancel_cb)
	finishHolder:addView(done)
	finishHolder:addView(cancel)
	top:addView(host)
	top:addView(finishHolder)
	
	
	holder = luajava.new(LinearLayout,context)
	holder:setOrientation(LinearLayout.VERTICAL)
	holder:setLayoutParams(fillparams)
	
	widget = luajava.new(TabWidget,context)
	widget:setId(android_R_id.tabs)
	widget:setLayoutParams(contentparams)
	widget:setWeightSum(3)
	
	content = luajava.new(FrameLayout,context)
	content:setId(android_R_id.tabcontent)
	content:setLayoutParams(contentparams)
	holder:addView(widget)
	holder:addView(content)
	
	host:addView(holder)
	host:setup()
	
	
	tab1 = host:newTabSpec("tab_one_btn_tab")
	label1 = luajava.new(TextView,context)
	label1:setLayoutParams(fillparams)
	label1:setText("Click")
	label1:setTextSize(TypedValue:applyDimension(TypedValue.COMPLEX_UNIT_SP,textSize,DisplayMetrics))
	label1:setBackgroundResource(R_drawable.tab_background)
	label1:setGravity(GRAVITY_CENTER)
	
	--first page.
	
	--tmpview1 = luajava.new(TextView,context)
	--tmpview1:setText("first page")
	--tmpview1:setId(1)
	--tmpview1:setLayoutParams(fillparams);
	clickPageScroller = luajava.new(ScrollView,context)
	clickPageScroller:setLayoutParams(fillparams)
	clickPageScroller:setId(1)
	
	clickPage = luajava.new(LinearLayout,context)
	clickPage:setLayoutParams(fillparams)
	clickPage:setId(11)
	clickPage:setOrientation(LinearLayout.VERTICAL)
	
	clickLabelRow = luajava.new(LinearLayout,context)
	clickLabelRow:setLayoutParams(fillparams)
	
	clickLabel = luajava.new(TextView,context)
	clickLabel:setTextSize(TypedValue:applyDimension(TypedValue.COMPLEX_UNIT_SP,textSize,DisplayMetrics))
	clickLabel:setText("Label:")
	clickLabel:setGravity(Gravity.RIGHT)
	clickLabelParams = luajava.new(LinearLayoutParams,80*density,WRAP_CONTENT)
	clickLabel:setLayoutParams(clickLabelParams)
	
	clickLabelEdit = luajava.new(EditText,context)
	clickLabelEdit:setTextSize(TypedValue:applyDimension(TypedValue.COMPLEX_UNIT_SP,textSize,DisplayMetrics))
	clickLabelEditParams = luajava.new(LinearLayoutParams,FILL_PARENT,WRAP_CONTENT)
	clickLabelEdit:setLines(1)
	clickLabelEdit:setLayoutParams(clickLabelEditParams)
	if(numediting > 1) then
		clickLabelEdit:setEnabled(false)
	else
		if(editorValues.label ~= nil) then
			clickLabelEdit:setText(editorValues.label)
		end
	end
	
	
	clickLabelRow:addView(clickLabel)
	clickLabelRow:addView(clickLabelEdit)
	
	
	clickCmdRow = luajava.new(LinearLayout,context)
	clickCmdRow:setLayoutParams(fillparams)
	
	clickCmdLabel = luajava.new(TextView,context)
	clickCmdLabel:setTextSize(TypedValue:applyDimension(TypedValue.COMPLEX_UNIT_SP,textSize,DisplayMetrics))
	clickCmdLabel:setText("CMD:")
	clickCmdLabel:setGravity(Gravity.RIGHT)
	clickCmdLabelParams = luajava.new(LinearLayoutParams,80*density,WRAP_CONTENT)
	clickCmdLabel:setLayoutParams(clickLabelParams)
	
	clickCmdEdit = luajava.new(EditText,context)
	clickCmdEdit:setTextSize(TypedValue:applyDimension(TypedValue.COMPLEX_UNIT_SP,textSize,DisplayMetrics))
	clickCmdEditParams = luajava.new(LinearLayoutParams,WRAP_CONTENT,WRAP_CONTENT)
	clickCmdEdit:setInputType(TYPE_TEXT_FLAG_MULTI_LINE)
	clickCmdEdit:setHorizontallyScrolling(false)
	clickCmdEdit:setMaxLines(1000)
	clickCmdEdit:setLayoutParams(clickLabelEditParams)
	if(numediting > 1) then
		clickCmdEdit:setEnabled(false)
	else
		if(editorValues.command ~= nil) then
			clickCmdEdit:setText(editorValues.command)
		end
	end
	
	clickCmdRow:addView(clickCmdLabel)
	clickCmdRow:addView(clickCmdEdit)
	clickPage:addView(clickLabelRow)
	clickPage:addView(clickCmdRow)
	
	clickPageScroller:addView(clickPage)
	content:addView(clickPageScroller)
	tab1:setIndicator(label1)
	tab1:setContent(1)
	
	tab2 = host:newTabSpec("tab_two_btn_tab")
	label2 = luajava.new(TextView,context)
	label2:setLayoutParams(fillparams)
	label2:setText("Flip")
	label2:setTextSize(TypedValue:applyDimension(TypedValue.COMPLEX_UNIT_SP,textSize,DisplayMetrics))
	label2:setBackgroundResource(R_drawable.tab_background)
	label2:setGravity(GRAVITY_CENTER)
	
	--second, flip page.
	flipPageScroller = luajava.new(ScrollView,context)
	flipPageScroller:setLayoutParams(fillparams)
	flipPageScroller:setId(2)
	
	flipPage = luajava.new(LinearLayout,context)
	flipPage:setLayoutParams(fillparams)
	flipPage:setId(22)
	flipPage:setOrientation(LinearLayout.VERTICAL)
	
	flipLabelRow = luajava.new(LinearLayout,context)
	flipLabelRow:setLayoutParams(fillparams)
	
	flipLabel = luajava.new(TextView,context)
	flipLabel:setTextSize(TypedValue:applyDimension(TypedValue.COMPLEX_UNIT_SP,textSize,DisplayMetrics))
	flipLabel:setText("Label:")
	flipLabel:setGravity(Gravity.RIGHT)
	flipLabelParams = luajava.new(LinearLayoutParams,80*density,WRAP_CONTENT)
	flipLabel:setLayoutParams(flipLabelParams)
	
	flipLabelEdit = luajava.new(EditText,context)
	flipLabelEdit:setTextSize(TypedValue:applyDimension(TypedValue.COMPLEX_UNIT_SP,textSize,DisplayMetrics))
	flipLabelEditParams = luajava.new(LinearLayoutParams,FILL_PARENT,WRAP_CONTENT)
	flipLabelEdit:setLines(1)
	flipLabelEdit:setLayoutParams(clickLabelEditParams)
	if(numediting > 1) then
		flipLabelEdit:setEnabled(false)
	else
		if(editorValues.flipLabel ~= nil) then
			flipLabelEdit:setText(editorValues.flipLabel)
		end
	end
	
	flipLabelRow:addView(flipLabel)
	flipLabelRow:addView(flipLabelEdit)
	
	
	flipCmdRow = luajava.new(LinearLayout,context)
	flipCmdRow:setLayoutParams(fillparams)
	
	flipCmdLabel = luajava.new(TextView,context)
	flipCmdLabel:setTextSize(TypedValue:applyDimension(TypedValue.COMPLEX_UNIT_SP,textSize,DisplayMetrics))
	flipCmdLabel:setText("CMD:")
	flipCmdLabel:setGravity(Gravity.RIGHT)
	flipCmdLabelParams = luajava.new(LinearLayoutParams,80*density,WRAP_CONTENT)
	flipCmdLabel:setLayoutParams(clickLabelParams)
	
	flipCmdEdit = luajava.new(EditText,context)
	flipCmdEdit:setTextSize(TypedValue:applyDimension(TypedValue.COMPLEX_UNIT_SP,textSize,DisplayMetrics))
	flipCmdEditParams = luajava.new(LinearLayoutParams,FILL_PARENT,WRAP_CONTENT)
	flipCmdEdit:setInputType(TYPE_TEXT_FLAG_MULTI_LINE)
	flipCmdEdit:setHorizontallyScrolling(false)
	flipCmdEdit:setMaxLines(1000)
	flipCmdEdit:setLayoutParams(flipLabelEditParams)
	if(numediting > 1) then
		flipCmdEdit:setEnabled(false)
	else
		if(editorValues.flipCommand ~= nil) then
			flipCmdEdit:setText(editorValues.flipCommand)
		end
	end
	
	flipCmdRow:addView(flipCmdLabel)
	flipCmdRow:addView(flipCmdEdit)
	flipPage:addView(flipLabelRow)
	flipPage:addView(flipCmdRow)
	--tmpview2 = luajava.new(TextView,context)
	--tmpview2:setText("second page")
	----tmpview2:setId(2)
	--tmpview2:setLayoutParams(fillparams);
	flipPageScroller:addView(flipPage)
	content:addView(flipPageScroller)
	tab2:setIndicator(label2)
	tab2:setContent(2)
	
	tab3 = host:newTabSpec("tab_three_btn_tab")
	label3 = luajava.new(TextView,context)
	label3:setLayoutParams(fillparams)
	label3:setText("Advanced")
	label3:setTextSize(TypedValue:applyDimension(TypedValue.COMPLEX_UNIT_SP,textSize,DisplayMetrics))
	label3:setBackgroundResource(R_drawable.tab_background)
	label3:setGravity(GRAVITY_CENTER)
	
	--tmpview3 = luajava.new(TextView,context)
	--tmpview3:setText("third page")
	--tmpview3:setId(3)
	--tmpview3:setLayoutParams(params);	
	
	local scrollerpage = makeAdvancedPage()
	local parent = scrollerpage:getParent()
	if(parent ~= nil) then
		parent:removeView(scrollerpage)
	end
	--buttonNameRow:setVisibility(View.VISIBLE)
	controlRowTwo:setVisibility(View.VISIBLE)
	labelRowFour:setVisibility(View.VISIBLE)
	
	buttonNameRow:setVisibility(View.VISIBLE)
	buttonTargetSetRow:setVisibility(View.VISIBLE)
	
	Validator:reset()
	if(editorValues.width ~= "MULTI") then
		Validator:add(widthEdit,Validator_Number_Not_Blank,"Width")
	else
		Validator:add(widthEdit,Validator_Number_Or_Blank,"Width")
	end
	
	if(editorValues.height ~= "MULTI") then
		Validator:add(heightEdit,Validator_Number_Not_Blank,"Height")
	else
		Validator:add(heightEdit,Validator_Number_Or_Blank,"Height")
	end
	
	if(editorValues.x ~= "MULTI") then
		Validator:add(xcoordEdit,Validator_Number_Not_Blank,"X Coordinate")
	else
		Validator:add(xcoordEdit,Validator_Number_Or_Blank,"X Coordinate")
	end
	
	if(editorValues.y ~="MULTI") then
		Validator:add(ycoordEdit,Validator_Number_Not_Blank,"Y Coordinate")
	else
		Validator:add(ycoordEdit,Validator_Number_Or_Blank,"Y Coordinate")
	end
	
	if(editorValues.labelSize ~= "MULTI") then
		Validator:add(labelSizeEdit,Validator_Number_Not_Blank,"Label size")
	else
		Validator:add(labelSizeEdit,Validator_Number_Or_Blank,"Label size")
	end
	
	content:addView(scrollerpage)
	tab3:setIndicator(label3)
	tab3:setContent(3)
	
	host:addTab(tab1)
	host:addTab(tab2)
	host:addTab(tab3)
	
	
	if(numediting > 1) then
		host:setCurrentTab(2)
	else
		host:setCurrentTab(0)
	end
	
	
	--dialogView = top
	--else
		--set up the dialog
		--Note("already constructed editor"..dialogView:toString())
	--end
	
	editorDialog = luajava.newInstance("com.offsetnull.bt.window.LuaDialog",context,top,false,nil)
	editorDialog:show()
	context = nil
end

field = ""
colorpickerdone = {}
function colorpickerdone.colorChanged(color)
	if(field == "flip") then
		flipColor:setBackgroundColor(color)
		flipColor:invalidate()
		theFlipColor = color
	elseif(field == "normal") then
		normalColor:setBackgroundColor(color)
		normalColor:invalidate()
		theNormalColor = color;
	elseif(field == "pressed") then
		pressedColor:setBackgroundColor(color)
		pressedColor:invalidate()
		thePressedColor = color
	elseif(field == "label") then
		normalLabelColor:setBackgroundColor(color)
		normalLabelColor:invalidate()
		theNormalLabelColor = color
	elseif(field == "flipLabel") then
		flipLabelColor:setBackgroundColor(color)
		flipLabelColor:invalidate()
		theFlipLabelColor = color
	end
end
colorpickerdone_cb = luajava.createProxy("com.offsetnull.bt.button.ColorPickerDialog$OnColorChangedListener",colorpickerdone)

swatchclicked = {}
function swatchclicked.onClick(v)
	field = v:getTag()
	color = 0
	if(field == "flip") then
		color = theFlipColor
	elseif(field == "normal") then
		color = theNormalColor
	elseif(field == "pressed") then
		color = thePressedColor
	elseif(field == "label") then
		color = theNormalLabelColor
	elseif(field == "flipLabel") then
		color = theFlipLabelColor
	end
	colorpickerdialog = luajava.newInstance("com.offsetnull.bt.button.ColorPickerDialog",view:getContext(),colorpickerdone_cb,color)
	colorpickerdialog:show()
end
swatchclicked_cb = luajava.createProxy("android.view.View$OnClickListener",swatchclicked)

function makeAdvancedPage()
	
	local fnew = luajava.new
	--local slp = nil
	local context = view:getContext()
	if(advancedPageScroller == nil) then
		advancedPageScroller = fnew(ScrollView,context)
		--slp = advancedPageScroller
		--slp.__FunctionCalled = "setLayoutParams"
		advancedPageScroller:setId(3)
		--Note("makeing advanced page:")
	else
		--return advancedPageScroller
	end
	
	if(advancedPage == nil) then
		advancedPage = fnew(LinearLayout,context)
		advancedPage:setOrientation(LinearLayout.VERTICAL)
		advancedPageScroller:addView(advancedPage)
	end
	
	if(buttonNameRow == nil) then
		buttonNameRow = fnew(LinearLayout,context)
		buttonNameRow:setLayoutParams(fillparams)
		advancedPage:addView(buttonNameRow)
	end
	if(numediting > 1) then
		buttonNameRow:setVisibility(View.GONE)
	else
		buttonNameRow:setVisibility(View.VISIBLE)
	end
	
	
	buttonNameLabelParams = fnew(LinearLayoutParams,80*density,WRAP_CONTENT)
	if(buttonNameLabel == nil) then
		buttonNameLabel = fnew(TextView,context)
		
		buttonNameLabel:setLayoutParams(buttonNameLabelParams)
		buttonNameLabel:setText("Name:")
		buttonNameLabel:setTextSize(TypedValue:applyDimension(TypedValue.COMPLEX_UNIT_SP,textSize,DisplayMetrics))
		buttonNameLabel:setGravity(Gravity.RIGHT)
		buttonNameRow:addView(buttonNameLabel)
	end
	
	buttonNameEditParams = fnew(LinearLayoutParams,FILL_PARENT,WRAP_CONTENT)
		
	if(buttonNameEdit == nil) then
		buttonNameEdit = fnew(EditText,context)	
		buttonNameEdit:setTextSize(TypedValue:applyDimension(TypedValue.COMPLEX_UNIT_SP,textSize,DisplayMetrics))
		buttonNameEdit:setLines(1)
		buttonNameEdit:setLayoutParams(buttonNameEditParams)
		buttonNameRow:addView(buttonNameEdit)
	end
	if(numediting > 1) then
		--Note("\nEditing multiple, not setting name\n")
		buttonNameEdit:setText("")
		buttonNameEdit:setEnabled(false)
	else
		--Note("\nSetting editor value,"..editorValues.name)
		if(editorValues.name ~= nil) then
		--Note("\nEditing button, name is,"..editorValues.name.."\n")
			buttonNameEdit:setEnabled(true)
			buttonNameEdit:setText(editorValues.name)
		else
			--Note("\nEditorvalues.name is nil\n")
		end
		
	end
	
	if(buttonTargetSetRow == nil) then
		buttonTargetSetRow = fnew(LinearLayout,context)
		buttonTargetSetRow:setLayoutParams(fillparams)
		advancedPage:addView(buttonTargetSetRow)
	end
	if(numediting > 1) then
		buttonTargetSetRow:setVisibility(View.GONE)
	else
		buttonTargetSetRow:setVisibility(View.VISIBLE)
	end
	
	buttonTargetSetLabelParams = fnew(LinearLayoutParams,80*density,WRAP_CONTENT)
	if(buttonTargetSetLabel == nil) then
		buttonTargetSetLabel = fnew(TextView,context)
		
		buttonTargetSetLabel:setLayoutParams(buttonNameLabelParams)
		buttonTargetSetLabel:setText("Target Set:")
		buttonTargetSetLabel:setTextSize(TypedValue:applyDimension(TypedValue.COMPLEX_UNIT_SP,textSize,DisplayMetrics))
		buttonTargetSetLabel:setGravity(Gravity.RIGHT)
		buttonTargetSetRow:addView(buttonTargetSetLabel)
	end
	
	buttonTargetSetEditParams = fnew(LinearLayoutParams,FILL_PARENT,WRAP_CONTENT)
		
	if(buttonTargetSetEdit == nil) then
		buttonTargetSetEdit = fnew(EditText,context)	
		buttonTargetSetEdit:setTextSize(TypedValue:applyDimension(TypedValue.COMPLEX_UNIT_SP,textSize,DisplayMetrics))
		buttonTargetSetEdit:setLines(1)
		buttonTargetSetEdit:setLayoutParams(buttonTargetSetEditParams)
		buttonTargetSetRow:addView(buttonTargetSetEdit)
	end
	if(numediting > 1) then
		buttonTargetSetEdit:setEnabled(false)
		buttonTargetSetEdit:setText("")
		
	else
		if(editorValues.switchTo ~= nil) then
			buttonTargetSetEdit:setEnabled(true)
			buttonTargetSetEdit:setText(editorValues.switchTo)
		end
	end
	
	
	colortopLabelParams = fnew(LinearLayoutParams,WRAP_CONTENT,WRAP_CONTENT)
		
	if(colortopLabel == nil) then
		colortopLabel = fnew(TextView,context)
		colortopLabelParams:setMargins(0,10,0,0)
		colortopLabel:setLayoutParams(colortopLabelParams)
		colortopLabel:setTextSize(TypedValue:applyDimension(TypedValue.COMPLEX_UNIT_SP,textSize,DisplayMetrics))
		colortopLabel:setText("Colors:")
		advancedPage:addView(colortopLabel)
	end
	
	if(colorRowOne == nil) then
		colorRowOne = fnew(LinearLayout,context)
		colorRowOne:setLayoutParams(fillparams)
		advancedPage:addView(colorRowOne)
	end
	
	if(colorHolderA == nil) then
		colorHolderA = fnew(LinearLayout,context)
		colorHolderA:setLayoutParams(fillparams)
		colorHolderA:setGravity(GRAVITY_CENTER)
		--Note("addiing normal color holder")
		colorRowOne:addView(colorHolderA)
	end
	
	wrapparams = fnew(LinearLayoutParams,WRAP_CONTENT,WRAP_CONTENT)
	touchparams = fnew(LinearLayoutParams,60,60)
	
	if(normalColor == nil) then
		normalColor = fnew(View,context)
		normalColor:setLayoutParams(touchparams)
		theNormalColor = editorValues.primaryColor
		normalColor:setBackgroundColor(theNormalColor)
		normalColor:setTag("normal")
		normalColor:setOnClickListener(swatchclicked_cb)
		--Note("addiing normal color")
		colorHolderA:addView(normalColor)
	else 
		theNormalColor = editorValues.primaryColor
		normalColor:setBackgroundColor(theNormalColor)
	end

	if(colorHolderB == nil) then
		colorHolderB = fnew(LinearLayout,context)
		colorHolderB:setLayoutParams(fillparams)
		colorHolderB:setGravity(GRAVITY_CENTER)
		--Note("addiing pressed color holder")
		colorRowOne:addView(colorHolderB)
	end
	
	if(pressedColor == nil) then
		pressedColor = fnew(View,context)
		pressedColor:setLayoutParams(touchparams)
		pressedColor:setTag("pressed")
		pressedColor:setOnClickListener(swatchclicked_cb)
		--thePressedColor = Color:argb(255,120,250,250)
		thePressedColor = editorValues.selectedColor
		pressedColor:setBackgroundColor(thePressedColor)
		--Note("addiing pressed color")
		colorHolderB:addView(pressedColor)
	else
		thePressedColor = editorValues.selectedColor
		pressedColor:setBackgroundColor(thePressedColor)
	end
	
	if(colorHolderC == nil) then
		colorHolderC = fnew(LinearLayout,context)
		colorHolderC:setLayoutParams(fillparams)
		colorHolderC:setGravity(GRAVITY_CENTER)
		--Note("addiing flip colo hodlerr")
		colorRowOne:addView(colorHolderC)
	end
	
	if(flipColor == nil) then
		flipColor = fnew(View,context)
		flipColor:setLayoutParams(touchparams)
		flipColor:setTag("flip")
		flipColor:setOnClickListener(swatchclicked_cb)
		theFlipColor = editorValues.flipColor
		--Note("addiing flip color")
		flipColor:setBackgroundColor(theFlipColor)
		colorHolderC:addView(flipColor)
	else
		theFlipColor = editorValues.flipColor
		flipColor:setBackgroundColor(theFlipColor)
	end
	
	if(labelRowOne == nil) then
		labelRowOne = fnew(LinearLayout,context)
		labelRowOne:setLayoutParams(fillparams)
		advancedPage:addView(labelRowOne)
	end

	if(normalLabel == nil) then
		normalLabel = fnew(TextView,context)
		normalLabel:setLayoutParams(fillparams)
		normalLabel:setGravity(GRAVITY_CENTER)
		normalLabel:setText("Normal")
		normalLabel:setTextSize(15)
		labelRowOne:addView(normalLabel)
	end
	
	if(pressedLabel == nil) then
		pressedLabel = fnew(TextView,context)
		pressedLabel:setLayoutParams(fillparams)
		pressedLabel:setGravity(GRAVITY_CENTER)
		pressedLabel:setText("Pressed")
		pressedLabel:setTextSize(15)
		labelRowOne:addView(pressedLabel)
	end
	
	if(flippedLabel == nil) then
		flippedLabel = fnew(TextView,context)
		flippedLabel:setLayoutParams(fillparams)
		flippedLabel:setGravity(GRAVITY_CENTER)
		flippedLabel:setText("Flipped")
		flippedLabel:setTextSize(15)
		labelRowOne:addView(flippedLabel)
	end
	
	if(colorRowTwo == nil) then
		colorRowTwo = fnew(LinearLayout,context)
		colorRowTwoParams = fnew(LinearLayoutParams,fillparams)
		colorRowTwoParams:setMargins(0,10,0,0)
		colorRowTwo:setLayoutParams(colorRowTwoParams)
		advancedPage:addView(colorRowTwo)
	end
	
	if(colorHolderD == nil) then
		colorHolderD = fnew(LinearLayout,context)
		colorHolderD:setLayoutParams(fillparams)
		colorHolderD:setGravity(GRAVITY_CENTER)
	end
	
	if(normalLabelColor == nil) then
		normalLabelColor = fnew(View,context)
		normalLabelColor:setLayoutParams(touchparams)
		theNormalLabelColor = editorValues.labelColor
		normalLabelColor:setBackgroundColor(theNormalLabelColor)
		normalLabelColor:setTag("label")
		normalLabelColor:setOnClickListener(swatchclicked_cb)
		colorHolderD:addView(normalLabelColor)
		colorRowTwo:addView(colorHolderD)
	else
		theNormalLabelColor = editorValues.labelColor
		normalLabelColor:setBackgroundColor(theNormalLabelColor)
	end
	
	if(colorHolderE == nil) then
		colorHolderE = fnew(LinearLayout,context)
		colorHolderE:setLayoutParams(fillparams)
		colorHolderE:setGravity(GRAVITY_CENTER)
		colorRowTwo:addView(colorHolderE)
	end
	
	if(flipLabelColor == nil) then
		flipLabelColor = fnew(View,context)
		flipLabelColor:setLayoutParams(touchparams)
		flipLabelColor:setTag("flipLabel")
		flipLabelColor:setOnClickListener(swatchclicked_cb)
		--theFlipLabelColor = Color:argb(255,120,250,250)
		theFlipLabelColor = editorValues.flipLabelColor
		flipLabelColor:setBackgroundColor(theFlipLabelColor)
		colorHolderE:addView(flipLabelColor)
	else
		theFlipLabelColor = editorValues.flipLabelColor
		flipLabelColor:setBackgroundColor(theFlipLabelColor)
	end
	
	if(colorHolderF == nil) then
		colorHolderF = fnew(LinearLayout,context)
		colorHolderF:setLayoutParams(fillparams)
		colorHolderF:setGravity(GRAVITY_CENTER)
		colorRowTwo:addView(colorHolderF)
	end
	
	if(invisible == nil) then
		invisible = fnew(View,context)
		invisible:setVisibility(View.INVISIBLE)
		invisible:setLayoutParams(fillparams)
		colorHolderF:addView(invisible)
	end
	
	if(labelRowTwo == nil) then
		labelRowTwo = fnew(LinearLayout,context)
		labelRowTwo:setLayoutParams(fillparams)
		advancedPage:addView(labelRowTwo)
	end
	
	if(normalLabelLabel == nil) then
		normalLabelLabel = fnew(TextView,context)
		normalLabelLabel:setLayoutParams(fillparams)
		normalLabelLabel:setGravity(GRAVITY_CENTER)
		normalLabelLabel:setText("Label")
		normalLabelLabel:setTextSize(15)
		labelRowTwo:addView(normalLabelLabel)
	end
	
	if(flipLabelLabel == nil) then
		flipLabelLabel = fnew(TextView,context)
		flipLabelLabel:setLayoutParams(fillparams)
		flipLabelLabel:setGravity(GRAVITY_CENTER)
		flipLabelLabel:setText("FlipLabel")
		flipLabelLabel:setTextSize(15)
		labelRowTwo:addView(flipLabelLabel)
	end
	
	if(invisLabel == nil) then
		invisLabel = fnew(TextView,context)
		invisLabel:setLayoutParams(fillparams)
		invisLabel:setGravity(GRAVITY_CENTER)
		invisLabel:setText("FlipLabel")
		invisLabel:setTextSize(15)
		invisLabel:setVisibility(View.INVISIBLE)
		labelRowTwo:addView(invisLabel)
	end
	
	if(typeInLabel == nil) then
		typeInLabel = fnew(TextView,context)
		typeInLabelParams = fnew(LinearLayoutParams,WRAP_CONTENT,WRAP_CONTENT)
		typeInLabelParams:setMargins(0,10,0,0)
		typeInLabel:setLayoutParams(typeInLabelParams)
		typeInLabel:setTextSize(TypedValue:applyDimension(TypedValue.COMPLEX_UNIT_SP,textSizeSmall,DisplayMetrics))
		typeInLabel:setText("Type-In Controls:")
		advancedPage:addView(typeInLabel)
	end
	
	if(controlRowOne == nil) then
		controlRowOne = fnew(LinearLayout,context)
		controlRowOne:setLayoutParams(fillparams)
		advancedPage:addView(controlRowOne)
	end
	
	if(controlHolderA == nil) then
		controlHolderA = fnew(LinearLayout,context)
		controlHolderA:setLayoutParams(fillparams)
		controlHolderA:setGravity(GRAVITY_CENTER)
		controlRowOne:addView(controlHolderA)
	end
	
	numbereditorParams = fnew(LinearLayoutParams,120*density,WRAP_CONTENT)
	if(labelSizeEdit == nil) then
		labelSizeEdit = fnew(EditText,context)
		labelSizeEdit:setInputType(TYPE_CLASS_NUMBER)
		labelSizeEdit:setLayoutParams(numbereditorParams)
		labelSizeEdit:setTextSize(TypedValue:applyDimension(TypedValue.COMPLEX_UNIT_SP,textSizeSmall,DisplayMetrics))
		controlHolderA:addView(labelSizeEdit)
	end
	if(editorValues.labelSize == "MULTI") then
		labelSizeEdit:setText("")
	else
		labelSizeEdit:setText(tostring(math.floor(editorValues.labelSize)))
	end
	
	if(controlHolderB == nil) then
		controlHolderB = fnew(LinearLayout,context)
		controlHolderB:setLayoutParams(fillparams)
		controlHolderB:setGravity(GRAVITY_CENTER)
		controlRowOne:addView(controlHolderB)
	end
	--numbereditorParams = fnew(LinearLayoutParams,120,WRAP_CONTENT)
	if(widthEdit == nil) then
		widthEdit = fnew(EditText,context)
		widthEdit:setLayoutParams(numbereditorParams)
		widthEdit:setInputType(TYPE_CLASS_NUMBER)
		widthEdit:setTextSize(TypedValue:applyDimension(TypedValue.COMPLEX_UNIT_SP,textSizeSmall,DisplayMetrics))
		controlHolderB:addView(widthEdit)
	end
	if(editorValues.width == "MULTI") then
		widthEdit:setText("")
	else
		widthEdit:setText(tostring(math.floor(editorValues.width)))
	end
	
	if(controlHolderC == nil) then
		controlHolderC = fnew(LinearLayout,context)
		controlHolderC:setLayoutParams(fillparams)
		controlHolderC:setGravity(GRAVITY_CENTER)
		controlRowOne:addView(controlHolderC)
	end
	--numbereditorParams = fnew(LinearLayoutParams,120,WRAP_CONTENT)
	if(heightEdit == nil) then
		heightEdit = fnew(EditText,context)
		heightEdit:setLayoutParams(numbereditorParams)
		heightEdit:setInputType(TYPE_CLASS_NUMBER)
		heightEdit:setTextSize(TypedValue:applyDimension(TypedValue.COMPLEX_UNIT_SP,textSizeSmall,DisplayMetrics))
		controlHolderC:addView(heightEdit)
	end
	if(editorValues.height == "MULTI") then
		heightEdit:setText("")
	else
		heightEdit:setText(tostring(math.floor(editorValues.height)))
	end
	
	if(labelRowThree == nil) then
		labelRowThree = fnew(LinearLayout,context)
		labelRowThree:setLayoutParams(fillparams)
		advancedPage:addView(labelRowThree)
	end
	
	if(labelSizeLabel == nil) then
		labelSizeLabel = fnew(TextView,context)
		labelSizeLabel:setLayoutParams(fillparams)
		labelSizeLabel:setGravity(GRAVITY_CENTER)
		labelSizeLabel:setText("Label Font Size")
		labelSizeLabel:setTextSize(TypedValue:applyDimension(TypedValue.COMPLEX_UNIT_SP,textSizeSmall,DisplayMetrics))
		labelRowThree:addView(labelSizeLabel)
	end
	
	if(widthLabel == nil) then
		widthLabel = fnew(TextView,context)
		widthLabel:setLayoutParams(fillparams)
		widthLabel:setGravity(GRAVITY_CENTER)
		widthLabel:setText("Width")
		widthLabel:setTextSize(TypedValue:applyDimension(TypedValue.COMPLEX_UNIT_SP,textSizeSmall,DisplayMetrics))
		labelRowThree:addView(widthLabel)
	end
	
	if(heightLabel == nil) then
		heightLabel = fnew(TextView,context)
		heightLabel:setLayoutParams(fillparams)
		heightLabel:setGravity(GRAVITY_CENTER)
		heightLabel:setText("Height")
		heightLabel:setTextSize(TypedValue:applyDimension(TypedValue.COMPLEX_UNIT_SP,textSizeSmall,DisplayMetrics))
		labelRowThree:addView(heightLabel)
	--invisLabel:setVisibility(View.INVISIBLE)
	end
	
	if(controlRowTwo == nil) then
		controlRowTwo = fnew(LinearLayout,context)
		controlRowTwo:setLayoutParams(fillparams)
		advancedPage:addView(controlRowTwo)
	end
	controlRowTwo:setVisibility(View.VISIBLE)
	
	if(controlHolderD == nil) then
		controlHolderD = fnew(LinearLayout,context)
		controlHolderD:setLayoutParams(fillparams)
		controlHolderD:setGravity(GRAVITY_CENTER)
		controlRowTwo:addView(controlHolderD)
	end
	--numbereditorParams = fnew(LinearLayoutParams,120,WRAP_CONTENT)
	if(xcoordEdit == nil) then
		xcoordEdit = fnew(EditText,context)
		xcoordEdit:setLayoutParams(numbereditorParams)
		xcoordEdit:setInputType(TYPE_CLASS_NUMBER)
		xcoordEdit:setTextSize(TypedValue:applyDimension(TypedValue.COMPLEX_UNIT_SP,textSizeSmall,DisplayMetrics))
		controlHolderD:addView(xcoordEdit)
	end
	if(editorValues.x == "MULTI") then
		--Note("setting x string:MULTI")
		xcoordEdit:setText("")
	else
		--Note("setting x string:"..editorValues.x)
		xcoordEdit:setText(tostring(math.floor(editorValues.x)))
	end
	
	if(controlHolderE == nil) then
		controlHolderE = fnew(LinearLayout,context)
		controlHolderE:setLayoutParams(fillparams)
		controlHolderE:setGravity(GRAVITY_CENTER)
		controlRowTwo:addView(controlHolderE)
	end
	--numbereditorParams = fnew(LinearLayoutParams,120,WRAP_CONTENT)
	if(ycoordEdit == nil) then
		ycoordEdit = fnew(EditText,context)
		ycoordEdit:setLayoutParams(numbereditorParams)
		ycoordEdit:setInputType(TYPE_CLASS_NUMBER)
		ycoordEdit:setTextSize(TypedValue:applyDimension(TypedValue.COMPLEX_UNIT_SP,textSizeSmall,DisplayMetrics))
		controlHolderE:addView(ycoordEdit)
	end
	if(editorValues.y == "MULTI") then
		ycoordEdit:setText("")
	else
		ycoordEdit:setText(tostring(math.floor(editorValues.y)))
	end
	
	if(controlHolderF == nil) then
		controlHolderF = fnew(LinearLayout,context)
		controlHolderF:setLayoutParams(fillparams)
		controlHolderF:setGravity(GRAVITY_CENTER)
		controlRowTwo:addView(controlHolderF)
	end
	
	if(invisibleControl == nil) then
		invisibleControl = fnew(View,context)
		invisibleControl:setVisibility(View.INVISIBLE)
		invisibleControl:setLayoutParams(fillparams)
		controlHolderF:addView(invisibleControl)
	end
	
	if(labelRowFour == nil) then
		labelRowFour = fnew(LinearLayout,context)
		labelRowFour:setLayoutParams(fillparams)
		advancedPage:addView(labelRowFour)
	end
	
	if(xcoordLabel == nil) then
		xcoordLabel = fnew(TextView,context)
		xcoordLabel:setLayoutParams(fillparams)
		xcoordLabel:setGravity(GRAVITY_CENTER)
		xcoordLabel:setText("X Coord")
		xcoordLabel:setTextSize(TypedValue:applyDimension(TypedValue.COMPLEX_UNIT_SP,textSizeSmall,DisplayMetrics))
		labelRowFour:addView(xcoordLabel)
	end
	
	if(ycoordLabel == nil) then
		ycoordLabel = fnew(TextView,context)
		ycoordLabel:setLayoutParams(fillparams)
		ycoordLabel:setGravity(GRAVITY_CENTER)
		ycoordLabel:setText("Y Coord")
		ycoordLabel:setTextSize(TypedValue:applyDimension(TypedValue.COMPLEX_UNIT_SP,textSizeSmall,DisplayMetrics))
		labelRowFour:addView(ycoordLabel)
	end
	
	if(invisControlLabel == nil) then
		invisControlLabel = fnew(TextView,context)
		invisControlLabel:setLayoutParams(fillparams)
		invisControlLabel:setGravity(GRAVITY_CENTER)
		invisControlLabel:setText("FlipLabel")
		invisControlLabel:setTextSize(TypedValue:applyDimension(TypedValue.COMPLEX_UNIT_SP,textSizeSmall,DisplayMetrics))
		invisControlLabel:setVisibility(View.INVISIBLE)
		labelRowFour:addView(invisControlLabel)
	end
	
	return advancedPageScroller
	
end

toolbarModifyClicked = {}
function toolbarModifyClicked.onClick(v)
	
	if(buttonsCleared) then
		revertButtons()
	end
	--local adapter = mListView:getAdapter()
	local entry = buttonSetList[lastSelectedIndex+1]
	--Note("toolbarModifyClicked:"..entry.name)
	if(entry.name ~= lastLoadedSet) then
		PluginXCallS("loadAndEditSet",entry.name)
		return
	end

	enterManagerMode()
	showeditormenu = true
	PushMenuStack("onEditorBackPressed")
	
	mSelectorDialog:dismiss()
end
toolbarModifyClicked_cb = luajava.createProxy("android.view.View$OnClickListener",toolbarModifyClicked)

toolbarLoadClicked = {}
function toolbarLoadClicked.onClick(v)
	local entry = buttonSetList[lastSelectedIndex+1]
	if(entry.name ~= lastLoadedSet) then
		PluginXCallS("loadButtonSet",entry.name)
	end
	mSelectorDialog:dismiss()
end
toolbarLoadClicked_cb = luajava.createProxy("android.view.View$OnClickListener",toolbarLoadClicked)

function loadAndEditSet(data)
	--Note("Loading and editing: "..data)
	loadButtons(data)
	enterManagerMode()
	showeditormenu = true
	PushMenuStack("onEditorBackPressed")
	mSelectorDialog:dismiss()
end

thetoolbar = nil
toolbarlength = 0

function makeToolbar()
	--thetoolbar = thetoolbar or {}
	if(not thetoolbar) then
	thetoolbar = layoutInflater:inflate(R_layout.editor_selection_list_row_toolbar,nil)
	toolbarparams = luajava.new(RelativeLayoutParams,RelativeLayoutParams.WRAP_CONTENT,RelativeLayoutParams.WRAP_CONTENT)
	toolbarparams:addRule(RelativeLayout.ALIGN_PARENT_TOP)
	toolbarparams:addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
	toolbarparams:addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
	thetoolbar:setLayoutParams(toolbarparams)
	
	toolbarToggle = luajava.new(ImageButton,view:getContext())
	toolbarModify = luajava.new(ImageButton,view:getContext())
	toolbarDelete = luajava.new(ImageButton,view:getContext())
	
	buttonParams = luajava.new(LinearLayoutParams,LinearLayoutParams.WRAP_CONTENT,LinearLayoutParams.WRAP_CONTENT)
	buttonParams:setMargins(0,0,0,0)
	
	toolbarToggle:setLayoutParams(buttonParams) 
	toolbarModify:setLayoutParams(buttonParams) 
	toolbarDelete:setLayoutParams(buttonParams) 
	
	toolbarToggle:setPadding(0,0,0,0) 
	toolbarModify:setPadding(0,0,0,0) 
	toolbarDelete:setPadding(0,0,0,0) 
	
	toolbarToggle:setImageResource(R_drawable.toolbar_load_button)
	toolbarModify:setImageResource(R_drawable.toolbar_modify_button)
	toolbarDelete:setImageResource(R_drawable.toolbar_delete_button)
	
	toolbarToggle:setOnKeyListener(theButtonKeyListener)
	toolbarModify:setOnKeyListener(theButtonKeyListener)
	toolbarDelete:setOnKeyListener(theButtonKeyListener)
	
	toolbarToggle:setOnClickListener(toolbarLoadClicked_cb)
	toolbarModify:setOnClickListener(toolbarModifyClicked_cb)
	--Note("THE MODIFYCLICKER IS SET")
	toolbarDelete:setOnClickListener(toolbarDeleteClicked_cb)
	
	thetoolbar:addView(toolbarToggle)
	thetoolbar:addView(toolbarModify)
	thetoolbar:addView(toolbarDelete)
	
	closeButton = thetoolbar:findViewById(R_id.toolbar_tab_close)
	closeButton:setOnKeyListener(theButtonKeyListener)
	
	local tmpa = closeButton:getDrawable()
	local tmpb = toolbarToggle:getDrawable()
	
	thetoolbarlength = tmpa:getIntrinsicWidth() + 3 * tmpb:getIntrinsicWidth()
	end
	animateInController = nil
	animateOut = nil
	animateOutNoTransition = nil
	
	
	local tmpanim = luajava.new(TranslateAnimation,thetoolbarlength,0,0,0)
	tmpanim:setDuration(300)
	
	local tmpset = luajava.new(AnimationSet,true)
	tmpset:addAnimation(tmpanim)
	
	animateInController = luajava.new(LayoutAnimationController,tmpset)
	
	animateOut = luajava.new(TranslateAnimation,0,thetoolbarlength,0,0)
	animateOut:setDuration(300)
	
	animateOutNoTransition = luajava.new(TranslateAnimation,0,thetoolbarlength,0,0)
	animateOutNoTransition:setDuration(300)
	--Note("attatching removal listener")
	animateOutNoTransition:setAnimationListener(animateOutNoTransition_cb)	
	
end



animateOutNoTransition_handler = {}
function animateOutNoTransition_handler.onAnimationEnd(animation)
	--local rl = thetoolbar:getParent()
	--//rl:removeAllViews()
	
	--Note("animate out animation end firing.")
	local parent = thetoolbar:getParent()
	parent:removeAllViews()
	parent:invalidate()
	lastSelectedIndex = -1
end
animateOutNoTransition_cb = luajava.createProxy("android.view.animation.Animation$AnimationListener",animateOutNoTransition_handler)

makeToolbar()

targetholder = nil
lastSelectedPosition = -1

rowClicker = {}
function rowClicker.onClick(v)
	local pos = v:getId() / 157
	
	if(lastSelectedIndex < 0) then
		lastSelectedIndex = pos
		local holder = v:findViewById(R_id.toolbarholder)
		holder:setLayoutAnimation(animateInController)
		local adapter = mListView:getAdapter()
		entry = adapter:getItem(lastSelectedIndex)
		holder:addView(thetoolbar)
		--Note("row clicked, none selected")
	elseif(lastSelectedIndex ~= pos) then
		--Note("row clicked, not the selected row")
		local parent = thetoolbar:getParent()
		if(parent ~= nil) then
			if(mListView:getFirstVisiblePosition() <= lastSelectedIndex and mListView:getLastVisiblePosition() >= lastSelectedIndex) then
				parent:setAnimationListener(toolbarCustomAnimationListener_cb)
				parent:startAnimation(animateOut)
				targetIndex = pos
				targetHolder = v:findViewById(R_id.toolbarholder)
				--Note("starting custom animation")
			else
				parent:removeAllViews()
				local holder = v:findViewById(R_id.toolbarholder)
				holder:setLayoutAnimation(animateInController)
				holder:addView(thetoolbar)
				--Note("not starting custom animation")
			end
		end
	else
		--Note("selected row clicked")
		local parent = thetoolbar:getParent()
		if(parent == nil) then
			lastSelectedIndex = pos
			local holder = v:findViewById(R_id.toolbarholder)
			holder:setLayoutAnimation(animateInController)
			holder:addView(thetoolbar)
		else
			targetIndex = pos
			thetoolbar:startAnimation(animateOutNoTransition)
		end
	end
end
rowClicker_cb = luajava.createProxy("android.view.View$OnClickListener",rowClicker)

toolbarCustomAnimationListener = {}
function toolbarCustomAnimationListener.onCustomAnimationEnd()
	local parent = thetoolbar:getParent()
	if(parent == nil) then return end
	
	parent:removeAllViews()
	if(targetHolder ~= nil) then
		targetHolder:setLayoutAnimation(animateInController)
		targetHolder:addView(thetoolbar)
	end
	--Note("customanimationlistener fired,"..lastSelectedIndex.." target:"..targetIndex)
	lastSelectedIndex = targetIndex
end
toolbarCustomAnimationListener_cb = luajava.createProxy("com.offsetnull.bt.window.AnimatedRelativeLayout$OnAnimationEndListener",toolbarCustomAnimationListener)

managerDoneButtonListener = {}
function managerDoneButtonListener.onClick(v)
	--Note("exiting manager mode")
	exitManagerMode()
end

managerDoneButton_cb = luajava.createProxy("android.view.View$OnClickListener",managerDoneButtonListener)

buttonSetSettingsButtonListener = {}
function buttonSetSettingsButtonListener.onClick(v)
	buttonOptions()
end
buttonSetSettingsButton_cb = luajava.createProxy("android.view.View$OnClickListener",buttonSetSettingsButtonListener)

backWidgetMovePaddingBottom = 10
backWidgetMoveLastY = -1
backWidgetMoveTouchListener = {}
backWidgetMoveTotal = 0
function backWidgetMoveTouchListener.onTouch(v,e)
	if(e:getAction() == MotionEvent.ACTION_MOVE) then
		local dy = e:getY() - backWidgetMoveLastY
		--Note("touch y event at: "..e:getY())
		backWidgetMoveLastY = e:getY()
		backWidgetMoveTotal = backWidgetMoveTotal - dy
		backWidgetMovePaddingBottom = backWidgetMovePaddingBottom + backWidgetMoveTotal
		backWidget:setPadding(0,0,0,backWidgetMovePaddingBottom)
		--Note("relayotging:"..backWidgetMovePaddingBottom.." dy:"..dy)
		backWidget:requestLayout()
	elseif(e:getAction() == MotionEvent.ACTION_UP) then
		backWidgetMoveTotal = 0
		
	elseif(e:getAction() == MotionEvent.ACTION_DOWN) then
		backWidgetMoveLastY = e:getY()
	end
	return true
end
backWidgetMoveTouch_cb = luajava.createProxy("android.view.View$OnTouchListener",backWidgetMoveTouchListener)

backWidget = nil
function makeBackWidget()
	if(backWidget ~= nil) then return backWidget end
	
	backWidget = luajava.new(LinearLayout,view:getContext())
	
	backWidgetParams = luajava.new(RelativeLayoutParams,RelativeLayoutParams.WRAP_CONTENT,RelativeLayoutParams.WRAP_CONTENT)
	backWidgetParams:addRule(RelativeLayout.ALIGN_LEFT,view:getId())
	backWidgetParams:addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
	
	backWidget:setPadding(0,0,0,backWidgetMovePaddingBottom)
	
	backWidget:setLayoutParams(backWidgetParams)
	backWidget:setOrientation(LinearLayout.VERTICAL)
	backWidgetButtonParams = luajava.new(LinearLayoutParams,LinearLayoutParams.WRAP_CONTENT,LinearLayoutParams.WRAP_CONTENT)
	backWidgetButtonParams:setMargins(0,0,0,0)
	
	backWidgetMoveButton = luajava.new(ImageButton,view:getContext())
	backWidgetMoveButton:setImageResource(R_drawable.toolbar_move_button)
	backWidgetMoveButton:setPadding(0,0,0,0)
	backWidgetMoveButton:setLayoutParams(backWidgetButtonParams)
	backWidgetMoveButton:setOnTouchListener(backWidgetMoveTouch_cb)
	
	buttonSetSettingsButton = luajava.new(ImageButton,view:getContext())
	buttonSetSettingsButton:setImageResource(R_drawable.toolbar_modify_button)
	buttonSetSettingsButton:setPadding(0,0,0,0)
	buttonSetSettingsButton:setLayoutParams(backWidgetButtonParams)
	buttonSetSettingsButton:setOnClickListener(buttonSetSettingsButton_cb)
	
	managerDoneButton = luajava.new(ImageButton,view:getContext())
	managerDoneButton:setImageResource(R_drawable.toolbar_check_button)
	managerDoneButton:setPadding(0,0,0,0)
	managerDoneButton:setLayoutParams(backWidgetButtonParams)
	
	managerDoneButton:setOnClickListener(managerDoneButton_cb)
	
	backWidget:addView(backWidgetMoveButton)
	backWidget:addView(buttonSetSettingsButton)
	backWidget:addView(managerDoneButton)
	
	return backWidget
end

setSettingsButtonListener = {}
function setSettingsButtonListener.onClick(v)
	local context = view:getContext()

	editorValues = {}
	editorValues.primaryColor = defaults.primaryColor
	editorValues.selectedColor = defaults.selectedColor
	editorValues.flipColor = defaults.flipColor
	editorValues.labelColor = defaults.labelColor
	editorValues.flipLabelColor = defaults.flipLabelColor
	editorValues.switchTo = ""
	editorValues.height = defaults.height
	editorValues.width = defaults.width
	editorValues.labelSize = defaults.labelSize
	editorValues.name = lastLoadedSet
	editorValues.x = 0
	editorValues.y = 0	

	local page = makeAdvancedPage()
	local parent = page:getParent()
	if(parent ~= nil) then
		parent:removeView(page)
	end
	
	buttonNameRow:setVisibility(View.GONE)
	buttonTargetSetRow:setVisibility(View.GONE)
	buttonNameRow:setVisibility(View.VISIBLE)
	controlRowTwo:setVisibility(View.GONE)
	labelRowFour:setVisibility(View.GONE)
	
	Validator:reset()
	Validator:add(buttonNameEdit,Validator_Not_Blank,"Set name")
	Validator:add(widthEdit,Validator_Number_Not_Blank,"Width")
	Validator:add(heightEdit,Validator_Number_Not_Blank,"Height")
	Validator:add(labelSizeEdit,Validator_Number_Not_Blank,"Label size")
	

	local width_param,height_param = getDialogDimensions()
	local top = luajava.new(LinearLayout,context)
	local topparams = luajava.new(LinearLayoutParams,width_param,height_param)
	
	top:setLayoutParams(topparams)
	top:setOrientation(LinearLayout.VERTICAL)
	local titletext = luajava.new(TextView,context)
	local titletextParams = luajava.new(LinearLayoutParams,FILL_PARENT,WRAP_CONTENT)
	--titletextParams:addRule(RelativeLayout.ALIGN_PARENT_TOP)
	
	titletext:setLayoutParams(titletextParams)
	titletext:setTextSize(36)
	titletext:setText("DEFAULTS EDITOR")
	titletext:setGravity(GRAVITY_CENTER)
	titletext:setId(1)
	top:addView(titletext)
	
	--buttonNameEdit:setText()

	--make the new tabhost.	
	local params = luajava.new(LinearLayoutParams,WRAP_CONTENT,WRAP_CONTENT)
	local fillparams = luajava.new(LinearLayoutParams,FILL_PARENT,WRAP_CONTENT,1)
	local contentparams = luajava.new(LinearLayoutParams,FILL_PARENT,WRAP_CONTENT)
	--fillparams:setGravity(Gravity.FILL_HORIZONTAL)
	--hostparams = luajava.new(LinearLayoutParams,FILL_PARENT,FILL_PARENT)
	--make the done and cancel buttons.
	--have to stuff them in linearlayout.
	local finishHolderParams = luajava.new(LinearLayoutParams,FILL_PARENT,WRAP_CONTENT)
	--finishHolderParams:addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
	local finishHolder = luajava.new(LinearLayout,context)
	finishHolder:setLayoutParams(finishHolderParams)
	finishHolder:setId(2)
	
	--local holder = luajava.new(LinearLayout,context)
	--holder:addView(page,contentparams)
	local holderparam = luajava.new(LinearLayoutParams,FILL_PARENT,WRAP_CONTENT,2)
	--holderparam:addRule(RelativeLayout.ABOVE,2)
	--holderparam:addRule(RelativeLayout.BELOW,1)
	--holderparam:addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
	--holderparam:addRule(RelativeLayout.ALIGN_PARENT_LEFT)
	
	page:setVisibility(View.VISIBLE)
	top:addView(page,holderparam)
	
	
	--finishbuttonParams = luajava.new(RelativeLayoutParams,RLayoutParams.FILL_PARENT,WRAP_CONTENT)
	local done = luajava.new(Button,context)
	done:setLayoutParams(fillparams)
	done:setText("Done")
	done:setOnClickListener(seteditorDone_cb)
	
	local cancel = luajava.new(Button,context)
	cancel:setLayoutParams(fillparams)
	cancel:setText("Cancel")
	cancel:setOnClickListener(seteditorCancel_cb)
	finishHolder:addView(done)
	finishHolder:addView(cancel)
	top:addView(finishHolder)
	
	buttSetSettingsEditor = luajava.newInstance("com.offsetnull.bt.window.LuaDialog",view:getContext(),top,false,nil)
	
	buttSetSettingsEditor:show()
end
setSettingsButton_cb = luajava.createProxy("android.view.View$OnClickListener",setSettingsButtonListener)

setEditorCancelListener = {}
function setEditorCancelListener.onClick(v)
	buttSetSettingsEditor:dismiss()
end
seteditorCancel_cb = luajava.createProxy("android.view.View$OnClickListener",setEditorCancelListener)

setEditorDoneListener = {}
function setEditorDoneListener.onClick(v)
	--apply the settings.
	local str = Validator:validate()
	if(str ~= nil) then
		Validator:showMessage(view:getContext(),str)
		return
	end
	
	
	labelsizetmp = labelSizeEdit:getText()
	labelsize = tonumber(labelsizetmp:toString())
	----Note(
	heighttmp = heightEdit:getText()
	
	height = tonumber(heighttmp:toString())
	--Note("height read from editor"..height)
	widthtmp = widthEdit:getText()
	width = tonumber(widthtmp:toString())
	
	--first strip any settings that match the current default.
	for i,b in pairs(buttons) do
		if rawget(b.data,"primaryColor") == defaults.primaryColor then
			rawset(b.data,"primaryColor",nil)
		end
		
		if rawget(b.data,"selectedColor") == defaults.selectedColor then
			rawset(b.data,"selectedColor",nil)
		end
		
		if rawget(b.data,"flipColor") == defaults.flipColor then
			rawset(b.data,"flipColor",nil)
		end
		
		if rawget(b.data,"labelColor") == defaults.labelColor then
			rawset(b.data,"primaryColor",nil)
		end
		
		if rawget(b.data,"flipLabelColor") == defaults.flipLabelColor then
			rawset(b.data,"flipLabelColor",nil)
		end
		
		if rawget(b.data,"height") == defaults.height then
			rawset(b.data,"height",nil)
		end
		
		if rawget(b.data,"width") == defaults.width then
			rawset(b.data,"width",nil)
		end
		
		if rawget(b.data,"labelSize") == defaults.labelSize then
			rawset(b.data,"labelSize",nil)
		end
	end
	
	defaults.primaryColor = theNormalColor
	defaults.selectedColor = thePressedColor
	defaults.flipColor = theFlipColor
	defaults.labelColor = theLabelColor
	defaults.flipLabelColor = theFlipLabelColor
	defaults.height = height
	defaults.width = width
	defaults.labelSize = labelsize

	buttSetSettingsEditor:dismiss()
	
	local tmp = {}
	for i,b in pairs(buttons) do
		tmp[i] = b.data
	end
		
	PluginXCallS("saveButtons",serialize(tmp))
	
	PluginXCallS("saveSetDefaults",serialize(defaults))
	
	drawButtons()
end
seteditorDone_cb = luajava.createProxy("android.view.View$OnClickListener",setEditorDoneListener)

function loadOptions(data)
	--Note("incoming options wad:"..data)
	options = loadstring(data)()
	buttonRoundness = tonumber(options.roundness)
	drawButtons()
	--Note("loaded button options:"..options.auto_edit)
end

function performHapticPress()
	--Note("performing haptic press")
	if(options.haptic_press == "2") then return end
	
	flags = 1
	if(options.haptic_press == "1") then
	--Note("overriding system")
		flags = 3
	end
	
	view:performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY,flags)
end

function performHapticFlip()
	--Note("performing haptic flip")
	if(options.haptic_flip == "2") then return end
	
	flags = 1
	if(options.haptic_flip == "1") then
	--Note("overriding system")
		flags = 3
	end
	
	view:performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY,flags)
end

function performHapticEdit()
	if(options.haptic_edit == "2") then return end
	
	flags = 1
	if(options.haptic_edit == "1") then
		flags = 3
	end
	
	view:performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY,flags)
end

buttonsCleared = false
clearSet = {}
revertButtonData = {}
revertButtonData.label = "BACK"
revertButtonData.width = 80*density
revertButtonData.height = 25*density
revertButtonData.x = 100
revertButtonData.y = 100
revertButton = BUTTON:new(revertButtonData,density)
revertButtonSet = {}
revertButtonSet[1] = revertButton
function clearButtons()
	if(buttonsCleared) then return end
	buttonsCleared = true
	revertset = buttons
	buttons = revertButtonSet
	drawButtons()
	suppress_editor = true
	view:invalidate()
end

function revertButtons()
	buttonsCleared = false
	buttons = revertset
	drawButtons()
	suppress_editor = false
	view:invalidate()
end

rootHolder = view:getParent()
mainwindow = rootHolder:findViewById(6666)


showeditormenu = false
editmenu = nil
topMenuItem = nil
function PopulateMenu(menu)
	--debugPrint("in options menu populate")
	
		if(showeditormenu) then
			local settings = menu:add("Editor Options")
			settings:setIcon(resLoader(respath,"settings.png"))
			settings:setOnMenuItemClickListener(buttonsetSettingsClicked_cb)
			local done = menu:add("Done")
			done:setIcon(resLoader(respath,"done.png"))
			done:setOnMenuItemClickListener(buttonsetMenuDoneClicked_cb)
			local cancel = menu:add("Cancel")
			cancel:setIcon(resLoader(respath,"cancel.png"))
			cancel:setOnMenuItemClickListener(buttonsetCancelClicked_cb)
			foo = function(item) item:setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS) end
			pcall(foo,done)
			pcall(foo,settings)
			pcall(foo,cancel)
			return
		end
		
	--if(topMenuItem == nil) then
		topMenuItem = menu:add(0,401,401,"Ex Button Sets")
		topMenuItem:setIcon(R_drawable.ic_menu_button_sets)
		topMenuItem:setOnMenuItemClickListener(buttonsetMenuClicked_cb)
		
		--Note("populated lua button sets")
		foo = function(item) item:setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER) end
		if(not pcall(foo,topMenuItem))  then
			--Note("action bar not supported,android version < 3.0")
		end
		
	--else
	--	menu:add(topMenuItem)
	--end
end

buttonsetMenuClicked = {}
function buttonsetMenuClicked.onMenuItemClick(item)
	--Note("menu item clicked")
	buttonList()
	--showeditormenu = true
	--PushMenuStack()
	
	return true
end
buttonsetMenuClicked_cb = luajava.createProxy("android.view.MenuItem$OnMenuItemClickListener",buttonsetMenuClicked)

buttonsetMenuDoneClicked = {}
function buttonsetMenuDoneClicked.onMenuItemClick(item)
	showeditormenu = false
	if(moveBitmap ~= nil) then
		exitMoveMode()
	end
	exitManagerMode()
	PopMenuStack()
	return true
end
buttonsetMenuDoneClicked_cb = luajava.createProxy("android.view.MenuItem$OnMenuItemClickListener",buttonsetMenuDoneClicked)

buttonsetSettingsClicked = {}
function buttonsetSettingsClicked.onMenuItemClick(item)
	buttonOptions()
	return true
end
buttonsetSettingsClicked_cb = luajava.createProxy("android.view.MenuItem$OnMenuItemClickListener",buttonsetSettingsClicked)

buttonsetCancelClicked = {}
function buttonsetCancelClicked.onMenuItemClick(item)
	showeditormenu = false
	PopMenuStack()
	if(moveBitmap ~= nil) then
		exitMoveMode()
	end
	exitManagerModeNoSave()
	return true
end
buttonsetCancelClicked_cb = luajava.createProxy("android.view.MenuItem$OnMenuItemClickListener",buttonsetCancelClicked)

resources = view:getContext():getResources()
function resLoader(root,bmp)
	local target = nil
	local metrics = resources:getDisplayMetrics()
	local d = metrics.density
	if(d < 1.0) then
		target = luajava.newInstance("android.graphics.drawable.BitmapDrawable",resources,root.."/ldpi/"..bmp)
	elseif(d >= 1.0 and d < 1.5) then
		target = luajava.newInstance("android.graphics.drawable.BitmapDrawable",resources,root.."/mdpi/"..bmp)
	elseif(d >= 1.5) then
		target = luajava.newInstance("android.graphics.drawable.BitmapDrawable",resources,root.."/hdpi/"..bmp)
	end
	
	return target
end

function onEditorBackPressed()
	showeditormenu = false
	PopMenuStack()
	exitManagerModeNoSave()
end

view:bringToFront()

function setDebug(off)
	if(off == "on") then
		debugString("Button window entering debug mode...")
		--WindowXCallS("button_window","setDebug","on")
		debugInfo = true
	else
		debugString("Button window debug mode...")
		--WindowXCallS("button_window","setDebug","off")
		debugInfo = false
	end
end

local importDialogClick = {}
function importDialogClick.onClick(dialog,which)
	local DialogInterface = luajava.bindClass("android.content.DialogInterface")
	
	if(which == DialogInterface.BUTTON_POSITIVE) then
		PluginXCallS("doImport","blank")
		dialog:dismiss()
		CloseOptionsDialog()
	else
		dialog:dismiss()
	end
end

local importDialogClick_cb = luajava.createProxy("android.content.DialogInterface$OnClickListener",importDialogClick)

local dismissDialog = {}
function dismissDialog.onClick(dialog,which)
	dialog:dismiss()
end
local dismissDialog_cb = luajava.createProxy("android.content.DialogInterface$OnClickListener",dismissDialog)

function failImport(str)
	local build = luajava.newInstance("android.app.AlertDialog$Builder",view:getContext())
	
	build:setPositiveButton("Ok",dismissDialog_cb)
	--build:setNegativeButton("No",importDialogClick_cb);
	build:setTitle("Import Failed")
	build:setMessage(str)
	alert = build:create()
	alert:show()
end

function importSuccess(str)
	local build = luajava.newInstance("android.app.AlertDialog$Builder",view:getContext())
	
	build:setPositiveButton("Ok",dismissDialog_cb)
	--build:setNegativeButton("No",importDialogClick_cb);
	build:setTitle("Import Complete")
	build:setMessage(str.." Buttons imported")
	alert = build:create()
	alert:show()
end

function askImport()
	local build = luajava.newInstance("android.app.AlertDialog$Builder",view:getContext())
	
	build:setPositiveButton("Yes",importDialogClick_cb)
	build:setNegativeButton("No",importDialogClick_cb);
	build:setTitle("Import Buttons?")
	build:setMessage("Import buttons from internal settings?")
	alert = build:create()
	alert:show()
end

PluginXCallS("buttonLayerReady","")
debugString("Button Window Script Loaded")