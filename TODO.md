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
      - [ ] not ruined by clear control chars and winch signals
    - [ ] yanking of current settings
      - [x] on X11
      - [ ] on windows, detecting it before java.awt.datatransfer cries
    - [ ] yanking the entire buffer
    - [ ] immediately execute non-interactive parameters as commands, like `less +X`, `bash -c`
- [ ] explore and fix selection loop issues
    - [ ] finish KeyBoard TODOs
    - [x] now breaks while completing first round
    - [x] revamp and fix loop
    - [ ] ~~add alphabet string as parameter for reselect to get rid of unnecessary reselect loop~~
    - [ ] ~~or another exception and message printing~~
    - ~~or limit completion and entry to the alphabetString entered in the first round and add a message when a different~~
      ~~one is attempted to be used~~
    - ~~when one does incompatible eWheel, reselect, incompatible rotors, reselect, the process seems to be incorrect~~
    - ~~also when backing out one has to press Ctrl+C once for every call site layered on top~~
    - ~~also exiting out of plugboard selection prints UserInterruptedException~~
    - ~~could also use a good refactoring~~
- [x] try and unify three processSelectX methods
    - could do reselection round by adding all scramblers to a list and filtering the current type
    - would require case separation for rotors
- [x] option to pass params to interactive mode
    - [x] change condition for interactive mode to not having `-` or `-f` params
- [ ] make keymap more intuitive and flexible
    - [ ] make keymap configurable
        - complicated, maybe not necessary
    - [ ] print help that reflects current keymap config
    - will need to deal with collisions
- [ ] print help at beginning
    - [x] fix unreliable anyKey
- [x] option to print auto entry/reflector for portability
    - [x] option to print entire machine state
- [ ] maybe do something sneaky for "security" of serialized scramblers
    - like gpg/rsa/ssh/whatever barcode thing
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
- [ ] open issue about OOBE in LineReader:5733 where secondaryPrompts has size 0
- [ ] implement ring settings
    - [ ] ring setting not part of customrotortype, should be, even if mutable
- [ ] character encoding shenanigans
- [ ] rename ...Types to ...Factories?
- [x] fully implement PlugBoard
    - [x] implement interactive setting of PlugBoard
    - [x] implement non-interactive setting of PlugBoard
        - source and dest will most likely have to go into two different options because we cannot guarantee essentially
          any character being out-of-band for the alphabet
        - the first character not in the current alphabet could be attempted as a separator
    - [x] add auto method and reselect prompt to other prompts
- [x] re-check missed details under [Turnover](https://en.wikipedia.org/wiki/Enigma_machine#Turnover)
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
        - [x] on start
            - [x] from FS
            - [x] from JAR
        - [ ] precedence, overwriting
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
- [x] some container binding Armature and PlugBoard
    - [x] could eventually become the common basis with other machines
    - [x] unify/move scramblerWiring in Armature into Enigma such that Plugboard is also connected
    - [x] start refactoring work
- [ ] cryptanalysis features
    - [ ] histogram and other widgets to demonstrate attack vectors
    - [ ] Bombe cracking simulation
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