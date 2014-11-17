local density = _G["density"]
local Context = _G["Context"]
local LinearLayoutParams = _G["LinearLayoutParams"]
local ORIENTATION_LANDSCAPE = _G["ORIENTATION_LANDSCAPE"]

module(...)

getDialogDimensions = function(context)
  --local context = view:getContext()
  local wm = context:getSystemService(Context.WINDOW_SERVICE)
  local display = wm:getDefaultDisplay()
  local displayWidth = display:getWidth()
  local displayHeight = display:getHeight()
  local use = displayWidth
  local orientation = context:getResources():getConfiguration().orientation
  if(displayHeight < displayWidth) then
    use = displayHeight
  end
  
  local dpi_bucket = use / density
  
  local height_param = LinearLayoutParams.WRAP_CONTENT
  local width_param = 450*density
  
  if(orientation == ORIENTATION_LANDSCAPE) then
    --landscape
    if(dpi_bucket >= 600) then
      height_param = 300*density
    end
    
    if(width_param > displayWidth) then
      width_param = displayHeight
    end
  else
    --portrait
        --landscape
    if(dpi_bucket >= 600) then
      height_param = 300*density
      
    end
    
    if(width_param > displayWidth) then
      width_param = displayWidth-(5*density)
    end
  end
  return width_param,height_param
end