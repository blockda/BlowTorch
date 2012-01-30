--debugPrint("package path:"..package.path)
package.path = "/mnt/sdcard/BlowTorch/?.lua"

--make a button.
debugPrint("in the chat window")
context = view:getContext()

RelativeLayoutParams = luajava.bindClass("android.widget.RelativeLayout$LayoutParams")
RelativeLayout = luajava.bindClass("android.widget.RelativeLayout")

--LinearLayout = luajava.bindClass("android.widget.LinearLayout")
--LinearLayoutParams = luajava.bindClass("android.widget.LinearLayout$LayoutParams")

debugPrint("bound necessary classes")

--make holder view for buttons.
--LinearLayout = luajava.bindClass("android.widget.LinearLayout")

uiButtonBar = luajava.new(RelativeLayout,context)
uiButtonBarParams = luajava.newInstance("android.widget.RelativeLayout$LayoutParams",RelativeLayoutParams.FILL_PARENT,RelativeLayoutParams.WRAP_CONTENT)
uiButtonBarParams:addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
--params:addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
uiButtonBar:setLayoutParams(uiButtonBarParams)
--uiButtonBar:setOrientation(LinearLayout.HORIZONTAL)

--button = luajava.newInstance("android.widget.Button",context)
--params = luajava.newInstance("android.widget.RelativeLayout$LayoutParams",LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT)
--params:addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
--params:addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
--debugPrint("created instances")
--button:setText("LUABUTTON")
--button:setTextSize(26)
--button:setLayoutParams(params)
--debugPrint("set settings")

--set up the animations.
expandAnimation = luajava.newInstance("android.view.animation.TranslateAnimation",0,0,-100,0)
shrinkAnimation = luajava.newInstance("android.view.animation.TranslateAnimation",0,0,0,-100)

windowMoveUpAnimation = luajava.newInstance("android.view.animation.TranslateAnimation",0,0,0,-50)
windowMoveDownAnimation = luajava.newInstance("android.view.animation.TranslateAnimation",0,0,0,50)


expandAnimation:setDuration(450)
shrinkAnimation:setDuration(450)
windowMoveUpAnimation:setDuration(450)
windowMoveUpAnimation:setFillAfter(false)
windowMoveDownAnimation:setDuration(450)
windowMoveDownAnimation:setFillAfter(false)

toggle = false

hider = {}
function hider.onClick(v)
	if(toggle == true) then
		debugPrint("shrink");
		parentView:startAnimation(shrinkAnimation)
		if(pinned == false) then
			view:startAnimation(windowMoveDownAnimation)
		end
		toggle = false
	end
end

function onParentAnimationEnd()
	debugPrint("in onParentAnimationEnd()")
	if(toggle == false) then
		width = view:getWidth()
		height = view:getHeight()
		height = height - 100
		view:updateDimensions(width,height)
	end
end

function onAnimationEnd()
	
	debugPrint("in onAnimationEnd()")
	params = view:getLayoutParams()
	if(toggle) then
		params:setMargins(0,-50,0,0)
	else
		params:setMargins(0,0,0,0)
	end
	view:setLayoutParams(params)
	view:requestLayout()
end

hider_cb = luajava.createProxy("android.view.View$OnClickListener",hider)
--button:setOnClickListener(toggler_cb)
view:addView(uiButtonBar)

debugPrint("added button")

parentView = view:getParentView()
parentView:bringChildToFront(view)
toucher = {}
function toucher.onLongClick(v)
	if(toggle == true) then
		--consume, but dont process.
		return true;
	end
	debugPrint("longpressed")
	debugPrint("grow");
	height = view:getHeight()
	width = view:getWidth()
	height = height + 100
	view:updateDimensions(width,height)
	parentView:bringToFront()
	parentView:startAnimation(expandAnimation)
	toggle = true
	if(pinned == false) then
		view:startAnimation(windowMoveUpAnimation)
	end
	return true;
end
toucher_cb = luajava.createProxy("android.view.View$OnLongClickListener",toucher)
view:setOnLongClickListener(toucher_cb)

function loadButtons(input)
	uiButtonBar:removeAllViews()
	
	--add the default buttons.
	uiButtonBar:addView(hideButton)
	uiButtonBar:addView(pinButton)
	uiButtonBar:addView(mainButton)
	
	--reconstruct the channel list table
	list = loadstring(input)()
	counter = 1
	for i,b in pairs(list) do
		if(i ~= "main") then
			newbutton = generateNewButton(i,counter)
			counter = counter + 1
		
			uiButtonBar:addView(newbutton)
		end
	end
	
	uiButtonBar:requestLayout()
end

function generateNewButton(name,grav)
	button = luajava.newInstance("android.widget.Button",context)
	params = luajava.newInstance("android.widget.RelativeLayout$LayoutParams",RelativeLayoutParams.WRAP_CONTENT,RelativeLayoutParams.WRAP_CONTENT)
	--params:setGravity(grav)
	if(grav == 1) then
		params:addRule(RelativeLayout.LEFT_OF,98)
		button:setId(grav)
	else
		params:addRule(RelativeLayout.LEFT_OF,grav-1)
		button:setId(grav)
	end
	--params:addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
	--params:addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
	debugPrint("created instances")
	button:setText(name)
	button:setTextSize(26)
	button:setLayoutParams(params)
	button:setOnClickListener(clicker_cb)
	return button
end

--init the first three, permanent buttons.
hideButton = luajava.newInstance("android.widget.Button",context)
hideParams = luajava.newInstance("android.widget.RelativeLayout$LayoutParams",RelativeLayoutParams.WRAP_CONTENT,RelativeLayoutParams.WRAP_CONTENT)
hideParams:addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
hideButton:setId(100)
hideButton:setText("Hide")
hideButton:setTextSize(26)
hideButton:setLayoutParams(hideParams)
hideButton:setOnClickListener(hider_cb)

pinButton = luajava.newInstance("android.widget.Button",context)
pinParams = luajava.newInstance("android.widget.RelativeLayout$LayoutParams",RelativeLayoutParams.WRAP_CONTENT,RelativeLayoutParams.WRAP_CONTENT)
pinParams:addRule(RelativeLayout.LEFT_OF,100);
pinButton:setId(99)
pinButton:setText("Pin")
pinButton:setTextSize(26)
pinButton:setLayoutParams(pinParams)

mainButton = luajava.newInstance("android.widget.Button",context)
mainParams = luajava.newInstance("android.widget.RelativeLayout$LayoutParams",RelativeLayoutParams.WRAP_CONTENT,RelativeLayoutParams.WRAP_CONTENT)
mainParams:addRule(RelativeLayout.LEFT_OF,99);
mainButton:setId(98)
mainButton:setText("main")
mainButton:setTextSize(26)
mainButton:setLayoutParams(mainParams)

clicker = {}
function clicker.onClick(v)
	--get the associated channel to the click, this is kind of awkward for now
	--but it is the label, so we dont care.
	label = v:getText()
	PluginXCallS("updateSelection",label)
end
clicker_cb = luajava.createProxy("android.view.View$OnClickListener",clicker)
mainButton:setOnClickListener(clicker_cb)

pinner = {}
function pinner.onClick(v)
	if(toggle == true) then
		if(pinned == true) then
			pinned = false
			pinButton:setText("pin")
		else
			--if(toggle == false) then
				pinned = true
				pinButton:setText("unpin")
			--end
		end
	end
	pinButton:invalidate()
end
pinner_cb = luajava.createProxy("android.view.View$OnClickListener",pinner)
pinButton:setOnClickListener(pinner_cb)

pinned = false
PluginXCallS("initReady","now")








