
function mult(value)
for t=1,value*3 do
	Note("I AM LUA!")
end
return value*3
end

function auto(cmd,start,stop,target)
for t=start,stop do
	Note(cmd.." "..t.."."..target)
end

end