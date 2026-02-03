package me.bates.batesmod;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

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

        public String[] cloneColors() {
            return colors == null ? null : colors.clone();
        }
    }

    public static MutableText deserialize(String s, String[] placeholders, String[] replacements) {
        return deserialize(applyPlaceholders(s, placeholders, replacements));
    }

    public static MutableText deserialize(String s) {
        Deque<Style> stack = new ArrayDeque<>();
        StringBuilder buffer = new StringBuilder();
        MutableText result = Text.empty();

        stack.push(new Style(false, false, false, false, false, null));

        for (int i = 0; i < s.length(); ) {
            char c = s.charAt(i);
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
                } else if (tag.equals("bold")) {
                    stack.push(Objects.requireNonNull(current).withBold(true));
                } else if (tag.equals("italic")) {
                    stack.push(Objects.requireNonNull(current).withItalic(true));
                } else if (tag.equals("underline")) {
                    stack.push(Objects.requireNonNull(current).withUnderline(true));
                } else if (tag.equals("strikethrough")) {
                    stack.push(Objects.requireNonNull(current).withStrikethrough(true));
                } else if (tag.equals("obfuscated")) {
                    stack.push(Objects.requireNonNull(current).withObfuscated(true));
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

    private static void flush(Deque<Style> stack, StringBuilder buffer, MutableText result) {
        if (buffer.isEmpty()) return;

        String text = buffer.toString();
        buffer.setLength(0);

        if (stack.isEmpty()) {
            result.append(Text.literal(text));
            return;
        }

        Style style = stack.peek();
        MutableText output;

        if (style.colors == null) {
            output = Text.literal(text);
        } else if (style.colors.length == 1) {
            output = colorText(text, style.colors[0]);
        } else {
            output = gradient(text, style.colors);
        }

        if (style.bold) output.styled(s -> s.withBold(true));
        if (style.italic) output.styled(s -> s.withItalic(true));
        if (style.underline) output.styled(s -> s.withUnderline(true));
        if (style.strikethrough) output.styled(s -> s.withStrikethrough(true));
        if (style.obfuscated) output.styled(s -> s.withObfuscated(true));

        result.append(output);
    }

    private static MutableText gradient(String text, String[] colors) {
        if (text.length() < 2) {
            throw new IllegalArgumentException("Text too short! At least 2 characters needed.");
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

        MutableText output = Text.empty();
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
            output.append(Text.literal(String.valueOf(text.charAt(i))).styled(style -> style.withColor(colorInt)));
        }

        return output;
    }

    private static MutableText colorText(String text, String color) {
        if (text.isEmpty()) {
            return Text.empty();
        }

        color = format(color);
        if (!color.matches("[0-9a-fA-F]{6}")) {
            throw new IllegalArgumentException("Invalid color: " + color);
        }

        int colorInt = Integer.parseInt(color, 16);
        return Text.literal(text).styled(style -> style.withColor(colorInt));
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

    private static String applyPlaceholders(String s, String[] placeholders, String[] replacements) {
        if (placeholders.length != replacements.length) {
            throw new IllegalArgumentException("Placeholders and replacement arrays don't match!");
        }

        for (int i = 0; i < placeholders.length; i++) {
            s = s.replace("%" + placeholders[i] + "%", replacements[i]);
        }

        return s;
    }
}
