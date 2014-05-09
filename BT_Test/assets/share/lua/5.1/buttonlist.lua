--encapulsation of the button list editor
local luajava = _G["luajava"]
local R_id = _G["R_id"]
local R_layout = _G["R_layout"]
local RelativeLayout = _G["RelativeLayout"]
local RelativeLayoutParams = _G["RelativeLayoutParams"]
local TranslateAnimation = _G["TranslateAnimation"]
local R_drawable = _G["R_drawable"]
local ImageButton = _G["ImageButton"]
local LinearLayoutParams = _G["LinearLayoutParams"]
local Context = _G["Context"]
local EditText = _G["EditText"]
local LinearLayout = _G["LinearLayout"]
local Button = _G["Button"]
local pairs = _G["pairs"]
local ipairs = _G["ipairs"]
local table = _G["table"]
local density = _G["density"]
local ViewGroup = _G["ViewGroup"]
local tonumber = _G["tonumber"]
local View = _G["View"]
local Color= _G["Color"]
local Note = _G["Note"]
local KeyEvent = _G["KeyEvent"]
local modifyButtonSetCallback = _G["modifyButtonSet"]
local DialogInterface = _G["DialogInterface"]
local PluginXCallS = _G["PluginXCallS"]
module(...)

local lastSelectedIndex = -1
local selectedIndex = -1
local selectedSet = nil
local sortedList = nil
local list = nil
local itemClicked = nil
local adapter = nil
local dialog = nil
local toolbar = nil
local animateIn = nil
local animateOut = {}
local animateOutAndDelete = {}
local animateOutListener = nil
local animateOutAndDeleteListener = {}
local dpadupdownlistener = nil
local dpadselectionlistener = nil
local makeToolbar = nil
local layoutInflater = nil
local context = nil
local modifyClickListener = nil
local newButtonListener = nil
local newSetDoneListener = nil
local newSetCancelListener = nil
local newSetEdit = nil
local newButtonSetDialog = nil
local loadClickListener = nil
local deleteClickListener = nil
local deleteConfirmListener = nil
local deleteCancelListener = nil
local doneListener = nil

local reclick_ = {}
local scrollListener = {}
local focusListener = nil

function init(pContext)	
	context = pContext
	layoutInflater = context:getSystemService(Context.LAYOUT_INFLATER_SERVICE)
	
	makeToolbar()
end

modifyClickListener = luajava.createProxy("android.view.View$OnClickListener",{
  onClick = function(v)
    Note("Modify clicked.")
    modifyButtonSetCallback(sortedList[lastSelectedIndex+1])
    
    dialog:dismiss()
  end
})


adapter = luajava.createProxy("android.widget.ListAdapter",{
	getView = function(pos,v,parent)
		local newview = nil
		if(v ~= nil) then
			newview = v
			
		else
			--Note("inflating view")
			newview = layoutInflater:inflate(R_layout.editor_selection_list_row,nil)
		
			local root = newview:findViewById(R_id.root)
			--root:setOnClickListener(rowClicker_cb)
			
		end
		
		newview:setId(157*pos)
		
		local holder = newview:findViewById(R_id.toolbarholder)
		holder:setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS)
		
		if(holder:getChildCount() > 0) then
			holder:removeAllViews()
			lastSelectedIndex = -1
		end
		
		item = sortedList[tonumber(pos)+1]
		
		if(item ~= nil) then
	
			label = newview:findViewById(R_id.infoTitle)
			extra = newview:findViewById(R_id.infoExtended)
			
			icon = newview:findViewById(R_id.icon)
			icon:setVisibility(View.GONE)
			label:setText(item.name)
			extra:setText("Contains: "..item.count.." buttons")
			
			if(selectedIndex == (pos+1)) then
				label:setBackgroundColor(Color:argb(55,255,255,255))
				extra:setBackgroundColor(Color:argb(55,255,255,255))
			else
				label:setBackgroundColor(Color:argb(0,0,0,0))
				extra:setBackgroundColor(Color:argb(0,0,0,0))
			end
			--newview:setId(pos)
		end
		return newview
	end,
	getCount = function() return #sortedList end,
	areAllItemsEnabled = function() return true end,
	isEnabled = function(position) return true end,
	getItem = function(position) return sortedList[position+1] end,
	getItemId = function(position) return 1 end,
	isEmpty = function() return false end,
	hasStableIds = function() return true end,
	getViewTypeCount = function() return 1 end,
	getItemViewType = function(pos) return 1 end
})

function dismissList()
  if(dialog ~= nil) then
    dialog:dismiss()
  end
end

