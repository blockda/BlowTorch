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
DialogInterface = luajava.bindClass("android.content.DialogInterface")
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
	AddOptionCallback("buttonList","Button Sets",nil)
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
	gridXwidth = defaults.gridXwidth*density
	gridYwidth = defaults.gridYwidth*density
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

gridXwidth = 40 * density --67 * density
gridYwidth = 40 * density --67 * density
seekerX = {}
function seekerX.onProgressChanged(v,prog,state)
	----Note("seekbarchanged:"..prog)
	local tmp = 32 + prog
	gridXwidth = tmp *density
	gridXSizeLabel:setText("Grid X Spacing: "..tmp)
	-- set default width
	defaults.gridXwidth = tmp

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
	gridYwidth = tmp *density
	gridYSizeLabel:setText("Grid Y Spacing: "..tmp)
	-- set default height
	defaults.gridYwidth = tmp
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

local TOOLBARHOLDER_ID = R_id.toolbarholder
local ROOT_ID = R_id.root
local EDITOR_SELECTION_LIST_ROW_ID = R_layout.editor_selection_list_row
local INFOTITLE_ID = R_id.infoTitle
local INFOEXTENDED = R_id.infoExtended		
local ICON_ID = R_id.icon
local VIEW_GONE = View.GONE

local buttonSetListDialog --need this

function showButtonList(data)
	--Note(data)
	setdata = loadstring(data)()
	
	--launch the new editor
	
	for key,value in pairs(luajava) do
  	Note("\npre found member " .. key);
	end
	if(buttonSetListDialog == nil) then
		buttonSetListDialog = require("buttonlist")
		buttonSetListDialog.init(mContext)
	end
	Note("Showing new list\n")
	--buttonSetListDialog.init()
	buttonSetListDialog.showList(setdata,lastLoadedSet)
	return
	
end

function updateButtonListDialog(data)
	--Note("\nConfirmingDelete")
	local incoming = loadstring(data)()
	
	buttonSetListDialog.updateButtonListDialog(incoming)
end

function updateButtonListDialogNoItems()
	--mListView:setAdapter(buttonListAdapter_cb)
	--emptyButtons()
	--mSelectorDialog:dismiss()
	buttonSetListDialog.updateButtonListDialogNoItems()
	emptyButtons()
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
	saveDefaultOptions()
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
	newb.data.width = defaults.width --(gridXwidth-5)/density
	newb.data.height = defaults.height --(gridYwidth-5)/density
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

-- Individual button editor 
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
	targettmp = buttonTargetSetEdit:getText();
	target = targettmp:toString();
	
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
		
		tmp.data.name = name
		tmp.data.switchTo = target
		
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

tabMinHeight = (35 * density) -- dp value TODO
bgGrey = Color:argb(255,0x99,0x99,0x99) -- background color

textSizeBig = (18) -- sp value
textSize = (14)  
textSizeSmall = (10) 
-- Note("Density: " .. density ..", TextSize: "..textSize .. "textSizeSmall: ".. textSizeSmall)
screenlayout = view:getContext():getResources():getConfiguration().screenLayout
local test = bit.band(screenlayout,Configuration.SCREENLAYOUT_SIZE_MASK)

local function foo()
	--Note(test)
	--Note(Configuration.SCREENLAYOUT_SIZE_XLARGE)
	--Note("Entering the foo()"..test)
	if(test == Configuration.SCREENLAYOUT_SIZE_XLARGE) then
		textSizeBig = (22)
		textSize = (18)
		textSizeSmall = (14)
	end
