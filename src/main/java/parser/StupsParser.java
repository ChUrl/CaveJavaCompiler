package parser;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import parser.ast.SyntaxTree;
import parser.ast.SyntaxTreeNode;
import parser.grammar.Grammar;
import parser.grammar.GrammarAnalyzer;
import util.Logger;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

/**
 * Leitet eine Liste von Token nach einer Grammatik mit Hilfe einer {@link ParsingTable} ab.
 */
public class StupsParser {

    private final ParsingTable parsetable;

    public StupsParser(ParsingTable parsetable) {
        this.parsetable = parsetable;
    }

    public static StupsParser fromGrammar(Grammar grammar) {
        final GrammarAnalyzer analyzer = GrammarAnalyzer.fromGrammar(grammar);
        return new StupsParser(analyzer.getTable());
    }

    private static String printSourceLine(int line, Collection<? extends Token> token) {
        final Optional<String> srcLine = token.stream()
                                              .filter(tok -> tok.getLine() == line)
                                              .map(Token::getText)
                                              .reduce((s1, s2) -> s1 + " " + s2);

        return "  :: " + srcLine.orElse("");
    }

    public SyntaxTree parse(List<? extends Token> token, Vocabulary voc) {
        Logger.logDebug("Beginning program-parsing", StupsParser.class);

        final SyntaxTreeNode root = new SyntaxTreeNode(Grammar.START_SYMBOL, 0);
        final SyntaxTree tree = new SyntaxTree(root);
        final Deque<SyntaxTreeNode> stack = new ArrayDeque<>();
        stack.push(root);

        int inputPosition = 0;

        // Parsing
        while (!stack.isEmpty()) {
            final String top = stack.peek().getName();

            Logger.logInfo("Parsing Top Symbol: \"" + top + "\"", StupsParser.class);

            final String currentTokenSym;
            int currentLine = 0;
            if (inputPosition >= token.size()) {
                // Wenn auf dem Stack mehr Nichtterminale liegen als Terminale in der Eingabe vorhanden sind
                // Die Eingabe wurde komplett konsumiert

                currentTokenSym = "$"; // EOF
            } else {
                // Es sind noch Eingabesymbole vorhanden

                currentTokenSym = voc.getSymbolicName(token.get(inputPosition).getType());
                currentLine = token.get(inputPosition).getLine();
            }

            final String prod = this.parsetable.get(top, currentTokenSym);

            if (top.equals(Grammar.EPSILON_SYMBOL)) {
                // Wenn auf dem Stack das Epsilonsymbol liegt

//                Logger.logInfo(" :: Skip epsilon", StupsParser.class);

                stack.pop();
            } else if (top.equals(currentTokenSym)) {
                // Wenn auf dem Stack ein Terminal liegt (dieses muss mit der Eingabe übereinstimmen)

//                Logger.logInfo(" :: Skip terminal-symbol (Matches input)", StupsParser.class);

                stack.pop();
                inputPosition++;
            } else if (this.parsetable.getTerminals().contains(top)) {
                // Wenn das Terminal auf dem Stack nicht mit der aktuellen Eingabe übereinstimmt

                Logger.logError("Line " + currentLine + " Syntaxerror: Expected " + top + " but found "
                                + currentTokenSym, StupsParser.class);
                Logger.logError(StupsParser.printSourceLine(currentLine, token), StupsParser.class);

                throw new ParseException("Invalid terminal on stack: " + top, tree);
            } else if (prod == null) {
                // Wenn es für das aktuelle Terminal und das Nichtterminal auf dem Stack keine Regel gibt

                Logger.logError("Line " + currentLine + " Syntaxerror: Didn't expect " + currentTokenSym, StupsParser.class);
                Logger.logError(StupsParser.printSourceLine(currentLine, token), StupsParser.class);

                throw new ParseException("No prod. for nonterminal " + top + ", terminal " + currentTokenSym, tree);
            } else {
                // Wenn das Nichtterminal auf dem Stack durch (s)eine Produktion ersetzt werden kann
                // Hier wird auch der AST aufgebaut

                Logger.logInfo(" :: Used rule: \"" + top + " -> " + prod + "\"", StupsParser.class);
                final SyntaxTreeNode pop = stack.pop();

                final String[] split = prod.split(" ");

                for (int i = split.length - 1; i >= 0; i--) {
                    final SyntaxTreeNode node = new SyntaxTreeNode(split[i], currentLine);

                    if (inputPosition + i < token.size()) {
                        // Die Schleife geht in der Eingabe weiter
                        final Token currentTok = token.get(inputPosition + i);

                        // Die Token mit semantischem Inhalt auswählen
                        if ("IDENTIFIER".equals(split[i]) || split[i].endsWith("_LIT")) {
                            node.setValue(currentTok.getText());
                        }
                    }

                    stack.push(node);
                    pop.addChild(node);
                }
            }
        }

        Logger.logDebug("Successfully parsed the program and built the parse-tree", StupsParser.class);

        return tree;
    }
}
