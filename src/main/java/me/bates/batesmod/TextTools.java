package me.bates.batesmod;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

public class TextTools {

    private record Style(boolean bold, boolean italic, boolean underline, boolean strikethrough, boolean obfuscated, String[] colors) {
    }

    private static Style copy(Style s) {
        return new Style(s.bold, s.italic, s.underline, s.strikethrough, s.obfuscated, s.colors);
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
                    Style next = copy(Objects.requireNonNull(current));
                    next = new Style(next.bold, next.italic, next.underline, next.strikethrough, next.obfuscated, new String[]{tag.substring(6)});
                    stack.push(next);
                } else if (tag.startsWith("gradient:")) {
                    Style next = copy(Objects.requireNonNull(current));
                    String[] colors = tag.substring(9).split(":");
                    next = new Style(next.bold, next.italic, next.underline, next.strikethrough, next.obfuscated, colors);
                    stack.push(next);
                } else if (tag.equals("bold")) {
                    Style next = copy(Objects.requireNonNull(current));
                    next = new Style(true, next.italic, next.underline, next.strikethrough, next.obfuscated, next.colors);
                    stack.push(next);
                } else if (tag.equals("italic")) {
                    Style next = copy(Objects.requireNonNull(current));
                    next = new Style(next.bold, true, next.underline, next.strikethrough, next.obfuscated, next.colors);
                    stack.push(next);
                } else if (tag.equals("underline")) {
                    Style next = copy(Objects.requireNonNull(current));
                    next = new Style(next.bold, next.italic, true, next.strikethrough, next.obfuscated, next.colors);
                    stack.push(next);
                } else if (tag.equals("strikethrough")) {
                    Style next = copy(Objects.requireNonNull(current));
                    next = new Style(next.bold, next.italic, next.underline, true, next.obfuscated, next.colors);
                    stack.push(next);
                } else if (tag.equals("obfuscated")) {
                    Style next = copy(Objects.requireNonNull(current));
                    next = new Style(next.bold, next.italic, next.underline, next.strikethrough, true, next.colors);
                    stack.push(next);
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

            String curColor = String.format("%02x%02x%02x", r, g, b);

            output = output.copy().append(colorText(text.substring(i, i + 1), curColor));
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
