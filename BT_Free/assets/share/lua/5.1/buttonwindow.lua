 respath = package.path
respath = string.sub(respath,0,string.find(respath,"?")-1).."res"

require("button")
require("serialize")
require("bit")
local marshal = require("marshal")
defaults = nil

local props = require("config")

debugInfo = false

local function debugString(string)
	if(debugInfo) then
		Note(string.format("\n%s\n",string))
	end
end

debugString("Button Window Script Loading...")

density = GetDisplayDensity()
Configuration = luajava.bindClass("android.content.res.Configuration")

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
Context = luajava.bindClass("android.content.Context")
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
		
	PluginXCallS("saveSetDefaults",serialize(defaults))
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
			c:drawLine( gridXwidth*x,0,gridXwidth*x,height,dpaint)
		end
		
		times = height / gridYwidth
		for y=1,times do
			c:drawLine(0,gridYwidth*y,width,gridYwidth*y,dpaint)
		end

end


gridXwidth = 40 * density --67 * density
gridYwidth = 40 * density --67 * density

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
	
	--for key,value in pairs(luajava) do
  --	Note("\npre found member " .. key);
	--end
	if(buttonSetListDialog == nil) then
		buttonSetListDialog = require("buttonlist")
		buttonSetListDialog.init(mContext)
	end
	--Note("Showing new list\n")
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
	buttonSetListDialog.updateButtonListDialogNoItems()
	emptyButtons()
end


function buttonOptions()
  local editorValues = {}
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
  
  editorValues.gridX = gridXwidth / density
  editorValues.gridY = gridYwidth / density
  editorValues.gridOpacity = manageropacity
  editorValues.gridIntersectionTest = intersectMode
  editorValues.gridSnap = gridsnap
  

  local editorOptionsDialog = require("editoroptionsdialog")
  editorOptionsDialog.init(mContext)
  editorOptionsDialog.setEditorDoneCallback(function(tmp)
    --v is a table with the values from the setPropertiesEditor as well as the things that are handled below but those are handled responsively
    
    
    --update old button sizes to new button sizes.
    for i=1,#buttons do
      local b = buttons[i]

      local data = b.data
      --local meta = getmetatable(data)
      --local index = meta.__index
      
      local compare = function(x,c,z)
        return tonumber(rawget(x,c)) == tonumber(z)
      end
      
      if(compare(data,"width",defaults.width)) then
        --Note("\n\nSPECIAL WIDTH CLEAR\n\n")
        rawset(b.data,"width",nil)
      end
      
      if(compare(data,"height",defaults.height)) then
        --Note("\n\nSPECIAL LABEL CLEAR\n\n")
        rawset(b.data,"height",nil)
      end
      
      if(compare(data,"labelSize",defaults.labelSize)) then
        rawset(b.data,"labelSize",nil)
      end
      --counter = counter + 1
      if(compare(data,"primaryColor",defaults.primaryColor)) then
        rawset(b.data,"primaryColor",nil)
      end
      
      if(compare(data,"flipColor",defaults.flipColor)) then
        rawset(b.data,"flipColor",nil)
      end
      
      if(compare(data,"selectedColor",defaults.selectedColor)) then
        rawset(b.data,"selectedColor",nil)
      end
      
      if(compare(data,"labelColor",defaults.labelColor)) then
        rawset(b.data,"labelColor",nil)
      end
      
      if(compare(data,"flipLabelColor",defaults.flipLabelColor)) then
        rawset(b.data,"flipLabelColor",nil)
      end
      
     
    end
    
    defaults.width = tmp.width
    defaults.height = tmp.height
    defaults.primaryColor = tmp.normalColor
    defaults.flipColor = tmp.flipColor
    defaults.selectedColor = tmp.pressedColor
    defaults.labelColor = tmp.normalLabelColor
    defaults.flipLabelColor = tmp.pressedLabelColor
    defaults.labelSize = tmp.labelSize
    
    for i=1,#buttons do
      local b = buttons[i]
      b:updateRect(statusoffset)     
    end
    
    --call redraw buttons to get any new colors in there.
    drawButtons()
    view:invalidate()
    
  end)
  editorOptionsDialog.setGridSnapCallback(function(v)
    gridsnap = v
  end)
  editorOptionsDialog.setGridXSpacingCallback(function(v)
    gridXwidth = v*density
    defaults.gridXwidth = v
    drawManagerGrid()
    view:invalidate()
  end)
  editorOptionsDialog.setGridYSpacingCallback(function(v)
    gridYwidth = v*density
    defaults.gridYwidth = v
    drawManagerGrid()
    view:invalidate()
  end)
  editorOptionsDialog.setGridOpacityCallback(function(v)
    dpaint:setAlpha(v)
    manageropacity = v
    drawManagerGrid()
    view:invalidate()
  end)
  editorOptionsDialog.setGridSnapTestCallback(function(v)
    intersectMode = v
  end)
  
  editorOptionsDialog.showDialog(editorValues)
  return

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
	local newb = BUTTON:new({x=pX,y=pY,label=""},density)
	--newb.x = x
	--newb.y = y
	--next two lines seem to be messing with the defaults.
	--newb.data.width = defaults.width --(gridXwidth-5)/density
	--newb.data.height = defaults.height --(gridYwidth-5)/density
	--newb.data.label = "newb"..counter
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

