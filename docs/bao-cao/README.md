# Khung báo cáo đồ án BodyPoint

Mở **Khung_Bao_Cao_BodyPoint.docx** bằng Microsoft Word để viết tiếp.
File được soạn theo PDF `HuongDanVietBaoCaoDoAn_CNTT.pdf` người dùng cung cấp
và yêu cầu bổ sung của giảng viên trong cuộc trao đổi. PDF gốc không được đưa lên repo.

## Ba section đã thiết lập

| Section | Phạm vi | Số trang |
| --- | --- | --- |
| 1 | Bìa chính và bìa lót | Không hiển thị số |
| 2 | Lời cảm ơn → Mục lục | i, ii, iii… bắt đầu từ i |
| 3 | Chương 1 → Tài liệu tham khảo | 1, 2, 3… bắt đầu lại từ 1 |

Các ngắt section là **Next Page**. Footer hai section có đánh số được ngắt
liên kết với section trước. Khi sửa trong Word, không xóa các Section Break.
Bật nút **¶** ở Home để nhìn thấy ngắt trang/ngắt section.

## Cách hoàn thiện

1. Điền họ tên, MSSV, giảng viên, năm nộp, tên đề tài chính thức và chèn logo trường.
   Không suy ra MSSV từ tên tài khoản GitHub.
2. Viết tiếp các đoạn gợi ý; thay hoặc xóa toàn bộ phần `[CẦN HOÀN THIỆN]` trước khi nộp.
3. Mỗi chương đã bố trí **5 trang dự trù**. Đây là khung để viết nội dung thực chất,
   chưa phải báo cáo hoàn chỉnh đủ chất lượng. Không giữ trang trống để đủ số trang.
   Có thể thay ngắt trang khi nội dung dài hơn, nhưng phải kiểm tra lại tối thiểu 5 trang/chương.
4. Thay ô `[CHÈN … TẠI ĐÂY]` bằng hình/sơ đồ/ảnh code thật; giữ caption đánh số theo chương.
   Mỗi ảnh hoặc ảnh code phải có **một đoạn mô tả ít nhất 5 dòng**. Các ví dụ trong file
   đã có 5 dòng riêng; viết lại cho khớp hình thật, không dùng chung một mô tả cho mọi hình.
   Nếu thêm hình mới, sao chép cả caption và đoạn mô tả; cập nhật số hình.
5. Dùng Heading 1/2/3 cho tiêu đề. Caption hình dùng style `ChuThichHinh`, caption bảng
   dùng `ChuThichBang`. Không xóa field mục lục/danh mục.
6. Sau khi viết xong: **Ctrl+A → F9 → Update entire table** để cập nhật mục lục,
   danh mục hình và bảng; nếu Word hỏi, đồng ý cập nhật trường của tài liệu.
   Mục lục có thể dài thêm sau cập nhật; số trang phần chính vẫn bắt đầu lại từ 1.
7. Kiểm tra bản in/PDF lần cuối: số trang, tiêu đề, bảng/hình không tràn lề, caption
   đi cùng hình, font nhất quán và đủ dòng mô tả. Font trên máy khác có thể làm đổi ngắt dòng.

Thiết lập tạm dùng A4, Times New Roman 14, giãn dòng 1,5; lề trái 3,5 cm,
phải/trên/dưới 2 cm. PDF mẫu không quy định cụ thể các thông số này;
nếu khoa có quy định riêng, ưu tiên điều chỉnh theo quy định đó.

## Nội dung kỹ thuật cần ghi đúng

- YOLO pose trong assets nhận người/khớp; không tự chứng minh động tác đúng/sai.
- Hiện tư thế được đánh giá bằng quy tắc hình học. Chưa xác minh notebook Colab được chia sẻ.
- Luồng lượt tập hiện dùng SharedPreferences; không mô tả các lớp Room còn lại như CSDL
  đang vận hành cho lượt tập nếu chưa kiểm tra. Khung vẫn giữ mục CSDL theo mẫu và giải thích điều này.
- Giãn bắp tay trước còn trong enum cho dữ liệu cũ; danh sách hoạt động hiện là giãn cổ,
  giãn vai và hai bài cổ tay. Kiểm tra lại phạm vi trước khi nộp.
- Kết quả kiểm thử mã nguồn: APK build được; 28 test gồm 24 đạt, 2 lỗi thiếu model
  phân loại, 2 bỏ qua do thiếu JNI Android TFLite trên host. Không coi đây là độ chính xác AI.
- Khả năng bám khớp liên tục, độ trễ và lỗi màu trên LDPlayer còn cần thử trên máy người dùng.

## Tạo lại khung

`python3 -m pip install python-docx` rồi chạy `python3 docs/bao-cao/tao_khung.py`
từ gốc repo. Script tạo lại file Word, **ghi đè file cùng tên**; không chạy lại sau khi
đã viết báo cáo riêng nếu chưa sao lưu bản đang làm. PDF đi kèm là bản xem trước tại
thời điểm dựng khung; cần xuất lại sau khi sửa nội dung và cập nhật mục lục.
