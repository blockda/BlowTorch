local buttons = _G["buttons"]
local LinearLayoutParams = _G["LinearLayoutParams"]
local LinearLayout = _G["LinearLayout"]
local luajava = _G["luajava"]
local TextView = _G["TextView"]
local Gravity = _G["Gravity"]
local Color = _G["Color"]
local TabHost = _G["TabHost"]
local TabWidget = _G["TabWidget"]
local android_R_id = _G["android_R_id"]
local R_drawable = _G["R_drawable"]
local Button = _G["Button"]
local FrameLayout = _G["FrameLayout"]
local EditText = _G["EditText"]
local density = _G["density"]
local TYPE_TEXT_FLAG_MULTI_LINE = _G["TYPE_TEXT_FLAG_MULTI_LINE"]
local Validator = _G["Validator"]
local Validator_Number_Not_Blank = _G["Validator_Number_Not_Blank"];
local Validator_Number_Or_Blank = _G["Validator_Number_Or_Blank"];
local ORIENTATION_LANDSCAPE = _G["ORIENTATION_LANDSCAPE"]
local Context = _G["Context"]
local ScrollView = _G["ScrollView"]
local require = _G["require"]
local View = _G["View"]
local Note = _G["Note"]
local pairs = _G["pairs"]
local buttonEditorDone = _G["buttonEditorDone"]
module(...)

local textSizeBig = (18) -- sp value
local textSize = (14)  
local textSizeSmall = (10) 
local bgGrey = Color:argb(255,0x99,0x99,0x99) -- background color
local tabMinHeight = (35 * density) -- dp value TODO

local WRAP_CONTENT = LinearLayoutParams.WRAP_CONTENT
local FILL_PARENT = LinearLayoutParams.FILL_PARENT
local GRAVITY_CENTER = Gravity.CENTER

local context = nil
local editorDialog

local doneClickListener
local cancelClickListener

--widgets
local title = nil --title text view
local clickLabelEdit --click state label editor
local clickCmdEdit --click state command editor
local flipLabelEdit --flip state label editor
local flipCmdEdit --flip state command editor

--the rest are harvested from the advanced page editor
local advancedEditor -- the shared advanced page editor loaded from module

function init(pContext)
	context = pContext
end

