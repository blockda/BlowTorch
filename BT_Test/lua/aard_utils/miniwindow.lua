require("serialize")

LinearLayout = luajava.bindClass("android.widget.LinearLayout")
LinearLayoutParams = luajava.bindClass("android.widget.LinearLayout$LayoutParams")
RelativeLayout = luajava.bindClass("android.widget.RelativeLayout")
RelativeLayoutParams = luajava.bindClass("android.widget.RelativeLayout$LayoutParams")

local view_id = view:getId()

local info_path = string.format("%s/view_%d_info"),install_dir,view_id)
local props_path = string.format("%s/view_%d_props"),install_dir,view_id)

local install_dir = GetPluginInstallDirectory()
local window_config = {}

local 

function file_exists(file)
	local file = io.open
end

function RegisterWindow(id,name,description)
	if(file_exists(info_path)) then return end
	
	local file = io.open(info_path,"w")
	
	data = {}
	data["id"] = id
	data["name"] = name
	data["description"] = description
	
	file:write(serialize(data))
	file:close()
end

function ReadWindowConfiguration()
	if(not file_exists(info_path)) then return end
	local file = io.open(props_path,"r")
	local data = file:read("*a")
	file:close()
	local tmp = loadstring(data)()
	return tmp
end

function ReadFile(path)
	if(not file_exists(path)) then return nil end
	local file = io.open(path,"r")
	local data = file:read("*a")
	file:close()
	return data
end

function WriteWindowConfiguration(config)
	if not config then return end
	local file = io.open(info_path,"w")	
	file:write(serialize(config))
	file:close()
end

function InitializeWindow()
	--generate the new layout parameters
	local parent = view:getParent()
	parent:removeView(view)
	local params = nil
	if(window_config.relative == "true") then --use relative layout style loading
		params = luajava.new(RelativeLayoutParams)
		--pull out the layout rules from the config table
		for rule,value in pairs(window_config.relative_rules) do
			params:addRule(rule,value)
		end
	else --use linear layout style loading
		params = luajava.new(LinearLayoutParams)
		--read out the weight and gravity params and set them
		params:setGravity(tonumber(window_config.gravity))
		params:setWeight(tonumber(window_config.weight))
	end
	
	view:setLayoutParams(params)
	local newparent = parent:findViewById(tonumber(window_config.parent))
	newparent:addView(view)
end




