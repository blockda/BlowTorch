package.path = package.path..";"..GetPluginInstallDirectory()
require("serialize")
require("miniwindow")

--bind necessary android classes
ScrollView = luajava.bindClass("android.widget.ScrollView")

--pre load constants that will be used all over the place
WRAP_CONTENT = LinearLayoutParams.WRAP_CONTENT
MATCH_PARENT = LinearLayoutParams.FILL_PARENT

--read the layout manager configuration from disk, right now this just consists of the layout mode
layout_config = read_file(string.format("%s/layout_manager_props"),install_dir)
if(not layout_mode) then layout_mode = {mode=0,scroll_width=300} else layout_mode = loadstring(layout_mode)() end

--check if the chat window is installed
chat_window_installed = PluginInstalled("chat_miniwindow")

--make the scroll window and linear layout target
local scroll_view = luajava.new(ScrollView,context)
local scroll_target = luajava.new(LinearLayout,context)

local scroll_view_params = luajava.new(RelativeLayoutParams,tonumber(layout_config.scroll_width),MATCH_PARENT)
scroll_view_params:addRule(RelativeLayout.ABOVE,6559) --horizontal divider between input bar and main window
if(chat_window_installed) then
	scroll_view_params:addRule(RelativeLayout.BELOW,6960) --horizontal divider between main window and chat_window
else
	scroll_view_params:addRule(RelativeLayout.ALIGN_PARENT_TOP) --use top of the pane because chat window is not installed.
end
scroll_view:setLayoutParams(scroll_view_params)

local linear_layout_params = luajava.new(LinearLayoutParams,MATCH_PARENT,MATCH_PARENT)
scroll_target:setOrientation(LinearLayout.VERTICAL)
scroll_target:setLayoutParams(linear_layout_params)

--add the views into the hierarchy
scroll_target:setId(886794)
scroll_view:setId(886795)
scroll_view:addView(scroll_target)

local rootView = view:getParent()
rootView:addView(scroll_view)

local 

