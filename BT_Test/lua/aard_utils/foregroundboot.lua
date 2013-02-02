--debugPrint("package path:"..package.path)

require("serialize")
--make a button.
--debugPrint("HEY HEY HEY LOOK HERE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\nsfsafasf\nin the ui bootstrap!!!!!")

--set up and add the scroll view.
ScrollView = luajava.bindClass("android.widget.ScrollView")
RelativeLayout = luajava.bindClass("android.widget.RelativeLayout")
RelativeLayoutParams = luajava.bindClass("android.widget.RelativeLayout$LayoutParams")
TextView = luajava.bindClass("android.widget.TextView")
Color = luajava.bindClass("android.graphics.Color")
Gravity = luajava.bindClass("android.view.Gravity")
TruncateAt = luajava.bindClass("android.text.TextUtils$TruncateAt")
--scrollholder = luajava.new(RelativeLayout,view:getContext())
--scrollholderParams = luajava.new(RelativeLayoutParams,400,400)
--scrollerParams:addRule(RelativeLayout.BELOW,5010)
--scrollholderParams:addRule(RelativeLayout.ABOVE,10)
--scrollholderParams:addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
--scrollholder:setLayoutParams(scrollholderParams)
--scrollholder:setId(6010)

scroller = luajava.new(ScrollView,view:getContext())
scrollerParams = luajava.new(RelativeLayoutParams,400,400)
scrollerParams:addRule(RelativeLayout.BELOW,5010)
scrollerParams:addRule(RelativeLayout.ABOVE,10)

scrollerParams:addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
scroller:setLayoutParams(scrollerParams)
scroller:setId(6010)

--scrollholder:addView(scroller)
parent = view:getParent()
parent:addView(scroller)

--parent:removeView(view)

holder = luajava.new(RelativeLayout,view:getContext())
holderParams = luajava.new(RelativeLayoutParams,RelativeLayoutParams.WRAP_CONTENT,RelativeLayoutParams.WRAP_CONTENT)
holder:setLayoutParams(holderParams)
scroller:addView(holder)

label = luajava.new(TextView,view:getContext())
label:setTextSize(22)
label:setText("BELOW THE MAP")
label:setTextColor(Color:argb(255,15,200,15))
labelParams = luajava.new(RelativeLayoutParams,RelativeLayoutParams.WRAP_CONTENT,RelativeLayoutParams.WRAP_CONTENT)
labelParams:addRule(RelativeLayout.ALIGN_LEFT,6020)
labelParams:addRule(RelativeLayout.ALIGN_RIGHT,6020)
labelParams:addRule(RelativeLayout.BELOW,6020)
label:setLayoutParams(labelParams)
label:setGravity(Gravity.CENTER)
label:setId(6022)
holder:addView(label)

label2 = luajava.new(TextView,view:getContext())
label2:setTextSize(25)
label2:setText("ABOVE THE MAP")
label2:setTextColor(Color:argb(255,15,200,15))
label2:setGravity(Gravity.CENTER)
label2:setId(6021)
label2:setLines(1)
label2:setHorizontallyScrolling(true)
label2:setFocusableInTouchMode(true)
label2:setFocusable(true)
label2:setEllipsize(TruncateAt.END)
--debugPrint("setEllipsized")
labelParams2 = luajava.new(RelativeLayoutParams,RelativeLayoutParams.WRAP_CONTENT,RelativeLayoutParams.WRAP_CONTENT)
labelParams2:addRule(RelativeLayout.ALIGN_LEFT,6020)
labelParams2:addRule(RelativeLayout.ALIGN_RIGHT,6020)
--labelParams2:addRule(RelativeLayout.ABOVE,6020)
--labelParams2:addRule(RelativeLayout.ALIGN_PARENT_TOP)
labelParams2:addRule(RelativeLayout.BELOW,9010)
label2:setLayoutParams(labelParams2)

holder:addView(label2)

--parent:removeView(view)
--parent:addView(view)


function demo(args)
	Note("doing demo "..args)
	num = tonumber(args)
	
	if(num ~= nil) then
		if(num == 1) then
			doDemoOne()
		elseif(num ==2) then
			doDemoTwo()
		elseif(num == 3) then
			doDemoThree()
		elseif(num == 4) then
			doDemoFour()
		elseif(num == 5) then
			doDemoFive()
		elseif(num == 0) then
			doDemoZero()
		end
	end
end

