package com.adasm.fuzzy;

/*******************************
 By Adam Michalowski (c) 2012
 *******************************/

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import javax.swing.*;

public class Test extends JFrame { private static final long serialVersionUID = 1L;
    public static FuzzyControl fc = new FuzzyControl();
    public static JButton buttonStartStop;
    public static JTextArea debugTextArea, scriptTextArea;
    public static boolean simulationOn = false;
    public static class ChartMember extends JPanel { private static final long serialVersionUID = 1L;
        public Base.Member member;
        public JLabel label;
        public int xCheck;
        public ChartMember(Base.Member m){
            member = m;
            setToolTipText("");
            setName(member.param.name + " MEMBER: "+ member.name);
            xCheck = 0;
            label = new JLabel("");
            label.setForeground(Color.blue);
            label.setFont(new Font("Lucida Console", Font.BOLD, 14));
            add(label);
            addMouseMotionListener(new MouseMotionListener() {
                public void mouseMoved(MouseEvent evt) {
                    xCheck = evt.getX();
                    repaint();
                }
                public void mouseDragged(MouseEvent evt) { }});
        }
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(Color.cyan);
            Dimension size = getSize();
            Insets insets = getInsets();

            double w = size.width - insets.left - insets.right - 1;
            double h = size.height - insets.top - insets.bottom - 1;
            int H = (int)h;
            double val = 0;
            if(member.param.asOutput == true)
                val = member.val;
            else
                val = member.curve.getY(member.param.inVal);

            for(int X = 0; X < w; X++) {
                double x = ((double)X / w) * Math.abs(member.param.range.b - member.param.range.a) + member.param.range.a;
                if(x < member.curve.a || x > member.curve.d)continue;
                double ymax = member.curve.getY(x);
                double ymin = Math.min(ymax, val);
                int Ymin = (int)((1-ymin)*h);
                int Ymax = (int)((1-ymax)*h);

                g2d.setColor(Color.gray);
                g2d.drawLine(X, H, X, Ymax);
                g2d.setColor(Color.black);
                g2d.drawLine(X, H, X, Ymin);
            }

            if(member.param.asOutput == true) {
                g2d.setColor(Color.green);
                int y = (int)((1.0 - val) * h);
                g2d.drawLine(0, y-1, (int)w, y-1);
                g2d.drawLine(0, y, (int)w, y);
                g2d.drawLine(0, y+1, (int)w, y+1);
                setToolTipText("clipY = " + val);
            }
            else {
                g2d.setColor(Color.green);
                int outX = (int)( w * ((member.param.inVal - member.param.range.a)/(member.param.range.b - member.param.range.a)) );
                g2d.drawLine(outX-1, H, outX-1, 0);
                g2d.drawLine(outX, H, outX, 0);
                g2d.drawLine(outX+1, H, outX+1, 0);
                setToolTipText(member.param.name + " = " + member.param.inVal);
            }

            double x = ((double)xCheck / w) * Math.abs(member.param.range.b - member.param.range.a) + member.param.range.a;
            double ymax = member.curve.getY(x);
            double ymin = Math.min(ymax, val);
            int Ymin = (int)((1-ymin)*h);
            int Ymax = (int)((1-ymax)*h);
            label.setText("X = " + x + "  Ymin = " + ymin + "  Ymax = " + ymax);
            g2d.setColor(Color.lightGray);
            g2d.drawLine(xCheck-1, H, xCheck-1, Ymax);
            g2d.drawLine(xCheck, H, xCheck, Ymax);
            g2d.drawLine(xCheck+1, H, xCheck+1, Ymax);
            g2d.setColor(Color.gray);
            g2d.drawLine(xCheck-1, H, xCheck-1, Ymin);
            g2d.drawLine(xCheck, H, xCheck, Ymin);
            g2d.drawLine(xCheck+1, H, xCheck+1, Ymin);