function showEditorDialog(editorValues,numediting)
	--make the parent view.
	--local button = nil
	
	--local context = view:getContext()
  local utils = require("buttonutils")
	local width_param,height_param = utils.getDialogDimensions(context)
	
	local top = luajava.new(LinearLayout,context)
	local topparams = luajava.new(LinearLayoutParams,width_param,height_param)
	-- topparams = luajava.new(LinearLayoutParams,WRAP_CONTENT,WRAP_CONTENT)
	--Note("\nlayout params:"..width_param.." "..height_param.."\n")
	top:setLayoutParams(topparams)
	--top:setScrollContainer(false)
	
	local title = luajava.new(TextView,context)
	top:setOrientation(LinearLayout.VERTICAL)
	local titletextParams = luajava.new(LinearLayoutParams,FILL_PARENT,WRAP_CONTENT)
	
	
	title:setLayoutParams(titletextParams)
	title:setTextSize(textSizeBig)
	title:setText("EDIT BUTTON")
	title:setGravity(GRAVITY_CENTER)
	title:setTextColor(Color:argb(255,0x33,0x33,0x33))
	title:setBackgroundColor(bgGrey)
	title:setId(1)
	top:addView(title)

	--make the new tabhost.	
	local params = luajava.new(LinearLayoutParams,WRAP_CONTENT,WRAP_CONTENT)
	local fillparams = luajava.new(LinearLayoutParams,FILL_PARENT,WRAP_CONTENT,1)
	local contentparams = luajava.new(LinearLayoutParams,FILL_PARENT,WRAP_CONTENT)

	local hostparams = luajava.new(LinearLayoutParams,FILL_PARENT,WRAP_CONTENT,2)
	local host = luajava.new(TabHost,context)

	host:setId(3)
	host:setLayoutParams(hostparams)
	
	
	
	--make the done and cancel buttons.
	--have to stuff them in linearlayout.
	local finishHolderParams = luajava.new(LinearLayoutParams,FILL_PARENT,WRAP_CONTENT)
	--finishHolderParams:addRule(RelativeLayout.BELOW,3)
	local finishHolder = luajava.new(LinearLayout,context)
	finishHolder:setLayoutParams(finishHolderParams)
	finishHolder:setId(2)
	
	--finishbuttonParams = luajava.new(RelativeLayoutParams,RLayoutParams.FILL_PARENT,WRAP_CONTENT)
	local done = luajava.new(Button,context)
	done:setLayoutParams(fillparams)
	done:setText("Done")
	done:setOnClickListener(doneClickListener)
	
	local cancel = luajava.new(Button,context)
	cancel:setLayoutParams(fillparams)
	cancel:setText("Cancel")
	cancel:setOnClickListener(cancelClickListener)
	finishHolder:addView(done)
	finishHolder:addView(cancel)
	top:addView(host)
	top:addView(finishHolder)
	
	
	local holder = luajava.new(LinearLayout,context)
	holder:setOrientation(LinearLayout.VERTICAL)
	holder:setLayoutParams(fillparams)
	
	local widget = luajava.new(TabWidget,context)
	widget:setId(android_R_id.tabs)
	widget:setLayoutParams(contentparams)
	widget:setWeightSum(3)
	
	local content = luajava.new(FrameLayout,context)
	content:setId(android_R_id.tabcontent)
	content:setLayoutParams(contentparams)
	holder:addView(widget)
	holder:addView(content)
	
	host:addView(holder)
	host:setup()
	
	
	local tab1 = host:newTabSpec("tab_one_btn_tab")
	local label1 = luajava.new(TextView,context)
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
	local clickPageScroller = luajava.new(ScrollView,context)
	clickPageScroller:setLayoutParams(fillparams)
	clickPageScroller:setId(1)
	
	local clickPage = luajava.new(LinearLayout,context)
	clickPage:setLayoutParams(fillparams)
	clickPage:setId(11)
	clickPage:setOrientation(LinearLayout.VERTICAL)
	
	local clickLabelRow = luajava.new(LinearLayout,context)
	clickLabelRow:setLayoutParams(fillparams)
	
	local clickLabel = luajava.new(TextView,context)
	clickLabel:setTextSize(textSize)
	clickLabel:setText("Label:")
	clickLabel:setGravity(Gravity.RIGHT)
	local clickLabelParams = luajava.new(LinearLayoutParams,80*density,WRAP_CONTENT)
	clickLabel:setLayoutParams(clickLabelParams)
	
	clickLabelEdit = luajava.new(EditText,context)
	clickLabelEdit:setTextSize(textSize)
	local clickLabelEditParams = luajava.new(LinearLayoutParams,FILL_PARENT,WRAP_CONTENT)
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
	
	
	local clickCmdRow = luajava.new(LinearLayout,context)
	clickCmdRow:setLayoutParams(fillparams)
	
	local clickCmdLabel = luajava.new(TextView,context)
	clickCmdLabel:setTextSize(textSize)
	clickCmdLabel:setText("CMD:")
	clickCmdLabel:setGravity(Gravity.RIGHT)
	local clickCmdLabelParams = luajava.new(LinearLayoutParams,80*density,WRAP_CONTENT)
	clickCmdLabel:setLayoutParams(clickLabelParams)
	
	clickCmdEdit = luajava.new(EditText,context)
	clickCmdEdit:setTextSize(textSize)
	local clickCmdEditParams = luajava.new(LinearLayoutParams,WRAP_CONTENT,WRAP_CONTENT)
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
	
	local tab2 = host:newTabSpec("tab_two_btn_tab")
	local label2 = luajava.new(TextView,context)
	label2:setLayoutParams(fillparams)
	label2:setText("Flip")
	label2:setTextSize(textSizeBig)
	label2:setBackgroundResource(R_drawable.tab_background)
	label2:setGravity(GRAVITY_CENTER)
	label2:setMinHeight(tabMinHeight)
	
	--second, flip page.
	local flipPageScroller = luajava.new(ScrollView,context)
	flipPageScroller:setLayoutParams(fillparams)
	flipPageScroller:setId(2)
	
	local flipPage = luajava.new(LinearLayout,context)
	flipPage:setLayoutParams(fillparams)
	flipPage:setId(22)
	flipPage:setOrientation(LinearLayout.VERTICAL)
	
	local flipLabelRow = luajava.new(LinearLayout,context)
	flipLabelRow:setLayoutParams(fillparams)
	
	local flipLabel = luajava.new(TextView,context)
	flipLabel:setTextSize(textSize)
	flipLabel:setText("Label:")
	flipLabel:setGravity(Gravity.RIGHT)
	local flipLabelParams = luajava.new(LinearLayoutParams,80*density,WRAP_CONTENT)
	flipLabel:setLayoutParams(flipLabelParams)
	
	flipLabelEdit = luajava.new(EditText,context)
	flipLabelEdit:setTextSize(textSize)
	local flipLabelEditParams = luajava.new(LinearLayoutParams,FILL_PARENT,WRAP_CONTENT)
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
	
	
	local flipCmdRow = luajava.new(LinearLayout,context)
	flipCmdRow:setLayoutParams(fillparams)
	
	local flipCmdLabel = luajava.new(TextView,context)
	flipCmdLabel:setTextSize(textSize)
	flipCmdLabel:setText("CMD:")
	flipCmdLabel:setGravity(Gravity.RIGHT)
	local flipCmdLabelParams = luajava.new(LinearLayoutParams,80*density,WRAP_CONTENT)
	flipCmdLabel:setLayoutParams(clickLabelParams)
	
	flipCmdEdit = luajava.new(EditText,context)
	flipCmdEdit:setTextSize(textSize)
	local flipCmdEditParams = luajava.new(LinearLayoutParams,FILL_PARENT,WRAP_CONTENT)
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
	
	local tab3 = host:newTabSpec("tab_three_btn_tab")
	local label3 = luajava.new(TextView,context)
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
	advancedEditor = require("buttoneditoradvanced")
	advancedEditor.init(context)
	local scrollerpage = advancedEditor.makeUI(editorValues,numediting)
	local parent = scrollerpage:getParent()
	if(parent ~= nil) then
		parent:removeView(scrollerpage)
	end
	--buttonNameRow:setVisibility(View.VISIBLE)
	
	
	Validator:reset()
	if(editorValues.width ~= "MULTI") then
		Validator:add(advancedEditor.getWidthEdit(),Validator_Number_Not_Blank,"Width")
	else
		Validator:add(advancedEditor.getWidthEdit(),Validator_Number_Or_Blank,"Width")
	end
	
	if(editorValues.height ~= "MULTI") then
		Validator:add(advancedEditor.getHeightEdit(),Validator_Number_Not_Blank,"Height")
	else
		Validator:add(advancedEditor.getHeightEdit(),Validator_Number_Or_Blank,"Height")
	end
	
	if(editorValues.x ~= "MULTI") then
		Validator:add(advancedEditor.getXCoordEdit(),Validator_Number_Not_Blank,"X Coordinate")
	else
		Validator:add(advancedEditor.getXCoordEdit(),Validator_Number_Or_Blank,"X Coordinate")
	end
	
	if(editorValues.y ~="MULTI") then
		Validator:add(advancedEditor.getYCoordEdit(),Validator_Number_Not_Blank,"Y Coordinate")
	else
		Validator:add(advancedEditor.getYCoordEdit(),Validator_Number_Or_Blank,"Y Coordinate")
	end
	
	if(editorValues.labelSize ~= "MULTI") then
		Validator:add(advancedEditor.getLabelSizeEdit(),Validator_Number_Not_Blank,"Label size")
	else
		Validator:add(advancedEditor.getLabelSizeEdit(),Validator_Number_Or_Blank,"Label size")
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


cancelClickListener = luajava.createProxy("android.view.View$OnClickListener",{
  onClick = function(v) editorDialog:dismiss() end
})

doneClickListener = luajava.createProxy("android.view.View$OnClickListener",{
  onClick = function(v) 
    local str = Validator:validate()
    if(str ~= nil) then
    Validator:showMessage(view:getContext(),str)
      return
    end
    
    --gather up editor data to pass back into the main button window callback
    local d = {}

  
    d.label = clickLabelEdit:getText():toString()
    --label = labeltmp:toString()
    d.cmd = clickCmdEdit:getText():toString()
    --cmd = cmdtmp:toString()
    d.flipLabel = flipLabelEdit:getText():toString()
    --fliplabel = fliplabeltmp:toString()
    d.flipCmd = flipCmdEdit:getText():toString()
    --flipcmd = flipcmdtmp:toString()
    
    local tmp = advancedEditor.getEditorValues()
    
    for i,v in pairs(tmp) do
      d[i] = v;
    end
    
    buttonEditorDone(d)
    
    --[[nametmp = buttonNameEdit:getText()
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
    width = tonumber(widthtmp:toString())]]
      
    editorDialog:dismiss()
  end
})

