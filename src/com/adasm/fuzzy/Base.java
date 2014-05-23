package com.adasm.fuzzy;

/*******************************
 By Adam Michalowski (c) 2012
 *******************************/

import java.util.*;
import javax.swing.JOptionPane;

public final class Base { static boolean debug = true;
    public static class Range {
        public Range(double _a, double _b) { a = _a; b = _b; }
        public double a, b;
    }
    public static class Curve {
        public Curve(double _a, double _b, double _c, double _d) { a = _a; b = _b; c = _c; d = _d; }
        public Curve(double _a, double _b, double _c) { a = _a; b = _b; c = _b; d = _c; }
        public Curve(double _a, double _b) { a = _a; b = _a; c = _b; d = _b; }
        public Curve(double _a) { a = _a; b = _a; c = _a; d = _a; }
        public Curve() { a = b = c = d = 0.0; }
        public double a, b, c, d;
        public double getY(double x) {
            if(x < a) return 0.0;
            else {	if(x < b) return (x - a)/(b - a);
            else {	if(x <= c) return 1.0;
            else {	if(x <= d) return (d - x) / (d - c);
            else return 0.0;
            }
            }
            }
        }
    }
    public static class Member {
        public String	name;
        public Param	param;
        public Curve	curve;
        public double	val;
        public ArrayList<Double> valHistory = new ArrayList<Double>();
    }
    public static class Param {
        public String	name;
        public Range	range;
        public double	step;
        public double	inVal, outVal;
        public ArrayList<Double> inValHistory = new ArrayList<Double>();
        public ArrayList<Double> outValHistory = new ArrayList<Double>();
        public ArrayList<Member> members = new ArrayList<Member>();
        public boolean	needInputValue, asOutput;
        public boolean defuzzy() {
            outVal = range.a;
            double currentStep;
            //COG
            double cogA = 0, cogB = 0;
            //Mean-Max
            //double maxV = 0, maxX = range.a, maxXLast = range.a;
            for(double x = range.a; x <= range.b; x += step) {
                currentStep = 0;
                for(Member member : members) {
                    if(x >= member.curve.a && x <= member.curve.d)
                        currentStep = Math.max(currentStep, Math.min(member.curve.getY(x), member.val));
                }
                //Mean-Max
                //if(maxV < currentStep) { maxV = currentStep; maxXLast = maxX = x; }
                //if(maxV > 0 && maxV == currentStep) { maxXLast = x; }
                //COG
                cogA += currentStep*(x-range.a);
                cogB += currentStep;
            }
            //Mean-Max
            //if(debug)Test.debugTextArea.append("\ndefuzzy() Mean Max: maxX = " + maxX + " maxXLast = " + maxXLast);
            //outVal = (maxX + maxXLast) / 2;
            //COG
            if(debug)Test.debugTextArea.append("\ndefuzzy() COG: cogA = " + cogA + " cogB = " + cogB);
            outVal = (cogB != 0) ? (cogA/cogB) : (0);
            return true;
        }
    }
    public static class RuleNode {
        public RuleNode() { left = right = null; }
        public RuleNode(int t, RuleNode l, RuleNode r, Param p, Member m)
        { type = t; left = l; right = r; param = p; member = m; }
        public int type; // 0 - IS, 1 - AND, 2 - OR
        public RuleNode left, right;
        public Param param; Member member;
        public double eval() {
            if(type == 0) { return member.curve.getY(param.inVal); }
            else if(left != null && right != null) {
                if(type == 1) { return Math.min(left.eval(), right.eval()); }
                else if(type == 2) { return Math.max(left.eval(), right.eval()); }
            }
            return -1.0;
        }
        public String getTree() {
            if(type == 0) { return "IS"; }
            if(left != null && right != null) {
                if(type == 1) { return "AND [" + left.getTree() + ", " + right.getTree() + "]"; }
                if(type == 2) { return "OR [" + left.getTree() + ", " + right.getTree() + "]"; }
            }
            return "ERROR";
        }
    };
    public static class Rule {
        public Rule() { root = null; }
        public ArrayList<String> tokens = new ArrayList<String>();
        public RuleNode root;
        public RuleNode makeRoot(int beg, int end, FuzzyData data) {
            if(beg > end || end - beg < 2){ JOptionPane.showMessageDialog(null, "Error parsing ruleIF"); return null; }
            int depth = 0;
            for(int i = beg; i <= end; ++i) {
                if(tokens.get(i).equals("("))++depth;
                else if(tokens.get(i).equals(")"))--depth;
                else if(depth == 0 && (tokens.get(i).equals("OR") || tokens.get(i).equals("or"))) return new RuleNode(2, makeRoot(beg, i - 1, data), makeRoot(i + 1, end, data), null, null);
            }
            for(int i = beg; i <= end; ++i) {
                if(tokens.get(i).equals("("))++depth;
                else if(tokens.get(i).equals(")"))--depth;
                else if(depth == 0 && (tokens.get(i).equals("AND") || tokens.get(i).equals("and"))) return new RuleNode(1, makeRoot(beg, i - 1, data), makeRoot(i + 1, end, data), null, null);
            }
            if(tokens.get(beg).equals("(") && tokens.get(end).equals(")")) {
                while(tokens.get(beg).equals("(") && tokens.get(end).equals(")") && beg <= end)
                {  ++beg; --end; }
                return makeRoot(beg, end, data);
            }
            if(end - beg == 2 && (tokens.get(beg + 1).equals("IS") || tokens.get(beg + 1).equals("is"))) {
                if(data.params.get(tokens.get(beg)) == null){ JOptionPane.showMessageDialog(null,"Unknown param " + tokens.get(beg)); return null; }
                if(data.members.get(tokens.get(beg) + tokens.get(end)) == null){ JOptionPane.showMessageDialog(null,"Unknown param member " + tokens.get(beg) + tokens.get(end)); return null; }
                data.params.get(tokens.get(beg)).needInputValue = true;
                return new RuleNode(0, null, null, data.params.get(tokens.get(beg)), data.members.get(tokens.get(beg) + tokens.get(end)));
            }
            JOptionPane.showMessageDialog(null,"KURWA");
            return null;
        }
        public String getDef() { String str = "IF "; for(String s : tokens)str += s + " "; str += "THEN " + outParamNameStr + " IS " + outParamMemberStr; return str; }
        public String outParamNameStr, outParamMemberStr;
        public Member outParamMember;
    }
    public static class FuzzyData {
        public HashMap<String, Param>		params = new HashMap<String, Param>();
        public HashMap<String, Member>		members = new HashMap<String, Member>();
        public ArrayList<Rule>				rules = new ArrayList<Rule>();
        public void reset() {
            params.clear();
            members.clear();
            rules.clear();
        }
        public boolean addParam(String name, Range range) {
            Param param = new Param();
            param.name = name;
            param.range = range;
            param.step = 1;
            param.inVal = param.outVal = 0.0;
            param.asOutput = false;
            param.needInputValue = false;
            params.put(name, param);
            return true;
        }
        public boolean addMember(String paramName, String memberName, Curve curve) {
            Param param = params.get(paramName);
            if(param != null){
                Member member = new Member();
                member.name = memberName;
                member.param = param;
                member.curve = curve;
                member.val = 1;

                param.members.add(member);
                members.put(paramName+memberName, member);

                return true;
            }
            return false;
        }
        public boolean addRule(String ruleIF, String paramName, String memberName) {
            Member outMember = members.get(paramName+memberName);
            if(outMember == null) return false;
            StringTokenizer tokenizer = new StringTokenizer(ruleIF, " \n\t\r");
            Rule rule = new Rule();
            while(tokenizer.hasMoreTokens()) {
                rule.tokens.add(tokenizer.nextToken());
            }
            rule.outParamNameStr = paramName;
            rule.outParamMemberStr = memberName;
            rule.outParamMember = outMember;
            rule.root = rule.makeRoot(0, rule.tokens.size()-1, this);
            if(rule.root == null){ JOptionPane.showMessageDialog(null,"Adding rule failed: root = null"); return false; }
            rules.add(rule);
            outMember.param.asOutput = true;
            return true;
        }
    }
}
