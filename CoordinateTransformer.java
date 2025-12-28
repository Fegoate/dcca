public class CoordinateTransformer {
    // 将球面坐标转换为笛卡尔坐标
    public static double[] sphericalToCartesian(double r, double theta, double phi) {
        double[] cartesian = new double[3];
        double thetaRad = Math.toRadians(theta);
        double phiRad = Math.toRadians(phi);

        cartesian[0] = r * Math.sin(thetaRad) * Math.sin(phiRad);
        cartesian[1] = r * Math.cos(thetaRad);
        cartesian[2] = r * Math.sin(thetaRad) * Math.cos(phiRad);

        return cartesian;
    }

    // 将笛卡尔坐标转换为球面坐标
    public static double[] cartesianToSpherical(double x, double y, double z) {
        double[] spherical = new double[3];

        spherical[0] = Math.sqrt(x * x + y * y + z * z);
        spherical[1] = Math.toDegrees(Math.acos(y / spherical[0]));
        spherical[2] = Math.toDegrees(Math.atan2(x, z));

        // 确保方位角在[0, 360)范围内
        if (spherical[2] < 0) {
            spherical[2] += 360;
        }

        return spherical;
    }

    // 旋转坐标系
    public static double[] rotateCoordinates(double x, double y, double z, double thetaX, double thetaY, double thetaZ) {
        double[] rotated = new double[3];
        double tx = Math.toRadians(thetaX);
        double ty = Math.toRadians(thetaY);
        double tz = Math.toRadians(thetaZ);

        // 绕X轴旋转
        double y1 = y * Math.cos(tx) - z * Math.sin(tx);
        double z1 = y * Math.sin(tx) + z * Math.cos(tx);

        // 绕Y轴旋转
        double x2 = x * Math.cos(ty) + z1 * Math.sin(ty);
        double z2 = -x * Math.sin(ty) + z1 * Math.cos(ty);

        // 绕Z轴旋转
        double x3 = x2 * Math.cos(tz) - y1 * Math.sin(tz);
        double y3 = x2 * Math.sin(tz) + y1 * Math.cos(tz);

        rotated[0] = x3;
        rotated[1] = y3;
        rotated[2] = z2;

        return rotated;
    }
}