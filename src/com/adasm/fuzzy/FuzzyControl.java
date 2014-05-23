package com.adasm.fuzzy;

/*******************************
 By Adam Michalowski (c) 2012
 *******************************/

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

public class FuzzyControl { static boolean debug = true;
    public Base.FuzzyData data = new Base.FuzzyData();
    public void resetAll() {
        if(debug)Test.debugTextArea.append("\nReset all.");
        for(Base.Param param : data.params.values()) {
            param.inVal = param.range.a;
            param.outVal = param.range.a;
            param.inValHistory.clear();
            param.outValHistory.clear();
        }
        for(Base.Member member : data.members.values()) {
            member.val = 0;
            member.valHistory.clear();
        }
    }
    public void resetOutput() {
        if(debug)Test.debugTextArea.append("\nReset output.");
        for(Base.Param param : data.params.values()) {
            param.outVal = param.range.a;
        }
        for(Base.Member member : data.members.values()) {
            member.val = 0;
        }
    }
    public boolean init() {
        data.addParam("Temperature", new Base.Range(-50, 100));
        data.addMember("Temperature", "Low", new Base.Curve(-50, -50, 19));
        data.addMember("Temperature", "NormalNight", new Base.Curve(18, 20, 21));
        data.addMember("Temperature", "NormalDay", new Base.Curve(20, 24, 26));
        data.addMember("Temperature", "High", new Base.Curve(25, 100, 100));
        data.addParam("Humidity", new Base.Range(0, 100));
        data.addMember("Humidity", "Low", new Base.Curve(0, 0, 70));
        data.addMember("Humidity", "Normal", new Base.Curve(30, 70, 90));
        data.addMember("Humidity", "High", new Base.Curve(70, 100, 100));
        data.addParam("Light", new Base.Range(0, 100));
        data.addMember("Light", "Low", new Base.Curve(0, 0, 100));
        data.addMember("Light", "High", new Base.Curve(0, 100, 100));
        data.addParam("CO2", new Base.Range(0, 1000));
        data.addMember("CO2", "Low", new Base.Curve(0, 0, 750));
        data.addMember("CO2", "Normal", new Base.Curve(700, 750, 800));
        data.addMember("CO2", "High", new Base.Curve(750, 1000, 1000));
        data.addParam("HeatFan", new Base.Range(0, 100));
        data.addMember("HeatFan", "Stop", new Base.Curve(0));
        data.addMember("HeatFan", "Normal", new Base.Curve(0, 50, 100));
        data.addMember("HeatFan", "High", new Base.Curve(50, 100, 100));
        data.addParam("CoolFan", new Base.Range(0, 100));
        data.addMember("CoolFan", "Stop", new Base.Curve(0));
        data.addMember("CoolFan", "Normal", new Base.Curve(0, 50, 100));
        data.addMember("CoolFan", "High", new Base.Curve(50, 100, 100));
        data.addParam("AirFan", new Base.Range(0, 100));
        data.addMember("AirFan", "Stop", new Base.Curve(0));
        data.addMember("AirFan", "Normal", new Base.Curve(0, 50, 100));
        data.addMember("AirFan", "High", new Base.Curve(50, 100, 100));
        data.addParam("CO2Out", new Base.Range(0, 100));
        data.addMember("CO2Out", "Stop", new Base.Curve(0));
        data.addMember("CO2Out", "Normal", new Base.Curve(0, 50, 100));
        data.addMember("CO2Out", "High", new Base.Curve(50, 100, 100));
        data.addParam("Water", new Base.Range(0, 100));
        data.addMember("Water", "Stop", new Base.Curve(0));
        data.addMember("Water", "Low", new Base.Curve(0, 0, 50));
        data.addMember("Water", "Normal", new Base.Curve(0, 50, 100));
        data.addMember("Water", "High", new Base.Curve(50, 100, 100));
        // CoolFan Control
        data.addRule("( Temperature is Low ) or ( Temperature is NormalNight and Light is Low ) or ( Temperature is NormalDay and Light is High )", "CoolFan", "Stop");
        data.addRule("( Temperature is NormalDay and Light is Low )", "CoolFan", "Normal");
        data.addRule("( Temperature is High )", "CoolFan", "High");
        // HeatFan Control
        data.addRule("( Temperature is Low )", "HeatFan", "High");
        data.addRule("( Temperature is NormalNight and Light is High )", "HeatFan", "Normal");
        data.addRule("( Temperature is High )", "HeatFan", "Stop");
        // AirFan Control
        data.addRule("( Humidity is High or Temperature is High or Light is High )", "AirFan", "High");
        data.addRule("( Humidity is Normal or Light is High ) or ( Humidity is Low )", "AirFan", "Normal");
        // Water Control
        data.addRule("( Temperature is High or Humidity is Low )", "Water", "High");
        data.addRule("( Temperature is NormalNight or Temperature is NormalDay )", "Water", "Normal");
        data.addRule("( Temperature is Low )", "Water", "Low");
        // CO2 Control
        data.addRule("( CO2 is Low )", "CO2Out", "High");
        data.addRule("( CO2 is Normal )", "CO2Out", "Low");
        data.addRule("( CO2 is High )", "CO2Out", "Stop");
        return false;
    }
    public String getScript() {
        String str = "";
        for(Base.Param param : data.params.values()) {
            str += "\nparam " + param.name + " " + param.range.a + " " + param.range.b + " " + param.step;
            for(Base.Member member : param.members) {
                str += "\nmember " + param.name + " " + member.name + " " + member.curve.a +  " " + member.curve.b +  " " + member.curve.c +  " " + member.curve.d;
            }
        }
        for(Base.Rule rule : data.rules) {
            str += "\n" + rule.getDef();
        }
        return str;
    }
    public double lerp(double a, double b, double amount) {
        return a + (b-a)*amount;
    }
    public boolean run(boolean randomParams) {
        if(debug)Test.debugTextArea.append("\n\n[" + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSSS")).format(Calendar.getInstance().getTime()) + "]\nRunning...");
        resetOutput();
        // INIT
        if(randomParams == true) {
            if(debug)Test.debugTextArea.append("\nGenerating random input values...");
            Random r = new Random();
            for(Base.Param param : data.params.values()) {
                param.inVal = param.range.a + Math.abs(param.range.b - param.range.a)*r.nextDouble();
                if(debug)Test.debugTextArea.append("\n " + param.name + " = " + param.inVal);
            }
        }

        // RULES PROC
        for(Base.Rule rule : data.rules) {
            rule.outParamMember.val = rule.root.eval();
            if(debug)Test.debugTextArea.append("\nRule " + rule.outParamNameStr+rule.outParamMemberStr + " eval ->" + rule.outParamMember.val);
        }

        // DEFUZZING
        for(Base.Param param : data.params.values()) {
            if(param.asOutput == true) {
                param.defuzzy();
                if(debug)Test.debugTextArea.append("\nDeffuzification of " + param.name + " ->" + param.outVal);
            }
        }
        // SAVING HISTORY
        for(Base.Param param : data.params.values()) {
            if(param.asOutput == true) {
                param.outValHistory.add(param.outVal);
                for(Base.Member member : param.members) {
                    member.valHistory.add(member.val);
                }
            }
            else {
                param.inValHistory.add(param.inVal);
            }
        }
        if(debug)Test.debugTextArea.append("\nOK.");
        return true;
    }
}
