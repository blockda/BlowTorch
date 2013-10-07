
Color = luajava.bindClass("android.graphics.Color")
Path = luajava.bindClass("android.graphics.Path")
PathDirection = luajava.bindClass("android.graphics.Path$Direction")
statusoffset = 0

buttonRoundness = 6
--PathDirection_CCW = PathDirection.CCW
--RegionOp = luajava.bindClass("android.graphics.Region$Op")
--RegionOp_REVERSE_DIFFERENCE = RegionOp.INTERSECT
BUTTONSET_DATA = {
						height 			= 48,
						width 			= 48,
						labelSize 		= 16,
						primaryColor 	= Color:argb(0x88,0x00,0x00,0xFF),
						labelColor		= Color:argb(0xAA,0xAA,0xAA,0xAA),
						selectedColor 	= Color:argb(0x88,0x00,0xFF,0x00),
						flipColor 		= Color:argb(0x88,0xFF,0x00,0x00),
						flipLabelColor 	= Color:argb(0x88,0x00,0x00,0xFF),
						command = "",
						label = "LABEL",
						flipLabel = "",
						flipCommand = "",
						switchTo = "",			
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
						--command 		= "",
						--label 			= "LABEL",
						--labelSize 		= 23,
						--flipLabel		= "",
						--flipCommand 	= "",
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
function BUTTON:new(data,density)
	local o = {}
	o.paintOpts = luajava.newInstance("android.graphics.Paint")
	o.paintOpts:setAntiAlias(true)
	o.paintOpts:setXfermode(xferModeSRC)
	o.rect = luajava.newInstance("android.graphics.RectF")
	o.inset = luajava.newInstance("android.graphics.RectF")
	--o.clip = luajava.new(Path)
	--o.clip:addRoundRect(o.rect,25,25,PathDirection_CCW)
	o.data = BUTTON_DATA:new(data)
	o.selected = false
	setmetatable(o,self)
	self.__index = self
	o.density = density
	o:updateRect(statusoffset)
	
	
	return o
end

--BUTTON.__index = BUTTON

--function BUTTON:new(data,default)

--end

function BUTTON:updateRect(statusoffset)
	--local r = self.rect
	local left = self.data.x - (self.data.width/2)*self.density
	local right = self.data.x + (self.data.width/2)*self.density
	local top = self.data.y - (self.data.height/2)*self.density + statusoffset
	local bottom = self.data.y + (self.data.height/2)*self.density + statusoffset
	local tmp = self.rect

	
	tmp:set(left,top,right,bottom)
	self.inset:set(left+1.0,top+1.0,right-1.0,bottom-1.0)
	--self.clip:reset()
	--temporarily adjust this rect.
	--debugPrint(tmp:toString())
	--tmp:inset(10.0,10.0)
	--debugPrint(tmp:toString())
	--self.clip:addRoundRect(tmp,25,25,PathDirection_CCW)
	--tmp:offset(1.0,1.0)
	--debugPrint(tmp:toString())
end

function BUTTON:draw(state,canvas)
	if(canvas == nil) then
		error("canvas parameter must not be null")
	end
	
	--canvas:save()
	--canvas:clipPath(self.clip,RegionOp_REVERSE_DIFFERENCE)
	
	
	local usestate = 0
	local p = self.paintOpts
	--local canvas = buttonCanvas
	if(state ~= nil) then
		usestate = state
	end
	
	local rect = self.rect
	
	if(usestate == 0) then
		p:setColor(self.data.primaryColor)
		canvas:drawRoundRect(rect,buttonRoundness,buttonRoundness,p)
	elseif(usestate == 1) then
		p:setColor(self.data.selectedColor)
		canvas:drawRoundRect(self.inset,buttonRoundness,buttonRoundness,p)
	elseif(usestate == 2) then
		p:setColor(self.data.flipColor)
		canvas:drawRoundRect(self.inset,buttonRoundness,buttonRoundness,p)
	end
	
	
	
	--canvas:drawRect(self.rect,p)
	--c:drawBitmap(bmp,b.x,b.y,nil)
	local label = nil
	if(usestate == 0 or usestate == 1) then
		p:setColor(self.data.labelColor)
		--debugPrint(string.format("LabelSize:%d",tonumber(self.data.labelSize)))
		p:setTextSize(tonumber(self.data.labelSize)*self.density)
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
	local tY = self.data.y + (p:getTextSize()/2) + statusoffset
	
	canvas:drawText(label,tX,tY,p)
	--canvas:restore()
	--debugPrint(string.format("Drawn button at: x=%d y=%d",self.data.x,self.data.y))
end

PorterDuffMode = luajava.bindClass("android.graphics.PorterDuff$Mode")
xferModeClear = luajava.newInstance("android.graphics.PorterDuffXfermode",PorterDuffMode.CLEAR)
xferModeSRC = luajava.newInstance("android.graphics.PorterDuffXfermode",PorterDuffMode.SRC)

function BUTTON:clearButton(canvas)
--local canvas = buttonCanvas
	local p = self.paintOpts
	p:setXfermode(xferModeClear)
	canvas:drawRoundRect(self.rect,5,5,p)
	p:setXfermode(nil)
end