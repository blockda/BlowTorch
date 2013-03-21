package.path = package.path..";"..GetPluginInstallDirectory().."/?.lua"
require("serialize")
require("miniwindow")

Log = luajava.bindClass("android.util.Log")
Log:e("LUA","loading lua custom debug output")

function trace (event, line)
	local info = debug.getinfo(2)
	local s = info.short_src
	local func = info.name
	if func == nil then func = "nil" end
	Log:e("LUA",s .. ":" .. line .. ":"..func)
end

function call (event,line)
	local info = debug.getinfo(2)
	local s = info.short_src
	local func = info.name
	if func == nil then func = "nil" end
	Log:e("LUA",event)
end

function ret (event,line)
	local info = debug.getinfo(2)
	local s = info.short_src
	local func = info.name
	if func == nil then func = "nil" end
	Log:e("LUA",event)
end
    
--debug.sethook(trace, "l")
Containers = {}
Containers.id = {}

Containers.views = {}

Layouts = {}
local chat_window_installed = PluginInstalled("chat_miniwindow")

Views = {}
local rootView = view:getParent()
Containers.id.root = rootView:getId()
Containers.views.root = rootView

Views.mainDisplay = 6666
Views.inputBar = 10
Views.divider1 = 40
Views.ScrollView1 = 886795
Views.ScrollTarget1 = 886794
if(chat_window_installed) then
	Views.chat_window = 5010
end

