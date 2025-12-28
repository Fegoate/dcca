import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.stream.Collectors;

public class VisualizationPanel extends JPanel implements ActionListener {
    private JTextField frequencyField;
    private JTextField incidentDirectionField;
    private JTextField elevationField;
    private JTextField azimuthField;
    private JTextField targetThetaField;
    private JButton calculateButton;
    private JLabel resultLabel;
    private List<RCSData> rcsDataList;
    private InterpolationEngine interpolationEngine;
    private double currentFrequency = 10.0;
    private double currentIncidentDirection = 0.0;
    private double currentElevation = 0.0;
    private double currentAzimuth = 0.0;
    private double currentRCS = 0.0;
    private boolean hasCalculated = false;

    public VisualizationPanel(List<RCSData> rcsDataList, InterpolationEngine interpolationEngine) {
        this.rcsDataList = rcsDataList;
        this.interpolationEngine = interpolationEngine;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(800, 600));

        // 创建控制面板
        JPanel controlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        controlPanel.setBorder(BorderFactory.createTitledBorder("参数设置"));

        // 设置频率输入
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        controlPanel.add(new JLabel("频率 (MHz):"), gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        frequencyField = new JTextField("10.0", 10);
        controlPanel.add(frequencyField, gbc);

        // 设置入射俯仰角输入
        gbc.gridx = 2;
        gbc.anchor = GridBagConstraints.EAST;
        controlPanel.add(new JLabel("入射俯仰角 (度):"), gbc);
        gbc.gridx = 3;
        gbc.anchor = GridBagConstraints.WEST;
        incidentDirectionField = new JTextField("0.0", 10);
        controlPanel.add(incidentDirectionField, gbc);

        // 设置入射方位角输入
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        controlPanel.add(new JLabel("入射方位角 (度):"), gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        elevationField = new JTextField("0.0", 10);
        controlPanel.add(elevationField, gbc);

        // 设置目标俯仰角输入
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        controlPanel.add(new JLabel("目标俯仰角 (度):"), gbc);
        gbc.gridx = 3;
        gbc.anchor = GridBagConstraints.WEST;
        targetThetaField = new JTextField("0.0", 10);
        controlPanel.add(targetThetaField, gbc);

        // 设置目标方位角输入
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        controlPanel.add(new JLabel("目标方位角 (度):"), gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        azimuthField = new JTextField("0.0", 10);
        controlPanel.add(azimuthField, gbc);

        // 设置计算按钮
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        calculateButton = new JButton("计算RCS");
        calculateButton.addActionListener(this);
        controlPanel.add(calculateButton, gbc);

        // 设置结果标签
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        resultLabel = new JLabel("请输入参数并点击计算按钮", JLabel.CENTER);
        resultLabel.setFont(new Font("Arial", Font.BOLD, 14));
        controlPanel.add(resultLabel, gbc);

        add(controlPanel, BorderLayout.NORTH);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (rcsDataList == null || rcsDataList.isEmpty()) {
            g.drawString("没有可用的RCS数据", getWidth() / 2 - 50, getHeight() / 2);
            return;
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawRCSDirectionMap(g2d);
        drawCurrentPoint(g2d);
    }

    private void drawRCSDirectionMap(Graphics2D g2d) {
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int radius = Math.min(centerX, centerY) - 50;

        // 绘制极坐标网格
        g2d.setColor(Color.LIGHT_GRAY);
        for (int i = 1; i <= 5; i++) {
            int r = (int) (radius * i / 5.0);
            g2d.drawOval(centerX - r, centerY - r, r * 2, r * 2);
        }

        // 绘制角度线
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4.0;
            int x = centerX + (int) (radius * Math.cos(angle));
            int y = centerY - (int) (radius * Math.sin(angle));
            g2d.drawLine(centerX, centerY, x, y);
            // 绘制角度标签
            String label = String.valueOf((int) (i * 45));
            int labelX = centerX + (int) ((radius + 15) * Math.cos(angle));
            int labelY = centerY - (int) ((radius + 15) * Math.sin(angle));
            g2d.drawString(label, labelX - 5, labelY + 5);
        }

        // 为了调试，暂时移除频率和入射方向的过滤条件，显示所有数据点
        List<RCSData> freqData = rcsDataList.stream()
                // 暂时不过滤任何数据，显示所有点
                .collect(Collectors.toList());
        System.out.println("绘制的点数量: " + freqData.size());

        if (freqData.isEmpty()) {
            g2d.setColor(Color.RED);
            g2d.drawString("当前参数下无匹配数据", centerX - 80, centerY);
            return;
        }

        // 计算RCS值的范围
        double minRCS = freqData.stream().mapToDouble(RCSData::getRcsValue).min().orElse(-50.0);
        double maxRCS = freqData.stream().mapToDouble(RCSData::getRcsValue).max().orElse(-10.0);
        double range = maxRCS - minRCS;

        if (range == 0) {
            range = 1;
        }

        // 绘制RCS方向图
        g2d.setStroke(new BasicStroke(2));
        for (RCSData data : freqData) {
            double phi = Math.toRadians(data.getPhi()); // 方位角
            double theta = Math.toRadians(data.getTheta()); // 俯仰角
            double rcsValue = data.getRcsValue();
            
            // 计算归一化半径（将RCS值映射到[0, radius]）
            double normalizedRCS = Math.max(0, Math.min(1, (rcsValue - minRCS) / range));
            int r = (int) (radius * normalizedRCS);

            // 转换极坐标到笛卡尔坐标（考虑俯仰角theta）
            // 极径r, 方位角phi(绕y轴), 俯仰角theta(与y轴的夹角)
            double xCartesian = r * Math.sin(theta) * Math.sin(phi);
            double yCartesian = r * Math.cos(theta);
            
            // 由于我们是在2D平面上显示，需要将3D坐标投影到2D平面
            // 这里使用简化的2D极坐标显示，主要根据方位角phi
            int x = centerX + (int) (r * Math.sin(phi));
            int y = centerY - (int) (r * Math.cos(phi));

            // 根据RCS值设置颜色
            Color color = getColorForRCS(rcsValue, minRCS, maxRCS);
            g2d.setColor(color);
            g2d.fillOval(x - 3, y - 3, 6, 6);
        }

        // 绘制图例
        drawLegend(g2d, minRCS, maxRCS);
    }

    private void drawCurrentPoint(Graphics2D g2d) {
        if (!hasCalculated) {
            return;
        }

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int radius = Math.min(centerX, centerY) - 50;

        // 获取当前频率和入射方向下的数据点以计算RCS范围
        List<RCSData> freqData = rcsDataList.stream()
                .filter(data -> Math.abs(data.getFrequency() - currentFrequency) < 0.1)
                .filter(data -> Math.abs(data.getIncidentDirection() - currentIncidentDirection) < 1.0)
                .collect(Collectors.toList());

        if (freqData.isEmpty()) {
            return;
        }

        double minRCS = freqData.stream().mapToDouble(RCSData::getRcsValue).min().orElse(0.0);
        double maxRCS = freqData.stream().mapToDouble(RCSData::getRcsValue).max().orElse(0.0);
        double range = maxRCS - minRCS;

        if (range == 0) {
            range = 1;
        }

        double normalizedRCS = (currentRCS - minRCS) / range;
        int r = (int) (radius * normalizedRCS);
        double phi = Math.toRadians(currentAzimuth);
        double theta = Math.toRadians(currentElevation);

        // 转换极坐标到笛卡尔坐标
        int x = centerX + (int) (r * Math.sin(phi));
        int y = centerY - (int) (r * Math.cos(phi));

        // 绘制当前点
        g2d.setColor(Color.BLUE);
        g2d.setStroke(new BasicStroke(3));
        g2d.fillOval(x - 5, y - 5, 10, 10);
        g2d.drawOval(x - 10, y - 10, 20, 20);

        // 绘制坐标值
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.drawString(String.format("(%.1f, %.1f)", currentElevation, currentAzimuth), x + 15, y - 15);
        g2d.drawString(String.format("RCS: %.2f dB(m²)", currentRCS), x + 15, y + 5);
    }

    private void drawLegend(Graphics2D g2d, double minRCS, double maxRCS) {
        int legendX = getWidth() - 120;
        int legendY = 50;
        int legendWidth = 80;
        int legendHeight = 200;

        // 绘制图例背景
        g2d.setColor(Color.WHITE);
        g2d.fillRect(legendX, legendY, legendWidth, legendHeight);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(legendX, legendY, legendWidth, legendHeight);

        // 绘制颜色渐变
        for (int y = 0; y < legendHeight; y++) {
            double ratio = (double) y / legendHeight;
            double rcsValue = maxRCS - (maxRCS - minRCS) * ratio;
            Color color = getColorForRCS(rcsValue, minRCS, maxRCS);
            g2d.setColor(color);
            g2d.drawLine(legendX + 10, legendY + y, legendX + legendWidth - 10, legendY + y);
        }

        // 绘制数值标签
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        g2d.drawString(String.format("%.1f dB", maxRCS), legendX + legendWidth + 5, legendY + 15);
        g2d.drawString(String.format("%.1f dB", (maxRCS + minRCS) / 2), legendX + legendWidth + 5, legendY + legendHeight / 2 + 5);
        g2d.drawString(String.format("%.1f dB", minRCS), legendX + legendWidth + 5, legendY + legendHeight - 5);
        g2d.drawString("RCS (dB)", legendX + 10, legendY - 10);
    }

    private Color getColorForRCS(double rcsValue, double minRCS, double maxRCS) {
        double normalized = (rcsValue - minRCS) / (maxRCS - minRCS);
        if (normalized < 0.2) {
            return new Color(0, (int) (255 * normalized * 5), 255);
        } else if (normalized < 0.4) {
            return new Color(0, 255, (int) (255 - 255 * (normalized - 0.2) * 5));
        } else if (normalized < 0.6) {
            return new Color((int) (255 * (normalized - 0.4) * 5), 255, 0);
        } else if (normalized < 0.8) {
            return new Color(255, (int) (255 - 255 * (normalized - 0.6) * 5), 0);
        } else {
            return new Color(255, 0, 0);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == calculateButton) {
            calculateRCS();
        }
    }

    private void calculateRCS() {
        try {
            currentFrequency = Double.parseDouble(frequencyField.getText());
            double incidentTheta = Double.parseDouble(incidentDirectionField.getText()); // 入射俯仰角
            double incidentPhi = Double.parseDouble(elevationField.getText()); // 入射方位角
            double targetTheta = Double.parseDouble(targetThetaField.getText()); // 目标俯仰角
            double targetPhi = Double.parseDouble(azimuthField.getText()); // 目标方位角

            // 使用插值引擎计算RCS值（使用新的球面坐标方法）
            currentRCS = interpolationEngine.calculateRCS(currentFrequency, incidentTheta, incidentPhi, targetTheta, targetPhi);
            hasCalculated = true;

            // 更新结果标签
            resultLabel.setText(String.format(
                "频率: %.1f MHz, 入射俯仰角: %.1f°, 入射方位角: %.1f°, 目标俯仰角: %.1f°, 目标方位角: %.1f° → RCS: %.2f dB(m²)",
                currentFrequency, incidentTheta, incidentPhi, targetTheta, targetPhi, currentRCS
            ));

            // 重绘面板
            repaint();
        } catch (NumberFormatException ex) {
            resultLabel.setText("输入参数格式错误，请输入数字");
        } catch (Exception ex) {
            resultLabel.setText("计算RCS失败: " + ex.getMessage());
        }
    }

    public void setRCSDataList(List<RCSData> rcsDataList) {
        this.rcsDataList = rcsDataList;
        repaint();
    }
}