function sortList(unsortedList)
  sortedList = {}
  for k,v in pairs(sortedList) do
    sortedList[v] = nil;
  end
  sortedList = nil
  sortedList = {}
  
  local counter = 1;
  selectedIndex = -1;
  for i,k in pairs(unsortedList) do
    local tmp = {}
    tmp.name = i
    tmp.count = k
    table.insert(sortedList,tmp)
  end

  table.sort(sortedList,function(a,b) if(a.name < b.name) then return true else return false end end)

  --find the selectedindex
  for i,k in ipairs(sortedList) do
    if(k.name == selectedSet) then
      selectedIndex = counter
    end
    counter = counter + 1
  end
end

function showList(unsortedList,lastLoadedSet)
	
	if(adapter ~= nil) then    Note("\nadapter is not nil"); end
	
	selectedSet = lastLoadedSet
	
	if(toolbar:getParent() ~= nil) then
		local parent = toolbar:getParent()
		parent:removeView(toolbar)
	end
	--sort the list
	sortList(unsortedList)
	

	--actually make the dialog
	local layout = luajava.newInstance("android.widget.RelativeLayout",context)
	layout = layoutInflater:inflate(R_layout.editor_selection_dialog,layout)
	
	list = layout:findViewById(R_id.list)
	--keep the list
	
	
	list:setScrollbarFadingEnabled(false)
	list:setOnItemClickListener(itemClicked)
	list:setSelector(R_drawable.blue_frame_nomargin_nobackground)
	list:setAdapter(adapter)
	list:setOnScrollListener(scrollListener)
	list:setOnFocusChangeListener(focusListener)
	list:setFocusable(true)
	list:bringToFront()
	
	local emptyView = layout:findViewById(R_id.empty)
	list:setEmptyView(emptyView)
	list:setSelectionFromTop(selectedIndex -1,10*density)

	local title = layout:findViewById(R_id.titlebar)
	title:setText("SELECT BUTTON SET")
	
	local newbutton = layout:findViewById(R_id.add)
	newbutton:setText("New Set")
	newbutton:setOnClickListener(newButtonListener)
	
	local donebutton = layout:findViewById(R_id.done)
	donebutton:setOnClickListener(doneListener)
	dialog = luajava.newInstance("com.offsetnull.bt.window.LuaDialog",context,layout,false,nil)

	--end
	dialog:show()
end

function updateButtonListDialog()
	list:setAdapter(adapter)
	dialog:dismiss()
end

function updateButtonListDialogNoItems()
	list:setAdapter(adapter)
	emptyButtons()
	mSelectorDialog:dismiss()
end




--function buttonListAdapter.


makeToolbar = function()
	--toolbar = toolbar or {}
	local toolbarlength
	if(not toolbar) then
		toolbar = layoutInflater:inflate(R_layout.editor_selection_list_row_toolbar,nil)
		local toolbarparams = luajava.new(RelativeLayoutParams,RelativeLayoutParams.WRAP_CONTENT,RelativeLayoutParams.WRAP_CONTENT)
		toolbarparams:addRule(RelativeLayout.ALIGN_PARENT_TOP)
		toolbarparams:addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
		toolbarparams:addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
		toolbar:setLayoutParams(toolbarparams)
		
		
		
		local buttonParams = luajava.new(LinearLayoutParams,LinearLayoutParams.WRAP_CONTENT,LinearLayoutParams.WRAP_CONTENT)
		buttonParams:setMargins(0,0,0,0)
		
		local makeButton = function(icon,listener)
			local button = luajava.new(ImageButton,context)
			button:setPadding(0,0,0,0)
			button:setOnKeyListener(dpadupdownlistener)
			button:setLayoutParams(buttonParams)
			button:setImageResource(icon)
			button:setOnClickListener(listener)
			button:setNextFocusDownId(R_id.list)
			button:setNextFocusUpId(R_id.list)
			return button
		end
		
		local toolbarToggle = makeButton(R_drawable.toolbar_load_button,loadClickListener)
		local toolbarModify = makeButton(R_drawable.toolbar_modify_button,modifyClickListener)
		local toolbarDelete = makeButton(R_drawable.toolbar_delete_button,deleteClickListener)
		
		toolbar:addView(toolbarToggle)
		toolbar:addView(toolbarModify)
		toolbar:addView(toolbarDelete)
		
		local closeButton = toolbar:findViewById(R_id.toolbar_tab_close)
		closeButton:setOnKeyListener(dpadupdownlistener)
		closeButton:setNextFocusUpId(R_id.list)
		closeButton:setNextFocusDownId(R_id.list)
		
		local tmpa = closeButton:getDrawable()
		local tmpb = toolbarToggle:getDrawable()
		
		toolbarlength = tmpa:getIntrinsicWidth() + 3 * tmpb:getIntrinsicWidth()
	end
	--animateInController = nil
	animateOut = nil	
	
	animateIn = luajava.new(TranslateAnimation,toolbarlength,0,0,0)
	animateIn:setDuration(300)
	
	animateOut = luajava.new(TranslateAnimation,0,toolbarlength,0,0)
	animateOut:setDuration(300)
	animateOut:setAnimationListener(animateOutListener)
	
	animateOutAndDelete = luajava.new(TranslateAnimation,0,toolbarlength,0,0)
	animateOutAndDelete:setDuration(300)
	animateOutAndDelete:setAnimationListener(animateOutAndDeleteListener)
	
