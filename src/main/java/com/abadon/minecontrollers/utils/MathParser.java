package com.abadon.minecontrollers.utils;


import java.util.*;

abstract class AbstractMathContext{
    public int result = 0;
    abstract void addToken(AbstractMathToken token);
    abstract AbstractCollection<AbstractMathToken> getTokens();
}
class MathContext extends AbstractMathContext{
    private ArrayList<AbstractMathToken> tokens = new ArrayList<>();
    @Override
    void addToken(AbstractMathToken token) {
        this.tokens.add(token);
    }

    @Override
    AbstractCollection<AbstractMathToken> getTokens() {
        return tokens;
    }
}
abstract class AbstractMathToken{
    String value;
    public AbstractMathToken(String value){
        this.value = value;
    }
    public String getValue(){
        return value;
    }
    public abstract void act();
}
class MathToken extends AbstractMathToken{
    public MathToken(String value) {
        super(value);
    }

    @Override
    public void act() {

    }

    public MathToken getToken(String value){
        return null;
    }
}
class IncorrectTokenValueException extends RuntimeException{
    public IncorrectTokenValueException(String msg){
        super(msg);
    }
}
class ValueToken extends MathToken{
    public ValueToken(){super(null);}
    private ValueToken(String value) {
        super(value);
        if(!value.matches("\\d+")){
            throw new IncorrectTokenValueException(value + " isn't a number");
        }
    }

    @Override
    public MathToken getToken(String value) {
        return new ValueToken(value);
    }
}
class SignedValueToken extends MathToken{
    public SignedValueToken(){super(null);}
    private SignedValueToken(String value) {
        super(value);
        if(!value.matches("-?\\d+")){
            throw new IncorrectTokenValueException(value + " isn't a number");
        }
    }

