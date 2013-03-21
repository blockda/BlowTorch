
--
-- Top level utility constants
respath = GetPluginInstallDirectory().."/icons"
local context = view:getContext()
local density = GetDisplayDensity()
local chatOutputView = view

local current_version = 14

local luajava = luajava
--
-- Classes needed by the script to do android-esque ui things.
local System = luajava.bindClass("java.lang.System")
local View = luajava.bindClass("android.view.View")
local Context = luajava.bindClass("android.content.Context")
local HorizontalScrollView = luajava.bindClass("android.widget.HorizontalScrollView")
local Drawable = luajava.bindClass("android.graphics.drawable.Drawable")
local RelativeLayoutParams = luajava.bindClass("android.widget.RelativeLayout$LayoutParams")
local RelativeLayout = luajava.bindClass("android.widget.RelativeLayout")
local LinearLayout = luajava.bindClass("android.widget.LinearLayout")
local LinearLayoutParams = luajava.bindClass("android.widget.LinearLayout$LayoutParams")
local MotionEvent = luajava.bindClass("android.view.MotionEvent")
local Array = luajava.bindClass("java.lang.reflect.Array")
local Color = luajava.bindClass("android.graphics.Color")
local File = luajava.bindClass("java.io.File")
local BitmapDrawable = luajava.bindClass("android.graphics.drawable.BitmapDrawable")
local ImageButton = luajava.bindClass("android.widget.ImageButton")
local Button = luajava.bindClass("android.widget.Button")
local TranslateAnimation = luajava.bindClass("android.view.animation.TranslateAnimation")
local AnimatedRelativeLayout = luajava.bindClass("com.offsetnull.bt.window.AnimatedRelativeLayout")
local RectF = luajava.bindClass("android.graphics.RectF")
local RoundRectShape = luajava.bindClass("android.graphics.drawable.shapes.RoundRectShape")
local ShapeDrawable = luajava.bindClass("android.graphics.drawable.ShapeDrawable")
local LayerDrawable = luajava.bindClass("android.graphics.drawable.LayerDrawable")
local StateListDrawable = luajava.bindClass("android.graphics.drawable.StateListDrawable")
local R_attr = luajava.bindClass("android.R$attr")
local Class = luajava.bindClass("java.lang.Class")
local Gravity = luajava.bindClass("android.view.Gravity")
--
-- Utility 'java primitive array' creation code
local Integer = luajava.bindClass("java.lang.Integer")
local Float = luajava.bindClass("java.lang.Float")
local Byte = luajava.bindClass("java.lang.Byte")

local IntegerInstance = luajava.new(Integer,0)
local IntegerClass = IntegerInstance:getClass()
local RawInteger = IntegerClass.TYPE

local FloatInstance = luajava.new(Float,0)
local FloatClass = FloatInstance:getClass()
local RawFloat = FloatClass.TYPE

local ByteInstance = luajava.new(Byte,0)
local ByteClass = ByteInstance:getClass()
local RawByte = ByteClass.TYPE

