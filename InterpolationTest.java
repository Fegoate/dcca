import java.util.List;

public class InterpolationTest {
    public static void main(String[] args) {
        try {
            // 读取数据
            System.out.println("正在读取RCS数据...");
            DataReader dataReader = new DataReader();
            List<RCSData> rcsDataList = dataReader.readAllData();

            // 初始化插值引擎
            System.out.println("正在初始化插值引擎...");
            InterpolationEngine interpolationEngine = new InterpolationEngine(rcsDataList);

            System.out.println("测试开始：");

            // 测试1：使用原有方法和新方法计算相同方向的结果，验证兼容性
            System.out.println("\n1. 测试兼容性：");
            double frequency = 10.0;
            double incidentDirection = 1.0;
            double theta = 45.0;
            double phi = 90.0;
            
            double rcsOld = interpolationEngine.calculateRCS(frequency, incidentDirection, theta, phi);
            System.out.printf("原有方法：频率=%.1f MHz, 方向=%.1f, theta=%.1f°, phi=%.1f°, RCS=%.2f dB(m²)\n", 
                    frequency, incidentDirection, theta, phi, rcsOld);

            // 测试2：测试新方法，使用球面坐标指定入射方向
            System.out.println("\n2. 测试新方法（球面坐标）：");
            // 方向1的球面坐标：传播方向(0,-1,0) 对应的球面坐标是 theta=180°, phi=0°
            double incidentTheta = 180.0;
            double incidentPhi = 0.0;
            
            double rcsNew = interpolationEngine.calculateRCS(frequency, incidentTheta, incidentPhi, theta, phi);
            System.out.printf("新方法：频率=%.1f MHz, 入射方向(theta=%.1f°, phi=%.1f°), theta=%.1f°, phi=%.1f°, RCS=%.2f dB(m²)\n", 
                    frequency, incidentTheta, incidentPhi, theta, phi, rcsNew);

            // 测试3：测试任意入射方向
            System.out.println("\n3. 测试任意入射方向：");
            // 测试一些随机方向
            double[][] testDirections = {
                {45.0, 45.0},    // 方向：(0.5, 0.5, 0.707)
                {90.0, 90.0},    // 方向：(1, 0, 0)
                {60.0, 120.0},   // 方向：(0.433, -0.5, 0.75)
                {135.0, 270.0}   // 方向：(-0.707, -0.707, 0)
            };
            
            for (int i = 0; i < testDirections.length; i++) {
                incidentTheta = testDirections[i][0];
                incidentPhi = testDirections[i][1];
                rcsNew = interpolationEngine.calculateRCS(frequency, incidentTheta, incidentPhi, theta, phi);
                System.out.printf("测试方向%d：入射方向(theta=%.1f°, phi=%.1f°), RCS=%.2f dB(m²)\n", 
                        i+1, incidentTheta, incidentPhi, rcsNew);
            }

            // 测试4：测试不同频率
            System.out.println("\n4. 测试不同频率：");
            double[] testFrequencies = {5.0, 12.5, 25.0, 30.0, 40.0};
            incidentTheta = 45.0;
            incidentPhi = 45.0;
            
            for (double f : testFrequencies) {
                rcsNew = interpolationEngine.calculateRCS(f, incidentTheta, incidentPhi, theta, phi);
                System.out.printf("频率=%.1f MHz, 入射方向(theta=%.1f°, phi=%.1f°), RCS=%.2f dB(m²)\n", 
                        f, incidentTheta, incidentPhi, rcsNew);
            }

            // 测试5：测试方位角周期性（270度和-90度应该得到相同结果）
            System.out.println("\n5. 测试方位角周期性：");
            frequency = 10.0;
            incidentDirection = 1.0;
            theta = 45.0;
            
            // 测试270度方位角
            double phi270 = 270.0;
            double rcs270 = interpolationEngine.calculateRCS(frequency, incidentDirection, theta, phi270);
            System.out.printf("方位角=%.1f°：频率=%.1f MHz, 方向=%.1f, theta=%.1f°, RCS=%.2f dB(m²)\n", 
                    phi270, frequency, incidentDirection, theta, rcs270);
            
            // 测试-90度方位角（应该和270度得到相同结果）
            double phiMinus90 = -90.0;
            double rcsMinus90 = interpolationEngine.calculateRCS(frequency, incidentDirection, theta, phiMinus90);
            System.out.printf("方位角=%.1f°：频率=%.1f MHz, 方向=%.1f, theta=%.1f°, RCS=%.2f dB(m²)\n", 
                    phiMinus90, frequency, incidentDirection, theta, rcsMinus90);
            
            // 验证结果是否一致
            boolean isConsistent = Math.abs(rcs270 - rcsMinus90) < 0.01; // 允许微小的浮点误差
            System.out.println("270度和-90度方位角结果是否一致：" + (isConsistent ? "是" : "否"));

            System.out.println("\n测试完成！");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("测试失败：" + e.getMessage());
        }
    }
}