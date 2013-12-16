local buttons = _G["buttons"]
local LinearLayoutParams = _G["LinearLayoutParams"]
local LinearLayout = _G["LinearLayout"]
local luajava = _G["luajava"]
local TextView = _G["TextView"]
module(...)

local WRAP_CONTENT = LinearLayoutParams.WRAP_CONTENT
local FILL_PARENT = LinearLayoutParams.FILL_PARENT

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