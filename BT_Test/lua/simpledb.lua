
--package.path = GetExternalStorageDirectory().."/BlowTorch/?.lua"
--package.cpath = GetExternalStorageDirectory().."/BlowTorch/?.so"

require("luabins")

debugPrint("loaded luabins")

require("lsqlite3")

local db = sqlite3.open("/mnt/sdcard/BlowTorch/testdb.lua");

function create_tables ()
   -- create rooms table
   dbCheckExecute([[
   PRAGMA foreign_keys = ON;
   PRAGMA journal_mode=WAL;
   PRAGMA temp_store=2;
 
   CREATE TABLE IF NOT EXISTS areas (
      areaid      INTEGER PRIMARY KEY AUTOINCREMENT,
      uid         TEXT    NOT NULL,   -- vnum or how the MUD identifies the area
      name        TEXT,               -- name of area
      date_added  DATE,               -- date added to database
      texture     TEXT,               -- background area texture
      color       TEXT,               -- ANSI colour code.
      UNIQUE (uid)
   );
   CREATE INDEX IF NOT EXISTS areas_uid_index ON areas (uid);
   CREATE INDEX IF NOT EXISTS areas_name_index ON areas (name);

   CREATE TABLE IF NOT EXISTS environments (
      environmentid INTEGER PRIMARY KEY AUTOINCREMENT,
      uid           TEXT    NOT NULL,   -- code for the environment
      name          TEXT,               -- name of environment
      color         INTEGER,            -- ANSI colour code
      date_added    DATE,               -- date added to database
      UNIQUE (uid)
   );
   CREATE INDEX IF NOT EXISTS name_index ON environments (name);
   
   CREATE TABLE IF NOT EXISTS rooms (
      roomid        INTEGER PRIMARY KEY AUTOINCREMENT,
      uid           TEXT NOT NULL,   -- vnum or how the MUD identifies the room
      name          TEXT,            -- name of room
      area          TEXT,            -- which area
      building      TEXT,            -- which building it is in
      terrain       TEXT,            -- eg. road OR water
      info          TEXT,            -- eg. shop,healer
      notes         TEXT,            -- player notes
      x             INTEGER,
      y             INTEGER,
      z             INTEGER,
      date_added    DATE,            -- date added to database
      UNIQUE (uid)
   );
   CREATE INDEX IF NOT EXISTS info_index ON rooms (info);
   CREATE INDEX IF NOT EXISTS terrain_index ON rooms (terrain);
   CREATE INDEX IF NOT EXISTS area_index ON rooms (area);
   CREATE INDEX IF NOT EXISTS rname_index ON rooms (name);  

   CREATE TABLE IF NOT EXISTS exits (
      dir         TEXT    NOT NULL, -- direction, eg. "n", "s"
      fromuid     TEXT    NOT NULL, -- exit from which room (in rooms table)
      touid       TEXT    NOT NULL, -- exit to which room (in rooms table)
      level       STRING  NOT NULL DEFAULT '0', -- minimum level to make use of this exit
      date_added  DATE,             -- date added to database
      PRIMARY KEY(fromuid, dir),
      FOREIGN KEY(fromuid) REFERENCES rooms(uid)
   );
   CREATE INDEX IF NOT EXISTS fromuid_index ON exits (fromuid);
   CREATE INDEX IF NOT EXISTS touid_index   ON exits (touid);
   ]])
  
   -- Since the MUD sends terrain as a string and not as an integer,
   -- it was wrong to originally produce rooms with integer terrains.
   -- Or maybe it's wrong for the MUD to send strings. Either way, we now
   -- have databases with inconsistent data. So let's make it consistent.
   dbCheckExecute("UPDATE OR IGNORE rooms SET terrain = ifnull((SELECT name FROM environments WHERE environments.uid = rooms.terrain), rooms.terrain);")
  
   -- check if rooms_lookup table exists
   dbCheckExecute([[
   BEGIN TRANSACTION;
   DROP TABLE IF EXISTS rooms_lookup;
   CREATE VIRTUAL TABLE rooms_lookup USING FTS3(uid, name);
   INSERT INTO rooms_lookup (uid, name) SELECT uid, name FROM rooms;
   COMMIT;
   ]])
