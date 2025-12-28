import javax.swing.*;
import java.awt.*;
import java.util.List;

public class RCSCalculator {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // 创建并设置主窗口
            JFrame frame = new JFrame("双站RCS计算器");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 700);
            frame.setLocationRelativeTo(null);
            frame.setLayout(new BorderLayout());

            try {
                // 读取数据
                System.out.println("正在读取RCS数据...");
                DataReader dataReader = new DataReader();
                List<RCSData> rcsDataList = dataReader.readAllData();

                // 初始化插值引擎
                System.out.println("正在初始化插值引擎...");
                InterpolationEngine interpolationEngine = new InterpolationEngine(rcsDataList);

                // 统计频率点和入射方向
                List<Double> frequencies = rcsDataList.stream()
                        .map(RCSData::getFrequency)
                        .distinct()
                        .sorted()
                        .toList();

                List<Double> incidentDirections = rcsDataList.stream()
                        .map(RCSData::getIncidentDirection)
                        .distinct()
                        .sorted()
                        .toList();

                System.out.println("插值引擎初始化完成:");
                System.out.println("- 频率点数量: " + frequencies.size());
                System.out.println("- 入射方向数量: " + incidentDirections.size());
                System.out.println("- 总数据点数量: " + rcsDataList.size());

                // 创建可视化面板
                VisualizationPanel visualizationPanel = new VisualizationPanel(rcsDataList, interpolationEngine);
                frame.add(visualizationPanel, BorderLayout.CENTER);

                System.out.println("数据加载完成，共读取 " + rcsDataList.size() + " 个数据点");

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, 
                        "加载数据失败: " + e.getMessage(), 
                        "错误", JOptionPane.ERROR_MESSAGE);
            }

            // 显示窗口
            frame.setVisible(true);
        });
    }
}