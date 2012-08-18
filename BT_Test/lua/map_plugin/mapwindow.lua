
--package.path = GetExternalStorageDirectory().."/BlowTorch/?.lua"
--package.cpath = GetExternalStorageDirectory().."/BlowTorch/?.so"

dbPath = GetPluginInstallDirectory().."/mapper-db"

require("luabins")
require("copytable")

require("lsqlite3")

local db = sqlite3.open(dbPath);

local rooms = {}
local room_not_in_database = {}

local density = GetDisplayDensity()
local boxSize = 40*density
local ROOM_SIZE = boxSize
local HALF_ROOM = boxSize/2
local DISTANCE_TO_NEXT_ROOM = 80*density
local THIRD_WAY = DISTANCE_TO_NEXT_ROOM /3
local HALF_WAY = DISTANCE_TO_NEXT_ROOM / 2
 connectors = {
      n =  { x1 = 0,            y1 = - HALF_ROOM, x2 = 0,                             y2 = - HALF_ROOM - HALF_WAY, at = { 0, -1 } }, 
      s =  { x1 = 0,            y1 =   HALF_ROOM, x2 = 0,                             y2 =   HALF_ROOM + HALF_WAY, at = { 0,  1 } }, 
      e =  { x1 =   HALF_ROOM,  y1 = 0,           x2 =   HALF_ROOM + HALF_WAY,  y2 = 0,                            at = {  1,  0 }}, 
      w =  { x1 = - HALF_ROOM,  y1 = 0,           x2 = - HALF_ROOM - HALF_WAY,  y2 = 0,                            at = { -1,  0 }}, 
   
      ne = { x1 =   HALF_ROOM,  y1 = - HALF_ROOM, x2 =   HALF_ROOM + HALF_WAY , y2 = - HALF_ROOM - HALF_WAY, at = { 1, -1 } }, 
      se = { x1 =   HALF_ROOM,  y1 =   HALF_ROOM, x2 =   HALF_ROOM + HALF_WAY , y2 =   HALF_ROOM + HALF_WAY, at = { 1,  1 } }, 
      nw = { x1 = - HALF_ROOM,  y1 = - HALF_ROOM, x2 = - HALF_ROOM - HALF_WAY , y2 = - HALF_ROOM - HALF_WAY, at = {-1, -1 } }, 
      sw = { x1 = - HALF_ROOM,  y1 =   HALF_ROOM, x2 = - HALF_ROOM - HALF_WAY , y2 =   HALF_ROOM + HALF_WAY, at = {-1,  1 } }, 
   
   } -- end connectors
   
   -- how to draw a stub line
   half_connectors = {
      n =  { x1 = 0,            y1 = - HALF_ROOM, x2 = 0,                        y2 = - HALF_ROOM - THIRD_WAY, at = { 0, -1 } }, 
      s =  { x1 = 0,            y1 =   HALF_ROOM, x2 = 0,                        y2 =   HALF_ROOM + THIRD_WAY, at = { 0,  1 } }, 
      e =  { x1 =   HALF_ROOM,  y1 = 0,           x2 =   HALF_ROOM + THIRD_WAY,  y2 = 0,                       at = {  1,  0 }}, 
      w =  { x1 = - HALF_ROOM,  y1 = 0,           x2 = - HALF_ROOM - THIRD_WAY,  y2 = 0,                       at = { -1,  0 }}, 
  
      ne = { x1 =   HALF_ROOM,  y1 = - HALF_ROOM, x2 =   HALF_ROOM + THIRD_WAY , y2 = - HALF_ROOM - THIRD_WAY, at = { 1, -1 } }, 
      se = { x1 =   HALF_ROOM,  y1 =   HALF_ROOM, x2 =   HALF_ROOM + HALF_WAY - 1 , y2 =   HALF_ROOM + HALF_WAY - 1, at = { 1,  1 } }, 
      nw = { x1 = - HALF_ROOM,  y1 = - HALF_ROOM, x2 = - HALF_ROOM - HALF_WAY, y2 = - HALF_ROOM - HALF_WAY, at = {-1, -1 } }, 
      sw = { x1 = - HALF_ROOM,  y1 =   HALF_ROOM, x2 = - HALF_ROOM - THIRD_WAY , y2 =   HALF_ROOM + THIRD_WAY, at = {-1,  1 } }, 
  
   } -- end half_connectors

