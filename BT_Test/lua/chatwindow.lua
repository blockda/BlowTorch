--debugPrint("package path:"..package.path)
package.path = "/mnt/sdcard/BlowTorch/?.lua"

--make a button.
debugPrint("in the chat window")
context = view:getContext()

RelativeLayoutParams = luajava.bindClass("android.widget.RelativeLayout$LayoutParams")
RelativeLayout = luajava.bindClass("android.widget.RelativeLayout")

LinearLayout = luajava.bindClass("android.widget.RelativeLayout")
LinearLayoutParams = luajava.bindClass("android.widget.RelativeLayout")

debugPrint("bound necessary classes")

--make holder view for buttons.
LinearLayout = luajava.bindClass("android.widget.LinearLayout")

uiButtonBar = LinearLayout.new(context)
uiButtonBarParams = params = luajava.newInstance("android.widget.RelativeLayout$LayoutParams",RelativeLayoutParams.FILL_PARENT,RelativeLayoutParams.WRAP_CONTENT)
params:addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
--params:addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
uiButtonBar:setLayoutParams(uiButtonBarParams)
uiButtonBar:setOrientation(LinearLayout.HORIZONTAL)

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

--toggler = {}
--function toggler.onClick(v)
--	if(toggle == true) then
--		debugPrint("shrink");
--		parentView:startAnimation(shrinkAnimation)
--		view:startAnimation(windowMoveDownAnimation)
--		toggle = false
--	end
--end

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

--toggler_cb = luajava.createProxy("android.view.View$OnClickListener",toggler)
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
	
	view:startAnimation(windowMoveUpAnimation)
	
	return true;
end
toucher_cb = luajava.createProxy("android.view.View$OnLongClickListener",toucher)
view:setOnLongClickListener(toucher_cb)

function updateButtons(input)
	uiButtonBar:removeAllViews()
	
	--reconstruct the channel list table
	list = loadstring(args)()
	counter = 1
	for i,b in pairs(list) do
		newbutton = generateNewButton(i,counter)
		counter = counter + 1
		
		uiButtonBar:addView(newbutton)
	end
	
	uiButtonBar:requestLayout()
end

function generateNewButton(name,grav)
	button = luajava.newInstance("android.widget.Button",context)
	params = luajava.newInstance("android.widget.LinearLayout$LayoutParams",LinearLayoutParams.WRAP_CONTENT,LinearLayoutParams.WRAP_CONTENT)
	params:setGravity(grav)
	--params:addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
	--params:addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
	debugPrint("created instances")
	button:setText(name)
	button:setTextSize(26)
	button:setLayoutParams(params)
	
	return button
end