local windowTracker = {}
function AddWindow(config)
	Note("\nInstalling window, raw data: "..config.."\n")
	local config = loadstring(config)()
	
	Note(string.format("\nconfig.id = %s\n",config.id))
	--vale = true
	--if(vale) then return end
	--return
	--Note("Attempting to install window: "..config.id)
	--dump(config)
	
	if(not layout_config.configs[layout_config.mode][config.id] and not layout_config.configs[1][config.id]) then
		--never been added, add to default scroll window.
		Note("\nDoing special no config mode insertion\n")
		local props = {}
		props.target = config.target or "ScrollTarget1"
		--math.randseed(os.time())
		--props.id = math.round(math.random(4000,40000))
		props.id = config.id
		props.width = config.width or MATCH_PARENT
		props.height = config.height or 200
		props.gravity = config.gravity or 0
		props.weight = config.weight or 0
		props.type = config.type or "linear"
		props.divider = config.divider or nil
		props.indexmod = config.indexmod or nil
		
		if(not layout_config.configs[layout_config.mode][config.id]) then
			layout_config.configs[layout_config.mode][config.id] = props
			config = props
			if(not layout_config.configs[1][config.id]) then
				layout_config.configs[1][config.id] = props
			end
		else
			--check if this has a default config
			if(not layout_config.configs[1][config.id]) then
				layout_config.configs[1][config.id] = props
			end
		end
		
		--config = layout_config.configs[layout_config.mode][config.id]
		
	else
		Note("\nView already has configuration, no special mode needed.\n")
		--return
		local id = config.id
		if(not layout_config.configs[1][id]) then
			layout_config.configs[1][id] = config
		end
		config = layout_config.configs[layout_config.mode][id]

		if(not config) then
			--try the 0th config
			config = layout_config.configs[1][id]
			if(not config) then return end --no configuration can be found, bail on loading it.
		end
	end
	vis = true
	if(not vis) then Note(string.format("\nsanity check for view: %s, %s\n",config.id,config.target)) return end
	local target = Containers.views[config.target]
	if(not target) then Note("No target specified for view: "..config.id) return end
	
	
	local source = rootView:findViewById(tonumber(config.id))
	
	if(not source) then Note("Layout manager could not find source view: "..config.id) return end
	vis = false
	

	--if(not vis) then Note(string.format("\nsanity remove view : %s\n",target:getId())) return end
	if(not source) then
		--did not find the root, the script host must not remove the view first
		return
	end
	
	if(not target) then
		--target doesn't exist
		return
	else
		local params = nil
		local divider = nil
		local divider_params = nil
		--build the layout params
		if(config.type == "linear") then
			params = luajava.new(LinearLayoutParams,config.width,config.height,config.weight)
			Note(string.format("layout height:%d\n",config.height))
			--SetLinearLayoutParamGravity(params,config.gravity)
			--Note(string.format("layout gravity:%d\n",params.gravity))
			--params:setGravity(config.gravity)
			--params:setWeight(config.weight)
			if(config.divider) then
				--Note("\nmaking divider\n")
				divider = MakeDivider(context)
				divider:setId(config.id+1)
				if(target:getOrientation() == LinearLayout.VERTICAL) then
					divider_params = luajava.new(LinearLayoutParams,MATCH_PARENT,3*density)
					--SetLinearLayoutParamGravity(params,config.gravity+1)
				else
					divider_params = luajava.new(LinearLayoutParams,3*density,MATCH_PARENT)
					--SetLinearLayoutParamGravity(params,config.gravity+1)
				end
				divider:setLayoutParams(divider_params)
				--view:getParent():addView(divider)
				--InstallWindow(divider_config)
			end
		else
		--relative
			params = luajava.new(RelativeLayoutParams,config.width,config.height)
			for i,v in pairs(config.rules) do
				Note(string.format("\nAdding relative rule: %d,%d\n",i,v))
				params:addRule(i,v)
			end
			
			--do affector rules
			if(config.affects) then
			for i,v in pairs(config.affects) do
				local viewid = Views[i]
				local view = rootView:findViewById(viewid)
				local vparams = view:getLayoutParams()
				Note(string.format("\nAffecting view: %d\n",viewid))
				for rule,value in pairs(v) do
					Note(string.format("\nAffecting relative rule: %d,%d,%d\n",viewid,tonumber(rule),tonumber(value)))
					--if(value < 0) then
						--vparams:removeRule(tonumber(rule))
					--else
						vparams:addRule(rule,value)
					--end
				end
			end
			end
		
		end
		source:getParent():removeView(source)
		--rootView:addView(source)
		source:setLayoutParams(params)
		if(config.type == "linear") then
			local mod = 0
			if config.indexmod ~= nil then mod = config.indexmod end
			if(target:getChildCount() < mod) then
				target:addView(source)
			else
				target:addView(source,mod)
			end
		else
			target:addView(source)
		end
		
		
		if(config.divider and divider) then
			--Note("\nadding divider\n")
			--target:addView(divider)
		end

	end
	if(not windowTracker[tostring(config.id)]) then
		table.insert(windowTracker,config.id)
		windowTracker[tostring(config.id)] = true
	end
end

function LoadLayout(layout)
	layout_config.mode = tonumber(layout)
	for i,v in ipairs(windowTracker) do
		AddWindow(string.format("return {id = %d}",v))
	end
end

function OnCreate()
	LoadLayout(layout_config.mode)
end

--debug.sethook(call,"c")
--debug.sethook(ret,"r")
--bind necessary android classes
ScrollView = luajava.bindClass("android.widget.ScrollView")
FrameLayoutParams = luajava.bindClass("android.widget.FrameLayout$LayoutParams")
Color = luajava.bindClass("android.graphics.Color")
local offsetnull_R_drawable = luajava.bindClass("com.offsetnull.bt.R$drawable")

context = view:getContext()
--pre load constants that will be used all over the place
WRAP_CONTENT = LinearLayoutParams.WRAP_CONTENT
MATCH_PARENT = LinearLayoutParams.FILL_PARENT

local RelativeLayout = RelativeLayout