end
pcall(foo)




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
	
	if(1==1) then
	 local buttonEditor = require("buttoneditor")
	 buttonEditor.init(mContext)
	 buttonEditor.showEditorDialog(editorValues,numediting)
	 return
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
	titletext:setTextSize(textSizeBig)
	titletext:setText("EDIT BUTTON")
	titletext:setGravity(GRAVITY_CENTER)
	titletext:setTextColor(Color:argb(255,0x33,0x33,0x33))
	titletext:setBackgroundColor(bgGrey)
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
	label1:setTextSize(textSizeBig)
	label1:setBackgroundResource(R_drawable.tab_background)
	label1:setGravity(GRAVITY_CENTER)
	label1:setMinHeight(tabMinHeight)
	
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
	clickLabel:setTextSize(textSize)
	clickLabel:setText("Label:")
	clickLabel:setGravity(Gravity.RIGHT)
	clickLabelParams = luajava.new(LinearLayoutParams,80*density,WRAP_CONTENT)
	clickLabel:setLayoutParams(clickLabelParams)
	
	clickLabelEdit = luajava.new(EditText,context)
	clickLabelEdit:setTextSize(textSize)
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
	clickCmdLabel:setTextSize(textSize)
	clickCmdLabel:setText("CMD:")
	clickCmdLabel:setGravity(Gravity.RIGHT)
	clickCmdLabelParams = luajava.new(LinearLayoutParams,80*density,WRAP_CONTENT)
	clickCmdLabel:setLayoutParams(clickLabelParams)
	
	clickCmdEdit = luajava.new(EditText,context)
	clickCmdEdit:setTextSize(textSize)
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
	label2:setTextSize(textSizeBig)
	label2:setBackgroundResource(R_drawable.tab_background)
	label2:setGravity(GRAVITY_CENTER)
	label2:setMinHeight(tabMinHeight)
	
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
	flipLabel:setTextSize(textSize)
	flipLabel:setText("Label:")
	flipLabel:setGravity(Gravity.RIGHT)
	flipLabelParams = luajava.new(LinearLayoutParams,80*density,WRAP_CONTENT)
	flipLabel:setLayoutParams(flipLabelParams)
	
	flipLabelEdit = luajava.new(EditText,context)
	flipLabelEdit:setTextSize(textSize)
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
	flipCmdLabel:setTextSize(textSize)
	flipCmdLabel:setText("CMD:")
	flipCmdLabel:setGravity(Gravity.RIGHT)
	flipCmdLabelParams = luajava.new(LinearLayoutParams,80*density,WRAP_CONTENT)
	flipCmdLabel:setLayoutParams(clickLabelParams)
	
	flipCmdEdit = luajava.new(EditText,context)
	flipCmdEdit:setTextSize(textSize)
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
	label3:setTextSize(textSizeBig)
	label3:setBackgroundResource(R_drawable.tab_background)
	label3:setGravity(GRAVITY_CENTER)
	label3:setMinHeight(tabMinHeight)
	
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

modifyButtonSet = function(entry) 
  Note("In Modify button set callback.")
  if(buttonsCleared) then
    revertButtons()
  end
  --local adapter = mListView:getAdapter()
  --local entry = buttonSetList[lastSelectedIndex+1]
  --Note("toolbarModifyClicked:"..entry.name)
  if(entry.name ~= lastLoadedSet) then
    PluginXCallS("loadAndEditSet",entry.name)
    return
  end

  enterManagerMode()
  showeditormenu = true
  PushMenuStack("onEditorBackPressed")
end

toolbarLoadClicked = {}
function toolbarLoadClicked.onClick(v)
	local entry = buttonSetList[lastSelectedIndex+1]
	if(entry.name ~= lastLoadedSet) then
		PluginXCallS("loadButtonSet",entry.name)
	end
	mSelectorDialog:dismiss()
end
toolbarLoadClicked_cb = luajava.createProxy("android.view.View$OnClickListener",toolbarLoadClicked)


deleteConfirmListener = {}
function deleteConfirmListener.onClick(dialog,which)
	--Note("deleting,"..which)
	if(which == DialogInterface.BUTTON_POSITIVE) then
		--find the button set.
		local entry = buttonSetList[lastSelectedIndex+1]
		--if(entry.name ~= lastLoadedSet) then
		buttonSetList[entry] = nil
		table.remove(buttonSetList,lastSelectedIndex+1)
		PluginXCallS("deleteButtonSet",entry.name)
		--end
	end