end -- function create_tables

function dbCheckExecute(query)
   local code = db:exec(query)
--~ failed attempt to make concurrent sessions viable
--~    if code == 5 or code == 6 then -- SQLITE_BUSY or SQLITE_LOCKED (oops?)
--~       wait.make (function()
--~          local code = 5
--~          local count = 0
--~          while ((code == 5 or code == 6) and count < 20) do
--~             db:exec ("ROLLBACK")
--~             wait.time(.1)
--~             code = db:exec(query)
--~             count = count + 1
--~          end
--~          dbcheck(code, query)
--~       end)
--~    else
      dbcheck(code, query)
--~    end
end

function dbcheck (code, query)
   if code ~= sqlite3.OK and    -- no error
      code ~= sqlite3.ROW and   -- completed OK with another row of data
      code ~= sqlite3.DONE then -- completed OK, no more rows
         local err = db:errmsg ()  -- the rollback will change the error message
         err = err.."\n\nCODE: "..code.."\nQUERY: "..query.."\n"
         db:exec ("ROLLBACK")      -- rollback any transaction to unlock the database
         error (err, 3)            -- show error in caller's context
   end -- if
end -- dbcheck 

function got_gmcp_room(inroom)
	debugPrint("mapping room:"..inroom.num);
   local room_number = inroom.num
   if not(room_number) then 
      return
   end
   
   --dumpTable("","inroom",inroom)
   
   gmcproom = {
      name = inroom.name,
      area = inroom.zone,
      building = 0,
      terrain = inroom.terrain,
      info = inroom.details,
      notes = "",
      x = inroom.coord.x,
      y = inroom.coord.y,
      z = 0,
      exits = inroom.exits,
      exit_locks = {}
      }
   
   -- Try to accomodate closed clan rooms and other nomap rooms.
   -- We'll have to make some other changes elsewhere as well.
   if room_number == "-1" then
      room_number = "nomap_"..gmcproom.name.."_"..gmcproom.area
   end
   
   current_room = room_number
   
   local area_exists = false
   for n in db:nrows (string.format ("SELECT uid FROM areas where uid=%s", fixsql(gmcproom.area))) do
      area_exists = true
   end
   if not area_exists then
      Send_GMCP_Packet("request area")
   end
   
   local room = rooms [room_number]
   -- not cached - see if in database
   if not room then
      room = load_room_from_database (room_number)
   end -- not in cache
   
   -- re-save if we got information that is different than before  
   local same_exits = ((room and compareTables(gmcproom.exits, room.exits)) or false)
   local same_area = ((room and (nilToStr(room.area) == nilToStr(gmcproom.area))) or false)
   if not room or nilToStr(room.name) ~= nilToStr(gmcproom.name) or
                  nilToStr(room.terrain) ~= nilToStr(gmcproom.terrain) or
                  nilToStr(room.info) ~= nilToStr(gmcproom.info) or
                  same_area == false or
                  same_exits == false then
      if same_area then
--          print("same area")
--          if room then 
--             print("ROOM")
--             tprint(room) 
--          end
--          print("GMCPROOM")
--          tprint(gmcproom)
         
        -- gmcproom.exits = (room.exits or {})
         gmcproom.exit_locks = (room.exit_locks or {})
         gmcproom.notes = nilToStr(room.notes)
         gmcproom.noportal = (room.noportal or 0)
         gmcproom.norecall = (room.norecall or 0)
      elseif room and nilToStr(room.area) ~= "" and areas[nilToStr(room.area)] then
         -- replacement area