install_dir = GetPluginInstallDirectory()
--read the layout manager configuration from disk, right now this just consists of the layout mode
layout_config = ReadFile(string.format("%s/layout_manager_props",install_dir))
if(not layout_config) then 
	layout_config = {mode=4,scroll_width=300,containers={}}
	layout_config.containers.ScrollView1 = {}
	layout_config.containers.ScrollView1.gravcount = 1
	layout_config.configs = {}
	for i=1,10 do layout_config.configs[i] = {} end
	--layout_config.configs[layout_config.mode] = {}
else 
	layout_config = loadstring(layout_mode)()
end


local mainDisplay = rootView:findViewById(6666) --main window id from the settings defaults
local inputBarView = rootView:findViewById(10) --the input bar

--check if the chat window is installed
local chat_window_installed = PluginInstalled("chat_miniwindow")

if(chat_window_installed) then Note("\nchat miniwindow installed\n") end

--make the scroll window and linear layout target
local scroll_view = luajava.new(ScrollView,context)
local scroll_target = luajava.new(LinearLayout,context)

local scrollViewConfig_0 = {}
scrollViewConfig_0.id = 886795
scrollViewConfig_0.width = 300
scrollViewConfig_0.height = 100
scrollViewConfig_0.type = "relative"
scrollViewConfig_0.target = "root"
scrollViewConfig_0.rules = {}
scrollViewConfig_0.rules[RelativeLayout.ABOVE] = 40
scrollViewConfig_0.rules[RelativeLayout.ALIGN_PARENT_RIGHT] = 1
if(chat_window_installed) then
	scrollViewConfig_0.rules[RelativeLayout.BELOW] = 5010
else
	scrollViewConfig_0.rules[RelativeLayout.ALIGN_PARENT_TOP] = 1
end

local scrollViewConfig_3 = {}
setmetatable(scrollViewConfig_3,{__index = scrollViewConfig_0})
scrollViewConfig_3.rules = {}
scrollViewConfig_3.rules[RelativeLayout.ABOVE] = 40
scrollViewConfig_3.rules[RelativeLayout.ALIGN_PARENT_RIGHT] = 1
scrollViewConfig_3.rules[RelativeLayout.ALIGN_PARENT_TOP] = 1

local scrollViewConfig_4 = {}
setmetatable(scrollViewConfig_4,{__index = scrollViewConfig_0})
scrollViewConfig_4.rules = {}
scrollViewConfig_4.rules[RelativeLayout.ALIGN_PARENT_BOTTOM] = 1
scrollViewConfig_4.rules[RelativeLayout.ALIGN_PARENT_RIGHT] = 1
scrollViewConfig_4.rules[RelativeLayout.ALIGN_PARENT_TOP] = 1

layout_config.configs[1][scrollViewConfig_0.id] = scrollViewConfig_0
layout_config.configs[2][scrollViewConfig_0.id] = scrollViewConfig_0
layout_config.configs[3][scrollViewConfig_0.id] = scrollViewConfig_0
layout_config.configs[4][scrollViewConfig_0.id] = scrollViewConfig_3
layout_config.configs[5][scrollViewConfig_0.id] = scrollViewConfig_4

local divider0Config_0 = {}
divider0Config_0.id = 886796
divider0Config_0.width = 3
divider0Config_0.height = 3
divider0Config_0.type = "relative"
divider0Config_0.target = "root"
divider0Config_0.rules = {}
divider0Config_0.rules[RelativeLayout.ALIGN_TOP] = 886795
divider0Config_0.rules[RelativeLayout.ALIGN_BOTTOM] = 886795
divider0Config_0.rules[RelativeLayout.LEFT_OF] = 886795
divider0Config_0.affects = {}
divider0Config_0.affects.mainDisplay = {}
divider0Config_0.affects.mainDisplay[RelativeLayout.ALIGN_PARENT_RIGHT] = 0
divider0Config_0.affects.mainDisplay[RelativeLayout.LEFT_OF] = 886796
if(chat_window_installed) then
	divider0Config_0.affects.chat_window = {}
	divider0Config_0.affects.chat_window[RelativeLayout.ALIGN_PARENT_RIGHT] = 1
