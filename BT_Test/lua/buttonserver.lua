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
	
	
	lob.set = buttonsets[args]
	lob.default = buttonset_defaults[args]
	
	if(set ~= nil) then
		--local lob = {}
		current_set = args
		--debugPrint(serialize(lob))
		WindowXCallS("button_window","loadButtons",serialize(lob))
		
	end
	
	
end

RegisterSpecialCommand("loadset","loadButtonSet")

current_set = DEFAULT

function saveButtons(arg)
	debugPrint("SAVE BUTTONS IMPL")
	
	local tmp = loadstring(arg)()
	
	buttonsets[current_set] = tmp
	--printTable("arg",arg)
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
	end 
	local sColorStr = a:getValue("","selectedColor")
	if(sColorStr ~=nil) then
		local BigInt = luajava.newInstance("java.math.BigInteger",sColorStr,16)
		tmp.selectedColor = BigInt:intValue()
	end 
	local fColorStr = a:getValue("","flipColor")
	if(fColorStr ~=nil) then
		local BigInt = luajava.newInstance("java.math.BigInteger",fColorStr,16)
		tmp.selectedColor = BigInt:intValue()
	end 
	local lColorStr = a:getValue("","labelColor")
	if(lColorStr ~=nil) then
		local BigInt = luajava.newInstance("java.math.BigInteger",lColorStr,16)
		tmp.labelColor = BigInt:intValue()
	end 
	local flColorStr = a:getValue("","flipLabelColor")
	if(flColorStr ~=nil) then
		local BigInt = luajava.newInstance("java.math.BigInteger",flColorStr,16)
		tmp.flipLabelColor = BigInt:intValue()
	end 
	--tmp.primaryColor = a:getValue("","priary
	buttonset_defaults[tmp.name] = tmp
	working_set = tmp.name
	
	printTable(string.format("defaults[%s]",working_set),buttonset_defaults[working_set])
	
end

button = {}
function button.start(a)
	debugPrint("NEW BUTTON:"..working_set)
	
	local tmp = {}
	tmp.x = a:getValue("","x")
	tmp.y = a:getValue("","y")
	tmp.label = a:getValue("","label") or ""
	tmp.flipLabel = a:getValue("","flipLabel") or ""
	tmp.labelSize = a:getValue("","labelSize") or 23
	
	local pColorStr = a:getValue("","primaryColor")
	if(pColorStr ~=nil) then
		local BigInt = luajava.newInstance("java.math.BigInteger",pColorStr,16)
		tmp.primaryColor = BigInt:intValue()
	end 
	local sColorStr = a:getValue("","selectedColor")
	if(sColorStr ~=nil) then
		local BigInt = luajava.newInstance("java.math.BigInteger",sColorStr,16)
		tmp.primaryColor = BigInt:intValue()
	end 
	local fColorStr = a:getValue("","flipColor")
	if(fColorStr ~=nil) then
		local BigInt = luajava.newInstance("java.math.BigInteger",fColorStr,16)
		tmp.primaryColor = BigInt:intValue()
	end 
	local lColorStr = a:getValue("","labelColor")
	if(lColorStr ~=nil) then
		local BigInt = luajava.newInstance("java.math.BigInteger",lColorStr,16)
		tmp.primaryColor = BigInt:intValue()
	end 
	local flColorStr = a:getValue("","flipLabelColor")
	if(flColorStr ~=nil) then
		local BigInt = luajava.newInstance("java.math.BigInteger",flColorStr,16)
		tmp.primaryColor = BigInt:intValue()
	end 
	
	if(buttonsets[working_set] == nil) then
		buttonsets[working_set] = {}
	end
	table.insert(buttonsets[working_set],tmp)
	--printTable(string.format("buttonsets[%s]",working_set),buttonsets)
	
end

bset_cb = luajava.createProxy("android.sax.StartElementListener",bset)
button_cb = luajava.createProxy("android.sax.StartElementListener",button)

function OnPrepareXML(root)
	--debugPrint("XMLLXLXLXLMXMXLMXLXMLXMXLMLLXMLXMXLXMLXMX")
	sets = root:getChild("buttonsets")
	set = sets:getChild("buttonset")
	button = set:getChild("button")

	set:setStartElementListener(bset_cb)
	button:setStartElementListener(button_cb)
end
debugPrint("loaded button prototypes")

function getButtonSetList(s)
	setdata = {}
	for i,v in pairs(buttonsets) do
		setdata[i] = #v
	end
	
	WindowXCallS("button_window","showButtonList",serialize(setdata))
end