local hasRooms = false

function setHasRooms()
	hasRooms = true
	view:invalidate()
end

local test = function()
	ActionBarLayoutParams = luajava.bindClass("android.app.ActionBar$LayoutParams")
end
pcall(test)
LuaFilter = luajava.bindClass("com.offsetnull.bt.window.LuaFilter")
LuaFilterProxy = luajava.bindClass("com.offsetnull.bt.window.LuaFilter$FilterProxy")
AutoCompleteTextView = luajava.bindClass("android.widget.AutoCompleteTextView")
MenuItem = luajava.bindClass("android.view.MenuItem")
Context = luajava.bindClass("android.content.Context")
Button = luajava.bindClass("android.widget.Button")
R_layout = luajava.bindClass("com.offsetnull.bt.R$layout")
R_id = luajava.bindClass("com.offsetnull.bt.R$id")
View = luajava.bindClass("android.view.View");
RelativeLayoutParams = luajava.bindClass("android.widget.RelativeLayout$LayoutParams")
RelativeLayout = luajava.bindClass("android.widget.RelativeLayout")
Color = luajava.bindClass("android.graphics.Color")
Paint = luajava.bindClass("android.graphics.Paint")
PaintStyle = luajava.bindClass("android.graphics.Paint$Style")
RectF = luajava.bindClass("android.graphics.RectF")
AlertDialogBuilder = luajava.bindClass("android.app.AlertDialog$Builder")
LinearLayoutParams = luajava.bindClass("android.widget.LinearLayout$LayoutParams")

local context = view:getContext()
local density = GetDisplayDensity()
local layoutInflater = context:getSystemService(Context.LAYOUT_INFLATER_SERVICE)
	

local menuItem = nil

items = {}
table.insert(items,"foo");
table.insert(items,"bar");
table.insert(items,"baz");
table.insert(items,"whop");
table.insert(items,"diz");
table.insert(items,"mizzle");

filterResults = {}

filterProxy = {}
function filterProxy.performFiltering(constriant,results)
	Note("performing filter:"..constriant)
	filterResults = {}                        
	for row in db:nrows("select uid,name from rooms where name like '%"..constriant.."%';") do 
		local tmp = {}
		tmp["uid"] = row["uid"]
		tmp["name"] = row["name"]
		table.insert(filterResults,tmp)
	end
	results:setCount(#filterResults)
	return results;
end

function filterProxy.publishResults(constraint,results)
	Note("publishing results: "..constraint)
	items = filterResults
	menuItem:setAdapter(filterAdapter_cb)
end
filterProxy_cb = luajava.createProxy("com.offsetnull.bt.window.LuaFilter$FilterProxy",filterProxy)

filterAdapter = {}
function filterAdapter.getFilter()
	local filter = luajava.new(LuaFilter)
	filter:setProxy(filterProxy_cb)
	return filter
end

function filterAdapter.getView(pos,v,parent)
	--Note("getting view")
	local newview = nil
	if(v ~= nil) then
		newview = v
		
	else
		--Note("inflating view")
		newview = layoutInflater:inflate(R_layout.editor_selection_list_row,nil)
	
		local root = newview:findViewById(R_id.root)
		--root:setOnClickListener(rowClicker_cb)
		
	end
	
	--newview:setId(157*pos)
	
	--local holder = newview:findViewById(R_id.toolbarholder)
	--holder:setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS)
	
	--if(holder:getChildCount() > 0) then
	--	holder:removeAllViews()
	--	lastSelectedIndex = -1
	--end
	
	item = items[tonumber(pos)+1]
	
	if(item ~= nil) then

		label = newview:findViewById(R_id.infoTitle)
		extra = newview:findViewById(R_id.infoExtended)
		
		icon = newview:findViewById(R_id.icon)
		icon:setVisibility(View.GONE)
		label:setText(item["name"])
		extra:setText("Id: "..item["uid"])
		
		--if(selectedIndex == (pos+1)) then
		--	label:setBackgroundColor(Color:argb(255,70,70,70))
		--	extra:setBackgroundColor(Color:argb(255,70,70,70))
		--else
		--	label:setBackgroundColor(Color:argb(255,5,5,5))
		--	extra:setBackgroundColor(Color:argb(255,5,5,5))
		--end
		----newview:setId(pos)
	end
	
	--if(newview ~= nil) then
	--	debugPrint("returning newview, it is not null")
	--end
	return newview
	
	
