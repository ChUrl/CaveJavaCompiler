package lexer;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Lexer;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class LexerTest {

    private Lexer initLexer(String program) {
        try {
            final Path path = Paths.get(this.getClass().getClassLoader().getResource("examplePrograms/" + program).toURI());
            final String programCode = Files.readString(path, StandardCharsets.US_ASCII);
            return new StupsLexer(CharStreams.fromString(programCode));
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }

        return null;
    }

    private static List<String> getSymbols(Lexer lex) {
        return lex.getAllTokens().stream()
                  .map(tok -> lex.getVocabulary().getSymbolicName(tok.getType()))
                  .collect(Collectors.toUnmodifiableList());
    }

    @Test
    void testEmptyFile() {
        final Lexer lex = this.initLexer("EmptyFile.stups");

        final List<String> token = LexerTest.getSymbols(lex);

        assertThat(token).isEmpty();
    }

    @Test
    void testWhitespace() {
        final Lexer lex = this.initLexer("Whitespace.stups");

        final List<String> token = LexerTest.getSymbols(lex);

        assertThat(token).containsExactly("IDENTIFIER",
                                          "IDENTIFIER",
                                          "IDENTIFIER",
                                          "IDENTIFIER");
    }

    @Test
    void testEmptyMain() {
        final Lexer lex = this.initLexer("EmptyMain.stups");

        final List<String> token = LexerTest.getSymbols(lex);

        assertThat(token).containsExactly("CLASS",
                                          "IDENTIFIER",
                                          "L_BRACE",
                                          "PUBLIC",
                                          "STATIC",
                                          "VOID_TYPE",
                                          "IDENTIFIER_MAIN",
                                          "L_PAREN",
                                          "STRING_TYPE",
                                          "L_BRACKET",
                                          "R_BRACKET",
                                          "IDENTIFIER",
                                          "R_PAREN",
                                          "L_BRACE",
                                          "R_BRACE",
                                          "R_BRACE");
    }

    @Test
    void testGeneralWhile() {
        final Lexer lex = this.initLexer("GeneralWhile.stups");

        final List<String> token = LexerTest.getSymbols(lex);

        assertThat(token).hasSize(68)
                         .containsSequence("WHILE",
                                           "L_PAREN",
                                           "IDENTIFIER",
                                           "LESS",
                                           "INTEGER_LIT",
                                           "R_PAREN")
                         .containsSequence("PRINTLN",
                                           "L_PAREN",
                                           "IDENTIFIER",
                                           "R_PAREN",
                                           "SEMICOLON");
    }

    @Test
    void testGeneralComment() {
        final Lexer lex = this.initLexer("GeneralComment.stups");

        final List<String> token = LexerTest.getSymbols(lex);

        assertThat(token).hasSize(21)
                         .doesNotContain("WHITESPACE")
                         .doesNotContainSequence("INT_TYPE",
                                                 "IDENTIFIER",
                                                 "ASSIGN",
                                                 "INTEGER_LIT",
                                                 "SEMICOLON")
                         .containsSequence("STRING_TYPE",
                                           "L_BRACKET",
                                           "R_BRACKET",
                                           "IDENTIFIER")
                         .containsSequence("PRINTLN",
                                           "L_PAREN",
                                           "STRING_LIT",
                                           "R_PAREN");
    }

    @Test
    void testGeneralIfElse() {
        final Lexer lex = this.initLexer("GeneralIfElse.stups");

        final List<String> token = LexerTest.getSymbols(lex);

        assertThat(token).hasSize(96)
                         .containsSequence("IF",
                                           "L_PAREN",
                                           "IDENTIFIER",
                                           "LESS",
                                           "IDENTIFIER",
                                           "R_PAREN",
                                           "L_BRACE",
                                           "PRINTLN",
                                           "L_PAREN",
                                           "STRING_LIT",
                                           "R_PAREN",
                                           "SEMICOLON",
                                           "R_BRACE",
                                           "ELSE")
                         .containsSequence("ELSE",
                                           "L_BRACE",
                                           "IF");
    }
}