--         print("different area")
--         if room then 
--            print("ROOM")
--            tprint(room) 
--         end
--         print("GMCPROOM")
--         tprint(gmcproom)

         mapper.mapprint("This room has moved areas. You should 'mapper purgezone "..nilToStr(room.area).."' if this new area replaces it.")
         map_purgeroom (nilToStr(room_number), gmcproom.area)
      else
         -- brand new area
--         print("new area")
         --gmcproom.exits = {}
         gmcproom.exit_locks = {}
         gmcproom.notes = ""
         gmcproom.noportal = 0
         gmcproom.norecall = 0
      end
      dbCheckExecute("BEGIN TRANSACTION;")
      local success = save_room_to_database(room_number, gmcproom)
      debugPrint("saved room"..room_number)
      if success then
      	debugPrint("saved room success, attempting exits")
         rooms[room_number] = gmcproom
         if not same_exits or not same_area then
            save_room_exits(room_number)
            debugPrint("saved room exits success")
         end
      end     
      dbCheckExecute("COMMIT;") 
      
      if not success then
         return
      end
   end -- if room not there
  
   --mapper.draw(room_number)

   --if expected_exit == "0" and from_room then
    --  fix_up_exit ()
   --end -- exit was wrong
   
   return
end

function save_room_exits(uid) 
	debugPrint("saving room exits")
   if rooms[uid] == nil then
   	debugPrint("rooms[uid] == nil")
      return
   end
   dumpTable("","rooms[uid]",rooms[uid])
   if rooms[uid].exits ~= nil then
      for dir,touid in pairs(rooms[uid].exits) do
         if dir then
            dbCheckExecute (string.format ([[
               INSERT OR REPLACE INTO exits (dir, fromuid, touid, date_added) 
               VALUES (%s, %s, %s, DATETIME('NOW'));
               ]], fixsql  (dir),  -- direction (eg. "n")
               fixsql  (uid),  -- from current room
               fixsql  (touid) -- destination room 
            ))

            --if show_database_mods then
              debugPrint ("Added exit: ".. dir.. "from room: "..uid.. " to room: ".. touid.. " to database.")
            --end -- if

            if rooms[uid].exits[dir] ~= touid then
               rooms[uid].exit_locks[dir] = "0"
            end
            rooms[uid].exits[dir] = touid
         else
            mapper.maperror ("Cannot make sense of:", exit)
         end -- if can decode    
      end -- for each exit
   end -- have exits.
end -- save_room_exits

function save_room_to_database (uid,room) 
   
   assert (uid, "No UID supplied to save_room_to_database")
   local area_exists = false
   for n in db:nrows (string.format ("SELECT uid FROM areas where uid=%s", fixsql(room.area))) do
      area_exists = true
   end
   if not area_exists then
      Send_GMCP_Packet("request area")
      return false
   end
   
   dbCheckExecute(string.format (
         "INSERT OR REPLACE INTO rooms (uid, name, terrain, info, x, y, z, area, noportal, norecall, date_added) VALUES (%s, %s, %s, %s, %i, %i, %i, %s, %d, %d, DATETIME('NOW'));",
            fixsql (uid), 
            fixsql (room.name),
            fixsql (room.terrain), 
            fixsql (room.info),
            room.x or 0, room.y or 0, room.z or 0, fixsql(room.area),
            room.noportal or 0,
            room.norecall or 0
         ))

   local exists = false      
   for n in db:nrows(string.format ("SELECT * FROM rooms_lookup WHERE uid = %s", fixsql(uid))) do 
      exists = true 
   end
   -- don't add multiple times, maintaining backwards database compatibility (there's no uniqueness constraint on rooms_lookup.uid)
   if not exists then
      dbCheckExecute(string.format ("INSERT INTO rooms_lookup (uid, name) VALUES (%s, %s);", fixsql(uid), fixsql(room.name)))
   else
      dbCheckExecute(string.format ("DELETE FROM rooms_lookup WHERE uid = %s",fixsql(uid)))
      dbCheckExecute(string.format ("INSERT INTO rooms_lookup (uid, name) VALUES (%s, %s);", fixsql(uid), fixsql(room.name)))
   end
   
   room_not_in_database [uid] = nil
   
   if show_database_mods then
      mapper.mapprint ("Added room", uid, "to database. Name:", room.name)
   end -- if
   return true
