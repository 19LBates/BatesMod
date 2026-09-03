package me.bates.batesmod;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.*;

public class TextTools {

    private static final int HEX_RADIX = 16;

    //For bit-shifting
    private static final int RED_SHIFT = 16;
    private static final int GREEN_SHIFT = 8;
    private static final int EIGHT_BIT_MASK = 0xFF;

    private static final Map<String, String> PREDEFINED_COLORS = Map.ofEntries(
            Map.entry("black", "000000"),
            Map.entry("0", "000000"),

            Map.entry("dark_blue", "0000AA"),
            Map.entry("1", "0000AA"),

            Map.entry("dark_green", "00AA00"),
            Map.entry("2", "00AA00"),

            Map.entry("dark_aqua", "00AAAA"),
            Map.entry("3", "00AAAA"),

            Map.entry("dark_red", "AA0000"),
            Map.entry("4", "AA0000"),

            Map.entry("dark_purple", "AA00AA"),
            Map.entry("5", "AA00AA"),

            Map.entry("gold", "FFAA00"),
            Map.entry("6", "FFAA00"),

            Map.entry("gray", "AAAAAA"),
            Map.entry("7", "AAAAAA"),

            Map.entry("dark_gray", "555555"),
            Map.entry("8", "555555"),

            Map.entry("blue", "5555FF"),
            Map.entry("9", "5555FF"),

            Map.entry("green", "55FF55"),
            Map.entry("a", "55FF55"),

            Map.entry("aqua", "55FFFF"),
            Map.entry("b", "55FFFF"),

            Map.entry("red", "FF5555"),
            Map.entry("c", "FF5555"),

            Map.entry("light_purple", "FF55FF"),
            Map.entry("d", "FF55FF"),

            Map.entry("yellow", "FFFF55"),
            Map.entry("e", "FFFF55"),

            Map.entry("white", "FFFFFF"),
            Map.entry("f", "FFFFFF")
    );

    private static final Tag BOLD = new BoldTag();
    private static final Tag ITALIC = new ItalicTag();
    private static final Tag UNDERLINE = new UnderlineTag();
    private static final Tag STRIKETHROUGH = new StrikethroughTag();
    private static final Tag OBFUSCATED = new ObfuscatedTag();
    private static final Tag COPYABLE = new CopyableTag();

    private static final Map<String, Tag> TAGS = Map.ofEntries(
            Map.entry("color", new ColorTag()),
            Map.entry("lerp", new LerpTag()),
            Map.entry("gradient", new GradientTag()),

            Map.entry("bold", BOLD),
            Map.entry("l", BOLD),

            Map.entry("italic", ITALIC),
            Map.entry("o", ITALIC),

            Map.entry("underline", UNDERLINE),
            Map.entry("n", UNDERLINE),

            Map.entry("strikethrough", STRIKETHROUGH),
            Map.entry("m", STRIKETHROUGH),

            Map.entry("obfuscated", OBFUSCATED),
            Map.entry("k", OBFUSCATED),

            Map.entry("copyable", COPYABLE),
            Map.entry("copy", COPYABLE)
    );

    private interface Color {
    }

    private record SolidColor(int rgb) implements Color {
    }

    private record Gradient(int[] rgb) implements Color {
    }

    private interface Tag {
        Style apply(String argument, Style current);
    }

    private static class ColorTag implements Tag {
        @Override
        public Style apply(String argument, Style current) {
            int color = hexStringToInt(argument);
            return Objects.requireNonNull(current).withColor(new SolidColor(color));
        }
    }

    private static class LerpTag implements Tag {
        @Override
        public Style apply(String argument, Style current) {
            //First two values are colors (hence limit of 2 for color array); third is float for linear interpolation
            String[] split = argument.split(":");
            int[] colors = Arrays.stream(split).limit(2).mapToInt(TextTools::hexStringToInt).toArray();
            float lerpAmount = Float.parseFloat(split[2]);
            int color = lerp(colors[0], colors[1], lerpAmount);
            return Objects.requireNonNull(current).withColor(new SolidColor(color));
        }
    }

