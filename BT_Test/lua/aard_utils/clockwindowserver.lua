require("serialize")

forecasted = false
forecastTicks = -1
forecastDuration = -1

function ticked()
	--debugPrint("ticked")
	timers.grey = timers.grey + 1
	timers.white = timers.white + 1
	timers.black = timers.black + 1
	if(timers.grey == 31) then
		timers.grey = 1
	end
	if(timers.black == 51) then
		timers.black = 1
	end
	if(timers.white == 66) then
		timers.white = 1
	end
	
	if(timers.forecast > 0) then
		timers.forecast = timers.forecast -1
	else
		if(timers.duration > 0) then
			timers.duration = timers.duration -1
		else
			forecasted = false
		end
	end
	
	if(greyCatch == 1 and whiteCatch == 1 and blackCatch == 1 and not forecasted) then
		forecastTicks, forecastDuration = doForecast()
		timers.forecast = forecastTicks
		timers.duration = forecastDuration
		Note(string.format("\nTriple moons forecasted: in %d ticks, for %d",forecastTicks,forecastDuration))
		forecasted = true
	end

	WindowXCallS("clock_widget","tickIncoming",serialize(timers))
end

function timeCatcher(name,line,map)

	--debugPrint("TIMERTEST: "..map["2"])
	str = ""
	if(map["2"] ~= nil) then
		num = 0
		if(map["2"] == "Midnight") then
			str = "00:00"
			num = 0
		elseif(map["2"] == "Noon") then
			str = "12:00"
			num = 12
		elseif(map["2"] == "am") then
			num = tonumber(map["1"])
			str = map["1"]..":00"
		elseif(map["2"] == "pm") then
			num = tonumber(map["1"])
			num = num+12
			str = num..":00"
		end
		--debugPrint(str)
		WindowXCallS("clock_widget","setTimeOrdinal",num)
	end
	
end

function yearCatcher(name,line,map)
	--debugPrint("in yearcatcher")
end

function OnMoonRise(name,line,map)
	moon = map["1"]
	--debugPrint(moon.." rising.")
	if(moon == "grey") then
		timers.grey = 22
		greyCatch = 1
	elseif(moon == "black") then
		timers.black = 37
		blackCatch = 1
	else
		timers.white = 48
		whiteCatch = 1
	end
end

function OnMoonSet(name,line,map)
	moon = map["1"]
	--debugPrint(moon.." setting.")
	if(moon == "grey") then
		timers.grey = 29
		greyCatch = 1
	elseif(moon == "black") then
		timers.black = 49
		blackCatch = 1
	else
		timers.white = 64
		whiteCatch = 1
	end
end

function doForecast()
	if(timers.grey <= 23 and timers.grey >= 30 and timers.black >= 38 and timers.black <= 50 and timers.white >=49 and timers.white <= 65) then
	--triple moons is happening right now
		return 0,0
	end
	
	tmp = {}
	tmp.black = timers.black
	tmp.white = timers.white
	tmp.grey = timers.grey
	
	ticksfromnow = 0
	
	done = false
	tmpForecast = -1
	tmpDuration = -1
	
	while(not done and ticksfromnow < 900) do
		ticksfromnow = ticksfromnow + 1
		
		tmp.black = tmp.black + 1
		tmp.white = tmp.white + 1
		tmp.grey = tmp.grey + 1
		
		if(tmp.black == 51) then tmp.black = 1 end
		if(tmp.white == 66) then tmp.white = 1 end
		if(tmp.grey == 31) then tmp.grey = 1 end
		
		if(tmp.grey >= 23 and tmp.grey <= 30 and tmp.black >= 38 and tmp.black <= 50 and tmp.white >=49 and tmp.white <= 65) then
			--triple moons is happening right now
			tmpForecast = ticksfromnow
			
			local diffGrey = 30-tmp.grey
			local diffBlack = 50-tmp.black
			local diffWhite = 65-tmp.white
			
			tmpDuration = diffGrey
			if(diffBlack < diffGrey) then tmpDuration = diffBlack end
			if(diffWhite < diffBlack) then tmpDuration = diffWhite end
			done = true
		end
		
	end
	
	return tmpForecast-1,tmpDuration+1
end

timers = {}
timers.grey = -100
timers.white = -100
timers.black = -100
timers.forecast = -100
timers.duration = -100

greyCatch = 0
whiteCatch = 0
blackCatch = 0