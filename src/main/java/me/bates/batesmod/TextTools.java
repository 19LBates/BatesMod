package me.bates.batesmod;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.*;

/**
 * {@code TextTools} is a utility class for deserializing
 * input markup code into a formatted {@link MutableComponent}, using the {@code deserialize} method.
 * Similar to MiniMessage formatting, this class accepts input in the form of tags such as
 * {@code <bold>text</bold>} and {@code <color:#abcdef>text</color>}.
 *
 * <p>The builder is exposed through the {@link TextTools#builder()} method.
 *
 * @author Lucas Bates
 */
public class TextTools {

    private static final int HEX_RADIX = 16;

    //For bit-shifting
    private static final int RED_SHIFT = 16;
    private static final int GREEN_SHIFT = 8;
    private static final int EIGHT_BIT_MASK = 0xFF;

    public static InputRequired builder() {
        return new Builder();
    }

    /**
     * The initial state of the {@link Builder}.
     * The {@link InputRequired#input} method is mandatory.
     */
    public interface InputRequired {
        InputReceived input(String input);
    }

    /**
     * The state of {@link Builder} after receiving an input.
     */
    public interface InputReceived {
        InputReceived placeholder(String placeholder, String replacement);

        InputReceived placeholder(String placeholder, String replacement, boolean literal);

        MutableComponent build();
    }

    /**
     * The {@code TextTools.Builder} class is a builder for {@link TextTools}.
     * It provides a more readable and less error-prone way of
     * using {@link TextTools}, helping to prevent common errors such as mismatches
     * between the number of placeholders and the number of replacements.
     */
    private static final class Builder implements InputRequired, InputReceived {

        private String input;

        private final List<String> placeholders = new ArrayList<>();
        private final List<String> replacements = new ArrayList<>();
        private final List<String> literalPlaceholders = new ArrayList<>();
        private final List<String> literalReplacements = new ArrayList<>();

        @Override
        public InputReceived input(String input) {
            this.input = Objects.requireNonNull(input, "Input cannot be null");
            return this;
        }

        @Override
        public InputReceived placeholder(String placeholder, String replacement) {
            placeholders.add(placeholder);
            replacements.add(replacement);
            return this;
        }

        @Override
        public InputReceived placeholder(String placeholder, String replacement, boolean literal) {
            if (literal) {
                literalPlaceholders.add(placeholder);
                literalReplacements.add(replacement);
                return this;
            }
            placeholders.add(placeholder);
            replacements.add(replacement);
            return this;
        }

        @Override
        public MutableComponent build() {
            return TextTools.deserialize(
                    input,
                    placeholders.toArray(String[]::new),
                    replacements.toArray(String[]::new),
                    literalPlaceholders.toArray(String[]::new),
                    literalReplacements.toArray(String[]::new)
            );
        }

    }

    @FunctionalInterface
    private interface Tag {
        Style apply(String argument, Style current);
    }

    private interface Color {
    }

    private record SolidColor(int rgb) implements Color {
    }

    private record Gradient(int[] rgb) implements Color {
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

    private record Segment(String text, Style style) {
    }

    private static MutableComponent deserialize(String s, String[] placeholders, String[] replacements, String[] literalPlaceholders, String[] literalReplacements) {
        s = applyPlaceholders(s, placeholders, replacements, false);
        s = applyPlaceholders(s, literalPlaceholders, literalReplacements, true);

        Deque<Style> stack = new ArrayDeque<>();
        StringBuilder buffer = new StringBuilder();
        List<Segment> segments = new ArrayList<>();

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
                flush(stack, buffer, segments);

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
                if ((tagString.startsWith("/") && stack.size() > 1)) {
                    //Closing tags
                    //Limitation: currently closes the previous tag, no matter what the contents of the closing tag is
                    //Example: <bold>Bold</literally_anything> Not Bold
                    stack.pop();

                } else {
                    int colonIndex = tagString.indexOf(':');
                    boolean hasColon = colonIndex != -1;

                    String tagName = hasColon ? tagString.substring(0, colonIndex) : tagString;
                    String tagArgs = hasColon ? tagString.substring(colonIndex + 1) : "";

                    Tag tag = Definitions.TAGS.get(tagName);
                    if (tag != null) stack.push(tag.apply(tagArgs, current));
                }


                //Continue after the end of the tag
                i = end + 1;

            } else {
                buffer.append(c);
                i++;
            }
        }