    private static class GradientTag implements Tag {
        @Override
        public Style apply(String argument, Style current) {
            int[] colors = Arrays.stream(argument.split(":")).mapToInt(TextTools::hexStringToInt).toArray();
            return Objects.requireNonNull(current).withColor(new Gradient(colors));
        }
    }

    private static class BoldTag implements Tag {
        @Override
        public Style apply(String argument, Style current) {
            return Objects.requireNonNull(current).withBold(true);
        }
    }

    private static class ItalicTag implements Tag {
        @Override
        public Style apply(String argument, Style current) {
            return Objects.requireNonNull(current).withItalic(true);
        }
    }

    private static class UnderlineTag implements Tag {
        @Override
        public Style apply(String argument, Style current) {
            return Objects.requireNonNull(current).withUnderline(true);
        }
    }

    private static class StrikethroughTag implements Tag {
        @Override
        public Style apply(String argument, Style current) {
            return Objects.requireNonNull(current).withStrikethrough(true);
        }
    }

    private static class ObfuscatedTag implements Tag {
        @Override
        public Style apply(String argument, Style current) {
            return Objects.requireNonNull(current).withObfuscated(true);
        }
    }

    private static class CopyableTag implements Tag {
        @Override
        public Style apply(String argument, Style current) {
            return Objects.requireNonNull(current).withCopyable(true);
        }
    }

    private record Style(boolean bold, boolean italic, boolean underline, boolean strikethrough, boolean obfuscated,
                         boolean copyable, Color color) {
        public Style withBold(boolean v) {
            return new Style(v, italic, underline, strikethrough, obfuscated, copyable, color);
        }

        public Style withItalic(boolean v) {
            return new Style(bold, v, underline, strikethrough, obfuscated, copyable, color);
        }

        public Style withUnderline(boolean v) {
            return new Style(bold, italic, v, strikethrough, obfuscated, copyable, color);
        }

        public Style withStrikethrough(boolean v) {
            return new Style(bold, italic, underline, v, obfuscated, copyable, color);
        }

        public Style withObfuscated(boolean v) {
            return new Style(bold, italic, underline, strikethrough, v, copyable, color);
        }

        public Style withCopyable(boolean v) {
            return new Style(bold, italic, underline, strikethrough, obfuscated, v, color);
        }

        public Style withColor(Color c) {
            return new Style(bold, italic, underline, strikethrough, obfuscated, copyable, c);
        }
    }

    public static MutableComponent deserialize(String s, String[] placeholders, String[] replacements) {
        return deserialize(applyPlaceholders(s, placeholders, replacements, false));
    }

    public static MutableComponent deserialize(String s, String[] placeholders, String[] replacements, String[] literalPlaceholders, String[] literalReplacements) {
        return deserialize(applyPlaceholders(s, literalPlaceholders, literalReplacements, true), placeholders, replacements);
    }

    public static MutableComponent deserialize(String s) {
        Deque<Style> stack = new ArrayDeque<>();
        StringBuilder buffer = new StringBuilder();
        MutableComponent result = Component.empty();

        stack.push(new Style(false, false, false, false, false, false, null));

        for (int i = 0; i < s.length(); ) {
            char c = s.charAt(i);
            if (c == '\\') {
                i++;
                if (i >= s.length()) {
                    throw new IllegalArgumentException("Trailing Backslash!");
                }
                buffer.append(s.charAt(i));
                i++;
                continue;
            }
            if (c == '<') {
                flush(stack, buffer, result);

                //Start searching for '>' after '<'
                int end = -1;
                for (int j = i + 1; j < s.length(); j++) {
                    if (s.charAt(j) == '>') {
                        end = j;
                        break;
                    }
                }
                if (end == -1) break;

                String tagString = s.substring(i + 1, end);

                Style current = stack.peek();

                //Handle tags
                String predefinedColor = PREDEFINED_COLORS.get(tagString);
                if (predefinedColor != null) {
                    stack.push(Objects.requireNonNull(current).withColor(new SolidColor(hexStringToInt(predefinedColor))));

                } else if ((tagString.startsWith("/") && stack.size() > 1)) {
                    //Closing tags
                    //Limitation: currently closes the previous tag, no matter what the contents of the closing tag is
                    //Example: <bold>Bold</literally_anything> Not Bold
                    stack.pop();

                } else {
                    int colonIndex = tagString.indexOf(':');
                    boolean hasColon = colonIndex != -1;

                    String tagName = hasColon ? tagString.substring(0, colonIndex) : tagString;
                    String tagArgs = hasColon ? tagString.substring(colonIndex + 1) : "";

                    Tag tag = TAGS.get(tagName);
                    if (tag != null) stack.push(tag.apply(tagArgs, current));
                }


                //Continue after the end of the tag
                i = end + 1;

            } else {
                buffer.append(c);
                i++;
            }
        }

        flush(stack, buffer, result);
        return result;
    }

