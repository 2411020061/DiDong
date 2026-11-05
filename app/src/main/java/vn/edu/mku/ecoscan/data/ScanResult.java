package vn.edu.mku.ecoscan.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "scan_results")
public class ScanResult {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String captureId; // e.g. "BP-1042"
    public long timestamp;
    public float confidence;
    public int landmarkCount; // 33

    // Joint Angles (Flexion / Extension)
    public float shoulderLeftAngle;
    public float shoulderRightAngle;
    public float elbowLeftAngle;
    public float elbowRightAngle;
    public float hipLeftAngle;
    public float hipRightAngle;
    public float kneeLeftAngle;
    public float kneeRightAngle;

    // Alignment / Posture
    public float shoulderTilt;
    public float hipTilt;
    public float trunkLean;
    public int bodySymmetry; // % e.g. 94
    public boolean raisedKneeDetected;

    public String mode; // "Full Body", "Upper Body", "Joint Detail"
    public String imagePath;
}