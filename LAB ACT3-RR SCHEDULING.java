import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class SOLOALVHIN extends JFrame {
   private JTextField processCountField, quantumField;
   private JButton setButton, calculateButton, clearButton;
   private JTable resultTable;
   private DefaultTableModel tableModel;
   private JLabel avgTatLabel, avgWtLabel;
   private GanttChartPanel ganttChartPanel;
   private final ArrayList<Process> processes = new ArrayList<>();
   private final ArrayList<GanttBlock> ganttChartData = new ArrayList<>();
   private static class Process {
       String name;
       int arrivalTime;
       int burstTime;
       int remainingTime;
       int completionTime = 0;
       int turnaroundTime = 0;
       int waitingTime = 0;
       Process(String name, int at, int bt) {
           this.name = name;
           this.arrivalTime = at;
           this.burstTime = bt;
           this.remainingTime = bt;
       }
   }
   private static class GanttBlock {
       String processName;
       int startTime;
       int endTime;
       GanttBlock(String name, int start, int end) {
           this.processName = name;
           this.startTime = start;
           this.endTime = end;
       }
   }
   public SOLOALVHIN() {
       setTitle("Round Robin Scheduling");
       setSize(1000, 700);
       setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       setLocationRelativeTo(null);
       setLayout(new BorderLayout(10, 10));
       JPanel inputPanel  = createInputPanel();
       JPanel mainPanel   = new JPanel(new BorderLayout(10, 10));
       JPanel tablePanel  = createTablePanel();
       JPanel ganttPanel  = createGanttPanel();
       JPanel footerPanel = createFooterPanel();
       mainPanel.add(tablePanel, BorderLayout.CENTER);
       mainPanel.add(ganttPanel, BorderLayout.SOUTH);
       add(inputPanel, BorderLayout.NORTH);
       add(mainPanel, BorderLayout.CENTER);
       add(footerPanel, BorderLayout.SOUTH);
   }
   private JPanel createInputPanel() {
       JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
       panel.setBorder(BorderFactory.createTitledBorder("Input"));
       panel.add(new JLabel("Number of Processes:"));
       processCountField = new JTextField(5);
       panel.add(processCountField);
       setButton = new JButton("Set Processes");
       setButton.addActionListener(e -> setProcesses());
       panel.add(setButton);
       panel.add(new JLabel("Time Quantum:"));
       quantumField = new JTextField(5);
       quantumField.setText("4");
       panel.add(quantumField);
       calculateButton = new JButton("Calculate");
       calculateButton.addActionListener(e -> calculate());
       panel.add(calculateButton);
       clearButton = new JButton("Start Again");
       clearButton.addActionListener(e -> clearAll());
       panel.add(clearButton);
       return panel;
   }
   private JPanel createTablePanel() {
       JPanel panel = new JPanel(new BorderLayout());
       panel.setBorder(BorderFactory.createTitledBorder("Processes & Results"));
       String[] cols = {"Process", "AT", "BT", "CT", "TAT", "WT"};
       tableModel = new DefaultTableModel(cols, 0);
       resultTable = new JTable(tableModel);
       resultTable.getTableHeader().setReorderingAllowed(false);
       resultTable.setRowHeight(22);
       JScrollPane sp = new JScrollPane(resultTable);
       panel.add(sp, BorderLayout.CENTER);
       return panel;
   }
   private JPanel createGanttPanel() {
       JPanel panel = new JPanel(new BorderLayout());
       panel.setBorder(BorderFactory.createTitledBorder("Gantt Chart"));
       ganttChartPanel = new GanttChartPanel();
       ganttChartPanel.setPreferredSize(new Dimension(900, 130));
       panel.add(ganttChartPanel, BorderLayout.CENTER);
       return panel;
   }
   private JPanel createFooterPanel() {
       JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 8));
       avgTatLabel = new JLabel("AVERAGE TAT: N/A");
       avgWtLabel  = new JLabel("AVERAGE WT: N/A");
       panel.add(avgTatLabel);
       panel.add(avgWtLabel);
       return panel;
   }
   private void setProcesses() {
       try {
           int count = Integer.parseInt(processCountField.getText().trim());
           if (count <= 0) {
               JOptionPane.showMessageDialog(this, "Number of processes must be > 0.", "Error", JOptionPane.ERROR_MESSAGE);
               return;
           }
           tableModel.setRowCount(0);
           processes.clear();
           ganttChartData.clear();
           for (int i = 1; i <= count; i++) {
               tableModel.addRow(new Object[]{"P" + i, "", "", "", "", ""});
           }
           ganttChartPanel.repaint();
           avgTatLabel.setText("AVERAGE TAT: N/A");
           avgWtLabel.setText("AVERAGE WT: N/A");
       } catch (NumberFormatException ex) {
           JOptionPane.showMessageDialog(this, "Enter a valid integer for number of processes.", "Error", JOptionPane.ERROR_MESSAGE);
       }
   }
   private void clearAll() {
       int confirm = JOptionPane.showConfirmDialog(
               this,
               "Are you sure you want to clear all data?",
               "Confirm Reset",
               JOptionPane.YES_NO_OPTION,
               JOptionPane.WARNING_MESSAGE
       );
       if (confirm == JOptionPane.YES_OPTION) {
           tableModel.setRowCount(0);
           processes.clear();
           ganttChartData.clear();
           ganttChartPanel.repaint();
           avgTatLabel.setText("AVERAGE TAT: N/A");
           avgWtLabel.setText("AVERAGE WT: N/A");
       }
   }
   private void calculate() {
       if (tableModel.getRowCount() == 0) {
           JOptionPane.showMessageDialog(this, "No processes defined.", "Error", JOptionPane.ERROR_MESSAGE);
           return;
       }
       processes.clear();
       for (int i = 0; i < tableModel.getRowCount(); i++) {
           try {
               String name = tableModel.getValueAt(i, 0).toString();
               int at = Integer.parseInt(tableModel.getValueAt(i, 1).toString());
               int bt = Integer.parseInt(tableModel.getValueAt(i, 2).toString());
               processes.add(new Process(name, at, bt));
           } catch (Exception ex) {
               JOptionPane.showMessageDialog(this, "Please fill AT and BT for all processes with valid integers.", "Error", JOptionPane.ERROR_MESSAGE);
               return;
           }
       }
       final int quantum;
       try {
           quantum = Integer.parseInt(quantumField.getText().trim());
           if (quantum <= 0) throw new NumberFormatException();
       } catch (NumberFormatException ex) {
           JOptionPane.showMessageDialog(this, "Invalid time quantum.", "Error", JOptionPane.ERROR_MESSAGE);
           return;
       }
       // Reset
       ganttChartData.clear();
       for (Process p : processes) {
           p.remainingTime = p.burstTime;
           p.completionTime = 0;
           p.turnaroundTime = 0;
           p.waitingTime = 0;
       }
       int n = processes.size();
       int completed = 0;
       int currentTime = 0;
       int idx = 0;
       int earliestArrival = Integer.MAX_VALUE;
       for (Process p : processes) earliestArrival = Math.min(earliestArrival, p.arrivalTime);
       if (currentTime < earliestArrival) currentTime = earliestArrival;
       while (completed < n) {
           boolean ranThisPass = false;
           for (int steps = 0; steps < n; steps++) {
               Process p = processes.get(idx);
               if (p.remainingTime > 0 && p.arrivalTime <= currentTime) {
                   int start = currentTime;
                   int slice = Math.min(quantum, p.remainingTime);
                   currentTime += slice;
                   p.remainingTime -= slice;
                   ganttChartData.add(new GanttBlock(p.name, start, currentTime));
                   if (p.remainingTime == 0) {
                       p.completionTime = currentTime;
                       completed++;
                   }
                   ranThisPass = true;
               }
               idx = (idx + 1) % n;
           }
           if (!ranThisPass) {
               int nextArrival = Integer.MAX_VALUE;
               for (Process p : processes) {
                   if (p.remainingTime > 0 && p.arrivalTime > currentTime) {
                       nextArrival = Math.min(nextArrival, p.arrivalTime);
                   }
               }
               if (nextArrival == Integer.MAX_VALUE) break;
               currentTime = nextArrival;
           }
       }
       double totalTat = 0, totalWt = 0;
       for (int i = 0; i < processes.size(); i++) {
           Process p = processes.get(i);
           p.turnaroundTime = p.completionTime - p.arrivalTime;
           p.waitingTime    = p.turnaroundTime - p.burstTime;
           totalTat += p.turnaroundTime;
           totalWt  += p.waitingTime;
           tableModel.setValueAt(p.arrivalTime, i, 1);
           tableModel.setValueAt(p.burstTime, i, 2);
           tableModel.setValueAt(p.completionTime, i, 3);
           tableModel.setValueAt(p.turnaroundTime, i, 4);
           tableModel.setValueAt(p.waitingTime, i, 5);
       }
       double avgTat = totalTat / processes.size();
       double avgWt  = totalWt  / processes.size();
       avgTatLabel.setText(String.format("AVERAGE TAT: %.1f", avgTat));
       avgWtLabel.setText(String.format("AVERAGE WT: %.1f", avgWt));
       ganttChartPanel.repaint();
   }
   private class GanttChartPanel extends JPanel {
       private final int barHeight = 36;
       private final int margin = 20;
       @Override
       protected void paintComponent(Graphics g) {
           super.paintComponent(g);
           if (ganttChartData.isEmpty()) return;
           int totalTime = ganttChartData.get(ganttChartData.size() - 1).endTime;
           if (totalTime <= 0) return;
           int width = getWidth() - 2 * margin;
           double scale = (double) width / totalTime;
           g.drawLine(margin, margin + barHeight, margin + (int) (totalTime * scale), margin + barHeight);
           for (GanttBlock b : ganttChartData) {
               int x1 = margin + (int) Math.round(b.startTime * scale);
               int x2 = margin + (int) Math.round(b.endTime   * scale);
               int w  = Math.max(1, x2 - x1);
               g.setColor(new Color(100, 149, 237));
               g.fillRect(x1, margin, w, barHeight);
               g.setColor(Color.BLACK);
               g.drawRect(x1, margin, w, barHeight);
               g.drawString(b.processName, x1 + 5, margin + barHeight / 2 + 5);
               g.drawString(String.valueOf(b.startTime), x1, margin + barHeight + 15);
           }
           g.drawString(String.valueOf(totalTime), margin + (int) Math.round(totalTime * scale), margin + barHeight + 15);
       }
   }
   public static void main(String[] args) {
       SwingUtilities.invokeLater(() -> new SOLOALVHIN().setVisible(true));
   }
}