import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

class RoundedButton extends JButton {
    private int radius;
    private Color shadowColor;
    
    public RoundedButton(String text, int radius) {
        super(text);
        this.radius = radius;
        this.shadowColor = new Color(0, 0, 0, 30);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setPreferredSize(new Dimension(120, 40));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2.setColor(shadowColor);
        g2.fillRoundRect(2, 3, getWidth() - 2, getHeight() - 2, radius, radius);
        
        if (getModel().isPressed()) {
            g2.setColor(getBackground().darker());
        } else if (getModel().isRollover()) {
            g2.setColor(getBackground().brighter());
        } else {
            g2.setColor(getBackground());
        }
        
        g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 3, radius, radius);
        g2.dispose();
        
        super.paintComponent(g);
    }
    
    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(255, 255, 255, 100));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(0, 0, getWidth() - 3, getHeight() - 4, radius, radius);
        g2.dispose();
    }
    
    @Override
    public boolean contains(int x, int y) {
        return new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius, radius).contains(x, y);
    }
}

class RoundedBorder extends AbstractBorder {
    private int radius;
    private Color color;

    public RoundedBorder(int radius, Color color) {
        this.radius = radius;
        this.color = color;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        g2.dispose();
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(5, 5, 5, 5);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.left = insets.right = insets.top = insets.bottom = 5;
        return insets;
    }
}

