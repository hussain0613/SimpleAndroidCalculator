package com.example.calc_model;

import java.util.EmptyStackException;
import java.util.Stack;
import java.util.Vector;

public class Model {
    String input_string;
    Vector<String> infix_tokens, postfix_tokens;
    double value;

    public Model()
    {
        value = 0.0;
        infix_tokens = new Vector<>();
        postfix_tokens = new Vector<>();
    }

    public void clear(){
        value = 0.0;
        infix_tokens.clear();
        postfix_tokens.clear();
    }

    int get_precedence(char op)
    {
        int prec;
        switch (op){
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

    void parse()
    {
        Stack<Character> conv_op_stack = new Stack<>();

        int start=-1;
        int i = 0;
        for(; i<input_string.length(); ++i){
            char ch = input_string.charAt(i);

            if(('0'<= ch  && ch <= '9') || ch == '.'){ //found a digit
                if(start == -1) start = i; // if not started already, start from here, else do nothing
            }
            else if((start == -1 && (ch == '+' || ch == '-')) && (i > 0 && input_string.charAt(i-1) != ')')){ // this is unary + or -, not binary
                // 2nd condition ta closing parenthesis er thik porer +/- er jonno, karon tokhon oitat ar unary na, borongh binary
                start = i;
            }

            else if(get_precedence(ch) != -1 || ch == '(' || ch == ')') {// found an operator or a parenthesis
                if (start != -1) {
                    infix_tokens.add(input_string.substring(start, i)); //  if started already, end here
                    postfix_tokens.add(input_string.substring(start, i));
                    start = -1;
                }
                infix_tokens.add(ch + ""); // add the operator or the parenthesis


                /*
                good debug looks like
                System.out.println("ch: "+ ch);
                if(!conv_op_stack.empty()) System.out.println("peek: " + conv_op_stack.peek());
                System.out.print("postfix exp: ");
                for(int j = 0; j< postfix_tokens.size(); ++j){
                    System.out.print(postfix_tokens.get(j) + ",");
                }
                System.out.println("\n");
                */


                if(get_precedence(ch) != -1){ // valid operator
                    int prec = get_precedence(ch);

                    while(!(conv_op_stack.empty() || prec > get_precedence(conv_op_stack.peek()) || conv_op_stack.peek() == '(')){
                        postfix_tokens.add(conv_op_stack.pop() + "");
                    }
                    conv_op_stack.push(ch);
                }
                else if(ch == '('){
                    conv_op_stack.push(ch);
                }else{ // ch = ')'
                    while(conv_op_stack.peek() != '('){
                        if (conv_op_stack.empty()){
                            throw new ArithmeticException("invalid input, closing parenthesis without an opening one");
                        }
                        postfix_tokens.add(conv_op_stack.pop() + "");
                    }
                    if(!conv_op_stack.empty()) conv_op_stack.pop(); // discarding the '('
                }
            }
            else{
                if(ch != ' ')
                    throw new ArithmeticException("invalid input, token");
            }

        }
        if (start != -1){
            infix_tokens.add(input_string.substring(start));
            postfix_tokens.add(input_string.substring((start)));
        }
        while(!conv_op_stack.empty()) {
            if(conv_op_stack.peek() != '(') postfix_tokens.add(conv_op_stack.pop() + "");
            else conv_op_stack.pop();
        }
    }

    void postfix_evaluation() throws EmptyStackException, ArithmeticException
    {
        Stack<Double> evl_stack = new Stack<>();
        for(int i = 0; i< postfix_tokens.size(); ++i){
            String token = postfix_tokens.get(i);
            if(token.length()>1 || get_precedence(token.charAt(0)) == -1){ //token has more than 1 char or not a valid operator i.e. token is a (un)signed number
                evl_stack.push(Double.parseDouble(token));
            }
            else{
                double a, b;
                b = evl_stack.pop();
                a = evl_stack.pop();


                double val = 0.0;
                switch (token){
                    case "+":
                        val = a+b;
                        break;
                    case "-":
                        val = a-b;
                        break;
                    case "*":
                        val = a*b;
                        break;
                    case "/":
                        val = a/b;
                        break;
                    case "^":
                        val = Math.pow(a, b);
                        break;
                }
                evl_stack.push(val);
            }
        }
        if(evl_stack.size() == 1) value = evl_stack.pop();
        else throw new ArithmeticException("invalid input, not empty when it should have been empty");
    }
    static public void main(String[] args)
    {
        System.out.println("Hello World!");

        Model mdl = new Model();
        //mdl.input_string = "-50+40*(30-20)/60";
        //mdl.input_string = " -2.0 * 6 ";
        mdl.input_string = "5-(2+3)-3";

        mdl.parse();
        System.out.println("input: " + mdl.input_string);

        System.out.print("infix exp: ");
        for(int i = 0; i< mdl.infix_tokens.size(); ++i){
            System.out.print(mdl.infix_tokens.get(i) + ",");
        }
        System.out.println();

        System.out.print("postfix exp: ");
        for(int i = 0; i< mdl.postfix_tokens.size(); ++i){
            System.out.print(mdl.postfix_tokens.get(i) + ",");
        }

        System.out.println();
        mdl.postfix_evaluation();
        System.out.println("value: " + mdl.value);
    }
}