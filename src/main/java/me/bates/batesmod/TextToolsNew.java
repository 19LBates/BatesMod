package me.bates.batesmod;

import org.jspecify.annotations.NonNull;

import java.util.*;

public class TextToolsNew {

    private static class Lexer {

        private enum TokenType {
            OpenTagBracket,
            CloseTagBracket,
            Slash,
            Colon,
            Identifier,
            Text,
            EOF
        }

        private static final Map<Character, TokenType> tokenCharacterMap = Map.of(
                '<', TokenType.OpenTagBracket,
                '>', TokenType.CloseTagBracket,
                '/', TokenType.Slash,
                ':', TokenType.Colon
        );

        private record Token(String value, TokenType type) {
            @Override
            public @NonNull String toString() {
                return type.toString() + "(\"" + value + "\")";
            }
        }

        private static Token[] tokenize(String src) {
            List<Token> tokens = new ArrayList<>();
            boolean inTag = false;

            int i = 0;

            while (i < src.length()) {
                char c = src.charAt(i);

                if (c == '\\') {
                    //Skip char (add next char as Text)
                    tokens.add(new Token(src.substring(i + 1, i + 2), TokenType.Text));
                    i += 2;
                    continue;
                }

                TokenType type = tokenCharacterMap.get(c);
                if (type != null) {
                    tokens.add(new Token(String.valueOf(c), type));
                    if (type == TokenType.OpenTagBracket) inTag = true;
                    if (type == TokenType.CloseTagBracket) inTag = false;
                    i++;

                } else {
                    int start = i;

                    while (i < src.length() && !tokenCharacterMap.containsKey(src.charAt(i))) {
                        i++;
                    }

                    tokens.add(new Token(src.substring(start, i), inTag ? TokenType.Identifier : TokenType.Text));
                }
            }

            tokens.add(new Token("End Of Markup", TokenType.EOF));
            return tokens.toArray(Token[]::new);
        }
    }

    private static class AST {
        private enum NodeType {
            Markup,
            Tag,
            TextLiteral
        }

        private static class Statement {
            NodeType kind;

            public Statement(NodeType kind) {
                this.kind = kind;
            }
        }

        private static class Markup extends Statement {
            Statement[] content;

            public Markup(Statement[] content) {
                super(NodeType.Markup);
                this.content = content;
            }

            @Override
            public String toString() {
                return "Markup" + Arrays.toString(content);
            }
        }

        private static class Tag extends Statement {
            String name;
            String[] args;
            Statement[] body;

            public Tag(String name, String[] args, Statement[] body) {
                super(NodeType.Tag);
                this.name = name;
                this.args = args;
                this.body = body;
            }

            @Override
            public String toString() {
                return "Tag " + name + " Args: " + Arrays.toString(args) + " Body: " + Arrays.toString(body);
            }
        }

        private static class TextLiteral extends Statement {
            String value;

            public TextLiteral(String value) {
                super(NodeType.TextLiteral);
                this.value = value;
            }

            @Override
            public String toString() {
                return "TextLiteral: " + value;
            }
        }
    }

    private static class Parser {
        private static Lexer.Token[] tokens;
        private static int curIndex = 0;

        private static AST.Markup parse(String src) {
            tokens = Lexer.tokenize(src);
            List<AST.Statement> statements = new ArrayList<>();

            while (!isEnd()) {
                statements.add(parseStatement());
            }

            return new AST.Markup(statements.toArray(AST.Statement[]::new));
        }

        private static AST.Statement parseStatement() {
            Lexer.Token token = current();
            return switch (token.type()) {
                case Text -> parseText();
                case OpenTagBracket -> parseTag();
                default -> throw new IllegalStateException("Unexpected token: " + token);
            };
        }

        private static AST.Statement parseText() {
            Lexer.Token token = eat();
            return new AST.TextLiteral(token.value);
        }

        private static AST.Statement parseTag() {
            expect(Lexer.TokenType.OpenTagBracket);
            String tagName = expect(Lexer.TokenType.Identifier).value;
            List<String> tagArgs = new ArrayList<>();

            while (!isEnd() && current().type != Lexer.TokenType.CloseTagBracket) {
                expect(Lexer.TokenType.Colon);
                tagArgs.add(expect(Lexer.TokenType.Identifier).value);
            }

            expect(Lexer.TokenType.CloseTagBracket);

            List<AST.Statement> children = new ArrayList<>();

            while (!isEnd() && !isClosingTag()) {
                children.add(parseStatement());
            }

            parseClosingTag(tagName);

            return new AST.Tag(tagName, tagArgs.toArray(String[]::new), children.toArray(AST.Statement[]::new));
        }

        private static boolean isEnd() {
            return current().type == Lexer.TokenType.EOF;
        }

        private static boolean isClosingTag() {
            return !isEnd()
                    && current().type == Lexer.TokenType.OpenTagBracket
                    && curIndex + 1 < tokens.length
                    && peekNext().type == Lexer.TokenType.Slash;
        }

        private static void parseClosingTag(String expectedName) {
            expect(Lexer.TokenType.OpenTagBracket);
            expect(Lexer.TokenType.Slash);

            String actualName = expect(Lexer.TokenType.Identifier).value;

            if (!expectedName.equals(actualName)) {
                throw new IllegalStateException("Expected </" + expectedName + "> but got </" + actualName + ">");
            }

            expect(Lexer.TokenType.CloseTagBracket);
        }

        private static Lexer.Token current() {
            return tokens[curIndex];
        }

        private static Lexer.Token peekNext() {
            return tokens[curIndex + 1];
        }

        private static Lexer.Token eat() {
            return tokens[curIndex++];
        }

        private static Lexer.Token expect(Lexer.TokenType expectedType) {
            if (isEnd()) {
                throw new IllegalStateException("Expected " + expectedType + " but reached markup EOF");
            }

            Lexer.Token token = eat();

            if (token.type != expectedType) {
                throw new IllegalStateException("Expected " + expectedType + " but got " + token.type);
            }

            return token;
        }
    }

    void main() {
        Scanner scanner = new Scanner(System.in);
        System.out.println(Parser.parse(scanner.nextLine()));
    }

}