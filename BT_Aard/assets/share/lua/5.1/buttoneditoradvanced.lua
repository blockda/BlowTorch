local LinearLayout = _G["LinearLayout"]
local luajava = _G["luajava"]
local ScrollView = _G["ScrollView"]
local View = _G["View"]
local Configuration = _G["Configuration"]
local density = _G["density"]
local TextView = _G["TextView"]
local EditText = _G["EditText"]
local Gravity = _G["Gravity"]
local FILL_PARENT = _G["FILL_PARENT"]
local WRAP_CONTENT = _G["WRAP_CONTENT"]
local LinearLayoutParams = _G["LinearLayoutParams"]
local Color = _G["Color"]
local GRAVITY_CENTER = _G["GRAVITY_CENTER"]
local TYPE_CLASS_NUMBER = _G["TYPE_CLASS_NUMBER"]
local math = _G["math"]
local tostring = _G["tostring"]
local Note = _G["Note"]
local string = _G["string"]
local tonumber = _G["tonumber"]
module(...)

local context = nil

local advancedPage
local advancedPageScroller
local quicknew = luajava.new
local editorValues
local fillparams = luajava.new(LinearLayoutParams,FILL_PARENT,WRAP_CONTENT,1)
local makeEdit
local makeLabel
local textSizeBig = (18) -- sp value
local textSize = (14)  
local textSizeSmall = (10) 
local bgGrey = Color:argb(255,0x99,0x99,0x99) -- background color
local tabMinHeight = (35 * density) -- dp value TODO

--widgets that need to be kept for harvesting
local xCoordEdit
local yCoordEdit
local heightEdit
local widthEdit
local labelSizeEdit
local nameEdit --button Name editor
local targetEdit --switch to target editor
--The actual color values that will be changed by the swatch click handler
local normalColor
local flipColor
local pressedColor
local normalLabelColor
local flipLabelColor
--The widget views that will launch and display the current color.
local normalColorPicker
local flipColorPicker
local pressedColorPicker
local normalLabelColorPicker
local flipLabelColorPicker

local selectedColorField --intermeidate variable to store which color field is currently selected

--ui callback listeners
local colorPickerDoneListener
local swatchClickListener

--funtions to hide/show the appropriate rows, defined later, leave global
showSetEditorControls = nil
showButtonEditorControls = nil

--ui widgets that need to be remembered for hide/show
local controlRowTwo
local labelRowFour
local buttonNameRow
local buttonTargetSetRow

--functions to get the appropriate editors to attatch the validator to, leave global
getWidthEdit = function() return widthEdit end
getHeightEdit = function() return heightEdit end
getXCoordEdit = function() return xCoordEdit end
getYCoordEdit = function() return yCoordEdit end
getLabelSizeEdit = function() return labelSizeEdit end
getButtonNameEdit = function() return nameEdit end

function init(pContext)
  context = pContext
end

