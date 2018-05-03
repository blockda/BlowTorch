local LinearLayoutParams = _G["LinearLayoutParams"]
local LinearLayout = _G["LinearLayout"]
local ScrollView = _G["ScrollView"]
local density = _G["density"]
local luajava = _G["luajava"]
local Button = _G["Button"]
local Gravity = _G["Gravity"]
local pairs = _G["pairs"]
local math = _G["math"]
local require = _G["require"]
local Validator = _G["Validator"]
local Validator_Number_Not_Blank = _G["Validator_Number_Not_Blank"];
local Validator_Number_Or_Blank = _G["Validator_Number_Or_Blank"];
local Validator_Not_Blank = _G["Validator_Not_Blank"]
local luajava = _G["luajava"]
local TextView = _G["TextView"]
local FILL_PARENT = _G["FILL_PARENT"]
local WRAP_CONTENT = _G["WRAP_CONTENT"]
local GRAVITY_CENTER = _G["GRAVITY_CENTER"]
local Color = _G["Color"]
--needed for advaced page of the set propertieseditor dialog
local View = _G["View"]
local Configuration = _G["Configuration"]
local EditText = _G["EditText"]
local TYPE_CLASS_NUMBER = _G["TYPE_CLASS_NUMBER"]
local tostring = _G["tostring"]
local Note = _G["Note"]
local string = _G["string"]
local tonumber = _G["tonumber"]
local serialize = _G["serialize"]
module(...)

local context

--text size values (REFACTOR!)
local textSizeBig = (18) -- sp value
local textSize = (14)  
local textSizeSmall = (10) 

--callbacks to be set from the parent window (the main button manager/editor)
local setGridSnap
local setGridXSpacing
local setGridYSpacing
local setGridOpacity
local setGridSnapTest
local setAdvancedProperties
local editorDone
setEditorDoneCallback = function(c) editorDone = c end

--setter methods for the above callbacks
setGridSnapCallback = function(c) setGridSnap = c end
setGridXSpacingCallback = function(c) setGridXSpacing = c end
setGridYSpacingCallback = function(c) setGridYSpacing = c end
setGridOpacityCallback = function(c) setGridOpacity = c end
setGridSnapTestCallback = function(c) setGridSnapTest = c end
setAdvancedPropertiesCallback = function(c) setAdvancedProperties = c end
--end callback handling variables

--local vairables to keep track of widget values
local gridSnap
local gridX
local gridY
local gridOpacity
local gridIntersectionTest
local setEditorValues
local editorValues

--event handlers
local gridSnapCheckChangeListener
local gridXSeekBarChangeListener
local gridYSeekBarChangeListener
local gridOpacitySeekBarChangeListener
local gridIntersectionTestRadioChangedListener
local doneListener
local setDefaultsEditorListener

--ui widgets
local dialog
local xSeekBarLabel
local ySeekBarLabel
local opacitySeekBarLabel

function init(pContext)
  context = pContext
end