end -- function save_room_to_database

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
      if not rooms then
         -- this shouldn't even be possible. what the hell.
         rooms = {}
      end
      rooms [u] = room
      for row in db:nrows(string.format ("SELECT * FROM bookmarks WHERE uid = %s", fixsql (u))) do
         rooms [u].notes = row.notes
      end   -- finding room
        
      return room
   end -- if found
  
   -- room not found in database
   room_not_in_database [u] = true
   return nil
    
end -- load_room_from_database

function OnBackgroundStartup()
  
   configs = {}  -- in case not found

   --fonts = utils.getfontfamilies ()

   -- if not there already, add it
   --if not fonts.Dina then
   ---   AddFont (GetInfo (66) .. "\\Dina.fon")
   --end -- if Dina not installed

   -- get saved configuration
   --assert (loadstring (GetVariable ("configs") or "")) ()

   -- allow for additions to configs
   --for k, v in pairs (default_config) do
   --   configs [k] = configs [k] or v
   --end -- for
  
   -- initialize mapper engine
   --mapper.init { findpath = findpath,
  --    config = configs,            -- colours, timing etc.
   --   get_room = get_room,        -- get_room (uid) called to get room info
   --   show_help = OnHelp,         -- to show help
   --   room_click = room_click,    -- called on RH click on room square
   --   timing = show_timing,       -- want to see timing
   --   show_completed = show_completed,  -- want to see "Speedwalk completed." message
   --   show_other_areas = show_other_areas,  -- want to see areas other than the current one?
   --   show_up_down = show_up_down,          -- want to follow up/down exits?
   --   show_area_exits = show_area_exits,    -- want to see area exits?
   --   speedwalk_prefix = speedwalk_prefix,  -- how to speedwalk
  -- } 
    
   -- open databases on disk 
   --worldPath = GetInfo(66)..sanitize_filename(WorldName())

   --db = assert (sqlite3.open(worldPath..".db"))
   db:busy_timeout(100)
   
   if not checkDatabaseIntegrity() then
      return
   end
  
   for row in db:nrows("PRAGMA user_version") do
      db_user_version = row.user_version
   end
  
   -- Only go through the structure creation if we haven't done it already
   -- to save time at startup.
   if db_user_version < 2 then
      create_tables()
   end

   -- Database version 3 used to preload terrains, but now we dynamically
   -- request the terrains list from the server with sendgmcp request sectors
   -- se we don't need it anymore. Now we just skip from 2 to 4.

   if db_user_version < 4 then
      -- add bookmarks and terrain tables to the main db since we're
      -- ditching the second db file
      dbCheckExecute([[  
      CREATE TABLE IF NOT EXISTS bookmarks (
         id          INTEGER PRIMARY KEY AUTOINCREMENT,
         uid         TEXT    NOT NULL,   -- vnum of room
         notes       TEXT,               -- user notes
         date_added  DATE,               -- date added to database
         UNIQUE (uid)
      );
      CREATE TABLE IF NOT EXISTS terrain (
         id          INTEGER PRIMARY KEY AUTOINCREMENT,
         name        TEXT    NOT NULL,   -- terrain name
         color       INTEGER,            -- RGB code
         date_added  DATE,               -- date added to database
         UNIQUE (name)
      );
      ]])
   end
   
   if db_user_version < 5 then
      check_rooms_flags()
   end

   if db_user_version < 6 then
      -- original database name for wayhouse was missing the apostrophe
      dbCheckExecute([[
      update areas set name="The Adventurers' Wayhouse" where uid="wayhouse";
      ]])
   end
   
   if db_user_version < 7 then
      fix_exits_table()
      dbCheckExecute([[
      CREATE TABLE IF NOT EXISTS storage (
         name        TEXT NOT NULL,
         data        TEXT NOT NULL,
         PRIMARY KEY (name)
      );
      ]])
      --loadstring(GetVariable("bounce_recall") or "")()
      --loadstring(GetVariable("bounce_portal") or "")()
     -- if bounce_recall then
     --    dbCheckExecute(string.format("INSERT OR REPLACE INTO storage (name, data) VALUES (%s,%s);", fixsql("bounce_recall"), fixsql(serialize.save("bounce_recall"))))
     -- end
      --if bounce_portal then
     --    dbCheckExecute(string.format("INSERT OR REPLACE INTO storage (name, data) VALUES (%s,%s);", fixsql("bounce_portal"), fixsql(serialize.save("bounce_portal"))))
     -- end
   end
   
   -- this should always be the last stage in db schema updates
   if db_user_version < 8 then
      dbCheckExecute("VACUUM;")
   end
   
   -- update db version
   dbCheckExecute("PRAGMA user_version = 8;")
   
   --b_b, err, erm = io.open(worldPath.."_bookmarks.db", "r")
  -- if b_b ~= nil then
   --   io.close(b_b)
   --   ColourNote("white", "blue", "Found obsolete bookmarks file "..sanitize_filename(WorldName()).."_bookmarks.db.")
   --   ColourNote("white", "blue", "Merging into the main database file.")
   --   ColourNote("white", "blue", sanitize_filename(WorldName()).."_bookmarks.db --> "..sanitize_filename(WorldName())..".db")
   --   db_bm = assert (sqlite3.open(worldPath.."_bookmarks.db"))

   --   local bm_found = false
   --   for row in db_bm:nrows("SELECT name FROM sqlite_master WHERE type='table' AND name='bookmarks';") do
   --      bm_found = true
   --   end
   --   local tr_found = false
    --  for row in db_bm:nrows("SELECT name FROM sqlite_master WHERE type='table' AND name='terrain';") do
   --      tr_found = true
   --   end
   --   if bm_found then
    --     for row in db_bm:nrows("SELECT * FROM bookmarks;") do
   --         dbCheckExecute(string.format("INSERT OR REPLACE INTO bookmarks (uid, notes, date_added) VALUES (%s, %s, DATETIME('NOW'));",
   --         fixsql(row.uid),fixsql(row.notes)))
  --       end
  --    end
   --   if tr_found then
   --      for row in db_bm:nrows("SELECT * FROM terrain;") do
   --         dbCheckExecute(string.format("INSERT OR REPLACE INTO terrain (name, color, date_added) VALUES (%s, %i, DATETIME('NOW'));",
   --         fixsql(row.name),row.color))
    --     end
    --  end
      
   --   db_bm:close()
  --    ok, err = os.remove(worldPath.."_bookmarks.db")
   --   if not ok then
   --      ColourNote("white","red", "Error trying to delete obsolete file: "..worldPath.."_bookmarks.db. Please delete it manually.")
   --   end
   --end

   -- grab all area names
   for row in db:nrows("SELECT * FROM areas") do
      area = {
         name = row.name,
         texture = row.texture,
         color = row.color
      }
      areas [row.uid] = area
   end   -- finding areas
  
   -- grab all user terrain info
   for row in db:nrows("SELECT * FROM terrain") do
      --user_terrain_colour [row.name] = row.color
   end -- finding terrains
 
   -- grab all environment names
   for row in db:nrows("SELECT * FROM environments") do
      --environments [tonumber (row.uid)] = row.name
      --terrain_colours [row.name] = tonumber (row.color)
   end -- finding environments
  
   --~    bounce_recall = {dir="home",uid=21335}
   --~    bounce_portal = {dir="enter",uid=26151}
   --for row in db:nrows("SELECT * FROM storage") do
   --   if row.name == "bounce_portal" or row.name == "bounce_recall" then
   --      loadstring(row.data or "")()
   --   end
   --end
  
  --if IsConnected() then
 --     OnPluginConnect()
 --  end
  
   -- if disabled last time, stay disabled
 --  if GetVariable ("enabled") == "false" then
 --     ColourNote ("yellow", "", "Warning: Plugin " .. GetPluginName ().. " is currently disabled.")
 --     check (EnablePlugin(GetPluginID (), false))
 --     return
 --  end -- they didn't enable us last time

  -- last_auto_backup = tonumber(GetVariable("last_auto_backup")) or 0
  -- SetTimerOption("backup_timer", "enabled", tonumber(GetVariable("backup_timer_enabled")) or 1)
 --  if GetTimerOption("backup_timer", "enabled") == 1 then
  --    if (os.time()-last_auto_backup) >= 86401 then
   --      Note("It looks like it has been more than 24 hours since your last automatic mapper database backup!")
   --      Note("Forcing an automatic backup now...")
   --      Note("")
   --      Repaint()
   --      automatic_backup()
    --  else
   --      local seconds = 86400-(os.time()-last_auto_backup)
    --     local hours = math.floor(seconds/3600)
   --      seconds = seconds - hours*3600
    --     local minutes = math.floor(seconds/60)
    --     seconds = math.floor(seconds - minutes*60)
         -- replace the timer with a new one that has the right time
   --      AddTimer("backup_timer", hours, minutes, seconds, "", timer_flag.Replace + timer_flag.Enabled + timer_flag.ActiveWhenClosed + timer_flag.OneShot, "automatic_backup")
   --   end
  -- end
