package com.offsetnull.bt.docs;

public class DocumentsHolder {

	public static int FOO = 4;
	public DocumentsHolder() {
/*! \page anatomy Plugin Anatomy
\section top General Construction
\subsection intro Introduction
Plugins for BlowTorch are a rough collection of files in a directory on the sdcard (or internal memory). A plugin contains a unique Lua state that it uses to load and run scripts.
A plugin contains a unique set of triggers, timers, aliases, scripts and windows. The BlowTorch main settings file contains a list of externally linked plugins; essentially a list of paths to xml files to load as part of the boot up sequence.
The scripting system will call certain global functions in the Lua state if they are defined at certain points during core operation.
this is what happens when we try to \image html test.jpg "Special Image"	

\subsection construction General Construction
A plugin consists of a snippet of XML that defines key parameters about the configuration and content it carries. Plugins may be grouped into a single file. The general structure of a plugin looks like the following:
\code
<plugin>
	<author>Author Name</author>
	<description>A short description.</description>
	<version>3</version>
	<triggers>
		<trigger name="foo" pattern="bar">
			<gag/>
	</trigger>
</plugin>
\endcode

It essentially looks like a settings file without the root level global configuration nodes. Browsing data elements, such as triggers, timers, or aliases, can be done in client by filtering the items from the extended options button in the appropriate selection dialog. [insert screen shot/pictures]

\subsection loading Loading Plugins
Load plugins using the MENU->Plugins->Load button. Use the Load menu to select valid XML plugin files from the [external memory folder]/BlowTorch/plugins folder. Once loaded, plugins may display description and author information. Invalid plugins will display the parse error to assist with debugging. Already loaded plugins should be indicated through the following icons:
	- Unloaded valid plugin file: [icon image]
	- Loaded valid plugin file: [icon image]
 	- Invalid plugin file: [icon image]
 
\subsection settings Saving Settings and Menu Items
Settings can be implemented in the plugin description file. These settings are available under the MENU->[Your plugin name]. Settings are saved when the BlowTorch core rewrites the plugin descriptor file in the save sequence. 
Settings are identified via the key property, which must be unique. When settings are changed from the the Options menu, it will call the global function OnOptionsChanged if defined in the plugin's Lua state. OnOptionsChanged is also called during the settings loading routine, once for every option that the plugin defines. This gives the opportunity for scripts to appropriately act on the setting value before windows are loaded or normal program operation starts.
 
\subsection windows Windowing System
The windowing system is going to have its own specific documentation but it is worth noting how the structure of the XML looks. As it appears in [link to the big picture], each window contains its own configuration data. The configuration data looks like following:
\code
<blowtorch xmlversion="2">
  	<plugins>
	    <plugin name="layout_manager" id="494">
			<windows>
				<window name="vitals_window" id="1010" script="vitalsWindowScript">
			    	<layoutGroup target="large">
						<layout orientation="landscape" below="6022" width="200" height="50" />
						<layout orientation="portrait" width="fill_parent" height="fill_parent" />
					</layoutGroup>
					<layoutGroup target="xlarge">
						<layout orientation="landscape" below="6022" width="400" height="100" />
						<layout orientation="portrait" width="fill_parent" height="fill_parent" />
					</layoutGroup>
					<options>
						<option key="font_size">13</option>
						<option key="line_extra">2</option>
					</options>
				</window>
			</windows>
		</plugin>
	</plugins>
</blowtorch>
\endcode

The window tag has the following attributes and child nodes:
	- Window Attributes
		- name - the name of the window
		- id - a unique integer to identify this window
		- script - the backing script that defines the window behavior
	- layoutGroup Attributes
		- target - makes this layoutGroup target a specific size bucket, normal, large, xlarge
	- layout Attributes
		- orientation - the orientation target of this layout group, landscape or portrait
		- height - the height of the window in pixels, can also be wrap_content, or fill_parent
		- width - the width of the window in pixels, can also be wrap_content, or fill_parent
		- above - the id of the window to be placed above
		- below - the id of the window to be place below
		- left_of - the id of the window to be to the right of this window (to the left of the target window)
		- right_of - the id of the window to be to the left of this window (to the right of the target)
		- align_parent_top - true or false to align the top of this window to the top of the parent view container.
		- align_parent_bottom - true or false to align the bottom of this window to the bottom of the parent view container.
	- options - have children option nodes
		- option Attributes
			- key - the unique option key, this can be any of the following, font_size, line_extra, font_path, color_mode, buffer_size, [Dan get the rest of the complete list]
		- option Text value
			- This value is loaded into the appropriate option at window creation time.

\tableofcontents
*/
		
		
		
		
/*! \page luaoverview Overview of Lua, Java and Android
\section top Overview
\subsection intro Introduction
The Android NDK is used to cross compile the LuaJIT source code and additional libraries into ARMv5 compatible (or other processor architecture) library and loaded into the process by the Linux side of the Android OS. Communication between the Native code and the Dalvik (Java) code is facilitated by the LuaJava API that utilizes java reflection and JNI. This combined with some harness and support code in the BlowTorch core, is how Lua is embedded.
SQLite, luabins, libmarshal, serialize lua modules are pre-built and included with the blowtorch distribution.
\section embedding Embedding
\subsection Overview
image: the big diagram of all the lua states
\subsection native Native Android
It is true that Android runs Java code, but technically it is a Linux process hosting a Dalvik Virtual Machine that is running Dalvik compatible byte code compiled from java code. This process is capable of loading native C code libraries, and the Dalvik VM can interface with this native code using the Java Native Interface (JNI). The JNI has been around for a very long time, much longer than Android.
\subsection separation Process Separation
Android makes developers jump through some hoops to achieve a "background process" that is immune from being terminated and harvested under low memory situations. 
One of these hoops is folding "critical code" into a Service class that is launched and maintained in much the same way as an Activity. 
In practice this resulted in the organization outlined in the above figure, both the background service and foreground activity are their own separate Linux processes, each running a Dalvik virtual machine that hosts Dalvik byte code compiled from Java source code. This has benefits and drawbacks - benefits include clean termination of the foreground process (or crash) and double the heap space for data (2x the processes means 2x the ram, with exceptions), whereas a drawback to this architecture is the inter-process communication method called AIDL.
\subsection aidl The AIDL Bridge
The AIDL bridge is sort of problematic. 
As of this point due to a quirk in the architecture it is a one way serialized communication channel (for our purposes, will explain later). 
The serialized part isn't a big deal for the data that will be passed back and fourth between scripts, but because the of the architecture of the interaction I cannot, at this time, make a Lua call across the bridge and get a return value from it. 
Because of this I have set up the WindowXCallS and PluginXCallS functions, which will call arbitrarily named global functions on the other side of the bridge with a string argument. 
The AIDL bridge has its benefits. It does provide a relatively efficient inter-process communications channel, and it may be possible in the future to open up the two way communication and it will clean up exiting code. 
The AIDL bridge is bi-directional (allows for return values) but there is a dalvik threading matter that I'm not sure how to deal with.
\subsection The BlowTorch Lua API
BlowTorch assigns a unique Lua State to each plugin that hosts the plugin code. 
Additionally, each plugin defined or created window contains a unique Lua State that will drive the window behavior and UI actions. 
Each have different API function calls because some things seemed appropriate to keep only on one side. 
There is more information on this [here], but the API consists of a few global objects that are pushed for convenience and an array of global functions that can be called to get information about the environment and get information about the device or runtime, or set settings, etc.
\section luajava APIs
\subsection overview Overview
LuaJava is an amazingly thin middle layer that sets up a method for exposing and instantiating objects in the Java virtual machine (in Android the Dalvik virtual machine) and provide a mapping to those objects as Lua tables. 
Since Lua runs in native code and interfaces with the Dalvik VM process directly, it can load and work with any class on the virtual machine's classpath. 
The classpath is a hierarchical ordering of classes, like a directory structure, with the forward slash (/) replaced with a period (.).
\subsection reflection Reflection
Reflection is relied on heavily by the LuaJava API; it is actually the entire bread and butter of the magic. 
Reflection in Java (and subsequently Dalvik) is a construct for interrogating and probing an unknown class at runtime to determine what its functions are, including arguments and return values. 
This process can be costly but there are a few ways to optimize its impact.
\subsection luajava_api LuaJava API
Essentially, the core API functions work with a "class" as the first argument. This is usually a string with the full classpath path to the desired object.
\code
paint = luajava.newInstance("android.graphics.Paint") 		  
exit_button = luajava.newInstance("android.widget.Button",context)
\endcode
		 
However, the newInstance method goes through the whole reflection routine each time it is called like this. 
LuaJava provides a way to cache these lookups in userdata tables that also provide access to static functions and fields. 
This cached class can be used as an argument to the new function.
\luacode
Paint = luajava.bindClass("android.graphics.Paint")
Button = luajava.bindClass("android.widget.Button")
     
bg_paint = luajava.new(Paint)
    		 
exit_button = luajava.new(Button,context)
\endluacode
		 
Field and function access
fields such as static constants can be accessed simply through the index method. Functions on non-static classes can be called using the "self notation" function call with the desired function name object:getWidget(foo) -> object["getWidget"](object,foo)
\luacode
Button = luajava.bindClass("android.widget.button")
LinearLayoutParams = luajava.bindClass("android.widget.LinearLayout$LayoutParams")
 
exit_button = luajava.new(Button,context)
exit_button_params = luajava.new(LinearLayoutParams,LinearLayoutParams.FILL_PARENT,LinearLayoutParams.WRAP_CONTENT)
  
exit_button:setLayoutParams(exit_button_params)
\endluacode

Dalvik objects are mapped to Lua tables as userdata with an overridden __index metamethod (among other metamethods), when the __index metamethod is called the LuaJava core code does a reflection lookup on the object to see if that function can be called, appropriate arguments etc and then does so. This is all very expensive, however for functions calls there is no way to speed this up. If you are just accessing static fields I would recommend caching them once in a global or local variable and avoid the reflection penalty. 
\luacode
Button = luajava.bindClass("android.widget.button")
LinearLayoutParams = luajava.bindClass("android.widget.LinearLayout$LayoutParams")

WRAP_CONENT = LinearLayoutParams.WRAP_CONTENT
FILL_PARENT = LinearLayoutParams.FILL_PARENT

exit_button = luajava.new(Button,context)
exit_button_params = luajava.new(LinearLayoutParams,FILL_PARENT,WRAP_CONTENT)

exit_button:setLayoutParams(exit_button_params)
\endluacode

You can also make the LuaJava API calls local to speed them up.
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

There are many ways to optimize the Lua code and I am no expert. These scripts are works in progress and earlier code may not be as effecient as the methods used in scripts developed more recently.

Proxy interfaces
Proxy interfaces are the best trick for exploiting the android operating system by far. The function luajava.createProxy() takes a class path string to an interface (or group of interfaces, and no caching on this one yet) and a table that implements the methods of the desired interfaces. This creates a proxy object that can be handed directly to Java and it will invoke Lua methods when called.
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

This works for anything that is defined as an interface in the Android documentation. Abstract classes, while proxyable in regular Java VM land, are not proxyable at this point in Android's lifecycle. Anything that takes an implementation of an abstract class can't be used, but I will provide helper functions to do so. See the autocomplete demo.

I added to the LuaJava API set while creating the Chat Window and Button Window. I required arrays of Dalvik objects and was able to construct them as needed. This method had no problems, until trying to use real file I/O using the Dalvik classes. When I needed a byte[] array in Lua, I was in trouble. The problem with the LuaJava API is that everything is run through the same set of functions and those functions look for things that are synonymous with Lua data types. It converts numbers to the Lua number (double), java.lang.String to Lua strings and so forth. Under this rule, byte arrays are treated as raw Lua strings and that was causing a problem for me. Therefore, I made luajava.array which takes a classpath class lookup (no cached classes yet) and makes a simple array, returning it without changing the type.
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
