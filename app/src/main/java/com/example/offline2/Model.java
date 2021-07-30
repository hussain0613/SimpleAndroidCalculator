package com.example.offline2;


import androidx.annotation.NonNull;

import java.util.EmptyStackException;
import java.util.Stack;
import java.util.Vector;

public class Model {
    public String input_string;
    private final Vector<String> postfix_tokens;
    private double value;

    public Model() {
        value = 0.0;
        postfix_tokens = new Vector<>();
    }

    @NonNull
    public String toString(){
        StringBuilder repr = new StringBuilder("Calculator<input=" + input_string);

        repr.append(";rpn="); //reverse polish notation
        for(int i=0; i<postfix_tokens.size(); ++i){
            repr.append(postfix_tokens.get(i)).append(",");
        }

        repr.append(";value=").append(value);
        repr.append(">");
        return repr.toString();
    }
    public double getValue(){
        parse();
        postfix_evaluation();
        return value;
    }

    public void clear(){
        value = 0.0;
        input_string = "";
        postfix_tokens.clear();
    }

    private int get_precedence(char op) {
        int prec;
        switch (op) {
            case '+':
            case '-':
                prec = 1;
                break;
            case '*':
            case '/':
                prec = 2;
                break;
            case '^':
                prec = 3;
                break;
            default:
                prec = -1;
                break;
        }
        return prec;
    }

    private void parse() {
        Stack<Character> conv_op_stack = new Stack<>();

        int start = -1;
        int i = 0;
        for (; i < input_string.length(); ++i) {
            char ch = input_string.charAt(i);

            if (('0' <= ch && ch <= '9') || ch == '.') { //found a digit
                if (start == -1)
                    start = i; // if not started already, start from here, else do nothing
            } else if((start == -1 && (ch == '+' || ch == '-')) && (i > 0 && input_string.charAt(i-1) != ')')){ // this is unary + or -, not binary
                // 2nd condition ta closing parenthesis er thik porer +/- er jonno, karon tokhon oitat ar unary na, borongh binary
                start = i;
            } else if (get_precedence(ch) != -1 || ch == '(' || ch == ')') {// found an operator or a parenthesis
                if (start != -1) {
                    postfix_tokens.add(input_string.substring(start, i));
                    start = -1;
                }


                if (get_precedence(ch) != -1) { // valid operator
                    int prec = get_precedence(ch);
                    while (!(conv_op_stack.empty() || prec > get_precedence(conv_op_stack.peek()) || conv_op_stack.peek() == '(')) {
                        postfix_tokens.add(conv_op_stack.pop() + "");
                    }
                    conv_op_stack.push(ch);
                } else if (ch == '(') {
                    conv_op_stack.push(ch);
                } else { // ch = ')'
                    while (conv_op_stack.peek() != '(') {
                        if (conv_op_stack.empty()) {
                            throw new ArithmeticException("invalid input, closing parenthesis without an opening one, obj: "+this);
                        }
                        postfix_tokens.add(conv_op_stack.pop() + "");
                    }
                    if (!conv_op_stack.empty()) conv_op_stack.pop(); // discarding the '('
                }
            } else {
                if (ch != ' ')
                    throw new ArithmeticException("invalid input, invalid token, obj: "+this);
            }

        }
        if (start != -1) {
            postfix_tokens.add(input_string.substring((start)));
        }
        while (!conv_op_stack.empty()) {
            if (conv_op_stack.peek() != '(') postfix_tokens.add(conv_op_stack.pop() + "");
            else conv_op_stack.pop();
        }
    }

    private void postfix_evaluation() throws EmptyStackException, ArithmeticException {
        Stack<Double> evl_stack = new Stack<>();
        for (int i = 0; i < postfix_tokens.size(); ++i) {
            String token = postfix_tokens.get(i);
            if (token.length() > 1 || get_precedence(token.charAt(0)) == -1) { //token has more than 1 char or not a valid operator i.e. token is a (un)signed number
                evl_stack.push(Double.parseDouble(token));
            } else {
                double a, b;
                b = evl_stack.pop();
                a = evl_stack.pop();


                double val = 0.0;
                switch (token) {
                    case "+":
                        val = a + b;
                        break;
                    case "-":
                        val = a - b;
                        break;
                    case "*":
                        val = a * b;
                        break;
                    case "/":
                        val = a / b;
                        break;
                    case "^":
                        val = Math.pow(a, b);
                        break;
                }
                evl_stack.push(val);
            }
        }
        if (evl_stack.size() == 1) value = evl_stack.pop();
        else throw new ArithmeticException("invalid input, stack ain't empty when it should have been empty, obj: "+this);
    }
}