--debugPrint("package path:"..package.path)
package.path = "/mnt/sdcard/BlowTorch/?.lua"

require("serialize")
--make a button.
debugPrint("HEY HEY HEY LOOK HERE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\nsfsafasf\nin the ui bootstrap!!!!!")

--set up and add the scroll view.
ScrollView = luajava.bindClass("android.widget.ScrollView")
RelativeLayout = luajava.bindClass("android.widget.RelativeLayout")
RelativeLayoutParams = luajava.bindClass("android.widget.RelativeLayout$LayoutParams")
TextView = luajava.bindClass("android.widget.TextView")

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

holder = luajava.new(RelativeLayout,view:getContext())
holderParams = luajava.new(RelativeLayoutParams,RelativeLayoutParams.WRAP_CONTENT,RelativeLayoutParams.WRAP_CONTENT)
holder:setLayoutParams(holderParams)
scroller:addView(holder)

label = luajava.new(TextView,view:getContext())
label:setTextSize(36)
label:setText("BELOW THE MAP")
labelParams = luajava.new(RelativeLayoutParams,RelativeLayoutParams.WRAP_CONTENT,RelativeLayoutParams.WRAP_CONTENT)
labelParams:addRule(RelativeLayout.ALIGN_LEFT,6020)
labelParams:addRule(RelativeLayout.ALIGN_RIGHT,6020)
labelParams:addRule(RelativeLayout.BELOW,6020)
label:setLayoutParams(labelParams)
label:setId(6022)
holder:addView(label)

label2 = luajava.new(TextView,view:getContext())
label2:setTextSize(36)
label2:setText("ABOVE THE MAP")
label2:setId(6021)
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