end -- OnPluginInstall

function fix_exits_table()
   -- The original database implementation for exits was naive and broken. We want to fix it up here.
   -- Unfortunately, sqlite requires a little dance of table dropping for this to happen.
   local level_column_exists = false
   for a in db:nrows "PRAGMA table_info('exits')" do
      if a["name"] == "level" then
         level_column_exists = true
      end
   end
   
   local query = [[
   BEGIN TRANSACTION;
   
   ALTER TABLE exits RENAME TO exits_backup;
   
   CREATE TABLE exits (
      dir         TEXT    NOT NULL, -- direction, eg. "n", "s"
      fromuid     TEXT    NOT NULL, -- exit from which room (in rooms table)
      touid       TEXT    NOT NULL, -- exit to which room (in rooms table)
      level       STRING  NOT NULL DEFAULT '0', -- minimum level to make use of this exit
      date_added  DATE,             -- date added to database
      PRIMARY KEY(fromuid, dir),
      FOREIGN KEY(fromuid) REFERENCES rooms(uid)
   );

   INSERT OR REPLACE INTO exits(dir,fromuid,touid,date_added]]..(((level_column_exists == false) and "") or ",level")..[[) SELECT dir, fromuid, touid, date_added]]..(((level_column_exists == false) and "") or ", level")..[[ FROM exits_backup;
   DROP TABLE exits_backup;
   CREATE INDEX IF NOT EXISTS fromuid_index ON exits (fromuid);
   CREATE INDEX IF NOT EXISTS touid_index   ON exits (touid);
   COMMIT;
   ]]
   dbCheckExecute(query)
