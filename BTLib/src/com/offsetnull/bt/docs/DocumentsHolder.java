package com.offsetnull.bt.docs;

public class DocumentsHolder {

	public static int FOO = 4;
	public DocumentsHolder() {
/*! \page luaoverview Overview of Lua, Java and Android
\section overview Overview
\subsection intro Introduction
The Android NDK is used to cross compile the LuaJIT source code and additional libraries into ARMv5 compatible (or other processor architecture) library and loaded into the process by the Linux side of the Android OS. Communication between the Native code and the Dalvik (Java) code is facilitated by the LuaJava API that utilizes java reflection and JNI. This combined with some harness and support code in the BlowTorch core, we arrive at the lua solution presented.
SQLite, luabins, libmarshal, serialize lua modules are pre built and included with the blowtorch distribution.
\section Embedding
\subsection Overview
image: the big diagram of all the lua states
\subsection native Native Android
It is true that Android runs java code, but technically it is a Linux process hosting a Dalvik Virtual Machine that is running dalvik compatible byte code compiled from java code. This process is capable of loading native C code libraries, and the Dalvik VM can interface with this native code using the Java Native Interface (JNI). The JNI has been around for a very long time, much longer than android.
\subsection separation Process Separation
Android makes developers jump through some hoops to achieve a "background process" that is immune from being terminated and harvested under low memory situations.
One of these hoops is folding "critical code" into a Service class that is launched and maintained in much the same way as an Activity.
In practice this resulted in the organization outlined in the above figure, both the background service and foreground activity are thier own seperate linux processes, each running a dalvik virtual machine that is hosting dalvik byte code compiled from java source code.  This has benefits and drawbacks, a benefits include clean termination of the foreground process (or crash) and double the heap space for data (2x the processes means 2x the ram, with exceptions), a partical downside to this architecture is the inter process communication method called AIDL. 
\subsection aidl The AIDL Bridge
The AIDL bridge is sort of problematic, as of this point due to a quirk in the architecture it is a one way serialized communication channel (for our purposes, will explain later). The serialized part isn't a big deal for the data that will be passed back and fourth between scripts, but because the of the architecture of the interaction I cannot at this time make a lua call across the bridge and get a return value from it. So because of this I have set up the WindowXCallS and PluginXCallS functions, which will call arbitrarily named global functions on the other side of the bridge with a string argument. The AIDL bridge has its benefits as it does provide a relatively efficient inter-process communications channel, and it may be possible in the future to open up the two way communication and it will clean up exiting code. In its nature the AIDL bridge is bi-directional (allows for return values) but there is a dalvik threading matter that I'm not sure how to deal with.
\subsection The BlowTorch Lua API
So in essence, there are background Lua States that host the main plugin code, and the foreground lua states that drive the windowing and ui action. Each have different API function calls because some things seemed appropraite to keep only on one side. There is more information on this [here], but the API consists of a few global objects that are pushed for convienience and an array of global functions that can be called to get information about the environment and get information about the device or runtime, or set settings, etc.
\section luajava APIs
\subsection overview Overview
LuaJava is an amazingly thin middle layer that sets up a method for exposing and instantiating objects in the Java virtual machine (in android the Dalvik virtual machine) and provide a mapping to those objects as lua tables. Since lua runs in native code and interfaces with the Dalvik VM process directly, it can load and work with any class on the virtual machine's classpath. If you are not familiar with the class path look here, or in summary, it is a hierarchical ordering of classes that is a directory structure of sorts with the / replaced with a .
\subsection reflection Reflection
Reflection is relied on heavily by the luajava api, it is actually the entire bread and butter of the magic. Reflection in java (and subsequently Dalvik) is a construct for interrogating and probing an unknown class at runtime to determine what its functions are, including arguments and return values. This process can be costly but there are a few ways to optimize the impact of it.
\subsection luajava_api LuaJava API
Essentially the core api functions work with a "class" as the first argument, this is usually a string with the full classpath path to the desired object.
\code
paint = luajava.newInstance("android.graphics.Paint") 		  
exit_button = luajava.newInstance("android.widget.Button",context)
\endcode
		 
However the newInstance method goes through the whole reflection routine each time it is called like this. LuaJava provides a way to cache these lookups in userdata tables that also provide access to static functions and fields. This cached class can be used as an argument to the new function.
\luacode
Paint = luajava.bindClass("android.graphics.Paint")
Button = luajava.bindClass("android.widget.Button")
     
bg_paint = luajava.new(Paint)
    		 
exit_button = luajava.new(Button,context)
\endluacode
		 
Field and function access
fields such as static constants can be accessed simply through the index method, functions on non-static classes can be called using the "self notation" function call with the desired function name object:getWidget(foo) -> object["getWidget"](object,foo)
\luacode
Button = luajava.bindClass("android.widget.button")
LinearLayoutParams = luajava.bindClass("android.widget.LinearLayout$LayoutParams")
 
exit_button = luajava.new(Button,context)
exit_button_params = luajava.new(LinearLayoutParams,LinearLayoutParams.FILL_PARENT,LinearLayoutParams.WRAP_CONTENT)
  
exit_button:setLayoutParams(exit_button_params)
\endluacode

Dalvik objects are mapped to lua tables as userdata with an overridden __index metamethod (among other metamethods), when the __index metamethod is called the luajava core code does a reflection lookup on the object to see if that function can be called, appropriate arguments etc and then does so. This is all very expensive, however for functions calls there is no way to speed this up. If you are just access static fields I would recommend caching them once in a global or local and avoid the reflection penalty. 
\luacode
Button = luajava.bindClass("android.widget.button")
LinearLayoutParams = luajava.bindClass("android.widget.LinearLayout$LayoutParams")

WRAP_CONENT = LinearLayoutParams.WRAP_CONTENT
FILL_PARENT = LinearLayoutParams.FILL_PARENT

exit_button = luajava.new(Button,context)
exit_button_params = luajava.new(LinearLayoutParams,FILL_PARENT,WRAP_CONTENT)

exit_button:setLayoutParams(exit_button_params)
\endluacode

You can also make the luajava api calls local to speed them up.
\luacode
local bind = luajava.bindClass
local newobj = luajava.new
 
Button = bind("android.widget.button")
LinearLayoutParams = bind("android.widget.LinearLayout$LayoutParams")

WRAP_CONENT = LinearLayoutParams.WRAP_CONTENT
FILL_PARENT = LinearLayoutParams.FILL_PARENT

exit_button = newobj(Button,context)
exit_button_params = newobj(LinearLayoutParams,FILL_PARENT,WRAP_CONTENT)

exit_button:setLayoutParams(exit_button_params)
\endluacode

There are many ways to optimize the lua code and I am no expert. Many of the scripts I've given as examples were works over time and as I got better with lua some things improved and the original stuff didn't get as good of job.

Proxy interfaces
This is the best trick for exploiting the android operating system by far. The function luajava.createProxy() takes a class path string to an interface (or group of interfaces, and no caching on this one yet) and a table that implements the methods of the desired interfaces. This creates a proxy object that can be handed directly to java and it will invoke lua methods when called.
\luacode
local bind = luajava.bindClass
local newobj = luajava.new

Button = bind("android.widget.button")
LinearLayoutParams = bind("android.widget.LinearLayout$LayoutParams")

WRAP_CONENT = LinearLayoutParams.WRAP_CONTENT
FILL_PARENT = LinearLayoutParams.FILL_PARENT
 
exit_button = newobj(Button,context)
exit_button_params = newobj(LinearLayoutParams,FILL_PARENT,WRAP_CONTENT)
  
exit_button:setLayoutParams(exit_button_params)

clicker = {}
function clicker.onClick(view)
	Note("Button Clicked!")
end
clicker_proxy = luajava.createProxy("android.view.View$OnClickListener",clicker)
exit_button:setOnClickListener(clicker_proxy)
layout:addView(exit_button)
\endluacode

And this works for anything that is defined as an interface in the android documentation. Abstract classes, while proxyable in regular Java VM land, are at this point in Android's lifecycle not proxyable. So anything that takes an implementation of an abstract class can't be used, but I will provide helper functions to do so. See the autocomplete demo.

There is one last thing to talk about which is an addition I made to to luajava api set. In the construction of the Chat window and the Button window I had the need for arrays of Dalvik objects and was able to construct them as I saw fit and everything worked fine. Until I had tried to do real file io using the Dalkvik classes, I had no problems. But when I needed a byte[] array in lua I was in trouble. The problem with the luajava api is that everything is run through the same set of functions and those functions look for things that are synonmymous with lua data types. So it convertes numbers to the lua number (double) and java.lang.String to lua strings and so forth. Under this rule byte arrays are treated as raw lua strings and that was causing a problem for me. So I made luajava.array which takes a classpath class lookup (no cached classes yet) and makes a simple array and returns it without changing the type.
\luacode
Byte = luajava.bindClass("java.lang.Byte")
RawByte = Byte.TYPE

raw_byte_array = luajava.array(RawByte,1024)

--equivlenet code
Array = luajava.bindClass("java.lang.reflection.Array")
Byte = luajava.bindClass("java.lang.Byte")
RawByte = Byte.TYPE

raw_byte_array = Array:newInstance(RawByte,1024)
--only raw_byte_array would be a string, so if passed to a function that expects a byte[] it would error.
\endluacode
\tableofcontents

*/
	}
	
	public static int getInt() {
		return 4;
	}
}
