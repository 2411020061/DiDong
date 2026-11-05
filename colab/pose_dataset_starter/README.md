# Bộ ảnh khởi đầu cho 5 bài tập

Đây là bộ **thử upload và xuất điểm MediaPipe**, chưa phải bộ train đáng tin: 29 ảnh (21 ảnh giãn cổ từ bộ cũ và 8 ảnh cho bốn bài còn lại). Ảnh cổ và cổ tay minh họa là ảnh AI, chưa được xác minh bằng camera thật. Không dùng số ảnh này để kết luận độ chính xác trên người thật.

- `neck/Dung/NghiengBen/BenTraiManHinh` và `BenPhaiManHinh` chứa ảnh nghiêng cổ **cả hai bên theo hướng thấy trên ảnh**. `neck/Dung/CheoXuong` chứa ảnh cúi chéo. `neck/Sai` chứa ảnh sai. Chúng được chép từ `neck_dataset_one_upload.zip`; notebook huấn luyện cổ cũ cũng tăng cường lật trái/phải sau khi chia train/test.
- `shoulder/Dung`: tay ngang ngực được tay kia đỡ khuỷu. `shoulder/Sai`: tư thế đứng trước khi giãn. Nguồn: [Free Exercise DB, Shoulder Stretch](https://github.com/yuhonas/free-exercise-db/tree/main/exercises/Shoulder_Stretch), giấy phép Unlicense trong `SOURCE_LICENSE.md`.
- `biceps/Dung` và `biceps/Sai`: hai khung hình cùng người từ [Free Exercise DB, Dynamic Chest Stretch](https://github.com/yuhonas/free-exercise-db/tree/main/exercises/Dynamic_Chest_Stretch). Ảnh mở hai tay ngang vai khớp hình học bài bắp tay; ảnh đưa hai tay ra trước là ví dụ sai. Đây chỉ là ảnh thử pipeline, không phải bằng chứng bài bắp tay đúng về mặt chuyên môn.
- `wrist_up/Dung` và `wrist_down/Dung`: ảnh AI minh họa một tay duỗi, tay kia kéo nhẹ các ngón lên hoặc xuống. Hai ảnh `Sai` cho thấy **một tay đưa thẳng về phía camera**, bàn tay mở, tay còn lại để trên đùi, không giữ tay. Không còn ảnh tay đưa sang ngang trong nhãn Sai.

Để train, hãy bổ sung ảnh thật có sự đồng ý của người trong ảnh: nhiều người, cả phía trái/phải, góc máy, ánh sáng và khoảng cách; mỗi bài có nhiều tư thế đúng và sai. Tách train/test theo **người tập**. Đặc biệt với cổ tay cần nhìn rõ cả hai bàn tay và chiều kéo ngón tay. MediaPipe Pose chỉ có điểm ngón tay thô; phân biệt chính xác động tác cổ tay có thể cần thêm MediaPipe Hands.

Nén các thư mục `neck`, `shoulder`, `biceps`, `wrist_up`, `wrist_down` thành ZIP để tải vào `export_pose_landmarks.ipynb`. CSV chỉ chứa đặc trưng và nhãn; ứng dụng camera chưa đọc CSV trực tiếp.