        flush(stack, buffer, segments);
        return render(segments.toArray(Segment[]::new));
    }

    private static void flush(Deque<Style> stack, StringBuilder buffer, List<Segment> segments) {
        String text = buffer.toString();
        buffer.setLength(0);
        Style style = stack.peek();
        segments.add(new Segment(text, style));
    }

    private static MutableComponent render(Segment[] segments) {
        MutableComponent output = Component.literal("");
        for (int i = 0; i < segments.length; i++) {
            Segment segment = segments[i];

            if (segment.style.color instanceof Gradient gradient) {
                //Keep going until a segment is found without the same gradient, or until end is reached
                int firstSegmentIndex = i;
                int totalLength = segment.text.length();

                while (i + 1 < segments.length) {
                    Segment next = segments[i + 1];
                    if (next.style.color != gradient) break;
                    totalLength += next.text.length();
                    i++;
                }

                int currentStartIndex = 0;

                for (int j = firstSegmentIndex; j <= i; j++) {
                    Segment gradientSegment = segments[j];
                    output.append(renderSegment(gradientSegment, currentStartIndex, totalLength));
                    currentStartIndex += gradientSegment.text.length();
                }

                continue;
            }

            output.append(renderSegment(segment));
        }
        return output;
    }

    private static MutableComponent renderSegment(Segment segment) {
        //startIndex and totalLength are only used for gradients
        return renderSegment(segment, 0, 0);
    }

    private static MutableComponent renderSegment(Segment segment, int startIndex, int totalLength) {
        String text = segment.text;
        Style style = segment.style;
        MutableComponent output;

        output = switch (style.color) {
            case SolidColor(int rgb) -> Component.literal(text).withColor(rgb);
            case Gradient(int[] rgb) -> generateGradient(text, rgb, startIndex, totalLength);
            case null -> Component.literal(text);
            default -> throw new IllegalStateException("Unexpected TextTools.Style: " + style.color);
        };

        output.withStyle(s -> {
            if (style.bold) s = s.withBold(true);
            if (style.italic) s = s.withItalic(true);
            if (style.underline) s = s.withUnderlined(true);
            if (style.strikethrough) s = s.withStrikethrough(true);
            if (style.obfuscated) s = s.withObfuscated(true);
            if (style.copyable) s = s.withClickEvent(new ClickEvent.CopyToClipboard(text));
            return s;
        });

        return output;
    }

    private static MutableComponent generateGradient(String text, int[] colors, int startIndex, int totalLength) {
        if (totalLength < 2) {
            throw new IllegalArgumentException("Component too short! At least 2 characters needed.");
        }

        if (colors.length < 2) {
            throw new IllegalArgumentException("Too few colors! At least 2 colors needed.");
        }

        MutableComponent output = Component.empty();
        int numSegments = colors.length - 1;

        for (int i = 0; i < text.length(); i++) {
            int overallIndex = i + startIndex;
            float t = overallIndex / (float) (totalLength - 1);
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

    private static final class Definitions {

        //Minecraft default colors
        private static final int BLACK = hexStringToInt("000000");
        private static final int DARK_BLUE = hexStringToInt("0000AA");
        private static final int DARK_GREEN = hexStringToInt("00AA00");
        private static final int DARK_AQUA = hexStringToInt("00AAAA");
        private static final int DARK_RED = hexStringToInt("AA0000");
        private static final int DARK_PURPLE = hexStringToInt("AA00AA");
        private static final int GOLD = hexStringToInt("FFAA00");
        private static final int GRAY = hexStringToInt("AAAAAA");
        private static final int DARK_GRAY = hexStringToInt("555555");
        private static final int BLUE = hexStringToInt("5555FF");
        private static final int GREEN = hexStringToInt("55FF55");
        private static final int AQUA = hexStringToInt("55FFFF");
        private static final int RED = hexStringToInt("FF5555");
        private static final int LIGHT_PURPLE = hexStringToInt("FF55FF");
        private static final int YELLOW = hexStringToInt("FFFF55");
        private static final int WHITE = hexStringToInt("FFFFFF");

        private static final Tag COLOR_TAG = (argument, current) -> {
            int color = hexStringToInt(argument);
            return current.withColor(new SolidColor(color));
        };

        private static final Tag LERP_TAG = (argument, current) -> {
            //First two values are colors (hence limit of 2 for color array); third is float for linear interpolation
            String[] split = argument.split(":");
            int[] colors = Arrays.stream(split).limit(2).mapToInt(TextTools::hexStringToInt).toArray();
            float lerpAmount = Float.parseFloat(split[2]);
            int color = lerp(colors[0], colors[1], lerpAmount);
            return current.withColor(new SolidColor(color));
        };

        private static final Tag GRADIENT_TAG = (argument, current) -> {
            int[] colors = Arrays.stream(argument.split(":")).mapToInt(TextTools::hexStringToInt).toArray();
            return current.withColor(new Gradient(colors));
        };

        private static final Tag BOLD_TAG = (_, current) -> current.withBold(true);
        private static final Tag ITALIC_TAG = (_, current) -> current.withItalic(true);
        private static final Tag UNDERLINE_TAG = (_, current) -> current.withUnderline(true);
        private static final Tag STRIKETHROUGH_TAG = (_, current) -> current.withStrikethrough(true);
        private static final Tag OBFUSCATED_TAG = (_, current) -> current.withObfuscated(true);
        private static final Tag COPYABLE_TAG = (_, current) -> current.withCopyable(true);

        private static final Tag BLACK_TAG = (_, current) -> current.withColor(new SolidColor(BLACK));
        private static final Tag DARK_BLUE_TAG = (_, current) -> current.withColor(new SolidColor(DARK_BLUE));
        private static final Tag DARK_GREEN_TAG = (_, current) -> current.withColor(new SolidColor(DARK_GREEN));
        private static final Tag DARK_AQUA_TAG = (_, current) -> current.withColor(new SolidColor(DARK_AQUA));
        private static final Tag DARK_RED_TAG = (_, current) -> current.withColor(new SolidColor(DARK_RED));
        private static final Tag DARK_PURPLE_TAG = (_, current) -> current.withColor(new SolidColor(DARK_PURPLE));
        private static final Tag GOLD_TAG = (_, current) -> current.withColor(new SolidColor(GOLD));
        private static final Tag GRAY_TAG = (_, current) -> current.withColor(new SolidColor(GRAY));
        private static final Tag DARK_GRAY_TAG = (_, current) -> current.withColor(new SolidColor(DARK_GRAY));
        private static final Tag BLUE_TAG = (_, current) -> current.withColor(new SolidColor(BLUE));
        private static final Tag GREEN_TAG = (_, current) -> current.withColor(new SolidColor(GREEN));
        private static final Tag AQUA_TAG = (_, current) -> current.withColor(new SolidColor(AQUA));
        private static final Tag RED_TAG = (_, current) -> current.withColor(new SolidColor(RED));
        private static final Tag LIGHT_PURPLE_TAG = (_, current) -> current.withColor(new SolidColor(LIGHT_PURPLE));
        private static final Tag YELLOW_TAG = (_, current) -> current.withColor(new SolidColor(YELLOW));
        private static final Tag WHITE_TAG = (_, current) -> current.withColor(new SolidColor(WHITE));

        private static final Map<String, Tag> TAGS = Map.ofEntries(
                Map.entry("color", COLOR_TAG),
                Map.entry("lerp", LERP_TAG),
                Map.entry("gradient", GRADIENT_TAG),
                Map.entry("bold", BOLD_TAG), Map.entry("l", BOLD_TAG),
                Map.entry("italic", ITALIC_TAG), Map.entry("o", ITALIC_TAG),
                Map.entry("underline", UNDERLINE_TAG), Map.entry("n", UNDERLINE_TAG),
                Map.entry("strikethrough", STRIKETHROUGH_TAG), Map.entry("m", STRIKETHROUGH_TAG),
                Map.entry("obfuscated", OBFUSCATED_TAG), Map.entry("k", OBFUSCATED_TAG),
                Map.entry("copyable", COPYABLE_TAG), Map.entry("copy", COPYABLE_TAG),

                Map.entry("black", BLACK_TAG), Map.entry("0", BLACK_TAG),
                Map.entry("dark_blue", DARK_BLUE_TAG), Map.entry("1", DARK_BLUE_TAG),
                Map.entry("dark_green", DARK_GREEN_TAG), Map.entry("2", DARK_GREEN_TAG),
                Map.entry("dark_aqua", DARK_AQUA_TAG), Map.entry("3", DARK_AQUA_TAG),
                Map.entry("dark_red", DARK_RED_TAG), Map.entry("4", DARK_RED_TAG),
                Map.entry("dark_purple", DARK_PURPLE_TAG), Map.entry("5", DARK_PURPLE_TAG),
                Map.entry("gold", GOLD_TAG), Map.entry("6", GOLD_TAG),
                Map.entry("gray", GRAY_TAG), Map.entry("7", GRAY_TAG),
                Map.entry("dark_gray", DARK_GRAY_TAG), Map.entry("8", DARK_GRAY_TAG),
                Map.entry("blue", BLUE_TAG), Map.entry("9", BLUE_TAG),
                Map.entry("green", GREEN_TAG), Map.entry("a", GREEN_TAG),
                Map.entry("aqua", AQUA_TAG), Map.entry("b", AQUA_TAG),
                Map.entry("red", RED_TAG), Map.entry("c", RED_TAG),
                Map.entry("light_purple", LIGHT_PURPLE_TAG), Map.entry("d", LIGHT_PURPLE_TAG),
                Map.entry("yellow", YELLOW_TAG), Map.entry("e", YELLOW_TAG),
                Map.entry("white", WHITE_TAG), Map.entry("f", WHITE_TAG)
        );
    }
}
