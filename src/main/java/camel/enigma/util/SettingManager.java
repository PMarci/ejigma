package camel.enigma.util;

import camel.enigma.io.LightBoard;
import camel.enigma.model.Armature;
import org.jline.terminal.Terminal;
import org.jline.utils.InfoCmp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SettingManager {

    private static final Logger logger = LoggerFactory.getLogger(SettingManager.class);

    @Autowired
    private Armature armature;

    @Autowired
    private LightBoard lightBoard;
//
//    @Autowired
//    private CamelContext camelContext;

    private static boolean detailMode;

    public void handleControlInput(ScrambleResult scrambleResult) {
        handleControlInput(scrambleResult.isDetailModeToggle(),
                           scrambleResult.isClearBuffer(),
                           scrambleResult.isResetOffsets());
    }

//    @Handler
    public void handleControlInput(
        /*@ExchangeProperty(Properties.DETAIL_MODE_TOGGLE) */Boolean detailModeToggle,
        /*@ExchangeProperty(Properties.CLEAR_BUFFER) */Boolean clearBuffer,
        /*@ExchangeProperty(Properties.RESET_OFFSETS) */Boolean resetOffsets) {

        Terminal terminal = lightBoard.getTerminal();
        if (detailModeToggle != null && detailModeToggle) {
            terminal.puts(InfoCmp.Capability.scroll_forward);
            terminal.puts(InfoCmp.Capability.cursor_down);
            terminal.puts(InfoCmp.Capability.clear_screen);
            logger.info("\nReceived Ctrl+B, toggling detail mode...");
            toggleDetailMode();
        }
        if (resetOffsets != null && resetOffsets) {
            terminal.puts(InfoCmp.Capability.newline);
            logger.info("\nReceived Ctrl+R, resetting offsets...");
            armature.resetOffsets();
        }
        if (clearBuffer != null && clearBuffer) {
            lightBoard.clearBuffer();
            lightBoard.redisplay();
        }
    }

//    private LightBoard getLightBoard() {
//        return camelContext.getEndpoint("keyboard", KeyBoardEndpoint.class).getLightBoard();
//    }

    private static void toggleDetailMode() {
        setDetailMode(!isDetailMode());
    }

    public static boolean isDetailMode() {
        return detailMode;
    }

    private static void setDetailMode(boolean detailMode) {
        SettingManager.detailMode = detailMode;
    }
}
