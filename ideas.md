* make scrambleresult accept text
    * history could then contain the text after each scrambler
    * implement string methods in scrambler and scramblerwheel that iterate over chars
    * detail mode will need to only accept chars or include a stepping function
    * the status could display the buffer text in the vicinity
        * maybe show buffer in a smaller scrolling region below wiring diagram and show highlighted char
        * this would mean each char would need to be associated with a list of historyentries
* highlight invalidly encoded regions after insert/delete in the middle = bracket matching problem
* shortcut to reencode buffer
* this will necessitate being able to assign an offset to each character pos in the buffer
* histogram widget
* JSON/YAML-agnostic config loading
    * "take inspiration" from FSCrawler (if it even has that)
* maybe some sort of QR-code or ssh image kind of functionality
    * using a standard library or rolling own for fun