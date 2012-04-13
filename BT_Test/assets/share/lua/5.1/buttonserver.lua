--debugPrint("package path:"..package.path)
package.path = package.path..";"..GetExternalStorageDirectory().."/BlowTorch/?.lua"
--package.cpath = GetExternalStorageDirectory().."/BlowTorch/?.so"

--debugPrint("package path:"..package.path)
require("button")
require("serialize")
local marshal = require("marshal")
--this is the back end of the script, it will take care of reading/writing buttons to disk
--and storing all the loaded buttons in memory.

--communicates back and fourth with the window script to huck data.
debugPrint("STARTING THE WHO BOOTSTRAP SEQUENCE!")
buttonsets = {} --raw table, holds tables of buttons.
buttonset_defaults = {} --raw table, holds defaults for a set name.

set_def = BUTTONSET_DATA:new()
set = {}

local b1 = BUTTON_DATA:new({x=200,y=200,label="butt1"})
local b2 = BUTTON_DATA:new({x=500,y=360,label="butt2"}) --raw default.

table.insert(set,b1)
table.insert(set,b2)

--buttonset_defaults["DEFAULT"] = set_def
--buttonsets["DEFAULT"] = set

alt = {}
local b3 = BUTTON_DATA:new({x=300,y=200})
local b4 = BUTTON_DATA:new({x=400,y=200}) 

b3.label = "YEA"
b4.label = "HEA!"

table.insert(alt,b3)
table.insert(alt,b4)

--buttonset_defaults["ALT"] = set_def
--buttonsets["ALT"] = alt

lob = {}