function doDemoOne()
	--demo one, this is the the vitals window under the input bar.
	inputbar = parent:findViewById(10)
	vitalsbar = parent:findViewById(1010)
	--debugPrint("something|"..vitalsbar:toString())
	holder:removeView(vitalsbar)
	--scroller:invalidate()
	oldparams = vitalsbar:getLayoutParams()
	vparams = luajava.new(RelativeLayoutParams,RelativeLayoutParams.FILL_PARENT,oldparams.height)
	--vparams:addRule(RelativeLayout.BELOW,10)
	vparams:addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
	vparams:addRule(RelativeLayout.LEFT_OF,9010)
	vitalsbar:setLayoutParams(vparams)
	originalinputparams = inputbar:getLayoutParams()
	iparams = luajava.new(RelativeLayoutParams,inputbar:getLayoutParams())
	iparams:addRule(RelativeLayout.ABOVE,1010)
	inputbar:setLayoutParams(iparams)
	
	parent:addView(vitalsbar)
	inputbar:requestLayout()
	
	
	tickbar = parent:findViewById(9010)
	statbar = parent:findViewById(9020)
	
	holder:removeView(tickbar)
	holder:removeView(statbar)
	
	originaltickparams = tickbar:getLayoutParams()
	originalstatparams = statbar:getLayoutParams()
	
	
	tickbarparams = luajava.new(RelativeLayoutParams,tickbar:getLayoutParams())
	statbarparams = luajava.new(RelativeLayoutParams,statbar:getLayoutParams())
	
	tickbarparams:addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
	tickbarparams:addRule(RelativeLayout.LEFT_OF,9020)

	statbarparams:addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
	statbarparams:addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
	
	statbar:setLayoutParams(statbarparams)
	tickbar:setLayoutParams(tickbarparams)
	
	parent:addView(statbar)
	parent:addView(tickbar)
	
	
end

function doDemoTwo()
	--everything except vitals in the scroller, vitals on the side
	parent:removeView(tickbar)
	parent:removeView(statbar)
	
	newtickparams = luajava.new(RelativeLayoutParams,originaltickparams)
	newstatparams = luajava.new(RelativeLayoutParams,originalstatparams)
	
	newtickparams:addRule(RelativeLayout.ALIGN_PARENT_TOP)
	
	newstatparams:addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
	newstatparams:addRule(RelativeLayout.BELOW,6022)
	
	tickbar:setLayoutParams(newtickparams)
	statbar:setLayoutParams(newstatparams)
	
	inputbar:setLayoutParams(originalinputparams)
	
	holder:addView(statbar)
	holder:addView(tickbar)
	chatwindow = parent:findViewById(5010)
	vparams = luajava.new(RelativeLayoutParams,100,inputbar:getTop() - chatwindow:getBottom())
	vparams:addRule(RelativeLayout.ABOVE,10)
	--vparams:addRule(RelativeLayout.BELOW,5010)
	vparams:addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
	vitalsbar:setLayoutParams(vparams)
	
	scrollerParams:addRule(RelativeLayout.ALIGN_PARENT_RIGHT,0)
	scrollerParams:addRule(RelativeLayout.LEFT_OF,1010)
	
	vitalsbar:requestLayout()
	--parent:addView(vitalsbar)
end

function doDemoThree()
	vparams = luajava.new(RelativeLayoutParams,100,RelativeLayoutParams.FILL_PARENT)
	vparams:addRule(RelativeLayout.ABOVE,10)
	--vparams:addRule(RelativeLayout.BELOW,5010)
	vparams:addRule(RelativeLayout.ALIGN_PARENT_TOP)
	vparams:addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
	
	vitalsbar:setLayoutParams(vparams)
	
	
	newchatparams = luajava.new(RelativeLayoutParams,chatwindow:getLayoutParams())
	newchatparams:addRule(RelativeLayout.ALIGN_PARENT_TOP)
	newchatparams:addRule(RelativeLayout.LEFT_OF,1010)
	
	chatwindow:setLayoutParams(newchatparams)

end

function doDemoFour()
	--newchatparams = luajava.new(RelativeLayoutParams,RelativeLayoutParams.FILL_PARENT,177)
	
	newchatparams:addRule(RelativeLayout.LEFT_OF,6010)
	newchatparams:addRule(RelativeLayout.ALIGN_PARENT_RIGHT,0)
	
	
	scrollerParams:addRule(RelativeLayout.BELOW,0)
	scrollerParams:addRule(RelativeLayout.ALIGN_PARENT_TOP)
	
	chatwindow:requestLayout()
end

function doDemoFive()
	scrollerParams:addRule(RelativeLayout.LEFT_OF,0)
	scrollerParams:addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
	
	vitalsbar:setLayoutParams(oldparams)
	parent:removeView(vitalsbar)
	
	
	--newstatparams:addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
	newstatparams:addRule(RelativeLayout.BELOW,1010)
	holder:addView(vitalsbar)
end

function doDemoZero()
	scrollerParams:addRule(RelativeLayout.ALIGN_PARENT_TOP,0)
	
	scrollerParams:addRule(RelativeLayout.BELOW,chatwindow:getId())
	
	--newchatparams = luajava.new(RelativeLayoutParams,chatwindow:getLayoutParams())
	--newchatparams:addRule(RelativeLayout.ALIGN_PARENT_TOP)
	--newchatparams:addRule(RelativeLayout.LEFT_OF,1010)
	newchatparams:addRule(RelativeLayout.LEFT_OF,0)
	newchatparams:addRule(RelativeLayout.ALIGN_PARENT_LEFT)
	newchatparams:addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
	--chatwindow:setLayoutLarasm(newchatparams)
	chatwindow:requestLayout()
end

Note("\nattempting to load in the new layout manager\n")
dofile(GetPluginInstallDirectory().."/layout_manager_ui.lua")