            g2d.setColor(Color.black);
            g2d.drawRect(0, 0, (int)w, (int)h);


        }
    }
    public static class ChartParam extends JPanel { private static final long serialVersionUID = 1L;
        public Base.Param param;
        public JLabel label;
        public int xCheck;
        public ChartParam(Base.Param p){
            param = p;
            setToolTipText(param.name + " Final");
            setName(param.name + " Final");
            xCheck = 0;
            label = new JLabel("");
            label.setForeground(Color.black);
            label.setFont(new Font("Lucida Console", Font.BOLD, 14));
            add(label);
            addMouseMotionListener(new MouseMotionListener() {
                public void mouseMoved(MouseEvent evt) {
                    xCheck = evt.getX();
                    repaint();
                }
                public void mouseDragged(MouseEvent evt) { }});
        }
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(Color.blue);
            Dimension size = getSize();
            Insets insets = getInsets();

            double w = size.width - insets.left - insets.right - 1;
            double h = size.height - insets.top - insets.bottom - 1;
            int H = (int)h;

            for(int X = 0; X < w; X++) {
                double x = ((double)X / w) * Math.abs(param.range.b - param.range.a) + param.range.a;
                double ymax = 0;
                double ymin = 0;
                for(Base.Member member : param.members) {
                    if(x >= member.curve.a || x <= member.curve.d) {
                        ymax = Math.max(ymax, member.curve.getY(x));
                        ymin = Math.max(ymin, Math.min(member.curve.getY(x), member.val));
                    }
                }

                int Ymin = (int)((1-ymin)*h);
                int Ymax = (int)((1-ymax)*h);

                g2d.setColor(Color.gray);
                g2d.drawLine(X, H, X, Ymax);
                g2d.setColor(Color.blue);
                g2d.drawLine(X, H, X, Ymin);
            }

            g2d.setColor(Color.red);
            int outX = (int)( w * ((param.outVal - param.range.a)/Math.abs(param.range.b - param.range.a)) );
            g2d.drawLine(outX-1, H, outX-1, 0);
            g2d.drawLine(outX, H, outX, 0);
            g2d.drawLine(outX+1, H, outX+1, 0);

            double x = ((double)xCheck / w) * Math.abs(param.range.b - param.range.a) + param.range.a;
            double ymax = 0;
            double ymin = 0;

            for(Base.Member member : param.members) {
                if(x >= member.curve.a || x <= member.curve.d) {
                    ymax = Math.max(ymax, member.curve.getY(x));
                    ymin = Math.max(ymin, Math.min(member.curve.getY(x), member.val));
                }
            }
            int Ymin = (int)((1-ymin)*h);
            int Ymax = (int)((1-ymax)*h);
            label.setText("X = " + x + "  Ymin = " + ymin + "  Ymax = " + ymax);
            g2d.setColor(Color.lightGray);
            g2d.drawLine(xCheck-1, H, xCheck-1, Ymax);
            g2d.drawLine(xCheck, H, xCheck, Ymax);
            g2d.drawLine(xCheck+1, H, xCheck+1, Ymax);
            g2d.setColor(Color.gray);
            g2d.drawLine(xCheck-1, H, xCheck-1, Ymin);
            g2d.drawLine(xCheck, H, xCheck, Ymin);
            g2d.drawLine(xCheck+1, H, xCheck+1, Ymin);

            g2d.setColor(Color.black);
            g2d.drawRect(0, 0, (int)w, (int)h);

            setToolTipText(param.name + " Final output: " + param.outVal);
        }
    }

    public static class EditParam extends JPanel { private static final long serialVersionUID = 1L;
        public Base.Param param;
        public JTextField textField = null;
        public String lastText = "";
        public EditParam(Base.Param p){
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
            param = p;
            setToolTipText(param.name);
            setName(param.name);
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            add(new JLabel(param.name + " [" + param.range.a+ ", " + param.range.b + "]  "));
            textField = new JTextField(20);
            textField.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    String text = textField.getText();
                    if(text.equals(lastText))return;
                    lastText = text;
                    param.inVal = Double.parseDouble(lastText);
                    repaint();
                } });
            add(textField);
        }
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            if(textField != null) textField.setText(""+param.inVal);
            setToolTipText(param.name + " INPUT: " + param.inVal + " OUTPUT: " + param.outVal);
            textField.setEnabled(!simulationOn);	    }
    }

    public static class HistoryChartMember extends JPanel { private static final long serialVersionUID = 1L;
        public Base.Member member;
        public JLabel label;
        public int xCheck;
        public HistoryChartMember(Base.Member m){
            member = m;
            xCheck = 0;
            label = new JLabel("Val = ?");
            label.setForeground(Color.darkGray);
            label.setFont(new Font("Lucida Console", Font.BOLD, 14));
            add(label);
            addMouseMotionListener(new MouseMotionListener() {
                public void mouseMoved(MouseEvent evt) {
                    xCheck = evt.getX();
                    repaint();
                }
                public void mouseDragged(MouseEvent evt) { }});
        }
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(Color.blue);
            Dimension size = getSize();
            Insets insets = getInsets();
            int w = size.width - insets.left - insets.right - 1;
            int h = size.height - insets.top - insets.bottom - 1;
            int i = member.valHistory.size() - 1;
            for(int X = w-1; X >= 0 && i >= 0 ; --X, --i) {
                double y =  ((double)h) * (1.0-member.valHistory.get(i));
                g2d.setColor(Color.gray);
                g2d.drawLine(X, h, X, (int)y);
            }

            i = member.valHistory.size() - 1 - (w - 1 - xCheck);
            if(i >= 0 && i < member.valHistory.size()) {
                g2d.setColor(Color.red);
                g2d.drawLine(xCheck, 0, xCheck, (int)h);
                label.setText("Val = " + member.valHistory.get(i));
            }
            else label.setText("Val = ?");
        }
    }

    public static class HistoryChartParamOut extends JPanel { private static final long serialVersionUID = 1L;
        public Base.Param param;
        public JLabel label;
        public int xCheck;
        public HistoryChartParamOut(Base.Param p){
            param = p;
            xCheck = 0;
            label = new JLabel("Val = ?");
            label.setForeground(Color.darkGray);
            label.setFont(new Font("Lucida Console", Font.BOLD, 14));
            add(label);
            addMouseMotionListener(new MouseMotionListener() {
                public void mouseMoved(MouseEvent evt) {
                    xCheck = evt.getX();
                    repaint();
                }
                public void mouseDragged(MouseEvent evt) { }});
        }
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(Color.blue);
            Dimension size = getSize();
            Insets insets = getInsets();
            int w = size.width - insets.left - insets.right - 1;
            int h = size.height - insets.top - insets.bottom - 1;
            int i = param.outValHistory.size() - 1;
            for(int X = w-1; X >= 0 && i >= 0 ; --X, --i) {
                double y =  ((double)h) * (1.0-(param.outValHistory.get(i) - param.range.a)/(param.range.b - param.range.a));
                g2d.setColor(Color.gray);
                g2d.drawLine(X, h, X, (int)y);
            }
            i = param.outValHistory.size() - 1 - (w - 1 - xCheck);
            if(i >= 0 && i < param.outValHistory.size()) {
                g2d.setColor(Color.red);
                g2d.drawLine(xCheck, 0, xCheck, (int)h);
                label.setText("Val = " + param.outValHistory.get(i));
            }
            else label.setText("Val = ?");
        }
    }

    public static class HistoryChartParamIn extends JPanel { private static final long serialVersionUID = 1L;
        public Base.Param param;
        public JLabel label;
        public int xCheck;
        public HistoryChartParamIn(Base.Param p){
            param = p;
            xCheck = 0;
            label = new JLabel("Val = ?");
            label.setForeground(Color.darkGray);
            label.setFont(new Font("Lucida Console", Font.BOLD, 14));
            add(label);
            addMouseMotionListener(new MouseMotionListener() {
                public void mouseMoved(MouseEvent evt) {
                    xCheck = evt.getX();
                    repaint();
                }
                public void mouseDragged(MouseEvent evt) { }});
        }
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(Color.blue);
            Dimension size = getSize();
            Insets insets = getInsets();
            int w = size.width - insets.left - insets.right - 1;
            int h = size.height - insets.top - insets.bottom - 1;
            int i = param.inValHistory.size() - 1;
            for(int X = w-1; X >= 0 && i >= 0 ; --X, --i) {
                double y =  ((double)h) * (1.0-(param.inValHistory.get(i) - param.range.a)/(param.range.b - param.range.a));
                g2d.setColor(Color.gray);
                g2d.drawLine(X, h, X, (int)y);
            }
            i = param.inValHistory.size() - 1 - (w - 1 - xCheck);
            if(i >= 0 && i < param.inValHistory.size()) {
                g2d.setColor(Color.red);
                g2d.drawLine(xCheck, 0, xCheck, (int)h);
                label.setText("Val = " + param.inValHistory.get(i));
            }
            else label.setText("Val = ?");
        }
    }

    public Test() {
        fc.init();
        initUI();
        fc.resetAll();
    }
    public void initUI() {
        ToolTipManager.sharedInstance().setInitialDelay(0);
        ToolTipManager.sharedInstance().setDismissDelay(3000);
        JTabbedPane container= new JTabbedPane();
        container.setFont(new Font("Arial", Font.BOLD, 12));
        //SETTINGS
        {
            JPanel paramPanel = new JPanel();
            paramPanel.setLayout(new BoxLayout(paramPanel, BoxLayout.PAGE_AXIS));

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
            buttonStartStop = new JButton("Start Random Input Testing");
            buttonStartStop.setActionCommand("start");
            buttonStartStop.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    if("start".equals(evt.getActionCommand())) {
                        simulationOn = true;
                        buttonStartStop.setText("Stop Random Input Testing");
                        buttonStartStop.setActionCommand("stop");
                        repaint();
                    }
                    else if("stop".equals(evt.getActionCommand())) {
                        simulationOn = false;
                        buttonStartStop.setText("Start Random Input Testing");
                        buttonStartStop.setActionCommand("start");
                        repaint();
                    }
                } } );
            buttonPanel.add(buttonStartStop);
            JButton buttonRun = new JButton("Execute FC");
            buttonRun.setActionCommand("run");
            buttonRun.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    if("run".equals(evt.getActionCommand())) {
                        fc.run(false);
                        repaint();
                    }
                } } );
            buttonPanel.add(buttonRun);
            JButton resetOutput = new JButton("Reset Output");
            resetOutput.setActionCommand("resetout");
            resetOutput.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    if("resetout".equals(evt.getActionCommand())) {
                        fc.resetOutput();
                        repaint();
                    }
                } } );
            buttonPanel.add(resetOutput);
            JButton resetALl = new JButton("Reset All");
            resetALl.setActionCommand("resetall");
            resetALl.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    if("resetall".equals(evt.getActionCommand())) {
                        fc.resetAll();
                        repaint();
                    }
                } } );
            buttonPanel.add(resetALl);
            paramPanel.add(buttonPanel);

            paramPanel.add(new JLabel("Input params:"));
            JPanel editPanel = new JPanel();
            editPanel.setLayout(new BoxLayout(editPanel, BoxLayout.PAGE_AXIS));
            for(Base.Param param : fc.data.params.values()) {
                if(param.needInputValue == true)
                    editPanel.add(new EditParam(param));
            }

            paramPanel.add(editPanel);
            container.addTab("####", paramPanel);
        }
        // DEBUG TAB
        {
            debugTextArea = new JTextArea(5, 20);
            JScrollPane scrollPane = new JScrollPane(debugTextArea);
            debugTextArea.setEditable(false);
            debugTextArea.setFont(new Font("Lucida Console", Font.TRUETYPE_FONT, 10));
            debugTextArea.append("### DEBUG OUTPUT ###\n");
            for(Base.Rule rule : fc.data.rules) {
                debugTextArea.append("\nRule  " + rule.outParamNameStr + rule.outParamMemberStr);
                debugTextArea.append("\n - Def: " + rule.getDef());
                debugTextArea.append("\n - root.getTree() = " + rule.root.getTree());
            }

            container.addTab("DEBUG", scrollPane);
        }
        // SCRIPT TAB
        {
            scriptTextArea = new JTextArea(5, 20);
            JScrollPane scrollPane = new JScrollPane(scriptTextArea);
            scriptTextArea.setEditable(true);
            scriptTextArea.setFont(new Font("Lucida Console", Font.TRUETYPE_FONT, 10));
            scriptTextArea.append(fc.getScript());
            container.addTab("SCRIPT", scrollPane);
        }
        // PARAMS
        JTabbedPane currentTab = new JTabbedPane();
        currentTab.setFont(new Font("Arial", Font.BOLD, 12));
        for(Base.Param param : fc.data.params.values()) {
            JPanel paramPanel = new JPanel();
            paramPanel.setLayout(new BoxLayout(paramPanel, BoxLayout.PAGE_AXIS));
            for(Base.Member member : param.members) {
                JPanel chartPanel = new JPanel();
                chartPanel.setBorder(BorderFactory.createTitledBorder(member.param.name+member.name + " Param[" + param.range.a + ", " + param.range.b + "] Member[" + member.curve.a + ", " + member.curve.d + "]"));
                chartPanel.setLayout(new BoxLayout(chartPanel, BoxLayout.PAGE_AXIS));
                chartPanel.add(new ChartMember(member));
                paramPanel.add(chartPanel);
            }
            if(param.asOutput) {
                JPanel chartPanel = new JPanel();
                chartPanel.setBorder(BorderFactory.createTitledBorder(param.name + " OUTPUT"));
                chartPanel.setLayout(new BoxLayout(chartPanel, BoxLayout.PAGE_AXIS));
                chartPanel.add(new ChartParam(param));
                paramPanel.add(chartPanel);
            }
            currentTab.addTab(param.name + ((param.asOutput)?(" OUT"):("")) + ((param.needInputValue)?(" IN"):("")), paramPanel);
        }
        container.addTab("Current", currentTab);
        // HISTORY
        JTabbedPane historyTab = new JTabbedPane();
        historyTab.setFont(new Font("Arial", Font.BOLD, 12));
        JPanel inputParamsPanel = new JPanel();
        inputParamsPanel.setLayout(new BoxLayout(inputParamsPanel, BoxLayout.PAGE_AXIS));
        for(Base.Param param : fc.data.params.values()) {
            if(param.asOutput == true) {
                JPanel historyPanel = new JPanel();
                int gridSize = (int)Math.sqrt((double)param.members.size());
                historyPanel.setLayout(new GridLayout(gridSize+1, gridSize));
                for(Base.Member member : param.members) {
                    JPanel chartPanel = new JPanel();
                    chartPanel.setBorder(BorderFactory.createTitledBorder(member.param.name+member.name));
                    chartPanel.setLayout(new BoxLayout(chartPanel, BoxLayout.PAGE_AXIS));
                    chartPanel.add(new HistoryChartMember(member));
                    historyPanel.add(chartPanel);
                }

                JPanel chartPanel = new JPanel();
                chartPanel.setBorder(BorderFactory.createTitledBorder(param.name));
                chartPanel.setLayout(new BoxLayout(chartPanel, BoxLayout.PAGE_AXIS));
                chartPanel.add(new HistoryChartParamOut(param));
                historyPanel.add(chartPanel);

                historyTab.addTab(param.name, historyPanel);
            }
            else {
                JPanel chartPanel = new JPanel();
                chartPanel.setBorder(BorderFactory.createTitledBorder(param.name));
                chartPanel.setLayout(new BoxLayout(chartPanel, BoxLayout.PAGE_AXIS));
                chartPanel.add(new HistoryChartParamIn(param));
                inputParamsPanel.add(chartPanel);
            }
        }

        historyTab.addTab("Input Params", inputParamsPanel);
        container.addTab("History", historyTab);
        add(container);
        setSize(960, 550);
        setTitle("FuzzyControl 1.5 - Michalowski Adam (c) 2012");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        validate();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                final Test ex = new Test();
                ex.setVisible(true);
                ex.setResizable(true);
                new Timer(33, new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        if(simulationOn == true) {
                            fc.run(simulationOn);
                            ex.repaint();
                        }
                    } }).start();
            }
        });
    }
}