end
deleteConfirmListener_cb = luajava.createProxy("android.content.DialogInterface$OnClickListener",deleteConfirmListener)

deleteCancelListener = {}
function deleteCancelListener.onClick(dialog,which)
	dialog:dismiss()
end
deleteCancelListener_cb = luajava.createProxy("android.content.DialogInterface$OnClickListener",deleteCancelListener)
toolbarDeleteClicked = {}
function toolbarDeleteClicked.onClick(v)
	local builder = luajava.newInstance("android.app.AlertDialog$Builder",view:getContext())
	builder:setTitle("Delete Button Set")
	builder:setMessage("Confirm delete?")
	builder:setPositiveButton("Yes",deleteConfirmListener_cb)
	builder:setNegativeButton("No",deleteCancelListener_cb)
	
	local dialog = builder:create()
	dialog:show()
	
end
toolbarDeleteClicked_cb = luajava.createProxy("android.view.View$OnClickListener",toolbarDeleteClicked)

function loadAndEditSet(data)
	--Note("Loading and editing: "..data)
	loadButtons(data)
	enterManagerMode()
	showeditormenu = true
	PushMenuStack("onEditorBackPressed")
	if(buttonSetListDialog ~= nil) then
	 buttonSetListDialog.dismissList()
	end
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
	
	
	animateIn = luajava.new(TranslateAnimation,thetoolbarlength,0,0,0)
	animateIn:setDuration(300)
	
	animateOut = luajava.new(TranslateAnimation,0,thetoolbarlength,0,0)
	animateOut:setDuration(300)
	animateOut:setAnimationListener(animateOut_cb)
	
end



animateOutHandler = {}
function animateOutHandler.onAnimationEnd(animation)
	--local rl = thetoolbar:getParent()
	--//rl:removeAllViews()
	
	--Note("animate out animation end firing.")
	local parent = thetoolbar:getParent()
	if(parent ~= nil) then 
		parent:removeView(thetoolbar)
		mList:requestFocus()
	end
end
animateOut_cb = luajava.createProxy("android.view.animation.Animation$AnimationListener",animateOutHandler)

makeToolbar()

targetholder = nil
lastSelectedPosition = -1

rowClicker = {}
function rowClicker.onClick(v)
	
end
rowClicker_cb = luajava.createProxy("android.view.View$OnClickListener",rowClicker)

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
	titletext:setTextSize(textSize * 2)
	titletext:setText("DEFAULTS EDITOR")
	titletext:setGravity(GRAVITY_CENTER)
	titletext:setTextColor(Color:argb(255,0x33,0x33,0x33))
	titletext:setBackgroundColor(bgGrey)
	--titleText:setTextColor()
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
	--defaults.gridYwidth = height --* density
	--defaults.gridXwidth = width --* density
		
	--sbX:setProgress((gridXwidth/density)-32)
	--sbY:setProgress((gridYwidth/density)-32)
	
	buttSetSettingsEditor:dismiss()
	
	saveDefaultOptions()
	
	--Note("gridXwidth:" .. gridXwidth)
--	local tmp = {}
--	for i,b in pairs(buttons) do
--		tmp[i] = b.data
--	end
--		
--	PluginXCallS("saveButtons",serialize(tmp))
--	
--	PluginXCallS("saveSetDefaults",serialize(defaults))
--	
--	drawButtons()
end
seteditorDone_cb = luajava.createProxy("android.view.View$OnClickListener",setEditorDoneListener)

function saveDefaultOptions()
	local tmp = {}
	for i,b in pairs(buttons) do
		tmp[i] = b.data
	end
		
	PluginXCallS("saveButtons",serialize(tmp))
	
	PluginXCallS("saveSetDefaults",serialize(defaults))
	
	drawButtons()
end

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


function emptyButtons()
	buttons = {}
	drawButtons()
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
		topMenuItem = menu:add(0,401,401,"Button Sets")
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