end

local divider0Config_3 = {}
setmetatable(divider0Config_3,{__index = divider0Config_0})
if(chat_window_installed) then
	divider0Config_3.affects = {}
	divider0Config_3.affects.chat_window = {}
	divider0Config_0.affects.chat_window[RelativeLayout.ALIGN_PARENT_RIGHT] = 0
	divider0Config_3.affects.chat_window[RelativeLayout.LEFT_OF] = 886796
	divider0Config_3.affects.mainDisplay = {}
	divider0Config_3.affects.mainDisplay[RelativeLayout.ALIGN_PARENT_RIGHT] = 0
	divider0Config_3.affects.mainDisplay[RelativeLayout.LEFT_OF] = 886796
end

local divider0Config_4 = {}
setmetatable(divider0Config_4,{__index = divider0Config_0})
divider0Config_4.affects = {}
divider0Config_4.affects.inputBar = {}
divider0Config_4.affects.inputBar[RelativeLayout.ALIGN_PARENT_RIGHT] = 0
divider0Config_4.affects.inputBar[RelativeLayout.LEFT_OF] = 886796
divider0Config_4.affects.mainDisplay = {}
divider0Config_4.affects.mainDisplay[RelativeLayout.ALIGN_PARENT_RIGHT] = 0
divider0Config_4.affects.mainDisplay[RelativeLayout.LEFT_OF] = 886796
if(chat_window_installed) then
	divider0Config_4.affects.chat_window = {}
	divider0Config_4.affects.chat_window[RelativeLayout.LEFT_OF] = 886796
end

layout_config.configs[1][divider0Config_0.id] = divider0Config_0
layout_config.configs[2][divider0Config_0.id] = divider0Config_0
layout_config.configs[3][divider0Config_0.id] = divider0Config_0
layout_config.configs[4][divider0Config_0.id] = divider0Config_3
layout_config.configs[5][divider0Config_0.id] = divider0Config_4

--local scroll_view_params = luajava.new(RelativeLayoutParams,tonumber(layout_config.scroll_width),MATCH_PARENT)
--scroll_view_params:addRule(RelativeLayout.ABOVE,40) --horizontal divider between input bar and main window
--if(chat_window_installed) then
--	scroll_view_params:addRule(RelativeLayout.BELOW,5010) --use top of the pane because chat window is not installed.
--else
--	scroll_view_params:addRule(RelativeLayout.ALIGN_TOP,rootView:getId()) --use top of the pane because chat window is not installed.
--end
--scroll_view_params:addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
--scroll_view:setLayoutParams(scroll_view_params)
scroll_view:setBackgroundColor(Color:argb(255,0,0,0))
scroll_target:setId(886794)

local linear_layout_params = luajava.new(LinearLayoutParams,MATCH_PARENT,MATCH_PARENT)
scroll_target:setOrientation(LinearLayout.VERTICAL)
scroll_target:setLayoutParams(linear_layout_params)
local horizontalDividerDrawable = context:getResources():getDrawable(offsetnull_R_drawable.horizontal_divider)
scroll_target:setDividerDrawable(horizontalDividerDrawable)
scroll_target:setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE)

--add the views into the hierarchy

scroll_view:setId(886795)
scroll_view:addView(scroll_target)
--rootView:addView(scroll_view)
--layout_config.configs[0][scrollViewConfig_0.id] = scrollViewConfig_0
--layout_config.configs[0][divider0Config_0.id] = divider0Config_0
--hack scroll view into the window system.

--make the divider
local divider = MakeDivider(context)
divider:setId(886796)
--local density = context:getResources():getDisplayMetrics().density
--local dividerParams = luajava.new(RelativeLayoutParams,3*density,MATCH_PARENT)
--dividerParams:addRule(RelativeLayout.LEFT_OF,886795)
--dividerParams:addRule(RelativeLayout.ALIGN_TOP,886795)
--dividerParams:addRule(RelativeLayout.ALIGN_BOTTOM,886795)
--dividerParams:addRule(RelativeLayout.RIGHT_OF,mainDisplay:getId())
--divider:setLayoutParams(dividerParams)

