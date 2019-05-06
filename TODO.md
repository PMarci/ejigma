### interactive mode

1. reintroduce status bar
    1. remove logback as feedback source
1. look into fullscreen mode
1. set offsets
    1. think about how to accomplish nice wheel-like UI for it
    1. multithreaded blinking?
1. option to print auto entry/reflector for portability
    1. maybe do something sneaky for "security"
    1. option to print entire armature state
1. handle WINCH and resize correctly
1. ability to handle newlines in buffer
1. move up and down in buffer
1. taint and recalculation
1. some sort of cache for recalculation (optionally?), depends of speed for realistic loads

### non-interactive mode

1. file reading and writing? maybe superfluous because of redirection
1. load armature state from file
   1. basically (de)serialization
   1. think about "security" implications

### general

1. fully implement PlugBoard
1. get new name (rotor stream cypher something something)
1. remove Spring and handle lifecycles like a big boy
   1. develop some alternative to the bean registry
   1. using Properties API
   1. JNDI?
1. create an API-like version for use in a web service for example
   1. maybe with single top level object like so many others
   1. think about what could be reused for typex, lorenz
1. what's an Uhr?
1. cryptanalysis features
