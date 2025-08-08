import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.geom.*;

// Custom round button class with enhanced styling
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
        
        // Shadow effect
        g2.setColor(shadowColor);
        g2.fillRoundRect(2, 3, getWidth() - 2, getHeight() - 2, radius, radius);
        
        // Button background
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

// Custom panel for Gantt chart visualization
class GanttChartPanel extends JPanel {
    private String[] processIds;
    private int[] startTimes;
    private int[] completionTimes;
    private Color[] processColors;
    
    public GanttChartPanel() {
        setPreferredSize(new Dimension(700, 120));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 149, 237), 2),
                "Gantt Chart Visualization",
                TitledBorder.CENTER, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(100, 149, 237)
            ),
            new EmptyBorder(10, 10, 10, 10)
        ));
    }
    
    public void updateChart(String[] pids, int[] st, int[] ct) {
        this.processIds = pids;
        this.startTimes = st;
        this.completionTimes = ct;
        
        // Generate distinct colors for each process
        this.processColors = new Color[pids.length];
        for (int i = 0; i < pids.length; i++) {
            float hue = (float) i / pids.length;
            this.processColors[i] = Color.getHSBColor(hue, 0.7f, 0.9f);
        }
        
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (processIds == null || processIds.length == 0) {
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
        
        int margin = 20;
        int chartHeight = 50;
        int chartY = (getHeight() - chartHeight) / 2 - 10;
        int maxTime = completionTimes[completionTimes.length - 1];
        double scale = (double) (getWidth() - 2 * margin) / maxTime;
        
        // Draw process blocks
        for (int i = 0; i < processIds.length; i++) {
            int x = margin + (int) (startTimes[i] * scale);
            int width = (int) ((completionTimes[i] - startTimes[i]) * scale);
            
            // Draw shadow
            g2.setColor(new Color(0, 0, 0, 30));
            g2.fillRoundRect(x + 2, chartY + 2, width, chartHeight, 10, 10);
            
            // Draw main block
            g2.setColor(processColors[i]);
            g2.fillRoundRect(x, chartY, width, chartHeight, 10, 10);
            
            // Draw border
            g2.setColor(processColors[i].darker());
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(x, chartY, width, chartHeight, 10, 10);
            
            // Draw process ID
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            FontMetrics fm = g2.getFontMetrics();
            int textX = x + (width - fm.stringWidth(processIds[i])) / 2;
            int textY = chartY + (chartHeight + fm.getHeight()) / 2 - 2;
            g2.drawString(processIds[i], textX, textY);
        }
        
        // Draw timeline
        g2.setColor(Color.DARK_GRAY);
        g2.setStroke(new BasicStroke(1));
        int timelineY = chartY + chartHeight + 15;
        g2.drawLine(margin, timelineY, getWidth() - margin, timelineY);
        
        // Draw time markers
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        for (int i = 0; i < processIds.length; i++) {
            int x = margin + (int) (startTimes[i] * scale);
            g2.drawLine(x, timelineY - 5, x, timelineY + 5);
            g2.drawString(String.valueOf(startTimes[i]), x - 5, timelineY + 20);
        }
        
        // Draw final time marker
        int finalX = margin + (int) (maxTime * scale);
        g2.drawLine(finalX, timelineY - 5, finalX, timelineY + 5);
        g2.drawString(String.valueOf(maxTime), finalX - 5, timelineY + 20);
        
        g2.dispose();
    }
    
    public void clearChart() {
        this.processIds = null;
        repaint();
    }
}

public class SNFN2 extends JFrame {
    
    private JComboBox<Integer> processCountCombo;
    private JTable table;
    private DefaultTableModel model;
    private JTextArea resultArea;
    private GanttChartPanel ganttPanel;
    private RoundedButton computeButton, resetButton;
    private int processCount = 0;
    
    public SNFN2() {
        setTitle("FCFS Scheduling Algorithm");
        setSize(900, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15));
        