local function makeFloatArray(table)
	local newarray = Array:newInstance(RawFloat,#table)
	for i,v in ipairs(table) do
		local index = i-1
		local floatval = luajava.new(Float,v)
		Array:setFloat(newarray,index,floatval:floatValue())
	end
	
	return newarray
end

local function makeIntArray(table)
	local newarray = Array:newInstance(RawInteger,#table)
	for i,v in ipairs(table) do
		local index = i-1
		local intval = luajava.new(Integer,v)
		Array:setInt(newarray,index,intval:intValue())
	end
	
	return newarray
end

local function makeEmptyIntArray()
	local newarray = Array:newInstance(RawInteger,0)
	return newarray
end

--
-- Utility function to do dpi-bucket aware path lookup for icon images
resources = chatOutputView:getContext():getResources()
function resLoader(root,bmp)
	local target = nil
	local metrics = resources:getDisplayMetrics()
	
	local d = metrics.density
	if(d < 1.0) then
		target = luajava.new(BitmapDrawable,resources,root.."/ldpi/"..bmp)
	elseif(d >= 1.0 and d < 1.5) then
		target = luajava.new(BitmapDrawable,resources,root.."/mdpi/"..bmp)
	elseif(d >= 1.5) then
		target = luajava.new(BitmapDrawable,resources,root.."/hdpi/"..bmp)
	end
	
	return target
end

--
-- Top level constants that will be referenced throughout the script.  
local WRAP_CONTENT = LinearLayoutParams.WRAP_CONTENT
local MATCH_PARENT = LinearLayoutParams.FILL_PARENT

local dividerHeight = math.floor(3*density);
local minHeight = chatOutputView:getLayoutParams().height + dividerHeight

local foldoutHeight = 0
local expanded = false

--
-- Construction of the "uitility bar" for the chat window, will hold the
-- non-moveable utility buttons, and the resizable, horizontally scrolling 
-- tab controller.
local wm = context:getSystemService(Context.WINDOW_SERVICE)
local display = wm:getDefaultDisplay()
local displayWidth = display:getWidth()
local displayHeight = display:getHeight()
local use = displayWidth
if(displayHeight < displayWidth) then
	use = displayHeight
end

local dpi_bucket = use / density

local uiButtonBarHeight = math.floor(35*density)
if(dpi_bucket >= 600) then
	uiButtonBarHeight = 45*density
end

local uiButtonBar = luajava.new(RelativeLayout,context)
local uiButtonBarParams = luajava.new(RelativeLayoutParams,MATCH_PARENT,WRAP_CONTENT)
--uiButtonBarParams:addRule(RelativeLayout.ABOVE,595701)
uiButtonBarParams:addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
uiButtonBar:setLayoutParams(uiButtonBarParams)
uiButtonBar:setBackgroundColor(Color:argb(255,0,0,0))
uiButtonBar:setVisibility(View.GONE)

local scrollHolder = luajava.new(HorizontalScrollView,context)
local scrollHolderParams = luajava.new(RelativeLayoutParams,MATCH_PARENT,WRAP_CONTENT)
scrollHolderParams:addRule(RelativeLayout.LEFT_OF,98)
scrollHolderParams:addRule(RelativeLayout.RIGHT_OF,101)
scrollHolderParams:addRule(RelativeLayout.ABOVE,105)
scrollHolder:setLayoutParams(scrollHolderParams)

secondDividerView = luajava.new(View,chatOutputView:getContext())
secondDividerparams = luajava.new(RelativeLayoutParams,MATCH_PARENT,dividerHeight)
secondDividerparams:addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
secondDividerView:setId(105)
secondDividerView:setLayoutParams(secondDividerparams)
secondDividerView:setBackgroundColor(Color:argb(255,68,68,136))



channelHolder = luajava.new(LinearLayout,context)
channelHolderParams = luajava.new(LinearLayoutParams,WRAP_CONTENT,WRAP_CONTENT)
channelHolder:setLayoutParams(channelHolderParams)

scrollHolder:addView(channelHolder)

local buttonMap = {}
local drawableMap = {}

--
-- Animations for the top level parent view, and the inner holder LinearLayout that holds
-- the actual text window and blue divider.
local expandAnimation = luajava.new(TranslateAnimation,0,0,-foldoutHeight,0)
local shrinkAnimation = luajava.new(TranslateAnimation,0,0,0,-foldoutHeight)
local windowMoveUpAnimation = luajava.new(TranslateAnimation,0,0,0,-uiButtonBarHeight)
local windowMoveDownAnimation = luajava.new(TranslateAnimation,0,0,0,uiButtonBarHeight)
local windowPinDownAnimation = luajava.new(TranslateAnimation,0,0,-uiButtonBarHeight,0)
local nullAnimation = luajava.new(TranslateAnimation,0,0,0,0)

expandAnimation:setDuration(450)
shrinkAnimation:setDuration(450)
windowMoveUpAnimation:setDuration(450)
windowMoveDownAnimation:setDuration(450)
nullAnimation:setDuration(450)
windowPinDownAnimation:setDuration(450)

local windowPinDownAnimationListner = {}
function windowPinDownAnimationListner.onAnimationEnd()
	uiButtonBar:setVisibility(View.GONE)
end
windowPinDownAnimationListener_cb = luajava.createProxy("android.view.animation.Animation$AnimationListener",windowPinDownAnimationListner)
windowPinDownAnimation:setAnimationListener(windowPinDownAnimationListener_cb)

function resizeMaxWindowNoAnimation()
	--Note("\ndeploying resizer"..maxHeight.."\n")
	replacementView:setDimensions(MATCH_PARENT,maxHeight)
	local p = luajava.new(LinearLayoutParams,MATCH_PARENT,maxHeight-uiButtonBarHeight-dividerHeight)
	--local p = luajava.new(LinearLayoutParams,MATCH_PARENT,maxHeight - uiButtonBarHeight - dividerHeight)
	chatOutputView:setLayoutParams(p)
	uiButtonBar:setVisibility(View.VISIBLE)
	local q = luajava.new(RelativeLayoutParams,MATCH_PARENT,WRAP_CONTENT)
	innerHolderView:setLayoutParams(q)
	replacementView:requestLayout()
	chatOutputView:requestLayout()
	expanded = true
end

--
--Hack callback chain method of getting the onAnimationEnd correctly for the AnimatedRelativeLayout top level parent view.
--oh man this needs so much documentation.
function onParentAnimationEnd()
	--Note("\nin parent animation end\n")
	if(expanded == true) then
		twidth = replacementView:getWidth()
		theight = replacementView:getHeight()
		
		theight = theight - foldoutHeight
		
		--replacementView:setDimensions(twidth,theight)
	
		local wheight = replacementView:getHeight() - uiButtonBarHeight - dividerHeight
		--Note("\nparentAnimationEnd:"..wheight..":"..theight)
		local tmpparams = luajava.new(LinearLayoutParams,replacementViewParams.width,wheight)
		chatOutputView:setLayoutParams(tmpparams)
		replacementView:setLayoutParams(replacementViewParams)
		replacementView:requestLayout()
		chatOutputView:invalidate()
		
	else
		
		--mainOutputView:setLayoutParams(mainOutputViewLayoutParams)
	end
end

--windowMoveUpAnimationListener_cb = luajava.createProxy("android.view.animation.Animation$AnimationListener",onParentAnimationEnd)

--function onAnimationEnd() 
--	if(expanded == false and pinned == true) then
--		uiButtonBar:setVisibility(View.GONE)
--	end
--end

local hider = {}
function hider.onClick(v)
	if(expanded == true) then
	
		local tmp = chatOutputView:getHeight()
		local p = luajava.new(LinearLayoutParams,MATCH_PARENT,tmp+uiButtonBarHeight)
		chatOutputView:setLayoutParams(p)
		innerHolderView:startAnimation(windowPinDownAnimation)	
		
		expanded = false
		
		foldoutHeight = 0
		
		--if(1 == 1) then
		--	return
		--end
		
		--altMainOutputViewLayoutParams = luajava.new(RelativeLayoutParams,mainOutputViewLayoutParams.width,mainOutputView:getHeight()+uiButtonBarHeight)
		--altMainOutputViewLayoutParams:addRule(RelativeLayout.ABOVE,40)
		--mainOutputView:setLayoutParams(altMainOutputViewLayoutParams)
		chatOutputView:setTextSelectionEnabled(false)
		--innerHolderView:startAnimation(windowMoveDownAnimation)
		--chatOutputView:startAnimation(nullAnimation)
		if(previousOnTop ~= nil) then
			previousOnTop:bringToFront()
		end
		if(1 == 1) then
			return
		end
		expandAnimation = luajava.new(TranslateAnimation,0,0,-foldoutHeight,0)
		shrinkAnimation = luajava.new(TranslateAnimation,0,0,0,-foldoutHeight)
		expandAnimation:setDuration(450)
		shrinkAnimation:setDuration(450)
	
		
		replacementView:startAnimation(shrinkAnimation)

		

		

		expanded = false
	else
		doExpand()
	end
end

hider_cb = luajava.createProxy("android.view.View$OnClickListener",hider)

--
--Yet More UI Construction
local rootView = chatOutputView:getParentView()
rootView:removeView(view)

replacementView = luajava.new(AnimatedRelativeLayout,chatOutputView:getContext())
replacementView:setAnimationListener(view)
replacementViewParams = chatOutputView:getLayoutParams()
chatOutputViewParams = luajava.new(RelativeLayoutParams,replacementViewParams.width,replacementViewParams.height)
replacementViewParams = luajava.new(RelativeLayoutParams,replacementViewParams.width,WRAP_CONTENT)


replacementView:setLayoutParams(replacementViewParams)
chatOutputView:setLayoutParams(chatOutputViewParams)

replacementView:setId(chatOutputView:getId())
chatOutputView:setId(59595)

--make divider
dividerView = luajava.new(View,chatOutputView:getContext())
dividerparams = luajava.new(LinearLayoutParams,MATCH_PARENT,dividerHeight)
dividerView:setId(59596)
dividerView:setLayoutParams(dividerparams)
dividerView:setBackgroundColor(Color:argb(255,68,68,136))

--linearholder = luajava.new(LinearLayout,chatOutputView:getContext())
--linearholderparams = luajava.new(RelativeLayoutParams,MATCH_PARENT,dividerHeight)
--linearholderparams:addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
--linearholder:setLayoutParams(linearholderparams)
--linearholder:setId(5995)

--secondDividerView = luajava.new(View,chatOutputView:getContext())
--secondDividerparams = luajava.new(LinearLayoutParams,MATCH_PARENT,dividerHeight)
--secondDividerparams:addRule(RelativeLayout.BELOW,uiButtonBar:getId())
--secondDividerView:setId(595701)
--secondDividerView:setLayoutParams(secondDividerparams)
--secondDividerView:setBackgroundColor(Color:argb(255,68,68,136))

--linearholder:addView(secondDividerView)

innerHolderView = luajava.new(LinearLayout,chatOutputView:getContext())
innerHolderViewLayoutParams = luajava.new(RelativeLayoutParams,replacementViewParams.width,WRAP_CONTENT)
innerHolderView:setOrientation(LinearLayout.VERTICAL)
innerHolderView:setLayoutParams(innerHolderViewLayoutParams)
innerHolderView:setId(59597)

innerHolderView:addView(chatOutputView)
innerHolderView:addView(dividerView)



replacementView:addView(uiButtonBar)

replacementView:addView(innerHolderView);
--replacementView:addView(linearholder)
rootView:addView(replacementView)

mainOutputView = rootView:findViewById(6666)
mainOutputViewLayoutParams = mainOutputView:getLayoutParams()
mainOutputViewLayoutParams:addRule(RelativeLayout.BELOW,replacementView:getId())
mainOutputViewLayoutParams:addRule(RelativeLayout.ABOVE,40)

altMainOutputViewLayoutParams = luajava.new(RelativeLayoutParams,mainOutputViewLayoutParams.width,mainOutputViewLayoutParams.height)
altMainOutputViewLayoutParams:addRule(RelativeLayout.BELOW,replacementView:getId())
altMainOutputViewLayoutParams:addRule(RelativeLayout.ABOVE,40)

mainOutputView:setLayoutParams(mainOutputViewLayoutParams)

function expandWindow()
	if(expanded == true) then
		--consume, but dont process.
		return true;
	end
	
	--Note("expanding window:")
	theight = replacementView:getHeight()
	twidth = replacementView:getWidth()
	theight = theight + foldoutHeight
	--Note("expanding window:"..theight..":"..foldoutHeight)
	
	if(theight > maxHeight) then
		local height = maxHeight - uiButtonBarHeight
		theight = maxHeight
		--foldoutHeight = 0
		
		if(chatOutputView:getHeight() > (maxHeight - dividerHeight)) then
		--Note("window height is greater than max")
		--ScheduleCallback(101,"resizeMaxWindowNoAnimation",300)
		expanded = true
		return
	end
	end
	

	--Note("expanding window:"..theight)
	
	replacementView:setDimensions(MATCH_PARENT,tonumber(theight))
	
	altMainOutputViewLayoutParams = luajava.new(RelativeLayoutParams,mainOutputViewLayoutParams.width,mainOutputView:getHeight())
	altMainOutputViewLayoutParams:addRule(RelativeLayout.ABOVE,40)
	
	--mainOutputView:setLayoutParams(altMainOutputViewLayoutParams)
	
	--resizeButton:setEnabled(true)
	
	local numchildren = tonumber(rootView:getChildCount())
	previousOnTop = rootView:getChildAt(numchildren-1)
	rootView:bringChildToFront(replacementView)
	replacementView:startAnimation(nullAnimation)
	expanded = true

	innerHolderView:startAnimation(windowMoveUpAnimation)
	--chatOutputView:startAnimation(windowMoveUpAnimation)
		
	uiButtonBar:setVisibility(View.VISIBLE)

	chatOutputView:requestLayout()
	replacementView:requestLayout()

	return true;
end


local longPressListener = {}
local moveDelta = 0
local moveTotal = 0
local moveLast = 0
function longPressListener.onTouch(v,e)
	action = e:getAction()
	if(action == MotionEvent.ACTION_DOWN) then
		ScheduleCallback(100,"doExpand",1000)
		moveLast = e:getY()
		moveTotal = 0
		v:onTouchEvent(e)
		return true
	elseif( action == MotionEvent.ACTION_MOVE) then
		moveDelta = e:getY() - moveLast
		moveLast = e:getY()
		moveTotal = moveTotal + moveDelta
		if(moveTotal > 30) then
			CancelCallback(100)
		end
		v:onTouchEvent(e)
		return true
	elseif(action == MotionEvent.ACTION_UP) then
		v:onTouchEvent(e) 
		moveTotal = 0
		CancelCallback(100)
		return true
	end
	return false
end
longPressListener_cb = luajava.createProxy("android.view.View$OnTouchListener",longPressListener)
chatOutputView:setOnTouchListener(longPressListener_cb)
chatOutputView:setTextSelectionEnabled(false)

function doExpand(id)
	expandWindow()
	chatOutputView:setTextSelectionEnabled(true)
end


function loadButtons(input)
	uiButtonBar:removeAllViews()
	channelHolder:removeAllViews()
	
	--add the default buttons.
	uiButtonBar:addView(hideButton)
	uiButtonBar:addView(mainButton)
	
	uiButtonBar:addView(scrollHolder)
	
	uiButtonBar:addView(resizeButton)
	--uiButtonBar:addView(delButton)
	
	uiButtonBar:addView(secondDividerView)
	
	--reconstruct the channel list table
	local data = loadstring(input)()
	selected = data.selected
	local list = data.list
	local counter = 1
	local lastbutton = nil
	
	--
	
	local a = {}
    for i,n in pairs(list) do table.insert(a, i) end
    table.sort(a)
    --for i,n in ipairs(a) do print(n) end
	
	for i,n in ipairs(a) do
		if(n ~= "main") then
			local newbutton = generateNewButton(n,counter)

			local params = newbutton:getLayoutParams()
			params:setMargins(2,0,2,0)
			counter = counter + 1
			buttonMap[n] = newbutton
			
			channelHolder:addView(newbutton)
			lastbutton = newbutton
		else

		end
	end
	
	if(lastbutton ~= nil) then
		local lparams = lastbutton:getLayoutParams()
		lparams:setMargins(0,0,2,0)
		uiButtonBar:requestLayout()
	end
	
	local drawable = drawableMap[selected]
	
	local dpaint = drawable:getPaint()
	dpaint:setARGB(255,50,50,200)
end



local cornerRadii = {}
table.insert(cornerRadii,0)
table.insert(cornerRadii,0)
table.insert(cornerRadii,0)
table.insert(cornerRadii,0)
table.insert(cornerRadii,8)
table.insert(cornerRadii,8)
table.insert(cornerRadii,8)
table.insert(cornerRadii,8)
	
local radii = makeFloatArray(cornerRadii)

local inset = luajava.new(RectF)
inset:set(5,0,5,5)
	
local function makeTabDrawable(label,a,r,g,b)
	local rectShape = luajava.new(RoundRectShape,radii,nil,nil)
	
	local shapeDrawable = luajava.new(ShapeDrawable,rectShape)
	drawableMap[label] = shapeDrawable

	
	local insetShape = luajava.new(RoundRectShape,radii,inset,radii)
	
	local insetDrawable = luajava.new(ShapeDrawable,insetShape)
	local ipaint = insetDrawable:getPaint()
	ipaint:setARGB(255,150,150,150)
	
	insetDrawable:setPadding(10,0,10,0)
	local layers  = Array:newInstance(Drawable,2)
	Array:set(layers,0,shapeDrawable)
	Array:set(layers,1,insetDrawable)
	
	local layer = luajava.new(LayerDrawable,layers)
	
	
	local rPaint = shapeDrawable:getPaint()
	rPaint:setARGB(a,r,g,b)
	return layer

end



local function makeStateDrawable(label)
	local stater = luajava.new(StateListDrawable)
	local pre = makeTabDrawable(label,255,100,0,0)

	local norm = makeTabDrawable(label,255,0,0,100)

	local pretmp = {}
	table.insert(pretmp,R_attr.state_pressed)
	local pressed = makeIntArray(pretmp)
	stater:addState(pressed,pre)
	
	local default = makeEmptyIntArray()
	stater:addState(default,norm)
	
	return stater
end

function generateNewButton(name,grav)
	local button = luajava.new(Button,context)
	params = luajava.new(LinearLayoutParams,WRAP_CONTENT,uiButtonBarHeight)
	button:setId(grav)
	button:setText(name)
	button:setTextSize(12*density)
	button:setLayoutParams(params)
	button:setOnClickListener(clicker_cb)
	button:setOnLongClickListener(longclicker_cb)
	button:setTextColor(Color:argb(255,150,150,150))
	local shape = makeStateDrawable(name)
	
	button:setBackgroundDrawable(shape)
	
	return button
end
hideButton = luajava.new(ImageButton,context)
hideParams = luajava.new(RelativeLayoutParams,WRAP_CONTENT,uiButtonBarHeight)
hideParams:addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
hideParams:addRule(RelativeLayout.ABOVE,105)
hideButton:setImageDrawable(resLoader(respath,"hide.png"))
hideButton:setId(100)

hideButton:setLayoutParams(hideParams)
hideButton:setOnClickListener(hider_cb)

mainButton = luajava.new(Button,context)
mainParams = luajava.new(RelativeLayoutParams,WRAP_CONTENT,uiButtonBarHeight)
mainParams:addRule(RelativeLayout.LEFT_OF,100);
mainParams:addRule(RelativeLayout.ABOVE,105)
mainParams:setMargins(2,0,0,0)
mainButton:setId(98)
mainButton:setText("main")
mainButton:setTextSize(12*density)
mainButton:setTextColor(Color:argb(255,150,150,150))
mainButton:setLayoutParams(mainParams)
mainButtonShape = makeStateDrawable("main")
mainButton:setBackgroundDrawable(mainButtonShape)
buttonMap["main"] = mainButton


resizeButton = luajava.new(ImageButton,context)
resizeParams = luajava.new(RelativeLayoutParams,WRAP_CONTENT,uiButtonBarHeight)
resizeParams:addRule(RelativeLayout.ALIGN_PARENT_LEFT)
resizeParams:addRule(RelativeLayout.ABOVE,105)
resizeButton:setImageDrawable(resLoader(respath,"resize.png"))
resizeButton:setId(101)

resizeButton:setLayoutParams(resizeParams)

selectedStatetmp = {}
table.insert(selectedStatetmp,R_attr.state_focused)
selectedState = makeIntArray(selectedStatetmp)
	
normalState = makeEmptyIntArray()

clicker = {}
function clicker.onClick(v)
	local label = v:getText()


	local drawable = drawableMap[label]
	
	local dpaint = drawable:getPaint()
	dpaint:setARGB(255,50,50,200)

	for i,b in pairs(drawableMap) do
		if(i ~= label) then
			local tmp = drawableMap[i]
			local tmppaint = tmp:getPaint()
			tmppaint:setARGB(225,0,0,100)
			local tview = buttonMap[i]
			tview:invalidate()
		end
	end
	v:invalidate()
	PluginXCallS("updateSelection",label)
end
clicker_cb = luajava.createProxy("android.view.View$OnClickListener",clicker)
mainButton:setOnClickListener(clicker_cb)

longclicker = {}
function longclicker.onLongClick(v)
	label = v:getText()
	SendToServer(".kb popup "..label.." ")
	return true
end
longclicker_cb = luajava.createProxy("android.view.View$OnLongClickListener",longclicker)

--set up the resizer button.
touchStartY = 0
touchStartTime = 0
resizer = {}
touchFinger = nil
function resizer.onTouch(v,e)
	
	
	action = e:getActionMasked()
	--if(action == MotionEvent.ACTION_POINTER_DOWN) then return true end
	if(action == MotionEvent.ACTION_DOWN) then
		--debugPrint("resizer down"
		--if(touchFinger == nil) then touchFinger = e:getPointerId(0) end
		--if(e:getPointerId(0) ~= touchFinger) then return false end
		
		touchStartY = e:getY()
		local tmp = replacementView:getHeight();
		local tmp2 = chatOutputView:getHeight();
		--debugPrint("resizer starting: height:"..tmp2.." parentHeight:"..tmp)
		fingerdown = true
		--touchFinger = e:getId()
		touchStartTime = System:currentTimeMillis()
		chatOutputView:invalidate()
		return true
	end
	
	if(action == MotionEvent.ACTION_MOVE) then
		now = System:currentTimeMillis()
		timeDelta = now - touchStartTime
		if(timeDelta < 30) then
			return true
		end
		touchStartTime = now
		
		
		local tmp = replacementView:getHeight();
		local tmp2 = chatOutputView:getHeight();
		--debugPrint("resizer start_moving: height:"..tmp2.." parentHeight:"..tmp)
			
		
		delta = e:getY() -  touchStartY
		if(delta ~= 0) then
			--resize window.
			twidth = chatOutputView:getWidth()
			theight = chatOutputView:getHeight() + delta
			wwidth = replacementView:getWidth()
			wheight = replacementView:getHeight() + delta
			if(wheight > maxHeight) then
				wheight = maxHeight
				theight = maxHeight - uiButtonBarHeight - dividerHeight
			end
			
			local min = minHeight
			--if(pinned == true) then
				min = min - uiButtonBarHeight - dividerHeight
			--end
			if(theight < min) then
				theight = min
				wheight = min+uiButtonBarHeight + dividerHeight
			end
			
			replacementView:setDimensions(MATCH_PARENT,wheight)
			
			--if(pinned == false) then
				--foldoutHeight = foldoutHeight + delta
				--if(foldoutHeight < 0) then
				--	foldoutHeight = 0
				--end
			--end
			
			
			--Note("resizer moving:"..delta.." calculated new height:"..min.." parentHeight:"..chatOutputView:getTop().."\n")
			local tmpparams = luajava.new(LinearLayoutParams,MATCH_PARENT,theight);
			--view:setDimensions(tonumber(twidth),tonumber(theight))
			--chatOutputView:invalidate()
			chatOutputView:setLayoutParams(tmpparams)
			chatOutputView:requestLayout()
			replacementView:requestLayout()
			
		end
		toucheStartY = e:getY()
		chatOutputView:invalidate()
		return true
	end
	
	if(action == MotionEvent.ACTION_UP) then
		fingerdown = false
		touchFinger = nil
		expandAnimation = luajava.new(TranslateAnimation,0,0,-foldoutHeight,0)
		shrinkAnimation = luajava.new(TranslateAnimation,0,0,0,-foldoutHeight)
		expandAnimation:setDuration(450)
		shrinkAnimation:setDuration(450)
		chatOutputView:invalidate()
		return true
	end
	return true
end
resizer_cb = luajava.createProxy("android.view.View$OnTouchListener",resizer)
resizeButton:setOnTouchListener(resizer_cb)

fingerdown = false

maxHeight = 0
function OnSizeChanged(neww,newh,oldw,oldh)

	if(not fingerdown) then
		--update min height
		maxHeight = inputbar:getTop()
		if(replacementView:getHeight() > maxHeight) then			
			ScheduleCallback(101,"resizeMaxWindowNoAnimation",300)
			return
		end
		chatOutputView:invalidate()
	else
		chatOutputView:invalidate()
	end
	
	
end

PluginXCallS("initReady","now")

function OnCreate()
	--EchoText("in create")
	--debugPrint("OnCreate for CHATWINDOW"..minHeight)
	inputbar = rootView:findViewById(10)
	
	maxHeight = inputbar:getTop()
	--EchoText("in create, max height is: "..maxHeight)
	
	--PluginXCallS("EchoText",string.format("Chat Plugin v1.%d Loaded...\n\n",current_version))
end

--function 


function setWindowSize(size)
	

	size = tonumber(size)
	chatOutputViewParams = luajava.new(LinearLayoutParams,MATCH_PARENT,size)
	minHeight = size + dividerHeight
	parentParams = luajava.new(RelativeLayoutParams,MATCH_PARENT,size+dividerHeight+uiButtonBarHeight)
	replacementView:setLayoutParams(parentParams)
	chatOutputView:setLayoutParams(chatOutputViewParams)
	chatOutputView:requestLayout()
	replacementView:requestLayout()
	
end

function makeRunner()
	runner = {}
	function runner.run()
	

		
		--start version check
		EchoText("\nChecking for updates...\n\n")
		local url = luajava.newInstance("java.net.URL","http://bt.happygoatstudios.com/test/chat_plugin_version");
	
		local c = url:openConnection()
	
		c:setRequestMethod("GET")
		c:setDoOutput(true)
		c:connect()
	
		

		reader = luajava.newInstance("java.io.InputStreamReader",c:getInputStream())
		
		bufferedreader = luajava.newInstance("java.io.BufferedReader",reader)
		
		line = bufferedreader:readLine()
		
		--Note(string.format("Server chat plugin version is: %s",line))
		
		c:disconnect()
		
		url = nil
		
		server_version = tonumber(line)
		
		if(current_version < server_version) then
			--need to download and unpack
				
			local foo = function()
				local url = luajava.newInstance("java.net.URL","http://bt.happygoatstudios.com/test/chat_plugin_package.zip");
	
				local c = url:openConnection()
				c:setDefaultUseCaches(false)
				c:setUseCaches(false)
				c:setRequestProperty("Cache-Control", "no-cache");
				c:setRequestMethod("GET")
				c:setDoOutput(true)
				
				c:connect()
			
				local size = c:getContentLength()
			
				local b = luajava.array(RawByte,1024)

				local istream = c:getInputStream()
				local outputFile = luajava.newInstance("java.io.File",string.format("%s/update.zip",GetPluginInstallDirectory()))
				local ostream = luajava.newInstance("java.io.FileOutputStream",outputFile)
			
				EchoText("\nOutput file location:"..outputFile:getPath().."\n\n")
			
				local length = istream:read(b)
			
				while(length ~= -1) do
					ostream:write(b,0,length)
					length = istream:read(b)
				end
			
				ostream:close()
				c:disconnect()
			
				local fis = luajava.newInstance("java.io.FileInputStream",outputFile)
				local bis = luajava.newInstance("java.io.BufferedInputStream",fis)
				local zis = luajava.newInstance("java.util.zip.ZipInputStream",bis)
				local entry = zis:getNextEntry()
			
				while(entry ~= nil) do
					if(entry:isDirectory()) then
						--Note("\nentry is a directory\n")
					else
						local file = entry:getName()
						local path = string.format("%s/%s",GetPluginInstallDirectory(),file)
						local oldpath = string.format("%s/%s.old",GetPluginInstallDirectory(),file)
						EchoText(string.format("\nDecompressing: %s\n\n",path))
						local testfile = luajava.newInstance("java.io.File",path)
						local oldfile = luajava.newInstance("java.io.File",oldpath)
						if(testfile:exists()) then
							EchoText("\nfile exists, renaming to"..oldpath.."\n")
							testfile:renameTo(oldfile)
						else
							local parent = testfile:getParentFile()
							if(parent:exists()) then
							
							else
								parent:mkdirs()
							end
						end
					
						local realfile = luajava.newInstance("java.io.FileOutputStream",path)
						local count = zis:read(b)
						while(count ~= -1) do
							realfile:write(b,0,count)
							count = zis:read(b)
						end
				
						realfile:close()

					end
					zis:closeEntry()
					entry = zis:getNextEntry()
				end
			
				zis:close()
			end --end local function encapsulation
			
			local status,err = pcall(foo,debug.traceback)
			if(status) then
				--clean up old files
				local clean = function(file)
					file:delete()
					EchoText("\nDeleting: ".. file:getAbsolutePath().."\n")
				end
				cleanDirectory(GetPluginInstallDirectory(),clean)
				
				EchoText("\nFinished update, plugin settings must be reloaded.")
				
				CloseOptionsDialog()
				runOnUIThread(function() showUpdateDoneDialog() end)
			else
				--rename old files back to original
				EchoText("Error Decompressing:\n"..err)
				local revert = function(file)
					local path = file:getAbsolutePath()
					local mod = path:sub(0,-4)
					EchoText(string.format"\nreverting file: %s\n",mod)
					local nfile = luajava.newInstance("java.io.File",mod)
					if(nfile:exists()) then
						nfile:delete()
					end
					file:renameTo(nfile)
				end
				
				cleanDirectory(GetPluginInstallDirectory(),revert)
			end
			
		else
			runOnUIThread(function() showNoUpdateDialog() end)
			EchoText("\nChat plugin is up to date.\n")
		end
		
		--download and unpack zip file
	end
	runner_proxy = luajava.createProxy("java.lang.Runnable",runner)
	runner_thread = luajava.newInstance("java.lang.Thread",runner_proxy)
end

function cleanDirectory(path,objective)
	local f = luajava.newInstance("java.io.File",path)
	if(f:isDirectory()) then
		--make the filename filter
		local tmp = {}
		function tmp.accept(file)
			local ret = false;
			local tmpfile = file:getAbsolutePath()
			
			if(tmpfile:match("%.old$")) then
				ret = true
			end
			
			if(file:isDirectory()) then
				ret = true
			end
			return ret
		end
		
		local filter = luajava.createProxy("java.io.FileFilter",tmp)
		local list = f:listFiles(filter)
		local count = Array:getLength(list)
		
		for i=0,count-1 do
			local file = Array:get(list,i)
			if(file:isDirectory()) then
				cleanDirectory(file:getAbsolutePath(),objective)
			else
				objective(file)
			end
		end
		
	else
	--shouldn't really be called on things that arent directories.
	end
end

function runDatRunner()
	makeRunner()
	runner_thread:start()
end

function runOnUIThread(f)
	--Note("\nAttempting to run on ui thread\n\n")
	local tmp = {}
	tmp.run = f
	alertrunner = luajava.createProxy("java.lang.Runnable",tmp)
	local activity = GetActivity()
	activity:runOnUiThread(alertrunner)
	--Note("\nFinished running in background\n\n")
end

function showUpdateDoneDialog()
	--PluginXCallS("EchoText","\nBuilding foreground UI\n\n")
	local builder = luajava.newInstance("android.app.AlertDialog$Builder",context)
	builder:setTitle("Update Successful")
	builder:setMessage("The chat plugin has been successfully updated. Settings must be reloaded for changes to take effect.")
	
	local done = {}
	function done.onClick(dialog,which)
		PluginXCallS("finishUpdate")
		dialog:dismiss()
	end

	local done_cb = luajava.createProxy("android.content.DialogInterface$OnClickListener",done)
	builder:setPositiveButton("Reload Settings",done_cb)
	progress_dialog = builder:create()
	progress_dialog:show()		
end

function showNoUpdateDialog()
	local builder = luajava.newInstance("android.app.AlertDialog$Builder",context)
	builder:setTitle("No Update Available")
	builder:setMessage("The chat plugin is up to date.")
	
	local done = {}
	function done.onClick(dialog,which)
		dialog:dismiss()
	end

	local done_cb = luajava.createProxy("android.content.DialogInterface$OnClickListener",done)
	builder:setPositiveButton("Done",done_cb)
	progress_dialog = builder:create()
	progress_dialog:show()	
end

function EchoText(text)
	PluginXCallS("EchoText",text)
end
