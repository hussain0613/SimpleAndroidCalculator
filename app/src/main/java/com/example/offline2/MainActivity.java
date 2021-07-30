package com.example.offline2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.EmptyStackException;

public class MainActivity extends AppCompatActivity {

    Model calc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        calc = new Model();
    }


    // between models and controllers
    void display_input(String text) {
        TextView tv = findViewById(R.id.input_view);
        tv.append(text);
    }

    void display_result(String text)
    {
        TextView tv = findViewById(R.id.result_view);
        tv.setText(text);
    }

    void clear()
    {
        TextView tv = findViewById(R.id.input_view);
        tv.setText("");
        tv = findViewById(R.id.result_view);
        tv.setText("");
        calc.clear();
    }

    // controllers (!)
    public void ac_btn_action(View view)
    {
        clear();
    }

    public void numerical_btn_actions(View view)
    {
        Button btn = (Button) view;
        String input = (String)btn.getText();
        display_input(input);
        //display_result("");
    }

    public void eql_btn_action(View view)
    {
        calc.clear();
        calc.input_string = ((TextView)findViewById(R.id.input_view)).getText().toString();

        try{
            display_result(Double.toString(calc.getValue()));
        }catch (ArithmeticException err){
            display_result("invalid input");
        }catch (EmptyStackException err){
            display_result("invalid input");
        }
    }
    public void del_btn_action(View view)
    {
        TextView tv = findViewById(R.id.input_view);
        String txt = tv.getText().toString();
        int n = txt.length();
        if(n>0) tv.setText(txt.substring(0, n-1));
        //display_result("");
    }
}