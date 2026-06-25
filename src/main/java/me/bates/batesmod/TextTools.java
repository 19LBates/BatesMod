package me.bates.batesmod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

public class TextTools {

    private record Style(boolean bold, boolean italic, boolean underline, boolean strikethrough, boolean obfuscated, String[] colors) {
        public Style withBold(boolean v) {
            return new Style(v, italic, underline, strikethrough, obfuscated, cloneColors());
        }

        public Style withItalic(boolean v) {
            return new Style(bold, v, underline, strikethrough, obfuscated, cloneColors());
        }

        public Style withUnderline(boolean v) {
            return new Style(bold, italic, v, strikethrough, obfuscated, cloneColors());
        }

        public Style withStrikethrough(boolean v) {
            return new Style(bold, italic, underline, v, obfuscated, cloneColors());
        }

        public Style withObfuscated(boolean v) {
            return new Style(bold, italic, underline, strikethrough, v, cloneColors());
        }

        public Style withColors(String[] c) {
            String[] newColors = c == null ? null : c.clone();
            return new Style(bold, italic, underline, strikethrough, obfuscated, newColors);
        }

        private String[] cloneColors() {
            return colors == null ? null : colors.clone();
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

        stack.push(new Style(false, false, false, false, false, null));

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
                int end = s.indexOf('>', i + 1);
                if (end == -1) break;
                String tag = s.substring(i + 1, end);

                Style current = stack.peek();

                //Open tags
                if (tag.startsWith("color:")) {
                    stack.push(Objects.requireNonNull(current).withColors(new String[]{tag.substring(6)}));
                } else if (tag.startsWith("gradient:")) {
                    stack.push(Objects.requireNonNull(current).withColors(tag.substring(9).split(":")));
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
                } else if (tag.equals("black") || tag.equals("0")) {
                    stack.push(Objects.requireNonNull(current).withColors(new String[]{"000000"}));
                } else if (tag.equals("dark_blue") || tag.equals("1")) {
                    stack.push(Objects.requireNonNull(current).withColors(new String[]{"0000AA"}));
                } else if (tag.equals("dark_green") || tag.equals("2")) {
                    stack.push(Objects.requireNonNull(current).withColors(new String[]{"00AA00"}));
                } else if (tag.equals("dark_aqua") || tag.equals("3")) {
                    stack.push(Objects.requireNonNull(current).withColors(new String[]{"00AAAA"}));
                } else if (tag.equals("dark_red") || tag.equals("4")) {
                    stack.push(Objects.requireNonNull(current).withColors(new String[]{"AA0000"}));
                } else if (tag.equals("dark_purple") || tag.equals("5")) {
                    stack.push(Objects.requireNonNull(current).withColors(new String[]{"AA00AA"}));
                } else if (tag.equals("gold") || tag.equals("6")) {
                    stack.push(Objects.requireNonNull(current).withColors(new String[]{"FFAA00"}));
                } else if (tag.equals("gray") || tag.equals("7")) {
                    stack.push(Objects.requireNonNull(current).withColors(new String[]{"AAAAAA"}));
                } else if (tag.equals("dark_gray") || tag.equals("8")) {
                    stack.push(Objects.requireNonNull(current).withColors(new String[]{"555555"}));
                } else if (tag.equals("blue") || tag.equals("9")) {
                    stack.push(Objects.requireNonNull(current).withColors(new String[]{"5555FF"}));
                } else if (tag.equals("green") || tag.equals("a")) {
                    stack.push(Objects.requireNonNull(current).withColors(new String[]{"55FF55"}));
                } else if (tag.equals("aqua") || tag.equals("b")) {
                    stack.push(Objects.requireNonNull(current).withColors(new String[]{"55FFFF"}));
                } else if (tag.equals("red") || tag.equals("c")) {
                    stack.push(Objects.requireNonNull(current).withColors(new String[]{"FF5555"}));
                } else if (tag.equals("light_purple") || tag.equals("d")) {
                    stack.push(Objects.requireNonNull(current).withColors(new String[]{"FF55FF"}));
                } else if (tag.equals("yellow") || tag.equals("e")) {
                    stack.push(Objects.requireNonNull(current).withColors(new String[]{"FF5555"}));
                } else if (tag.equals("white") || tag.equals("f")) {
                    stack.push(Objects.requireNonNull(current).withColors(new String[]{"FFFFFF"}));
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

        if (style.colors == null) {
            output = Component.literal(text);
        } else if (style.colors.length == 1) {
            output = colorComponent(text, style.colors[0]);
        } else {
            output = gradient(text, style.colors);
        }

        if (style.bold) output.withStyle(s -> s.withBold(true));
        if (style.italic) output.withStyle(s -> s.withItalic(true));
        if (style.underline) output.withStyle(s -> s.withUnderlined(true));
        if (style.strikethrough) output.withStyle(s -> s.withStrikethrough(true));
        if (style.obfuscated) output.withStyle(s -> s.withObfuscated(true));

        result.append(output);
    }

    private static MutableComponent gradient(String text, String[] colors) {
        if (text.length() < 2) {
            throw new IllegalArgumentException("Component too short! At least 2 characters needed.");
        }

        if (colors.length < 2) {
            throw new IllegalArgumentException("Too few colors! At least 2 colors needed.");
        }

        colors = colors.clone();
        for (int i = 0; i < colors.length; i++) {
            colors[i] = format(colors[i]);
        }

        int[][] rgbColors = new int[colors.length][3];
        for (int i = 0; i < colors.length; i++) {
            if (!colors[i].matches("[0-9a-fA-F]{6}")) {
                throw new IllegalArgumentException("Invalid color:" + colors[i]);
            }
            rgbColors[i][0] = Integer.parseInt(colors[i].substring(0, 2), 16);
            rgbColors[i][1] = Integer.parseInt(colors[i].substring(2, 4), 16);
            rgbColors[i][2] = Integer.parseInt(colors[i].substring(4, 6), 16);
        }

        MutableComponent output = Component.empty();
        int len = text.length();
        int numSegments = colors.length - 1;

        for (int i = 0; i < len; i++) {
            float t = i / (float) (len - 1);
            int segIndex = Math.min((int) (t * numSegments), numSegments - 1);
            float localT = t * numSegments - segIndex;

            int r = lerp(rgbColors[segIndex][0], rgbColors[segIndex + 1][0], localT);
            int g = lerp(rgbColors[segIndex][1], rgbColors[segIndex + 1][1], localT);
            int b = lerp(rgbColors[segIndex][2], rgbColors[segIndex + 1][2], localT);

            int colorInt = (r << 16) | (g << 8) | b;
            output.append(Component.literal(String.valueOf(text.charAt(i))).withStyle(style -> style.withColor(colorInt)));
        }

        return output;
    }

    private static MutableComponent colorComponent(String text, String color) {
        if (text.isEmpty()) {
            return Component.empty();
        }

        color = format(color);
        if (!color.matches("[0-9a-fA-F]{6}")) {
            throw new IllegalArgumentException("Invalid color: " + color);
        }

        int colorInt = Integer.parseInt(color, 16);
        return Component.literal(text).withStyle(style -> style.withColor(colorInt));
    }

    private static int lerp(int num1, int num2, float t) {
        return (int) (num1 + (num2 - num1) * t);
    }

    private static String format(String s) {
        if (s.contains("#")) {
            s = s.substring(1);
        }
        return s.toLowerCase();
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
