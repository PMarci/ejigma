### interactive mode

1. reintroduce status bar
    1. ~~remove logback as feedback source~~ ✔
2. ~~look into full-screen mode~~ ✔
3. set offsets
    1. think about how to accomplish nice wheel-like UI for it
    2. multi-threaded blinking?
4. command mode instead of obscure shortcuts
    1. help command
        1. bind F1 to help command
    2. loading of ScramblerTypes
    3. loading of Armature configs
    4. writing of buffer to file
    5. reloading configs from canonical sources
    6. printing of current settings
    7. yanking the entire buffer
5. option to pass params to interactive mode
    1. change condition for interactive mode to not having `-` or `-f` params
6. make keymap more intuitive and flexible
    1. make keymap configurable
        1. complicated, maybe not necessary
    2. print help that reflects current keymap config
    3. will need to deal with collisions
7. print help at beginning
    1. fix unreliable anyKey
8. option to print auto entry/reflector for portability
    1. maybe do something sneaky for "security"
    2. option to print entire armature state
9. widget system
    1. facsimile widgets for
        1. keyboard?
        2. plugboard
        3. rotor display
        4. lightboard
    2. detail mode widget partially superseded by above
    3. auto-arrange is hard
    4. auto-resize is hard
    5. start with fixed sizes
    6. serialize/deserialize widget configs like tmux/byobu
        1. open certain configs with configurable key bindings
    7. border collapse
10. ~~handle WINCH and resize correctly~~ ✔
11. ~~ability to handle newlines in buffer~~ ✔
12. ~~move up and down in buffer~~ ✔
13. taint and recalculation
    1. will need highlighting
    2. some sort of cache for recalculation (optionally?), depends on speed for realistic loads
    3. recalculate with each keystroke or highlight invalid intervals
    4. armature state at buffer index 0 determines scrambling output for each char in buffer
        1. some sort of `offset(substring, amount)` method is needed
            1. where to put it
    5. handle newlines

### non-interactive mode

1. find standard/established way of handling parameters
    1. pairwise handling of params instead of `java`/`sed -i` type
    2. some self-documented help option
2. set offsets
3. load armature state from file
    1. basically (de)serialization
    1. think about "security" implications

### general

1. open issue about SelectionReader and BindingReader
2. fully implement PlugBoard
    1. implement interactive setting of PlugBoard
3. implement ring settings
4. settings files
    1. consider using Properties API for settings
    2. alternatively, just JAXB
5. composite config sources? cli params + settings?
    1. ~~are settings files even needed?~~ ✔
        1. set up search in canonical locations
            1. pwd, ~/.ejigma, cli
            2. precedence
                1. static files < ~/.ejigma < pwd < cli
6. set up scrambler type loading and reloading in canonical locations
    1. loading
        1. on start
        2. using shortcut
        3. from cli param
    2. reloading (interactive)
        1. using shortcut
        2. entering a filename
            1. FS hinting?
7. file reading and writing
    1. open (in the FS sense) file as output `-o`
        1. in interactive mode, create an empty file as output
            1. exit if non-empty for now, maybe later allow a force option `-O`
            2. alternatively, see below
        2. save on shortcut or autosave
            1. config option
        3. same param could set output file for non-interactive mode
    2. open file and encrypt contents `-f`
        1. interactive mode could allow the opening and encryption of a file into the buffer
            1. autosave shouldn't interfere with opening files this way
                1. when `-f` or the equivalent command is used, don't set the file as the output
            2. large/binary file warning
                1. consider for non-interactive as well
        2. ~~non-interactive file encryption~~ ✔
8. ~~get new name (rotor stream cypher something something, YARSCE?)~~ ✔
9. get better name
10. ~~remove Spring and handle lifecycles like a big boy~~ ✔
    1. ~~develop some alternative to the bean registry~~
11. some container binding Armature and PlugBoard
    1. could eventually become the common basis with other machines
12. cryptanalysis features
    1. histogram and other widgets to demonstrate attack vectors
    2. BOMB cracking simulation
13. create an API-like version for use in a web service for example
    1. maybe with single top level object like so many others
14. think about delivery, installation
    1. ~~single file executable~~ ✔
        1. doesn't work on Windows
            1. maybe try BAT equivalent
    2. launch4j
        1. Minecraft uses it, must be good enough
    3. if all else fails, look into installers
15. Generalize across rotary cypher machines
    1. what's an Uhr?
    2. think about what could be reused for Typex, Lorenz
        1. Typex, Lorenz would need radically different buffer functions
        2. five-hole output?
    3. include every model and scrambler under the Sun for brownie points
    4. compatibility considerations beyond alphabetString (?)

### issues

* in Windows terminal WSL2, OSX Terminal and iTerm, switching anything using the SelectionReader breaks arrow key
  navigation at the `readBinding()` - `getBound()` level
    * seems like SelectionReader is capturing some part of the input bytes
        * the first arrow key press after switching doesn't produce enough bytes to constitute a valid entry in the
          alphabet and so does nothing
    * works in CMD, presumably PS