function showDialog(initialValues)

  gridX = initialValues.gridX
  gridY = initialValues.gridY
  gridOpacity = initialValues.gridOpacity
  gridIntersectionTest = initialValues.gridIntersectionTest
  gridSnap = initialValues.gridSnap
  
  editorValues = initialValues

  local ll = luajava.newInstance("android.widget.LinearLayout",context)
  ll:setOrientation(1)
  local llparams = luajava.new(LinearLayoutParams,350*density,LinearLayoutParams.WRAP_CONTENT,1)
  ll:setLayoutParams(llparams)
  --ll:setGravity(Gravity.CENTER)
  
  local scroller = luajava.new(ScrollView,context)
  scroller:setLayoutParams(llparams)
  
  local fillparams = luajava.new(LinearLayoutParams,LinearLayoutParams.FILL_PARENT,LinearLayoutParams.WRAP_CONTENT,1)
  local wrapparams = luajava.new(LinearLayoutParams,LinearLayoutParams.WRAP_CONTENT,LinearLayoutParams.WRAP_CONTENT,1)
  wrapparams:setMargins(0,15,0,0)
  local wrapparamsNoWeight = luajava.new(LinearLayoutParams,LinearLayoutParams.WRAP_CONTENT,LinearLayoutParams.WRAP_CONTENT)
  
  --lp = luajava.newInstance("android.view.ViewGroup$LayoutParams",-1,-2)

  local cb = luajava.newInstance("android.widget.CheckBox",context)
  cb:setChecked(gridSnap)
  cb:setText("Snap To Grid")
  cb:setTextSize(textSizeSmall)
  cb:setOnCheckedChangeListener(gridSnapCheckChangeListener)
  cb:setLayoutParams(fillparams)
  
  local subrow = luajava.new(LinearLayout,context)
  subrow:setLayoutParams(fillparams)
  
  --gridSizeRow = luajava.newInstance("android.widget.LinearLayout",context)
  --gridSizeRow:setOrientation(1)
  
  --Note("seekbar creation")
  local xSeekBar = luajava.newInstance("android.widget.SeekBar",context)
  xSeekBar:setOnSeekBarChangeListener(gridXSeekBarChangeListener)
  xSeekBar:setLayoutParams(fillparams)
  xSeekBarLabel = luajava.newInstance("android.widget.TextView",context)
  xSeekBarLabel:setLayoutParams(wrapparams)
  xSeekBarLabel:setTextSize(textSizeSmall)
  xSeekBarLabel:setText("Grid X Spacing: "..gridX)
  xSeekBar:setProgress((gridX)-32)
  
  local ySeekBar = luajava.newInstance("android.widget.SeekBar",context)
  ySeekBar:setOnSeekBarChangeListener(gridYSeekBarChangeListener)
  ySeekBar:setLayoutParams(fillparams)
  ySeekBarLabel = luajava.newInstance("android.widget.TextView",context)
  ySeekBarLabel:setLayoutParams(wrapparams)
  ySeekBarLabel:setTextSize(textSizeSmall)
  ySeekBarLabel:setText("Grid Y Spacing: "..gridY)
  ySeekBar:setProgress((gridY)-32)
  
  local opacitySeekBar = luajava.newInstance("android.widget.SeekBar",context)
  
  opacitySeekBar:setLayoutParams(fillparams)
  opacitySeekBar:setMax(255)
  ----Note("settings opacity slider to:"..manageropacity)
  opacitySeekBar:setProgress(gridOpacity)
  opacitySeekBar:setOnSeekBarChangeListener(gridOpacitySeekBarChangeListener)
  
  opacitySeekBarLabel = luajava.newInstance("android.widget.TextView",context)
  opacitySeekBarLabel:setLayoutParams(wrapparams)
  opacitySeekBarLabel:setTextSize(textSizeSmall)
  opacitySeekBarLabel:setText("Grid Opacity: "..gridOpacity)
  
  local rg_static = luajava.bindClass("android.widget.RadioGroup")
  
  --local subrow2 = luajava.new(LinearLayout,context)
  --subrow2:setLayoutParams(fillparams)
  
  local rg = luajava.newInstance("android.widget.RadioGroup",context)
  local rgLayoutParams = luajava.newInstance("android.widget.LinearLayout$LayoutParams",-2,-2)
  rg:setLayoutParams(rgLayoutParams)
  rg:setOnCheckedChangeListener(gridIntersectionTestRadioChangedListener)
  rg:setOrientation(0)
  
  local contain = luajava.newInstance("android.widget.RadioButton",context)
  contain:setText("Contains")
  contain:setTextSize(textSizeSmall)
  contain:setId(1)
  
  local intersect = luajava.newInstance("android.widget.RadioButton",context)
  intersect:setText("Intersect")
  intersect:setTextSize(textSizeSmall)
  intersect:setId(0)
  
  
  
  local rg_lp = luajava.bindClass("android.widget.RadioGroup$LayoutParams")
  
  local rg_lp_gen = luajava.new(rg_lp,fillparams)
  local rg_lp_gen2 = luajava.new(rg_lp,fillparams)
  rg_lp_gen2:setMargins(25,0,0,0)
  
  rg:addView(intersect,0,rg_lp_gen)
  rg:addView(contain,1,rg_lp_gen2)
  rg:check(gridIntersectionTest)
  
  local selectionTextLabel = luajava.newInstance("android.widget.TextView",context)
  selectionTextLabel:setLayoutParams(wrapparams)
  selectionTextLabel:setTextSize(textSizeSmall)
  selectionTextLabel:setText("Drag rectangle selection test:")
  
  --subrow2:addView(selectionTextLabel)
  --subrow2:addView(rg)
  
  local setSettingsButton = luajava.new(Button,context)
  setSettingsButton:setLayoutParams(fillparams)
  setSettingsButton:setText("Edit Defaults")
  setSettingsButton:setOnClickListener(setDefaultsEditorListener)
  --Note("adding views")
  
  subrow:addView(cb)
  subrow:addView(setSettingsButton)
  ll:addView(subrow)
  ll:addView(xSeekBarLabel)
  ll:addView(xSeekBar)
  ll:addView(ySeekBarLabel)
  ll:addView(ySeekBar)
  ll:addView(opacitySeekBarLabel)
  ll:addView(opacitySeekBar)
  ll:addView(selectionTextLabel)
  ll:addView(rg)
  
  local boptHolder = luajava.new(LinearLayout,context)
  boptHolder:setLayoutParams(fillparams)
  boptHolder:setGravity(Gravity.CENTER)
  local boptDoneButton = luajava.newInstance("android.widget.Button",context)
  boptDoneButton:setText("Done")
  boptDoneButton:setLayoutParams(wrapparamsNoWeight)
  boptDoneButton:setOnClickListener(doneListener)
  boptHolder:addView(boptDoneButton)
  
  ll:addView(boptHolder)
  
  scroller:addView(ll)
  --ll:addView(rg)
  
  --ll:addView(setSettingsButton)
  --ll:addView(subrow)
  --set up the show editor settings button.
  --Note("builder alert creation")
  --builder = luajava.newInstance("android.app.AlertDialog$Builder",context)
  dialog = luajava.newInstance("com.offsetnull.bt.window.LuaDialog",context,scroller,false,nil)
  dialog:show()
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