end

local function removeToolbar()
	toolbar:startAnimation(animateOut)
end


dpadupdownlistener = luajava.createProxy("android.view.View$OnKeyListener",{
	onKey = function(v,keyCode,event)
		if(KeyEvent.KEYCODE_DPAD_UP == keyCode or KeyEvent.KEYCODE_DPAD_DOWN == keyCode) then
			removeToolbar()
			--list:requestFocus()
		end
		return false
	end
})


dpadselectionlistener = luajava.createProxy("android.widget.AdapterView$OnItemSelectedListener",{
	onItemSelected = function(adapter,view,position,rowid)
		if(view:getTop() < 0 or view:getBottom() > list:getHeight()) then
			list:smoothScrollToPosition(position,100)
		end
	end
})

local function makeSelectionRunnerForRow(pos,target)
	local scrollselectionrunner_ = {}
	function scrollselectionrunner.run()
		list:performItemClick((list:getAdapter()):getView(target,1,1))
	end
	local scrollselectionrunner = luajava.createProxy("java.lang.Runnable",scrollselectionrunner_)
	target:postDelayed(scrollselectionrunner)
end


animateOutListener = luajava.createProxy("android.view.animation.Animation$AnimationListener",{
	onAnimationEnd = function(animation)
		local parent = toolbar:getParent()
		if(parent ~= nil) then 
			parent:removeView(toolbar)
			--list:requestFocus()
		end
	end
})

animateOutAndDeleteListener = luajava.createProxy("android.view.animation.Animation$AnimationListener",{
  onAnimationEnd = function(animation)
    local parent = toolbar:getParent()
    if(parent ~= nil) then 
      parent:removeView(toolbar)
      --list:requestFocus()
    end
    local entry = sortedList[lastSelectedIndex+1]
    sortedList[entry] = nil
    table.remove(sortedList,lastSelectedIndex+1)
    PluginXCallS("deleteButtonSet",entry.name)
  end
})



itemClicked = luajava.createProxy("android.widget.AdapterView$OnItemClickListener",{
	onItemClick = function(arg0,view,position,arg3)
		Note("\ndoing click\n")
		if(toolbar:getParent() ~= nil) then
			removeToolbar()
			return
		end
		
		local duration = 500
		if(view:getBottom() > list:getHeight() or view:getTop() < 0) then
			Note("\nsmoothscrolling\n")
			list:smoothScrollToPosition(position,100)
			reclick_.target = position
			list:postDelayed(luajava.createProxy("java.lang.Runnable",reclick_),100)
			return
		end
		
		lastSelectedIndex = position
		local frame = list:getParent()
		local target = frame:getParent()
		local params = luajava.new(RelativeLayoutParams,RelativeLayoutParams.WRAP_CONTENT,RelativeLayoutParams.WRAP_CONTENT)
		params:addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
		params:addRule(RelativeLayout.ALIGN_PARENT_TOP)
		local y = position - list:getFirstVisiblePosition()
		Note("\ny pos="..y)
		local v_top = (list:getChildAt(y)):getTop()
		local f_top = frame:getTop()
		
		params:setMargins(0,v_top + f_top,0,0)
		toolbar:setLayoutParams(params)
		target:addView(toolbar)
		toolbar:startAnimation(animateIn)
		local child = toolbar:getChildAt(1)
		child:requestFocus()
	end,
	onNothingSelected = function(arg0) end --don't care
})

reclick_.target = -1
function reclick_.run()
	list:performItemClick(adapter:getView(reclick_.target,nil,nil),reclick_.target,reclick_.target)
end

scrollListener = luajava.createProxy("android.widget.AbsListView$OnScrollListener",{
	onScrollStateChanged = function(view,scrollstate)
		if(toolbar:getParent() ~= nil) then
			removeToolbar()
		end
	end,
	onScroll = function(view,first,visCount,totalCount)
		--don't care
	end
})

