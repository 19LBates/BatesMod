package me.bates.batesmod;

import net.minecraft.network.chat.*;

import java.util.*;
import java.util.function.UnaryOperator;

/**
 * {@code TextTools} is a utility class for deserializing
 * input markup code into a formatted {@link MutableComponent}.
 * Similar to MiniMessage formatting, this class accepts input in the form of tags such as
 * {@code <bold>text</bold>} and {@code <color:#abcdef>text</color>}.
 *
 * <p>Closing tags currently close the most recently opened tag, regardless of the closing tag's name.
 * For example, {@code <bold>hello</literally_anything>world} is rendered {@code hello} in bold and {@code world} normally.
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
     * The {@code TextTools.Builder} class is the builder for {@link TextTools}.
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
        FormattingState apply(String argument, FormattingState current);
    }

    private record Gradient(int[] rgb) {
    }

    private record FormattingState(Style style, Gradient gradient) {
        public FormattingState changeStyle(UnaryOperator<Style> change) {
            return new FormattingState(change.apply(style), gradient);
        }
        public FormattingState withGradient(Gradient gradient) {
            return new FormattingState(style, gradient);
        }
    }

    private record Segment(String text, FormattingState formattingState) {
    }

    private static MutableComponent deserialize(String s, String[] placeholders, String[] replacements, String[] literalPlaceholders, String[] literalReplacements) {
        s = applyPlaceholders(s, placeholders, replacements, false);
        s = applyPlaceholders(s, literalPlaceholders, literalReplacements, true);

        Deque<FormattingState> stack = new ArrayDeque<>();
        StringBuilder buffer = new StringBuilder();
        List<Segment> segments = new ArrayList<>();

        stack.push(new FormattingState(Style.EMPTY, null));

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

                int end = findTagEnd(s, i);
                if (end == -1) break;

                String tagString = s.substring(i + 1, end);

                FormattingState current = stack.peek();

                //Handle tags
                if ((tagString.startsWith("/") && stack.size() > 1)) {
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

    private static int findTagEnd(String s, int start) {
        int nestLevel = 1;

        for (int i = start + 1; i < s.length(); i++) {
            switch (s.charAt(i)) {
                case '\\':
                    i++;
                    continue;
                case '<':
                    nestLevel++;
                    break;
                case '>':
                    nestLevel--;
                    break;
            }

            if (nestLevel == 0) {
                return i;
            }
        }

        return -1;
    }

    private static void flush(Deque<FormattingState> stack, StringBuilder buffer, List<Segment> segments) {
        if (buffer.isEmpty()) return;
        String text = buffer.toString();
        buffer.setLength(0);
        FormattingState formattingState = stack.peek();
        segments.add(new Segment(text, formattingState));
    }

    private static MutableComponent render(Segment[] segments) {
        MutableComponent output = Component.literal("");
        for (int i = 0; i < segments.length; i++) {
            Segment segment = segments[i];

            if (segment.formattingState.gradient instanceof Gradient gradient) {
                //Keep going until a segment is found without the same gradient, or until end is reached
                int firstSegmentIndex = i;
                int totalLength = segment.text.length();

                while (i + 1 < segments.length) {
                    Segment next = segments[i + 1];
                    if (next.formattingState.gradient != gradient) break;
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
        FormattingState formattingState = segment.formattingState;
        MutableComponent output;

        output = switch (formattingState.gradient) {
            case Gradient(int[] rgb) -> generateGradient(text, rgb, startIndex, totalLength);
            case null -> Component.literal(text);
        };

        output.withStyle(formattingState.style);

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
        if (s.startsWith("#")) {
            s = s.substring(1);
        }
        return Integer.parseUnsignedInt(s, HEX_RADIX);
    }

    private static String applyPlaceholders(String s, String[] placeholders, String[] replacements, boolean literal) {

        if (placeholders == null || placeholders.length == 0 || replacements == null || replacements.length == 0) return s;

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
        private static final int BLACK = 0x000000;
        private static final int DARK_BLUE = 0x0000AA;
        private static final int DARK_GREEN = 0x00AA00;
        private static final int DARK_AQUA = 0x00AAAA;
        private static final int DARK_RED = 0xAA0000;
        private static final int DARK_PURPLE = 0xAA00AA;
        private static final int GOLD = 0xFFAA00;
        private static final int GRAY = 0xAAAAAA;
        private static final int DARK_GRAY = 0x555555;
        private static final int BLUE = 0x5555FF;
        private static final int GREEN = 0x55FF55;
        private static final int AQUA = 0x55FFFF;
        private static final int RED = 0xFF5555;
        private static final int LIGHT_PURPLE = 0xFF55FF;
        private static final int YELLOW = 0xFFFF55;
        private static final int WHITE = 0xFFFFFF;

        private static final Tag COLOR_TAG = (argument, current) -> {
            int color = hexStringToInt(argument);
            return current.changeStyle(s -> s.withColor(color)).withGradient(null);
        };

        private static final Tag LERP_TAG = (argument, current) -> {
            //First two values are colors (hence limit of 2 for color array); third is float for linear interpolation
            String[] split = argument.split(":");

            if (split.length != 3) {
                throw new IllegalArgumentException("Invalid arguments for lerp: \" " + argument + "\". Lerp requires 3 arguments: <lerp:color1:color2:amount>.");
            }

            int color1 = hexStringToInt(split[0]);
            int color2 = hexStringToInt(split[1]);
            float lerpAmount = Float.parseFloat(split[2]);

            int colorResult = lerp(color1, color2, lerpAmount);
            return current.changeStyle(s -> s.withColor(colorResult)).withGradient(null);
        };

        private static final Tag GRADIENT_TAG = (argument, current) -> {
            int[] colors = Arrays.stream(argument.split(":")).mapToInt(TextTools::hexStringToInt).toArray();
            return current.withGradient(new Gradient(colors));
        };

        private static final Tag SHADOW_COLOR_TAG = (argument, current) -> {
            int color = hexStringToInt(argument);
            return current.changeStyle(s -> s.withShadowColor(color));
        };

        private static final Tag BOLD_TAG = (_, current) -> current.changeStyle(s -> s.withBold(true));
        private static final Tag ITALIC_TAG = (_, current) -> current.changeStyle(s -> s.withItalic(true));
        private static final Tag UNDERLINE_TAG = (_, current) -> current.changeStyle(s -> s.withUnderlined(true));
        private static final Tag STRIKETHROUGH_TAG = (_, current) -> current.changeStyle(s -> s.withStrikethrough(true));
        private static final Tag OBFUSCATED_TAG = (_, current) -> current.changeStyle(s -> s.withObfuscated(true));

        private static final Tag COPYABLE_TAG = (argument, current) -> {
            ClickEvent event = new ClickEvent.CopyToClipboard(deserialize(argument, null, null, null, null).getString());
            return current.changeStyle(s -> s.withClickEvent(event));
        };

        private static final Tag HOVER_SHOW_TEXT_TAG = (argument, current) -> {
            HoverEvent event = new HoverEvent.ShowText(deserialize(argument, null, null, null, null));
            return current.changeStyle(s -> s.withHoverEvent(event));
        };

        private static final Tag BLACK_TAG = (_, current) -> current.changeStyle(s -> s.withColor(BLACK)).withGradient(null);
        private static final Tag DARK_BLUE_TAG = (_, current) -> current.changeStyle(s -> s.withColor(DARK_BLUE)).withGradient(null);
        private static final Tag DARK_GREEN_TAG = (_, current) -> current.changeStyle(s -> s.withColor(DARK_GREEN)).withGradient(null);
        private static final Tag DARK_AQUA_TAG = (_, current) -> current.changeStyle(s -> s.withColor(DARK_AQUA)).withGradient(null);
        private static final Tag DARK_RED_TAG = (_, current) -> current.changeStyle(s -> s.withColor(DARK_RED)).withGradient(null);
        private static final Tag DARK_PURPLE_TAG = (_, current) -> current.changeStyle(s -> s.withColor(DARK_PURPLE)).withGradient(null);
        private static final Tag GOLD_TAG = (_, current) -> current.changeStyle(s -> s.withColor(GOLD)).withGradient(null);
        private static final Tag GRAY_TAG = (_, current) -> current.changeStyle(s -> s.withColor(GRAY)).withGradient(null);
        private static final Tag DARK_GRAY_TAG = (_, current) -> current.changeStyle(s -> s.withColor(DARK_GRAY)).withGradient(null);
        private static final Tag BLUE_TAG = (_, current) -> current.changeStyle(s -> s.withColor(BLUE)).withGradient(null);
        private static final Tag GREEN_TAG = (_, current) -> current.changeStyle(s -> s.withColor(GREEN)).withGradient(null);
        private static final Tag AQUA_TAG = (_, current) -> current.changeStyle(s -> s.withColor(AQUA)).withGradient(null);
        private static final Tag RED_TAG = (_, current) -> current.changeStyle(s -> s.withColor(RED)).withGradient(null);
        private static final Tag LIGHT_PURPLE_TAG = (_, current) -> current.changeStyle(s -> s.withColor(LIGHT_PURPLE)).withGradient(null);
        private static final Tag YELLOW_TAG = (_, current) -> current.changeStyle(s -> s.withColor(YELLOW)).withGradient(null);
        private static final Tag WHITE_TAG = (_, current) -> current.changeStyle(s -> s.withColor(WHITE)).withGradient(null);

        private static final Map<String, Tag> TAGS = Map.ofEntries(
                Map.entry("color", COLOR_TAG),
                Map.entry("lerp", LERP_TAG),
                Map.entry("gradient", GRADIENT_TAG),
                Map.entry("shadow", SHADOW_COLOR_TAG),
                Map.entry("bold", BOLD_TAG), Map.entry("l", BOLD_TAG),
                Map.entry("italic", ITALIC_TAG), Map.entry("o", ITALIC_TAG),
                Map.entry("underline", UNDERLINE_TAG), Map.entry("n", UNDERLINE_TAG),
                Map.entry("strikethrough", STRIKETHROUGH_TAG), Map.entry("m", STRIKETHROUGH_TAG),
                Map.entry("obfuscated", OBFUSCATED_TAG), Map.entry("k", OBFUSCATED_TAG),
                Map.entry("copyable", COPYABLE_TAG), Map.entry("copy", COPYABLE_TAG),
                Map.entry("hover_text", HOVER_SHOW_TEXT_TAG), Map.entry("hover", HOVER_SHOW_TEXT_TAG),

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