end
function filterAdapter.getCount()
	Note("getting count:"..#items)
	return #items
end
function filterAdapter.areAllItemsEnabled()
	--debugPrint("areAllItemsEnabled()")
	return true
end
function filterAdapter.getItemViewType(pos)
	--debugPrint("getItemViewType()")
	return 0
end
function filterAdapter.isEnabled(pos)
	--debugPrint("isEnabled(pos)")
	return true
end
function filterAdapter.getItem(pos)
	--debugPrint("getItem(pos)")
	--return luajava.newInstance("java.lang.Object")
	local item = items[pos+1]
	return item["name"]
end
function filterAdapter.isEmpty()
--debugPrint("isEmpty()")
	return false
end
function filterAdapter.hasStableIds()
--debugPrint("hasStableIds()")
	return true
end
function filterAdapter.getViewTypeCount()
--debugPrint("getViewTypeCount()")
	return 1
end
function filterAdapter.getItemId(pos)
	return pos
end
--function buttonListAdapter.
filterAdapter_cb = luajava.createProxy("android.widget.ListAdapter,android.widget.Filterable",filterAdapter)

function OnCreate()
	--Note("\nin on create here.\n"..thiswillbenil)
	
	local root = view:getParentView()
	local main = root:findViewById(6666)
	local p = main:getLayoutParams()
	p:addRule(RelativeLayout.LEFT_OF,1290)
	main:requestLayout()
	
	--local root = view:getParentView()
	--root:removeView(view)
	
	
end

local viewHeight = nil
local viewWidth = nil

function PopulateMenu(menu)
	local foo = function(menu)
		
		local err = MenuItem.SHOW_AS_ACTION_ALWAYS
		
		if(menuItem == nil) then
			makeMenuItem()
		end
		local item = menu:add(0,402,402,"Room Search")
		item:setActionView(menuItem)
		item:setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
		--item:setIcon(R_drawable.ic_menu_button_sets)
		--item:setOnMenuItemClickListener(buttonsetMenuClicked_cb)
		--Note("populated action search bar")
	end
	
	if(pcall(foo)) then
		--honeycomb plus
	else
		makeMenuItem()
		local item = menu:add(0,402,402,"Room Search")
		item:setOnMenuItemClickListener(menuClicked_cb)
	end

end

menuClicked = {}
function menuClicked.onMenuItemClick(item)
	legacySearch()
	return true
end
menuClicked_cb = luajava.createProxy("android.view.MenuItem$OnMenuItemClickListener",menuClicked)

function legacySearch()
	if(builder) then alert:show() return end
	builder = luajava.new(AlertDialogBuilder,context)
	
	builder:setPositiveButton("Dismiss",closeLegacySearch_cb)
	
	--local parent = menuItem:getParentView()
	--if(parent) then
	--	parent:removeView(menuItem)
	--end
	builder:setView(menuItem)
	
	alert = builder:create()
	
	alert:show()
end

closeLegacySearch = {}
function closeLegacySearch.onClick(dialog,which)
	dialog:dismiss()
	return
end
closeLegacySearch_cb = luajava.createProxy("android.content.DialogInterface$OnClickListener",closeLegacySearch)

function makeMenuItem()
	if(menuItem ~= nil) then return end
	menuItem = luajava.new(AutoCompleteTextView,context)
	local p = nil
	local honeyTest = function()
		p = luajava.new(ActionBarLayoutParams,200*density,ActionBarLayoutParams.WRAP_CONTENT)
	end
	
	if(not pcall(honeyTest)) then
		p = luajava.new(LinearLayoutParams,200*density,LinearLayoutParams.FILL_PARENT)
	end
	menuItem:setLayoutParams(p)
	menuItem:setMinWidth(200*density)
	menuItem:setAdapter(filterAdapter_cb)
	menuItem:setThreshold(1)
	menuItem:setOnItemClickListener(itemClicker_cb)
end

itemClicker = {}
function itemClicker.onItemClick(parent,view,pos,id)
	local item = items[pos+1]
	PluginXCallS("runtoRoom",tostring(item["uid"]))
	Note("itemClicked:"..item["name"])
end
itemClicker_cb = luajava.createProxy("android.widget.AdapterView$OnItemClickListener",itemClicker)


function get_room (uid)

   -- look it up
   local ourroom = rooms [uid]

   -- not cached - see if in database
   if not ourroom then
      ourroom = load_room_from_database (uid)
      rooms [uid] = ourroom -- cache for later
   end -- not in cache
   
   if not ourroom then
      return { unknown = true }
   end -- if
   
   return ourroom
end

function load_room_from_database (uid)

   local room
   local u = tostring(uid)
   assert (uid, "No UID supplied to load_room_from_database")
   -- if not in database, don't look again

   if room_not_in_database [u] then
      return nil
   end -- no point looking
   
   for row in db:nrows(string.format ("SELECT * FROM rooms WHERE uid = %s", fixsql (u))) do

      	room = {
      	name = row.name,
      	area = row.area,
      	building = row.building,
      	terrain = row.terrain,
      	info = row.info,
      	notes = row.notes,
      	x = row.x or 0,
      	y = row.y or 0,
      	z = row.z or 0,
      	noportal = row.noportal,
      	norecall = row.norecall,
      	exits = {},
      	exit_locks = {}
      	}
   		for exitrow in db:nrows(string.format ("SELECT * FROM exits WHERE fromuid = %s", fixsql (u))) do
         	room.exits [exitrow.dir] = tostring (exitrow.touid)
         	room.exit_locks [exitrow.dir] = tostring(exitrow.level)
      	end -- for each exit
   end   -- finding room

	
   if room then
   	  --dumpTable("","room",room)
      return room
   end -- if found
   
   -- room not found in database
   room_not_in_database [u] = true
   return nil
end -- load_room_from_database


local boxStroke = nil
local boxFill = nil

local roomRect = nil

--function OnCreate()
	
--end
local current_room = nil

function updateCurrentRoom(uid)
	current_room = tonumber(uid)
	view:invalidate()
end

function OnDraw(c)
	if(not viewHeight) then
		viewHeight = view:getHeight()
		viewWidth = view:getWidth()
	end

	if(not boxStroke) then
		boxStroke = luajava.new(Paint)
		boxStroke:setStyle(PaintStyle.STROKE)
		boxStroke:setStrokeWidth(5*density)
		boxStroke:setColor(Color:argb(255,120,30,120))
	end
	
	if(not boxFill) then
		boxFill = luajava.new(Paint)
		boxFill:setStyle(PaintStyle.FILL)
		boxFill:setColor(Color:argb(255,30,120,30))
	end

	c:drawColor(Color:argb(255,33,33,200))
	
	
	if(not roomRect) then
		roomRect = luajava.new(RectF)
	end
	
	local sizeOver2 = boxSize /2
	roomRect:set(-sizeOver2,-sizeOver2,sizeOver2,sizeOver2)
	
	
	rooms_to_be_drawn = {}
	drawn = {}
	drawn_coords = {}
	plan_to_draw = {}
	area_exits = {}
	Note("window size: "..viewWidth.." x "..viewHeight.."\n")
	table.insert(rooms_to_be_drawn,add_another_room(current_room,{},viewWidth/2,viewHeight/2))
	
	--return
	depth = 1
	
	while #rooms_to_be_drawn > 0 and depth < 30 do
		local old_generation = rooms_to_be_drawn
		rooms_to_be_drawn = {}
		for i,part in ipairs(old_generation) do
			draw_room(part.uid,part.path,part.x,part.y,c)
		end
		depth = depth + 1
	end
	
end

function add_another_room (uid, path, x, y)
   local path = path or {}
   return {uid=uid, path=path, x = x, y = y}
end  -- add_another_room

function draw_room(uid,path,x,y,c)
	local coords = string.format("%i,%i",math.floor(x),math.floor(y))
	Note("drawing room "..uid.." at "..x.." x "..y.."\n")
	drawn_coords[coords] = uid
	
	if drawn[uid] then
		return
	end	
	
	drawn[uid] = {coords = coords,path = path}
	
	local room = rooms[uid]
	
	if not room then
		room = get_room(uid)
		rooms[uid] = room
	end
	
	if not room or room.unknown then return end
	
	local left, top, right, bottom = x - HALF_ROOM, y - HALF_ROOM, x + HALF_ROOM, y + HALF_ROOM
   
   -- forget it if off screen
   	if x < HALF_ROOM or y < HALF_ROOM or x > viewWidth - HALF_ROOM or y > viewHeight - HALF_ROOM then
    	return
  	end -- if
  	
  	local texits = {}
  	for dir, exit_uid in pairs (room.exits) do
      table.insert (texits, dir)
      local exit_info = connectors [dir]
      local stub_exit_info = half_connectors [dir]
      local locked_exit = not (room.exit_locks == nil or room.exit_locks[dir] == nil or room.exit_locks[dir] == "0")
      --local exit_line_colour = (locked_exit and 0x0000FF) or config.EXIT_COLOUR.colour
      --local arrow = arrows [dir]
      
      -- draw up in the ne/nw position if not already an exit there at this level
      if dir == "u" then
         exit_info = connectors.nw
         stub_exit_info = half_connectors.nw
         arrow = arrows.nw
        -- exit_line_colour = (locked_exit and 0x0000FF) or config.EXIT_COLOUR_UP_DOWN.colour
      elseif dir == "d" then
         exit_info = connectors.se
         stub_exit_info = half_connectors.se
         arrow = arrows.se
        -- exit_line_colour = (locked_exit and 0x0000FF) or config.EXIT_COLOUR_UP_DOWN.colour
      end -- if down
      
      if exit_info then
         --local linetype = miniwin.pen_solid -- unbroken
         --local linewidth = (locked_exit and 2) or 1 -- not recent
         
         -- try to cache room
         if not rooms [exit_uid] then
            rooms [exit_uid] = get_room (exit_uid)
         end -- if
         
         
         
         if rooms [exit_uid].unknown then
            --linetype = miniwin.pen_dot -- dots
         end -- if
         
         local next_x = x + exit_info.at [1] * (ROOM_SIZE + DISTANCE_TO_NEXT_ROOM)
         local next_y = y + exit_info.at [2] * (ROOM_SIZE + DISTANCE_TO_NEXT_ROOM)
         
         local next_coords = string.format ("%i,%i", math.floor (next_x), math.floor (next_y))
         Note(string.format("\nroom %s exit %s to %s at %i,%i\n",uid,dir,exit_uid,math.floor(next_x),math.floor(next_y)))
         -- remember if a zone exit (first one only)
--      if show_area_exits and room.area ~= rooms [exit_uid].area then
         if show_area_exits and room.area ~= rooms [exit_uid].area and not rooms[exit_uid].unknown then
            area_exits [ rooms [exit_uid].area ] = area_exits [ rooms [exit_uid].area ] or {x = x, y = y}
         end -- if
         
         -- if another room (not where this one leads to) is already there, only draw "stub" lines
         if drawn_coords [next_coords] and drawn_coords [next_coords] ~= exit_uid then
            exit_info = stub_exit_info
         elseif exit_uid == uid then 
            -- here if room leads back to itself
            exit_info = stub_exit_info
            --linetype = miniwin.pen_dash -- dash
         else
         --if (not show_other_areas and rooms [exit_uid].area ~= current_area) or
            if (not show_other_areas and rooms [exit_uid].area ~= current_area and not rooms[exit_uid].unknown) or
               (not show_up_down and (dir == "u" or dir == "d")) then
               exit_info = stub_exit_info    -- don't show other areas
            else
               -- if we are scheduled to draw the room already, only draw a stub this time
               if plan_to_draw [exit_uid] and plan_to_draw [exit_uid] ~= next_coords then
                  -- here if room already going to be drawn
                  exit_info = stub_exit_info
                  --linetype = miniwin.pen_dash -- dash
               else
                  -- remember to draw room next iteration
                  local new_path = copytable.deep (path)
                  table.insert (new_path, { dir = dir, uid = exit_uid })
                  table.insert (rooms_to_be_drawn, add_another_room (exit_uid, new_path, next_x, next_y))
                  drawn_coords [next_coords] = exit_uid
                  plan_to_draw [exit_uid] = next_coords
                  
                  -- if exit room known
                  --if not rooms [exit_uid].unknown then
                  --   local exit_time = last_visited [exit_uid] or 0
                  --   local this_time = last_visited [uid] or 0
                  --   local now = os.time ()
                  --   if exit_time > (now - config.LAST_VISIT_TIME.time) and
                  --      this_time > (now - config.LAST_VISIT_TIME.time) then
                  --      linewidth = 2
                  --   end -- if
                  --end -- if
               end -- if
            end -- if
         end -- if drawn on this spot

         --WindowLine (win, x + exit_info.x1, y + exit_info.y1, x + exit_info.x2, y + exit_info.y2, exit_line_colour, linetype + 0x0200, linewidth)
         
         -- one-way exit?
         
         --if not rooms [exit_uid].unknown then
         --   local dest = rooms [exit_uid]
         --   -- if inverse direction doesn't point back to us, this is one-way
         --   if dest.exits [inverse_direction [dir]] ~= uid then
         --      -- turn points into string, relative to where the room is
         --      local points = string.format ("%i,%i,%i,%i,%i,%i", 
         --         x + arrow [1],
         --         y + arrow [2],
         --         x + arrow [3],
         --         y + arrow [4],
         --         x + arrow [5],
         --         y + arrow [6])
         --         
         --      -- draw arrow
         --      WindowPolygon(win, points, 
         --         exit_line_colour, miniwin.pen_solid, 1, 
         --         exit_line_colour, miniwin.brush_solid, 
         --         true, true)
         --   end -- one way
         --end -- if we know of the room where it does
      end -- if we know what to do with this direction
   end -- for each exit
   
   --actually draw the window
   	c:save()
   	Note("\nDrawing room("..uid..") @ "..x.." x "..y.."\n")
   	
	c:translate(x,y)
	
	c:drawRect(roomRect,boxFill)
	c:drawRect(roomRect,boxStroke)
	
	c:restore()
end

function fixsql (s)
   if s then
      return "'" .. (string.gsub (s, "'", "''")) .. "'" -- replace single quotes with two lots of single quotes
   else
      return "NULL"
   end -- if
end -- fixsql

function dumpTable(indent,name,t)
	for i,v in pairs(t) do
		if(type(v) == "table") then
			dumpTable(indent.."  ",name.."."..i,v)
		else
			Note(indent..name.."."..i..":"..v.."\n")
		end
	end
end