--coopt the main windows layout rules to be to the left of the new scroll view
--local mainParams = mainDisplay:getLayoutParams()
--mainParams:addRule(RelativeLayout.LEFT_OF,886796)
--mainDisplay:setLayoutParams(mainParams)
rootView:addView(divider)
--local rootView = view:getParent()
rootView:addView(scroll_view)

--set up and add the lower linear layout 
local bottomHolder = luajava.new(LinearLayout,context)
bottomHolder:setId(9983)
--local bottomHolderParams = luajava.new(RelativeLayoutParams,MATCH_PARENT,0)
--bottomHolderParams:addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
--bottomHolder:setLayoutParams(bottomHolderParams)
local verticalDividerDrawable = context:getResources():getDrawable(offsetnull_R_drawable.vertical_divider)
bottomHolder:setDividerDrawable(verticalDividerDrawable)
bottomHolder:setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE)

local bottomHolderConfig_0 = {}
bottomHolderConfig_0.id = 9983
bottomHolderConfig_0.width = MATCH_PARENT
bottomHolderConfig_0.height = 0
bottomHolderConfig_0.type = "relative"
bottomHolderConfig_0.target = "root"
bottomHolderConfig_0.rules = {}
bottomHolderConfig_0.rules[RelativeLayout.ALIGN_PARENT_BOTTOM] = 1
bottomHolderConfig_0.affects = {}
bottomHolderConfig_0.affects.inputBar = {}
bottomHolderConfig_0.affects.inputBar[RelativeLayout.ALIGN_PARENT_BOTTOM] = 0
bottomHolderConfig_0.affects.inputBar[RelativeLayout.ABOVE] = 9983

local bottomHolderConfig_2 = {}
setmetatable(bottomHolderConfig_2,{__index = bottomHolderConfig_0})
bottomHolderConfig_2.height = 100*density

layout_config.configs[1][bottomHolderConfig_0.id] = bottomHolderConfig_0
layout_config.configs[2][bottomHolderConfig_2.id] = bottomHolderConfig_2
layout_config.configs[3][bottomHolderConfig_2.id] = bottomHolderConfig_0
layout_config.configs[4][bottomHolderConfig_2.id] = bottomHolderConfig_0
layout_config.configs[5][bottomHolderConfig_2.id] = bottomHolderConfig_0
rootView:addView(bottomHolder)

--bottomHolder:setVisibility(View.VISIBLE)
--rootView:requestLayout()


Containers.id.ScrollTarget1 = scroll_target:getId()
Containers.id.Bottom = bottomHolder:getId()

Containers.views.ScrollTarget1 = scroll_target
Containers.views.Bottom = bottomHolder
if(chat_window_installed) then

end

--rootView:invalidate()

AddWindow(serialize(scrollViewConfig_0))
AddWindow(serialize(divider0Config_0))
AddWindow(serialize(bottomHolderConfig_0))
vitalsconfig_1 = {}
vitalsconfig_1.id = 1010
vitalsconfig_1.width = MATCH_PARENT
vitalsconfig_1.height = WRAP_CONTENT
vitalsconfig_1.type = "linear"
vitalsconfig_1.target = "Bottom"
vitalsconfig_1.weight = 1
layout_config.configs[2][vitalsconfig_1.id] = vitalsconfig_1
--vitalsconfig.rules = {}
--vitalsconfig.rules[RelativeLayout.ALIGN_PARENT_BOTTOM] = 1
--vitalsconfig.affects = {}
--vitalsconfig.affects.inputBar = {}
--vitalsconfig.affects.inputBar[RelativeLayout.ALIGN_PARENT_BOTTOM] = 0
--vitalsconfig.affects.inputBar[RelativeLayout.ABOVE] = 1010
--layout_config.configs[0][vitalsconfig.id] = vitalsconfig

