import java.util.*;
import java.util.stream.Collectors;

public class InterpolationEngine {
    private List<RCSData> rcsDataList;
    // 方向映射：key为方向编号，value为传播方向的笛卡尔坐标向量
    private Map<Integer, double[]> directionMap;

    public InterpolationEngine(List<RCSData> rcsDataList) {
        this.rcsDataList = rcsDataList;
        initializeDirectionMap();
    }

    // 初始化方向映射
    private void initializeDirectionMap() {
        directionMap = new HashMap<>();
        // 方向1：传播方向:(0,-1,0) 电场方向(0,0,1)
        directionMap.put(1, new double[]{0, -1, 0});
        // 方向2：传播方向：(0,0,1) 电场方向：（0,1,0）
        directionMap.put(2, new double[]{0, 0, 1});
        // 方向3：传播方向：(1,0,0) 电场方向：（0,1,0）
        directionMap.put(3, new double[]{1, 0, 0});
        // 方向4：传播方向：(0,1,0) 电场方向：（1,0,0）
        directionMap.put(4, new double[]{0, 1, 0});
        // 方向5：传播方向：(0,0,-1) 电场方向：（0,1,0）
        directionMap.put(5, new double[]{0, 0, -1});
        // 方向6：传播方向：(-1,0,0) 电场方向：（0,1,0）
        directionMap.put(6, new double[]{-1, 0, 0});
        // 方向7：传播方向：(0.707,0.707,0) 电场方向：（0,0,1）
        directionMap.put(7, new double[]{0.707, 0.707, 0});
        // 方向8：传播方向：(0.707,0,0.707) 电场方向：（0,1,0）
        directionMap.put(8, new double[]{0.707, 0, 0.707});
    }

    // 原有方法保持不变，确保兼容性
    public double calculateRCS(double frequency, double incidentDirection, double theta, double phi) {
        // 找到最接近的频率点
        List<Double> frequencies = rcsDataList.stream()
                .map(RCSData::getFrequency)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        double closestFreq1 = frequencies.get(0);
        double closestFreq2 = frequencies.get(0);

        for (double f : frequencies) {
            if (f <= frequency) {
                closestFreq1 = f;
            }
            if (f >= frequency) {
                closestFreq2 = f;
                break;
            }
        }

        // 找到最接近的入射方向点
        List<Double> incidentDirections = rcsDataList.stream()
                .map(RCSData::getIncidentDirection)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        double closestDir1 = incidentDirections.get(0);
        double closestDir2 = incidentDirections.get(0);

        for (double d : incidentDirections) {
            if (d <= incidentDirection) {
                closestDir1 = d;
            }
            if (d >= incidentDirection) {
                closestDir2 = d;
                break;
            }
        }

        // 对四个角落点进行插值
        double f1 = closestFreq1;
        double f2 = closestFreq2;
        double d1 = closestDir1;
        double d2 = closestDir2;

        double rcs11 = getClosestRCS(f1, d1, theta, phi);
        double rcs12 = getClosestRCS(f1, d2, theta, phi);
        double rcs21 = getClosestRCS(f2, d1, theta, phi);
        double rcs22 = getClosestRCS(f2, d2, theta, phi);

        // 二维线性插值
        double rcsFreq1 = interpolate(rcs11, rcs12, d1, d2, incidentDirection);
        double rcsFreq2 = interpolate(rcs21, rcs22, d1, d2, incidentDirection);
        double finalRCS = interpolate(rcsFreq1, rcsFreq2, f1, f2, frequency);

        return finalRCS;
    }

