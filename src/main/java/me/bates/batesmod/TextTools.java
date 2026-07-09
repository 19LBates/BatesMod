package me.bates.batesmod;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.*;

public class TextTools {

    private static final int HEX = 16;
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

    private interface Color {
    }

    record SolidColor(int rgb) implements Color {
    }

    record Gradient(int[] rgb) implements Color {
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

                String tag = s.substring(i + 1, end);

                Style current = stack.peek();

                //Open tags
                String predefined_color = PREDEFINED_COLORS.get(tag);
                if (predefined_color != null) {
                    stack.push(Objects.requireNonNull(current).withColor(new SolidColor(hexStringToInt(predefined_color))));

                } else if (tag.startsWith("color:")) {
                    int color = hexStringToInt(tag.substring(6));
                    stack.push(Objects.requireNonNull(current).withColor(new SolidColor(color)));

                } else if (tag.startsWith("lerp:")) {
                    //First two values are colors, third is amount to lerp
                    String[] split = tag.substring(5).split(":");
                    int[] colors = Arrays.stream(split).limit(2).mapToInt(TextTools::hexStringToInt).toArray();
                    float lerpAmount = Float.parseFloat(split[2]);
                    int color = lerp(colors[0], colors[1], lerpAmount);
                    stack.push(Objects.requireNonNull(current).withColor(new SolidColor(color)));

                } else if (tag.startsWith("gradient:")) {
                    int[] colors = Arrays.stream(tag.substring(9).split(":")).mapToInt(TextTools::hexStringToInt).toArray();
                    stack.push(Objects.requireNonNull(current).withColor(new Gradient(colors)));

                } else if (tag.equals("bold") || tag.equals("l")) {
                    stack.push(Objects.requireNonNull(current).withBold(true));

                } else if (tag.equals("italic") || tag.equals("o") || tag.equals("i")) {
                    stack.push(Objects.requireNonNull(current).withItalic(true));

                } else if (tag.equals("underline") || tag.equals("n") || tag.equals("u")) {
                    stack.push(Objects.requireNonNull(current).withUnderline(true));

                } else if (tag.equals("strikethrough") || tag.equals("m") || tag.equals("s")) {
                    stack.push(Objects.requireNonNull(current).withStrikethrough(true));

                } else if (tag.equals("obfuscated") || tag.equals("k")) {
                    stack.push(Objects.requireNonNull(current).withObfuscated(true));

                } else if (tag.equals("copyable") || tag.equals("copy")) {
                    stack.push(Objects.requireNonNull(current).withCopyable(true));
                }

                //Close tags
                else if ((tag.startsWith("/") && stack.size() > 1)) {
                    stack.pop();
                }

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
            output = colorComponent(text, ((SolidColor) style.color).rgb);
        } else {
            output = generateGradient(text, ((Gradient) style.color).rgb);
        }

        output.withStyle(s -> {
            if (style.bold) s.withBold(true);
            if (style.italic) s.withItalic(true);
            if (style.underline) s.withUnderlined(true);
            if (style.strikethrough) s.withStrikethrough(true);
            if (style.obfuscated) s.withObfuscated(true);
            if (style.copyable) s.withClickEvent(new ClickEvent.CopyToClipboard(text));
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

    private static MutableComponent colorComponent(String text, int color) {
        if (text.isEmpty()) return Component.empty();
        return Component.literal(text).withStyle(style -> style.withColor(color));
    }

    private static int lerp(int color1, int color2, float t) {
        int r1 = (color1 >> 16) & EIGHT_BIT_MASK;
        int g1 = (color1 >> 8) & EIGHT_BIT_MASK;
        int b1 = color1 & EIGHT_BIT_MASK;

        int r2 = (color2 >> 16) & EIGHT_BIT_MASK;
        int g2 = (color2 >> 8) & EIGHT_BIT_MASK;
        int b2 = color2 & EIGHT_BIT_MASK;

        int r = (int) (r1 + (r2 - r1) * t);
        int g = (int) (g1 + (g2 - g1) * t);
        int b = (int) (b1 + (b2 - b1) * t);

        return (r << 16) | (g << 8) | b;
    }

    private static int hexStringToInt(String s) {
        if (s.contains("#")) {
            s = s.substring(1);
        }
        return Integer.parseInt(s, HEX);
    }

    private static String applyPlaceholders(String s, String[] placeholders, String[] replacements, boolean literal) {
        String out = s;

        if (placeholders.length != replacements.length) {
            throw new IllegalArgumentException("Placeholders and replacement arrays don't match!");
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
