package vn.edu.mku.ecoscan.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ScanResultDao {

    @Insert
    long insert(ScanResult result);

    @Query("SELECT * FROM scan_results ORDER BY timestamp DESC")
    List<ScanResult> getAll();

    @Query("SELECT * FROM scan_results WHERE id = :id LIMIT 1")
    ScanResult getById(int id);

    @Query("SELECT * FROM scan_results ORDER BY timestamp DESC LIMIT 1")
    ScanResult getLatest();

    @Query("DELETE FROM scan_results WHERE id = :id")
    void deleteById(int id);
}