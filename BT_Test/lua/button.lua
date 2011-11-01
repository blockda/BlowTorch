
Color = luajava.bindClass("android.graphics.Color")

BUTTONSET_DATA = {
						height 			= 80,
						width 			= 80,
						labelSize 		= 23,
						primaryColor 	= Color:argb(0x88,0x00,0x00,0xFF),
						labelColor		= Color:argb(0xAA,0xAA,0xAA,0xAA),
						selectedColor 	= Color:argb(0x88,0x00,0xFF,0x00),
						flipColor 		= Color:argb(0x88,0xFF,0x00,0x00),
						flipLabelColor 	= Color:argb(0x88,0x00,0x00,0xFF)			
			  		}
function BUTTONSET_DATA:new(o)
	o = o or {}
	setmetatable(o,self)
	--self.__index = self
	return o
end

BUTTONSET_DATA.__index = BUTTONSET_DATA
--setmetatable(BUTTONSET_DATA,BUTTONSET_DATA)

BUTTON_DATA = 	 { 	
						x				= 100,
						y				= 100,
						--height 			= 80,
						--width 			= 80,
						command 		= "",
						label 			= "LABEL",
						--labelSize 		= 23,
						flipLabel		= "",
						flipCommand 	= "",
						--primaryColor 	= Color:argb(0x88,0x00,0x00,0xFF),
						--labelColor		= Color:argb(0xAA,0xAA,0xAA,0xAA),
						--selectedColor 	= Color:argb(0x88,0x00,0xFF,0x00),
						--flipColor 		= Color:argb(0x88,0xFF,0x00,0x00),
						--flipLabelColor 	= Color:argb(0x88,0x00,0x00,0xFF)				
			  	 }
--setmetatable(BUTTON_DATA,BUTTONSET_DATA)
--BUTTON_DATA.__index = BUTTONSET_DATA
function BUTTON_DATA:new(o)
	o = o or {}
	setmetatable(o,self)
	--if(self.__index == nil) then
	--	self.__index = default or BUTTONSET_DATA:new()
	--end
	--setmetatable(self,default or BUTTONSET_DATA)
	--if(default ~= nil) then
	--	self.__index = default
	--else
	--	self.__index = BUTTONSET_DATA
	--end
	--if(default ~= nil) then
		--o:updateToDefaults(default)
	--end
	return o
end

BUTTON_DATA.__index = BUTTONSET_DATA
--setmetatable(BUTTON_DATA,BUTTONSET_DATA)

BUTTON = {} -- this class is purley a factory. these represent "in use" buttons
function BUTTON:new(data)
	local o = {}
	o.paintOpts = luajava.newInstance("android.graphics.Paint")
	o.paintOpts:setAntiAlias(true)
	o.rect = luajava.newInstance("android.graphics.RectF")
	o.data = BUTTON_DATA:new(data)
	o.selected = false
	setmetatable(o,self)
	self.__index = self
	o:updateRect()
	
	return o
end

--BUTTON.__index = BUTTON

--function BUTTON:new(data,default)

--end

function BUTTON:updateRect()
	--local r = self.rect
	local left = self.data.x - (self.data.width/2)
	local right = self.data.x + (self.data.width/2)
	local top = self.data.y - (self.data.height/2)
	local bottom = self.data.y + (self.data.height/2)
	local tmp = self.rect
	tmp:set(left,top,right,bottom)
end

function BUTTON:draw(state,canvas)
	if(canvas == nil) then
		error("canvas parameter must not be null")
	end
	
	local usestate = 0
	local p = self.paintOpts
	--local canvas = buttonCanvas
	if(state ~= nil) then
		usestate = state
	end
	
	if(usestate == 0) then
		p:setColor(self.data.primaryColor)
	elseif(usestate == 1) then
		p:setColor(self.data.selectedColor)
	elseif(usestate == 2) then
		p:setColor(self.data.flipColor)
	end
	
	
	canvas:drawRoundRect(self.rect,5,5,p)
	--c:drawBitmap(bmp,b.x,b.y,nil)
	local label = nil
	if(usestate == 0 or usestate == 1) then
		p:setColor(self.data.labelColor)
		--debugPrint(string.format("LabelSize:%d",tonumber(self.data.labelSize)))
		p:setTextSize(tonumber(self.data.labelSize))
		label = self.data.label
	elseif(usestate == 2) then
		p:setColor(self.data.flipLabelColor)
		if(self.data.flipLabel == "" or self.data.flipLabel == nil) then
			label = self.data.label
		else
			label = self.data.flipLabel
		end
		
	end
	local tX = self.data.x - (p:measureText(label)/2)
	local tY = self.data.y + (p:getTextSize()/2)
	
	canvas:drawText(label,tX,tY,p)
	--debugPrint(string.format("Drawn button at: x=%d y=%d",self.data.x,self.data.y))
end

PorterDuffMode = luajava.bindClass("android.graphics.PorterDuff$Mode")
xferMode = luajava.newInstance("android.graphics.PorterDuffXfermode",PorterDuffMode.CLEAR)

function BUTTON:clearButton(canvas)
--local canvas = buttonCanvas
	local p = self.paintOpts
	p:setXfermode(xferMode)
	canvas:drawRoundRect(self.rect,5,5,p)
	p:setXfermode(nil)
end