    @Override
    public MathToken getToken(String value) {
        return new SignedValueToken(value);
    }
}
class BracketToken extends MathToken{
    public BracketToken(){super(null);}
    private BracketToken(String value){
        super(value);
        if(!(value.equals("(") || value.equals(")")))
            throw new IncorrectTokenValueException(value + " isn't a bracket");
    }
    public boolean isOpen(){
        return value.equals("(");
    }
    @Override
    public MathToken getToken(String value) {
        return new BracketToken(value);
    }
}
class OperatorToken extends MathToken{
    public OperatorToken(){super(null);}
    private String availableOperators = "+-*/^&|";
    private OperatorToken(String value) {
        super(value);
        boolean isValid = false;
        for(char operator : availableOperators.toCharArray()){
            if (value.equals(String.valueOf(operator))) {
                isValid = true;
                break;
            }
        }
        if(!isValid) throw new IncorrectTokenValueException(value + "isn't an operator");
    }
    public int getPriority(){
        switch (value){
            case "&":{
                return 0;
            }
            case "|":{
                return 0;
            }
            case "+":{
                return 1;
            }
            case "-":{
                return 1;
            }
            case "*":{
                return 2;
            }
            case "/":{
                return 2;
            }
            case "^":{
                return 3;
            }
        }
        return 0;
    }
    @Override
    public MathToken getToken(String value) {
        return new OperatorToken(value);
    }
}
class ContextToken extends MathToken{
    private MathContext context;
    public void setContext(MathContext context){
        this.context = context;
    }
    public MathContext getContext(){
        return context;
    }
    public ContextToken() {
        super("");
    }
}
class TokensTree {
    AbstractMathContext context = null;
    private LinkedList<TokensTree> trees = new LinkedList<>();
    public TokensTree(){
    }
    public void setContext(AbstractMathContext context){
        this.context = context;
    }
    public void addTree(TokensTree tree){
        trees.add(tree);
    }
    public boolean hasTrees(){
        return trees.size() != 0;
    }
    public TokensTree next(){
        if(trees.getFirst().hasTrees()){
            return trees.getFirst().next();
        } else {
            return trees.pop();
        }
    }
}
class Tokenizer {
    private String source;
    public Tokenizer(String source){
        this.source = source;
    }
    public final ArrayList<MathToken> TOKENS = new ArrayList<>(List.of(new MathToken[]{
            new ValueToken(),
            new OperatorToken(),
            new BracketToken()
    }));
    public ArrayList<MathToken> getTokens(){
        int begin = 0;
        MathToken parsedToken = null;
        ArrayList<MathToken> parsedTokens = new ArrayList<>();
        for(int i = begin+1; i <= source.length(); i++){
            boolean succes = false;
            for(MathToken token : TOKENS){
                try{
                    parsedToken = token.getToken(source.substring(begin, i));
                    succes = true;
                    break;
                } catch (IncorrectTokenValueException exception){
                }
            }
            if(!succes){
                begin = i-1;
                if(parsedToken != null) i--;
                parsedTokens.add(parsedToken);
                parsedToken = null;
            }
            if(i == source.length() && parsedToken != null){
                parsedTokens.add(parsedToken);
            }
        }
        return parsedTokens;
    }
}
public class MathParser {
    private static int mathOperation(OperatorToken token, int a, int b){
        switch (token.getValue()){
            case "+":{
                return a + b;
            }
            case "-":{
                return a - b;
            }
            case "*":{
                return a * b;
            }
            case "/":{
                return a / b;
            }
            case "^":{
                return (int)Math.pow((double) a, (double) b);
            }
            case "&":{
                return a & b;
            }
            case "|":{
                return a | b;
            }
        }
        return 0;
    }
    public static void parseTree(TokensTree tree){
        while (true){
            TokensTree subtree = null;
            if(tree.hasTrees())
                subtree = tree.next();
            else subtree = tree;

            LinkedList<AbstractMathToken> tokens = new LinkedList<>();
            ArrayList<OperatorToken> operatorTokens = new ArrayList<>();
            for(AbstractMathToken token : subtree.context.getTokens()){
                if(token instanceof OperatorToken operatorToken){
                    operatorTokens.add(operatorToken);
                }
                tokens.add(token);
            }
            operatorTokens.sort((a, b) -> b.getPriority() - a.getPriority());
            int output = 0;
            for(OperatorToken operatorToken : operatorTokens){

                int firstValue = 0;
                int secondValue = 0;
                AbstractMathToken firstToken = null;
                AbstractMathToken secondToken = null;
                if(tokens.indexOf(operatorToken)-1 >= 0){
                    if(tokens.get(tokens.indexOf(operatorToken)-1) instanceof ValueToken token) {
                        firstToken = token;
                        firstValue = Integer.parseInt(token.getValue());
                    }
                    else if(tokens.get(tokens.indexOf(operatorToken)-1) instanceof SignedValueToken token) {
                        firstToken = token;
                        firstValue = Integer.parseInt(token.getValue());
                    }
                    else if (tokens.get(tokens.indexOf(operatorToken)-1) instanceof ContextToken ctx) {
                        firstToken = ctx;
                        firstValue = ctx.getContext().result;
                    }
                }
                if(tokens.indexOf(operatorToken)+1 < tokens.size()){
                    if(tokens.get(tokens.indexOf(operatorToken)+1) instanceof ValueToken token){
                        secondToken = token;
                        secondValue = Integer.parseInt(token.getValue());
                    }
                    if(tokens.get(tokens.indexOf(operatorToken)+1) instanceof SignedValueToken token){
                        secondToken = token;
                        secondValue = Integer.parseInt(token.getValue());
                    }
                    else if (tokens.get(tokens.indexOf(operatorToken)+1) instanceof ContextToken ctx) {
                        secondToken = ctx;
                        secondValue = ctx.getContext().result;
                    }
                }
                if(firstToken == null || secondToken == null) return;
                int result = mathOperation(operatorToken, firstValue, secondValue);
                tokens.set(tokens.indexOf(operatorToken), new SignedValueToken().getToken(String.valueOf(result)));
                tokens.remove(firstToken);
                tokens.remove(secondToken);
                output = result;
            }
            subtree.context.result = output;
            if(subtree == tree) return;
        }
    }
    public static String parse(String expression){
        expression = expression.replaceAll("\\s+", "");
        Tokenizer tokenizer = new Tokenizer(expression);
        Stack<MathContext> contextStack = new Stack<>();
        Stack<TokensTree> treeStack = new Stack<>();
        TokensTree tree = new TokensTree();
        MathContext context = new MathContext();
        tree.setContext(context);
        //getting tokens and creating context tree
        ArrayList<MathToken> tokens = tokenizer.getTokens();
        if(tokens.size() < 3) return expression;
        for (AbstractMathToken token : tokens){
            if (token == null) return expression;
            if(token instanceof BracketToken bracket){
                if(bracket.isOpen()){
                    contextStack.add(context);
                    MathContext newContext = new MathContext();
                    ContextToken ctx = new ContextToken();
                    ctx.setContext(newContext);
                    context.addToken(ctx);
                    context = newContext;
                    TokensTree newTree = new TokensTree();
                    tree.addTree(newTree);
                    treeStack.add(tree);
                    tree = newTree;
                } else {
                    tree.setContext(context);
                    context = contextStack.pop();
                    tree = treeStack.pop();
                }
            } else {
                context.addToken(token);
            }
        }
        parseTree(tree);
        return String.valueOf(tree.context.result);
    }
}