function makeUI(editorValues,numediting)
  
  local fnew = luajava.new
  --local context = view:getContext()
  local LabelWidth = nil -- margin size for different screen layouts
  if(advancedPageScroller == nil) then
    advancedPageScroller = fnew(ScrollView,context)
    advancedPageScroller:setId(3)
  end
  
  if(advancedPage == nil) then
    advancedPage = fnew(LinearLayout,context)
    advancedPage:setOrientation(LinearLayout.VERTICAL)
    advancedPageScroller:addView(advancedPage)
  end
  
  --buttonNameRow
  if(buttonNameRow == nil) then
    buttonNameRow = fnew(LinearLayout,context)
    buttonNameRow:setLayoutParams(fillparams)
    advancedPage:addView(buttonNameRow)
  end
  
  if(numediting > 1) then
    buttonNameRow:setVisibility(View.GONE)
  else
    buttonNameRow:setVisibility(View.VISIBLE)
  end
  -- Adjust margins for larger screen sizes
  if(test == Configuration.SCREENLAYOUT_SIZE_XLARGE) then
    LabelWidth = 100 * density
  else
    LabelWidth = 80 * density
  end
  
  local buttonNameLabelParams = fnew(LinearLayoutParams,LabelWidth,WRAP_CONTENT)
  
  local buttonNameLabel = nil
  if(buttonNameLabel == nil) then
    buttonNameLabel = fnew(TextView,context)
    
    buttonNameLabel:setLayoutParams(buttonNameLabelParams)
    buttonNameLabel:setText("Name:")
    buttonNameLabel:setTextSize(textSize)
    buttonNameLabel:setGravity(Gravity.RIGHT)
    buttonNameRow:addView(buttonNameLabel)
  end
  
  buttonNameEditParams = fnew(LinearLayoutParams,FILL_PARENT,WRAP_CONTENT)
    
  --local buttonNameEdit = makeEdit(buttonNameEditParams)
  if(nameEdit == nil) then
    nameEdit = fnew(EditText,context) 
    nameEdit:setTextSize(textSize)
    nameEdit:setLines(1)
    nameEdit:setLayoutParams(buttonNameEditParams)
    buttonNameRow:addView(nameEdit)
  end
  if(numediting > 1) then
    --Note("\nEditing multiple, not setting name\n")
    nameEdit:setText("")
    nameEdit:setEnabled(false)
    --nameEdit:setVisibility(View.GONE)
  else
    nameEdit:setVisibility(View.VISIBLE)
    --Note("\nSetting editor value,"..editorValues.name)
    if(editorValues.name ~= nil) then
    --Note("\nEditing button, name is,"..editorValues.name.."\n")
      nameEdit:setEnabled(true)
      nameEdit:setText(editorValues.name)
    else
      --Note("\nEditorvalues.name is nil\n")
    end
    
  end
  
  if(buttonTargetSetRow == nil) then
    buttonTargetSetRow = fnew(LinearLayout,context)
    buttonTargetSetRow:setLayoutParams(fillparams)
    advancedPage:addView(buttonTargetSetRow)
  end
  if(numediting > 1) then
    buttonTargetSetRow:setVisibility(View.GONE)
  else
    buttonTargetSetRow:setVisibility(View.VISIBLE)
  end
  
  buttonTargetSetLabelParams = fnew(LinearLayoutParams,LabelWidth,WRAP_CONTENT)
  if(buttonTargetSetLabel == nil) then
    buttonTargetSetLabel = fnew(TextView,context)
    
    buttonTargetSetLabel:setLayoutParams(buttonNameLabelParams)
    buttonTargetSetLabel:setText("Target Set:")
    buttonTargetSetLabel:setTextSize(textSize)
    buttonTargetSetLabel:setGravity(Gravity.RIGHT)
    buttonTargetSetRow:addView(buttonTargetSetLabel)
  end
  
  buttonTargetSetEditParams = fnew(LinearLayoutParams,FILL_PARENT,WRAP_CONTENT)
    
  if(targetEdit == nil) then
    targetEdit = fnew(EditText,context)  
    targetEdit:setTextSize(textSize)
    targetEdit:setLines(1)
    targetEdit:setLayoutParams(buttonTargetSetEditParams)
    buttonTargetSetRow:addView(targetEdit)
  end
  if(numediting > 1) then
    targetEdit:setEnabled(false)
    targetEdit:setText("")
    
  else
    if(editorValues.switchTo ~= nil) then
      targetEdit:setEnabled(true)
      targetEdit:setText(editorValues.switchTo)
    end
  end
  
  
  colortopLabelParams = fnew(LinearLayoutParams,FILL_PARENT,WRAP_CONTENT)
    
  if(colortopLabel == nil) then
    colortopLabel = fnew(TextView,context)
    colortopLabelParams:setMargins(0,10,0,10)
    colortopLabel:setLayoutParams(colortopLabelParams)
    colortopLabel:setTextSize(textSize)
    colortopLabel:setText("COLORS")
    colortopLabel:setGravity(GRAVITY_CENTER)
    colortopLabel:setTextColor(Color:argb(255,0x33,0x33,0x33))
    colortopLabel:setBackgroundColor(bgGrey)
    advancedPage:addView(colortopLabel)
  end
  
  if(colorRowOne == nil) then
    colorRowOne = fnew(LinearLayout,context)
    colorRowOne:setLayoutParams(fillparams)
    advancedPage:addView(colorRowOne)
  end
  
  if(colorHolderA == nil) then
    colorHolderA = fnew(LinearLayout,context)
    colorHolderA:setLayoutParams(fillparams)
    colorHolderA:setGravity(GRAVITY_CENTER)
    --Note("addiing normal color holder")
    colorRowOne:addView(colorHolderA)
  end
  
  wrapparams = fnew(LinearLayoutParams,WRAP_CONTENT,WRAP_CONTENT)
  touchparams = fnew(LinearLayoutParams,60,60)
  
  if(normalColorPicker == nil) then
    normalColorPicker = fnew(View,context)
    normalColorPicker:setLayoutParams(touchparams)
    normalColor = editorValues.primaryColor
    normalColorPicker:setBackgroundColor(normalColor)
    normalColorPicker:setTag("normal")
    normalColorPicker:setOnClickListener(swatchClickListener)
    --Note("addiing normal color")
    colorHolderA:addView(normalColorPicker)
  else 
    normalColor = editorValues.primaryColor
    normalColorPicker:setBackgroundColor(normalColor)
  end

  if(colorHolderB == nil) then
    colorHolderB = fnew(LinearLayout,context)
    colorHolderB:setLayoutParams(fillparams)
    colorHolderB:setGravity(GRAVITY_CENTER)
    --Note("addiing pressed color holder")
    colorRowOne:addView(colorHolderB)
  end
  
  if(pressedColorPicker == nil) then
    pressedColorPicker = fnew(View,context)
    pressedColorPicker:setLayoutParams(touchparams)
    pressedColorPicker:setTag("pressed")
    pressedColorPicker:setOnClickListener(swatchClickListener)
    --thePressedColor = Color:argb(255,120,250,250)
    pressedColor = editorValues.selectedColor
    pressedColorPicker:setBackgroundColor(pressedColor)
    --Note("addiing pressed color")
    colorHolderB:addView(pressedColorPicker)
  else
    pressedColor = editorValues.selectedColor
    pressedColorPicker:setBackgroundColor(pressedColor)
  end
  
  if(colorHolderC == nil) then
    colorHolderC = fnew(LinearLayout,context)
    colorHolderC:setLayoutParams(fillparams)
    colorHolderC:setGravity(GRAVITY_CENTER)
    --Note("addiing flip colo hodlerr")
    colorRowOne:addView(colorHolderC)
  end
  
  if(flipColorPicker == nil) then
    flipColorPicker = fnew(View,context)
    flipColorPicker:setLayoutParams(touchparams)
    flipColorPicker:setTag("flip")
    flipColorPicker:setOnClickListener(swatchClickListener)
    flipColor = editorValues.flipColor
    --Note("addiing flip color")
    flipColorPicker:setBackgroundColor(flipColor)
    colorHolderC:addView(flipColorPicker)
  else
    flipColor = editorValues.flipColor
    flipColorPicker:setBackgroundColor(flipColor)
  end
  
  if(labelRowOne == nil) then
    labelRowOne = fnew(LinearLayout,context)
    labelRowOne:setLayoutParams(fillparams)
    advancedPage:addView(labelRowOne)
  end

  if(normalLabel == nil) then
    normalLabel = fnew(TextView,context)
    normalLabel:setLayoutParams(fillparams)
    normalLabel:setGravity(GRAVITY_CENTER)
    normalLabel:setText("Normal")
    normalLabel:setTextSize(textSizeSmall)
    labelRowOne:addView(normalLabel)
  end
  
  if(pressedLabel == nil) then
    pressedLabel = fnew(TextView,context)
    pressedLabel:setLayoutParams(fillparams)
    pressedLabel:setGravity(GRAVITY_CENTER)
    pressedLabel:setText("Pressed")
    pressedLabel:setTextSize(textSizeSmall)
    labelRowOne:addView(pressedLabel)
  end
  
  if(flippedLabel == nil) then
    flippedLabel = fnew(TextView,context)
    flippedLabel:setLayoutParams(fillparams)
    flippedLabel:setGravity(GRAVITY_CENTER)
    flippedLabel:setText("Flipped")
    flippedLabel:setTextSize(textSizeSmall)
    labelRowOne:addView(flippedLabel)
  end
  
  if(colorRowTwo == nil) then
    colorRowTwo = fnew(LinearLayout,context)
    colorRowTwoParams = fnew(LinearLayoutParams,fillparams)
    colorRowTwoParams:setMargins(0,10,0,0)
    colorRowTwo:setLayoutParams(colorRowTwoParams)
    advancedPage:addView(colorRowTwo)
  end
  
  if(colorHolderD == nil) then
    colorHolderD = fnew(LinearLayout,context)
    colorHolderD:setLayoutParams(fillparams)
    colorHolderD:setGravity(GRAVITY_CENTER)
  end
  
  if(normalLabelColorPicker == nil) then
    normalLabelColorPicker = fnew(View,context)
    normalLabelColorPicker:setLayoutParams(touchparams)
    normalLabelColor = editorValues.labelColor
    normalLabelColorPicker:setBackgroundColor(normalLabelColor)
    normalLabelColorPicker:setTag("label")
    normalLabelColorPicker:setOnClickListener(swatchClickListener)
    colorHolderD:addView(normalLabelColorPicker)
    colorRowTwo:addView(colorHolderD)
  else
    normalLabelColor = editorValues.labelColor
    normalLabelColorPicker:setBackgroundColor(normalLabelColor)
  end
  
  if(colorHolderE == nil) then
    colorHolderE = fnew(LinearLayout,context)
    colorHolderE:setLayoutParams(fillparams)
    colorHolderE:setGravity(GRAVITY_CENTER)
    colorRowTwo:addView(colorHolderE)
  end
  
  if(flipLabelColorPicker == nil) then
    flipLabelColorPicker = fnew(View,context)
    flipLabelColorPicker:setLayoutParams(touchparams)
    flipLabelColorPicker:setTag("flipLabel")
    flipLabelColorPicker:setOnClickListener(swatchClickListener)
    --theFlipLabelColor = Color:argb(255,120,250,250)
    flipLabelColor = editorValues.flipLabelColor
    flipLabelColorPicker:setBackgroundColor(flipLabelColor)
    colorHolderE:addView(flipLabelColorPicker)
  else
    flipLabelColor = editorValues.flipLabelColor
    flipLabelColorPicker:setBackgroundColor(flipLabelColor)
  end
  
  if(colorHolderF == nil) then
    colorHolderF = fnew(LinearLayout,context)
    colorHolderF:setLayoutParams(fillparams)
    colorHolderF:setGravity(GRAVITY_CENTER)
    colorRowTwo:addView(colorHolderF)
  end
  
  if(invisible == nil) then
    invisible = fnew(View,context)
    invisible:setVisibility(View.INVISIBLE)
    invisible:setLayoutParams(fillparams)
    colorHolderF:addView(invisible)
  end
  
  if(labelRowTwo == nil) then
    labelRowTwo = fnew(LinearLayout,context)
    labelRowTwo:setLayoutParams(fillparams)
    advancedPage:addView(labelRowTwo)
  end
  
  if(normalLabelLabel == nil) then
    normalLabelLabel = fnew(TextView,context)
    normalLabelLabel:setLayoutParams(fillparams)
    normalLabelLabel:setGravity(GRAVITY_CENTER)
    normalLabelLabel:setText("Label")
    normalLabelLabel:setTextSize(textSizeSmall)
    labelRowTwo:addView(normalLabelLabel)
  end
  
  if(flipLabelLabel == nil) then
    flipLabelLabel = fnew(TextView,context)
    flipLabelLabel:setLayoutParams(fillparams)
    flipLabelLabel:setGravity(GRAVITY_CENTER)
    flipLabelLabel:setText("FlipLabel")
    flipLabelLabel:setTextSize(textSizeSmall)
    labelRowTwo:addView(flipLabelLabel)
  end
  
  if(invisLabel == nil) then
    invisLabel = fnew(TextView,context)
    invisLabel:setLayoutParams(fillparams)
    invisLabel:setGravity(GRAVITY_CENTER)
    invisLabel:setText("FlipLabel")
    invisLabel:setTextSize(textSizeSmall)
    invisLabel:setVisibility(View.INVISIBLE)
    labelRowTwo:addView(invisLabel)
  end
  
  if(typeInLabel == nil) then
    typeInLabel = fnew(TextView,context)
    typeInLabelParams = fnew(LinearLayoutParams,FILL_PARENT,WRAP_CONTENT)
    typeInLabelParams:setMargins(0,10,0,10)
    typeInLabel:setLayoutParams(typeInLabelParams)
    typeInLabel:setTextSize(textSize)
    typeInLabel:setText("TYPE-IN CONTROLS")
    typeInLabel:setGravity(GRAVITY_CENTER)
    typeInLabel:setTextColor(Color:argb(255,0x33,0x33,0x33))
    typeInLabel:setBackgroundColor(bgGrey)
    advancedPage:addView(typeInLabel)
  end
  
  if(controlRowOne == nil) then
    controlRowOne = fnew(LinearLayout,context)
    controlRowOne:setLayoutParams(fillparams)
    advancedPage:addView(controlRowOne)
  end
  
  if(controlHolderA == nil) then
    controlHolderA = fnew(LinearLayout,context)
    controlHolderA:setLayoutParams(fillparams)
    controlHolderA:setGravity(GRAVITY_CENTER)
    controlRowOne:addView(controlHolderA)
  end
  
  numbereditorParams = fnew(LinearLayoutParams,120*density,WRAP_CONTENT)
  if(labelSizeEdit == nil) then
    labelSizeEdit = fnew(EditText,context)
    labelSizeEdit:setInputType(TYPE_CLASS_NUMBER)
    labelSizeEdit:setLayoutParams(numbereditorParams)
    labelSizeEdit:setGravity(GRAVITY_CENTER)
    labelSizeEdit:setTextSize(textSize)
    controlHolderA:addView(labelSizeEdit)
  end
  if(editorValues.labelSize == "MULTI") then
    labelSizeEdit:setText("")
  else
    labelSizeEdit:setText(tostring(math.floor(editorValues.labelSize)))
  end
  
  if(controlHolderB == nil) then
    controlHolderB = fnew(LinearLayout,context)
    controlHolderB:setLayoutParams(fillparams)
    controlHolderB:setGravity(GRAVITY_CENTER)
    controlRowOne:addView(controlHolderB)
  end
  --numbereditorParams = fnew(LinearLayoutParams,120,WRAP_CONTENT)
  if(widthEdit == nil) then
    widthEdit = fnew(EditText,context)
    widthEdit:setLayoutParams(numbereditorParams)
    widthEdit:setInputType(TYPE_CLASS_NUMBER)
    widthEdit:setGravity(GRAVITY_CENTER)
    widthEdit:setTextSize(textSize)
    controlHolderB:addView(widthEdit)
  end
  if(editorValues.width == "MULTI") then
    widthEdit:setText("")
  else
    widthEdit:setText(tostring(math.floor(editorValues.width)))
  end
  
  if(controlHolderC == nil) then
    controlHolderC = fnew(LinearLayout,context)
    controlHolderC:setLayoutParams(fillparams)
    controlHolderC:setGravity(GRAVITY_CENTER)
    controlRowOne:addView(controlHolderC)
  end
  --numbereditorParams = fnew(LinearLayoutParams,120,WRAP_CONTENT)
  if(heightEdit == nil) then
    heightEdit = fnew(EditText,context)
    heightEdit:setLayoutParams(numbereditorParams)
    heightEdit:setInputType(TYPE_CLASS_NUMBER)
    heightEdit:setGravity(GRAVITY_CENTER)
    heightEdit:setTextSize(textSize)
    controlHolderC:addView(heightEdit)
  end
  if(editorValues.height == "MULTI") then
    heightEdit:setText("")
  else
    heightEdit:setText(tostring(math.floor(editorValues.height)))
  end
  
  if(labelRowThree == nil) then
    labelRowThree = fnew(LinearLayout,context)
    labelRowThree:setLayoutParams(fillparams)
    advancedPage:addView(labelRowThree)
  end
  
  if(labelSizeLabel == nil) then
    labelSizeLabel = fnew(TextView,context)
    labelSizeLabel:setLayoutParams(fillparams)
    labelSizeLabel:setGravity(GRAVITY_CENTER)
    labelSizeLabel:setText("Label Font Size")
    labelSizeLabel:setTextSize(textSizeSmall)
    labelRowThree:addView(labelSizeLabel)
  end
  
  if(widthLabel == nil) then
    widthLabel = fnew(TextView,context)
    widthLabel:setLayoutParams(fillparams)
    widthLabel:setGravity(GRAVITY_CENTER)
    widthLabel:setText("Width")
    widthLabel:setTextSize(textSizeSmall)
    labelRowThree:addView(widthLabel)
  end
  
  if(heightLabel == nil) then
    heightLabel = fnew(TextView,context)
    heightLabel:setLayoutParams(fillparams)
    heightLabel:setGravity(GRAVITY_CENTER)
    heightLabel:setText("Height")
    heightLabel:setTextSize(textSizeSmall)
    labelRowThree:addView(heightLabel)
  --invisLabel:setVisibility(View.INVISIBLE)
  end
  
  if(controlRowTwo == nil) then
    controlRowTwo = fnew(LinearLayout,context)
    controlRowTwo:setLayoutParams(fillparams)
    advancedPage:addView(controlRowTwo)
  end
  controlRowTwo:setVisibility(View.VISIBLE)
  
  if(controlHolderD == nil) then
    controlHolderD = fnew(LinearLayout,context)
    controlHolderD:setLayoutParams(fillparams)
    controlHolderD:setGravity(GRAVITY_CENTER)
    controlRowTwo:addView(controlHolderD)
  end
  --numbereditorParams = fnew(LinearLayoutParams,120,WRAP_CONTENT)
  if(xCoordEdit == nil) then
    xCoordEdit = fnew(EditText,context)
    xCoordEdit:setLayoutParams(numbereditorParams)
    xCoordEdit:setInputType(TYPE_CLASS_NUMBER)
    xCoordEdit:setGravity(GRAVITY_CENTER)
    xCoordEdit:setTextSize(textSize)
    controlHolderD:addView(xCoordEdit)
  end
  if(editorValues.x == "MULTI") then
    --Note("setting x string:MULTI")
    xCoordEdit:setText("")
  else
    --Note("setting x string:"..editorValues.x)
    xCoordEdit:setText(tostring(math.floor(editorValues.x)))
  end
  
  if(controlHolderE == nil) then
    controlHolderE = fnew(LinearLayout,context)
    controlHolderE:setLayoutParams(fillparams)
    controlHolderE:setGravity(GRAVITY_CENTER)
    controlRowTwo:addView(controlHolderE)
  end
  --numbereditorParams = fnew(LinearLayoutParams,120,WRAP_CONTENT)
  if(yCoordEdit == nil) then
    yCoordEdit = fnew(EditText,context)
    yCoordEdit:setLayoutParams(numbereditorParams)
    yCoordEdit:setInputType(TYPE_CLASS_NUMBER)
    yCoordEdit:setGravity(GRAVITY_CENTER)
    yCoordEdit:setTextSize(textSize)
    controlHolderE:addView(yCoordEdit)
  end
  if(editorValues.y == "MULTI") then
    yCoordEdit:setText("")
  else
    yCoordEdit:setText(tostring(math.floor(editorValues.y)))
  end
  
  if(controlHolderF == nil) then
    controlHolderF = fnew(LinearLayout,context)
    controlHolderF:setLayoutParams(fillparams)
    controlHolderF:setGravity(GRAVITY_CENTER)
    controlRowTwo:addView(controlHolderF)
  end
  
  if(invisibleControl == nil) then
    invisibleControl = fnew(View,context)
    invisibleControl:setVisibility(View.INVISIBLE)
    invisibleControl:setLayoutParams(fillparams)
    controlHolderF:addView(invisibleControl)
  end
  
  if(labelRowFour == nil) then
    labelRowFour = fnew(LinearLayout,context)
    labelRowFour:setLayoutParams(fillparams)
    advancedPage:addView(labelRowFour)
  end
  
  if(xcoordLabel == nil) then
    xcoordLabel = fnew(TextView,context)
    xcoordLabel:setLayoutParams(fillparams)
    xcoordLabel:setGravity(GRAVITY_CENTER)
    xcoordLabel:setText("X Coord")
    xcoordLabel:setTextSize(textSizeSmall)
    labelRowFour:addView(xcoordLabel)
  end
  
  if(ycoordLabel == nil) then
    ycoordLabel = fnew(TextView,context)
    ycoordLabel:setLayoutParams(fillparams)
    ycoordLabel:setGravity(GRAVITY_CENTER)
    ycoordLabel:setText("Y Coord")
    ycoordLabel:setTextSize(textSizeSmall)
    labelRowFour:addView(ycoordLabel)
  end
  
  if(invisControlLabel == nil) then
    invisControlLabel = fnew(TextView,context)
    invisControlLabel:setLayoutParams(fillparams)
    invisControlLabel:setGravity(GRAVITY_CENTER)
    invisControlLabel:setText("FlipLabel")
    invisControlLabel:setTextSize(textSizeSmall)
    invisControlLabel:setVisibility(View.INVISIBLE)
    labelRowFour:addView(invisControlLabel)
  end
  
  return advancedPageScroller
  
