
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
	self.__index = self
	return o
end

BUTTON_DATA = 	 { 	
						x				= 100,
						y				= 100,
						height 			= 80,
						width 			= 80,
						command 		= "",
						label 			= "LABEL",
						labelSize 		= 23,
						flipLabel		= "",
						flipCommand 	= "",
						primaryColor 	= Color:argb(0x88,0x00,0x00,0xFF),
						labelColor		= Color:argb(0xAA,0xAA,0xAA,0xAA),
						selectedColor 	= Color:argb(0x88,0x00,0xFF,0x00),
						flipColor 		= Color:argb(0x88,0xFF,0x00,0x00),
						flipLabelColor 	= Color:argb(0x88,0x00,0x00,0xFF)				
			  	 }
function BUTTON_DATA:new(o,default)
	o = o or {}
	setmetatable(o,self)
	self.__index = self
	if(default ~= nil) then
		o:updateToDefaults(default)
	end
	return o
end

function BUTTON_DATA:updateToDefaults(default)

	self.height = default.height
	self.width = default.width
	self.labelSize = default.labelSize
	self.primaryColor = default.primaryColor
	self.labelColor = default.labelColor
	self.selectedColor = default.selectedColor
	self.flipColor = default.flipColor
	self.flipLabelColor = default.flipLabelColor
	
end

BUTTON = {} -- this class is purley a factory. these represent "in use" buttons
function BUTTON:new(default)
	local o = {}
	o.paintOpts = luajava.bindClass("android.graphics.Paint")
	o.rect = luajava.newInstance("android.graphics.RectF")
	o.data = BUTTON_DATA:new(nil,default)
	o.setmetatable(o,self)
	self.__index = self
	o:updateRect()
end

function BUTTON:updateRect()
	--local r = self.rect
	local left = self.data.x - (b.width/2)
	local right = self.data.x + (b.width/2)
	local top = self.data.y - (b.height/2)
	local bottom = self.data.y + (b.height/2)
	local tmp = b.rect
	tmp:set(left,top,right,bottom)
end

function BUTTON:draw(state,canvas)
	if(canvas == nil) then
		error("canvas parameter must not be null")
	end
	
	local usestate = 0
	local p = self.paintOpts
	local canvas = buttonCanvas
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
	
	
	canvas:drawRoundRect(b.rect,5,5,b.paintOpts)
	--c:drawBitmap(bmp,b.x,b.y,nil)
	local label = nil
	if(usestate == 0 or usestate == 1) then
		p:setColor(self.data.labelColor)
		p:setTextSize(self.data.labelSize)
		label = self.data.label
	elseif(usestate == 2)
		p:setColor(self.data.flipLabelColor)
		label = self.data.flipLabel
	end
	local tX = self.data.x - (p:measureText(label)/2)
	local tY = self.data.y + (p:getTextSize()/2)
	
	canvas:drawText(label,tX,tY,p)
end