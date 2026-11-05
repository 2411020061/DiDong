# Ví dụ model nhận diện rác của dự án cũ

Các file này được giữ làm tài liệu tham khảo, không được ứng dụng giãn cổ hiện tại sử dụng. `labelmap.txt` đi cùng `ssd_mobilenet_v1.tflite`; `trash_labels.txt` đi cùng `trash_classifier.tflite`. Không đổi nội dung hai file nhãn thành `Dung/Sai`: thứ tự và số lớp của chúng thuộc các model rác, không thuộc model giãn cổ.

Nhãn của model giãn cổ nằm ở `app/src/main/assets/neck_labels.txt` theo đúng thứ tự đầu ra của notebook Colab: dòng 1 là `Dung` (chỉ số 0), dòng 2 là `Sai` (chỉ số 1). Sau khi huấn luyện, notebook tự xuất `neck_labels.txt` và `neck_stretch.tflite`. Chép hai file đó cùng nhau vào `app/src/main/assets/` khi tích hợp model; file nhãn có thể so sánh với bản đang có trước khi thay.
