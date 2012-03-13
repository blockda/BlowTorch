--debugPrint("package path:"..package.path)
package.path = "/mnt/sdcard/BlowTorch/?.lua"
--debugPrint("package path:"..package.path)
require("button")
require("serialize")
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
		WindowXCallS("button_window","loadButtons",serialize(lob))
		
	end
	
	
end

function loadAndEditSet(data)
	lastSelectedSet = data
	
	lob.name = data
	lob.set = buttonsets[data]
	lob.default = buttonset_defaults[data]
	
	if(lob.set ~= nil) then
		current_set = data
		WindowXCallS("button_window","loadAndEditSet",serialize(lob))
	end
end

RegisterSpecialCommand("loadset","loadButtonSet")

current_set = DEFAULT

function saveButtons(arg)
	debugPrint("SAVE BUTTONS IMPL")
	
	local tmp = loadstring(arg)()
	
	buttonsets[current_set] = tmp
	--printTable("arg",arg)
	saveSettings()
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
end

function legacyButtonsImported()
	debugPrint("doing button import")
	printTable("buttonsets",buttonsets)
	printTable("buttonset_defaults",buttonset_defaults)
end