--[[BEGIN GLOBAL ENTRY POINT INTO buttonEditorDone
    This is called from the buttoneditor module to process the new button data
    Leave this global
]]
function buttonEditorDone(data)
	--apply the settings out.
	
	if(numediting == 1) then
		local tmp = buttons[lastselectedindex]

		
		--Note("EDITING SINGLE BUTTON BEFORE BUTTON:"..tmp.data.height)
		--printTable("button",tmp)
		
		
		tmp.data.x = data.xCoord
		tmp.data.y = data.yCoord
		tmp.data.height = data.height
		tmp.data.width = data.width
		tmp.data.labelSize = data.labelSize
		
		tmp.data.primaryColor = data.normalColor
		tmp.data.flipColor = data.flipColor
		tmp.data.selectedColor = data.pressedColor
		tmp.data.labelColor = data.normalLabelColor
		tmp.data.flipLabelColor = data.flipLabelColor
		
		tmp.data.command = data.cmd
		tmp.data.label = data.label
		tmp.data.flipLabel = data.flipLabel
		tmp.data.flipCommand = data.flipCmd
		
		tmp.data.name = data.name
		tmp.data.switchTo = data.target
		
		tmp:updateRect(statusoffset)
		--Note("EDITING SINGLE BUTTON AFTER BUTTON:"..tmp.data.height)
		--printTable("edited",tmp)
		
	elseif(numediting > 1) then
		for i,b in ipairs(buttons) do
			if(b.selected == true) then
				--do the settings update for relevent data
				if(data.width ~= nil and data.width ~= editorValues.width) then
					b.data.width = data.width
				end
				
				if(data.height ~= nil and data.height ~= editorValues.height) then
					b.data.height = data.height
				end
				
				if(data.xCoord ~= nil and data.xCoord ~= editorValues.x) then
					b.data.x = data.xCoord
				end
				
				if(data.yCoord ~= nil and data.yCoord ~= editorValues.y) then
					b.data.y = data.yCoord
				end
				
				if(data.labelSize ~= nil and data.labelSize ~= editorValues.labelSize) then
					b.data.labelSize = data.labelSize
				end
				
				if(data.normalColor ~= editorValues.primaryColor) then
					b.data.primaryColor = data.normalColor
				end
				
				if(data.pressedColor ~= editorValues.selectedColor) then
					b.data.selectedColor = data.pressedColor
				end
				
				if(data.flipColor ~= editorValues.flipColor) then
					b.data.flipColor = data.flipColor
				end
				
				if(data.normalLabelColor ~= editorValues.labelColor) then
					b.data.labelColor = data.normalLabelColor
				end
				
				if(data.flipLabelColor ~= editorValues.flipLabelColor) then
					b.data.flipLabelColor = data.flipLabelColor
				end
				
				b:updateRect(statusoffset)
			end
		end
	end
	
	drawButtons()
	view:invalidate()
end
--[[END buttonEditorDone global callback]]
normalColor = nil
flipColor = nil
pressedColor = nil
normalLabelColor = nil
flipLabelColor = nil

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
	
	
 local buttonEditor = require("buttoneditor")
 buttonEditor.init(mContext)
 --Note("showing button editor "..numediting)
 buttonEditor.showEditorDialog(editorValues,numediting)
 return
end

modifyButtonSet = function(entry) 
  --Note("In Modify button set callback.")
  if(buttonsCleared) then
    revertButtons()
  end
  if(entry.name ~= lastLoadedSet) then
    PluginXCallS("loadAndEditSet",entry.name)
    return
  end

  enterManagerMode()
  showeditormenu = true
  PushMenuStack("onEditorBackPressed")
end

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

--delete after testing
setSettingsButtonListener = {}
function setSettingsButtonListener.onClick(v)

  local editorValues = {}
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
  
  local editorOptionsDialog = require("editoroptionsdialog")
  editorOptionsDialog.init(mContext)
  editorOptionsDialog.setEditorDoneCallback()
  editorOptionsDialog.showDialog(editorValues)  

end
setSettingsButton_cb = luajava.createProxy("android.view.View$OnClickListener",setSettingsButtonListener)

--keep below for handling the data coming back from the advanced editor
setEditorCancelListener = {}
function setEditorCancelListener.onClick(v)
	buttSetSettingsEditor:dismiss()
end
seteditorCancel_cb = luajava.createProxy("android.view.View$OnClickListener",setEditorCancelListener)

--delete after testing
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
	buttonRoundness = tonumber(options.roundness) * density
	--Note("options loaded, roundess="..buttonRoundness)
	--clearButtons()
	drawButtons()
	view:invalidate()
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
revertButtonData.width = 70 --this unit is in dips, density is applied later
revertButtonData.height = 40 --same here.
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
		topMenuItem = menu:add(0,401,401,props.label)

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
    PluginXCallS("getButtonSetList","all")
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
	xpcall(buttonOptions,function(error) if error ~= nil then Note(error) end end) 
	
	
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
	
	local bmp = target:getBitmap()
	local Bitmap = luajava.bindClass("android.graphics.Bitmap")
	--scale to a bitmap of the appropriate size 40x40 dip? ish
	local resize = luajava.newInstance("android.graphics.drawable.BitmapDrawable",resources,Bitmap:createScaledBitmap(bmp,40*density,40*density,true))
	
	
	return resize
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
