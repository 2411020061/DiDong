# Thử quy trình CSV → TFLite cho 5 bài tập

1. Mở `export_pose_landmarks.ipynb` trên Colab, upload `pose_dataset_starter.zip`, rồi tải `bodypoint_pose_landmarks.csv`.
2. Mở `train.ipynb` bản mới, chạy từ trên xuống và upload CSV đó. Notebook đọc 33 điểm khớp của từng ảnh, chuẩn hóa 132 đặc trưng, rồi train **một model riêng cho mỗi bài**: `neck`, `shoulder`, `biceps`, `wrist_up`, `wrist_down`.
3. Tải `bodypoint_pose_models.zip`. Trong ZIP, mỗi bài có `<exercise>_stretch.tflite`, `<exercise>_labels.txt` và `<exercise>_model_info.json`; nhãn luôn theo thứ tự `Dung`, `Sai`.

Bộ ảnh mẫu có 29 ảnh: giãn cổ 11 đúng/10 sai; mỗi bài còn lại 1 đúng/1 sai. Những bài chỉ có một ảnh gốc mỗi nhãn **chỉ thử được đường đi xuất model**, không có tập test độc lập và không có số đo độ chính xác hợp lệ. Ảnh AI/ảnh công khai và ảnh lật không chứng minh được model hoạt động trên camera thật. Muốn đánh giá từng bài cần bổ sung nhiều ảnh gốc độc lập từ nhiều người và góc quay, đặc biệt thấy rõ hai bàn tay cho cổ tay. `pose_split_manifest.csv` trong ZIP ghi nguồn ảnh và phần chia train/test.

Ứng dụng Android hiện chỉ nạp `neck_stretch.tflite` và `neck_labels.txt`. Chép hai file giãn cổ từ ZIP vào `app/src/main/assets/` rồi build APK để thử bài cổ. Các model còn lại được xuất để thử quy trình; app vẫn dùng quy tắc hình học cho các bài này và chưa nạp các TFLite mới. Không chép các model thử nghiệm ít mẫu vào app để chấm tư thế thật.
