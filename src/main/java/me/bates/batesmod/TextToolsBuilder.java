package me.bates.batesmod;

import net.minecraft.text.MutableText;

import java.util.ArrayList;
import java.util.List;

public class TextToolsBuilder {
    private String input;
    private final List<String> placeholders = new ArrayList<>();
    private final List<String> replacements = new ArrayList<>();
    private final List<String> literalPlaceholders = new ArrayList<>();
    private final List<String> literalReplacements = new ArrayList<>();

    public TextToolsBuilder input(String input) {
        this.input = input;
        return this;
    }

    public TextToolsBuilder placeholder(String placeholder, String replacement) {
        this.placeholders.add(placeholder);
        this.replacements.add(replacement);
        return this;
    }

    public TextToolsBuilder literalPlaceholder(String placeholder, String replacement) {
        this.literalPlaceholders.add(placeholder);
        this.literalReplacements.add(replacement);
        return this;
    }

    public MutableText build() {
        if (input == null) {
            throw new NullPointerException("Input cannot be null");
        }

        return TextTools.deserialize(input, placeholders.toArray(String[]::new),
                replacements.toArray(String[]::new),
                literalPlaceholders.toArray(String[]::new),
                literalReplacements.toArray(String[]::new)
        );
    }
}