vitalsconfig_2 = {}
setmetatable(vitalsconfig_2,{__index = vitalsconfig_1})
vitalsconfig_2.width = 50*density
vitalsconfig_2.height = WRAP_CONTENT
vitalsconfig_2.type = "relative"
vitalsconfig_2.target = "root"
vitalsconfig_2.rules = {}
vitalsconfig_2.rules[RelativeLayout.ABOVE] = 40
vitalsconfig_2.rules[RelativeLayout.ALIGN_PARENT_RIGHT] = 1
vitalsconfig_2.rules[RelativeLayout.ALIGN_PARENT_TOP] = 1
vitalsconfig_2.affects = {}
vitalsconfig_2.affects.ScrollView1 = {}
vitalsconfig_2.affects.ScrollView1[RelativeLayout.ALIGN_PARENT_RIGHT] = 0
vitalsconfig_2.affects.ScrollView1[RelativeLayout.LEFT_OF] = 1010
if(chat_window_installed) then
	vitalsconfig_2.affects.chat_window = {}
	vitalsconfig_2.affects.chat_window[RelativeLayout.LEFT_OF] = 1010
end
layout_config.configs[3][vitalsconfig_2.id] = vitalsconfig_2

maphack_2 = {}
local hacktable = {}
hacktable.__index = function(table,index)
	if not layout_config.configs[1][rawget(table,"id")] then 
		return nil 
	else
		return layout_config.configs[1][rawget(table,"id")][index]
	end
end
setmetatable(maphack_2,hacktable)
maphack_2.indexmod = 1
maphack_2.id = 6020
layout_config.configs[2][6020] = maphack_2
maphack_3 = {}
setmetatable(maphack_3,hacktable)
maphack_3.indexmod = 3
maphack_3.id = 6020
layout_config.configs[3][6020] = maphack_3

mapexitshack_2 = {}
mapexitshack_2.id = 6022
mapexitshack_2.indexmod = 2
setmetatable(mapexitshack_2,hacktable)
layout_config.configs[2][6022] = mapexitshack_2
mapexitshack_3 = {}
mapexitshack_3.id = 6022
mapexitshack_3.indexmod = 4
setmetatable(mapexitshack_3,hacktable)
layout_config.configs[3][6022] = mapexitshack_3


maplabelhack_2 = {}
maplabelhack_2.id = 6021
maplabelhack_2.indexmod = 0
setmetatable(maplabelhack_2,hacktable)
layout_config.configs[2][6021] = maplabelhack_2
maplabelhack_3 = {}
maplabelhack_3.id = 6021
maplabelhack_3.indexmod = 2
setmetatable(maplabelhack_3,hacktable)
layout_config.configs[3][6021] = maplabelhack_3


statsconfig = {}
statsconfig.id = 9020
statsconfig.width = WRAP_CONTENT
statsconfig.height = MATCH_PARENT
statsconfig.type = "linear"
statsconfig.target = "Bottom"
statsconfig.weight = 0
statsconfig.divider = true
layout_config.configs[2][statsconfig.id] = statsconfig

clockconfig = {}
clockconfig.id = 9010
clockconfig.width = WRAP_CONTENT
clockconfig.height = MATCH_PARENT
clockconfig.type = "linear"
clockconfig.target = "Bottom"
clockconfig.weight = 0
clockconfig.divider = true
layout_config.configs[2][clockconfig.id] = clockconfig



function dump(o)
	if type(o) == 'table' then
		local s = '{ '
		for k,v in pairs(o) do
			if type(k) ~= 'number' then k = '"'..k..'"' end
			s = s .. '['..k..'] = ' .. dump(v) .. ','
		end
		return s .. '} '
	else
		return tostring(o)
	end
end

--debug.sethook()