    public static TextToolsBuilder builder() {
        return new TextToolsBuilder();
    }

    private static void flush(Deque<Style> stack, StringBuilder buffer, MutableComponent result) {
        if (buffer.isEmpty()) return;

        String text = buffer.toString();
        buffer.setLength(0);

        if (stack.isEmpty()) {
            result.append(Component.literal(text));
            return;
        }

        Style style = stack.peek();
        MutableComponent output;

        if (style.color == null) {
            output = Component.literal(text);
        } else if (style.color instanceof SolidColor) {
            output = Component.literal(text).withStyle(s -> s.withColor(((SolidColor) style.color).rgb));
        } else {
            output = generateGradient(text, ((Gradient) style.color).rgb);
        }

        output.withStyle(s -> {
            if (style.bold) s = s.withBold(true);
            if (style.italic) s = s.withItalic(true);
            if (style.underline) s = s.withUnderlined(true);
            if (style.strikethrough) s = s.withStrikethrough(true);
            if (style.obfuscated) s = s.withObfuscated(true);
            if (style.copyable) s = s.withClickEvent(new ClickEvent.CopyToClipboard(text));
            return s;
        });

        result.append(output);
    }

    private static MutableComponent generateGradient(String text, int[] colors) {
        if (text.length() < 2) {
            throw new IllegalArgumentException("Component too short! At least 2 characters needed.");
        }

        if (colors.length < 2) {
            throw new IllegalArgumentException("Too few colors! At least 2 colors needed.");
        }

        MutableComponent output = Component.empty();
        int len = text.length();
        int numSegments = colors.length - 1;

        for (int i = 0; i < len; i++) {
            float t = i / (float) (len - 1);
            int segIndex = Math.min((int) (t * numSegments), numSegments - 1);
            float localT = t * numSegments - segIndex;
            int curColor = lerp(colors[segIndex], colors[segIndex + 1], localT);
            output.append(Component.literal(String.valueOf(text.charAt(i))).withStyle(style -> style.withColor(curColor)));
        }

        return output;
    }

    private static int lerp(int color1, int color2, float t) {
        int r1 = (color1 >> RED_SHIFT) & EIGHT_BIT_MASK;
        int g1 = (color1 >> GREEN_SHIFT) & EIGHT_BIT_MASK;
        int b1 = color1 & EIGHT_BIT_MASK;

        int r2 = (color2 >> RED_SHIFT) & EIGHT_BIT_MASK;
        int g2 = (color2 >> GREEN_SHIFT) & EIGHT_BIT_MASK;
        int b2 = color2 & EIGHT_BIT_MASK;

        int r = (int) (r1 + (r2 - r1) * t);
        int g = (int) (g1 + (g2 - g1) * t);
        int b = (int) (b1 + (b2 - b1) * t);

        return (r << RED_SHIFT) | (g << GREEN_SHIFT) | b;
    }

    private static int hexStringToInt(String s) {
        if (s.contains("#")) {
            s = s.substring(1);
        }
        return Integer.parseInt(s, HEX_RADIX);
    }

    private static String applyPlaceholders(String s, String[] placeholders, String[] replacements, boolean literal) {
        String out = s;

        if (placeholders.length != replacements.length) {
            throw new IllegalArgumentException("Placeholders and replacement arrays don't match in length!");
        }

        for (int i = 0; i < placeholders.length; i++) {
            String placeholder = placeholders[i];
            String replacement = replacements[i];
            if (literal) {
                replacement = replacement.replace("<", "\\<").replace(">", "\\>");
            }
            out = out.replace("%" + placeholder + "%", replacement);
        }

        return out;
    }
}
