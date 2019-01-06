package camel.enigma.util;

import camel.enigma.io.KeyBoardEndpoint;
import camel.enigma.io.LightBoard;
import camel.enigma.model.Armature;
import org.apache.camel.CamelContext;
import org.apache.camel.ExchangeProperty;
import org.apache.camel.Handler;
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
    private CamelContext camelContext;

    private static boolean detailMode;

    @Handler
    public void handleControlInput(
            @ExchangeProperty(Properties.DETAIL_MODE_TOGGLE) Boolean detailModeToggle,
            @ExchangeProperty(Properties.RESET_OFFSETS) Boolean resetOffsets) {

        LightBoard lightBoard = getLightBoard();
        if (detailModeToggle != null && detailModeToggle) {
            Terminal terminal = lightBoard.getTerminal();
            terminal.puts(InfoCmp.Capability.scroll_forward);
            terminal.puts(InfoCmp.Capability.cursor_down);
            terminal.puts(InfoCmp.Capability.clear_screen);
            logger.info("\nReceived Ctrl+B, toggling detail mode...");
            toggleDetailMode();
            lightBoard.clearBuffer();
        }
        if (resetOffsets != null && resetOffsets) {
            System.out.printf("%n");
            logger.info("\nReceived Ctrl+R, resetting offsets...");
            armature.resetOffsets();
            lightBoard.clearBuffer();
        }
    }

    private LightBoard getLightBoard() {
        return camelContext.getEndpoint("keyboard", KeyBoardEndpoint.class).getLightBoard();
    }

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
