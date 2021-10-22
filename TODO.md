## TOC

- [interactive mode](#interactive-mode)
- [non-interactive mode](#non-interactive-mode)
- [general](#general)
- [issues](#issues)

### interactive mode

- [ ] reintroduce status bar
    - [ ] rotor displays
        - [ ] maybe implement offset change on cursor move already
            - in such a way that there's only one rotor offset for each buffer index
    - [x] remove logback as feedback source
- [x] look into full-screen mode
- [ ] set offsets
    - [ ] think about how to accomplish nice wheel-like UI for it
    - [ ] which should later grow into a widget
    - [ ] multi-threaded blinking?
- [ ] command mode instead of obscure shortcuts
    - [ ] help command
        - [ ] bind F1 to help command
    - [ ] loading of ScramblerTypes
    - [ ] loading of Armature configs
    - [ ] writing of buffer to file
    - [ ] reloading configs from canonical sources
    - [ ] printing of current settings
    - [ ] yanking the entire buffer
- [x] option to pass params to interactive mode
    - [x] change condition for interactive mode to not having `-` or `-f` params
- [ ] make keymap more intuitive and flexible
    - [ ] make keymap configurable
        - complicated, maybe not necessary
    - [ ] print help that reflects current keymap config
    - will need to deal with collisions
- [ ] print help at beginning
    - [x] fix unreliable anyKey
- [ ] option to print auto entry/reflector for portability
    - [ ] maybe do something sneaky for "security"
        - like gpg/rsa/ssh/whatever barcode thing
    - [ ] option to print entire machine state
- [ ] widget system
    - [ ] facsimile widgets for
        - [ ] keyboard?
        - [ ] plugboard
        - [ ] rotor display
        - [ ] lightboard
    - [ ] detail mode widget partially superseded by above
    - [ ] auto-arrange is hard
    - [ ] auto-resize is hard
    - [ ] start with fixed sizes
    - [ ] serialize/deserialize widget configs like tmux/byobu
        - [ ] open certain configs with configurable key bindings
    - [ ] border collapse
- [x] handle WINCH and resize correctly
- [x] ability to handle newlines in buffer
- [x] move up and down in buffer
- [ ] scroll display as well
    - without moving widgets off-screen
- [ ] taint and recalculation
    - [ ] will need highlighting
    - [ ] some sort of cache for recalculation (optionally?), depends on speed for realistic loads
    - [ ] recalculate with each keystroke or highlight invalid intervals
    - [ ] machine state at buffer index 0 determines scrambling output for each char in buffer
        - [ ] some sort of `offset(substring, amount)` method is needed
            - [ ] where to put it
    - [ ] handle newlines

### non-interactive mode

- [ ] find standard/established way of handling parameters
    - [ ] pairwise handling of params instead of `java`/`sed -i` type
    - [ ] some self-documented help option
- [ ] set offsets
- [ ] load machine state from file
    - [ ] basically (de)serialization
    - [ ] think about "security" implications

### general

- [x] open issue about SelectionReader and BindingReader
- [ ] fully implement PlugBoard
    - [ ] implement interactive setting of PlugBoard
    - [ ] implement non-interactive setting of PlugBoard
        - source and dest will most likely have to go into two different options because we cannot guarantee essentially
          any character being out-of-band for the alphabet
        - the first charcter not in the current alphabet could be attempted as a separator
    - [x] add auto method and reselect prompt to other prompts
- [ ] implement ring settings
- [ ] settings files
    - [ ] consider using Properties API for settings
    - [ ] alternatively, just JAXB
- [ ] composite config sources? cli params + settings?
    - [x] are settings files even needed?
        - [ ] set up search in canonical locations
            - [ ] pwd, ~/.ejigma, cli
            - [ ] precedence
                - should be: static files < ~/.ejigma < pwd < cli
- [ ] set up scrambler type loading and reloading in canonical locations
    - [ ] loading
        - [ ] on start
        - [ ] using shortcut
        - [ ] from cli param
    - [ ] reloading (interactive)
        - [ ] using shortcut
        - [ ] entering a filename
            - [ ] FS hinting?
- [ ] file reading and writing
    - [ ] open (in the FS sense) file as output `-o`
        - [ ] in interactive mode, create an empty file as output
            - [ ] exit if non-empty for now, maybe later allow a force option `-O`
            - [ ] alternatively, see below
        - [ ] save on shortcut or autosave
            - [ ] config option
        - [ ] same param could set output file for non-interactive mode
    - [ ] open file and encrypt contents `-f`
        - [ ] interactive mode could allow the opening and encryption of a file into the buffer
            - [ ] autosave shouldn't interfere with opening files this way
                - [ ] when `-f` or the equivalent command is used, don't set the file as the output
            - [ ] large/binary file warning
                - [ ] consider for non-interactive as well
        - [x] non-interactive file encryption
- [x] get new name (rotor stream cypher something something, YARSCE?)
- [ ] get better name
- [x] remove Spring and handle lifecycles like a big boy
    - [ ] ~~develop some alternative to the bean registry~~
- [ ] some container binding Armature and PlugBoard
    - [ ] could eventually become the common basis with other machines
    - [ ] unify/move scramblerWiring in Armature into Enigma such that Plugboard is also connected
    - [x] start refactoring work
- [ ] cryptanalysis features
    - [ ] histogram and other widgets to demonstrate attack vectors
    - [ ] BOMB cracking simulation
- [ ] create an API-like version for use in a web service for example
    - [x] maybe with single top level object like so many others
- [ ] think about delivery, installation
    - [x] single file executable
        - doesn't work on Windows, maybe try BAT equivalent
    - [ ] launch4j
        - Minecraft uses it, must be good enough
    - [ ] if all else fails, look into installers
- [ ] Generalize across rotary cypher machines
    - [ ] what's an Uhr?
    - [ ] think about what could be reused for Typex, Lorenz
        - [ ] Typex, Lorenz would need radically different buffer functions
        - [ ] five-hole output?
    - [ ] include every model and scrambler under the Sun for brownie points
    - [ ] compatibility considerations beyond alphabetString (?)

### issues

* in Windows terminal WSL2, OSX Terminal and iTerm, switching anything using the SelectionReader breaks arrow key
  navigation at the `readBinding()` - `getBound()` level
    * seems like SelectionReader is capturing some part of the input bytes
        * the first arrow key press after switching doesn't produce enough bytes to constitute a valid entry in the
          alphabet and so does nothing
    * works in CMD, presumably PS