focusListener = luajava.createProxy("android.view.View$OnFocusChangeListener",{
	onFocusChange = function(view,hasfocus)
		if(hasfocus) then
			list:setSelector(R_drawable.blue_frame_nomargin_nobackground)
		else
			list:setSelector(R_drawable.transparent)
		end
	end
})

newButtonListener = luajava.createProxy("android.view.View$OnClickListener",{
  onClick = function(v)
    --Note("new button pressed")
    dialog:dismiss()
    --local context = view:getContext()
    --make the new button set text input dialog and show it.
    local linear = luajava.new(LinearLayout,context)
    
    local llparams = luajava.new(LinearLayoutParams,350*density,LinearLayoutParams.WRAP_CONTENT)
    
    local fillparams = luajava.new(LinearLayoutParams,LinearLayoutParams.FILL_PARENT,LinearLayoutParams.WRAP_CONTENT,1)
    
    local buttonholder = luajava.new(LinearLayout,context)
    buttonholder:setLayoutParams(llparams)
    buttonholder:setOrientation(LinearLayout.HORIZONTAL)
    linear:setLayoutParams(llparams)
    linear:setOrientation(LinearLayout.VERTICAL)
    
    newSetEdit = luajava.new(EditText,context)
    newSetEdit:setHint("New Button Set Name")
    
    local done = luajava.new(Button,context)
    done:setText("Done")
    done:setLayoutParams(fillparams)
    done:setOnClickListener(newSetDoneListener)
    
    local cancel = luajava.new(Button,context)
    cancel:setText("Cancel")
    cancel:setLayoutParams(fillparams)
    cancel:setOnClickListener(newSetCancelListener)
    
    buttonholder:addView(done)
    buttonholder:addView(cancel)
    
    linear:addView(newSetEdit)
    linear:addView(buttonholder)
    
    newButtonSetDialog = luajava.newInstance("com.offsetnull.bt.window.LuaDialog",context,linear,false,nil)
    newButtonSetDialog:show()
  end
})

newSetDoneListener = luajava.createProxy("android.view.View$OnClickListener",{
  onClick = function(view)
    newButtonSetDialog:dismiss()
    local text = newSetEdit:getText():toString()
    PluginXCallS("makeNewButtonSet",text)
  end
})

newSetCancelListener = luajava.createProxy("android.view.View$OnClickListener",{ 
  onClick = function(v)
    newButtonSetDialog:dismiss()
  end
})

loadClickListener = luajava.createProxy("android.view.View$OnClickListener",{
  onClick = function(v)
  local entry = sortedList[lastSelectedIndex+1]
  if(entry.name ~= selectedSet) then
    PluginXCallS("loadButtonSet",entry.name)
  end
  dialog:dismiss()
end
})

deleteConfirmListener = luajava.createProxy("android.content.DialogInterface$OnClickListener",{
  onClick = function(dialog,which)
  --Note("deleting,"..which)
    if(which == DialogInterface.BUTTON_POSITIVE) then
      --local entry = sortedList[lastSelectedIndex+1]
      --sortedList[entry] = nil
      --table.remove(sortedList,lastSelectedIndex+1)
      --PluginXCallS("deleteButtonSet",entry.name)
      toolbar:startAnimation(animateOutAndDelete)
    end
  end
})

deleteCancelListener = luajava.createProxy("android.content.DialogInterface$OnClickListener",{
  onClick = function(dialog,which)
    dialog:dismiss()
  end
})

deleteClickListener = luajava.createProxy("android.view.View$OnClickListener",{
  onClick = function(v)
    local builder = luajava.newInstance("android.app.AlertDialog$Builder",v:getContext())
    builder:setTitle("Delete Button Set")
    builder:setMessage("Confirm delete?")
    builder:setPositiveButton("Yes",deleteConfirmListener)
    builder:setNegativeButton("No",deleteCancelListener)
    
    local canceldialog = builder:create()
    canceldialog:show()
  end
})

function updateButtonListDialog(data)
  
  --buttonSetListDialog.updateButtonListDialog()
  selectedSet = data.setname
  --unsortedList = data.setlist
  sortList(data.setlist)
  Note("\nConfirmingDelete: " .. data.setname)
  list:setAdapter(adapter)
  --dialog:dismiss()
end

function updateButtonListDialogNoItems()
  sortedList = {}
  list:setAdapter(adapter)
  --emptyButtons()
  --dialog:dismiss()
end

doneListener = luajava.createProxy("android.view.View$OnClickListener",{
  onClick = function(v)
    --local foo = nil
    --pcall(foo)
    dialog:dismiss()
  end
})