public class SJFAlgo {
    public static void main(String[] args) throws Exception {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Could not set system look and feel: " + e.getMessage());
        }
        SwingUtilities.invokeLater(SJFAlgo::new);
    }

    private JFrame frame;
    private JComboBox<Integer> processCountCombo;
    private RoundedButton setProcessesBtn, computeBtn, resetBtn;
    private JTable processTable;
    private DefaultTableModel tableModel;
    private JPanel ganttPanel, resultPanel;
    private JTextArea resultArea;
    private GanttChartPanel ganttChartPanel;
    private int processCount = 3;
    private boolean isComputed = false;

    public SJFAlgo() {
        frame = new JFrame("SJF Scheduling Algorithm");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 850);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(15, 15));
        
        // Gradient background
        frame.setContentPane(new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(240, 248, 255),
                    0, getHeight(), new Color(230, 240, 250)
                );
                g2.setPaint(gradient);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        });

        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setProcessRows(processCount);

        frame.setResizable(false);
        frame.setVisible(true);
    }
    
    private void initializeComponents() {
        Integer[] processOptions = {3, 4, 5};
        processCountCombo = new JComboBox<>(processOptions);
        processCountCombo.setSelectedItem(3);
        processCountCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        processCountCombo.setPreferredSize(new Dimension(80, 35));

        setProcessesBtn = new RoundedButton("Set Processes", 20);
        setProcessesBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        setProcessesBtn.setBackground(new Color(100, 149, 237));
        setProcessesBtn.setForeground(Color.WHITE);
        setProcessesBtn.setMaximumSize(new Dimension(140, 35));

        computeBtn = new RoundedButton("Compute", 25);
        computeBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        computeBtn.setBackground(new Color(46, 139, 87));
        computeBtn.setForeground(Color.WHITE);
        computeBtn.setToolTipText("Calculate SJF scheduling results");
        computeBtn.setPreferredSize(new Dimension(130, 45));
        computeBtn.setMaximumSize(new Dimension(130, 45));

        resetBtn = new RoundedButton("Try Again", 25);
        resetBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        resetBtn.setBackground(new Color(220, 20, 60));
        resetBtn.setForeground(Color.WHITE);
        resetBtn.setToolTipText("Clear all data");
        resetBtn.setPreferredSize(new Dimension(130, 45));
        resetBtn.setMaximumSize(new Dimension(130, 45));
        resetBtn.setEnabled(false);

        String[] columns = {"Process", "AT", "BT", "ET", "TAT", "WT"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return !isComputed && (column == 1 || column == 2);
            }
        };
        
        processTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                
                c.setForeground(Color.BLACK);

                if (!isRowSelected(row)) {
                    if (column <= 2) {
                        c.setBackground(row % 2 == 0 ? new Color(255, 255, 255) : new Color(248, 252, 255));
                    } else {
                        c.setBackground(row % 2 == 0 ? new Color(240, 255, 240) : new Color(230, 250, 230));
                    }
                } else {
                    c.setForeground(Color.BLACK);
                }

                return c;
            }
        };
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                setHorizontalAlignment(JLabel.CENTER);
                setForeground(Color.BLACK);

                if (column > 2) {
                    setFont(new Font("Segoe UI", Font.BOLD, 13));
                } else {
                    setFont(new Font("Segoe UI", Font.PLAIN, 13));
                }

                return c;
            }
        };

        for (int i = 0; i < processTable.getColumnCount(); i++) {
            processTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            if (i == 0) {
                processTable.getColumnModel().getColumn(i).setPreferredWidth(80);
            } else {
                processTable.getColumnModel().getColumn(i).setPreferredWidth(100);
            }
        }
        
        processTable.setRowHeight(32);
        processTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        processTable.getTableHeader().setBackground(new Color(100, 149, 237));
        processTable.getTableHeader().setForeground(Color.BLACK);
        processTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        processTable.setGridColor(new Color(200, 220, 240));
        processTable.setSelectionBackground(new Color(184, 207, 229));

        resultArea = new JTextArea(6, 60);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        resultArea.setBackground(new Color(248, 248, 255));
        resultArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Gantt chart panel
        ganttChartPanel = new GanttChartPanel();
    }
    
    private void setupLayout() {
        // Main container with consistent margins
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Top Panel (Input) - Fixed height
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));
        inputPanel.setOpaque(false);
        inputPanel.setBorder(new CompoundBorder(
            new RoundedBorder(20, new Color(100, 149, 237)),
            new EmptyBorder(20, 25, 20, 25)
        ));
        inputPanel.setPreferredSize(new Dimension(850, 80));
        inputPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        
        JLabel titleLabel = new JLabel("SJF Process Scheduler");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(25, 25, 112));
        
        JLabel processLabel = new JLabel("Number of Processes:");
        processLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        processLabel.setForeground(new Color(25, 25, 112));
        
        processCountCombo.setMaximumSize(new Dimension(80, 35));
        
        inputPanel.add(titleLabel);
        inputPanel.add(Box.createHorizontalGlue());
        inputPanel.add(processLabel);
        inputPanel.add(Box.createHorizontalStrut(10));
        inputPanel.add(processCountCombo);
        inputPanel.add(Box.createHorizontalStrut(15));
        inputPanel.add(setProcessesBtn);
        
        JPanel centerSection = new JPanel(new GridLayout(2, 1, 0, 20));
        centerSection.setOpaque(false);
        
        JScrollPane tableScroll = new JScrollPane(processTable);
        tableScroll.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                new RoundedBorder(15, new Color(100, 149, 237)),
                "Process Information Table",
                TitledBorder.CENTER, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(25, 25, 112)
            ),
            new EmptyBorder(10, 10, 10, 10)
        ));
        tableScroll.setPreferredSize(new Dimension(850, 200));
        
        ganttPanel = new JPanel(new BorderLayout());
        ganttPanel.setOpaque(false);
        ganttPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                new RoundedBorder(15, new Color(100, 149, 237)),
                "Gantt Chart Visualization", 
                TitledBorder.CENTER, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(25, 25, 112)
            ),
            new EmptyBorder(10, 10, 10, 10)
        ));
        ganttPanel.setPreferredSize(new Dimension(850, 200));
        ganttPanel.add(ganttChartPanel, BorderLayout.CENTER);
        
        centerSection.add(tableScroll);
        centerSection.add(ganttPanel);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setOpaque(false);
        buttonPanel.setPreferredSize(new Dimension(850, 60));
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(computeBtn);
        buttonPanel.add(Box.createHorizontalStrut(40));
        buttonPanel.add(resetBtn);
        buttonPanel.add(Box.createHorizontalGlue());
        
        resultPanel = new JPanel(new BorderLayout());
        resultPanel.setOpaque(false);
        JScrollPane resultScroll = new JScrollPane(resultArea);
        resultScroll.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                new RoundedBorder(15, new Color(46, 139, 87)),
                "Calculation Results",
                TitledBorder.CENTER, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(25, 100, 25)
            ),
            new EmptyBorder(10, 10, 10, 10)
        ));
        resultPanel.add(resultScroll, BorderLayout.CENTER);
        resultPanel.setPreferredSize(new Dimension(850, 150));
        
        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(centerSection, BorderLayout.CENTER);
        
        JPanel bottomSection = new JPanel(new BorderLayout(0, 15));
        bottomSection.setOpaque(false);
        bottomSection.add(buttonPanel, BorderLayout.NORTH);
        bottomSection.add(resultPanel, BorderLayout.CENTER);
        
        mainPanel.add(bottomSection, BorderLayout.SOUTH);
        frame.add(mainPanel, BorderLayout.CENTER);
    }
    
    private void setupEventHandlers() {
        setProcessesBtn.addActionListener(e -> {
            if (!isComputed) {
                processCount = (Integer) processCountCombo.getSelectedItem();
                setProcessRows(processCount);
            }
        });

        computeBtn.addActionListener(e -> computeSJF());
        resetBtn.addActionListener(e -> resetWithConfirmation());
    }

    private void setProcessRows(int count) {
        tableModel.setRowCount(0);
        for (int i = 1; i <= count; i++) {
            tableModel.addRow(new Object[]{"P" + i, "", "", "", "", ""});
        }
        ganttChartPanel.clearChart();
        resultArea.setText("Ready to compute! Please enter Arrival Time (AT) and Burst Time (BT) for each process.");
        if (processTable.getRowCount() > 0) {
            processTable.requestFocus();
            processTable.changeSelection(0, 1, false, false);
        }
    }

    private void computeSJF() {
        int n = tableModel.getRowCount();
        List<Process> processes = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            try {
                Object atObj = tableModel.getValueAt(i, 1);
                Object btObj = tableModel.getValueAt(i, 2);
                String atStr = (atObj == null) ? "" : atObj.toString().trim();
                String btStr = (btObj == null) ? "" : btObj.toString().trim();

                if (atStr.isEmpty() || btStr.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "All AT and BT fields must be filled!", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int at = Integer.parseInt(atStr);
                int bt = Integer.parseInt(btStr);

                if (at < 0 || bt <= 0) {
                    JOptionPane.showMessageDialog(frame, "AT must be >= 0 and BT must be > 0!", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                processes.add(new Process(i, "P" + (i+1), at, bt));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Please enter valid Arrival and Burst Times for all processes.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        List<Process> completed = new ArrayList<>();
        int time = 0;
        boolean[] done = new boolean[n];
        int completedCount = 0;
        List<GanttBlock> ganttBlocks = new ArrayList<>();

        while (completedCount < n) {
            int idx = -1;
            int minBT = Integer.MAX_VALUE;
            for (int i = 0; i < n; i++) {
                Process p = processes.get(i);
                if (!done[i] && p.at <= time) {
                    if (p.bt < minBT || (p.bt == minBT && p.at < processes.get(idx == -1 ? i : idx).at)) {
                        minBT = p.bt;
                        idx = i;
                    }
                }
            }
            if (idx == -1) {
                time++;
                continue;
            }
            Process p = processes.get(idx);
            int startTime = time;
            p.ct = time + p.bt;
            p.tat = p.ct - p.at;
            p.wt = p.tat - p.bt;
            time = p.ct;
            done[idx] = true;
            completed.add(p);
            completedCount++;

            ganttBlocks.add(new GanttBlock(p.name, startTime, p.ct, getProcessColor(idx)));
        }

        completed.sort(Comparator.comparingInt(proc -> proc.id));
        for (int i = 0; i < n; i++) {
            Process p = completed.get(i);
            tableModel.setValueAt(p.ct, i, 3);
            tableModel.setValueAt(p.tat, i, 4);
            tableModel.setValueAt(p.wt, i, 5);
        }

        ganttChartPanel.setGanttBlocks(ganttBlocks);

        double avgTAT = completed.stream().mapToInt(p -> p.tat).average().orElse(0);
        double avgWT = completed.stream().mapToInt(p -> p.wt).average().orElse(0);

        resultArea.setText(String.format(
            "Results:\nAverage Turn Around Time: %.2f\nAverage Waiting Time: %.2f",
            avgTAT, avgWT));

        isComputed = true;
        computeBtn.setEnabled(false);
        resetBtn.setEnabled(true);
        setProcessesBtn.setEnabled(false);
        processCountCombo.setEnabled(false);
        processTable.repaint();
    }

    private void resetWithConfirmation() {
        int result = JOptionPane.showConfirmDialog(
                frame,
                "Are you sure you want to reset all data and start again?",
                "Confirm Reset",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            resetAll();
        }
    }

    private void resetAll() {
        isComputed = false;
        setProcessRows(processCount);
        computeBtn.setEnabled(true);
        resetBtn.setEnabled(false);
        setProcessesBtn.setEnabled(true);
        processCountCombo.setEnabled(true);
        processTable.repaint();
        resultArea.setText("System reset! Select number of processes to begin.");
    }

    private Color getProcessColor(int processIndex) {
        Color[] colors = {
            new Color(255, 99, 132),   // Red
            new Color(54, 162, 235),   // Blue  
            new Color(255, 205, 86),   // Yellow
            new Color(75, 192, 192),   // Teal
            new Color(153, 102, 255)   // Purple
        };
        return colors[processIndex % colors.length];
    }

    private static class Process {
        int id;
        String name;
        int at, bt, ct, tat, wt;
        Process(int id, String name, int at, int bt) {
            this.id = id;
            this.name = name;
            this.at = at;
            this.bt = bt;
        }
    }

    private static class GanttBlock {
        String processName;
        int startTime;
        int endTime;
        Color color;

        GanttBlock(String processName, int startTime, int endTime, Color color) {
            this.processName = processName;
            this.startTime = startTime;
            this.endTime = endTime;
            this.color = color;
        }
    }

    private class GanttChartPanel extends JPanel {
        private List<GanttBlock> ganttBlocks;
        private final int BLOCK_HEIGHT = 50;
        private final int MARGIN = 20;

        public GanttChartPanel() {
            setBackground(Color.WHITE);
            ganttBlocks = new ArrayList<>();
        }

        public void setGanttBlocks(List<GanttBlock> blocks) {
            this.ganttBlocks = blocks;
            repaint();
        }

        public void clearChart() {
            ganttBlocks.clear();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            if (ganttBlocks == null || ganttBlocks.isEmpty()) {
                g.setColor(Color.GRAY);
                g.setFont(new Font("Segoe UI", Font.ITALIC, 14));
                FontMetrics fm = g.getFontMetrics();
                String message = "Gantt chart will appear here after computation";
                int x = (getWidth() - fm.stringWidth(message)) / 2;
                int y = getHeight() / 2;
                g.drawString(message, x, y);
                return;
            }
            
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int chartHeight = BLOCK_HEIGHT;
            int chartY = (getHeight() - chartHeight) / 2 - 10;
            int maxTime = ganttBlocks.get(ganttBlocks.size() - 1).endTime;
            double scale = (double) (getWidth() - 2 * MARGIN) / maxTime;
            
            for (GanttBlock block : ganttBlocks) {
                int x = MARGIN + (int) (block.startTime * scale);
                int width = (int) ((block.endTime - block.startTime) * scale);
                
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillRoundRect(x + 2, chartY + 2, width, chartHeight, 10, 10);
                
                g2.setColor(block.color);
                g2.fillRoundRect(x, chartY, width, chartHeight, 10, 10);
                
                g2.setColor(block.color.darker());
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(x, chartY, width, chartHeight, 10, 10);
                
                g2.setColor(Color.BLACK);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                int textX = x + (width - fm.stringWidth(block.processName)) / 2;
                int textY = chartY + (chartHeight + fm.getHeight()) / 2 - 2;
                g2.drawString(block.processName, textX, textY);
            }
            
            g2.setColor(Color.DARK_GRAY);
            g2.setStroke(new BasicStroke(1));
            int timelineY = chartY + chartHeight + 15;
            g2.drawLine(MARGIN, timelineY, getWidth() - MARGIN, timelineY);
            
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            for (GanttBlock block : ganttBlocks) {
                int x = MARGIN + (int) (block.startTime * scale);
                g2.drawLine(x, timelineY - 5, x, timelineY + 5);
                g2.drawString(String.valueOf(block.startTime), x - 5, timelineY + 20);
            }

            GanttBlock lastBlock = ganttBlocks.get(ganttBlocks.size() - 1);
            int finalX = MARGIN + (int) (lastBlock.endTime * scale);
            g2.drawLine(finalX, timelineY - 5, finalX, timelineY + 5);
            g2.drawString(String.valueOf(lastBlock.endTime), finalX - 5, timelineY + 20);
            
            g2.dispose();
        }
    }
}