gridSnapCheckChangeListener = luajava.createProxy("android.widget.CompoundButton$OnCheckedChangeListener",{
  onCheckedChanged = function(v,isChecked)
    gridSnap = isChecked
    if(setGridSnap ~= nil) then
      setGridSnap(isChecked)
    end
  end
})

gridXSeekBarChangeListener = luajava.createProxy("android.widget.SeekBar$OnSeekBarChangeListener",{
  onProgressChanged = function(v,progress,state)
    gridX = (progress + 32)
    xSeekBarLabel:setText("Grid X Spacing: "..gridX)
    if(setGridXSpacing ~= nil) then
      setGridXSpacing(gridX)
    end
  end
})

gridYSeekBarChangeListener = luajava.createProxy("android.widget.SeekBar$OnSeekBarChangeListener",{
  onProgressChanged = function(v,progress,state)
    gridY = (progress + 32)
    ySeekBarLabel:setText("Grid Y Spacing: "..gridY)
    if(setGridYSpacing ~= nil) then
      setGridYSpacing(gridY)
    end
  end
})

gridOpacitySeekBarChangeListener = luajava.createProxy("android.widget.SeekBar$OnSeekBarChangeListener",{
  onProgressChanged = function(v,progress,state)
    gridOpacity = progress
    local opacitypct = math.floor((gridOpacity / 255)*100)
    opacitySeekBarLabel:setText("Grid Opacity: "..opacitypct.."%")
    if(setGridOpacity ~= nil) then
      setGridOpacity(progress)
    end
  end 
})

gridIntersectionTestRadioChangedListener = luajava.createProxy("android.widget.RadioGroup$OnCheckedChangeListener",{
  onCheckedChanged = function(group,id)
    gridIntersectionTest = id
    if(setGridSnapTest ~= nil) then
      setGridSnapTest(gridIntersectionTest)
    end
  end
})

doneListener = luajava.createProxy("android.view.View$OnClickListener",{
  onClick = function(v)
    --collect editor values
    if(setEditorValues ~= nil) then
      for i,k in pairs(setEditorValues) do
        editorValues[i] = k
      end
    end
    if(editorDone ~= nil) then
      editorDone(editorValues)
    end
  
    dialog:dismiss()
  end
})

setDefaultsEditorListener = luajava.createProxy("android.view.View$OnClickListener",{
  onClick = function(v)
    local callback = function(values)
      --this is called when the defaults editor is done.
      setEditorValues = values;
    end
    
    local setEditor = require("setpropertieseditor")
    setEditor.init(context)
    setEditor.setEditorDoneCallback(callback)
    setEditor.showDialog(editorValues)
  end
})