end

function getEditorValues()
  local tmp = {}
    tmp.xCoord = tonumber(xCoordEdit:getText():toString())
    tmp.yCoord = tonumber(yCoordEdit:getText():toString())
    tmp.name = nameEdit:getText():toString()
    tmp.target = targetEdit:getText():toString()
    tmp.normalColor = normalColor
    tmp.flipColor = flipColor
    tmp.pressedColor = pressedColor
    tmp.normalLabelColor = normalLabelColor
    tmp.flipLabelColor = flipLabelColor
    tmp.labelSize = tonumber(labelSizeEdit:getText():toString())
    tmp.height = tonumber(heightEdit:getText():toString())
    tmp.width = tonumber(widthEdit:getText():toString())
  return tmp
end

makeLabel = function(text,textSize,gravity,params)
  local tmp = quicknew(TextView,context)
  tmp:setLayoutParams(params)
  tmp:setText(text)
  tmp:setTextSize(textSize)
  tmp:setGravity(gravity)
  return tmp
end

makeEdit = function(params,pTextSize)
  local tmp = quicknew(EditText,context)
  tmp:setLines(1)
  tmp:setLayoutParams(params)
  tmp:setTextSize(pTextSize)
  return tmp
end

swatchClickListener = luajava.createProxy("android.view.View$OnClickListener",{
  onClick = function(v)
    selectedColorField = v:getTag()
    local color = 0
    if(selectedColorField == "flip") then
      color = flipColor
    elseif(selectedColorField == "normal") then
      color = normalColor
    elseif(selectedColorField == "pressed") then
      color = pressedColor
    elseif(selectedColorField == "label") then
      color = normalLabelColor
    elseif(selectedColorField == "flipLabel") then
      color = flipLabelColor
    end
    colorpickerdialog = luajava.newInstance("com.offsetnull.bt.button.ColorPickerDialog",context,colorPickerDoneListener,color)
    colorpickerdialog:show()
  end
})