    // 新方法：支持任意入射方向（球面坐标）和频率
    public double calculateRCS(double frequency, double incidentTheta, double incidentPhi, double theta, double phi) {
        // 将入射方向转换为笛卡尔坐标向量
        double[] incidentVector = CoordinateTransformer.sphericalToCartesian(1, incidentTheta, incidentPhi);
        
        // 找到最接近的频率点
        List<Double> frequencies = rcsDataList.stream()
                .map(RCSData::getFrequency)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        double closestFreq1 = frequencies.get(0);
        double closestFreq2 = frequencies.get(0);

        for (double f : frequencies) {
            if (f <= frequency) {
                closestFreq1 = f;
            }
            if (f >= frequency) {
                closestFreq2 = f;
                break;
            }
        }

        // 找到最接近的两个入射方向点
        List<Integer> directions = new ArrayList<>(directionMap.keySet());
        directions.sort(Integer::compareTo);

        // 找到最接近的方向
        double minDistance = Double.MAX_VALUE;
        int closestDir1 = 1;
        int closestDir2 = 2;
        
        // 计算与入射向量最接近的方向
        for (int dir : directions) {
            double[] dirVector = directionMap.get(dir);
            if (dirVector == null) continue;
            
            double distance = vectorDistance(incidentVector, dirVector);
            
            if (distance < minDistance) {
                closestDir2 = closestDir1;
                closestDir1 = dir;
                minDistance = distance;
            } else {
                double[] dirVector2 = directionMap.get(closestDir2);
                if (dirVector2 != null && distance < vectorDistance(incidentVector, dirVector2)) {
                    closestDir2 = dir;
                }
            }
        }

        // 对四个角落点进行插值
        double f1 = closestFreq1;
        double f2 = closestFreq2;
        double d1 = closestDir1;
        double d2 = closestDir2;

        double rcs11 = getClosestRCS(f1, d1, theta, phi);
        double rcs12 = getClosestRCS(f1, d2, theta, phi);
        double rcs21 = getClosestRCS(f2, d1, theta, phi);
        double rcs22 = getClosestRCS(f2, d2, theta, phi);

        // 计算方向向量的线性插值参数
        double[] dirVector1 = directionMap.get(d1);
        double[] dirVector2 = directionMap.get(d2);
        
        // 确保方向向量不为空
        if (dirVector1 == null) dirVector1 = directionMap.get(1);
        if (dirVector2 == null) dirVector2 = directionMap.get(2);
        
        double dirDist1 = vectorDistance(incidentVector, dirVector1);
        double dirDist2 = vectorDistance(incidentVector, dirVector2);
        double dirParam = dirDist1 / (dirDist1 + dirDist2);

        // 二维线性插值：先在频率方向插值，再在方向方向插值
        double rcsFreq1 = interpolate(rcs11, rcs12, 0, 1, dirParam);
        double rcsFreq2 = interpolate(rcs21, rcs22, 0, 1, dirParam);
        double finalRCS = interpolate(rcsFreq1, rcsFreq2, f1, f2, frequency);

        return finalRCS;
    }

    // 计算两个向量之间的距离（余弦距离）
    private double vectorDistance(double[] v1, double[] v2) {
        double dotProduct = v1[0] * v2[0] + v1[1] * v2[1] + v1[2] * v2[2];
        double magnitude1 = Math.sqrt(v1[0] * v1[0] + v1[1] * v1[1] + v1[2] * v1[2]);
        double magnitude2 = Math.sqrt(v2[0] * v2[0] + v2[1] * v2[1] + v2[2] * v2[2]);
        
        // 余弦相似度
        double cosSimilarity = dotProduct / (magnitude1 * magnitude2);
        // 转换为距离（0-2之间）
        return 1 - cosSimilarity;
    }

    private double getClosestRCS(double frequency, double incidentDirection, double theta, double phi) {
        // 找到最接近的theta和phi的数据点
        RCSData closestData = null;
        double minDistance = Double.MAX_VALUE;

        for (RCSData data : rcsDataList) {
            if (Math.abs(data.getFrequency() - frequency) < 0.1 && Math.abs(data.getIncidentDirection() - incidentDirection) < 0.1) {
                double deltaTheta = data.getTheta() - theta;
                double deltaPhi = data.getPhi() - phi;
                
                // 处理方位角的周期性（360度循环）
                double periodicDeltaPhi = Math.abs(deltaPhi);
                periodicDeltaPhi = Math.min(periodicDeltaPhi, 360 - periodicDeltaPhi);
                
                double distance = Math.sqrt(
                        Math.pow(deltaTheta, 2) +
                        Math.pow(periodicDeltaPhi, 2)
                );

                if (distance < minDistance) {
                    minDistance = distance;
                    closestData = data;
                }
            }
        }

        if (closestData == null) {
            // 如果找不到匹配的数据点，返回默认值
            return -50.0;
        }

        return closestData.getRcsValue();
    }

    private double interpolate(double value1, double value2, double x1, double x2, double x) {
        if (x1 == x2) {
            return value1;
        }

        return value1 + (value2 - value1) * (x - x1) / (x2 - x1);
    }
}