        // Gradient background
        setContentPane(new JPanel() {
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
    }
    
    private void initializeComponents() {
        // Input Panel Components
        processCountCombo = new JComboBox<>(new Integer[]{3, 4, 5});
        processCountCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        processCountCombo.setPreferredSize(new Dimension(80, 35));
        
        // Table Setup
        model = new DefaultTableModel(new Object[]{"Process", "AT", "BT", "CT", "TAT", "WT"}, 0);
        table = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                
                // Always set foreground (font color) to black for all columns
                c.setForeground(Color.BLACK);

                if (!isRowSelected(row)) {
                    if (column <= 2) { // Input columns: Process, AT, BT
                        c.setBackground(row % 2 == 0 ? new Color(255, 255, 255) : new Color(248, 252, 255));
                    } else { // Result columns: CT, TAT, WT
                        c.setBackground(row % 2 == 0 ? new Color(240, 255, 240) : new Color(230, 250, 230));
                    }
                } else {
                    // Even when selected, keep the font black
                    c.setForeground(Color.BLACK);
                }

                return c;
            }
            
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0 && column != 3 && column != 4 && column != 5;
            }
        };
        
        // Enhanced table styling
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            setHorizontalAlignment(JLabel.CENTER);
            setForeground(Color.BLACK); // ✅ Ensures black font color

            if (column > 2) {
                setFont(new Font("Segoe UI", Font.BOLD, 13));
            } else {
                setFont(new Font("Segoe UI", Font.PLAIN, 13));
            }

            return c;
        }
    };

        
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        table.setRowHeight(32);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(100, 149, 237));
        table.getTableHeader().setForeground(Color.BLACK);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setGridColor(new Color(200, 220, 240));
        table.setSelectionBackground(new Color(184, 207, 229));
        
        // Buttons
        computeButton = new RoundedButton("Compute", 25);
        computeButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        computeButton.setBackground(new Color(46, 139, 87));
        computeButton.setForeground(Color.WHITE);
        computeButton.setToolTipText("Calculate scheduling results");
        
        resetButton = new RoundedButton("Reset", 25);
        resetButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        resetButton.setBackground(new Color(220, 20, 60));
        resetButton.setForeground(Color.WHITE);
        resetButton.setToolTipText("Clear all data");
        
        // Result Area
        resultArea = new JTextArea(6, 60);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        resultArea.setBackground(new Color(248, 248, 255));
        resultArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Gantt Chart Panel
        ganttPanel = new GanttChartPanel();
    }
    
    private void setupLayout() {
        // Top Panel (Input)
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        inputPanel.setOpaque(false);
        inputPanel.setBorder(new CompoundBorder(
            new RoundedBorder(20, new Color(100, 149, 237)),
            new EmptyBorder(15, 20, 15, 20)
        ));
        
        JLabel titleLabel = new JLabel("🔄 FCFS Process Scheduler");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(25, 25, 112));
        
        JLabel processLabel = new JLabel("Number of Processes:");
        processLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        processLabel.setForeground(new Color(25, 25, 112));
        
        RoundedButton setButton = new RoundedButton("Set Processes", 20);
        setButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        setButton.setBackground(new Color(100, 149, 237));
        setButton.setForeground(Color.WHITE);
        
        inputPanel.add(titleLabel);
        inputPanel.add(Box.createHorizontalStrut(20));
        inputPanel.add(processLabel);
        inputPanel.add(processCountCombo);
        inputPanel.add(setButton);
        
        add(inputPanel, BorderLayout.NORTH);
        
        // Center Panel (Table + Gantt)
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        // Table
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                new RoundedBorder(15, new Color(100, 149, 237)),
                "📊 Process Information Table",
                TitledBorder.CENTER, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(25, 25, 112)
            ),
            new EmptyBorder(5, 5, 5, 5)
        ));
        tableScroll.setPreferredSize(new Dimension(850, 200));
        
        centerPanel.add(tableScroll, BorderLayout.NORTH);
        centerPanel.add(ganttPanel, BorderLayout.CENTER);
        
        add(centerPanel, BorderLayout.CENTER);
        
        // Bottom Panel
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(10, 15, 15, 15));
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.add(computeButton);
        buttonPanel.add(resetButton);
        
        // Results Panel
        JScrollPane resultScroll = new JScrollPane(resultArea);
        resultScroll.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                new RoundedBorder(15, new Color(46, 139, 87)),
                "📈 Calculation Results",
                TitledBorder.CENTER, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(25, 100, 25)
            ),
            new EmptyBorder(5, 5, 5, 5)
        ));
        
        bottomPanel.add(buttonPanel, BorderLayout.NORTH);
        bottomPanel.add(resultScroll, BorderLayout.CENTER);
        
        add(bottomPanel, BorderLayout.SOUTH);
        
        // Event handlers for the set button
        setButton.addActionListener(e -> {
            processCount = (Integer) processCountCombo.getSelectedItem();
            model.setRowCount(0);
            for (int i = 0; i < processCount; i++) {
                model.addRow(new Object[]{"P" + (i + 1), "", "", "", "", ""});
            }
            ganttPanel.clearChart();
            resultArea.setText("✨ Ready to compute! Please enter Arrival Time (AT) and Burst Time (BT) for each process.");
        });
    }
    
    private void setupEventHandlers() {
        computeButton.addActionListener(e -> computeScheduling());
        resetButton.addActionListener(e -> resetAll());
    }
    
    private void computeScheduling() {
        try {
            if (processCount == 0) {
                JOptionPane.showMessageDialog(this, "Please set the number of processes first!");
                return;
            }
            
            int[] at = new int[processCount];
            int[] bt = new int[processCount];
            int[] st = new int[processCount];
            int[] ct = new int[processCount];
            int[] tat = new int[processCount];
            int[] wt = new int[processCount];
            String[] pid = new String[processCount];
            
            // Read input data
            for (int i = 0; i < processCount; i++) {
                pid[i] = (String) model.getValueAt(i, 0);
                String atStr = model.getValueAt(i, 1).toString().trim();
                String btStr = model.getValueAt(i, 2).toString().trim();
                
                if (atStr.isEmpty() || btStr.isEmpty()) {
                    throw new IllegalArgumentException("All AT and BT fields must be filled!");
                }
                
                at[i] = Integer.parseInt(atStr);
                bt[i] = Integer.parseInt(btStr);
                
                if (at[i] < 0 || bt[i] <= 0) {
                    throw new IllegalArgumentException("AT must be ≥ 0 and BT must be > 0!");
                }
            }
            
            // Sort by arrival time (FCFS)
            for (int i = 0; i < processCount - 1; i++) {
                for (int j = i + 1; j < processCount; j++) {
                    if (at[i] > at[j]) {
                        // Swap all arrays
                        swap(at, i, j);
                        swap(bt, i, j);
                        String temp = pid[i]; pid[i] = pid[j]; pid[j] = temp;
                    }
                }
            }
            
            // Calculate start and completion times
            st[0] = at[0];
            ct[0] = st[0] + bt[0];
            
            for (int i = 1; i < processCount; i++) {
                st[i] = Math.max(at[i], ct[i - 1]);
                ct[i] = st[i] + bt[i];
            }
            
            // Calculate TAT and WT
            double totalTAT = 0, totalWT = 0;
            for (int i = 0; i < processCount; i++) {
                tat[i] = ct[i] - at[i];
                wt[i] = tat[i] - bt[i];
                totalTAT += tat[i];
                totalWT += wt[i];
            }
            
            double avgTAT = totalTAT / processCount;
            double avgWT = totalWT / processCount;
            
            // Update table
            for (int i = 0; i < processCount; i++) {
                model.setValueAt(pid[i], i, 0);
                model.setValueAt(at[i], i, 1);
                model.setValueAt(bt[i], i, 2);
                model.setValueAt(ct[i], i, 3);
                model.setValueAt(tat[i], i, 4);
                model.setValueAt(wt[i], i, 5);
            }
            
            // Update Gantt chart
            ganttPanel.updateChart(pid, st, ct);
            
            // Update results
            StringBuilder results = new StringBuilder();
            results.append("🎯 FCFS SCHEDULING RESULTS\n");
            results.append("═══════════════════════════════════════\n\n");
            
            results.append("📊 Process Execution Summary:\n");
            for (int i = 0; i < processCount; i++) {
                results.append(String.format("   %s: [%d → %d] Duration: %d, TAT: %d, WT: %d\n",
                    pid[i], st[i], ct[i], bt[i], tat[i], wt[i]));
            }
            
            results.append(String.format("\n📈 Performance Metrics:\n"));
            results.append(String.format("   • Average Turnaround Time: %.2f time units\n", avgTAT));
            results.append(String.format("   • Average Waiting Time: %.2f time units\n", avgWT));
            results.append(String.format("   • Total Execution Time: %d time units\n", ct[processCount - 1]));
            results.append(String.format("   • CPU Utilization: %.1f%%", 
                (double) totalTAT / ct[processCount - 1] * 100));
            
            resultArea.setText(results.toString());
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, 
                "❌ Please enter valid integers for AT and BT values!", 
                "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, 
                "❌ " + ex.getMessage(), 
                "Input Validation Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "❌ An error occurred: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }
    
    private void resetAll() {
        processCountCombo.setSelectedIndex(0);
        processCount = 0;
        model.setRowCount(0);
        ganttPanel.clearChart();
        resultArea.setText("🔄 System reset! Select number of processes to begin.");
    }
    
    // Custom rounded border class
    static class RoundedBorder extends AbstractBorder {
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
    }
    
    public static void main(String[] args) {
        // Set system look and feel with better error handling
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | 
                 IllegalAccessException | UnsupportedLookAndFeelException e) {
            System.err.println("Could not set system look and feel: " + e.getMessage());
            // Fall back to default look and feel - application will still work
        }
        
        SwingUtilities.invokeLater(() -> {
            try {
                new SNFN2().setVisible(true);
            } catch (Exception e) {
                System.err.println("Error starting application: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}