function loadButtonSet(args)
	
	--debugPrint("trying to load.."..args.." setcount:"..#buttonsets)
	
	--for i,b in pairs(buttonsets) do
	--	printTable(i,b)
	--end
	
	lob.name = args
	lob.set = buttonsets[args]
	lob.default = buttonset_defaults[args]
	
	if(lob.set ~= nil) then
		--local lob = {}
		current_set = args
		--debugPrint(serialize(lob))
		--WindowXCallS("button_window","loadButtons",serialize(lob))
		--local orig = { answer = 42}
		
		
		--assert(marshal.encode(orig))
		
		--local str = marshal.encode(orig)
		--debugPrint("attempting byte dump")
		--for i=1,#str do
		--	local c = str:sub(i,i)
			--local df = tonumber(c)
			--if(df ~= nil) then
		--		debugPrint("byte: "..string.byte(c))
			--else
			--	debugPrint("byte nil, probably 0x8e");
			--end
		--end
		
		
		--debugPrint("trying to copy")
		--local copy = marshal.clone(orig)
		--debugPrint("cloned value"..copy.answer)
		--local copy = marshal.decode(str)
		
		--debugPrint(copy.answer)
		
		--debugPrint(str)
		WindowXCallB("button_window","loadButtons",marshal.encode(lob))
		
	end
	
	
end

function loadAndEditSet(data)
	lastSelectedSet = data
	
	lob.name = data
	lob.set = buttonsets[data]
	lob.default = buttonset_defaults[data]
	
	if(lob.set ~= nil) then
		current_set = data
		--WindowXCallS("button_window","loadAndEditSet",serialize(lob))
		WindowXCallB("button_window","loadAndEditSet",marshal.encode(lob))
	end
end

RegisterSpecialCommand("loadset","loadButtonSet")
RegisterSpecialCommand("clearbuttons","clearButtons")

current_set = DEFAULT

function clearButtons()
	--all that needs to be done is call into the window to kick the process off
	WindowXCallS("button_window","clearButtons","")
end

function saveButtons(arg)
	debugPrint("SAVE BUTTONS IMPL")
	
	local tmp = loadstring(arg)()
	
	buttonsets[current_set] = tmp
	--printTable("arg",arg)
	saveSettings()
end

function makeNewButtonSet(name)
	buttonset_defaults[name] = {}
	buttonsets[name] = {}
	loadAndEditSet(name)
end


function printTable(key,o)
	for i,v in pairs(o) do
		if(type(v)=="table") then
			printTable(key.."."..i,v)
		else 
			debugPrint(key.."."..i.."<==>"..v)
		end
	end
end

bset = {}
working_set = nil
function bset.start(a)
	
	
	local tmp = {}
	tmp.name = a:getValue("","name") or "default"
	debugPrint("NEW BUTTON SET:"..tmp.name)
	tmp.width = a:getValue("","width") or "80"
	tmp.height = a:getValue("","height") or "80"
	tmp.labelSize = a:getValue("","labelSize") or "23"
	local pColorStr = a:getValue("","primaryColor")
	if(pColorStr ~=nil) then
		local BigInt = luajava.newInstance("java.math.BigInteger",pColorStr,16)
		tmp.primaryColor = BigInt:intValue()
		--tmp.primaryColor = tonumber(pColorStr,16)
	end 
	local sColorStr = a:getValue("","selectedColor")
	if(sColorStr ~=nil) then
		local BigInt = luajava.newInstance("java.math.BigInteger",sColorStr,16)
		tmp.selectedColor = BigInt:intValue()
		--tmp.selectedColor = tonumber(sColorStr,16)
	end 
	local fColorStr = a:getValue("","flipColor")
	if(fColorStr ~=nil) then
		local BigInt = luajava.newInstance("java.math.BigInteger",fColorStr,16)
		tmp.selectedColor = BigInt:intValue()
		--tmp.flipColor = tonumber(fColorStr,16)
	end 
	local lColorStr = a:getValue("","labelColor")
	if(lColorStr ~=nil) then
		local BigInt = luajava.newInstance("java.math.BigInteger",lColorStr,16)
		tmp.labelColor = BigInt:intValue()
		--tmp.labelColor = tonumber(lColorStr,16)
	end 
	local flColorStr = a:getValue("","flipLabelColor")
	if(flColorStr ~=nil) then
		local BigInt = luajava.newInstance("java.math.BigInteger",flColorStr,16)
		tmp.flipLabelColor = BigInt:intValue()
		--tmp.selectedColor = tonumber(flColorStr,16)
	end 
	--tmp.primaryColor = a:getValue("","priary
	buttonset_defaults[tmp.name] = tmp
	working_set = tmp.name
	
	--printTable(string.format("defaults[%s]",working_set),buttonset_defaults[working_set])
	
end

button = {}
function button.start(a)
	--debugPrint("NEW BUTTON:"..working_set)
	
	local tmp = {}
	tmp.x = a:getValue("","x")
	tmp.y = a:getValue("","y")
	tmp.label = a:getValue("","label") or ""
	tmp.flipLabel = a:getValue("","flipLabel") or ""
	tmp.labelSize = a:getValue("","labelSize") or 23
	tmp.command = a:getValue("","command") or ""
	tmp.flipCommand = a:getValue("","flipCommand") or ""
	tmp.name = a:getValue("","name")
	tmp.height = a:getValue("","height")
	tmp.width = a:getValue("","width")
	
	local pColorStr = a:getValue("","primaryColor")
	if(pColorStr ~=nil) then
		local BigInt = luajava.newInstance("java.math.BigInteger",pColorStr,16)
		tmp.primaryColor = BigInt:intValue()
		--tmp.primaryColor = tonumber(pColorStr,16)
	end 
	local sColorStr = a:getValue("","selectedColor")
	if(sColorStr ~=nil) then
		local BigInt = luajava.newInstance("java.math.BigInteger",sColorStr,16)
		tmp.primaryColor = BigInt:intValue()
		--tmp.selectedColor = tonumber(sColorStr,16)
	end 
	local fColorStr = a:getValue("","flipColor")
	if(fColorStr ~=nil) then
		local BigInt = luajava.newInstance("java.math.BigInteger",fColorStr,16)
		tmp.primaryColor = BigInt:intValue()
		--tmp.flipColor = tonumber(fColorStr,16)
	end 
	local lColorStr = a:getValue("","labelColor")
	if(lColorStr ~=nil) then
		local BigInt = luajava.newInstance("java.math.BigInteger",lColorStr,16)
		tmp.primaryColor = BigInt:intValue()
		--tmp.labelColor = tonumber(lColorStr,16)
	end 
	local flColorStr = a:getValue("","flipLabelColor")
	if(flColorStr ~=nil) then
		local BigInt = luajava.newInstance("java.math.BigInteger",flColorStr,16)
		tmp.primaryColor = BigInt:intValue()
		--tmp.flipLabelColor = tonumber(flColorStr,16)
	end 
	
	if(buttonsets[working_set] == nil) then
		buttonsets[working_set] = {}
	end
	table.insert(buttonsets[working_set],tmp)
	--printTable(string.format("buttonsets[%s]",working_set),buttonsets)
	
end

bset_cb = luajava.createProxy("android.sax.StartElementListener",bset)
button_cb = luajava.createProxy("android.sax.StartElementListener",button)

function handleSelected(body)
	debugPrint("found the selectedNode:"..body)
	current_set = body
end

selectedListener = {}
selectedListener["end"] = handleSelected


selected_cb = luajava.createProxy("android.sax.TextElementListener",selectedListener)

function handleButtonSerializer(body)
	debugPrint("doing string serailze for buttons")
	buttonsets = loadstring(body)()
end
buttonserializer = {}
buttonserializer["end"] = handleButtonSerializer
buttonserializer_cb = luajava.createProxy("android.sax.TextElementListener",buttonserializer)

function handleButtonSetSerializer(body)
	debugPrint("doing string serailze for buttonsets")
	buttonset_defaults = loadstring(body)()
end
buttonsetserializer = {}
buttonsetserializer["end"] = handleButtonSetSerializer
buttonsetserializer_cb = luajava.createProxy("android.sax.TextElementListener",buttonsetserializer)

function OnPrepareXML(root)
	debugPrint("XMLLXLXLXLMXMXLMXLXMLXMXLMLLXMLXMXLXMLXMX")
	sets = root:getChild("buttonsets")
	set = sets:getChild("buttonset")
	button = set:getChild("button")
	selected = sets:getChild("selected")
	
	buttons = sets:getChild("buttons")
	defaults = sets:getChild("defaults")

	set:setStartElementListener(bset_cb)
	button:setStartElementListener(button_cb)
	selected:setTextElementListener(selected_cb)
	
	buttons:setTextElementListener(buttonserializer_cb)
	defaults:setTextElementListener(buttonsetserializer_cb)
end
debugPrint("loaded button prototypes")

function getButtonSetList(s)
	
	debugPrint("getting button list")

	setdata = {}
	for i,v in pairs(buttonsets) do
		setdata[i] = #v
	end
	
	WindowXCallS("button_window","showButtonList",serialize(setdata))
end

function saveSetDefaults(data)
	defaults = loadstring(data)()
	
	buttonset_defaults[current_set] = defaults
	--wow, that was easy.
end



function OnXmlExport(out)
	--local System = luajava.bindClass("java.lang.System")
	--now = System:currentTimeMillis()
	debugPrint("buttonset save routine GO!")
	
	if(out ~= nil) then
		debugPrint("xmlserializer is not null")
	else
		debugPrint("xmlserializer is null")
	end
	--local startTag = out.startTag(out)
	--local out = xout
	local bsets = buttonsets
	local bset_defaults = buttonset_defaults
	local Integer = luajava.bindClass("java.lang.Integer")
	--local startTag = out.startTag
	--local endTag = out.endTag
	--local attribute = out.attribute
	out:startTag("","buttonsets")
	--for i,b in pairs(bsets) do
	--	out:startTag("","buttonset")
	----	debugPrint("attempting to output set"..i)
	--	local defs = bset_defaults[i]
	--	out:attribute("","name",i)
		
	--	if(defs.primaryColor ~= nil) then
	--		out:attribute("","primaryColor",Integer:toHexString(tonumber(defs.primaryColor)))
	--	end
		
	--	if(defs.selectedColor ~= nil) then
	--		out:attribute("","selectedColor",Integer:toHexString(tonumber(defs.selectedColor)))
	--	end
		
	--	if(defs.flipColor ~= nil) then
	--		out:attribute("","flipColor",Integer:toHexString(tonumber(defs.flipColor)))
	--	end
		
	--	if(defs.labelColor ~= nil) then
	--		out:attribute("","labelColor",Integer:toHexString(tonumber(defs.labelColor)))
	--	end
		
	--	if(defs.flipLabelColor ~= nil) then
	--		out:attribute("","flipLabelColor",Integer:toHexString(tonumber(defs.flipLabelColor)))
	--	end
		
	--	if(defs.labelSize ~= nil) then
	--		out:attribute("","labelSize",tostring(defs.labelSize))
	--	end
		
	--	if(defs.height ~= nil) then
	--		out:attribute("","height",tostring(defs.height))
	--	end
		
	--	if(defs.width ~= nil) then
	--		out:attribute("","width",tostring(defs.width))
	--	end
		
	--	for k,x in pairs(b) do
	--		out:startTag("","button")
			--for l,z in pairs(x) do
	--			if(x.name ~= nil) then
	--				out:attribute("","name",x.name)
	--			end
			
	--			if(rawget(x,"primaryColor") ~= nil) then
	--				out:attribute("","primaryColor",Integer:toHexString(tonumber(x.primaryColor)))
	--			end
				
	--			if(rawget(x,"selectedColor") ~= nil) then
	--				out:attribute("","selectedColor",Integer:toHexString(tonumber(x.selectedColor)))
	--			end
				
	--			if(rawget(x,"flipColor") ~= nil) then
	--				out:attribute("","flipColor",Integer:toHexString(tonumber(x.flipColor)))
	--			end
				
	--			if(rawget(x,"labelColor") ~= nil) then
	--				out:attribute("","labelColor",Integer:toHexString(tonumber(x.labelColor)))
	--			end
				
	--			if(rawget(x,"flipLabelColor") ~= nil) then
	--				out:attribute("","flipLabelColor",Integer:toHexString(tonumber(x.flipLabelColor)))
	--			end
				
	--			if(rawget(x,"labelSize") ~= nil) then
	--				out:attribute("","labelSize",tostring(x.labelSize))
	--			end
				
	--			if(rawget(x,"height") ~= nil) then
	--				out:attribute("","height",tostring(x.height))
	--			end
				
	--			if(rawget(x,"width") ~= nil) then
	--				out:attribute("","width",tostring(x.width))
	--			end
				
	--			out:attribute("","x",tostring(x["x"]))
	--			out:attribute("","y",tostring(x["y"]))
				
	--			if(rawget(x,"command") ~= nil) then
	--				out:attribute("","command",x.command)
	--			end
				
	--			if(rawget(x,"flipCommand") ~= nil) then
	--				out:attribute("","flipCommand",x.flipCommand)
	--			end
	--			
	--			if(rawget(x,"flipLabel") ~= nil) then
	--				out:attribute("","flipLabel",x.flipLabel)
	--			end
				
	--			if(rawget(x,"label") ~= nil) then
	--				out:attribute("","label",x.label)
	--			end
			--end
	--			out:endTag("","button")
	--		end	
			--end
	--	out:endTag("","buttonset")
	--end
	out:startTag("","selected")
	out:text(current_set)
	out:endTag("","selected")
	out:startTag("","buttons")
		out:cdsect(serialize(buttonsets))
	out:endTag("","buttons")
		
	out:startTag("","defaults")
		out:cdsect(serialize(buttonset_defaults))
	out:endTag("","defaults")
	out:endTag("","buttonsets")
	--delta = System:currentTimeMillis() - now
	--debugPrint("saved all buttons, took "..delta.." millis.")
end

function buttonLayerReady()
	loadButtonSet(current_set)
	loadOptions()
end

function legacyButtonsImported()
	debugPrint("doing button import")
	printTable("buttonsets",buttonsets)
	printTable("buttonset_defaults",buttonset_defaults)
end

function OnOptionChanged(key,value)
	debugPrint(key..":"..value)
	local func = optionsTable[key]
	func(value)
	
end

--boolean windowReady
function loadOptions()
	WindowXCallS("button_window","loadOptions",serialize(options))
end

function setAutoLaunch(value)
	
	options.auto_launch = value
	if(userPresent()) then
		loadOptions()
	end
end

function setAutoCreate(value)
	options.auto_create = value
	if(userPresent()) then
		loadOptions()
	end
end 

function setRoundness(value)
	options.roundness = value
	if(userPresent()) then
		loadOptions()
	end
end

function setHapticFeedbackEditor(value)
	options.haptic_edit = value
	if(userPresent()) then
		loadOptions()
	end
end

function setHapticFeedbackPressed(value)
	options.haptic_press = value
	if(userPresent()) then
		loadOptions()
	end
end

function setHapticFeedbackFlipped(value)
	options.haptic_flip = value
	if(userPresent()) then
		loadOptions()
	end
end


Integer = luajava.newInstance("java.lang.Integer",0)
IntegerClass = Integer:getClass()
RawInteger = IntegerClass.TYPE

function makeIntArray(table)
	newarray = Array:newInstance(RawInteger,#table)
	for i,v in ipairs(table) do
		index = i-1
		intval = luajava.new(Integer,v)
		Array:setInt(newarray,index,intval:intValue())
	end
	
	return newarray
end

android_R_attr = luajava.bindClass("android.R$attr")
android_R_style = luajava.bindClass("android.R$style")
android_R_dimen = luajava.bindClass("android.R$dimen")
function alignDefaultButtons()
	margin = 7
	right = 0
	left = 1000000
	bottom = 0
	top = 1000000
	density = GetDisplayDensity()
	
	local set = buttonsets["default"]
	local defaults = buttonset_defaults["default"]

	for i,b in pairs(set) do
		b.x = b.x*density
		b.y = b.y*density
		
		local width = b.width or defaults.width
		local height = b.height or defaults.height
		
		width = width*density
		height = height*density
		
		local l = b.x - width/2
		local r = b.x + width/2
		local t = b.y - height/2
		local bot = b.y + height/2
		
		if(r > right) then right = r end
		if(l < left) then left = l end
		if(t  < top) then top = t end
		if(bot > bottom) then bottom = bot end
	end
	
	heightPixels = context:getResources():getDisplayMetrics().heightPixels
	widthPixels = context:getResources():getDisplayMetrics().widthPixels
	width = widthPixels
	if(width < heightPixels) then width = heightPixels end
		
	xoffset = width - right - (margin*density)
	yoffset = 0
	pcall(function ()
		local attrs = {}
		local id = android_R_attr.actionBarSize
		--if it doesn't get here then it is not honeycomb, so no actionbar
		yoffset = 55*density
		--debugPrint("attempting dimension lookup")
		--debugPrint("ACTION BAR DIMENSION: "..android_R_dimen.action_bar_height)
	end)
	--pcall(getActionBarHeight)
	debugPrint("ACTION BAR HEIGHT IS:"..yoffset)
	for i,b in pairs(set) do
		b.x = b.x + xoffset
		b.y = b.y + yoffset
	end
end

optionsTable = {}
optionsTable.haptic_edit = setHapticFeedbackEditor
optionsTable.haptic_press = setHapticFeedbackPressed
optionsTable.haptic_flip = setHapticFeedbackFlipped
optionsTable.roundess = setRoundness
optionsTable.auto_launch = setAutoLaunch
optionsTable.auto_create = setAutoCreate

options = {}
options.haptic_edit = 0
options.haptic_press = 0
options.haptic_flip = 0
options.roundness = 6
options.auto_launch = true
options.auto_create = true

