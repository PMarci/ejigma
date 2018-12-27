package camel.enigma.util;

import camel.enigma.io.LightBoard;
import camel.enigma.model.Armature;
import org.apache.camel.ExchangeProperty;
import org.apache.camel.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.fusesource.jansi.Ansi.ansi;

@Component
public class SettingManager {

    private static final Logger logger = LoggerFactory.getLogger(SettingManager.class);

    @Autowired
    private Armature armature;

    @Autowired
    private LightBoard lightBoard;

    private static boolean detailMode;

    @Handler
    public void handleControlInput(
            @ExchangeProperty(Properties.DETAIL_MODE_TOGGLE) Boolean detailModeToggle,
            @ExchangeProperty(Properties.RESET_OFFSETS) Boolean resetOffsets) {
        if (detailModeToggle != null && detailModeToggle) {
            System.out.printf("%n");
            System.out.print(ansi().cursor(1,1).eraseScreen());
            logger.info("\nReceived Ctrl+B, toggling detail mode...");
            toggleDetailMode();
            lightBoard.clearBuffer();
        }
        if (resetOffsets != null && resetOffsets) {
            System.out.printf("%n");
            logger.info("\nReceived Ctrl+R, resetting offsets...");
            armature.resetOffsets();
            lightBoard.clearBuffer();
            lightBoard.updateStatus(lightBoard.createStatusStrings());
        }
    }

    private static void toggleDetailMode() {
        setDetailMode(!isDetailMode());
    }


    public static boolean isDetailMode() {
        return detailMode;
    }

    public static void setDetailMode(boolean detailMode) {
        SettingManager.detailMode = detailMode;
    }
}