colorPickerDoneListener = luajava.createProxy("com.offsetnull.bt.button.ColorPickerDialog$OnColorChangedListener",{
  colorChanged = function(color)
    if(selectedColorField == "flip") then
      flipColorPicker:setBackgroundColor(color)
      flipColorPicker:invalidate()
      flipColor = color
    elseif(selectedColorField == "normal") then
      normalColorPicker:setBackgroundColor(color)
      normalColorPicker:invalidate()
      normalColor = color;
    elseif(selectedColorField == "pressed") then
      pressedColorPicker:setBackgroundColor(color)
      pressedColorPicker:invalidate()
      pressedColor = color
    elseif(selectedColorField == "label") then
      normalLabelColorPicker:setBackgroundColor(color)
      normalLabelColorPicker:invalidate()
      normalLabelColor = color
    elseif(selectedColorField == "flipLabel") then
      flipLabelColorPicker:setBackgroundColor(color)
      flipLabelColorPicker:invalidate()
      flipLabelColor = color
    end
  end
})

showSetEditorControls = function()
  buttonTargetSetRow:setVisibility(View.GONE)
  buttonNameRow:setVisibility(View.VISIBLE)
  controlRowTwo:setVisibility(View.GONE)
  labelRowFour:setVisibility(View.GONE)
end

showButtonEditorControls = function()
  controlRowTwo:setVisibility(View.VISIBLE)
  labelRowFour:setVisibility(View.VISIBLE)
  buttonNameRow:setVisibility(View.VISIBLE)
  buttonTargetSetRow:setVisibility(View.VISIBLE)
end

