package.path = package.path..";"..GetPluginInstallDirectory().."/?.lua"

if(not view) then
Note("\nCampaign Foreground started, view is nil\n")
end

require("serialize")
require("miniwindow")

config = {}
config.divider = true
config.height = WRAP_CONTENT
config.width = MATCH_PARENT
config.gravity = 100
config.id = view:getId()
view:setTextSelectionEnabled(false)
InstallWindow(config)

function requestLayout()
	view:requestLayout()
end