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

    void parse2(){
        int start = -1;
        int i = 0;
        for(;i<input_string.length(); ++i){
            char ch = input_string.charAt(i);

//            System.out.println(ch);
//            System.out.println(infix_tokens.toString() + "\n");

            if(('0'<= ch && ch<= '9') || ch == '.'){// found a digit or a dot
                if(ch == '.'){
                    if(i+1<input_string.length()){// not last digit
                        char ch2 = input_string.charAt(i+1);
                        if(!('0'<= ch2 && ch2 <= '9'))// not a number // if digit, then fine
                            throw new NumberFormatException("invalid number, non digit char after . at i =" + i);
                    }else{
                        throw new NumberFormatException("invalid number, . as the last input");
                    }
                }
                if(start == -1) start = i; //else just continue
            }
            else if (get_precedence(ch) != -1 || ch == '(' || ch ==')'){ // not a digit, but a valid operator or parenthesis

                if((start == -1 && (ch == '+' || ch == '-')) ) {//not a digit, nor after any digit but is +/-, might be unary
                    if(i>1 && input_string.charAt(i-1) == ')') {// right after a closing parenthesis, so it's binary
                        infix_tokens.add(ch + "");
                        start = -1;
                    }else{//not binary
                        if(i<input_string.length()-1){// not the last char
                            char ch2 = input_string.charAt(i+1);
                            if(('0'<= ch2 && ch2<= '9') || ch2 == '.' || ch2 == '+' || ch2 == '-'){//next char is a number or a +/-
                                // unary
                                start = i;
                            }else{ // some other operator after +/-
                                if(ch2 == '(') { // not exactly binary, but can treat as one
                                    infix_tokens.add(ch + "");
                                    start = -1;
                                }
                                else // some other operator or closing parenthesis right after +/-
                                    throw new ArithmeticException("invalid input, at i=" + i+" '"+ch2+"' right after '"+ch+"'");
                            }
                        }else{ // last char is +/-, so invalid input
                            throw new ArithmeticException("invalid input, last char input is "+ch);
                        }
                    }
                }else{ // normal operator or parenthesis
                    if(start != -1){
                        infix_tokens.add(input_string.substring(start, i));
                        start = -1;
                    }
                    infix_tokens.add(ch+"");
                }

            }
            else{
                throw new ArithmeticException("invalid character '" + ch + "' at i="+i );
            }
        }
        if(start != -1)// means some left
            infix_tokens.add(input_string.substring(start));
    }

    boolean is_number(String str){
        // assuming all char are digits or . or operators or parentheses and of at least length 1
        if(str.length()>1)return true;
        else{
            char ch = str.charAt(0);
            if(get_precedence(ch) != -1 || ch == '(' || ch == ')') return false;
            else return true;
        }
    }

    void infix_to_postfix()
    {
        // here assuming all the individual entry in infix_tokens are valid

        Stack<Character> conv_op_stack = new Stack<>();

        int i = 0;
        for(;i<infix_tokens.size(); ++i){
            String token = infix_tokens.get(i);

            if(is_number(token)){ // so it must be a number
                //System.out.println("adding num: " + token);
                if(i > 0 && (is_number(infix_tokens.get(i-1)) || infix_tokens.get(i-1).charAt(0) == ')')){
                    throw new ArithmeticException("invalid input, invalid char before number");
                }
                if(i+1 < infix_tokens.size() && (is_number(infix_tokens.get(i+1)) || infix_tokens.get(i+1).charAt(0) == '(')){
                    throw new ArithmeticException("invalid input, invalid char after number");
                }

                postfix_tokens.add(token);
            }else{//as all are valid then must be an operator or parenthesis
                char op = token.charAt(0);
                int precedence = get_precedence(op);

                if(precedence != -1){ // operator
                    if(i == 0){ // it is the first one
                        if(precedence != 1) throw new ArithmeticException("invalid input, first token ="+ token);
                    }
                    if(i+1 >= infix_tokens.size()) {// it is the last one and not the first one
                        throw new ArithmeticException("invalid input, operator at the end");
                    }
                    // in the middle, not the first nor the last one
                    String left =  null, right = infix_tokens.get(i+1);
                    if (i!= 0) left = infix_tokens.get(i-1);
                    if(left!= null && !is_number(left)){//left is not a number
                        if(precedence != 1 && !left.equals(")")){ // token is not +/-, if token is +/- anything can be on it's left
                            // token is an op other than +/-, left is not a number nor closing parenthesis, so it's invalid
                            throw new ArithmeticException("invalid input, invalid char '" + left +"' on the left of operator '" + token+"'");
                        }
                    }
                    if(!is_number(right)){ // right is not a number
                        if(right.equals(")") || (!is_number(right) && get_precedence(right.charAt(0)) > 1)) // right is closing paren or some op other than +/-
                            throw new ArithmeticException("invalid input, invalid char '" + right +"' on the right of operator '" + token+"'");
                    }


                    while(!(conv_op_stack.empty() || precedence >= get_precedence(conv_op_stack.peek()) || conv_op_stack.peek() == '(')) {
                        //System.out.println("got op "+ op +". adding op: " + conv_op_stack.peek());
                        postfix_tokens.add(conv_op_stack.pop() + "");
                    }
                    // pushing op only if stack is empty or top is '(' or op_prec is >= top_prec
                    conv_op_stack.push(op);
                }
                else if(op == '(') {
                    if(i>0){
                        String left = infix_tokens.get(i-1);
                        if(is_number(left) || left.equals(")"))
                            throw new ArithmeticException("invalid input, found '"+left+"' on the left of '('");
                    }
                    if(i+1 < infix_tokens.size()){
                        String right = infix_tokens.get(i+1);
                        if(right.equals(")") || (!is_number(right) && get_precedence(right.charAt(0)) > 1))
                            throw new ArithmeticException("invalid input, found '"+right+"' right after '('");
                    }else{
                        throw new ArithmeticException("invalid input, '(' at the end, i="+i);
                    }
                    conv_op_stack.push(op);
                }
                else{ // must be ')'
                    if(i>0){
                        String left = infix_tokens.get(i-1);
                        if(left.equals("(") || (!is_number(left) && get_precedence(left.charAt(0))>0))
                            throw new ArithmeticException("invalid input, found '"+left+"' on the left of ')'");
                    }else
                        throw new ArithmeticException("invalid input, ')' at the beginning");

                    if(i+1 < infix_tokens.size()){
                        String right = infix_tokens.get(i+1);
                        if(is_number(right) || right.equals("("))
                            throw new ArithmeticException("invalid input, found '"+right+"' right after '('");
                    }

                    while(!conv_op_stack.empty() && conv_op_stack.peek() != '(') {
                        //System.out.println("got ). adding op: " + conv_op_stack.peek());
                        postfix_tokens.add(conv_op_stack.pop() + "");
                    }
                    if(!conv_op_stack.empty()) conv_op_stack.pop();
                }
            }
        }
        while(!conv_op_stack.empty()) {
            //System.out.println("adding leftovers: " + conv_op_stack.peek());
            postfix_tokens.add(conv_op_stack.pop() + "");
        }
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
            else if((start == -1 && (ch == '+' || ch == '-')) && ((i > 0 && input_string.charAt(i-1) != ')') || i == 0)){ // this is unary + or -, not binary
                // 2nd condition ta closing parenthesis er thik porer +/- er jonno, karon tokhon oitat ar unary na, borongh binary
                // however if the first condition is true, and i ==0 then it might be an unary +/-
                start = i;
            }

            else if(get_precedence(ch) != -1 || ch == '(' || ch == ')') {// found an operator or a parenthesis
                if (start != -1) {
                    String token = input_string.substring(start, i);
                    infix_tokens.add(token); //  if started already, end here
                    postfix_tokens.add(token);
                    start = -1;
                }
                infix_tokens.add(ch + ""); // add the operator or the parenthesis


                //*
                //good debug looks like
                System.out.println("ch: "+ ch);
                if(!conv_op_stack.empty()) System.out.println("peek: " + conv_op_stack.peek());
                System.out.print("postfix exp: ");
                for(int j = 0; j< postfix_tokens.size(); ++j){
                    System.out.print(postfix_tokens.get(j) + ",");
                }
                System.out.println("\n");
                //*/


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

                if(evl_stack.empty() && (token.equals("+") || token.equals("-")))
                    a = 0.0;
                else
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
        else throw new ArithmeticException("invalid input, eval_stack size = "+evl_stack.size()+" when it should have been 1");
    }
    static public void main(String[] args)
    {
        System.out.println("Hello World!");

        Model mdl = new Model();
        //mdl.input_string = "-50+40*(30-20)/60";
        //mdl.input_string = " -2.0 * 6 ";
        //mdl.input_string = "5-(2+3)-3";
        //mdl.input_string = "-3*3";
        //mdl.input_string = "-(---3*3)";
        //mdl.input_string = "-50-(20-50)---20..0";
        //mdl.input_string = "-(3-*-3)";
        mdl.input_string = "(2*3)*3";

        System.out.println("input: " + mdl.input_string);
        //mdl.parse();
        mdl.parse2();

        System.out.print("infix exp: ");
        for(int i = 0; i< mdl.infix_tokens.size(); ++i){
            System.out.print(mdl.infix_tokens.get(i) + ",");
        }
        System.out.println();

        mdl.infix_to_postfix();
        System.out.print("postfix exp: ");
        for(int i = 0; i< mdl.postfix_tokens.size(); ++i){
            System.out.print(mdl.postfix_tokens.get(i) + ",");
        }

        System.out.println();
        mdl.postfix_evaluation();
        System.out.println("value: " + mdl.value);
    }
}