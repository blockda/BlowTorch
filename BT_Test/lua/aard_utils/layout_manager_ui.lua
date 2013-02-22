package.path = package.path..";"..GetPluginInstallDirectory().."/?.lua"
require("serialize")
require("miniwindow")

--bind necessary android classes
ScrollView = luajava.bindClass("android.widget.ScrollView")
FrameLayoutParams = luajava.bindClass("android.widget.FrameLayout$LayoutParams")
Color = luajava.bindClass("android.graphics.Color")

context = view:getContext()
--pre load constants that will be used all over the place
WRAP_CONTENT = LinearLayoutParams.WRAP_CONTENT
MATCH_PARENT = LinearLayoutParams.FILL_PARENT

install_dir = GetPluginInstallDirectory()
--read the layout manager configuration from disk, right now this just consists of the layout mode
layout_config = ReadFile(string.format("%s/layout_manager_props",install_dir))
if(not layout_config) then 
	layout_config = {mode=0,scroll_width=300,containers={}}
	layout_config.containers.ScrollView1 = {}
	layout_config.containers.ScrollView1.gravcount = 1
	layout_config.configs = {}
	layout_config.configs[layout_config.mode] = {}
else 
	layout_config = loadstring(layout_mode)()
end

local rootView = view:getParent()
local mainDisplay = rootView:findViewById(6666) --main window id from the settings defaults

--check if the chat window is installed
chat_window_installed = PluginInstalled("chat_miniwindow")

if(chat_window_installed) then Note("\nchat miniwindow installed\n") end

--make the scroll window and linear layout target
local scroll_view = luajava.new(ScrollView,context)
local scroll_target = luajava.new(LinearLayout,context)

local scroll_view_params = luajava.new(RelativeLayoutParams,tonumber(layout_config.scroll_width),MATCH_PARENT)
scroll_view_params:addRule(RelativeLayout.ABOVE,40) --horizontal divider between input bar and main window
if(chat_window_installed) then
	scroll_view_params:addRule(RelativeLayout.BELOW,5010) --use top of the pane because chat window is not installed.
else
	scroll_view_params:addRule(RelativeLayout.ALIGN_TOP,rootView:getId()) --use top of the pane because chat window is not installed.
end
scroll_view_params:addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
scroll_view:setLayoutParams(scroll_view_params)
scroll_view:setBackgroundColor(Color:argb(255,255,0,0))

local linear_layout_params = luajava.new(LinearLayoutParams,MATCH_PARENT,MATCH_PARENT)
scroll_target:setOrientation(LinearLayout.VERTICAL)
scroll_target:setLayoutParams(linear_layout_params)

--add the views into the hierarchy
scroll_target:setId(886794)
scroll_view:setId(886795)
scroll_view:addView(scroll_target)

--make the divider
local divider = MakeDivider(context)
divider:setId(886796)
local density = context:getResources():getDisplayMetrics().density
local dividerParams = luajava.new(RelativeLayoutParams,3*density,MATCH_PARENT)
dividerParams:addRule(RelativeLayout.LEFT_OF,886795)
dividerParams:addRule(RelativeLayout.ALIGN_TOP,886795)
dividerParams:addRule(RelativeLayout.ALIGN_BOTTOM,886795)
--dividerParams:addRule(RelativeLayout.RIGHT_OF,mainDisplay:getId())
divider:setLayoutParams(dividerParams)

--coopt the main windows layout rules to be to the left of the new scroll view
local mainParams = mainDisplay:getLayoutParams()
mainParams:addRule(RelativeLayout.LEFT_OF,886796)
mainDisplay:setLayoutParams(mainParams)
rootView:addView(divider)
--local rootView = view:getParent()
rootView:addView(scroll_view)

--rootView:requestLayout()



--rootView:invalidate()
Containers = {}
Containers.id = {}
Containers.id.ScrollView1 = scroll_target:getId()
Containers.id.root = rootView:getId()

Containers.views = {}
Containers.views.ScrollView1 = scroll_target
Containers.views.root = rootView

Layouts = {}

Views = {}
Views.mainDisplay = 6666
Views.inputBar = 10
Views.divider1 = 40

vitalsconfig = {}
vitalsconfig.id = 1010
vitalsconfig.width = MATCH_PARENT
vitalsconfig.height = WRAP_CONTENT
vitalsconfig.type = "relative"
vitalsconfig.target = "root"
vitalsconfig.rules = {}
vitalsconfig.rules[RelativeLayout.ALIGN_PARENT_BOTTOM] = 1
vitalsconfig.affects = {}
vitalsconfig.affects.inputBar = {}
vitalsconfig.affects.inputBar[RelativeLayout.ALIGN_PARENT_BOTTOM] = 0
vitalsconfig.affects.inputBar[RelativeLayout.ABOVE] = 1010

layout_config.configs[0][vitalsconfig.id] = vitalsconfig

function InstallWindow(config)
	Note("\nInstalling window, raw data: "..config.."\n")
	local config = loadstring(config)()
	
	Note(string.format("\nconfig.id = %s\n",config.id))
	--vale = true
	--if(vale) then return end
	--return
	--Note("Attempting to install window: "..config.id)
	--dump(config)
	
	if(not layout_config.configs[layout_config.mode][config.id]) then
		--never been added, add to default scroll window.
		Note("\nDoing special no config mode insertion\n")
		local props = {}
		props.target = config.target or "ScrollView1"
		--math.randseed(os.time())
		--props.id = math.round(math.random(4000,40000))
		props.id = config.id
		props.width = config.width or MATCH_PARENT
		props.height = config.height or 200
		props.gravity = config.gravity or 0
		props.weight = config.weight or 0
		props.type = config.type or "linear"
		props.divider = config.divider or nil
		
		layout_config.configs[layout_config.mode][config.id] = props
		
		config = props
	else
		Note("\nView already has configuration, no special mode needed.\n")
		--return
		config = layout_config.configs[layout_config.mode][config.id]
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
			params = luajava.new(LinearLayoutParams,config.width,config.height,config.gravity)
			--params:setGravity(config.gravity)
			--params:setWeight(config.weight)
			if(config.divider) then
				--Note("\nmaking divider\n")
				divider = MakeDivider(context)
				divider:setId(config.id+1)
				divider_params = luajava.new(LinearLayoutParams,MATCH_PARENT,3*density)
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
		source:getParent():removeView(source)
		--rootView:addView(source)
		source:setLayoutParams(params)
		target:addView(source)
		
		if(config.divider and divider) then
			--Note("\nadding divider\n")
			target:addView(divider)
		end

	end
	
end


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