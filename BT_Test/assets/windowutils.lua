counter = 0

function drawTrigger(message)
	canvas:clearCanvas()
	--canvas:drawColor(0x00000000)
	density = paint:density()
	--Note("density="..density)
	height = 15 * density
	bottom = 30 * density
	left = 30 * density
	
	top = bottom - height
	
	
	paint:setTextSize(height)
	paint:setAntiAlias(true)
	
	width = paint:measureText(message)
	right = left+width;
	
	
	paint:color(0xFF333333)
	canvas:drawRect(left,top,right,bottom,paint)
	paint:color(0xFFEEEEEE)
	canvas:drawText(message,left,bottom,paint)
	counter = counter + 1
	updateWindow()
end


--function draw()
--	paint:setTextSize(13)
--	paint:setAntiAlias(true)
--	canvas:drawText("Drawing from lua!!!",200,200,paint)
--	paint:color(0xFF0000FF)
--	canvas:drawRect(100,100,110,110,paint)

--end


--draw()