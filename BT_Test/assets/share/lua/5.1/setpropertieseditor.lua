local Validator = _G["Validator"]
local Validator_Number_Not_Blank = _G["Validator_Number_Not_Blank"];
local Validator_Number_Or_Blank = _G["Validator_Number_Or_Blank"];
local LinearLayout = _G["LinearLayout"]
local LinearLayoutParams = _G["LinearLayoutParams"]
local luajava = _G["luajava"]
local TextView = _G["TextView"]
local FILL_PARENT = _G["FILL_PARENT"]
local WRAP_CONTENT = _G["WRAP_CONTENT"]
local GRAVITY_CENTER = _G["GRAVITY_CENTER"]
local Color = _G["Color"]
local Button = _G["Button"]
local require = _G["require"]
--for the advacned page
local ScrollView = _G["ScrollView"]
local View = _G["View"]
local Configuration = _G["Configuration"]
local density = _G["density"]
local EditText = _G["EditText"]
local Gravity = _G["Gravity"]
local TYPE_CLASS_NUMBER = _G["TYPE_CLASS_NUMBER"]
local math = _G["math"]
local tostring = _G["tostring"]
local Note = _G["Note"]
local string = _G["string"]
local tonumber = _G["tonumber"]
local Validator_Not_Blank = _G["Validator_Not_Blank"]
module(...)

local textSizeBig = (18) -- sp value
local textSize = (14)  
local textSizeSmall = (10) 
local bgGrey = Color:argb(255,0x99,0x99,0x99) -- background color

local context
local advancedEditor

local dialog
local cancelClickListener
local doneClickListener

--callbacks
local editorDone
setEditorDoneCallback = function(c) editorDone = c end

function init(pContext)
  context = pContext
end

function showDialog(editorValues)
  advancedEditor = require("buttoneditoradvanced")
  advancedEditor.init(context)
  local advancedPage = advancedEditor.makeUI(editorValues,1)
  local parent = advancedPage:getParent()
  if(parent ~= nil) then
    parent:removeView(advancedPage)
  end
  
  advancedEditor.showSetEditorControls()
  
  Validator:reset()
  Validator:add(advancedEditor.getButtonNameEdit(),Validator_Not_Blank,"Set name")
  Validator:add(advancedEditor.getWidthEdit(),Validator_Number_Not_Blank,"Width")
  Validator:add(advancedEditor.getHeightEdit(),Validator_Number_Not_Blank,"Height")
  Validator:add(advancedEditor.getLabelSizeEdit(),Validator_Number_Not_Blank,"Label size")
  
  local utils = require("buttonutils")
  local width_param,height_param = utils.getDialogDimensions(context)
  local top = luajava.new(LinearLayout,context)
  local topparams = luajava.new(LinearLayoutParams,width_param,height_param)
  
  top:setLayoutParams(topparams)
  top:setOrientation(LinearLayout.VERTICAL)
  local titletext = luajava.new(TextView,context)
  local titletextParams = luajava.new(LinearLayoutParams,FILL_PARENT,WRAP_CONTENT)
  --titletextParams:addRule(RelativeLayout.ALIGN_PARENT_TOP)
  
  titletext:setLayoutParams(titletextParams)
  titletext:setTextSize(textSize * 2)
  titletext:setText("DEFAULTS EDITOR")
  titletext:setGravity(GRAVITY_CENTER)
  titletext:setTextColor(Color:argb(255,0x33,0x33,0x33))
  titletext:setBackgroundColor(bgGrey)
  --titleText:setTextColor()
  titletext:setId(1)
  top:addView(titletext)
  
  --buttonNameEdit:setText()

  --make the new tabhost. 
  local params = luajava.new(LinearLayoutParams,WRAP_CONTENT,WRAP_CONTENT)
  local fillparams = luajava.new(LinearLayoutParams,FILL_PARENT,WRAP_CONTENT,1)
  local contentparams = luajava.new(LinearLayoutParams,FILL_PARENT,WRAP_CONTENT)
  --fillparams:setGravity(Gravity.FILL_HORIZONTAL)
  --hostparams = luajava.new(LinearLayoutParams,FILL_PARENT,FILL_PARENT)
  --make the done and cancel buttons.
  --have to stuff them in linearlayout.
  local finishHolderParams = luajava.new(LinearLayoutParams,FILL_PARENT,WRAP_CONTENT)
  --finishHolderParams:addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
  local finishHolder = luajava.new(LinearLayout,context)
  finishHolder:setLayoutParams(finishHolderParams)
  finishHolder:setId(2)
  
  --local holder = luajava.new(LinearLayout,context)
  --holder:addView(page,contentparams)
  local holderparam = luajava.new(LinearLayoutParams,FILL_PARENT,WRAP_CONTENT,2)
  --holderparam:addRule(RelativeLayout.ABOVE,2)
  --holderparam:addRule(RelativeLayout.BELOW,1)
  --holderparam:addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
  --holderparam:addRule(RelativeLayout.ALIGN_PARENT_LEFT)
  
  advancedPage:setVisibility(View.VISIBLE)
  top:addView(advancedPage,holderparam)
  
  
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
  top:addView(finishHolder)
  
  dialog = luajava.newInstance("com.offsetnull.bt.window.LuaDialog",context,top,false,nil)
  
  dialog:show()
end

cancelClickListener = luajava.createProxy("android.view.View$OnClickListener",{
  onClick = function(v) dialog:dismiss() end
})

doneClickListener = luajava.createProxy("android.view.View$OnClickListener",{
  onClick = function(v) 
    local str = Validator:validate()
    if(str ~= nil) then
    Validator:showMessage(context,str)
      return
    end
    
    --gather up editor data to pass back into the main button window callback

    
    local tmp = advancedEditor.getEditorValues()
    
    if(editorDone ~= nil) then
      editorDone(tmp)
    end
      
    dialog:dismiss()
  end
})