end

function check_rooms_flags()
   local flag_exists = { noportal = false,
                         norecall = false }
   for a in db:nrows "PRAGMA table_info('rooms')" do
      if flag_exists[a.name] ~= nil then
         flag_exists[a.name] = true
      end
   end
   
   for k, v in pairs(flag_exists) do
      if not v then
         local sql = 'ALTER TABLE rooms ADD ' .. k .. ' INTEGER'
         dbCheckExecute(sql)
      end
   end
end -- function check_rooms_flags

function checkDatabaseIntegrity()
   --Note("CHECKING INTEGRITY")
   --Repaint()
   -- If needed, force wal_checkpoint databases to make sure everything gets written out
   -- this is a harmless no-op if not using journal_mode=WAL
   dbCheckExecute("PRAGMA wal_checkpoint(FULL);")
   local integrityCheck = true
   for row in db:nrows("PRAGMA quick_check;") do
      --tprint(row)
      if row.integrity_check ~= "ok" then 
         integrityCheck = false
      end
   end
   if not integrityCheck then
      --Note("FAILED INTEGRITY CHECK. CLOSE MUSHCLIENT AND RESTORE A KNOWN GOOD DATABASE.")
      --utils.msgbox("FAILED MAPPER DB INTEGRITY CHECK. CLOSE MUSHCLIENT AND RESTORE A KNOWN GOOD DATABASE IMMEDIATELY.", "Error!", "ok", "!", 1)
      return false
   end
   --Note("INTEGRITY CHECK PASSED")
   return true
