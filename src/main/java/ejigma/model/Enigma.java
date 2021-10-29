package ejigma.model;

import ejigma.exception.ArmatureInitException;
import ejigma.exception.ScramblerSettingException;
import ejigma.io.KeyBoard;
import ejigma.io.LightBoard;
import ejigma.model.type.*;
import ejigma.util.ScrambleResult;
import ejigma.util.TerminalProvider;
import ejigma.util.Util;
import org.jline.terminal.Terminal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;

public class Enigma {

    private final Terminal terminal;
    private LightBoard lightBoard;
    private Armature armature;
    private PlugBoard plugBoard;
    private KeyBoard keyBoard;
    private ConfigContainer configContainer;

    private List<ScramblerMounting<?, ?>> scramblerWiring;
    private String alphabetString;

    public Enigma() throws IOException {
        this(true);
    }

    public Enigma(boolean dumbTerm) throws IOException {
        this.terminal = TerminalProvider.initTerminal(dumbTerm);
    }

    public void init(ConfigContainer configContainer) throws ArmatureInitException {
        init(configContainer,
             Armature.DEFAULT_ENTRY_WHEEL_TYPE,
             Armature.DEFAULT_ROTOR_TYPES,
             Armature.DEFAULT_REFLECTOR_TYPE);
    }

    public void init(ConfigContainer configContainer,
                     EntryWheelType entryWheelType,
                     RotorType[] rotorTypes,
                     ReflectorType reflectorType) throws ArmatureInitException {

        this.setConfigContainer(configContainer);
        this.setArmature(new Armature(this, entryWheelType, rotorTypes, reflectorType));
        this.setLightBoard(new LightBoard(this));
        this.setKeyBoard(new KeyBoard(this));
        initWiring();
    }

    public void start() {
        lightBoard.display();
        keyBoard.doStart();
    }

    public ScrambleResult handle(ScrambleResult scrambleResult) {

        armature.click();

        return encrypt(scrambleResult);
    }

    public String scramble(String input) {
        return input.chars()
                .mapToObj(i -> (char) i)
                .map(c -> {
                    if (!Util.containsChar(alphabetString, c)) {
                        char upperCase = Character.toUpperCase(c);
                        if (Util.containsChar(alphabetString, upperCase)) {
                            c = upperCase;
                        }
                    }
                    return c;
                })
                .filter(c -> alphabetString.indexOf(c) != -1)
                .map(c -> {
                    armature.click();
                    c = encrypt(c);
                    return c;
                })
                .collect(Collector.of(
                        StringBuilder::new,
                        StringBuilder::append,
                        StringBuilder::append,
                        StringBuilder::toString));
    }

    public ScrambleResult encrypt(ScrambleResult current) {
        for (int i = 0; i < scramblerWiring.size(); i++) {
            ScramblerMounting<?, ?> scramblerMounting = scramblerWiring.get(i);
            current = scramblerMounting.scramble(current);
            if (i == scramblerWiring.size() - 1) {
                current.recordOutput();
            }
        }
        return current;
    }

    private char encrypt(char c) {
        for (ScramblerMounting<?, ?> scramblerMounting : scramblerWiring) {
            c = scramblerMounting.scramble(c);
        }
        return c;
    }

    public void setEntryWheel(EntryWheelType entryWheelType) throws ArmatureInitException {
        armature.setEntryWheel(entryWheelType);
        initWiring();
    }

    public void forceSetEntryWheel(EntryWheelType entryWheelType) {
        armature.forceSetEntryWheel(entryWheelType);
        initWiring();
    }

    public void setAutoEntryWheel(String alphabetString) {
        armature.setAutoEntryWheel(alphabetString);
        initWiring();
    }

    public void setReflector(ReflectorType reflectorType) throws ArmatureInitException {
        armature.setReflector(reflectorType);
        initWiring();
    }

    public void forceSetReflector(ReflectorType reflectorType) {
        armature.forceSetReflector(reflectorType);
        initWiring();
    }

    public void setAutoReflector(String alphabetString) {
        armature.setAutoReflector(alphabetString);
        initWiring();
    }

    public void setRotors(RotorType[] types) throws ArmatureInitException {
        armature.setRotors(types);
        initWiring();
    }

    public void forceSetRotors(RotorType[] types) {
        armature.forceSetRotors(types);
        initWiring();
    }

    public void setPlugBoard(PlugBoardConfig newType) throws ArmatureInitException, ScramblerSettingException {
        validateWithCurrent(newType);
        // TODO more elegant handling
        PlugBoard vPlugBoard = newType.unsafeScrambler();
        vPlugBoard.validatePlugBoard();
        forceSetPlugBoard(vPlugBoard);
        initWiring();
    }

    public void forceSetPlugBoard(PlugBoard plugBoard) {
        this.plugBoard = plugBoard;
        initWiring();
    }

    public void forceSetPlugBoard(PlugBoardConfig newType) {
        this.plugBoard = newType.freshScrambler();
        initWiring();
    }

    public void setAutoPlugBoard(String alphabetString) {
        this.plugBoard = PlugBoard.auto(alphabetString).freshScrambler();
        initWiring();
    }

    public void validateWithCurrent(PlugBoardConfig plugBoardConfig) throws ArmatureInitException {
            Armature.validateAllTypes(
                    armature.getEntryWheelType(),
                    armature.getRotorTypes(),
                    armature.getReflectorType(),
                    plugBoardConfig);
    }

    private void initWiring() {
        List<ScramblerMounting<?, ?>> wiring = new ArrayList<>(armature.getScramblerWiring());
        if (plugBoard != null) {
            wiring.add(0, new ScramblerMounting<>(plugBoard));
            wiring.add(wiring.size() - 1, new ScramblerMounting<>(plugBoard));
        }
        this.scramblerWiring = wiring;
    }

    public void resetFromCache(KeyBoard.ScramblerCache cache) {
        armature.forceSetEntryWheel(cache.getEntryWheel());
        armature.forceSetRotors(cache.getRotors());
        armature.forceSetReflector(cache.getReflector());
        forceSetPlugBoard(cache.getPlugBoard());
    }

    public void setOffsets(String offsetString) throws ScramblerSettingException {
        armature.setOffsets(offsetString);
    }

    public LightBoard getLightBoard() {
        return lightBoard;
    }

    public void setLightBoard(LightBoard lightBoard) {
        this.lightBoard = lightBoard;
    }

    public Armature getArmature() {
        return armature;
    }

    public void setArmature(Armature armature) {
        this.armature = armature;
        this.alphabetString = armature.getAlphabetString();
        initWiring();
    }

    public PlugBoard getPlugBoard() {
        return plugBoard;
    }

    public void initPlugBoard() throws ScramblerSettingException {
        this.plugBoard = new PlugBoard();
        initWiring();
    }

    public KeyBoard getKeyBoard() {
        return keyBoard;
    }

    public void setKeyBoard(KeyBoard keyBoard) {
        this.keyBoard = keyBoard;
    }

    public ConfigContainer getConfigContainer() {
        return configContainer;
    }

    public void setConfigContainer(ConfigContainer configContainer) {
        this.configContainer = configContainer;
    }

    public Terminal getTerminal() {
        return terminal;
    }
}
