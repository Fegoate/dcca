public class RCSData {
    private double frequency;
    private double incidentDirection;
    private double theta;
    private double phi;
    private double rcsValue;

    public RCSData(double frequency, double incidentDirection, double theta, double phi, double rcsValue) {
        this.frequency = frequency;
        this.incidentDirection = incidentDirection;
        this.theta = theta;
        this.phi = phi;
        this.rcsValue = rcsValue;
    }

    public double getFrequency() {
        return frequency;
    }

    public double getIncidentDirection() {
        return incidentDirection;
    }

    public double getTheta() {
        return theta;
    }

    public double getPhi() {
        return phi;
    }

    public double getRcsValue() {
        return rcsValue;
    }

    @Override
    public String toString() {
        return String.format("RCSData{frequency=%.1f MHz, incidentDirection=%.1f°, theta=%.1f°, phi=%.1f°, rcsValue=%.2f dB(m²)}",
                frequency, incidentDirection, theta, phi, rcsValue);
    }
}