end

function update_gmcp_area(gmcparea)
	debugPrint("saving area"..gmcparea.name)
   local areaid = gmcparea.id
   local areaname = gmcparea.name
   local texture = gmcparea.texture
   local color = gmcparea.col
   local x,y,z = gmcparea.x,gmcparea.y,gmcparea.z

   dbCheckExecute (string.format (
      "REPLACE INTO areas (uid, name, date_added, texture, color) VALUES (%s, %s, DATETIME('NOW'), %s, %s);",
      fixsql (areaid), 
      fixsql (areaname),
      fixsql (texture),
      fixsql (color)
   ))

   area = {
      name = areaname,
      texture = texture,
      color = color
   }
   areas [areaid] = area

   Send_GMCP_Packet("request room") -- Just got a new area update. Now check for our room again.
   return
end

-- original findpath function idea contributed by Spartacus
function findpath(src, dst, noportals, norecalls)
   local depth = 0
   local max_depth = 300
   local room_sets = {}
   local rooms_list = {}
   local found = false
   local ftd = {}
   local f = ""
   local next_room = 0
  
   if type(src) ~= "number" then
      src = string.match(src, "^(nomap_.+)$") or tonumber(src)
   end
   if type(dst) ~= "number" then
      dst = string.match(dst, "^(nomap_.+)$") or tonumber(dst)
   end
   
   if src == dst or src == nil or dst == nil then
   	  debugPrint("src or dst is nil or equal")
      return {}
   end
   
   src = tostring(src)
   dst = tostring(dst)
   
   table.insert(rooms_list, fixsql(dst))
  
   local visited = ""
   --local main_status = GetInfo(53)
   while not found and depth < max_depth do
      --SetStatus(main_status.." (searching depth "..depth..")")
     -- BroadcastPlugin (999, "repaint")
      depth = depth + 1
      if depth > 1 then
         ftd = room_sets[depth-1] or {}
         rooms_list = {}
         for k,v in pairs(ftd) do
            table.insert(rooms_list, fixsql(v.fromuid))
         end -- for from, to, dir      
      end -- if depth

      -- prune the search space
      if visited ~= "" then 
         visited = visited..","..table.concat(rooms_list, ",")
      else
         if noportals then
            visited = visited..fixsql("*")..","
         end
         if norecalls then
            visited = visited..fixsql("**")..","
         end
         visited = visited..table.concat(rooms_list, ",")
      end
    
      -- get all exits to any room in the previous set
      local q = string.format ("select fromuid, touid, dir from exits where touid in (%s) and fromuid not in (%s) and level <= %s order by length(dir) asc",table.concat(rooms_list,","), visited, 9999)
      debugPrint("findpath query:"..q)
      local dcount = 0
      room_sets[depth] = {}
      for row in db:nrows(q) do
         dcount = dcount + 1
         -- ordering by length(dir) ensures that custom exits (always longer than 1 char) get 
         -- used preferentially to normal ones (1 char)
         room_sets[depth][row.fromuid] = {fromuid=row.fromuid, touid=row.touid, dir=row.dir}
         if row.fromuid == "*" or (row.fromuid == "**" and f ~= "*" and f ~= src) or row.fromuid == src then
            f = row.fromuid
            found = true
            found_depth = depth
         end -- if src
      end -- for select

      if dcount == 0 then
      	debugPrint("no path from here to there")
         return -- there is no path from here to there
      end -- if dcount
   end -- while
  
   if found == false then
   		debugPrint("did the bizness and didn't find a path")
      return
   end
  
   -- We've gotten back to the starting room from our destination. Now reconstruct the path.
   local path = {}
   -- set ftd to the first from,to,dir set where from was either our start room or * or **
   ftd = room_sets[found_depth][f]
   
   if not rooms[src] then
      rooms[src] = load_room_from_database(src)
   end
   
   if (f == "*" and rooms[src].noportal == 1) or (f == "**" and rooms[src].norecall == 1) then
      if rooms[src].norecall ~= 1 and bounce_recall ~= nil then
         table.insert(path, bounce_recall)
      elseif rooms[src].noportal ~= 1 and bounce_portal ~= nil then
         table.insert(path, bounce_portal)
      else
         local jump_room, type = findNearestJumpRoom(src, dst, f)
         if not jump_room then
            return
         end
         local path, first_depth = findpath(src,jump_room, true, true) -- this could be optimized away by building the path in findNearestJumpRoom, but the gain would be negligible
         if bit.band(type, 1) ~= 0 then
            -- type 1 means just walk to the destination
            return path, first_depth
         else
            local second_path, second_depth = findpath(jump_room, dst)
            for i,v in ipairs(second_path) do
               table.insert(path, v)
            end
            return path, first_depth+second_depth
         end
      end
   end

   table.insert(path, {dir=ftd.dir, uid=ftd.touid})
   

   next_room = ftd.touid
   while depth > 1 do
      depth = depth - 1
      ftd = room_sets[depth][next_room]
      next_room = ftd.touid
      debugPrint("building back:"..next_room)
