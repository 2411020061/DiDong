# Kiểm tra nhận người và đếm thời gian giữ tư thế

Mở dự án trong Android Studio, chạy bản debug trên LDPlayer hoặc điện thoại,
cấp quyền camera và mở bài Giãn cổ. CameraX là mặc định cả trên máy ảo;
chỉ dùng cầu nối khi đã nhập mã ghép nối. Chạm trạng thái camera rồi chọn
“Dùng CameraX” để bỏ kết nối cầu nối đã lưu. Ngồi sao cho đầu, hai tai và hai vai nằm
trong vùng camera. Nếu LDPlayer không đưa webcam vào CameraX, dùng cầu nối
webcam theo `tools/WEBCAM_LDPLAYER.md`.

- Có người: hiện khung `person` kèm độ tin cậy và các khớp nhìn thấy.
- Chưa đúng: khung vàng, hiện hướng dẫn, thời gian giữ đúng là 0 / 15 giây.
- Đúng: khung xanh lá đậm, hiện “Đúng”, đồng hồ đếm lùi từ 15 giây.
- Sai, ra khỏi hình hoặc quá 1,2 giây không có kết quả mới: xóa thời gian giữ
  liên tục; đồng hồ trở về 15 giây. Không cộng thời gian khi camera bị đứng.
- Giữ đúng đủ 15 giây: lưu kết quả và chuyển sang Theo dõi.

## Model hiện tại

`app/src/main/assets/yolo26m-pose.tflite` nhận người và 17 khớp COCO.
Input float32 `[1,3,640,640]`, RGB / 255; output `[1,56,8400]`.
Graph đã chuẩn hóa tọa độ và tính sigmoid cho xác suất; decoder không chia
tọa độ thêm cho 640 hoặc tính sigmoid lần nữa. Không tăng giả độ tin cậy khớp.
Ảnh được giữ tỉ lệ và đệm xám thành 640 × 640; tọa độ đầu ra được bỏ phần đệm
trước khi vẽ lên camera. CameraX dùng YUV_420_888, đọc row/pixel stride riêng
của từng plane. Không tự xoay ảnh 90 độ khi mới mở; nút Xoay hình dùng khi cần.
Phân tích chạy trên executor riêng với KEEP_ONLY_LATEST: bỏ frame đang chờ cũ,
không xếp hàng toàn bộ video. Model 83 MB có thể chạy chậm trên LDPlayer;
kiểm tra trên thiết bị thật trước khi kết luận về tốc độ nhận diện liên tục.
Các điểm ngón tay không có trong COCO nên không được dùng làm bằng chứng
chấm tư thế cổ tay.

Model này không phân loại đúng/sai bài tập. Bản hiện tại chấm bằng
`NeckPoseEvaluator` / `ExercisePoseEvaluator`. Muốn dùng model đã train riêng,
cần model phân loại, nhãn, kích thước tensor và đúng cách tiền xử lý lúc train.
Không thể coi 17 khớp COCO được ánh xạ thành 33 điểm là dữ liệu MediaPipe 3D
thật để đưa vào model khác mà chưa xác minh tính tương thích.

## Kiểm tra tự động

Chạy `:app:assembleDebug` và `:app:testDebugUnitTest` bằng Gradle.
`YoloOutputTest` kiểm tra cảnh không người, tọa độ chuẩn hóa và khớp ít tin cậy.
Các test timer hiện có kiểm tra tư thế sai, khoảng ngắt khung hình và đủ thời gian.

Kiểm tra cloud: APK build được; 28 test gồm 24 pass, 2 fail do thiếu
`stretch_classifier.tflite`, 2 skip do thiếu JNI Android TFLite trên host.
Chưa xác minh webcam thật hoặc LDPlayer trong cloud.