-- this caching is probably not noticeably useful, so disable it for now
--      if not rooms[ftd.touid] then -- if not in memory yet, get it
--         rooms[ftd.touid] = load_room_from_database (ftd.touid)
--      end
      table.insert(path, {dir=ftd.dir, uid=ftd.touid})
   end -- while
   dumpTable("","path",path)
   return path, found_depth
end -- function findpath

function doFindPath()
	--findpath.
	debugPrint("trying to find paths")
	local found,depth = findpath(32418,28634)
	local sw = ""
	for i,v in ipairs(found) do
		sw = sw..v.dir
	end
	debugPrint("path found:"..sw)
	sendToServer("run "..sw)
end

areas = {}
rooms = {}
room_not_in_database = {}
create_tables()
debugPrint("created tables")
RegisterSpecialCommand("dumpdb","doSomething")

RegisterSpecialCommand("findpath","doFindPath")

function doSomething()
	
	for row in db:nrows("SELECT * FROM exits") do
	  dumpTable("","row",row)
	end
end

function fixsql (s)
   if s then
      return "'" .. (string.gsub (s, "'", "''")) .. "'" -- replace single quotes with two lots of single quotes
   else
      return "NULL"
   end -- if
end -- fixsql

function nilToStr(n)
   return (((n ~= nil) and tostring(n)) or "")
end

function compareTables(primary, secondary)
   for i,v in pairs(primary) do
      if secondary[i] ~= v then
         return false
      end
   end
   return true
end

function dumpTable(indent,name,t)
	for i,v in pairs(t) do
		if(type(v) == "table") then
			dumpTable(indent.."  ",name.."."..i,v)
		else
			debugPrint(indent..name.."."..i..":"..v)
		end
	end
end
